# Pakistan Railways Train Companion - Enterprise Architecture Document

This document outlines the complete full-stack production architecture, deployment specs, security policies, and synchronization details for the Pakistan Railways Train Companion System.

## 1. System Topology Overview

The system consists of three central clients connected to a shared API & Telemetry WebSocket cluster:

```
                  +----------------------------------------------+
                  |            Google Maps SDK & APIs            |
                  +----------------------+-----------------------+
                                         |
                                         | (Overlays & Map Tiles)
                                         v
+------------------------+      +------------------+      +------------------------+
|   React Native App     |      |  Node.js API     |      |    React Admin Panel   |
|   (Mobile Passenger)   |<====>|  Gateway Server  |<====>|    (Fleet Operations)  |
+------------------------+      +--------+---------+      +------------------------+
                                         |
                                         |
                                         v
                            +--------------------------+
                            |      MongoDB Atlas       |
                            |   (Primary Data Store)   |
                            +--------------------------+
```

### Server Components
*   **Express Gateway Server**: Orchestrates secure REST JSON requests for auth, search engine, scheduled tables, stations cache, local weather caches, and prayer timetables.
*   **Socket.IO Server**: Dispatches bi-directional full-duplex event packets. Subscribes active mobile screens to real-time train telemetry tracking streams. Receives locomotive hardware updates via transponder relays.
*   **MongoDB Atlas Cluster**: Scalable primary document store utilizing strict schemas, document validations, and indexed coordinates.

---

## 2. Real-Time Telemetry & WebSocket Channels

To minimize overhead and battery consumption on mobile clients, the telemetry service utilizes an event-driven channel-based subscription framework over WebSockets.

### Event Channels Contract

#### `subscribe:train` (Client -> Server)
Emitted by the React Native client when loading the tracking details screen.
*   **Payload Schema**:
    ```json
    { "trainNumber": "7UP" }
    ```

#### `telemetry:update` (Server -> Client Broadcast)
Emitted by the Socket.IO cluster in real-time when locomotive hardware updates or dispatchers update schedules.
*   **Payload Schema**:
    ```json
    {
      "trainNumber": "7UP",
      "latitude": 30.6682,
      "longitude": 73.1114,
      "speedKmh": 85,
      "delayMinutes": 10,
      "currentStation": "Sahiwal Junction",
      "previousStation": "Karachi Cantt",
      "nextStation": "Lahore Junction",
      "journeyProgress": 0.79,
      "timestamp": 1783938482000
    }
    ```

#### `unsubscribe:train` (Client -> Server)
Emitted by the mobile client upon exit or when returning to home screens to preserve network bandwidth.
*   **Payload Schema**:
    ```json
    { "trainNumber": "7UP" }
    ```

---

## 3. JWT Security & Refresh Policies

Session tracking relies on double-token JWT Authentication:
1.  **Access Token (JWT)**: Valid for **15 minutes**. Dispatched in headers as `Authorization: Bearer <token>`.
2.  **Refresh Token (JWT)**: Valid for **7 days**. Securely stored on mobile client devices via native secure storage. Dispatched to `/api/auth/refresh` to restore session access.

Upon receiving an expired access token response (HTTP `401 Unauthorized`), the React Native client's custom Axios response interceptor holds further requests in a promise queue, triggers a refresh request to obtain a new access token, and then seamlessly retries original requests with no user-facing disruption.

---

## 4. Production Deployment Checklist

### API & Socket Server (Railway / Render)
1.  Set up production environment variables (`MONGO_URI`, `JWT_SECRET`, `JWT_REFRESH_SECRET`, `PORT`).
2.  Deploy backend source files via GitHub continuous integration.
3.  Configure Horizontal Auto-scaling with Redis adapter support for Socket.IO if traffic increases.

### Database (MongoDB Atlas)
1.  Whitelists API server IP range in Network Access policies.
2.  Seed initial fleet data via `npm run seed`.
3.  Confirm index optimization for query fields (`User.email`, `Train.trainNumber`, `Station.code`).

### Mobile (React Native - Play Store Production)
1.  Add Google Maps SDK Production API Keys inside Android app manifests.
2.  Configure Gradle release keystores.
3.  Bundle APK and AAB via standard CLI: `react-native bundle --platform android --dev false`.
