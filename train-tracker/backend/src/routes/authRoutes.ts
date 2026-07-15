import { Router } from 'express';
import { signUp, verifyOtp, login, refreshSessionToken, forgotPassword, resetPassword, registerFcmToken } from '../controllers/authController';

const router = Router();

router.post('/signup', signUp);
router.post('/verify-otp', verifyOtp);
router.post('/login', login);
router.post('/refresh', refreshSessionToken);
router.post('/forgot-password', forgotPassword);
router.post('/reset-password', resetPassword);
router.post('/register-fcm-token', registerFcmToken);

export default router;
