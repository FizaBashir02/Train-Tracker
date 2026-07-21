import { Response } from 'express';
import bcrypt from 'bcrypt';
import { AuthenticatedRequest } from '../middlewares/authMiddleware';
import User from '../models/User';
import Favorite from '../models/Favorite';
import RecentSearch from '../models/RecentSearch';
import Feedback from '../models/Feedback';
import Report from '../models/Report';

// Password strength validation helper
const isStrongPassword = (pwd: string): { valid: boolean; reason?: string } => {
  if (pwd.length < 8) {
    return { valid: false, reason: 'Password must be at least 8 characters long' };
  }
  if (!/[A-Z]/.test(pwd)) {
    return { valid: false, reason: 'Password must contain at least one uppercase letter' };
  }
  if (!/[a-z]/.test(pwd)) {
    return { valid: false, reason: 'Password must contain at least one lowercase letter' };
  }
  if (!/[0-9]/.test(pwd)) {
    return { valid: false, reason: 'Password must contain at least one number' };
  }
  if (!/[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(pwd)) {
    return { valid: false, reason: 'Password must contain at least one special character' };
  }
  return { valid: true };
};

// --- User Profile ---
export const getUserProfile = async (req: AuthenticatedRequest, res: Response) => {
  try {
    if (!req.user) {
      return res.status(401).json({ success: false, message: 'Unauthorized access' });
    }

    const user = await User.findById(req.user.id).select('-passwordHash -refreshToken -passwordHistory -otpCode -otpExpiry');
    if (!user) {
      return res.status(404).json({ success: false, message: 'User not found' });
    }

    return res.status(200).json({ success: true, user });
  } catch (error: any) {
    return res.status(500).json({ success: false, message: 'Server error retrieving user profile' });
  }
};

export const updateUserProfile = async (req: AuthenticatedRequest, res: Response) => {
  try {
    if (!req.user) {
      return res.status(401).json({ success: false, message: 'Unauthorized access' });
    }

    const { firstName, lastName, phone } = req.body;
    const user = await User.findById(req.user.id);
    if (!user) {
      return res.status(404).json({ success: false, message: 'User not found' });
    }

    if (firstName && typeof firstName === 'string') user.firstName = firstName.trim();
    if (lastName && typeof lastName === 'string') user.lastName = lastName.trim();
    if (phone && typeof phone === 'string') {
      const cleanPhone = phone.trim();
      if (cleanPhone !== user.phone) {
        const phoneExists = await User.findOne({ phone: cleanPhone });
        if (phoneExists) {
          return res.status(400).json({ success: false, message: 'Phone number already in use' });
        }
        user.phone = cleanPhone;
      }
    }

    await user.save();
    return res.status(200).json({
      success: true,
      message: 'Profile updated successfully',
      user: {
        firstName: user.firstName,
        lastName: user.lastName,
        email: user.email,
        phone: user.phone,
        role: user.role
      }
    });
  } catch (error: any) {
    return res.status(500).json({ success: false, message: 'Server error updating user profile' });
  }
};

// --- Favorites ---
export const getFavorites = async (req: AuthenticatedRequest, res: Response) => {
  try {
    if (!req.user) {
      return res.status(401).json({ success: false, message: 'Unauthorized' });
    }

    const favorites = await Favorite.find({ userEmail: req.user.email });
    return res.status(200).json({ success: true, favorites });
  } catch (error: any) {
    return res.status(500).json({ success: false, message: 'Server error fetching favorites' });
  }
};

export const addFavorite = async (req: AuthenticatedRequest, res: Response) => {
  try {
    if (!req.user) {
      return res.status(401).json({ success: false, message: 'Unauthorized' });
    }

    const { trainNumber, trainName } = req.body;
    if (!trainNumber || !trainName) {
      return res.status(400).json({ success: false, message: 'Train Number and Name are required' });
    }

    const favorite = await Favorite.findOneAndUpdate(
      { userEmail: req.user.email, trainNumber: String(trainNumber).trim().toUpperCase() },
      { trainName: String(trainName).trim() },
      { upsert: true, new: true }
    );

    return res.status(201).json({ success: true, message: 'Train added to favorites', favorite });
  } catch (error: any) {
    return res.status(500).json({ success: false, message: 'Server error adding favorite' });
  }
};

export const removeFavorite = async (req: AuthenticatedRequest, res: Response) => {
  try {
    if (!req.user) {
      return res.status(401).json({ success: false, message: 'Unauthorized' });
    }

    const { trainNumber } = req.params;
    await Favorite.deleteOne({ userEmail: req.user.email, trainNumber: String(trainNumber).trim().toUpperCase() });

    return res.status(200).json({ success: true, message: 'Train removed from favorites' });
  } catch (error: any) {
    return res.status(500).json({ success: false, message: 'Server error removing favorite' });
  }
};

// --- Recent Searches ---
export const getRecentSearches = async (req: AuthenticatedRequest, res: Response) => {
  try {
    if (!req.user) {
      return res.status(401).json({ success: false, message: 'Unauthorized' });
    }

    const searches = await RecentSearch.find({ userEmail: req.user.email }).sort({ updatedAt: -1 }).limit(10);
    return res.status(200).json({ success: true, searches });
  } catch (error: any) {
    return res.status(500).json({ success: false, message: 'Server error fetching recent searches' });
  }
};

export const addRecentSearch = async (req: AuthenticatedRequest, res: Response) => {
  try {
    if (!req.user) {
      return res.status(401).json({ success: false, message: 'Unauthorized' });
    }

    const { queryText, searchType } = req.body;
    if (!queryText) {
      return res.status(400).json({ success: false, message: 'Query text is required' });
    }

    const search = await RecentSearch.findOneAndUpdate(
      { userEmail: req.user.email, queryText: String(queryText).trim(), searchType: String(searchType || 'general') },
      { updatedAt: new Date() },
      { upsert: true, new: true }
    );

    return res.status(201).json({ success: true, search });
  } catch (error: any) {
    return res.status(500).json({ success: false, message: 'Server error adding recent search' });
  }
};

export const clearRecentSearches = async (req: AuthenticatedRequest, res: Response) => {
  try {
    if (!req.user) {
      return res.status(401).json({ success: false, message: 'Unauthorized' });
    }

    await RecentSearch.deleteMany({ userEmail: req.user.email });
    return res.status(200).json({ success: true, message: 'Search history cleared successfully' });
  } catch (error: any) {
    return res.status(500).json({ success: false, message: 'Server error clearing recent searches' });
  }
};

// --- Feedback & Reports ---
export const submitFeedback = async (req: AuthenticatedRequest, res: Response) => {
  try {
    if (!req.user) {
      return res.status(401).json({ success: false, message: 'Unauthorized' });
    }

    const { subject, message, rating } = req.body;
    if (!subject || !message || rating === undefined) {
      return res.status(400).json({ success: false, message: 'All fields (subject, message, rating) are required' });
    }

    const numericRating = Number(rating);
    if (isNaN(numericRating) || numericRating < 1 || numericRating > 5) {
      return res.status(400).json({ success: false, message: 'Rating must be a number between 1 and 5' });
    }

    const feedback = new Feedback({
      userEmail: req.user.email,
      subject: String(subject).trim(),
      message: String(message).trim(),
      rating: numericRating
    });

    await feedback.save();
    return res.status(201).json({ success: true, message: 'Feedback submitted successfully.', feedback });
  } catch (error: any) {
    return res.status(500).json({ success: false, message: 'Server error submitting feedback' });
  }
};

export const submitReport = async (req: AuthenticatedRequest, res: Response) => {
  try {
    if (!req.user) {
      return res.status(401).json({ success: false, message: 'Unauthorized' });
    }

    const { trainNumber, issueType, description } = req.body;
    if (!issueType || !description) {
      return res.status(400).json({ success: false, message: 'Issue type and description are required' });
    }

    const report = new Report({
      userEmail: req.user.email,
      trainNumber: trainNumber ? String(trainNumber).trim().toUpperCase() : undefined,
      issueType: String(issueType).trim(),
      description: String(description).trim(),
      status: 'pending'
    });

    await report.save();
    return res.status(201).json({ success: true, message: 'Report submitted successfully to the dispatch team', report });
  } catch (error: any) {
    return res.status(500).json({ success: false, message: 'Server error submitting report' });
  }
};

export const changePassword = async (req: AuthenticatedRequest, res: Response) => {
  try {
    if (!req.user) {
      return res.status(401).json({ success: false, message: 'Unauthorized access' });
    }

    const { oldPasswordHash, newPasswordHash } = req.body;
    if (!oldPasswordHash || !newPasswordHash) {
      return res.status(400).json({ success: false, message: 'Old password and new password are required' });
    }

    const pwdCheck = isStrongPassword(newPasswordHash);
    if (!pwdCheck.valid) {
      return res.status(400).json({ success: false, message: pwdCheck.reason });
    }

    const user = await User.findById(req.user.id);
    if (!user) {
      return res.status(404).json({ success: false, message: 'User not found' });
    }

    const isMatch = await bcrypt.compare(oldPasswordHash, user.passwordHash);
    if (!isMatch) {
      return res.status(400).json({ success: false, message: 'Incorrect old password' });
    }

    // Check Password History
    for (const oldHash of user.passwordHistory || []) {
      const isReused = await bcrypt.compare(newPasswordHash, oldHash);
      if (isReused) {
        return res.status(400).json({ success: false, message: 'You cannot reuse a recent password.' });
      }
    }

    const salt = await bcrypt.genSalt(12);
    const newHashed = await bcrypt.hash(newPasswordHash, salt);

    user.passwordHash = newHashed;
    user.tokenVersion += 1; // Revoke tokens on other devices upon password change
    user.refreshToken = undefined;

    user.passwordHistory.push(newHashed);
    if (user.passwordHistory.length > 5) {
      user.passwordHistory.shift();
    }

    await user.save();

    return res.status(200).json({ success: true, message: 'Password changed successfully. Please log in again.' });
  } catch (error: any) {
    return res.status(500).json({ success: false, message: 'Server error changing password' });
  }
};

export const deleteAccount = async (req: AuthenticatedRequest, res: Response) => {
  try {
    if (!req.user) {
      return res.status(401).json({ success: false, message: 'Unauthorized access' });
    }

    const user = await User.findByIdAndDelete(req.user.id);
    if (!user) {
      return res.status(404).json({ success: false, message: 'User not found' });
    }

    await Favorite.deleteMany({ userEmail: user.email });
    await RecentSearch.deleteMany({ userEmail: user.email });

    return res.status(200).json({ success: true, message: 'Account deleted successfully' });
  } catch (error: any) {
    return res.status(500).json({ success: false, message: 'Server error deleting account' });
  }
};

export const uploadProfilePicture = async (req: AuthenticatedRequest, res: Response) => {
  try {
    if (!req.user) {
      return res.status(401).json({ success: false, message: 'Unauthorized access' });
    }

    const { profilePictureUrl } = req.body;
    if (!profilePictureUrl || typeof profilePictureUrl !== 'string') {
      return res.status(400).json({ success: false, message: 'Valid profile picture URL is required' });
    }

    // Simple URL / Base64 format validation
    if (!profilePictureUrl.startsWith('data:image/') && !profilePictureUrl.startsWith('http://') && !profilePictureUrl.startsWith('https://')) {
      return res.status(400).json({ success: false, message: 'Profile picture must be a valid HTTP URL or Image Data URI' });
    }

    const user = await User.findById(req.user.id);
    if (!user) {
      return res.status(404).json({ success: false, message: 'User not found' });
    }

    user.profilePictureUrl = profilePictureUrl;
    await user.save();

    return res.status(200).json({ success: true, message: 'Profile picture updated successfully', profilePictureUrl });
  } catch (error: any) {
    return res.status(500).json({ success: false, message: 'Server error updating profile picture' });
  }
};
