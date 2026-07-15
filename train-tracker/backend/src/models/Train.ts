import mongoose, { Schema, Document } from 'mongoose';

export interface IScheduleStop {
  stationCode: string;
  stationName: string;
  arrival: string; // "14:30"
  departure: string; // "14:35"
  stopDurationMinutes: number;
  distanceKm: number;
}

export interface ITrain extends Document {
  trainNumber: string; // e.g. "7UP" or "41DN"
  trainName: string; // e.g. "Tezgam Express"
  source: string;
  destination: string;
  trainType: 'Express' | 'Passenger' | 'Freight';
  departureTime: string;
  arrivalTime: string;
  totalDistanceKm: number;
  stops: IScheduleStop[];
  isActive: boolean;
  createdAt: Date;
  updatedAt: Date;
}

const ScheduleStopSchema = new Schema({
  stationCode: { type: String, required: true, uppercase: true },
  stationName: { type: String, required: true },
  arrival: { type: String, required: true },
  departure: { type: String, required: true },
  stopDurationMinutes: { type: Number, required: true, default: 0 },
  distanceKm: { type: Number, required: true, default: 0 }
});

const TrainSchema: Schema = new Schema({
  trainNumber: { type: String, required: true, unique: true, index: true, uppercase: true, trim: true },
  trainName: { type: String, required: true, trim: true },
  source: { type: String, required: true, uppercase: true },
  destination: { type: String, required: true, uppercase: true },
  trainType: { type: String, enum: ['Express', 'Passenger', 'Freight'], default: 'Express' },
  departureTime: { type: String, required: true },
  arrivalTime: { type: String, required: true },
  totalDistanceKm: { type: Number, required: true, default: 0 },
  stops: [ScheduleStopSchema],
  isActive: { type: Boolean, default: true }
}, {
  timestamps: true
});

export default mongoose.model<ITrain>('Train', TrainSchema);
