import { Request, Response } from 'express';
import bcrypt from 'bcrypt';
import jwt from 'jsonwebtoken';
import User from '../models/User';
import { ENV } from '../config/env';

export const registerUser = async (req: Request, res: Response) => {
  try {
    const { firstName, lastName, email, phone, password } = req.body;

    if (!email || !password || !firstName || !lastName) {
      return res.status(400).json({ success: false, message: 'All fields are required' });
    }

    const existingUser = await User.findOne({ email: email.toLowerCase() });
    if (existingUser) {
      return res.status(400).json({ success: false, message: 'User with this email already exists' });
    }

    const salt = await bcrypt.genSalt(10);
    const passwordHash = await bcrypt.hash(password, salt);

    const user = new User({
      firstName,
      lastName,
      email: email.toLowerCase(),
      phone: phone || '',
      passwordHash,
      isVerified: true
    });

    await user.save();

    return res.status(201).json({
      success: true,
      message: 'Account registered successfully. Please login.'
    });
  } catch (error) {
    console.error('Registration Error:', error);
    return res.status(500).json({ success: false, message: 'Server error during registration' });
  }
};

export const loginUser = async (req: Request, res: Response) => {
  try {
    const { identifier, password } = req.body;

    if (!identifier || !password) {
      return res.status(400).json({ success: false, message: 'Email/Identifier and password required' });
    }

    const user = await User.findOne({
      $or: [{ email: identifier.toLowerCase() }, { phone: identifier }]
    });

    if (!user) {
      return res.status(401).json({ success: false, message: 'Invalid credentials' });
    }

    const isMatch = await bcrypt.compare(password, user.passwordHash);
    if (!isMatch) {
      return res.status(401).json({ success: false, message: 'Invalid credentials' });
    }

    const token = jwt.sign(
      { userId: user._id, email: user.email },
      ENV.JWT_SECRET,
      { expiresIn: '7d' }
    );

    const refreshToken = jwt.sign(
      { userId: user._id, email: user.email },
      ENV.JWT_REFRESH_SECRET,
      { expiresIn: '30d' }
    );

    return res.status(200).json({
      token,
      refreshToken,
      user: {
        firstName: user.firstName,
        lastName: user.lastName,
        email: user.email,
        phone: user.phone
      }
    });
  } catch (error) {
    console.error('Login Error:', error);
    return res.status(500).json({ success: false, message: 'Server error during login' });
  }
};

export const verifyOtp = async (req: Request, res: Response) => {
  try {
    const { email } = req.body;
    const user = await User.findOne({ email: email?.toLowerCase() });

    if (!user) {
      return res.status(404).json({ success: false, message: 'User not found' });
    }

    user.isVerified = true;
    await user.save();

    const token = jwt.sign(
      { userId: user._id, email: user.email },
      ENV.JWT_SECRET,
      { expiresIn: '7d' }
    );

    const refreshToken = jwt.sign(
      { userId: user._id, email: user.email },
      ENV.JWT_REFRESH_SECRET,
      { expiresIn: '30d' }
    );

    return res.status(200).json({
      token,
      refreshToken,
      user: {
        firstName: user.firstName,
        lastName: user.lastName,
        email: user.email,
        phone: user.phone
      }
    });
  } catch (error) {
    return res.status(500).json({ success: false, message: 'Server error during OTP verification' });
  }
};

export const refreshToken = async (req: Request, res: Response) => {
  try {
    const { refreshToken: token } = req.body;
    if (!token) {
      return res.status(400).json({ success: false, message: 'Refresh token required' });
    }

    const decoded = jwt.verify(token, ENV.JWT_REFRESH_SECRET) as { userId: string; email: string };
    const newToken = jwt.sign(
      { userId: decoded.userId, email: decoded.email },
      ENV.JWT_SECRET,
      { expiresIn: '7d' }
    );
    const newRefreshToken = jwt.sign(
      { userId: decoded.userId, email: decoded.email },
      ENV.JWT_REFRESH_SECRET,
      { expiresIn: '30d' }
    );

    return res.status(200).json({
      token: newToken,
      refreshToken: newRefreshToken
    });
  } catch (error) {
    return res.status(403).json({ success: false, message: 'Invalid or expired refresh token' });
  }
};

export const registerFcmToken = async (req: Request, res: Response) => {
  try {
    const { email, fcmToken } = req.body;
    if (email && fcmToken) {
      await User.updateOne({ email: email.toLowerCase() }, { $set: { fcmToken } });
    }
    return res.status(200).json({ success: true, message: 'FCM Token registered successfully' });
  } catch (error) {
    return res.status(500).json({ success: false, message: 'Error registering FCM token' });
  }
};
