import { Request, Response } from 'express';
import bcrypt from 'bcrypt';
import jwt from 'jsonwebtoken';
import nodemailer from 'nodemailer';
import crypto from 'crypto';
import User from '../models/User';
import Notification from '../models/Notification';
import { AuthenticatedRequest } from '../middlewares/authMiddleware';

const getJwtSecrets = () => {
  const secret = process.env.JWT_SECRET;
  const refreshSecret = process.env.JWT_REFRESH_SECRET;
  if (!secret || !refreshSecret) {
    throw new Error('JWT_SECRET and JWT_REFRESH_SECRET must be configured in environment');
  }
  return { secret, refreshSecret };
};

// Password strength validation helper
const isStrongPassword = (pwd: string): { valid: boolean; reason?: string } => {
  if (pwd.length < 8) {
    return { valid: false, reason: 'Password must be at least 8 characters long' };
  }
  if (!/[A-Z]/.test(pwd)) {
    return { valid: false, reason: 'Password must contain at least one uppercase letter' };
  }
  if (!/[a-z]/.test(pwd)) {
    return { valid: false, reason: 'Password must contain at least one lowercase letter' };
  }
  if (!/[0-9]/.test(pwd)) {
    return { valid: false, reason: 'Password must contain at least one number' };
  }
  if (!/[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(pwd)) {
    return { valid: false, reason: 'Password must contain at least one special character' };
  }
  return { valid: true };
};

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
    console.warn(`[SMTP-WARNING] SMTP credentials are not configured in environment variables.`);
    return false;
  }

  try {
    await transporter.sendMail({
      from: process.env.SMTP_FROM || `"Pakistan Railways Companion" <noreply@pakistan-railways.example.com>`,
      to,
      subject,
      text,
      html: html || text,
    });
    return true;
  } catch (error: any) {
    console.error(`[SMTP-ERROR] Failed to send email to ${to}:`, error.message);
    return false;
  }
};

export const signUp = async (req: Request, res: Response) => {
  try {
    const { firstName, lastName, email, phone, passwordHash } = req.body;

    if (!firstName || !lastName || !email || !phone || !passwordHash) {
      return res.status(400).json({ 
        success: false, 
        message: 'All fields (firstName, lastName, email, phone, passwordHash) are required' 
      });
    }

    const cleanEmail = email.trim().toLowerCase();
    const cleanPhone = phone.trim();

    const pwdCheck = isStrongPassword(passwordHash);
    if (!pwdCheck.valid) {
      return res.status(400).json({ success: false, message: pwdCheck.reason });
    }

    const existingUser = await User.findOne({ 
      $or: [
        { email: cleanEmail }, 
        { phone: cleanPhone }
      ] 
    });

    if (existingUser) {
      const matchReason = existingUser.email === cleanEmail ? 'Email already registered' : 'Phone number already registered';
      return res.status(400).json({ success: false, message: matchReason });
    }

    const salt = await bcrypt.genSalt(12);
    const hashedPassword = await bcrypt.hash(passwordHash, salt);

    // Cryptographically secure numeric OTP code
    const otpBuffer = crypto.randomBytes(2);
    const otpCode = (1000 + (otpBuffer.readUInt16BE(0) % 9000)).toString();
    const otpExpiry = new Date(Date.now() + 15 * 60 * 1000);

    const newUser = new User({
      firstName,
      lastName,
      email: cleanEmail,
      phone: cleanPhone,
      passwordHash: hashedPassword,
      role: 'passenger',
      isEmailVerified: false,
      otpCode,
      otpExpiry,
      otpAttempts: 0,
      lastOtpSentAt: new Date(),
      failedLoginAttempts: 0,
      passwordHistory: [hashedPassword],
      tokenVersion: 0
    });

    await newUser.save();

    const mailText = `Welcome to Pakistan Railways Companion app! Use code ${otpCode} to verify your email address. Valid for 15 minutes.`;
    await sendEmail(cleanEmail, "Verify Your Email Address", mailText);

    await Notification.create({
      title: "Welcome aboard!",
      message: `Your companion account has been pre-registered. Use code ${otpCode} to verify your email.`,
      category: "broadcast",
      recipientEmail: cleanEmail
    });

    return res.status(201).json({
      success: true,
      message: 'Registration successful. Verification OTP sent to registered email.',
      email: cleanEmail
    });
  } catch (error: any) {
    return res.status(500).json({ success: false, message: 'Server error during registration' });
  }
};

