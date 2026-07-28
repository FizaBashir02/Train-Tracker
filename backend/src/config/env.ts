import dotenv from 'dotenv';
dotenv.config();

export val ENV = {
  PORT: process.env.PORT || '5000',
  MONGODB_URI: process.env.MONGODB_URI || 'mongodb://localhost:27017/pakistan_train_db',
  JWT_SECRET: process.env.JWT_SECRET || 'fallback_jwt_secret_key_12345',
  JWT_REFRESH_SECRET: process.env.JWT_REFRESH_SECRET || 'fallback_jwt_refresh_secret_key_12345',
  CORS_ORIGIN: process.env.CORS_ORIGIN || '*',
};
