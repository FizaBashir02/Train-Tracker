import { Response } from 'express';
import { AuthenticatedRequest } from '../middlewares/authMiddleware';
import User from '../models/User';
import Train from '../models/Train';
import Station from '../models/Station';
import Report from '../models/Report';
import Feedback from '../models/Feedback';
import AdminLog from '../models/AdminLog';

// Helper to audit events safely
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

    const reportsSummary = await Report.aggregate([
      { $group: { _id: '$issueType', count: { $sum: 1 } } }
    ]);

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
    return res.status(500).json({ success: false, message: 'Server error generating analytics' });
  }
};

// --- Reports CRUD / Triage ---
export const getReports = async (req: AuthenticatedRequest, res: Response) => {
  try {
    const { status, issueType, page = '1', limit = '10' } = req.query;
    let filterQuery: any = {};
    if (status && typeof status === 'string') filterQuery.status = String(status).trim();
    if (issueType && typeof issueType === 'string') filterQuery.issueType = String(issueType).trim();

    const pageNum = Math.max(1, parseInt(page as string) || 1);
    const limitNum = Math.min(100, Math.max(1, parseInt(limit as string) || 10));
    const skipNum = (pageNum - 1) * limitNum;

    const reports = await Report.find(filterQuery)
      .sort({ createdAt: -1 })
      .skip(skipNum)
      .limit(limitNum);

    const total = await Report.countDocuments(filterQuery);

    return res.status(200).json({
      success: true,
      reports,
      pagination: {
        total,
        page: pageNum,
        pages: Math.ceil(total / limitNum)
      }
    });
  } catch (error: any) {
    return res.status(500).json({ success: false, message: 'Server error retrieving reports' });
  }
};

export const updateReportStatus = async (req: AuthenticatedRequest, res: Response) => {
  try {
    const { id } = req.params;
    const { status } = req.body;

    if (!status || !['pending', 'in-review', 'resolved', 'dismissed'].includes(status)) {
      return res.status(400).json({ success: false, message: 'Valid status is required' });
    }

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
    return res.status(500).json({ success: false, message: 'Server error updating report status' });
  }
};

// --- Feedbacks ---
export const getFeedbacks = async (req: AuthenticatedRequest, res: Response) => {
  try {
    const feedbacks = await Feedback.find().sort({ createdAt: -1 }).limit(100);
    return res.status(200).json({ success: true, feedbacks });
  } catch (error: any) {
    return res.status(500).json({ success: false, message: 'Server error retrieving feedback' });
  }
};

// --- Audit logs ---
export const getAuditLogs = async (req: AuthenticatedRequest, res: Response) => {
  try {
    const logs = await AdminLog.find().sort({ createdAt: -1 }).limit(100);
    return res.status(200).json({ success: true, logs });
  } catch (error: any) {
    return res.status(500).json({ success: false, message: 'Server error retrieving audit logs' });
  }
};

// --- Train CRUD ---
export const createTrain = async (req: AuthenticatedRequest, res: Response) => {
  try {
    const { trainNumber, trainName, origin, destination, runsOnDays, activeStatus } = req.body;
    if (!trainNumber || !trainName || !origin || !destination) {
      return res.status(400).json({ success: false, message: 'Train number, name, origin, and destination are required' });
    }

    const train = new Train({
      trainNumber: String(trainNumber).trim().toUpperCase(),
      trainName: String(trainName).trim(),
      origin: String(origin).trim(),
      destination: String(destination).trim(),
      runsOnDays: Array.isArray(runsOnDays) ? runsOnDays : [],
      isActive: activeStatus !== false
    });

    await train.save();

    await logAdminAction(
      req.user?.email || 'admin@companion.com',
      'CREATE_TRAIN',
      `Registered train schedule: ${train.trainNumber} - ${train.trainName}`
    );

    return res.status(201).json({ success: true, message: 'Train schedule registered successfully', train });
  } catch (error: any) {
    return res.status(500).json({ success: false, message: 'Server error creating train schedule' });
  }
};

