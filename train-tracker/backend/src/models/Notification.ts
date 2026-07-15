import mongoose, { Schema, Document } from 'mongoose';

export interface INotification extends Document {
  title: string;
  message: string;
  category: string; // "alert", "delay", "news"
  recipientEmail?: string; // If empty, it's a broadcast
  isRead: boolean;
  createdAt: Date;
}

const NotificationSchema = new Schema({
  title: { type: String, required: true },
  message: { type: String, required: true },
  category: { type: String, required: true, default: "alert" },
  recipientEmail: { type: String, index: true },
  isRead: { type: Boolean, default: false }
}, {
  timestamps: true
});

export default mongoose.model<INotification>('Notification', NotificationSchema);
