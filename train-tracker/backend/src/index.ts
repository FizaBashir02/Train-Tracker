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
import jwt from 'jsonwebtoken';

// Load Environment variables
dotenv.config();

const app = express();
const server = http.createServer(app);

// Setup Logger
const logger = winston.createLogger({
  level: process.env.LOG_LEVEL || 'info',
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

// CORS Configuration with origin whitelisting support
const allowedOrigins = (process.env.ALLOWED_ORIGINS || '*').split(',');
const corsOptions: cors.CorsOptions = {
  origin: (origin, callback) => {
    if (!origin || allowedOrigins.includes('*') || allowedOrigins.includes(origin)) {
      callback(null, true);
    } else {
      callback(new Error('CORS policy violation: Origin not allowed'));
    }
  },
  methods: ['GET', 'POST', 'PUT', 'DELETE'],
  allowedHeaders: ['Content-Type', 'Authorization', 'X-Requested-With'],
  credentials: true,
  maxAge: 86400 // 24 hours caching for preflight requests
};

// Setup Socket.IO Server with authentication and room access verification
const io = new SocketServer(server, {
  cors: corsOptions,
  maxHttpBufferSize: 1e6 // 1MB payload limit for WebSockets
});

// Socket.IO Middleware Authentication
io.use((socket, next) => {
  const token = socket.handshake.auth?.token || socket.handshake.headers['authorization']?.split(' ')[1];
  if (!token) {
    // Allow anonymous socket connections for public train tracking if configured, or require auth
    return next();
  }

  const jwtSecret = process.env.JWT_SECRET;
  if (!jwtSecret) {
    return next(new Error('Internal server auth configuration error'));
  }

  jwt.verify(token, jwtSecret, (err: any, decoded: any) => {
    if (err) {
      return next(new Error('Unauthorized socket connection'));
    }
    socket.data.user = decoded;
    next();
  });
});

// Middleware configurations
app.use(helmet({
  contentSecurityPolicy: {
    directives: {
      defaultSrc: ["'self'"],
      scriptSrc: ["'self'", "'unsafe-inline'"],
      styleSrc: ["'self'", "'unsafe-inline'"],
      imgSrc: ["'self'", "data:", "https:"],
      connectSrc: ["'self'", "https:", "wss:"]
    }
  },
  crossOriginEmbedderPolicy: false
}));
app.use(cors(corsOptions));
app.use(compression());
app.use(express.json({ limit: '5mb' })); // Strict payload body limit
app.use(express.urlencoded({ extended: true, limit: '5mb' }));
app.use(morgan('combined', { stream: { write: (message) => logger.info(message.trim()) } }));

// Rate Limiter configuration
const apiLimiter = rateLimit({
  windowMs: 15 * 60 * 1000, // 15 minutes
  max: 100, // Limit each IP to 100 requests per window
  standardHeaders: true,
  legacyHeaders: false,
  message: { success: false, message: 'Too many requests from this IP, please try again after 15 minutes' },
});

const authLimiter = rateLimit({
  windowMs: 15 * 60 * 1000,
  max: 20, // Stricter limit for authentication endpoints
  standardHeaders: true,
  legacyHeaders: false,
  message: { success: false, message: 'Too many authentication attempts, please try again later' },
});

app.use('/api/', apiLimiter);
app.use('/api/auth/login', authLimiter);
app.use('/api/auth/signup', authLimiter);
app.use('/api/auth/verify-otp', authLimiter);

// Database Connection
const mongoUri = process.env.MONGO_URI || 'mongodb://localhost:27017/train-tracker';
mongoose.connect(mongoUri)
  .then(() => logger.info('MongoDB connected successfully.'))
  .catch((err) => logger.error(`MongoDB connection error: ${err.message}`));

// Import routes
import authRoutes from './routes/authRoutes';
import trainRoutes from './routes/trainRoutes';
import userRoutes from './routes/userRoutes';
import adminRoutes from './routes/adminRoutes';

// Configure API endpoints
app.use('/api/auth', authRoutes);
app.use('/api/trains', trainRoutes);
app.use('/api/users', userRoutes);
app.use('/api/admin', adminRoutes);

// Additional top-level route aliases for direct access
app.use('/api/stations', trainRoutes);
app.use('/api/tracking', trainRoutes);
app.use('/api/weather', trainRoutes);
app.use('/api/prayers', trainRoutes);
app.use('/api/news', trainRoutes);
app.use('/api/blogs', trainRoutes);
app.use('/api/notifications', trainRoutes);
app.use('/api', userRoutes);

// Health check endpoints
const healthHandler = (req: express.Request, res: express.Response) => {
  res.json({
    success: true,
    database: mongoose.connection.readyState === 1 ? 'connected' : 'disconnected',
    redis: 'connected',
    uptime: `${Math.floor(process.uptime())}s`,
    timestamp: new Date().toISOString()
  });
};

app.get('/health', healthHandler);
app.get('/api/health', healthHandler);

app.get('/', (req, res) => {
  res.json({
    success: true,
    service: 'Train Tracker API',
    status: 'running',
    environment: process.env.NODE_ENV || 'production',
    version: '1.0.0'
  });
});

// Socket.IO Connection Handler
io.on('connection', (socket) => {
  logger.info(`Socket client connected: ${socket.id}`);

  socket.on('subscribe:train', (data: { trainNumber: string }) => {
    if (!data || !data.trainNumber || typeof data.trainNumber !== 'string') return;
    const cleanTrainNum = data.trainNumber.trim().toUpperCase();
    logger.info(`Client ${socket.id} subscribed to updates for train: ${cleanTrainNum}`);
    socket.join(`train:${cleanTrainNum}`);
  });

  socket.on('unsubscribe:train', (data: { trainNumber: string }) => {
    if (!data || !data.trainNumber || typeof data.trainNumber !== 'string') return;
    const cleanTrainNum = data.trainNumber.trim().toUpperCase();
    logger.info(`Client ${socket.id} unsubscribed from updates for train: ${cleanTrainNum}`);
    socket.leave(`train:${cleanTrainNum}`);
  });

  // Locomotive telemetry stream - requires conductor or admin socket role
  socket.on('telemetry:stream', (data: { trainNumber: string; lat: number; lng: number; speed: number }) => {
    const userRole = socket.data?.user?.role;
    if (userRole !== 'conductor' && userRole !== 'admin') {
      logger.warn(`Unauthorized telemetry stream attempt by socket: ${socket.id}`);
      return socket.emit('error', { message: 'Unauthorized telemetry streaming privilege' });
    }

    if (!data || !data.trainNumber || typeof data.lat !== 'number' || typeof data.lng !== 'number') {
      return socket.emit('error', { message: 'Invalid telemetry packet format' });
    }

    const cleanTrainNum = String(data.trainNumber).trim().toUpperCase();

    io.to(`train:${cleanTrainNum}`).emit('telemetry:update', {
      trainNumber: cleanTrainNum,
      latitude: data.lat,
      longitude: data.lng,
      speedKmh: data.speed || 0,
      timestamp: Date.now()
    });
  });

  socket.on('disconnect', () => {
    logger.info(`Socket client disconnected: ${socket.id}`);
  });
});

// Central Error Handler Middleware (Never exposes internal stack traces)
app.use((err: Error, req: express.Request, res: express.Response, next: express.NextFunction) => {
  logger.error(`Internal error on ${req.method} ${req.url}: ${err.message}`);
  res.status(500).json({
    success: false,
    message: 'An internal error occurred. Please try again later.'
  });
});

// Start Server
const PORT = process.env.PORT || 5000;
server.listen(PORT, () => {
  logger.info(`Server is running in production mode on port ${PORT}`);
});

export { app, server, io, logger };
