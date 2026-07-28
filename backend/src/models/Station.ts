import mongoose, { Schema, Document } from 'mongoose';

export interface IStation extends Document {
  stationCode: string;
  name: string;
  city: string;
  province: string;
  category: string;
  totalPlatforms: number;
  contactNumber: string;
  address: string;
  amenities: string[];
}

const StationSchema: Schema = new Schema({
  stationCode: { type: String, required: true, unique: true, index: true },
  name: { type: String, required: true },
  city: { type: String, required: true, index: true },
  province: { type: String, default: 'Punjab' },
  category: { type: String, default: 'Major Junction' },
  totalPlatforms: { type: Number, default: 4 },
  contactNumber: { type: String, default: '117' },
  address: { type: String, default: 'Pakistan Railways Station' },
  amenities: [{ type: String }]
}, { timestamps: true });

export default mongoose.model<IStation>('Station', StationSchema);
