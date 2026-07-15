import mongoose, { Schema, Document } from 'mongoose';

export interface IStation extends Document {
  code: string; // e.g. "LHR"
  name: string; // e.g. "Lahore Junction"
  address: string;
  latitude: number;
  longitude: number;
  contactNumber: string;
  facilities: string[];
  isActive: boolean;
  createdAt: Date;
  updatedAt: Date;
}

const StationSchema: Schema = new Schema({
  code: { type: String, required: true, unique: true, index: true, uppercase: true, trim: true },
  name: { type: String, required: true, trim: true },
  address: { type: String, required: true },
  latitude: { type: Number, required: true },
  longitude: { type: Number, required: true },
  contactNumber: { type: String, default: "117" },
  facilities: [{ type: String }],
  isActive: { type: Boolean, default: true }
}, {
  timestamps: true
});

export default mongoose.model<IStation>('Station', StationSchema);
