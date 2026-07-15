import mongoose, { Schema, Document } from 'mongoose';

export interface IBlog extends Document {
  title: string;
  category: string;
  readTime: string;
  content: string;
}

const BlogSchema = new Schema({
  title: { type: String, required: true },
  category: { type: String, required: true },
  readTime: { type: String, required: true },
  content: { type: String, required: true }
}, {
  timestamps: true
});

export default mongoose.model<IBlog>('Blog', BlogSchema);
