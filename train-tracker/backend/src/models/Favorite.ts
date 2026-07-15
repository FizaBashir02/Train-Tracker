import mongoose, { Schema, Document } from 'mongoose';

export interface IFavorite extends Document {
  userEmail: string;
  trainNumber: string;
  trainName: string;
  createdAt: Date;
  updatedAt: Date;
}

const FavoriteSchema: Schema = new Schema({
  userEmail: { type: String, required: true, index: true, lowercase: true, trim: true },
  trainNumber: { type: String, required: true, uppercase: true, trim: true },
  trainName: { type: String, required: true, trim: true }
}, {
  timestamps: true
});

// Compound index to prevent duplicate favorites for the same user
FavoriteSchema.index({ userEmail: 1, trainNumber: 1 }, { unique: true });

export default mongoose.model<IFavorite>('Favorite', FavoriteSchema);
