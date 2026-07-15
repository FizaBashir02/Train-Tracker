import { Response } from 'express';
import bcrypt from 'bcrypt';
import { AuthenticatedRequest } from '../middlewares/authMiddleware';
import User from '../models/User';
import Favorite from '../models/Favorite';
import RecentSearch from '../models/RecentSearch';
import Feedback from '../models/Feedback';
import Report from '../models/Report';

// --- User Profile ---
export const getUserProfile = async (req: AuthenticatedRequest, res: Response) => {
  try {
    if (!req.user) {
      return res.status(401).json({ success: false, message: 'Unauthorized access' });
    }

    const user = await User.findById(req.user.id).select('-passwordHash -refreshToken');
    if (!user) {
      return res.status(404).json({ success: false, message: 'User not found' });
    }

    return res.status(200).json({ success: true, user });
  } catch (error: any) {
    return res.status(500).json({ success: false, message: error.message });
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

    if (firstName) user.firstName = firstName;
    if (lastName) user.lastName = lastName;
    if (phone) {
      // Check phone uniqueness if modified
      if (phone !== user.phone) {
        const phoneExists = await User.findOne({ phone });
        if (phoneExists) {
          return res.status(400).json({ success: false, message: 'Phone number already in use' });
        }
        user.phone = phone;
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
    return res.status(500).json({ success: false, message: error.message });
  }
};

// --- Favorites ---
export const getFavorites = async (req: AuthenticatedRequest, res: Response) => {
  try {
    if (!req.user) {
      return res.status(401).json({ success: false, message: 'Unauthorized access' });
    }

    const favorites = await Favorite.find({ userEmail: req.user.email });
    return res.status(200).json({ success: true, favorites });
  } catch (error: any) {
    return res.status(500).json({ success: false, message: error.message });
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

    // Upsert to handle unique index safely
    const favorite = await Favorite.findOneAndUpdate(
      { userEmail: req.user.email, trainNumber: trainNumber.toUpperCase() },
      { trainName },
      { upsert: true, new: true }
    );

    return res.status(201).json({ success: true, message: 'Train added to favorites', favorite });
  } catch (error: any) {
    return res.status(500).json({ success: false, message: error.message });
  }
};

export const removeFavorite = async (req: AuthenticatedRequest, res: Response) => {
  try {
    if (!req.user) {
      return res.status(401).json({ success: false, message: 'Unauthorized' });
    }

    const { trainNumber } = req.params;
    await Favorite.deleteOne({ userEmail: req.user.email, trainNumber: trainNumber.toUpperCase() });

    return res.status(200).json({ success: true, message: 'Train removed from favorites' });
  } catch (error: any) {
    return res.status(500).json({ success: false, message: error.message });
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
    return res.status(500).json({ success: false, message: error.message });
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
      { userEmail: req.user.email, queryText: queryText.trim(), searchType },
      { updatedAt: new Date() },
      { upsert: true, new: true }
    );

    return res.status(201).json({ success: true, search });
  } catch (error: any) {
    return res.status(500).json({ success: false, message: error.message });
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
    return res.status(500).json({ success: false, message: error.message });
  }
};

// --- Feedback & Reports ---
export const submitFeedback = async (req: AuthenticatedRequest, res: Response) => {
  try {
    if (!req.user) {
      return res.status(401).json({ success: false, message: 'Unauthorized' });
    }

    const { subject, message, rating } = req.body;
    if (!subject || !message || !rating) {
      return res.status(400).json({ success: false, message: 'All fields are required' });
    }

    const feedback = new Feedback({
      userEmail: req.user.email,
      subject,
      message,
      rating
    });

    await feedback.save();
    return res.status(201).json({ success: true, message: 'Feedback submitted successfully. Thank you!', feedback });
  } catch (error: any) {
    return res.status(500).json({ success: false, message: error.message });
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
      trainNumber,
      issueType,
      description,
      status: 'pending'
    });

    await report.save();
    return res.status(201).json({ success: true, message: 'Report submitted successfully to the dispatch team', report });
  } catch (error: any) {
    return res.status(500).json({ success: false, message: error.message });
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

    const user = await User.findById(req.user.id);
    if (!user) {
      return res.status(404).json({ success: false, message: 'User not found' });
    }

    const isMatch = await bcrypt.compare(oldPasswordHash, user.passwordHash);
    if (!isMatch) {
      return res.status(400).json({ success: false, message: 'Incorrect old password' });
    }

    const salt = await bcrypt.genSalt(10);
    const newHashed = await bcrypt.hash(newPasswordHash, salt);

    user.passwordHash = newHashed;
    await user.save();

    return res.status(200).json({ success: true, message: 'Password changed successfully' });
  } catch (error: any) {
    return res.status(500).json({ success: false, message: error.message });
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

    // Clean up associated data
    await Favorite.deleteMany({ userEmail: user.email });
    await RecentSearch.deleteMany({ userEmail: user.email });

    return res.status(200).json({ success: true, message: 'Account deleted successfully' });
  } catch (error: any) {
    return res.status(500).json({ success: false, message: error.message });
  }
};

export const uploadProfilePicture = async (req: AuthenticatedRequest, res: Response) => {
  try {
    if (!req.user) {
      return res.status(401).json({ success: false, message: 'Unauthorized access' });
    }

    const { profilePictureUrl } = req.body;
    if (!profilePictureUrl) {
      return res.status(400).json({ success: false, message: 'Profile picture URL is required' });
    }

    const user = await User.findById(req.user.id);
    if (!user) {
      return res.status(404).json({ success: false, message: 'User not found' });
    }

    user.profilePictureUrl = profilePictureUrl;
    await user.save();

    return res.status(200).json({ success: true, message: 'Profile picture updated successfully', profilePictureUrl });
  } catch (error: any) {
    return res.status(500).json({ success: false, message: error.message });
  }
};

