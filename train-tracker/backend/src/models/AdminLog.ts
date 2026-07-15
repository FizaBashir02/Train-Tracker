import mongoose, { Schema, Document } from 'mongoose';

export interface IAdminLog extends Document {
  adminEmail: string;
  action: string;
  details: string;
  createdAt: Date;
  updatedAt: Date;
}

const AdminLogSchema: Schema = new Schema({
  adminEmail: { type: String, required: true, lowercase: true, trim: true },
  action: { type: String, required: true },
  details: { type: String, required: true }
}, {
  timestamps: true
});

export default mongoose.model<IAdminLog>('AdminLog', AdminLogSchema);
