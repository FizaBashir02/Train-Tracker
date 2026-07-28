import { Router } from 'express';
import { getProfile, updateProfile, changePassword, deleteAccount, uploadProfilePicture } from '../controllers/userController';
import { authenticateToken } from '../middleware/auth';

const router = Router();

router.get('/profile', authenticateToken, getProfile);
router.put('/profile', authenticateToken, updateProfile);
router.post('/profile/change-password', authenticateToken, changePassword);
router.delete('/profile', authenticateToken, deleteAccount);
router.post('/profile/picture', authenticateToken, uploadProfilePicture);

export default router;
