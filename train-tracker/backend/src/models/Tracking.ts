import mongoose, { Schema, Document } from 'mongoose';

export interface ITracking extends Document {
  trainNumber: string;
  trainName: string;
  currentLatitude: number;
  currentLongitude: number;
  currentSpeedKmh: number;
  delayMinutes: number;
  currentStation: string;
  previousStation: string;
  nextStation: string;
  distanceRemainingKm: number;
  journeyProgress: number; // Float value between 0.0 and 1.0
  lastUpdated: Date;
}

const TrackingSchema: Schema = new Schema({
  trainNumber: { type: String, required: true, unique: true, uppercase: true, index: true },
  trainName: { type: String, required: true },
  currentLatitude: { type: Number, required: true },
  currentLongitude: { type: Number, required: true },
  currentSpeedKmh: { type: Number, default: 0 },
  delayMinutes: { type: Number, default: 0 },
  currentStation: { type: String, required: true },
  previousStation: { type: String, required: true },
  nextStation: { type: String, required: true },
  distanceRemainingKm: { type: Number, default: 0 },
  journeyProgress: { type: Number, min: 0.0, max: 1.0, default: 0.0 },
  lastUpdated: { type: Date, default: Date.now }
}, {
  timestamps: true
});

export default mongoose.model<ITracking>('Tracking', TrackingSchema);
