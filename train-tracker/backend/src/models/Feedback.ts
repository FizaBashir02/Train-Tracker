import mongoose, { Schema, Document } from 'mongoose';

export interface IFeedback extends Document {
  userEmail: string;
  subject: string;
  message: string;
  rating: number;
  createdAt: Date;
  updatedAt: Date;
}

const FeedbackSchema: Schema = new Schema({
  userEmail: { type: String, required: true, lowercase: true, trim: true },
  subject: { type: String, required: true, trim: true },
  message: { type: String, required: true, trim: true },
  rating: { type: Number, required: true, min: 1, max: 5 }
}, {
  timestamps: true
});

export default mongoose.model<IFeedback>('Feedback', FeedbackSchema);
