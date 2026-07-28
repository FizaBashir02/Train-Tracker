import { Router, Request, Response, NextFunction } from 'express';
import { getStations, getStationById } from '../controllers/trainController';

const router = Router();

const safeAsync = (fn: (req: Request, res: Response, next: NextFunction) => Promise<any>) => {
  return (req: Request, res: Response, next: NextFunction) => {
    return Promise.resolve(fn(req, res, next)).catch(next);
  };
};

router.get('/', safeAsync(getStations));
router.get('/:id', safeAsync(getStationById));

export default router;
