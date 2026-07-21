import { Request, Response } from 'express';
import Train from '../models/Train';
import Station from '../models/Station';
import Tracking from '../models/Tracking';
import WeatherCache from '../models/WeatherCache';
import PrayerCache from '../models/PrayerCache';
import News from '../models/News';
import Blog from '../models/Blog';

// Helper to escape special regex characters to prevent ReDoS / Regex Injection
const escapeRegex = (str: string) => {
  return str.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
};

export const searchTrains = async (req: Request, res: Response) => {
  try {
    const { source, dest, type } = req.query;
    
    let query: any = {};
    if (source && typeof source === 'string') {
      query.source = new RegExp(escapeRegex(source.trim()), 'i');
    }
    if (dest && typeof dest === 'string') {
      query.destination = new RegExp(escapeRegex(dest.trim()), 'i');
    }
    if (type && typeof type === 'string' && type !== 'All') {
      query.trainType = type.trim();
    }

    const trains = await Train.find(query).limit(100);
    const results = trains.map(t => ({
      trainName: t.trainName,
      trainNumber: t.trainNumber,
      source: t.source,
      destination: t.destination,
      departure: t.departureTime,
      arrival: t.arrivalTime,
      duration: `${t.totalDistanceKm} KM - ${t.stops.length} Stops`,
      trainType: t.trainType
    }));

    return res.status(200).json(results);
  } catch (error: any) {
    return res.status(500).json({ success: false, message: 'Server error searching trains' });
  }
};

export const getTrainSchedule = async (req: Request, res: Response) => {
  try {
    const { trainNumber } = req.params;
    if (!trainNumber) {
      return res.status(400).json({ success: false, message: 'Train number is required' });
    }

    const cleanTrainNum = String(trainNumber).trim().toUpperCase();
    const train = await Train.findOne({ trainNumber: cleanTrainNum });

    if (!train) {
      return res.status(404).json({ success: false, message: `Schedule not found for train ${cleanTrainNum}` });
    }

    const schedule = {
      trainName: train.trainName,
      trainNumber: train.trainNumber,
      stations: train.stops.map((stop) => ({
        stationName: stop.stationName,
        stationCode: stop.stationCode,
        arrival: stop.arrival,
        departure: stop.departure,
        distanceKm: stop.distanceKm,
        stopDurationMinutes: stop.stopDurationMinutes,
        dayNumber: 1
      })),
      totalStops: train.stops.length,
      totalDistanceKm: train.totalDistanceKm,
      totalJourneyTime: "N/A"
    };

    return res.status(200).json(schedule);
  } catch (error: any) {
    return res.status(500).json({ success: false, message: 'Server error retrieving schedule' });
  }
};

export const getStationInfo = async (req: Request, res: Response) => {
  try {
    const { stationCode } = req.params;
    if (!stationCode) {
      return res.status(400).json({ success: false, message: 'Station code is required' });
    }

    const cleanCode = String(stationCode).trim().toUpperCase();
    const station = await Station.findOne({ code: cleanCode });

    if (!station) {
      return res.status(404).json({ success: false, message: `Station info not found for ${cleanCode}` });
    }

    const info = {
      stationName: station.name,
      code: station.code,
      address: station.address,
      contactNumber: station.contactNumber,
      facilities: station.facilities,
      nearbyHotels: [],
      nearbyRestaurants: [],
      nearbyBusStops: [],
      todayArrivals: [],
      todayDepartures: [],
      delayedTrains: []
    };

    return res.status(200).json(info);
  } catch (error: any) {
    return res.status(500).json({ success: false, message: 'Server error retrieving station info' });
  }
};

export const getLiveStatus = async (req: Request, res: Response) => {
  try {
    const { trainNumber } = req.params;
    if (!trainNumber) {
      return res.status(400).json({ success: false, message: 'Train number required' });
    }

    const cleanTrainNum = String(trainNumber).trim().toUpperCase();
    const tracking = await Tracking.findOne({ trainNumber: cleanTrainNum });

    if (!tracking) {
      const train = await Train.findOne({ trainNumber: cleanTrainNum });
      if (!train) {
        return res.status(404).json({ success: false, message: `Train ${cleanTrainNum} not found` });
      }

      const activeTracking = {
        trainNumber: train.trainNumber,
        trainName: train.trainName,
        currentLatitude: 31.5204,
        currentLongitude: 74.3587,
        currentSpeedKmh: 75,
        delayMinutes: 10,
        currentStation: train.source,
        previousStation: train.source,
        nextStation: train.stops[0]?.stationName || train.destination,
        distanceRemainingKm: train.totalDistanceKm,
        journeyProgress: 0.15,
        lastUpdated: new Date()
      };
      return res.status(200).json(activeTracking);
    }

    return res.status(200).json(tracking);
  } catch (error: any) {
    return res.status(500).json({ success: false, message: 'Server error retrieving live status' });
  }
};

export const getFreightTrains = async (req: Request, res: Response) => {
  try {
    const freights = await Train.find({ trainType: 'Freight' }).limit(50);
    const results = freights.map(f => ({
      trainNumber: f.trainNumber,
      trainName: f.trainName,
      source: f.source,
      destination: f.destination,
      departureTime: f.departureTime,
      arrivalTime: f.arrivalTime,
      weightTons: 1200,
      commodityType: "Cargo Containers",
      status: "On Schedule"
    }));

    return res.status(200).json(results);
  } catch (error: any) {
    return res.status(500).json({ success: false, message: 'Server error retrieving freight trains' });
  }
};

