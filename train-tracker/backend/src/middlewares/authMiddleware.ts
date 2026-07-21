import { Request, Response, NextFunction } from 'express';
import jwt from 'jsonwebtoken';
import User from '../models/User';

export interface AuthenticatedRequest extends Request {
  user?: {
    id: string;
    email: string;
    role: 'passenger' | 'admin' | 'conductor';
    tokenVersion?: number;
  };
}

export const authenticateToken = async (req: AuthenticatedRequest, res: Response, next: NextFunction) => {
  try {
    const authHeader = req.headers['authorization'];
    const token = authHeader && authHeader.split(' ')[1];

    if (!token) {
      return res.status(401).json({ success: false, message: 'Access token is required' });
    }

    const jwtSecret = process.env.JWT_SECRET;
    if (!jwtSecret) {
      console.error('JWT_SECRET is missing from environment variables');
      return res.status(500).json({ success: false, message: 'Server security configuration error' });
    }

    const decoded = jwt.verify(token, jwtSecret) as { id: string; email: string; role: 'passenger' | 'admin' | 'conductor'; tokenVersion?: number };

    // Perform database lookup to verify user activity and tokenVersion (revoke sessions)
    const user = await User.findById(decoded.id).select('tokenVersion role email lockUntil');
    if (!user) {
      return res.status(401).json({ success: false, message: 'User account no longer exists' });
    }

    if (user.lockUntil && user.lockUntil > new Date()) {
      return res.status(423).json({ success: false, message: 'Account is temporarily locked due to security policy' });
    }

    if (decoded.tokenVersion !== undefined && user.tokenVersion !== decoded.tokenVersion) {
      return res.status(401).json({ success: false, message: 'Session invalidated. Please log in again.' });
    }

    req.user = {
      id: decoded.id,
      email: user.email,
      role: user.role,
      tokenVersion: user.tokenVersion
    };

    next();
  } catch (err) {
    return res.status(401).json({ success: false, message: 'Invalid or expired access token' });
  }
};

export const requireRole = (roles: Array<'passenger' | 'admin' | 'conductor'>) => {
  return (req: AuthenticatedRequest, res: Response, next: NextFunction) => {
    if (!req.user) {
      return res.status(401).json({ success: false, message: 'Authentication is required' });
    }

    if (!roles.includes(req.user.role)) {
      return res.status(403).json({ success: false, message: 'Unauthorized access for your user role' });
    }

    next();
  };
};
