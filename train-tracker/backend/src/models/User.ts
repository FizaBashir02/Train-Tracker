import mongoose, { Schema, Document } from 'mongoose';

export interface IUser extends Document {
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  passwordHash: string;
  role: 'passenger' | 'admin' | 'conductor' | 'USER' | 'user';
  isVerified: boolean;
  isActive: boolean;
  isEmailVerified: boolean;
  profilePictureUrl?: string;
  otpCode?: string;
  otpExpiry?: Date;
  otpAttempts: number;
  lastOtpSentAt?: Date;
  refreshToken?: string;
  fcmToken?: string;
  tokenVersion: number;
  failedLoginAttempts: number;
  lockUntil?: Date;
  passwordHistory: string[];
  createdAt: Date;
  updatedAt: Date;
}

const UserSchema: Schema = new Schema({
  firstName: { type: String, required: true, trim: true },
  lastName: { type: String, required: true, trim: true },
  email: { type: String, required: true, unique: true, index: true, lowercase: true, trim: true },
  phone: { type: String, required: true, unique: true, trim: true },
  passwordHash: { type: String, required: true },
  role: { type: String, enum: ['passenger', 'admin', 'conductor', 'USER', 'user'], default: 'passenger' },
  isVerified: { type: Boolean, default: false },
  isActive: { type: Boolean, default: true },
  isEmailVerified: { type: Boolean, default: false },
  profilePictureUrl: { type: String },
  otpCode: { type: String },
  otpExpiry: { type: Date },
  otpAttempts: { type: Number, default: 0 },
  lastOtpSentAt: { type: Date },
  refreshToken: { type: String },
  fcmToken: { type: String },
  tokenVersion: { type: Number, default: 0 },
  failedLoginAttempts: { type: Number, default: 0 },
  lockUntil: { type: Date },
  passwordHistory: [{ type: String }]
}, {
  timestamps: true
});

export default mongoose.model<IUser>('User', UserSchema);
