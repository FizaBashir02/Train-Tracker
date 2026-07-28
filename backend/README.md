# Pakistan Train Scheduling System - Express Backend

Production-ready Node.js + Express + TypeScript REST API backend for the Pakistan Train Scheduling System with MongoDB Atlas and JWT authentication.

## API Endpoints

### Auth
- `POST /api/auth/register` (or `/auth/signup`)
- `POST /api/auth/login`
- `POST /api/auth/verify-otp`
- `POST /api/auth/refresh`
- `POST /api/auth/register-fcm-token`

### Users (Protected)
- `GET /api/users/profile`
- `PUT /api/users/profile`
- `POST /api/users/profile/change-password`
- `POST /api/users/profile/picture`
- `DELETE /api/users/profile`

### Schedules & Trains
- `GET /api/trains` - List trains / search with filters
- `GET /api/trains/search` - Search trains source/destination
- `GET /api/trains/:id` - Get train details
- `GET /api/trains/:number/schedule` - Get schedule for train number
- `GET /api/trains/freight` - List freight trains
- `GET /api/trains/weather` - Get weather for Pakistani city
- `GET /api/trains/prayer` - Get Namaz prayer timings for Pakistani city
- `GET /api/trains/news` - Get official railway announcements
- `GET /api/trains/blogs` - Get railway blogs & news

### Stations & Routes
- `GET /api/stations` - Search & list stations
- `GET /api/stations/:id` - Station details
- `GET /api/routes` - Search & list routes
- `GET /api/routes/:id` - Route details

## Environment Setup
Set `MONGODB_URI`, `JWT_SECRET`, and `JWT_REFRESH_SECRET` in your environment or `.env` file.
