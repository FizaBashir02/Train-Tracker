import { Router } from 'express';
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

// Protect all user endpoints
router.use(authenticateToken);

// Profile
router.get('/profile', getUserProfile);
router.put('/profile', updateUserProfile);
router.post('/profile/change-password', changePassword);
router.delete('/profile', deleteAccount);
router.post('/profile/picture', uploadProfilePicture);

// Favorites
router.get('/favorites', getFavorites);
router.post('/favorites', addFavorite);
router.delete('/favorites/:trainNumber', removeFavorite);

// Recent Searches
router.get('/recent-searches', getRecentSearches);
router.post('/recent-searches', addRecentSearch);
router.delete('/recent-searches', clearRecentSearches);

// Feedback & Reports
router.post('/feedback', submitFeedback);
router.post('/report', submitReport);

export default router;
