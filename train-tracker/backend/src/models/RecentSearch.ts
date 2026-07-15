import mongoose, { Schema, Document } from 'mongoose';

export interface IRecentSearch extends Document {
  userEmail: string;
  queryText: string;
  searchType: 'train' | 'station';
  createdAt: Date;
  updatedAt: Date;
}

const RecentSearchSchema: Schema = new Schema({
  userEmail: { type: String, required: true, index: true, lowercase: true, trim: true },
  queryText: { type: String, required: true, trim: true },
  searchType: { type: String, enum: ['train', 'station'], default: 'train' }
}, {
  timestamps: true
});

// Index to automatically expire search history entries older than 30 days
RecentSearchSchema.index({ createdAt: 1 }, { expireAfterSeconds: 2592000 });

export default mongoose.model<IRecentSearch>('RecentSearch', RecentSearchSchema);
