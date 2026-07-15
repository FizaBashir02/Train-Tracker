/**
 * Enterprise-grade shared data contracts ensuring absolute alignment between 
 * the Node.js API backend, Vite Admin Dashboard, and the React Native Mobile Companion App.
 */

export interface UserDto {
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  role: 'passenger' | 'admin' | 'conductor';
}

export interface AuthResponseDto {
  success: boolean;
  accessToken?: string;
  refreshToken?: string;
  user?: UserDto;
  message?: string;
}

export interface LiveTelemetryDto {
  trainNumber: string;
  trainName: string;
  latitude: number;
  longitude: number;
  speedKmh: number;
  delayMinutes: number;
  currentStation: string;
  previousStation: string;
  nextStation: string;
  journeyProgress: number;
  timestamp: number;
}

export interface StationDto {
  code: string;
  name: string;
  address: string;
  latitude: number;
  longitude: number;
  contactNumber: string;
  facilities: string[];
}

export interface WeatherDto {
  location: string;
  temperature: string;
  condition: string;
  humidity: string;
}

export interface PrayerTimesDto {
  islamicDate: string;
  fajr: string;
  dhuhr: string;
  asr: string;
  maghrib: string;
  isha: string;
  qiblaDirection: string;
}
