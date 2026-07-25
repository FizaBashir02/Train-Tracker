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
  getBlogs,
  getNotifications
} from '../controllers/trainController';

const router = Router();

router.get('/', searchTrains);
router.get('/search', searchTrains);
router.get('/schedule/:trainNumber', getTrainSchedule);
router.get('/:trainNumber/schedule', getTrainSchedule);
router.get('/station/:stationCode', getStationInfo);
router.get('/live-status/:trainNumber', getLiveStatus);
router.get('/:trainNumber/live-status', getLiveStatus);
router.get('/freight', getFreightTrains);
router.get('/weather', getWeather);
router.get('/prayer', getPrayerTimes);
router.get('/prayers', getPrayerTimes);
router.get('/news', getNews);
router.get('/blogs', getBlogs);
router.get('/notifications', getNotifications);

export default router;
