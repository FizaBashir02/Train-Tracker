import { Request, Response } from 'express';
import { validationResult } from 'express-validator';
import bcrypt from 'bcrypt';
import jwt from 'jsonwebtoken';
import nodemailer from 'nodemailer';
import crypto from 'crypto';
import User from '../models/User';
import Notification from '../models/Notification';
import { AuthenticatedRequest } from '../middlewares/authMiddleware';

const getJwtSecrets = () => {
  const secret = process.env.JWT_SECRET || 'fallback_jwt_secret_key_railway_production_2026';
  const refreshSecret = process.env.JWT_REFRESH_SECRET || 'fallback_jwt_refresh_secret_key_railway_production_2026';
  return { secret, refreshSecret };
};

// Helper function to safely redact sensitive data from request body logging
const getSafeBody = (body: any) => {
  if (!body || typeof body !== 'object') return body;
  const safe = { ...body };
  for (const key of Object.keys(safe)) {
    if (/password/i.test(key) || /secret/i.test(key) || /token/i.test(key)) {
      safe[key] = '[REDACTED]';
    }
  }
  return safe;
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

// Helper function to verify if SMTP credentials are configured
const isSmtpConfigured = (): boolean => {
  return Boolean(process.env.SMTP_HOST && process.env.SMTP_USER && process.env.SMTP_PASS);
};

// Safely create SMTP transporter instance
const createSmtpTransporter = () => {
  const host = process.env.SMTP_HOST || '';
  const port = parseInt(process.env.SMTP_PORT || '587', 10);
  const secure = process.env.SMTP_SECURE === 'true';
  const user = process.env.SMTP_USER || '';
  const pass = process.env.SMTP_PASS || '';

  return nodemailer.createTransport({
    host,
    port,
    secure,
    auth: user || pass ? { user, pass } : undefined,
    tls: {
      rejectUnauthorized: false
    }
  });
};

const sendEmail = async (
  to: string, 
  subject: string, 
  text: string, 
  html?: string,
  maxRetries = 3
): Promise<{ success: boolean; message?: string; error?: any }> => {
  if (!isSmtpConfigured()) {
    console.warn(`[SMTP-WARNING] SMTP credentials are not configured in environment variables. Email to ${to} skipped.`);
    return { success: false, message: 'SMTP credentials are not configured in environment.' };
  }

  let attempt = 0;
  let lastError: any = null;

  while (attempt < maxRetries) {
    attempt++;
    try {
      const transporter = createSmtpTransporter();
      await transporter.sendMail({
        from: process.env.SMTP_FROM || `"Pakistan Railways Companion" <noreply@pakistan-railways.example.com>`,
        to,
        subject,
        text,
        html: html || text,
      });
      console.log(`[SMTP-SUCCESS] Email successfully sent to ${to} (Attempt ${attempt}/${maxRetries})`);
      return { success: true };
    } catch (error: any) {
      lastError = error;
      console.error(`[SMTP-ERROR] Attempt ${attempt}/${maxRetries} failed for email to ${to}: ${error?.code || ''} ${error?.message || error}`, error);
      if (attempt < maxRetries) {
        await new Promise((resolve) => setTimeout(resolve, 1000));
      }
    }
  }

  return { success: false, message: lastError?.message || 'Failed to send email after multiple retries', error: lastError };
};

export const signUp = async (req: Request, res: Response) => {
  try {
    console.log('[AUTH] signUp request body:', getSafeBody(req.body));

    const valErrors = validationResult(req);
    if (!valErrors.isEmpty()) {
      console.error('[VALIDATION-ERROR] signUp validation errors:', JSON.stringify(valErrors.array()));
      return res.status(400).json({
        success: false,
        message: 'Validation failed',
        errors: valErrors.array(),
        fieldErrors: valErrors.mapped()
      });
    }

    const firstName = req.body.firstName || req.body.first_name;
    const lastName = req.body.lastName || req.body.last_name;
    const email = req.body.email;
    const phone = req.body.phone || req.body.phoneNumber || req.body.phone_number;
    const rawPassword = req.body.password || req.body.passwordHash;

    const fieldErrors: Record<string, string> = {};
    if (!firstName || !firstName.trim()) fieldErrors.firstName = 'First name is required';
    if (!lastName || !lastName.trim()) fieldErrors.lastName = 'Last name is required';
    if (!email || !email.trim()) fieldErrors.email = 'Email address is required';
    if (!phone || !phone.trim()) fieldErrors.phone = 'Phone number is required';
    if (!rawPassword) fieldErrors.password = 'Password is required';

    if (Object.keys(fieldErrors).length > 0) {
      console.error('[VALIDATION-ERROR] signUp missing required fields:', fieldErrors);
      return res.status(400).json({
        success: false,
        message: 'All required fields (firstName, lastName, email, phone, password) must be provided',
        errors: fieldErrors,
        fieldErrors
      });
    }

    const cleanEmail = email.trim().toLowerCase();
    const cleanPhone = phone.trim();
    const strippedPhone = cleanPhone.replace(/[\s\-]/g, '');

    const isEmailValid = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(cleanEmail);
    if (!isEmailValid) {
      const errors = { email: 'Invalid email address format' };
      return res.status(400).json({
        success: false,
        message: 'Invalid email address format',
        errors,
        fieldErrors: errors
      });
    }

    const isPakistaniPhone = /^((\+923|923|03)\d{9})$/.test(strippedPhone);
    if (!isPakistaniPhone) {
      const errors = { phone: 'Invalid Pakistani mobile number format (e.g. 03001234567 or +923001234567)' };
      return res.status(400).json({
        success: false,
        message: 'Invalid Pakistani mobile number format',
        errors,
        fieldErrors: errors
      });
    }

    if (rawPassword.length < 8) {
      const errors = { password: 'Password must be at least 8 characters' };
      return res.status(400).json({
        success: false,
        message: 'Password must be at least 8 characters',
        errors,
        fieldErrors: errors
      });
    }

    const existingEmail = await User.findOne({ email: cleanEmail });
    if (existingEmail) {
      const errors = { email: 'Email already registered' };
      return res.status(400).json({
        success: false,
        message: 'Email already registered',
        errors,
        fieldErrors: errors
      });
    }

    const existingPhone = await User.findOne({ 
      $or: [
        { phone: cleanPhone },
        { phone: strippedPhone }
      ]
    });
    if (existingPhone) {
      const errors = { phone: 'Phone number already registered' };
      return res.status(400).json({
        success: false,
        message: 'Phone number already registered',
        errors,
        fieldErrors: errors
      });
    }

    const salt = await bcrypt.genSalt(12);
    const hashedPassword = await bcrypt.hash(rawPassword, salt);

    let userRole = (req.body.role || 'USER').toString().trim();
    const validRoles = ['USER', 'user', 'passenger', 'admin', 'conductor'];
    if (!userRole || !validRoles.includes(userRole)) {
      userRole = 'USER';
    }

    const newUser = new User({
      firstName: firstName.trim(),
      lastName: lastName.trim(),
      email: cleanEmail,
      phone: cleanPhone,
      passwordHash: hashedPassword,
      role: userRole,
      isVerified: true,
      isActive: true,
      isEmailVerified: true,
      otpAttempts: 0,
      failedLoginAttempts: 0,
      passwordHistory: [hashedPassword],
      tokenVersion: 0
    });

    await newUser.save();

    try {
      await Notification.create({
        title: "Welcome aboard!",
        message: `Your companion account has been registered successfully.`,
        category: "broadcast",
        recipientEmail: cleanEmail
      });
    } catch (notifErr: any) {
      console.warn(`[NOTIF-WARN] Failed to create welcome notification for ${cleanEmail}:`, notifErr?.message || notifErr);
    }

    return res.status(201).json({
      success: true,
      message: 'Account created successfully'
    });
  } catch (error: any) {
    console.error('[AUTH-ERROR] Exception in signUp:', error?.stack || error);
    return res.status(500).json({ 
      success: false, 
      message: 'Server error during registration', 
      error: error?.message || String(error),
      stack: error?.stack 
    });
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
      { id: user._id, email: user.email, role: user.role, tokenVersion: user.tokenVersion || 0 },
      secret,
      { expiresIn: '15m' }
    );

    const refreshToken = jwt.sign(
      { id: user._id, tokenVersion: user.tokenVersion || 0 },
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
    console.log('[AUTH] login request body:', getSafeBody(req.body));

    const valErrors = validationResult(req);
    if (!valErrors.isEmpty()) {
      console.error('[VALIDATION-ERROR] login validation errors:', JSON.stringify(valErrors.array()));
      return res.status(400).json({
        success: false,
        message: 'Validation failed',
        errors: valErrors.array(),
        fieldErrors: valErrors.mapped()
      });
    }

    const identifier = req.body.identifier || req.body.email || req.body.phone || req.body.username;
    const rawPassword = req.body.password || req.body.passwordHash;

    const fieldErrors: Record<string, string> = {};
    if (!identifier) fieldErrors.identifier = 'Identifier (Email/Phone) is required';
    if (!rawPassword) fieldErrors.password = 'Password is required';

    if (Object.keys(fieldErrors).length > 0) {
      console.error('[VALIDATION-ERROR] login missing required fields:', fieldErrors);
      return res.status(400).json({
        success: false,
        message: 'Identifier (Email/Phone) and password are required',
        errors: fieldErrors,
        fieldErrors
      });
    }

    const cleanIdentifier = identifier.trim().toLowerCase();
    const cleanPhone = identifier.trim();

    // 1. findOne()
    const user = await User.findOne({ 
      $or: [
        { email: cleanIdentifier }, 
        { phone: cleanPhone }
      ] 
    });

    if (!user) {
      console.error('[AUTH-WARNING] login user not found:', cleanIdentifier);
      return res.status(404).json({ success: false, message: 'User not found' });
    }

    // 2. compare bcrypt password
    const isMatch = await bcrypt.compare(rawPassword, user.passwordHash);
    if (!isMatch) {
      console.error('[AUTH-WARNING] login password mismatch for user:', cleanIdentifier);
      return res.status(401).json({ success: false, message: 'Invalid credentials' });
    }

    // 3. generate JWT
    const { secret, refreshSecret } = getJwtSecrets();

    const accessToken = jwt.sign(
      { id: user._id, email: user.email, role: user.role, tokenVersion: user.tokenVersion || 0 },
      secret,
      { expiresIn: '15m' }
    );

    const refreshToken = jwt.sign(
      { id: user._id, tokenVersion: user.tokenVersion || 0 },
      refreshSecret,
      { expiresIn: '7d' }
    );

    // 4. return response (NO user.save()!)
    return res.status(200).json({
      success: true,
      token: accessToken,
      accessToken,
      refreshToken,
      user: {
        id: user._id,
        firstName: user.firstName,
        lastName: user.lastName,
        email: user.email,
        phone: user.phone,
        role: user.role
      }
    });
  } catch (error: any) {
    console.error('[AUTH-ERROR] Exception in login:', error?.stack || error);
    return res.status(500).json({ 
      success: false, 
      message: 'Server error during authentication', 
      error: error?.message || String(error),
      stack: error?.stack 
    });
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

// Aliases for compatibility
export const signup = signUp;
export const verifyOTP = verifyOtp;
export const refreshToken = refreshSessionToken;
