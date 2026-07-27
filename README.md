<div align="center">

# 🚆 Train Tracker

### Real-Time Pakistan Railways Tracking Application

A modern Android application for tracking Pakistan Railways trains in real-time using **Jetpack Compose**, **Node.js**, **Express.js**, **MongoDB Atlas**, **Socket.IO**, **Firebase Cloud Messaging**, and **Google Maps**.

![Platform](https://img.shields.io/badge/Platform-Android-green)
![Backend](https://img.shields.io/badge/Backend-Express.js-black)
![Database](https://img.shields.io/badge/Database-MongoDB-green)
![Language](https://img.shields.io/badge/Kotlin-Jetpack%20Compose-blue)
![License](https://img.shields.io/badge/License-MIT-orange)

</div>

---

# 📱 Overview

Train Tracker is a production-ready Android application that enables users to monitor Pakistan Railways trains in real time.

The application provides:

- Live Train Tracking
- Train Search
- Train Schedule
- Station Information
- Route Details
- Arrival & Departure Time
- Prayer Timings
- Weather Updates
- Railway News
- Blogs
- Notifications
- Favorite Trains
- Recent Searches
- User Authentication
- Google Maps Integration
- Push Notifications

---

# ✨ Features

## Authentication

- Email Registration
- Mobile Registration
- Login
- JWT Authentication
- Secure Password Hashing
- Refresh Tokens
- OTP Verification
- Logout

---

## Home

- Live Train Status
- Featured Trains
- Station Updates
- Weather
- Prayer Timings
- Railway News

---

## Live Tracking

- Search Train
- Track Current Location
- ETA
- Speed
- Distance
- Next Station
- Previous Station
- Route Visualization

---

## Google Maps

- Live Marker
- Route Polyline
- Current Position
- Zoom Controls
- User Location

---

## Stations

- Search Stations
- Nearby Stations
- Arrival Time
- Departure Time
- Platforms

---

## Train Schedule

- Complete Timetable
- Intermediate Stations
- Expected Delay
- Running Status

---

## Notifications

- Push Notifications
- Delay Alerts
- Arrival Alerts
- Departure Alerts
- Emergency Notices

---

## News

- Pakistan Railways News
- Blogs
- Announcements

---

## Prayer Timings

- Fajr
- Dhuhr
- Asr
- Maghrib
- Isha

Automatically updates according to the user's location.

---

## Settings

- Dark Mode
- Light Mode
- Urdu Language
- English Language
- Notification Settings
- About
- Privacy Policy

---

# 🏗 Architecture

```
Android App
      │
      │ HTTPS REST API
      ▼
Express.js Backend
      │
      ├──────── Socket.IO
      │
      ├──────── MongoDB Atlas
      │
      ├──────── Firebase Cloud Messaging
      │
      └──────── Google Maps API
```

---

# 🛠 Tech Stack

## Android

- Kotlin
- Jetpack Compose
- Material 3
- Navigation Compose
- Room Database
- Retrofit
- OkHttp
- Moshi
- Coroutines
- ViewModel
- StateFlow
- Google Maps SDK
- Firebase Messaging

---

## Backend

- Node.js
- Express.js
- MongoDB
- Mongoose
- JWT
- Socket.IO
- Nodemailer
- Express Validator
- Helmet
- Morgan
- Winston
- Compression

---

## Deployment

- Railway
- MongoDB Atlas
- Firebase
- Google Cloud

---

# 📂 Project Structure

```
Train-Tracker
│
├── app
│   ├── src
│   ├── ui
│   ├── screens
│   ├── repository
│   ├── network
│   ├── models
│   ├── navigation
│   └── MainActivity.kt
│
├── backend
│   ├── src
│   │
│   ├── controllers
│   ├── routes
│   ├── middleware
│   ├── services
│   ├── models
│   ├── config
│   ├── utils
│   └── index.ts
│
├── assets
├── screenshots
└── README.md
```

---

# ⚙ Environment Variables

## Backend `.env`

```
PORT=8080

NODE_ENV=production

MONGO_URI=YOUR_MONGODB_URI

JWT_SECRET=YOUR_SECRET

JWT_REFRESH_SECRET=YOUR_REFRESH_SECRET

GOOGLE_MAPS_API_KEY=YOUR_MAPS_KEY

SMTP_HOST=smtp.gmail.com

SMTP_PORT=587

SMTP_USER=YOUR_EMAIL

SMTP_PASS=YOUR_APP_PASSWORD

SMTP_FROM=YOUR_EMAIL

API_URL=https://your-backend.up.railway.app/api/

FIREBASE_SERVICE_ACCOUNT_JSON=YOUR_FIREBASE_JSON
```

---

# 🚀 Installation

## Clone Repository

```bash
git clone https://github.com/yourusername/train-tracker.git

cd train-tracker
```

---

# Backend Setup

```bash
cd backend

npm install
```

Create

```
.env
```

Run

```bash
npm run dev
```

Production

```bash
npm run build

npm start
```

---

# Android Setup

Open Android Studio

Open

```
app
```

Sync Gradle

Build

Run

---

# Build APK

Debug APK

```bash
cd app

gradlew assembleDebug
```

Windows

```powershell
.\gradlew assembleDebug
```

APK Location

```
app/build/outputs/apk/debug/app-debug.apk
```

---

Release APK

```powershell
.\gradlew assembleRelease
```

Output

```
app/build/outputs/apk/release/app-release.apk
```

---

# API Base URL

```
https://your-railway-url.up.railway.app/api/
```

---

# Railway Deployment

```bash
git push
```

Railway automatically

- Builds Docker Image
- Deploys Backend
- Connects MongoDB
- Starts Express Server

---

# MongoDB Collections

```
users

trains

stations

tracking

favorites

notifications

weather

prayers

news

blogs

reports

feedback
```

---

# Authentication

```
POST /api/auth/signup

POST /api/auth/login

POST /api/auth/logout

POST /api/auth/refresh
```

---

# Main APIs

```
GET /api/trains

GET /api/tracking

GET /api/stations

GET /api/weather

GET /api/prayers

GET /api/news

GET /api/blogs

POST /api/favorites
```

---

# Screenshots

```
screenshots/

home.png

tracking.png

map.png

login.png

profile.png
```

---

# Security

- JWT Authentication
- Password Hashing
- Helmet
- Input Validation
- CORS Protection
- Rate Limiting
- Environment Variables
- Secure API

---

# Future Improvements

- Offline Mode
- Wear OS Support
- Live GPS Streaming
- AI Delay Prediction
- Voice Search
- Ticket Booking
- Seat Availability
- Payment Gateway
- Admin Dashboard
- Analytics

---

# Contributing

```bash
Fork

Clone

Create Branch

Commit

Push

Create Pull Request
```

---

# License

MIT License

---

# Author

**Fiza Bashir**

Software Engineering Student

React Native Developer

Full Stack Developer

Android Developer

GitHub:
https://github.com/FizaBashir02

---

<div align="center">

Made with ❤️ using Kotlin, Express.js and MongoDB

</div>
