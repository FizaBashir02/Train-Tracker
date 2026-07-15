# Pakistan Railways Train Companion - Production Deployment Manual

This repository contains the complete enterprise full-stack codebase for the Pakistan Railways Train Companion. 

---

## 1. Directory Structure

```
train-tracker/
├── backend/            # Express.js REST API & Socket.IO real-time telemetry server
├── admin/              # Vite + React Admin Operations Dashboard
├── mobile/             # React Native Passenger Application
├── shared/             # TypeScript Schemas & Shared DTO Interfaces
├── docs/               # System & Enterprise Architecture Manuals
└── scripts/            # Database Seeders & Workspace Compilation Utility Scripts
```

---

## 2. MongoDB Atlas Configuration

For production scaling and efficient coordinates-based queries, configure your Atlas cluster as follows:

### Required Indexes Setup
Run these in your Atlas database command shell or establish them via Compass:

1.  **User Collection**:
    *   `email`: `{ "email": 1 }` (Unique, Collation: `{ locale: "en", strength: 2 }` for case-insensitivity)
2.  **Train Collection**:
    *   `trainNumber`: `{ "trainNumber": 1 }` (Unique)
    *   `route`: `{ "stations.stationCode": 1 }` (Multikey index to search trains by calling stations)
3.  **Station Collection**:
    *   `code`: `{ "code": 1 }` (Unique)
    *   `location`: `{ "coordinates": "2dsphere" }` (For proximity searches and geo-queries)
4.  **Favorite Collection**:
    *   `userEmail_trainNumber`: `{ "userEmail": 1, "trainNumber": 1 }` (Unique compound key)

### Security Checklist
*   Navigate to **Network Access** in the Atlas UI and add the static IP addresses of your deployed Railway/Render API gateway server.
*   Store your connection string inside `MONGO_URI` environment variable. Do **NOT** hardcode passwords in the codebase.

---

## 3. Firebase Cloud Messaging (FCM) Integration

For background push alerts, system warnings, and announcements:

### Service Configuration Steps
1.  Go to the **Firebase Console** and open your project.
2.  Navigate to **Project Settings > Service Accounts**.
3.  Click **Generate New Private Key** to download your service account credential JSON file.
4.  Encode this JSON file to Base64:
    ```bash
    cat path/to/service-account.json | base64
    ```
5.  Set the base64 string as the `FIREBASE_SERVICE_ACCOUNT_JSON_BASE64` environment variable in your production API server settings.

### Notification Topics
The server supports broadcasting alerts over the following FCM topics:
*   `all_announcements`: Broad updates about national track alignments, schedule modifications, or news blogs.
*   `train_<number>` (e.g. `train_7UP`): Delay warnings, platform shifts, or emergency stops for specific lines.

---

## 4. Google Maps SDK Setup

Interactive map screens utilize the official Google Maps SDK for mobile:

### Mobile Setup (Android)
1.  Obtain an API Key from the **Google Cloud Console > Credentials**.
2.  In the Console, search for and enable the following APIs:
    *   **Maps SDK for Android**
    *   **Directions API** (For calculating polyline coordinates between stations)
3.  Add the API key to your Android manifest configuration block:
    ```xml
    <meta-data
        android:name="com.google.android.geo.API_KEY"
        android:value="YOUR_PRODUCTION_GOOGLE_MAPS_API_KEY_HERE" />
    ```
4.  Restrict this key in the Google Cloud Console to only work on your specific Android package name (`com.aistudio.*`) and sign-in SHA-1 fingerprint for security.

---

## 5. Local Setup & Execution

### Option A: Using Docker Compose (Recommended)
Launch the entire system (Database, Redis Cache, Backend API Server) in one command:
```bash
docker-compose up --build
```

### Option B: Manual Workspace Build
Compile and start segments manually using our utility script:
```bash
# Set execution permissions
chmod +x scripts/build.sh

# Build all monorepo directories
./scripts/build.sh

# Run backend API server
cd backend && npm run start
```
