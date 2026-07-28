import mongoose, { Schema, Document } from 'mongoose';

export interface IRoute extends Document {
  routeId: string;
  routeName: string;
  origin: string;
  terminus: string;
  totalDistanceKm: number;
  stations: string[];
  trainsCount: number;
}

const RouteSchema: Schema = new Schema({
  routeId: { type: String, required: true, unique: true },
  routeName: { type: String, required: true },
  origin: { type: String, required: true },
  terminus: { type: String, required: true },
  totalDistanceKm: { type: Number, default: 0 },
  stations: [{ type: String }],
  trainsCount: { type: Number, default: 0 }
}, {
  timestamps: true
});

export default mongoose.model<IRoute>('Route', RouteSchema);
