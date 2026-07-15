import mongoose from 'mongoose';
import dotenv from 'dotenv';
import Train from '../backend/src/models/Train';
import Station from '../backend/src/models/Station';
import Tracking from '../backend/src/models/Tracking';
import News from '../backend/src/models/News';
import Blog from '../backend/src/models/Blog';

dotenv.config();

const MONGO_URI = process.env.MONGO_URI || 'mongodb://localhost:27017/train-tracker';

const stationsData = [
  { code: 'LHR', name: 'Lahore Junction', address: 'Allama Iqbal Road, Lahore, Punjab', latitude: 31.5744, longitude: 74.3494, facilities: ['Waiting Lounge', 'Restrooms', 'Food Court', 'ATM', 'Ticketing Cabin'] },
  { code: 'KC', name: 'Karachi Cantt', address: 'Dr. Daudpota Road, Karachi, Sindh', latitude: 24.8532, longitude: 67.0347, facilities: ['Executive Waiting Lounge', 'VIP Rooms', 'Prayer Hall', 'Porter Service', 'Executive Cafe'] },
  { code: 'RWP', name: 'Rawalpindi Station', address: 'Saddar, Rawalpindi, Punjab', latitude: 33.6011, longitude: 73.0712, facilities: ['Waiting Rooms', 'Cloak Room', 'Refreshments stalls'] },
  { code: 'PEW', name: 'Peshawar Cantt', address: 'Peshawar Road, Peshawar, KPK', latitude: 34.0044, longitude: 71.5441, facilities: ['Basic Ticketing Booths', 'Waiting Bench Area'] },
  { code: 'SWL', name: 'Sahiwal Junction', address: 'Railway Road, Sahiwal, Punjab', latitude: 30.6682, longitude: 73.1114, facilities: ['Ticketing Counters', 'Restrooms'] }
];

const trainsData = [
  {
    trainNumber: '7UP',
    trainName: 'Tezgam Express',
    source: 'KC',
    destination: 'RWP',
    trainType: 'Express',
    departureTime: '17:30',
    arrivalTime: '14:15',
    totalDistanceKm: 1540,
    stops: [
      { stationCode: 'KC', stationName: 'Karachi Cantt', arrival: '17:30', departure: '17:30', stopDurationMinutes: 0, distanceKm: 0 },
      { stationCode: 'SWL', stationName: 'Sahiwal Junction', arrival: '08:30', departure: '08:40', stopDurationMinutes: 10, distanceKm: 1220 },
      { stationCode: 'LHR', stationName: 'Lahore Junction', arrival: '10:45', departure: '11:15', stopDurationMinutes: 30, distanceKm: 1380 },
      { stationCode: 'RWP', stationName: 'Rawalpindi Station', arrival: '14:15', departure: '14:15', stopDurationMinutes: 0, distanceKm: 1540 }
    ],
    isActive: true
  },
  {
    trainNumber: '9DN',
    trainName: 'Karakoram Express',
    source: 'KC',
    destination: 'LHR',
    trainType: 'Express',
    departureTime: '16:00',
    arrivalTime: '09:30',
    totalDistanceKm: 1210,
    stops: [
      { stationCode: 'KC', stationName: 'Karachi Cantt', arrival: '16:00', departure: '16:00', stopDurationMinutes: 0, distanceKm: 0 },
      { stationCode: 'LHR', stationName: 'Lahore Junction', arrival: '09:30', departure: '09:30', stopDurationMinutes: 0, distanceKm: 1210 }
    ],
    isActive: true
  }
];

const trackingData = [
  {
    trainNumber: '7UP',
    trainName: 'Tezgam Express',
    currentLatitude: 30.6682,
    currentLongitude: 73.1114,
    currentSpeedKmh: 85,
    delayMinutes: 0,
    currentStation: 'Sahiwal Junction',
    previousStation: 'Karachi Cantt',
    nextStation: 'Lahore Junction',
    distanceRemainingKm: 320,
    journeyProgress: 0.79
  },
  {
    trainNumber: '9DN',
    trainName: 'Karakoram Express',
    currentLatitude: 27.6811,
    currentLongitude: 68.8953,
    currentSpeedKmh: 98,
    delayMinutes: 15,
    currentStation: 'Rohri Junction',
    previousStation: 'Karachi Cantt',
    nextStation: 'Lahore Junction',
    distanceRemainingKm: 650,
    journeyProgress: 0.46
  }
];

const newsData = [
  { title: "Pakistan Railways Upgrades Core Track Network", category: "Infrastructure", date: "Today", summary: "In an effort to elevate transit speeds, Pakistan Railways initiates major rehabilitation of Main Line-1 (ML-1) infrastructure." },
  { title: "New Digital Portal Dispatched for Passengers", category: "E-Services", date: "Yesterday", summary: "Pakistan Railways launches upgraded modern web interfaces and companion apps to simplify route planning and live navigation." }
];

const blogsData = [
  { title: "Top 5 Scenic Rail Routes in Pakistan", category: "Travel Guide", readTime: "4 mins", content: "From the historical winding Bolan Pass tracks in Balochistan to the lush green agricultural panoramas of Punjab, Pakistan possesses some of the most culturally and visually stunning railway routes in Asia." }
];

async function seedDatabase() {
  try {
    await mongoose.connect(MONGO_URI);
    console.log('Successfully connected to MongoDB for seeding operation.');

    // Clear old instances
    await Station.deleteMany({});
    await Train.deleteMany({});
    await Tracking.deleteMany({});
    await News.deleteMany({});
    await Blog.deleteMany({});

    // Seed datasets
    await Station.insertMany(stationsData);
    await Train.insertMany(trainsData);
    await Tracking.insertMany(trackingData);
    await News.insertMany(newsData);
    await Blog.insertMany(blogsData);

    console.log('Database pre-seeded successfully! All models integrated.');
  } catch (error) {
    console.error('Failed to seed MongoDB database:', error);
  } finally {
    await mongoose.disconnect();
  }
}

seedDatabase();
