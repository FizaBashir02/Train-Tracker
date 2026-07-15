import mongoose, { Schema, Document } from 'mongoose';

export interface IWeatherCache extends Document {
  location: string;
  temperature: string;
  condition: string;
  humidity: string;
  updatedAt: Date;
}

const WeatherCacheSchema = new Schema({
  location: { type: String, required: true, index: true, lowercase: true, trim: true },
  temperature: { type: String, required: true },
  condition: { type: String, required: true },
  humidity: { type: String, required: true }
}, {
  timestamps: true
});

// Cache automatically expires after 30 minutes
WeatherCacheSchema.index({ updatedAt: 1 }, { expireAfterSeconds: 1800 });

export default mongoose.model<IWeatherCache>('WeatherCache', WeatherCacheSchema);
