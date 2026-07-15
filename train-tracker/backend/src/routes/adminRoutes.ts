import { Router } from 'express';
import { authenticateToken, requireRole } from '../middlewares/authMiddleware';
import { 
  getDashboardAnalytics, 
  getReports, 
  updateReportStatus, 
  getFeedbacks, 
  getAuditLogs, 
  createTrain, 
  updateTrain, 
  deleteTrain, 
  createStation, 
  updateStation, 
  deleteStation 
} from '../controllers/adminController';

const router = Router();

// Secure admin workspace
router.use(authenticateToken);
router.use(requireRole(['admin']));

// Analytics
router.get('/analytics', getDashboardAnalytics);

// Passenger reports triage
router.get('/reports', getReports);
router.put('/reports/:id', updateReportStatus);

// Feedbacks & audit log queries
router.get('/feedback', getFeedbacks);
router.get('/audit-logs', getAuditLogs);

// Fleet Train CRUD
router.post('/trains', createTrain);
router.put('/trains/:trainNumber', updateTrain);
router.delete('/trains/:trainNumber', deleteTrain);

// Transit Station CRUD
router.post('/stations', createStation);
router.put('/stations/:code', updateStation);
router.delete('/stations/:code', deleteStation);

export default router;
