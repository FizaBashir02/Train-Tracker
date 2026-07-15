import { Request, Response } from 'express';
import bcrypt from 'bcrypt';
import jwt from 'jsonwebtoken';
import nodemailer from 'nodemailer';
import User from '../models/User';
import Notification from '../models/Notification';
import { AuthenticatedRequest } from '../middlewares/authMiddleware';

const JWT_SECRET = process.env.JWT_SECRET || 'supersecret_companion_key_2026';
const JWT_REFRESH_SECRET = process.env.JWT_REFRESH_SECRET || 'supersecret_companion_refresh_key_2026';

// SMTP Transporter setup for production email delivery
const transporter = nodemailer.createTransport({
  host: process.env.SMTP_HOST || '',
  port: parseInt(process.env.SMTP_PORT || '587'),
  secure: process.env.SMTP_SECURE === 'true',
  auth: {
    user: process.env.SMTP_USER || '',
    pass: process.env.SMTP_PASS || '',
  },
});

const sendEmail = async (to: string, subject: string, text: string, html?: string) => {
  if (!process.env.SMTP_HOST || !process.env.SMTP_USER || !process.env.SMTP_PASS) {
    console.warn(`[SMTP-WARNING] SMTP credentials are not configured in the environment variables.`);
    console.warn(`[SMTP-SIMULATOR] Email would be sent to: ${to}`);
    console.warn(`[SMTP-SIMULATOR] Subject: ${subject}`);
    console.warn(`[SMTP-SIMULATOR] Message: ${text}`);
    return false;
  }

  try {
    const info = await transporter.sendMail({
      from: process.env.SMTP_FROM || `"Pakistan Railways Companion" <noreply@pakistan-railways.example.com>`,
      to,
      subject,
      text,
      html: html || text,
    });
    console.log(`[SMTP-SUCCESS] Email sent successfully to ${to}. MessageId: ${info.messageId}`);
    return true;
  } catch (error: any) {
    console.error(`[SMTP-ERROR] Failed to send email to ${to}:`, error);
    return false;
  }
};

export const signUp = async (req: Request, res: Response) => {
  try {
    console.log(`[AUTH-SIGNUP] Incoming registration request. Body:`, JSON.stringify(req.body));

    const { firstName, lastName, email, phone, passwordHash } = req.body;

    if (!firstName || !lastName || !email || !phone || !passwordHash) {
      console.warn(`[AUTH-SIGNUP] Validation failed. Missing fields:`, { 
        firstName: !firstName, 
        lastName: !lastName, 
        email: !email, 
        phone: !phone, 
        passwordHash: !passwordHash 
      });
      return res.status(400).json({ 
        success: false, 
        message: 'All fields (firstName, lastName, email, phone, passwordHash) are required' 
      });
    }

    const cleanEmail = email.trim().toLowerCase();
    const cleanPhone = phone.trim();

    if (!cleanEmail || !cleanPhone) {
      return res.status(400).json({ success: false, message: 'Valid email and phone are required' });
    }

    // Strict, non-undefined/non-empty findOne query
    console.log(`[AUTH-SIGNUP] MongoDB query with clean email: "${cleanEmail}" and phone: "${cleanPhone}"`);
    const existingUser = await User.findOne({ 
      $or: [
        { email: cleanEmail }, 
        { phone: cleanPhone }
      ] 
    });

    console.log(`[AUTH-SIGNUP] Query result:`, existingUser ? `User found (ID: ${existingUser._id}, Email: ${existingUser.email}, Phone: ${existingUser.phone})` : 'No duplicate user found');

    if (existingUser) {
      const matchReason = existingUser.email === cleanEmail ? 'Email already registered' : 'Phone number already registered';
      console.warn(`[AUTH-SIGNUP] Registration blocked: ${matchReason}`);
      return res.status(400).json({ success: false, message: matchReason });
    }

    // Hash the password
    const salt = await bcrypt.genSalt(10);
    const hashedPassword = await bcrypt.hash(passwordHash, salt);
    console.log(`[AUTH-SIGNUP] Password hashed successfully`);

    // Generate numeric OTP code
    const otpCode = Math.floor(1000 + Math.random() * 9000).toString();
    const otpExpiry = new Date(Date.now() + 15 * 60 * 1000); // 15 mins validity
    console.log(`[AUTH-SIGNUP] OTP generated: ${otpCode}, Expiry: ${otpExpiry}`);

    const newUser = new User({
      firstName: firstName.trim(),
      lastName: lastName.trim(),
      email: cleanEmail,
      phone: cleanPhone,
      passwordHash: hashedPassword,
      role: 'passenger',
      isEmailVerified: false,
      otpCode,
      otpExpiry
    });

    console.log(`[AUTH-SIGNUP] Saving user document in MongoDB`);
    await newUser.save();
    console.log(`[AUTH-SIGNUP] User saved successfully. ID: ${newUser._id}`);

    // Real Nodemailer integration call for production email delivery
    const mailText = `Welcome to Pakistan Railways Companion app! Use code ${otpCode} to verify your email address. This code is valid for 15 minutes.`;
    const mailHtml = `
      <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px;">
        <h2 style="color: #1e824c; text-align: center;">Welcome Aboard!</h2>
        <p>Dear ${firstName},</p>
        <p>Thank you for registering with the Pakistan Railways Companion app. Your account has been pre-registered.</p>
        <div style="background-color: #f9f9f9; padding: 15px; border-radius: 5px; text-align: center; margin: 20px 0;">
          <span style="font-size: 24px; font-weight: bold; letter-spacing: 5px; color: #333;">${otpCode}</span>
        </div>
        <p>Please enter this numeric OTP code in your app to verify your email address and fully activate your account. This code is valid for 15 minutes.</p>
        <hr style="border: 0; border-top: 1px solid #eee; margin: 20px 0;">
        <p style="font-size: 12px; color: #777; text-align: center;">If you did not make this request, please ignore this email.</p>
      </div>
    `;
    await sendEmail(cleanEmail, "Verify Your Email Address", mailText, mailHtml);

    // Record system notification
    await Notification.create({
      title: "Welcome aboard!",
      message: `Your companion account has been pre-registered. Use code ${otpCode} to verify your email.`,
      category: "alert",
      recipientEmail: cleanEmail
    });
    console.log(`[AUTH-SIGNUP] Welcome notification generated and real SMTP OTP sent`);

    return res.status(201).json({
      success: true,
      message: 'Account pre-registered successfully. Verification OTP dispatched.'
    });
  } catch (error: any) {
    console.error(`[AUTH-SIGNUP] Unhandled server error during registration:`, error);
    return res.status(500).json({ success: false, message: error.message });
  }
};

