# Pakistan Train Scheduling System

This application provides comprehensive train timetable management, schedule search, route lookup, station information, platform details, and fare calculation for Pakistan Railways.

> **Note:** This application operates strictly as a Train Scheduling System. It does **NOT** provide live train tracking or real-time GPS tracking.

## Core Features

- **Train Timetable & Search**: Search trains by name or number to view complete arrival, departure, stop duration, platform, and operating days schedule.
- **Route Lookup**: View complete station-by-station stop lists, distance in kilometers, and total journey durations.
- **Station Information**: Comprehensive listing of major Pakistan Railways stations with platform numbers, daily schedules, and amenities.
- **Fare Calculator**: Calculate detailed passenger fares based on train class (Economy, Business, AC Standard) and source/destination stations.
- **Local Utilities**: Features station-specific local weather forecasts and Islamic Namaz prayer timings.
- **Multi-Language Support**: Complete English and Urdu language interface support.

## Tech Stack

- **Platform**: Native Android (Kotlin)
- **UI Framework**: Jetpack Compose (Material Design 3)
- **Architecture**: MVVM with Repository Pattern
- **Local Database**: Room DB for offline schedule caching and favorites
- **Asynchronous Flow**: Kotlin Coroutines & Flow
