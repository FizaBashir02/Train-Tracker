import mongoose, { Schema, Document } from 'mongoose';

export interface INews extends Document {
  title: string;
  category: string;
  date: string;
  summary: string;
}

const NewsSchema = new Schema({
  title: { type: String, required: true },
  category: { type: String, required: true },
  date: { type: String, required: true },
  summary: { type: String, required: true }
}, {
  timestamps: true
});

export default mongoose.model<INews>('News', NewsSchema);
