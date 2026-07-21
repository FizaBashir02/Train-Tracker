import { Router } from 'express';
import { 
  signUp, 
  verifyOtp, 
  login, 
  refreshSessionToken, 
  logout, 
  logoutAllDevices, 
  forgotPassword, 
  resetPassword, 
  registerFcmToken 
} from '../controllers/authController';
import { authenticateToken } from '../middlewares/authMiddleware';

const router = Router();

router.post('/signup', signUp);
router.post('/verify-otp', verifyOtp);
router.post('/login', login);
router.post('/refresh', refreshSessionToken);
router.post('/forgot-password', forgotPassword);
router.post('/reset-password', resetPassword);

// Authenticated routes
router.post('/logout', authenticateToken, logout);
router.post('/logout-all', authenticateToken, logoutAllDevices);
router.post('/register-fcm-token', authenticateToken, registerFcmToken);

export default router;
