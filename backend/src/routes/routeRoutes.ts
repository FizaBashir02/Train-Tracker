import { Router } from 'express';
import { getRoutes, getRouteDetails } from '../controllers/routeController';

const router = Router();

router.get('/routes', getRoutes);
router.get('/routes/:id', getRouteDetails);

export default router;
