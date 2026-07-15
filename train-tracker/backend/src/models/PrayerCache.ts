import mongoose, { Schema, Document } from 'mongoose';

export interface IPrayerCache extends Document {
  location: string;
  islamicDate: string;
  fajr: string;
  dhuhr: string;
  asr: string;
  maghrib: string;
  isha: string;
  qiblaDirection: string;
  updatedAt: Date;
}

const PrayerCacheSchema = new Schema({
  location: { type: String, required: true, index: true, lowercase: true, trim: true },
  islamicDate: { type: String, required: true },
  fajr: { type: String, required: true },
  dhuhr: { type: String, required: true },
  asr: { type: String, required: true },
  maghrib: { type: String, required: true },
  isha: { type: String, required: true },
  qiblaDirection: { type: String, default: "261° (W)" }
}, {
  timestamps: true
});

// Cache expires after 24 hours (86400 seconds)
PrayerCacheSchema.index({ updatedAt: 1 }, { expireAfterSeconds: 86400 });

export default mongoose.model<IPrayerCache>('PrayerCache', PrayerCacheSchema);
