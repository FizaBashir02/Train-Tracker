import mongoose, { Schema, Document } from 'mongoose';

export interface IScheduleStation {
  stationName: string;
  stationCode: string;
  arrivalTime: string;
  departureTime: string;
  stopDurationMinutes: number;
  distanceKm: number;
  platform: string;
  isMajor: boolean;
}

export interface ITrainSchedule extends Document {
  trainNumber: string;
  trainName: string;
  sourceStation: string;
  destinationStation: string;
  departureTime: string;
  arrivalTime: string;
  durationHours: string;
  runningDays: string;
  trainType: string; // Express, Passenger, Freight
  status: string;
  platform: string;
  distanceKm: number;
  economyFare: number;
  standardFare: number;
  businessFare: number;
  classesAvailable: string[];
  scheduleStations: IScheduleStation[];
  routeOverview: string;
}

const ScheduleStationSchema: Schema = new Schema({
  stationName: { type: String, required: true },
  stationCode: { type: String, required: true },
  arrivalTime: { type: String, required: true },
  departureTime: { type: String, required: true },
  stopDurationMinutes: { type: Number, default: 2 },
  distanceKm: { type: Number, default: 0 },
  platform: { type: String, default: '1' },
  isMajor: { type: Boolean, default: false }
});

const TrainScheduleSchema: Schema = new Schema({
  trainNumber: { type: String, required: true, unique: true, index: true },
  trainName: { type: String, required: true },
  sourceStation: { type: String, required: true, index: true },
  destinationStation: { type: String, required: true, index: true },
  departureTime: { type: String, required: true },
  arrivalTime: { type: String, required: true },
  durationHours: { type: String, required: true },
  runningDays: { type: String, default: 'Daily' },
  trainType: { type: String, default: 'Express', index: true },
  status: { type: String, default: 'On Schedule' },
  platform: { type: String, default: '1' },
  distanceKm: { type: Number, default: 1200 },
  economyFare: { type: Number, default: 1800 },
  standardFare: { type: Number, default: 3200 },
  businessFare: { type: Number, default: 5500 },
  classesAvailable: [{ type: String }],
  scheduleStations: [ScheduleStationSchema],
  routeOverview: { type: String, default: '' }
}, { timestamps: true });

export default mongoose.model<ITrainSchedule>('TrainSchedule', TrainScheduleSchema);
