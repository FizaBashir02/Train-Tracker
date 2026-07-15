import { Router } from 'express';
import { 
  searchTrains, 
  getTrainSchedule, 
  getStationInfo, 
  getLiveStatus, 
  getFreightTrains, 
  getWeather, 
  getPrayerTimes, 
  getNews, 
  getBlogs 
} from '../controllers/trainController';

const router = Router();

router.get('/search', searchTrains);
router.get('/schedule/:trainNumber', getTrainSchedule);
router.get('/station/:stationCode', getStationInfo);
router.get('/live-status/:trainNumber', getLiveStatus);
router.get('/freight', getFreightTrains);
router.get('/weather', getWeather);
router.get('/prayer', getPrayerTimes);
router.get('/news', getNews);
router.get('/blogs', getBlogs);

export default router;
