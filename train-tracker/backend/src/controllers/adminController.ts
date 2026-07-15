import { Response } from 'express';
import { AuthenticatedRequest } from '../middlewares/authMiddleware';
import User from '../models/User';
import Train from '../models/Train';
import Station from '../models/Station';
import Report from '../models/Report';
import Feedback from '../models/Feedback';
import AdminLog from '../models/AdminLog';

// Helper to audit events
const logAdminAction = async (adminEmail: string, action: string, details: string) => {
  try {
    await AdminLog.create({ adminEmail, action, details });
  } catch (err) {
    console.error('Audit logging failed', err);
  }
};

// --- Dashboard Analytics ---
export const getDashboardAnalytics = async (req: AuthenticatedRequest, res: Response) => {
  try {
    if (!req.user || req.user.role !== 'admin') {
      return res.status(403).json({ success: false, message: 'Forbidden' });
    }

    const totalUsers = await User.countDocuments();
    const totalTrains = await Train.countDocuments({ isActive: true });
    const totalStations = await Station.countDocuments();
    const totalReports = await Report.countDocuments();

    // Use aggregation pipelines to group reports by issue type
    const reportsSummary = await Report.aggregate([
      { $group: { _id: '$issueType', count: { $sum: 1 } } }
    ]);

    // Use aggregation to group users by role
    const usersSummary = await User.aggregate([
      { $group: { _id: '$role', count: { $sum: 1 } } }
    ]);

    return res.status(200).json({
      success: true,
      analytics: {
        totalUsers,
        totalTrains,
        totalStations,
        totalReports,
        reportsSummary,
        usersSummary
      }
    });
  } catch (error: any) {
    return res.status(500).json({ success: false, message: error.message });
  }
};

// --- Reports CRUD / Triage ---
export const getReports = async (req: AuthenticatedRequest, res: Response) => {
  try {
    const { status, issueType, page = '1', limit = '10' } = req.query;
    let filterQuery: any = {};
    if (status) filterQuery.status = status;
    if (issueType) filterQuery.issueType = issueType;

    const skipNum = (parseInt(page as string) - 1) * parseInt(limit as string);

    const reports = await Report.find(filterQuery)
      .sort({ createdAt: -1 })
      .skip(skipNum)
      .limit(parseInt(limit as string));

    const total = await Report.countDocuments(filterQuery);

    return res.status(200).json({
      success: true,
      reports,
      pagination: {
        total,
        page: parseInt(page as string),
        pages: Math.ceil(total / parseInt(limit as string))
      }
    });
  } catch (error: any) {
    return res.status(500).json({ success: false, message: error.message });
  }
};

export const updateReportStatus = async (req: AuthenticatedRequest, res: Response) => {
  try {
    const { id } = req.params;
    const { status } = req.body;

    const report = await Report.findById(id);
    if (!report) {
      return res.status(404).json({ success: false, message: 'Report not found' });
    }

    report.status = status;
    await report.save();

    await logAdminAction(
      req.user?.email || 'admin@companion.com',
      'UPDATE_REPORT_STATUS',
      `Modified status of report ${id} to ${status}`
    );

    return res.status(200).json({ success: true, message: 'Report status updated successfully', report });
  } catch (error: any) {
    return res.status(500).json({ success: false, message: error.message });
  }
};

// --- Feedbacks ---
export const getFeedbacks = async (req: AuthenticatedRequest, res: Response) => {
  try {
    const feedbacks = await Feedback.find().sort({ createdAt: -1 });
    return res.status(200).json({ success: true, feedbacks });
  } catch (error: any) {
    return res.status(500).json({ success: false, message: error.message });
  }
};

// --- Audit logs ---
export const getAuditLogs = async (req: AuthenticatedRequest, res: Response) => {
  try {
    const logs = await AdminLog.find().sort({ createdAt: -1 }).limit(100);
    return res.status(200).json({ success: true, logs });
  } catch (error: any) {
    return res.status(500).json({ success: false, message: error.message });
  }
};

