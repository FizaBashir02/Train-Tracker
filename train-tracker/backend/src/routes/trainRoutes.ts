import { Router, Request, Response, NextFunction } from 'express';
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

const safeAsync = (fn: (req: Request, res: Response, next: NextFunction) => Promise<any>) => {
  return (req: Request, res: Response, next: NextFunction) => {
    try {
      return Promise.resolve(fn(req, res, next)).catch((err: any) => {
        console.error('[TRAIN-ROUTE-ERROR]', err);
        if (!res.headersSent) {
          res.status(500).json({ success: false, message: 'Internal server error processing train request' });
        }
      });
    } catch (err: any) {
      console.error('[TRAIN-ROUTE-EXCEPTION]', err);
      if (!res.headersSent) {
        res.status(500).json({ success: false, message: 'Internal server error processing train request' });
      }
    }
  };
};

router.get('/', safeAsync(searchTrains));
router.get('/search', safeAsync(searchTrains));
router.get('/schedule/:trainNumber', safeAsync(getTrainSchedule));
router.get('/:trainNumber/schedule', safeAsync(getTrainSchedule));
router.get('/station/:stationCode', safeAsync(getStationInfo));
router.get('/live-status/:trainNumber', safeAsync(getLiveStatus));
router.get('/:trainNumber/live-status', safeAsync(getLiveStatus));
router.get('/freight', safeAsync(getFreightTrains));
router.get('/weather', safeAsync(getWeather));
router.get('/prayer', safeAsync(getPrayerTimes));
router.get('/prayers', safeAsync(getPrayerTimes));
router.get('/news', safeAsync(getNews));
router.get('/blogs', safeAsync(getBlogs));
router.get('/notifications', safeAsync(getNotifications));

export default router;
