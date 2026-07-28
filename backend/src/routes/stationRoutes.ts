import { Router } from 'express';
import { getStations, getStationDetails } from '../controllers/stationController';

const router = Router();

router.get('/stations', getStations);
router.get('/stations/:id', getStationDetails);

export default router;
