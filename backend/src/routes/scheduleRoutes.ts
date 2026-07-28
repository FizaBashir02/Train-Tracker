import { Router } from 'express';
import {
  getAllTrains,
  searchTrains,
  getTrainDetails,
  getTrainSchedule,
  getFreightTrains,
  getWeather,
  getNamazTimings,
  getNews,
  getBlogs
} from '../controllers/scheduleController';

const router = Router();

router.get('/trains', getAllTrains);
router.get('/trains/search', searchTrains);
router.get('/trains/freight', getFreightTrains);
router.get('/trains/weather', getWeather);
router.get('/trains/prayer', getNamazTimings);
router.get('/trains/news', getNews);
router.get('/trains/blogs', getBlogs);
router.get('/trains/:id', getTrainDetails);
router.get('/trains/:number/schedule', getTrainSchedule);

export default router;
