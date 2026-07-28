import { Router } from 'express';
import { registerUser, loginUser, verifyOtp, refreshToken, registerFcmToken } from '../controllers/authController';

const router = Router();

router.post('/signup', registerUser);
router.post('/register', registerUser);
router.post('/login', loginUser);
router.post('/verify-otp', verifyOtp);
router.post('/refresh', refreshToken);
router.post('/register-fcm-token', registerFcmToken);

export default router;
