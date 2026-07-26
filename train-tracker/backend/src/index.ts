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

// Setup Logger (Console only for cloud environments like Railway)
const logger = winston.createLogger({
  level: process.env.LOG_LEVEL || 'info',
  format: winston.format.combine(
    winston.format.timestamp(),
    winston.format.json()
  ),
  transports: [
    new winston.transports.Console({
      format: winston.format.combine(
        winston.format.colorize(),
        winston.format.simple()
      )
    })
  ],
});

const app = express();
const server = http.createServer(app);

// Global Unhandled Process Exception & Signal Handlers to prevent container shutdown
process.on('uncaughtException', (err) => {
  console.error('[PROCESS] Uncaught Exception caught (prevented crash):', err);
  logger.error(`Uncaught Exception: ${err?.message || err}`);
});

process.on('unhandledRejection', (reason, promise) => {
  console.error('[PROCESS] Unhandled Rejection at:', promise, 'reason:', reason);
  logger.error(`Unhandled Rejection: ${reason}`);
});

process.on('SIGINT', () => {
  console.log('[PROCESS] SIGINT signal received. Server will remain active.');
  logger.info('[PROCESS] SIGINT signal handled; server remaining active.');
});

process.on('SIGTERM', () => {
  console.log('[PROCESS] SIGTERM signal received. Server will remain active.');
  logger.info('[PROCESS] SIGTERM signal handled; server remaining active.');
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
app.use(express.json({ limit: '5mb' }));
app.use(express.urlencoded({ extended: true, limit: '5mb' }));
app.use(morgan('combined', { stream: { write: (message) => logger.info(message.trim()) } }));

// Rate Limiter configuration
const apiLimiter = rateLimit({
  windowMs: 15 * 60 * 1000,
  max: 100,
  standardHeaders: true,
  legacyHeaders: false,
  message: { success: false, message: 'Too many requests from this IP, please try again after 15 minutes' },
});

const authLimiter = rateLimit({
  windowMs: 15 * 60 * 1000,
  max: 20,
  standardHeaders: true,
  legacyHeaders: false,
  message: { success: false, message: 'Too many authentication attempts, please try again later' },
});

app.use('/api/', apiLimiter);
app.use('/api/auth/login', authLimiter);
app.use('/api/auth/signup', authLimiter);
app.use('/api/auth/verify-otp', authLimiter);

// Safely load and register routes in try/catch blocks
let authRoutes: any;
let trainRoutes: any;
let userRoutes: any;
let adminRoutes: any;

try {
  authRoutes = require('./routes/authRoutes').default;
} catch (err: any) {
  logger.error(`Failed to load authRoutes: ${err?.message || err}`);
}

try {
  trainRoutes = require('./routes/trainRoutes').default;
} catch (err: any) {
  logger.error(`Failed to load trainRoutes: ${err?.message || err}`);
}

try {
  userRoutes = require('./routes/userRoutes').default;
} catch (err: any) {
  logger.error(`Failed to load userRoutes: ${err?.message || err}`);
}

try {
  adminRoutes = require('./routes/adminRoutes').default;
} catch (err: any) {
  logger.error(`Failed to load adminRoutes: ${err?.message || err}`);
}

if (authRoutes) {
  app.use('/api/auth', authRoutes);
}

if (trainRoutes) {
  app.use('/api/trains', trainRoutes);
  app.use('/api/stations', trainRoutes);
  app.use('/api/tracking', trainRoutes);
  app.use('/api/weather', trainRoutes);
  app.use('/api/prayers', trainRoutes);
  app.use('/api/news', trainRoutes);
  app.use('/api/blogs', trainRoutes);
  app.use('/api/notifications', trainRoutes);
}

if (userRoutes) {
  app.use('/api/users', userRoutes);
  app.use('/api', userRoutes);
}

if (adminRoutes) {
  app.use('/api/admin', adminRoutes);
}

// MongoDB connection state, options, and auto-reconnect listeners
async function syncMongoIndexes() {
  try {
    const modelNames = mongoose.modelNames();
    for (const name of modelNames) {
      try {
        await mongoose.model(name).syncIndexes();
        logger.info(`[MONGODB] Synchronized indexes for model: ${name}`);
      } catch (idxErr: any) {
        logger.warn(`[MONGODB] Index sync warning for model ${name}: ${idxErr?.message || idxErr}`);
      }
    }
  } catch (err: any) {
    logger.warn(`[MONGODB] Global index sync error: ${err?.message || err}`);
  }
}

mongoose.connection.on('connected', () => {
  logger.info('[MONGODB] Connection established successfully.');
  console.log('[MONGODB] Connected to MongoDB database.');
  syncMongoIndexes().catch((err) => {
    logger.warn(`[MONGODB] Background index sync error: ${err?.message || err}`);
  });
});

mongoose.connection.on('error', (err) => {
  logger.error(`[MONGODB] Database connection error: ${err?.message || err}. Server will remain active.`);
  console.error(`[MONGODB] Database error: ${err?.message || err}`);
});

mongoose.connection.on('disconnected', () => {
  logger.warn('[MONGODB] Connection lost. Mongoose will attempt auto-reconnect in background...');
  console.warn('[MONGODB] Disconnected from database. Auto-reconnecting in background...');
});

mongoose.connection.on('reconnected', () => {
  logger.info('[MONGODB] Connection restored successfully.');
  console.log('[MONGODB] Reconnected to MongoDB.');
});

async function initMongoDB() {
  const mongoUri = process.env.MONGO_URI || 'mongodb://localhost:27017/train-tracker';
  const mongoOptions: mongoose.ConnectOptions = {
    serverSelectionTimeoutMS: 10000,
    socketTimeoutMS: 45000,
    connectTimeoutMS: 10000,
    maxPoolSize: 10,
    autoIndex: true
  };

  try {
    await mongoose.connect(mongoUri, mongoOptions);
    logger.info('[MONGODB] Initial connection call executed.');
  } catch (err: any) {
    logger.error(`[MONGODB] Initial connection attempt failed: ${err?.message || err}. Continuing server execution; auto-reconnecting in background...`);
    console.error(`[MONGODB-WARNING] Initial connection failed. Continuing server startup...`);
  }
}

// Redis connection state & safe initialization
let redisStatus: 'connected' | 'disconnected' | 'disabled' = 'disabled';
let redisClient: any = null;

async function initRedis() {
  const redisUrl = process.env.REDIS_URL || process.env.REDIS_HOST;
  if (!redisUrl) {
    logger.info('[REDIS] Redis URL not configured in environment. Continuing without Redis cache.');
    redisStatus = 'disabled';
    return;
  }

  try {
    let RedisLib: any = null;
    try {
      RedisLib = require('ioredis');
    } catch {
      try {
        RedisLib = require('redis');
      } catch {
        RedisLib = null;
      }
    }

    if (!RedisLib) {
      logger.warn('[REDIS] Redis library (ioredis/redis) is not installed. Proceeding without Redis.');
      redisStatus = 'disabled';
      return;
    }

    redisStatus = 'disconnected';

    if (typeof RedisLib === 'function') {
      redisClient = new RedisLib(redisUrl, {
        retryStrategy: (times: number) => {
          logger.warn(`[REDIS] Connection unavailable. Auto-reconnection attempt #${times}...`);
          return Math.min(times * 1000, 10000);
        },
        maxRetriesPerRequest: null,
        enableOfflineQueue: false
      });

      redisClient.on('connect', () => {
        redisStatus = 'connected';
        logger.info('[REDIS] Connected successfully to Redis server.');
      });

      redisClient.on('error', (err: any) => {
        redisStatus = 'disconnected';
        logger.warn(`[REDIS] Redis client error: ${err?.message || err}. Server will continue running.`);
      });
    } else if (RedisLib.createClient) {
      redisClient = RedisLib.createClient({ url: redisUrl });
      redisClient.on('error', (err: any) => {
        redisStatus = 'disconnected';
        logger.warn(`[REDIS] Redis client error: ${err?.message || err}. Server will continue running.`);
      });
      redisClient.on('connect', () => {
        redisStatus = 'connected';
        logger.info('[REDIS] Connected successfully to Redis server.');
      });
      await redisClient.connect().catch((err: any) => {
        redisStatus = 'disconnected';
        logger.warn(`[REDIS] Initial Redis connection failed: ${err?.message || err}. Reconnecting automatically in background...`);
      });
    }
  } catch (err: any) {
    redisStatus = 'disconnected';
    logger.warn(`[REDIS] Failed to initialize Redis: ${err?.message || err}. Server continuing without Redis.`);
  }
}

// Firebase connection state & safe initialization
let firebaseStatus: 'initialized' | 'disabled' = 'disabled';
let firebaseAdminApp: any = null;

async function initFirebase() {
  const fs = require('fs');
  const path = require('path');

  const possibleCredentialPaths = [
    process.env.GOOGLE_APPLICATION_CREDENTIALS,
    process.env.FIREBASE_SERVICE_ACCOUNT_PATH,
    path.join(__dirname, '../google-services.json'),
    path.join(__dirname, '../serviceAccountKey.json'),
    path.join(process.cwd(), 'google-services.json'),
    path.join(process.cwd(), 'serviceAccountKey.json')
  ].filter(Boolean);

  let credentialPath: string | null = null;
  for (const p of possibleCredentialPaths) {
    if (typeof p === 'string' && fs.existsSync(p)) {
      credentialPath = p;
      break;
    }
  }

  const rawJsonEnv = process.env.FIREBASE_SERVICE_ACCOUNT_JSON || process.env.FIREBASE_CONFIG;

  if (!credentialPath && !rawJsonEnv) {
    logger.warn('[FIREBASE-WARNING] google-services.json or service account credentials not found. Continuing server startup without Firebase...');
    console.warn('[FIREBASE-WARNING] google-services.json or service account credentials missing. Server will continue startup...');
    firebaseStatus = 'disabled';
    return;
  }

  try {
    let admin: any = null;
    try {
      admin = require('firebase-admin');
    } catch {
      logger.warn('[FIREBASE-WARNING] firebase-admin package is not installed. Continuing server startup without Firebase...');
      firebaseStatus = 'disabled';
      return;
    }

    if (!admin.apps.length) {
      let certObj: any = null;
      if (rawJsonEnv) {
        try {
          certObj = typeof rawJsonEnv === 'string' ? JSON.parse(rawJsonEnv) : rawJsonEnv;
        } catch {
          certObj = null;
        }
      }

      if (credentialPath) {
        firebaseAdminApp = admin.initializeApp({
          credential: admin.credential.cert(credentialPath)
        });
      } else if (certObj) {
        firebaseAdminApp = admin.initializeApp({
          credential: admin.credential.cert(certObj)
        });
      } else {
        firebaseAdminApp = admin.initializeApp();
      }
    } else {
      firebaseAdminApp = admin.app();
    }

    firebaseStatus = 'initialized';
    logger.info('[FIREBASE] Firebase Admin initialized successfully.');
  } catch (err: any) {
    firebaseStatus = 'disabled';
    logger.warn(`[FIREBASE-WARNING] Failed to initialize Firebase: ${err?.message || err}. Continuing server startup...`);
  }
}

// Health check endpoints
const healthHandler = (req: express.Request, res: express.Response) => {
  res.json({
    success: true,
    database: mongoose.connection.readyState === 1 ? 'connected' : 'disconnected',
    redis: redisStatus,
    firebase: firebaseStatus,
    uptime: `${Math.floor(process.uptime())}s`,
    timestamp: new Date().toISOString()
  });
};

app.get('/health', healthHandler);
app.get('/api/health', healthHandler);
app.get('/live', (req, res) => res.status(200).json({ status: 'alive' }));
app.get('/ready', healthHandler);

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

// 404 Not Found Middleware
app.use((req: express.Request, res: express.Response) => {
  res.status(404).json({
    success: false,
    message: `Cannot ${req.method} ${req.url}`
  });
});

// Central Error Handler Middleware
app.use((err: Error, req: express.Request, res: express.Response, next: express.NextFunction) => {
  logger.error(`Internal error on ${req.method} ${req.url}: ${err.message}`);
  res.status(500).json({
    success: false,
    message: 'An internal error occurred. Please try again later.'
  });
});

// Startup logic wrapped in try/catch to ensure server listener runs cleanly
async function startServer() {
  try {
    console.log('[STARTUP] Step 1: Initializing configuration & middleware...');
    
    console.log('[STARTUP] Step 2: Connecting to MongoDB database...');
    await initMongoDB();

    console.log('[STARTUP] Step 2.5: Initializing Redis (if configured)...');
    try {
      await initRedis();
    } catch (redisInitErr: any) {
      logger.warn(`[REDIS] Exception during initRedis: ${redisInitErr?.message || redisInitErr}. Server continuing normally.`);
    }

    console.log('[STARTUP] Step 2.6: Initializing Firebase (if configured)...');
    try {
      await initFirebase();
    } catch (fbInitErr: any) {
      logger.warn(`[FIREBASE-WARNING] Exception during initFirebase: ${fbInitErr?.message || fbInitErr}. Server continuing normally.`);
    }

    console.log('[STARTUP] Step 3: Starting HTTP & WebSocket Server...');
    const PORT = process.env.PORT || 8080;
    server.listen(Number(PORT), '0.0.0.0', () => {
      logger.info(`Server is running in production mode on port ${PORT}`);
      console.log(`[STARTUP] Step 3: Server is running in production mode on port ${PORT}`);
      console.log('[STARTUP] Startup sequence completed successfully.');
    });
  } catch (startupErr: any) {
    console.error('[STARTUP] Critical exception in startServer wrapper:', startupErr);
    const PORT = process.env.PORT || 8080;
    if (!server.listening) {
      server.listen(Number(PORT), '0.0.0.0', () => {
        console.log(`[STARTUP] Fallback HTTP server listening on port ${PORT}`);
      });
    }
  }
}

startServer();

export { app, server, io, logger };

