import { Router, Request, Response, NextFunction } from 'express';
import { getRoutes, getRouteById } from '../controllers/trainController';

const router = Router();

const safeAsync = (fn: (req: Request, res: Response, next: NextFunction) => Promise<any>) => {
  return (req: Request, res: Response, next: NextFunction) => {
    return Promise.resolve(fn(req, res, next)).catch(next);
  };
};

router.get('/', safeAsync(getRoutes));
router.get('/:id', safeAsync(getRouteById));

export default router;