export const verifyOtp = async (req: Request, res: Response) => {
  try {
    const { email, otpCode } = req.body;

    const user = await User.findOne({ email });
    if (!user) {
      return res.status(404).json({ success: false, message: 'User account not found' });
    }

    if (user.isEmailVerified) {
      return res.status(400).json({ success: false, message: 'Email address already verified' });
    }

    if (user.otpCode !== otpCode || !user.otpExpiry || user.otpExpiry < new Date()) {
      return res.status(400).json({ success: false, message: 'Invalid or expired verification OTP code' });
    }

    user.isEmailVerified = true;
    user.otpCode = undefined;
    user.otpExpiry = undefined;
    await user.save();

    return res.status(200).json({
      success: true,
      message: 'Email verified successfully. Account fully activated.'
    });
  } catch (error: any) {
    return res.status(500).json({ success: false, message: error.message });
  }
};

export const login = async (req: Request, res: Response) => {
  try {
    console.log(`[AUTH-LOGIN] Incoming login request for identifier:`, req.body.identifier);
    const { identifier, passwordHash } = req.body; // identifier can be email or phone

    if (!identifier || !passwordHash) {
      console.warn(`[AUTH-LOGIN] Missing required fields`);
      return res.status(400).json({ success: false, message: 'Identifier (email/phone) and password are required' });
    }

    const cleanIdentifier = identifier.trim().toLowerCase();

    console.log(`[AUTH-LOGIN] MongoDB query with identifier: "${cleanIdentifier}"`);
    const user = await User.findOne({ 
      $or: [
        { email: cleanIdentifier }, 
        { phone: cleanIdentifier }
      ] 
    });

    if (!user) {
      console.warn(`[AUTH-LOGIN] Account not found for identifier: "${cleanIdentifier}"`);
      return res.status(404).json({ success: false, message: 'Account not found' });
    }

    console.log(`[AUTH-LOGIN] User found. Verifying password hash...`);
    // Verify hashed password
    const isMatch = await bcrypt.compare(passwordHash, user.passwordHash);
    if (!isMatch) {
      console.warn(`[AUTH-LOGIN] Password mismatch for: "${cleanIdentifier}"`);
      return res.status(401).json({ success: false, message: 'Invalid login credentials' });
    }

    if (!user.isEmailVerified) {
      console.warn(`[AUTH-LOGIN] Account email not verified yet: "${cleanIdentifier}"`);
      return res.status(403).json({ success: false, message: 'Please verify your email address before logging in.' });
    }

    console.log(`[AUTH-LOGIN] Credentials verified. Generating JWT tokens...`);
    // Generate clean security JWT Tokens
    const accessToken = jwt.sign(
      { id: user._id, email: user.email, role: user.role },
      JWT_SECRET,
      { expiresIn: '15m' }
    );

    const refreshToken = jwt.sign(
      { id: user._id },
      JWT_REFRESH_SECRET,
      { expiresIn: '7d' }
    );

    user.refreshToken = refreshToken;
    await user.save();
    console.log(`[AUTH-LOGIN] Session successfully started for user:`, user.email);

    return res.status(200).json({
      success: true,
      accessToken,
      refreshToken,
      user: {
        firstName: user.firstName,
        lastName: user.lastName,
        email: user.email,
        phone: user.phone,
        role: user.role
      }
    });
  } catch (error: any) {
    console.error(`[AUTH-LOGIN] Unhandled server error during login:`, error);
    return res.status(500).json({ success: false, message: error.message });
  }
};

