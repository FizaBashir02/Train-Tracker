import { Router, Request, Response, NextFunction } from 'express';
import * as authController from '../controllers/authController';
import { authenticateToken } from '../middlewares/authMiddleware';

const router = Router();

const safeHandler = (handlerName: keyof typeof authController | string) => {
  return (req: Request, res: Response, next: NextFunction) => {
    const fn = (authController as any)[handlerName];
    if (typeof fn === 'function') {
      return fn(req, res, next);
    }
    return res.status(501).json({
      success: false,
      message: `Authentication endpoint '${String(handlerName)}' is currently unavailable`
    });
  };
};

const safeAuth = (req: any, res: any, next: any) => {
  if (typeof authenticateToken === 'function') {
    return authenticateToken(req, res, next);
  }
  next();
};

// Auth routes with alias support
router.post('/signup', safeHandler('signUp'));
router.post('/register', safeHandler('signUp'));

router.post('/verify-otp', safeHandler('verifyOtp'));
router.post('/verifyOTP', safeHandler('verifyOtp'));

router.post('/login', safeHandler('login'));

router.post('/refresh', safeHandler('refreshSessionToken'));
router.post('/refreshToken', safeHandler('refreshSessionToken'));

router.post('/forgot-password', safeHandler('forgotPassword'));
router.post('/reset-password', safeHandler('resetPassword'));

// Authenticated routes
router.post('/logout', safeAuth, safeHandler('logout'));
router.post('/logout-all', safeAuth, safeHandler('logoutAllDevices'));
router.post('/register-fcm-token', safeAuth, safeHandler('registerFcmToken'));

export default router;