// --- Train CRUD ---
export const createTrain = async (req: AuthenticatedRequest, res: Response) => {
  try {
    const train = new Train(req.body);
    await train.save();

    await logAdminAction(
      req.user?.email || 'admin@companion.com',
      'CREATE_TRAIN',
      `Registered train schedule: ${train.trainNumber} - ${train.trainName}`
    );

    return res.status(201).json({ success: true, message: 'Train schedule registered successfully', train });
  } catch (error: any) {
    return res.status(500).json({ success: false, message: error.message });
  }
};

export const updateTrain = async (req: AuthenticatedRequest, res: Response) => {
  try {
    const { trainNumber } = req.params;
    const updatedTrain = await Train.findOneAndUpdate(
      { trainNumber: trainNumber.toUpperCase() },
      req.body,
      { new: true }
    );

    if (!updatedTrain) {
      return res.status(404).json({ success: false, message: 'Train schedule not found' });
    }

    await logAdminAction(
      req.user?.email || 'admin@companion.com',
      'UPDATE_TRAIN',
      `Modified schedule fields for train: ${trainNumber}`
    );

    return res.status(200).json({ success: true, message: 'Train schedule updated successfully', train: updatedTrain });
  } catch (error: any) {
    return res.status(500).json({ success: false, message: error.message });
  }
};

export const deleteTrain = async (req: AuthenticatedRequest, res: Response) => {
  try {
    const { trainNumber } = req.params;
    const deleted = await Train.findOneAndDelete({ trainNumber: trainNumber.toUpperCase() });

    if (!deleted) {
      return res.status(404).json({ success: false, message: 'Train schedule not found' });
    }

    await logAdminAction(
      req.user?.email || 'admin@companion.com',
      'DELETE_TRAIN',
      `Deleted schedule for train: ${trainNumber}`
    );

    return res.status(200).json({ success: true, message: 'Train schedule deleted successfully' });
  } catch (error: any) {
    return res.status(500).json({ success: false, message: error.message });
  }
};

// --- Station CRUD ---
export const createStation = async (req: AuthenticatedRequest, res: Response) => {
  try {
    const station = new Station(req.body);
    await station.save();

    await logAdminAction(
      req.user?.email || 'admin@companion.com',
      'CREATE_STATION',
      `Registered transit station: ${station.code} - ${station.name}`
    );

    return res.status(201).json({ success: true, message: 'Station registered successfully', station });
  } catch (error: any) {
    return res.status(500).json({ success: false, message: error.message });
  }
};

export const updateStation = async (req: AuthenticatedRequest, res: Response) => {
  try {
    const { code } = req.params;
    const updatedStation = await Station.findOneAndUpdate(
      { code: code.toUpperCase() },
      req.body,
      { new: true }
    );

    if (!updatedStation) {
      return res.status(404).json({ success: false, message: 'Station not found' });
    }

    await logAdminAction(
      req.user?.email || 'admin@companion.com',
      'UPDATE_STATION',
      `Modified station properties for: ${code}`
    );

    return res.status(200).json({ success: true, message: 'Station updated successfully', station: updatedStation });
  } catch (error: any) {
    return res.status(500).json({ success: false, message: error.message });
  }
};

export const deleteStation = async (req: AuthenticatedRequest, res: Response) => {
  try {
    const { code } = req.params;
    const deleted = await Station.findOneAndDelete({ code: code.toUpperCase() });

    if (!deleted) {
      return res.status(404).json({ success: false, message: 'Station not found' });
    }

    await logAdminAction(
      req.user?.email || 'admin@companion.com',
      'DELETE_STATION',
      `Deleted station records for: ${code}`
    );

    return res.status(200).json({ success: true, message: 'Station deleted successfully' });
  } catch (error: any) {
    return res.status(500).json({ success: false, message: error.message });
  }
};
