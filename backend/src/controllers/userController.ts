import { Response } from 'express';
import bcrypt from 'bcrypt';
import User from '../models/User';
import { AuthRequest } from '../middleware/auth';

export const getProfile = async (req: AuthRequest, res: Response) => {
  try {
    const userId = req.user?.userId;
    const user = await User.findById(userId).select('-passwordHash');
    if (!user) {
      return res.status(404).json({ success: false, message: 'User not found' });
    }
    return res.status(200).json({
      firstName: user.firstName,
      lastName: user.lastName,
      email: user.email,
      phone: user.phone,
      profilePictureUrl: user.profilePictureUrl
    });
  } catch (error) {
    return res.status(500).json({ success: false, message: 'Server error fetching profile' });
  }
};

export const updateProfile = async (req: AuthRequest, res: Response) => {
  try {
    const userId = req.user?.userId;
    const { firstName, lastName, phone } = req.body;

    const user = await User.findById(userId);
    if (!user) {
      return res.status(404).json({ success: false, message: 'User not found' });
    }

    if (firstName) user.firstName = firstName;
    if (lastName) user.lastName = lastName;
    if (phone) user.phone = phone;

    await user.save();
    return res.status(200).json({ success: true, message: 'Profile updated successfully' });
  } catch (error) {
    return res.status(500).json({ success: false, message: 'Server error updating profile' });
  }
};

export const changePassword = async (req: AuthRequest, res: Response) => {
  try {
    const userId = req.user?.userId;
    const { oldPasswordHash, newPasswordHash } = req.body;

    const user = await User.findById(userId);
    if (!user) {
      return res.status(404).json({ success: false, message: 'User not found' });
    }

    const salt = await bcrypt.genSalt(10);
    user.passwordHash = await bcrypt.hash(newPasswordHash, salt);
    await user.save();

    return res.status(200).json({ success: true, message: 'Password changed successfully' });
  } catch (error) {
    return res.status(500).json({ success: false, message: 'Server error changing password' });
  }
};

export const deleteAccount = async (req: AuthRequest, res: Response) => {
  try {
    const userId = req.user?.userId;
    await User.findByIdAndDelete(userId);
    return res.status(200).json({ success: true, message: 'Account deleted successfully' });
  } catch (error) {
    return res.status(500).json({ success: false, message: 'Server error deleting account' });
  }
};

export const uploadProfilePicture = async (req: AuthRequest, res: Response) => {
  try {
    const userId = req.user?.userId;
    const { profilePictureUrl } = req.body;
    await User.findByIdAndUpdate(userId, { profilePictureUrl });
    return res.status(200).json({ success: true, message: 'Profile picture updated successfully' });
  } catch (error) {
    return res.status(500).json({ success: false, message: 'Server error uploading profile picture' });
  }
};
