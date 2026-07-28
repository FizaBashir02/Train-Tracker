import { connectDB } from './config/db';
import TrainSchedule from './models/TrainSchedule';
import Station from './models/Station';
import Route from './models/Route';

const seedData = async () => {
  await connectDB();
  console.log('Seeding initial Pakistan Railways train data...');

  try {
    await TrainSchedule.deleteMany({});
    await Station.deleteMany({});
    await Route.deleteMany({});

    await TrainSchedule.insertMany([
      {
        trainNumber: '42DN',
        trainName: 'Karakoram Express',
        sourceStation: 'Lahore Junction',
        destinationStation: 'Karachi Cantt',
        departureTime: '15:00',
        arrivalTime: '10:15',
        durationHours: '19h 15m',
        runningDays: 'Daily',
        trainType: 'Express',
        status: 'On Schedule',
        platform: '2',
        distanceKm: 1210,
        economyFare: 2200,
        standardFare: 4200,
        businessFare: 7500,
        classesAvailable: ['Economy', 'AC Standard', 'AC Business'],
        scheduleStations: [
          { stationName: 'Lahore Junction', stationCode: 'LHR', arrivalTime: '15:00', departureTime: '15:30', stopDurationMinutes: 30, distanceKm: 0, platform: '2', isMajor: true },
          { stationName: 'Sahiwal', stationCode: 'SWL', arrivalTime: '17:45', departureTime: '17:50', stopDurationMinutes: 5, distanceKm: 170, platform: '1', isMajor: true },
          { stationName: 'Khanewal Junction', stationCode: 'KWL', arrivalTime: '19:20', departureTime: '19:30', stopDurationMinutes: 10, distanceKm: 280, platform: '3', isMajor: true },
          { stationName: 'Bahawalpur', stationCode: 'BWP', arrivalTime: '21:10', departureTime: '21:15', stopDurationMinutes: 5, distanceKm: 390, platform: '1', isMajor: true },
          { stationName: 'Rohri Junction', stationCode: 'ROH', arrivalTime: '02:30', departureTime: '02:50', stopDurationMinutes: 20, distanceKm: 700, platform: '2', isMajor: true },
          { stationName: 'Hyderabad Junction', stationCode: 'HDD', arrivalTime: '07:45', departureTime: '07:55', stopDurationMinutes: 10, distanceKm: 1030, platform: '1', isMajor: true },
          { stationName: 'Karachi Cantt', stationCode: 'KCT', arrivalTime: '10:15', departureTime: '10:15', stopDurationMinutes: 0, distanceKm: 1210, platform: '1', isMajor: true }
        ],
        routeOverview: 'Lahore -> Sahiwal -> Khanewal -> Bahawalpur -> Rohri -> Hyderabad -> Karachi'
      },
      {
        trainNumber: '5UP',
        trainName: 'Green Line Express',
        sourceStation: 'Karachi Cantt',
        destinationStation: 'Islamabad (Margalla)',
        departureTime: '22:00',
        arrivalTime: '20:15',
        durationHours: '22h 15m',
        runningDays: 'Daily',
        trainType: 'Express',
        status: 'On Schedule',
        platform: '1',
        distanceKm: 1520,
        economyFare: 3500,
        standardFare: 6500,
        businessFare: 11000,
        classesAvailable: ['AC Standard', 'AC Business'],
        scheduleStations: [
          { stationName: 'Karachi Cantt', stationCode: 'KCT', arrivalTime: '22:00', departureTime: '22:00', stopDurationMinutes: 0, distanceKm: 0, platform: '1', isMajor: true },
          { stationName: 'Hyderabad Junction', stationCode: 'HDD', arrivalTime: '00:15', departureTime: '00:25', stopDurationMinutes: 10, distanceKm: 180, platform: '1', isMajor: true },
          { stationName: 'Rohri Junction', stationCode: 'ROH', arrivalTime: '05:00', departureTime: '05:20', stopDurationMinutes: 20, distanceKm: 510, platform: '2', isMajor: true },
          { stationName: 'Bahawalpur', stationCode: 'BWP', arrivalTime: '09:40', departureTime: '09:45', stopDurationMinutes: 5, distanceKm: 820, platform: '1', isMajor: true },
          { stationName: 'Lahore Junction', stationCode: 'LHR', arrivalTime: '14:30', departureTime: '15:00', stopDurationMinutes: 30, distanceKm: 1210, platform: '3', isMajor: true },
          { stationName: 'Rawalpindi', stationCode: 'RWP', arrivalTime: '19:40', departureTime: '19:55', stopDurationMinutes: 15, distanceKm: 1500, platform: '2', isMajor: true },
          { stationName: 'Islamabad (Margalla)', stationCode: 'MGL', arrivalTime: '20:15', departureTime: '20:15', stopDurationMinutes: 0, distanceKm: 1520, platform: '1', isMajor: true }
        ],
        routeOverview: 'Karachi -> Hyderabad -> Rohri -> Bahawalpur -> Lahore -> Rawalpindi -> Islamabad'
      }
    ]);

    await Station.insertMany([
      { stationCode: 'LHR', name: 'Lahore Junction', city: 'Lahore', province: 'Punjab', category: 'Major Junction', totalPlatforms: 11, contactNumber: '042-99200421', address: 'Empress Road, Lahore', amenities: ['Waiting Room', 'VIP Lounge', 'Ticket Counter', 'ATM', 'Food Court', 'Mosque'] },
      { stationCode: 'KCT', name: 'Karachi Cantt', city: 'Karachi', province: 'Sindh', category: 'Major Terminal', totalPlatforms: 8, contactNumber: '021-99206062', address: 'Dr. Daud Pota Road, Karachi', amenities: ['Waiting Lounge', 'Ticket Counter', 'ATM', 'Food Plaza', 'Parking'] }
    ]);

    await Route.insertMany([
      { routeName: 'Main Line 1 (ML-1)', sourceCity: 'Karachi', destinationCity: 'Peshawar', totalDistanceKm: 1687, totalStationsCount: 184, keyViaStations: ['Hyderabad', 'Rohri', 'Multan', 'Lahore', 'Rawalpindi'], majorTrains: ['Green Line', 'Karakoram Express', 'Tezgam', 'Khyber Mail'] }
    ]);

    console.log('Database seeded successfully!');
    process.exit(0);
  } catch (error) {
    console.error('Seeding error:', error);
    process.exit(1);
  }
};

seedData();
