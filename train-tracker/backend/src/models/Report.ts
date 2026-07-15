import mongoose, { Schema, Document } from 'mongoose';

export interface IReport extends Document {
  userEmail: string;
  trainNumber?: string;
  issueType: 'delay' | 'maintenance' | 'security' | 'cleanliness' | 'other';
  description: string;
  status: 'pending' | 'reviewed' | 'resolved';
  createdAt: Date;
  updatedAt: Date;
}

const ReportSchema: Schema = new Schema({
  userEmail: { type: String, required: true, lowercase: true, trim: true },
  trainNumber: { type: String, uppercase: true, trim: true },
  issueType: { type: String, enum: ['delay', 'maintenance', 'security', 'cleanliness', 'other'], required: true },
  description: { type: String, required: true, trim: true },
  status: { type: String, enum: ['pending', 'reviewed', 'resolved'], default: 'pending' }
}, {
  timestamps: true
});

export default mongoose.model<IReport>('Report', ReportSchema);
