import app from './app';
import { connectDB } from './config/db';
import { ENV } from './config/env';

const startServer = async () => {
  await connectDB();

  const PORT = parseInt(ENV.PORT, 10);
  const server = app.listen(PORT, '0.0.0.0', () => {
    console.log(`🚀 Pakistan Train Scheduling System Backend running on port ${PORT}`);
  });

  const handleShutdown = (signal: string) => {
    console.log(`Received ${signal}. Shutting down gracefully...`);
    server.close(() => {
      console.log('Server closed.');
      process.exit(0);
    });
  };

  process.on('SIGTERM', () => handleShutdown('SIGTERM'));
  process.on('SIGINT', () => handleShutdown('SIGINT'));
};

startServer();
