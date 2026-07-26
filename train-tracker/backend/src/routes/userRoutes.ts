import { Router, Request, Response, NextFunction } from 'express';
import { authenticateToken } from '../middlewares/authMiddleware';
import { 
  getUserProfile, 
  updateUserProfile, 
  changePassword,
  deleteAccount,
  uploadProfilePicture,
  getFavorites, 
  addFavorite, 
  removeFavorite, 
  getRecentSearches, 
  addRecentSearch, 
  clearRecentSearches, 
  submitFeedback, 
  submitReport 
} from '../controllers/userController';

const router = Router();

const safeAsync = (fn: (req: any, res: Response, next: NextFunction) => Promise<any>) => {
  return (req: Request, res: Response, next: NextFunction) => {
    try {
      return Promise.resolve(fn(req, res, next)).catch((err: any) => {
        console.error('[USER-ROUTE-ERROR]', err);
        if (!res.headersSent) {
          res.status(500).json({ success: false, message: 'Internal server error processing user request' });
        }
      });
    } catch (err: any) {
      console.error('[USER-ROUTE-EXCEPTION]', err);
      if (!res.headersSent) {
        res.status(500).json({ success: false, message: 'Internal server error processing user request' });
      }
    }
  };
};

// Protect all user endpoints
router.use(authenticateToken);

// Profile
router.get('/profile', safeAsync(getUserProfile));
router.put('/profile', safeAsync(updateUserProfile));
router.post('/profile/change-password', safeAsync(changePassword));
router.delete('/profile', safeAsync(deleteAccount));
router.post('/profile/picture', safeAsync(uploadProfilePicture));

// Favorites
router.get('/favorites', safeAsync(getFavorites));
router.post('/favorites', safeAsync(addFavorite));
router.delete('/favorites/:trainNumber', safeAsync(removeFavorite));

// Recent Searches
router.get('/recent-searches', safeAsync(getRecentSearches));
router.post('/recent-searches', safeAsync(addRecentSearch));
router.delete('/recent-searches', safeAsync(clearRecentSearches));

// Feedback & Reports
router.post('/feedback', safeAsync(submitFeedback));
router.post('/report', safeAsync(submitReport));

export default router;