export const verifyOtp = async (req: Request, res: Response) => {
  try {
    const { email, otpCode } = req.body;
    if (!email || !otpCode) {
      return res.status(400).json({ success: false, message: 'Email and OTP code are required' });
    }

    const cleanEmail = email.trim().toLowerCase();
    const user = await User.findOne({ email: cleanEmail });

    if (!user) {
      return res.status(404).json({ success: false, message: 'Account not found' });
    }

    if (user.isEmailVerified) {
      return res.status(200).json({ success: true, message: 'Email is already verified.' });
    }

    // OTP Rate limiting and expiry check
    if (user.otpAttempts >= 5) {
      return res.status(429).json({ success: false, message: 'Too many invalid OTP attempts. Please request a new OTP.' });
    }

    if (!user.otpCode || user.otpCode !== otpCode || !user.otpExpiry || user.otpExpiry < new Date()) {
      user.otpAttempts += 1;
      await user.save();
      return res.status(400).json({ success: false, message: 'Invalid or expired verification OTP code.' });
    }

    user.isEmailVerified = true;
    user.otpCode = undefined;
    user.otpExpiry = undefined;
    user.otpAttempts = 0;

    const { secret, refreshSecret } = getJwtSecrets();

    const accessToken = jwt.sign(
      { id: user._id, email: user.email, role: user.role, tokenVersion: user.tokenVersion },
      secret,
      { expiresIn: '15m' }
    );

    const refreshToken = jwt.sign(
      { id: user._id, tokenVersion: user.tokenVersion },
      refreshSecret,
      { expiresIn: '7d' }
    );

    user.refreshToken = refreshToken;
    await user.save();

    return res.status(200).json({
      success: true,
      message: 'Email address verified successfully.',
      token: accessToken,
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
    return res.status(500).json({ success: false, message: 'Server error during verification' });
  }
};

export const login = async (req: Request, res: Response) => {
  try {
    const { identifier, passwordHash } = req.body;
    if (!identifier || !passwordHash) {
      return res.status(400).json({ success: false, message: 'Identifier (Email/Phone) and password are required' });
    }

    const cleanIdentifier = identifier.trim().toLowerCase();
    const user = await User.findOne({ 
      $or: [
        { email: cleanIdentifier }, 
        { phone: cleanIdentifier }
      ] 
    });

    if (!user) {
      return res.status(401).json({ success: false, message: 'Invalid credentials' });
    }

    // Account lockout enforcement
    if (user.lockUntil && user.lockUntil > new Date()) {
      const remainingMins = Math.ceil((user.lockUntil.getTime() - Date.now()) / 60000);
      return res.status(423).json({ success: false, message: `Account is temporarily locked. Try again in ${remainingMins} minutes.` });
    }

    const isMatch = await bcrypt.compare(passwordHash, user.passwordHash);
    if (!isMatch) {
      user.failedLoginAttempts += 1;
      if (user.failedLoginAttempts >= 5) {
        user.lockUntil = new Date(Date.now() + 15 * 60 * 1000); // Lock for 15 minutes
        user.failedLoginAttempts = 0;
      }
      await user.save();
      return res.status(401).json({ success: false, message: 'Invalid credentials' });
    }

    if (!user.isEmailVerified) {
      return res.status(403).json({ success: false, message: 'Please verify your email address before logging in.' });
    }

    // Reset lock counter on successful login
    user.failedLoginAttempts = 0;
    user.lockUntil = undefined;

    const { secret, refreshSecret } = getJwtSecrets();

    const accessToken = jwt.sign(
      { id: user._id, email: user.email, role: user.role, tokenVersion: user.tokenVersion },
      secret,
      { expiresIn: '15m' }
    );

    const refreshToken = jwt.sign(
      { id: user._id, tokenVersion: user.tokenVersion },
      refreshSecret,
      { expiresIn: '7d' }
    );

    user.refreshToken = refreshToken;
    await user.save();

    return res.status(200).json({
      success: true,
      token: accessToken,
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
    return res.status(500).json({ success: false, message: 'Server error during authentication' });
  }
};

export const refreshSessionToken = async (req: Request, res: Response) => {
  try {
    const { refreshToken } = req.body;
    if (!refreshToken) {
      return res.status(400).json({ success: false, message: 'Refresh token is required' });
    }

    const { secret, refreshSecret } = getJwtSecrets();
    const decoded = jwt.verify(refreshToken, refreshSecret) as { id: string; tokenVersion?: number };

    const user = await User.findById(decoded.id);
    if (!user || user.refreshToken !== refreshToken || user.tokenVersion !== decoded.tokenVersion) {
      return res.status(403).json({ success: false, message: 'Invalid or revoked refresh token session' });
    }

    // Token Rotation
    const newAccessToken = jwt.sign(
      { id: user._id, email: user.email, role: user.role, tokenVersion: user.tokenVersion },
      secret,
      { expiresIn: '15m' }
    );

    const newRefreshToken = jwt.sign(
      { id: user._id, tokenVersion: user.tokenVersion },
      refreshSecret,
      { expiresIn: '7d' }
    );

    user.refreshToken = newRefreshToken;
    await user.save();

    return res.status(200).json({
      success: true,
      token: newAccessToken,
      accessToken: newAccessToken,
      refreshToken: newRefreshToken
    });
  } catch (error: any) {
    return res.status(403).json({ success: false, message: 'Session expired. Please log in again.' });
  }
};

export const logout = async (req: AuthenticatedRequest, res: Response) => {
  try {
    if (req.user) {
      const user = await User.findById(req.user.id);
      if (user) {
        user.refreshToken = undefined;
        await user.save();
      }
    }
    return res.status(200).json({ success: true, message: 'Logged out successfully' });
  } catch (err) {
    return res.status(500).json({ success: false, message: 'Logout failed' });
  }
};

export const logoutAllDevices = async (req: AuthenticatedRequest, res: Response) => {
  try {
    if (req.user) {
      const user = await User.findById(req.user.id);
      if (user) {
        user.tokenVersion += 1; // Increment version to invalidate ALL outstanding tokens
        user.refreshToken = undefined;
        await user.save();
      }
    }
    return res.status(200).json({ success: true, message: 'Logged out from all devices successfully' });
  } catch (err) {
    return res.status(500).json({ success: false, message: 'Failed to revoke sessions across devices' });
  }
};

export const forgotPassword = async (req: Request, res: Response) => {
  try {
    const { email } = req.body;
    if (!email) {
      return res.status(400).json({ success: false, message: 'Email address is required' });
    }

    const cleanEmail = email.trim().toLowerCase();
    const user = await User.findOne({ email: cleanEmail });

    // Prevent user enumeration by returning consistent success message
    if (!user) {
      return res.status(200).json({
        success: true,
        message: 'If a matching account exists, a password reset code has been sent.'
      });
    }

    // Cooldown check: 1 minute between OTP requests
    if (user.lastOtpSentAt && (Date.now() - user.lastOtpSentAt.getTime()) < 60000) {
      return res.status(429).json({ success: false, message: 'Please wait before requesting another reset code.' });
    }

    const otpBuffer = crypto.randomBytes(2);
    const otpCode = (1000 + (otpBuffer.readUInt16BE(0) % 9000)).toString();
    user.otpCode = otpCode;
    user.otpExpiry = new Date(Date.now() + 15 * 60 * 1000);
    user.otpAttempts = 0;
    user.lastOtpSentAt = new Date();
    await user.save();

    const resetText = `Your companion app password reset code is ${otpCode}. Valid for 15 minutes.`;
    await sendEmail(cleanEmail, "Reset Your Password", resetText);

    return res.status(200).json({
      success: true,
      message: 'If a matching account exists, a password reset code has been sent.'
    });
  } catch (error: any) {
    return res.status(500).json({ success: false, message: 'Server error processing request' });
  }
};

export const resetPassword = async (req: Request, res: Response) => {
  try {
    const { email, otpCode, newPasswordHash } = req.body;
    if (!email || !otpCode || !newPasswordHash) {
      return res.status(400).json({ success: false, message: 'Email, OTP, and new password are required' });
    }

    const pwdCheck = isStrongPassword(newPasswordHash);
    if (!pwdCheck.valid) {
      return res.status(400).json({ success: false, message: pwdCheck.reason });
    }

    const cleanEmail = email.trim().toLowerCase();
    const user = await User.findOne({ email: cleanEmail });

    if (!user) {
      return res.status(400).json({ success: false, message: 'Invalid request' });
    }

    if (user.otpAttempts >= 5) {
      return res.status(429).json({ success: false, message: 'Too many invalid attempts. Request a new code.' });
    }

    if (user.otpCode !== otpCode || !user.otpExpiry || user.otpExpiry < new Date()) {
      user.otpAttempts += 1;
      await user.save();
      return res.status(400).json({ success: false, message: 'Invalid or expired reset code' });
    }

    // Check Password History (prevent re-using previous passwords)
    for (const oldHash of user.passwordHistory || []) {
      const isReused = await bcrypt.compare(newPasswordHash, oldHash);
      if (isReused) {
        return res.status(400).json({ success: false, message: 'You cannot reuse a recent password.' });
      }
    }

    const salt = await bcrypt.genSalt(12);
    const newHash = await bcrypt.hash(newPasswordHash, salt);

    user.passwordHash = newHash;
    user.otpCode = undefined;
    user.otpExpiry = undefined;
    user.otpAttempts = 0;
    user.refreshToken = undefined;
    user.tokenVersion += 1; // Revoke all active sessions
    
    user.passwordHistory.push(newHash);
    if (user.passwordHistory.length > 5) {
      user.passwordHistory.shift(); // Keep last 5 passwords
    }

    await user.save();

    return res.status(200).json({
      success: true,
      message: 'Password updated successfully. Please log in with your new password.'
    });
  } catch (error: any) {
    return res.status(500).json({ success: false, message: 'Server error resetting password' });
  }
};

export const registerFcmToken = async (req: AuthenticatedRequest, res: Response) => {
  try {
    const { fcmToken } = req.body;
    if (!fcmToken || typeof fcmToken !== 'string') {
      return res.status(400).json({ success: false, message: 'Valid FCM token required' });
    }
    if (req.user) {
      await User.findByIdAndUpdate(req.user.id, { fcmToken });
    }
    return res.status(200).json({
      success: true,
      message: 'FCM token registered successfully.'
    });
  } catch (error: any) {
    return res.status(500).json({ success: false, message: 'Server error registering FCM token' });
  }
};
