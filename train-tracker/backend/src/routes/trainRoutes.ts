import { Router, Request, Response, NextFunction } from 'express';
import { 
  searchTrains, 
  getTrainById,
  getTrainSchedule, 
  getStations, 
  getStationById,
  getRoutes,
  getRouteById,
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

// Train Schedule Endpoints
router.get('/', safeAsync(searchTrains));
router.get('/search', safeAsync(searchTrains));
router.get('/freight', safeAsync(getFreightTrains));
router.get('/schedule', safeAsync(getTrainSchedule));
router.get('/schedule/:trainNumber', safeAsync(getTrainSchedule));
router.get('/:id/schedule', safeAsync(getTrainSchedule));
router.get('/:id', safeAsync(getTrainById));

// Utility Endpoints
router.get('/weather', safeAsync(getWeather));
router.get('/prayer', safeAsync(getPrayerTimes));
router.get('/prayers', safeAsync(getPrayerTimes));
router.get('/news', safeAsync(getNews));
router.get('/blogs', safeAsync(getBlogs));
router.get('/notifications', safeAsync(getNotifications));

export default router;