export const updateTrain = async (req: AuthenticatedRequest, res: Response) => {
  try {
    const { trainNumber } = req.params;
    const cleanTrainNum = String(trainNumber).trim().toUpperCase();

    const allowedFields = ['trainName', 'origin', 'destination', 'runsOnDays', 'isActive', 'stops'];
    const updateData: any = {};
    for (const field of allowedFields) {
      if (req.body[field] !== undefined) {
        updateData[field] = req.body[field];
      }
    }

    const updatedTrain = await Train.findOneAndUpdate(
      { trainNumber: cleanTrainNum },
      updateData,
      { new: true }
    );

    if (!updatedTrain) {
      return res.status(404).json({ success: false, message: 'Train schedule not found' });
    }

    await logAdminAction(
      req.user?.email || 'admin@companion.com',
      'UPDATE_TRAIN',
      `Modified schedule fields for train: ${cleanTrainNum}`
    );

    return res.status(200).json({ success: true, message: 'Train schedule updated successfully', train: updatedTrain });
  } catch (error: any) {
    return res.status(500).json({ success: false, message: 'Server error updating train schedule' });
  }
};

export const deleteTrain = async (req: AuthenticatedRequest, res: Response) => {
  try {
    const { trainNumber } = req.params;
    const cleanTrainNum = String(trainNumber).trim().toUpperCase();
    const deleted = await Train.findOneAndDelete({ trainNumber: cleanTrainNum });

    if (!deleted) {
      return res.status(404).json({ success: false, message: 'Train schedule not found' });
    }

    await logAdminAction(
      req.user?.email || 'admin@companion.com',
      'DELETE_TRAIN',
      `Deleted schedule for train: ${cleanTrainNum}`
    );

    return res.status(200).json({ success: true, message: 'Train schedule deleted successfully' });
  } catch (error: any) {
    return res.status(500).json({ success: false, message: 'Server error deleting train schedule' });
  }
};

// --- Station CRUD ---
export const createStation = async (req: AuthenticatedRequest, res: Response) => {
  try {
    const { code, name, city, latitude, longitude } = req.body;
    if (!code || !name || !city) {
      return res.status(400).json({ success: false, message: 'Station code, name, and city are required' });
    }

    const station = new Station({
      code: String(code).trim().toUpperCase(),
      name: String(name).trim(),
      city: String(city).trim(),
      latitude: Number(latitude) || 0,
      longitude: Number(longitude) || 0
    });

    await station.save();

    await logAdminAction(
      req.user?.email || 'admin@companion.com',
      'CREATE_STATION',
      `Registered transit station: ${station.code} - ${station.name}`
    );

    return res.status(201).json({ success: true, message: 'Station registered successfully', station });
  } catch (error: any) {
    return res.status(500).json({ success: false, message: 'Server error registering station' });
  }
};

export const updateStation = async (req: AuthenticatedRequest, res: Response) => {
  try {
    const { code } = req.params;
    const cleanCode = String(code).trim().toUpperCase();

    const allowedFields = ['name', 'city', 'latitude', 'longitude', 'platforms'];
    const updateData: any = {};
    for (const field of allowedFields) {
      if (req.body[field] !== undefined) {
        updateData[field] = req.body[field];
      }
    }

    const updatedStation = await Station.findOneAndUpdate(
      { code: cleanCode },
      updateData,
      { new: true }
    );

    if (!updatedStation) {
      return res.status(404).json({ success: false, message: 'Station not found' });
    }

    await logAdminAction(
      req.user?.email || 'admin@companion.com',
      'UPDATE_STATION',
      `Modified station properties for: ${cleanCode}`
    );

    return res.status(200).json({ success: true, message: 'Station updated successfully', station: updatedStation });
  } catch (error: any) {
    return res.status(500).json({ success: false, message: 'Server error updating station' });
  }
};

export const deleteStation = async (req: AuthenticatedRequest, res: Response) => {
  try {
    const { code } = req.params;
    const cleanCode = String(code).trim().toUpperCase();
    const deleted = await Station.findOneAndDelete({ code: cleanCode });

    if (!deleted) {
      return res.status(404).json({ success: false, message: 'Station not found' });
    }

    await logAdminAction(
      req.user?.email || 'admin@companion.com',
      'DELETE_STATION',
      `Deleted station records for: ${cleanCode}`
    );

    return res.status(200).json({ success: true, message: 'Station deleted successfully' });
  } catch (error: any) {
    return res.status(500).json({ success: false, message: 'Server error deleting station' });
  }
};
