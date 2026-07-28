import { Request, Response } from 'express';
import TrainSchedule from '../models/TrainSchedule';

const DUMMY_TRAINS = [
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
  },
  {
    trainNumber: '7UP',
    trainName: 'Tezgam Express',
    sourceStation: 'Karachi Cantt',
    destinationStation: 'Rawalpindi',
    departureTime: '17:30',
    arrivalTime: '19:00',
    durationHours: '25h 30m',
    runningDays: 'Daily',
    trainType: 'Express',
    status: 'On Schedule',
    platform: '3',
    distanceKm: 1500,
    economyFare: 2100,
    standardFare: 3900,
    businessFare: 6800,
    classesAvailable: ['Economy', 'AC Standard', 'AC Business'],
    scheduleStations: [],
    routeOverview: 'Karachi -> Hyderabad -> Sukkur -> Multan -> Lahore -> Gujranwala -> Rawalpindi'
  },
  {
    trainNumber: '1UP',
    trainName: 'Khyber Mail',
    sourceStation: 'Karachi City',
    destinationStation: 'Peshawar Cantt',
    departureTime: '22:15',
    arrivalTime: '06:30',
    durationHours: '32h 15m',
    runningDays: 'Daily',
    trainType: 'Express',
    status: 'On Schedule',
    platform: '2',
    distanceKm: 1680,
    economyFare: 2400,
    standardFare: 4500,
    businessFare: 7800,
    classesAvailable: ['Economy', 'AC Standard', 'AC Business'],
    scheduleStations: [],
    routeOverview: 'Karachi -> Multan -> Lahore -> Rawalpindi -> Peshawar'
  },
  {
    trainNumber: 'F-901',
    trainName: 'CPEC Coal Special',
    sourceStation: 'Port Qasim',
    destinationStation: 'Sahiwal Power Plant',
    departureTime: '04:00',
    arrivalTime: '02:00',
    durationHours: '22h 00m',
    runningDays: 'Tue, Thu, Sat',
    trainType: 'Freight',
    status: 'Operational',
    platform: 'Freight Yard 4',
    distanceKm: 1100,
    economyFare: 0,
    standardFare: 0,
    businessFare: 0,
    classesAvailable: ['Freight Bulk Container'],
    scheduleStations: [],
    routeOverview: 'Port Qasim Freight Terminal -> Rohri Yard -> Khanewal Freight Loop -> Sahiwal'
  }
];

export const getAllTrains = async (req: Request, res: Response) => {
  try {
    const { source, destination, type, name, number } = req.query;
    let trains = await TrainSchedule.find().lean();
    if (trains.length === 0) {
      trains = DUMMY_TRAINS as any;
    }

    if (source) {
      trains = trains.filter(t => t.sourceStation.toLowerCase().includes((source as string).toLowerCase()));
    }
    if (destination) {
      trains = trains.filter(t => t.destinationStation.toLowerCase().includes((destination as string).toLowerCase()));
    }
    if (type && type !== 'All') {
      trains = trains.filter(t => t.trainType.toLowerCase() === (type as string).toLowerCase());
    }
    if (name) {
      trains = trains.filter(t => t.trainName.toLowerCase().includes((name as string).toLowerCase()));
    }
    if (number) {
      trains = trains.filter(t => t.trainNumber.toLowerCase().includes((number as string).toLowerCase()));
    }

    return res.status(200).json(trains);
  } catch (error) {
    return res.status(200).json(DUMMY_TRAINS);
  }
};

export const searchTrains = async (req: Request, res: Response) => {
  try {
    const { source = '', destination = '', type = 'All' } = req.query;
    let trains = await TrainSchedule.find().lean();
    if (trains.length === 0) {
      trains = DUMMY_TRAINS as any;
    }

    let filtered = trains;
    if (source) {
      filtered = filtered.filter(t => t.sourceStation.toLowerCase().includes((source as string).toLowerCase()));
    }
    if (destination) {
      filtered = filtered.filter(t => t.destinationStation.toLowerCase().includes((destination as string).toLowerCase()));
    }
    if (type && type !== 'All') {
      filtered = filtered.filter(t => t.trainType.toLowerCase() === (type as string).toLowerCase());
    }

    return res.status(200).json(filtered);
  } catch (error) {
    return res.status(200).json(DUMMY_TRAINS);
  }
};

export const getTrainDetails = async (req: Request, res: Response) => {
  const { id } = req.params;
  const train = DUMMY_TRAINS.find(t => t.trainNumber.toUpperCase() === id.toUpperCase() || t.trainName.toLowerCase().includes(id.toLowerCase())) || DUMMY_TRAINS[0];
  return res.status(200).json(train);
};

export const getTrainSchedule = async (req: Request, res: Response) => {
  const { number } = req.params;
  const train = DUMMY_TRAINS.find(t => t.trainNumber.toUpperCase() === number.toUpperCase()) || DUMMY_TRAINS[0];
  return res.status(200).json(train);
};

export const getFreightTrains = async (req: Request, res: Response) => {
  const freight = DUMMY_TRAINS.filter(t => t.trainType === 'Freight');
  return res.status(200).json(freight.length > 0 ? freight : [DUMMY_TRAINS[4]]);
};

export const getWeather = async (req: Request, res: Response) => {
  const location = (req.query.location as string) || 'Lahore';
  return res.status(200).json({
    location,
    temperature: '34°C',
    humidity: '58%',
    condition: 'Sunny',
    windSpeed: '12 km/h'
  });
};

export const getNamazTimings = async (req: Request, res: Response) => {
  const location = (req.query.location as string) || 'Lahore';
  return res.status(200).json({
    location,
    hijriDate: '14 Safar 1448 AH',
    gregorianDate: new Date().toLocaleDateString('en-US', { weekday: 'long', year: 'numeric', month: 'short', day: 'numeric' }),
    fajr: '04:15 AM',
    sunrise: '05:35 AM',
    dhuhr: '12:15 PM',
    asr: '04:45 PM',
    maghrib: '07:10 PM',
    isha: '08:35 PM'
  });
};

export const getNews = async (req: Request, res: Response) => {
  return res.status(200).json([
    { id: '1', title: 'New Coaches Added to Green Line', category: 'New Trains', date: 'Today', description: 'Pakistan Railways has added modern, comfortable Chinese passenger coaches to the Green Line Express.' },
    { id: '2', title: 'Railway Line Maintenance near Jhelum', category: 'Maintenance', date: 'Yesterday', description: 'Annual safety upgrades on the main up-line near Jhelum completed.' },
    { id: '3', title: 'PR Helpline 117 Upgraded with 24/7 Schedule Support', category: 'Announcements', date: '2 days ago', description: 'The official helpline 117 has been modernized with instant AI schedule inquiry systems.' }
  ]);
};

export const getBlogs = async (req: Request, res: Response) => {
  return res.status(200).json([
    { id: '101', title: 'A Journey Through Bolan Pass: Pakistan’s Architectural Marvel', author: 'PR Heritage Cell', readTimeMinutes: 5, snippet: 'Discover the scenic railway tracks winding through 21 tunnels in Balochistan.', content: 'The Bolan Pass railway line is one of the most remarkable engineering achievements...' }
  ]);
};
