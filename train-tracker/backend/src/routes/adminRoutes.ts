import { Router, Request, Response, NextFunction } from 'express';
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

const safeAsync = (fn: (req: any, res: Response, next: NextFunction) => Promise<any>) => {
  return (req: Request, res: Response, next: NextFunction) => {
    try {
      return Promise.resolve(fn(req, res, next)).catch((err: any) => {
        console.error('[ADMIN-ROUTE-ERROR]', err);
        if (!res.headersSent) {
          res.status(500).json({ success: false, message: 'Internal server error processing admin request' });
        }
      });
    } catch (err: any) {
      console.error('[ADMIN-ROUTE-EXCEPTION]', err);
      if (!res.headersSent) {
        res.status(500).json({ success: false, message: 'Internal server error processing admin request' });
      }
    }
  };
};

// Secure admin workspace
router.use(authenticateToken);
router.use(requireRole(['admin']));

// Analytics
router.get('/analytics', safeAsync(getDashboardAnalytics));

// Passenger reports triage
router.get('/reports', safeAsync(getReports));
router.put('/reports/:id', safeAsync(updateReportStatus));

// Feedbacks & audit log queries
router.get('/feedback', safeAsync(getFeedbacks));
router.get('/audit-logs', safeAsync(getAuditLogs));

// Fleet Train CRUD
router.post('/trains', safeAsync(createTrain));
router.put('/trains/:trainNumber', safeAsync(updateTrain));
router.delete('/trains/:trainNumber', safeAsync(deleteTrain));

// Transit Station CRUD
router.post('/stations', safeAsync(createStation));
router.put('/stations/:code', safeAsync(updateStation));
router.delete('/stations/:code', safeAsync(deleteStation));

export default router;
