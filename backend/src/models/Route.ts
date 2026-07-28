import mongoose, { Schema, Document } from 'mongoose';

export interface IRoute extends Document {
  routeName: string;
  sourceCity: string;
  destinationCity: string;
  totalDistanceKm: number;
  totalStationsCount: number;
  keyViaStations: string[];
  majorTrains: string[];
}

const RouteSchema: Schema = new Schema({
  routeName: { type: String, required: true, unique: true },
  sourceCity: { type: String, required: true, index: true },
  destinationCity: { type: String, required: true, index: true },
  totalDistanceKm: { type: Number, default: 1200 },
  totalStationsCount: { type: Number, default: 25 },
  keyViaStations: [{ type: String }],
  majorTrains: [{ type: String }]
}, { timestamps: true });

export default mongoose.model<IRoute>('Route', RouteSchema);
