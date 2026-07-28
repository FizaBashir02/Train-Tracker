import { Request, Response } from 'express';
import Station from '../models/Station';

const DUMMY_STATIONS = [
  { id: 'lhr_1', stationCode: 'LHR', name: 'Lahore Junction', city: 'Lahore', province: 'Punjab', category: 'Major Junction', totalPlatforms: 11, contactNumber: '042-99200421', address: 'Empress Road, Lahore', amenities: ['Waiting Room', 'VIP Lounge', 'Ticket Counter', 'ATM', 'Food Court', 'Mosque'] },
  { id: 'kct_1', stationCode: 'KCT', name: 'Karachi Cantt', city: 'Karachi', province: 'Sindh', category: 'Major Terminal', totalPlatforms: 8, contactNumber: '021-99206062', address: 'Dr. Daud Pota Road, Karachi', amenities: ['Waiting Lounge', 'Ticket Counter', 'ATM', 'Food Plaza', 'Parking'] },
  { id: 'rwp_1', stationCode: 'RWP', name: 'Rawalpindi Station', city: 'Rawalpindi', province: 'Punjab', category: 'Major Station', totalPlatforms: 5, contactNumber: '051-9270721', address: 'Saddar, Rawalpindi', amenities: ['AC Lounge', 'Ticket Counters', 'Food Stalls', 'Mosque'] },
  { id: 'psh_1', stationCode: 'PSH', name: 'Peshawar Cantt', city: 'Peshawar', province: 'KPK', category: 'Terminal', totalPlatforms: 4, contactNumber: '091-9212117', address: 'Mall Road, Peshawar Cantt', amenities: ['Waiting Room', 'Ticket Window', 'Refreshment Shop'] },
  { id: 'qta_1', stationCode: 'QTA', name: 'Quetta Station', city: 'Quetta', province: 'Balochistan', category: 'Zonal Headquarters', totalPlatforms: 4, contactNumber: '081-9201117', address: 'Zarghoon Road, Quetta', amenities: ['Waiting Room', 'Ticket Office', 'Tuck Shop'] }
];

export const getStations = async (req: Request, res: Response) => {
  try {
    const { search } = req.query;
    let list = DUMMY_STATIONS;
    if (search) {
      const q = (search as string).toLowerCase();
      list = list.filter(s => s.name.toLowerCase().includes(q) || s.city.toLowerCase().includes(q) || s.stationCode.toLowerCase().includes(q));
    }
    return res.status(200).json(list);
  } catch (error) {
    return res.status(200).json(DUMMY_STATIONS);
  }
};

export const getStationDetails = async (req: Request, res: Response) => {
  const { id } = req.params;
  const station = DUMMY_STATIONS.find(s => s.stationCode.toLowerCase() === id.toLowerCase() || s.id === id) || DUMMY_STATIONS[0];
  return res.status(200).json(station);
};
