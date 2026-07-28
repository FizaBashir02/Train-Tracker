import { Request, Response } from 'express';
import Train from '../models/Train';
import Station from '../models/Station';
import Schedule from '../models/Schedule';
import Route from '../models/Route';
import WeatherCache from '../models/WeatherCache';
import PrayerCache from '../models/PrayerCache';
import News from '../models/News';
import Blog from '../models/Blog';
import Notification from '../models/Notification';
import { dummyTrainsList, generateExtraTrains, dummyStations, dummyRoutes } from '../seed/seedData';

const escapeRegex = (str: string) => {
  return str.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
};

// GET /api/trains or GET /api/trains/search
export const searchTrains = async (req: Request, res: Response) => {
  try {
    const { name, number, source, destination, dest, status, type } = req.query;
    
    let query: any = {};
    if (name && typeof name === 'string') {
      query.trainName = new RegExp(escapeRegex(name.trim()), 'i');
    }
    if (number && typeof number === 'string') {
      query.trainNumber = new RegExp(escapeRegex(number.trim()), 'i');
    }
    const srcVal = source || req.query.sourceStation;
    if (srcVal && typeof srcVal === 'string' && srcVal.trim() !== '') {
      query.sourceStation = new RegExp(escapeRegex((srcVal as string).trim()), 'i');
    }
    const dstVal = destination || dest || req.query.destinationStation;
    if (dstVal && typeof dstVal === 'string' && dstVal.trim() !== '') {
      query.destinationStation = new RegExp(escapeRegex((dstVal as string).trim()), 'i');
    }
    if (status && typeof status === 'string' && status !== 'All') {
      query.status = status.trim();
    }
    if (type && typeof type === 'string' && type !== 'All') {
      query.trainType = type.trim();
    }

    let trains = await Train.find(query).limit(100);
    
    // Fallback if DB is empty / offline
    if (trains.length === 0 && Object.keys(query).length === 0) {
      trains = generateExtraTrains() as any;
    }

    const results = trains.map(t => ({
      id: (t as any)._id || t.trainNumber,
      trainNumber: t.trainNumber,
      trainName: t.trainName,
      trainType: t.trainType,
      sourceStation: t.sourceStation || (t as any).source,
      destinationStation: t.destinationStation || (t as any).destination,
      departureTime: t.departureTime,
      arrivalTime: t.arrivalTime,
      duration: t.duration || "12h 00m",
      distance: t.distance || 500,
      status: t.status || "On Time",
      platform: t.platform || "1",
      fareEconomy: t.fareEconomy || 1500,
      fareBusiness: t.fareBusiness || 3500,
      fareAC: t.fareAC || 5500,
      daysOfOperation: t.daysOfOperation || ["Daily"],
      intermediateStations: t.intermediateStations || [],
      route: t.route || "Main Line 1",
      availableSeats: t.availableSeats || 40,
      lastUpdated: t.lastUpdated || "Just now"
    }));

    return res.status(200).json(results);
  } catch (error: any) {
    // Fallback to static dummy array on DB error
    const fallback = generateExtraTrains();
    return res.status(200).json(fallback);
  }
};

// GET /api/trains/:id
export const getTrainById = async (req: Request, res: Response) => {
  try {
    const { id } = req.params;
    if (!id) {
      return res.status(400).json({ success: false, message: 'Train ID or Number required' });
    }

    const cleanId = String(id).trim().toUpperCase();
    let train = await Train.findOne({ 
      $or: [{ _id: id.match(/^[0-9a-fA-F]{24}$/) ? id : null }, { trainNumber: cleanId }] 
    });

    if (!train) {
      const extra = generateExtraTrains();
      const found = extra.find(t => t.trainNumber.toUpperCase() === cleanId || t.trainName.toLowerCase().includes(cleanId.toLowerCase()));
      if (found) {
        return res.status(200).json(found);
      }
      return res.status(404).json({ success: false, message: `Train ${cleanId} not found` });
    }

    return res.status(200).json(train);
  } catch (error: any) {
    return res.status(500).json({ success: false, message: 'Error fetching train details' });
  }
};

// GET /api/trains/schedule or GET /api/trains/:id/schedule
export const getTrainSchedule = async (req: Request, res: Response) => {
  try {
    const trainNumber = req.params.trainNumber || req.params.id || (req.query.trainNumber as string);
    if (!trainNumber) {
      const allSchedules = generateExtraTrains().map(t => ({
        trainNumber: t.trainNumber,
        trainName: t.trainName,
        sourceStation: t.sourceStation,
        destinationStation: t.destinationStation,
        duration: t.duration,
        stations: t.intermediateStations
      }));
      return res.status(200).json(allSchedules);
    }

    const cleanNum = String(trainNumber).trim().toUpperCase();
    const train = await Train.findOne({ trainNumber: cleanNum });

    if (!train) {
      const extra = generateExtraTrains();
      const found = extra.find(t => t.trainNumber.toUpperCase() === cleanNum);
      if (found) {
        return res.status(200).json({
          trainName: found.trainName,
          trainNumber: found.trainNumber,
          sourceStation: found.sourceStation,
          destinationStation: found.destinationStation,
          stations: found.intermediateStations,
          totalStops: found.intermediateStations.length,
          totalDistanceKm: found.distance,
          totalJourneyTime: found.duration
        });
      }
      return res.status(404).json({ success: false, message: `Schedule not found for train ${cleanNum}` });
    }

    return res.status(200).json({
      trainName: train.trainName,
      trainNumber: train.trainNumber,
      sourceStation: train.sourceStation,
      destinationStation: train.destinationStation,
      stations: train.intermediateStations,
      totalStops: train.intermediateStations.length,
      totalDistanceKm: train.distance,
      totalJourneyTime: train.duration
    });
  } catch (error: any) {
    return res.status(500).json({ success: false, message: 'Server error retrieving schedule' });
  }
};

