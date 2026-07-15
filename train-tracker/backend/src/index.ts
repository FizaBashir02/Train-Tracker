import express from 'express';
import http from 'http';
import { Server as SocketServer } from 'socket.io';
import mongoose from 'mongoose';
import cors from 'cors';
import helmet from 'helmet';
import morgan from 'morgan';
import compression from 'compression';
import rateLimit from 'express-rate-limit';
import winston from 'winston';
import dotenv from 'dotenv';

// Load Environment variables
dotenv.config();

// Create Express and HTTP Servers
const app = express();
const server = http.createServer(app);

// Setup Socket.IO Server
const io = new SocketServer(server, {
  cors: {
    origin: '*',
    methods: ['GET', 'POST'],
  },
});

// Setup Logger
const logger = winston.createLogger({
  level: 'info',
  format: winston.format.combine(
    winston.format.timestamp(),
    winston.format.json()
  ),
  transports: [
    new winston.transports.Console(),
    new winston.transports.File({ filename: 'logs/error.log', level: 'error' }),
    new winston.transports.File({ filename: 'logs/combined.log' }),
  ],
});

// Middleware configurations
app.use(helmet());
app.use(cors());
app.use(compression());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));
app.use(morgan('combined', { stream: { write: (message) => logger.info(message.trim()) } }));

// Rate Limiter configuration
const apiLimiter = rateLimit({
  windowMs: 15 * 60 * 1000, // 15 minutes
  max: 100, // Limit each IP to 100 requests per windowMs
  standardHeaders: true,
  legacyHeaders: false,
  message: 'Too many requests from this IP, please try again after 15 minutes',
});
app.use('/api/', apiLimiter);

// Database Connection
const mongoUri = process.env.MONGO_URI || 'mongodb://localhost:27017/train-tracker';
mongoose.connect(mongoUri)
  .then(() => logger.info('MongoDB Atlas connected successfully.'))
  .catch((err) => logger.error(`MongoDB connection error: ${err.message}`));

// Import routes
import authRoutes from './routes/authRoutes';
import trainRoutes from './routes/trainRoutes';
import userRoutes from './routes/userRoutes';
import adminRoutes from './routes/adminRoutes';

// Configure Api endpoints
app.use('/api/auth', authRoutes);
app.use('/api/trains', trainRoutes);
app.use('/api/users', userRoutes);
app.use('/api/admin', adminRoutes);

// Base route
app.get('/', (req, res) => {
  res.json({ status: 'healthy', service: 'Pakistan Railways Train Tracker Backend API', timestamp: new Date() });
});

// Socket.IO Connection Handler
io.on('connection', (socket) => {
  logger.info(`Socket client connected: ${socket.id}`);

  socket.on('subscribe:train', (data: { trainNumber: string }) => {
    logger.info(`Client ${socket.id} subscribed to updates for train: ${data.trainNumber}`);
    socket.join(`train:${data.trainNumber}`);
  });

  socket.on('unsubscribe:train', (data: { trainNumber: string }) => {
    logger.info(`Client ${socket.id} unsubscribed from updates for train: ${data.trainNumber}`);
    socket.leave(`train:${data.trainNumber}`);
  });

  // Locomotive GPS simulation or hardware stream integration
  socket.on('telemetry:stream', (data: { trainNumber: string; lat: number; lng: number; speed: number }) => {
    logger.info(`Incoming GPS telemetry for train #${data.trainNumber}: [${data.lat}, ${data.lng}], speed: ${data.speed}km/h`);
    
    // Broadcast real-time location packets directly to all subscribed passenger clients
    io.to(`train:${data.trainNumber}`).emit('telemetry:update', {
      trainNumber: data.trainNumber,
      latitude: data.lat,
      longitude: data.lng,
      speedKmh: data.speed,
      timestamp: Date.now()
    });
  });

  socket.on('disconnect', () => {
    logger.info(`Socket client disconnected: ${socket.id}`);
  });
});

// Global Error Handler Middleware
app.use((err: Error, req: express.Request, res: express.Response, next: express.NextFunction) => {
  logger.error(`Internal server error: ${err.message}`);
  res.status(500).json({ success: false, message: 'Internal Server Error' });
});

// Start Server
const PORT = process.env.PORT || 5000;
server.listen(PORT, () => {
  logger.info(`Server is running in production mode on port ${PORT}`);
});

export { app, server, io, logger };
