import { io, Socket } from 'socket.io-client';

const SOCKET_URL = 'https://api.train-companion.com'; // Production WebSocket domain

class LiveTrackingSocketService {
  private socket: Socket | null = null;

  connect(token: string) {
    if (this.socket?.connected) return;

    this.socket = io(SOCKET_URL, {
      transports: ['websocket'],
      auth: { token },
      reconnection: true,
      reconnectionAttempts: 10,
      reconnectionDelay: 2000,
    });

    this.socket.on('connect', () => {
      console.log('Socket.IO connected successfully to production cluster.');
    });

    this.socket.on('connect_error', (error) => {
      console.error('Socket.IO connection error:', error);
    });

    this.socket.on('disconnect', (reason) => {
      console.log('Socket.IO disconnected:', reason);
    });
  }

  subscribeToTrain(trainNumber: string, onUpdate: (data: any) => void) {
    if (!this.socket) return;

    this.socket.emit('subscribe:train', { trainNumber });
    this.socket.on('telemetry:update', onUpdate);
  }

  unsubscribeFromTrain(trainNumber: string) {
    if (!this.socket) return;

    this.socket.emit('unsubscribe:train', { trainNumber });
    this.socket.off('telemetry:update');
  }

  disconnect() {
    if (this.socket) {
      this.socket.disconnect();
      this.socket = null;
    }
  }
}

export const socketService = new LiveTrackingSocketService();
