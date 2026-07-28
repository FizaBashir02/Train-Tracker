import mongoose, { Schema, Document } from 'mongoose';

export interface IIntermediateStation {
  stationCode: string;
  stationName: string;
  arrival: string;
  departure: string;
  stopDurationMinutes: number;
  distanceKm: number;
  platform: string;
}

export interface ITrain extends Document {
  trainNumber: string; // e.g. "7UP" or "1UP"
  trainName: string; // e.g. "Green Line Express"
  trainType: 'Express' | 'Passenger' | 'Freight';
  sourceStation: string;
  destinationStation: string;
  departureTime: string;
  arrivalTime: string;
  duration: string;
  distance: number;
  status: 'On Time' | 'Delayed' | 'Cancelled' | 'Boarding Soon' | 'Departed' | 'Arrived';
  platform: string;
  fareEconomy: number;
  fareBusiness: number;
  fareAC: number;
  daysOfOperation: string[];
  intermediateStations: IIntermediateStation[];
  route: string;
  availableSeats: number;
  lastUpdated: string;
  isActive: boolean;
  createdAt: Date;
  updatedAt: Date;
}

const IntermediateStationSchema = new Schema({
  stationCode: { type: String, required: true, uppercase: true },
  stationName: { type: String, required: true },
  arrival: { type: String, required: true },
  departure: { type: String, required: true },
  stopDurationMinutes: { type: Number, required: true, default: 0 },
  distanceKm: { type: Number, required: true, default: 0 },
  platform: { type: String, default: '1' }
}, { _id: false });

const TrainSchema: Schema = new Schema({
  trainNumber: { type: String, required: true, unique: true, index: true, uppercase: true, trim: true },
  trainName: { type: String, required: true, trim: true },
  trainType: { type: String, enum: ['Express', 'Passenger', 'Freight'], default: 'Express' },
  sourceStation: { type: String, required: true },
  destinationStation: { type: String, required: true },
  departureTime: { type: String, required: true },
  arrivalTime: { type: String, required: true },
  duration: { type: String, required: true, default: '12h 00m' },
  distance: { type: Number, required: true, default: 500 },
  status: { 
    type: String, 
    enum: ['On Time', 'Delayed', 'Cancelled', 'Boarding Soon', 'Departed', 'Arrived'], 
    default: 'On Time' 
  },
  platform: { type: String, default: '1' },
  fareEconomy: { type: Number, default: 1500 },
  fareBusiness: { type: Number, default: 3500 },
  fareAC: { type: Number, default: 5500 },
  daysOfOperation: [{ type: String }],
  intermediateStations: [IntermediateStationSchema],
  route: { type: String, default: 'Main Line 1' },
  availableSeats: { type: Number, default: 50 },
  lastUpdated: { type: String, default: 'Just now' },
  isActive: { type: Boolean, default: true }
}, {
  timestamps: true
});

export default mongoose.model<ITrain>('Train', TrainSchema);
