import { Request, Response, NextFunction } from 'express';
import jwt from 'jsonwebtoken';

export interface AuthenticatedRequest extends Request {
  user?: {
    id: string;
    email: string;
    role: 'passenger' | 'admin' | 'conductor';
  };
}

export const authenticateToken = (req: AuthenticatedRequest, res: Response, next: NextFunction) => {
  const authHeader = req.headers['authorization'];
  const token = authHeader && authHeader.split(' ')[1];

  if (!token) {
    return res.status(401).json({ success: false, message: 'Access token is required' });
  }

  const jwtSecret = process.env.JWT_SECRET || 'supersecret_companion_key_2026';

  jwt.verify(token, jwtSecret, (err, decoded) => {
    if (err) {
      return res.status(403).json({ success: false, message: 'Invalid or expired access token' });
    }

    req.user = decoded as AuthenticatedRequest['user'];
    next();
  });
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