// GET /api/stations
export const getStations = async (req: Request, res: Response) => {
  try {
    const { search } = req.query;
    let query: any = {};
    if (search && typeof search === 'string') {
      const regex = new RegExp(escapeRegex(search.trim()), 'i');
      query = { $or: [{ name: regex }, { code: regex }] };
    }

    let stations = await Station.find(query);
    if (stations.length === 0 && !search) {
      stations = dummyStations as any;
    }

    return res.status(200).json(stations);
  } catch (error: any) {
    return res.status(200).json(dummyStations);
  }
};

// GET /api/stations/:id
export const getStationById = async (req: Request, res: Response) => {
  try {
    const { id } = req.params;
    if (!id) {
      return res.status(400).json({ success: false, message: 'Station code or ID required' });
    }

    const cleanCode = String(id).trim().toUpperCase();
    let station = await Station.findOne({
      $or: [{ _id: id.match(/^[0-9a-fA-F]{24}$/) ? id : null }, { code: cleanCode }]
    });

    if (!station) {
      const found = dummyStations.find(s => s.code.toUpperCase() === cleanCode || s.name.toLowerCase().includes(cleanCode.toLowerCase()));
      if (found) {
        return res.status(200).json(found);
      }
      return res.status(404).json({ success: false, message: `Station ${cleanCode} not found` });
    }

    return res.status(200).json(station);
  } catch (error: any) {
    return res.status(500).json({ success: false, message: 'Error retrieving station details' });
  }
};

// GET /api/routes
export const getRoutes = async (req: Request, res: Response) => {
  try {
    let routes = await Route.find();
    if (routes.length === 0) {
      routes = dummyRoutes as any;
    }
    return res.status(200).json(routes);
  } catch (error: any) {
    return res.status(200).json(dummyRoutes);
  }
};

// GET /api/routes/:id
export const getRouteById = async (req: Request, res: Response) => {
  try {
    const { id } = req.params;
    const cleanId = String(id).trim().toUpperCase();
    let route = await Route.findOne({
      $or: [{ _id: id.match(/^[0-9a-fA-F]{24}$/) ? id : null }, { routeId: cleanId }]
    });

    if (!route) {
      const found = dummyRoutes.find(r => r.routeId === cleanId);
      if (found) {
        return res.status(200).json(found);
      }
      return res.status(404).json({ success: false, message: `Route ${cleanId} not found` });
    }

    return res.status(200).json(route);
  } catch (error: any) {
    return res.status(500).json({ success: false, message: 'Error retrieving route' });
  }
};

// GET /api/trains/freight
export const getFreightTrains = async (req: Request, res: Response) => {
  try {
    let freights = await Train.find({ trainType: 'Freight' });
    if (freights.length === 0) {
      freights = generateExtraTrains().filter(t => t.trainType === 'Freight') as any;
    }
    return res.status(200).json(freights);
  } catch (error: any) {
    const freights = generateExtraTrains().filter(t => t.trainType === 'Freight');
    return res.status(200).json(freights);
  }
};

export const getWeather = async (req: Request, res: Response) => {
  const loc = (req.query.location as string) || "Lahore";
  return res.status(200).json({
    location: loc,
    temperature: "32°C",
    humidity: "55%",
    condition: "Sunny"
  });
};

export const getPrayerTimes = async (req: Request, res: Response) => {
  const loc = (req.query.location as string) || "Lahore";
  return res.status(200).json({
    islamicDate: "12 Safar 1448 AH",
    fajr: "04:15 AM",
    sunrise: "05:30 AM",
    dhuhr: "12:15 PM",
    asr: "04:45 PM",
    maghrib: "07:05 PM",
    isha: "08:30 PM",
    qiblaDirection: "261° W"
  });
};

export const getNews = async (req: Request, res: Response) => {
  return res.status(200).json([
    { title: "Green Line Express Schedule Updated", category: "Announcements", summary: "Pakistan Railways updates Green Line timetable for seamless journeys." },
    { title: "New Digital Ticketing Counters Active", category: "New Trains", summary: "Self-service kiosk facilities introduced at Lahore & Karachi Cantt." }
  ]);
};

export const getBlogs = async (req: Request, res: Response) => {
  return res.status(200).json([
    { title: "Exploring ML-1 Railway Heritage", category: "Travel", content: "A deep dive into Pakistan's historic railway track connecting Karachi and Peshawar." }
  ]);
};

export const getNotifications = async (req: Request, res: Response) => {
  return res.status(200).json([
    { id: 1, title: "Welcome to Pakistan Railways Schedule", message: "Search schedules, fare details, and platform numbers offline and online.", category: "update" }
  ]);
};