const mapWeatherCode = (code: number): string => {
  switch (code) {
    case 0: return "Clear Sky";
    case 1:
    case 2:
    case 3: return "Partly Cloudy";
    case 45:
    case 48: return "Foggy";
    case 51:
    case 53:
    case 55: return "Drizzle";
    case 61:
    case 63:
    case 65: return "Rainy";
    case 71:
    case 73:
    case 75: return "Snowy";
    case 80:
    case 81:
    case 82: return "Rain Showers";
    case 95:
    case 96:
    case 99: return "Thunderstorm";
    default: return "Partly Cloudy";
  }
};

const geocodeCity = async (cityName: string): Promise<{ lat: number; lng: number } | null> => {
  try {
    const geoUrl = `https://geocoding-api.open-meteo.com/v1/search?name=${encodeURIComponent(cityName)}&count=1&language=en&format=json`;
    const res = await fetch(geoUrl);
    const data: any = await res.json();
    if (data.results && data.results.length > 0) {
      return {
        lat: data.results[0].latitude,
        lng: data.results[0].longitude
      };
    }
  } catch (e) {
    console.error(`[GEOCODE-ERROR] Failed to geocode ${cityName}:`, e);
  }
  return null;
};

export const getWeather = async (req: Request, res: Response) => {
  try {
    const { location } = req.query;
    if (!location || typeof location !== 'string') {
      return res.status(400).json({ success: false, message: 'Valid location query parameter is required' });
    }

    const city = location.trim().toLowerCase();
    let weather = await WeatherCache.findOne({ location: city });

    if (!weather) {
      const coords = await geocodeCity(city) || { lat: 31.5204, lng: 74.3587 };
      
      const weatherUrl = `https://api.open-meteo.com/v1/forecast?latitude=${coords.lat}&longitude=${coords.lng}&current=temperature_2m,relative_humidity_2m,weather_code`;
      const response = await fetch(weatherUrl);
      const data: any = await response.json();

      if (data && data.current) {
        weather = new WeatherCache({
          location: city,
          temperature: `${Math.round(data.current.temperature_2m)}°C`,
          condition: mapWeatherCode(data.current.weather_code),
          humidity: `${data.current.relative_humidity_2m}%`
        });
        await weather.save();
      } else {
        throw new Error("Unable to parse live weather API payload");
      }
    }

    return res.status(200).json({
      location: weather.location.toUpperCase(),
      temperature: weather.temperature,
      condition: weather.condition,
      humidity: weather.humidity
    });
  } catch (error: any) {
    return res.status(500).json({ success: false, message: 'Server error retrieving weather' });
  }
};

export const getPrayerTimes = async (req: Request, res: Response) => {
  try {
    const { location } = req.query;
    if (!location || typeof location !== 'string') {
      return res.status(400).json({ success: false, message: 'Valid location query parameter is required' });
    }

    const city = location.trim().toLowerCase();
    let prayer = await PrayerCache.findOne({ location: city });

    if (!prayer) {
      const coords = await geocodeCity(city) || { lat: 31.5204, lng: 74.3587 };

      const timestamp = Math.floor(Date.now() / 1000);
      const prayerUrl = `https://api.aladhan.com/v1/timings/${timestamp}?latitude=${coords.lat}&longitude=${coords.lng}&method=2`;
      const response = await fetch(prayerUrl);
      const data: any = await response.json();

      let qiblaDeg = "261° (W)";
      try {
        const qiblaUrl = `https://api.aladhan.com/v1/qibla/${coords.lat}/${coords.lng}`;
        const qiblaRes = await fetch(qiblaUrl);
        const qiblaData: any = await qiblaRes.json();
        if (qiblaData && qiblaData.data) {
          qiblaDeg = `${Math.round(qiblaData.data.direction)}°`;
        }
      } catch (qe) {
        console.error(`[QIBLA-ERROR]`, qe);
      }

      if (data && data.data && data.data.timings) {
        const timings = data.data.timings;
        const hijri = data.data.date.hijri;
        
        prayer = new PrayerCache({
          location: city,
          islamicDate: `${hijri.day} ${hijri.month.en} ${hijri.year} AH`,
          fajr: timings.Fajr,
          dhuhr: timings.Dhuhr,
          asr: timings.Asr,
          maghrib: timings.Maghrib,
          isha: timings.Isha,
          qiblaDirection: qiblaDeg
        });
        await prayer.save();
      } else {
        throw new Error("Unable to parse live prayer API payload");
      }
    }

    return res.status(200).json({
      islamicDate: prayer.islamicDate,
      fajr: prayer.fajr,
      dhuhr: prayer.dhuhr,
      asr: prayer.asr,
      maghrib: prayer.maghrib,
      isha: prayer.isha,
      qiblaDirection: prayer.qiblaDirection
    });
  } catch (error: any) {
    return res.status(500).json({ success: false, message: 'Server error retrieving prayer times' });
  }
};

export const getNews = async (req: Request, res: Response) => {
  try {
    const news = await News.find().sort({ createdAt: -1 }).limit(50);
    return res.status(200).json(news);
  } catch (error: any) {
    return res.status(500).json({ success: false, message: 'Server error retrieving news' });
  }
};

export const getBlogs = async (req: Request, res: Response) => {
  try {
    const blogs = await Blog.find().sort({ createdAt: -1 }).limit(50);
    return res.status(200).json(blogs);
  } catch (error: any) {
    return res.status(500).json({ success: false, message: 'Server error retrieving blogs' });
  }
};