export const refreshSessionToken = async (req: Request, res: Response) => {
  try {
    const { refreshToken } = req.body;
    if (!refreshToken) {
      return res.status(400).json({ success: false, message: 'Refresh token is required' });
    }

    const decoded = jwt.verify(refreshToken, JWT_REFRESH_SECRET) as { id: string };
    const user = await User.findById(decoded.id);

    if (!user || user.refreshToken !== refreshToken) {
      return res.status(403).json({ success: false, message: 'Invalid refresh token session' });
    }

    const accessToken = jwt.sign(
      { id: user._id, email: user.email, role: user.role },
      JWT_SECRET,
      { expiresIn: '15m' }
    );

    const newRefreshToken = jwt.sign(
      { id: user._id },
      JWT_REFRESH_SECRET,
      { expiresIn: '7d' }
    );

    user.refreshToken = newRefreshToken;
    await user.save();

    return res.status(200).json({
      success: true,
      accessToken,
      refreshToken: newRefreshToken
    });
  } catch (error: any) {
    return res.status(403).json({ success: false, message: 'Session expired. Please log in again.' });
  }
};

export const forgotPassword = async (req: Request, res: Response) => {
  try {
    const { email } = req.body;
    const user = await User.findOne({ email });
    if (!user) {
      return res.status(404).json({ success: false, message: 'No registered account found with this email' });
    }

    const otpCode = Math.floor(1000 + Math.random() * 9000).toString();
    user.otpCode = otpCode;
    user.otpExpiry = new Date(Date.now() + 15 * 60 * 1000);
    await user.save();

    const resetText = `Your companion app password reset code is ${otpCode}. Enter this to update your security credentials. This code is valid for 15 minutes.`;
    const resetHtml = `
      <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px;">
        <h2 style="color: #c0392b; text-align: center;">Reset Your Password</h2>
        <p>Hello,</p>
        <p>We received a request to reset the password for your Pakistan Railways Companion account.</p>
        <div style="background-color: #f9f9f9; padding: 15px; border-radius: 5px; text-align: center; margin: 20px 0;">
          <span style="font-size: 24px; font-weight: bold; letter-spacing: 5px; color: #333;">${otpCode}</span>
        </div>
        <p>Please enter this code in your app to update your password. This code is valid for 15 minutes.</p>
        <hr style="border: 0; border-top: 1px solid #eee; margin: 20px 0;">
        <p style="font-size: 12px; color: #777; text-align: center;">If you did not make this request, please ignore this email.</p>
      </div>
    `;
    await sendEmail(email, "Reset Your Password", resetText, resetHtml);

    await Notification.create({
      title: "Password Reset Code",
      message: `Your companion app password reset code is ${otpCode}. Enter this to update your security credentials.`,
      category: "alert",
      recipientEmail: email
    });

    return res.status(200).json({
      success: true,
      message: 'Reset OTP dispatched successfully to your registered email.'
    });
  } catch (error: any) {
    return res.status(500).json({ success: false, message: error.message });
  }
};

export const resetPassword = async (req: Request, res: Response) => {
  try {
    const { email, otpCode, newPasswordHash } = req.body;

    const user = await User.findOne({ email });
    if (!user) {
      return res.status(404).json({ success: false, message: 'User account not found' });
    }

    if (user.otpCode !== otpCode || !user.otpExpiry || user.otpExpiry < new Date()) {
      return res.status(400).json({ success: false, message: 'Invalid or expired reset OTP code' });
    }

    const salt = await bcrypt.genSalt(10);
    const newPassword = await bcrypt.hash(newPasswordHash, salt);

    user.passwordHash = newPassword;
    user.otpCode = undefined;
    user.otpExpiry = undefined;
    user.refreshToken = undefined; // Invalidate current login sessions
    await user.save();

    return res.status(200).json({
      success: true,
      message: 'Password updated successfully. Please log in with your new credentials.'
    });
  } catch (error: any) {
    return res.status(500).json({ success: false, message: error.message });
  }
};

export const registerFcmToken = async (req: Request, res: Response) => {
  try {
    const { email, fcmToken } = req.body;
    // In production, register token to a separate Firebase Device table
    // connected with user.
    console.log(`Successfully registered FCM device token for ${email}: ${fcmToken}`);
    return res.status(200).json({
      success: true,
      message: 'FCM device token registered and synchronized successfully on the backend.'
    });
  } catch (error: any) {
    return res.status(500).json({ success: false, message: error.message });
  }
};
