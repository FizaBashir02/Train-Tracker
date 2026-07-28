import mongoose, { Schema, Document } from 'mongoose';

export interface IScheduleStop {
  stationCode: string;
  stationName: string;
  arrival: string;
  departure: string;
  stopDurationMinutes: number;
  distanceKm: number;
  platform: string;
}

export interface ISchedule extends Document {
  trainNumber: string;
  trainName: string;
  sourceStation: string;
  destinationStation: string;
  totalDistanceKm: number;
  totalJourneyTime: string;
  daysOfOperation: string[];
  stops: IScheduleStop[];
}

const ScheduleStopSchema = new Schema({
  stationCode: { type: String, required: true },
  stationName: { type: String, required: true },
  arrival: { type: String, required: true },
  departure: { type: String, required: true },
  stopDurationMinutes: { type: Number, default: 0 },
  distanceKm: { type: Number, default: 0 },
  platform: { type: String, default: '1' }
}, { _id: false });

const ScheduleSchema: Schema = new Schema({
  trainNumber: { type: String, required: true, unique: true, uppercase: true },
  trainName: { type: String, required: true },
  sourceStation: { type: String, required: true },
  destinationStation: { type: String, required: true },
  totalDistanceKm: { type: Number, default: 0 },
  totalJourneyTime: { type: String, default: '12h 00m' },
  daysOfOperation: [{ type: String }],
  stops: [ScheduleStopSchema]
}, {
  timestamps: true
});

export default mongoose.model<ISchedule>('Schedule', ScheduleSchema);
