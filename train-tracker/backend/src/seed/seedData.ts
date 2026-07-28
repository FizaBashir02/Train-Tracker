import Train from '../models/Train';
import Station from '../models/Station';
import Schedule from '../models/Schedule';
import Route from '../models/Route';

export const dummyStations = [
  { code: 'KHI', name: 'Karachi Cantt', address: 'Dr. Daudpota Road, Karachi', latitude: 24.8532, longitude: 67.0345, contactNumber: '021-99206080', facilities: ['VIP Waiting Lounge', 'Air Conditioned Mosque', 'Executive Food Court', '24/7 Ticket Counters', 'Car Parking'] },
  { code: 'LHR', name: 'Lahore Junction', address: 'Empress Road, Lahore', latitude: 31.5744, longitude: 74.3317, contactNumber: '042-99201888', facilities: ['Executive Lounge', 'Grand Mosque', 'Subway & Food Outlets', 'Luggage Cloak Room', 'ATMs'] },
  { code: 'RWP', name: 'Rawalpindi', address: 'Saddar, Rawalpindi', latitude: 33.5971, longitude: 73.0483, contactNumber: '051-9270830', facilities: ['Waiting Hall', 'Canteen', 'Taxi Stand', 'Prayer Area', 'Help Desk'] },
  { code: 'ISB', name: 'Islamabad', address: 'Sector H-9, Islamabad', latitude: 33.6702, longitude: 73.0234, contactNumber: '051-9257048', facilities: ['Modern Waiting Lounge', 'Cafe', 'Prayer Hall', 'Ticket Counters'] },
  { code: 'FSD', name: 'Faisalabad', address: 'Station Road, Faisalabad', latitude: 31.4180, longitude: 73.0790, contactNumber: '041-9200388', facilities: ['Waiting Room', 'Food Stalls', 'Mosque', 'Parking'] },
  { code: 'MUX', name: 'Multan Cantt', address: 'Cantt Area, Multan', latitude: 30.1852, longitude: 71.4391, contactNumber: '061-9200588', facilities: ['Air-Conditioned Waiting Area', 'Food Court', 'Mosque', 'Ticket Office'] },
  { code: 'HYD', name: 'Hyderabad Junction', address: 'Station Road, Hyderabad', latitude: 25.3924, longitude: 68.3737, contactNumber: '022-9200188', facilities: ['Waiting Hall', 'Tea Stalls', 'Prayer Room', 'Parking'] },
  { code: 'SKR', name: 'Sukkur', address: 'Station Road, Sukkur', latitude: 27.7052, longitude: 68.8574, contactNumber: '071-9310188', facilities: ['Waiting Area', 'Mosque', 'Refreshments'] },
  { code: 'RRI', name: 'Rohri Junction', address: 'Rohri, Sukkur District', latitude: 27.6833, longitude: 68.8950, contactNumber: '071-9310200', facilities: ['Major Junction Lounge', 'Food Shops', 'Overhead Bridge'] },
  { code: 'SWL', name: 'Sahiwal', address: 'Railway Road, Sahiwal', latitude: 30.6682, longitude: 73.1114, contactNumber: '040-9200100', facilities: ['Waiting Area', 'Ticket Window', 'Prayer Space'] },
  { code: 'GRW', name: 'Gujranwala', address: 'G.T. Road, Gujranwala', latitude: 32.1617, longitude: 74.1883, contactNumber: '055-9200150', facilities: ['Waiting Room', 'Food Outlets', 'Parking'] },
  { code: 'PEW', name: 'Peshawar Cantt', address: 'Saddar Road, Peshawar', latitude: 34.0085, longitude: 71.5369, contactNumber: '091-9210188', facilities: ['Historical Station Lounge', 'Food Stalls', 'Security Desk'] },
  { code: 'UET', name: 'Quetta', address: 'Zarghoon Road, Quetta', latitude: 30.1980, longitude: 67.0125, contactNumber: '081-9201188', facilities: ['Mountain Line Station', 'Heated Waiting Area', 'Tea Shops'] }
];

export const dummyRoutes = [
  { routeId: 'R-ML1', routeName: 'Main Line 1 (ML-1) Karachi - Peshawar', origin: 'Karachi Cantt', terminus: 'Peshawar Cantt', totalDistanceKm: 1721, stations: ['KHI', 'HYD', 'RRI', 'MUX', 'SWL', 'LHR', 'GRW', 'RWP', 'ISB', 'PEW'], trainsCount: 30 },
  { routeId: 'R-ML2', routeName: 'Main Line 2 (ML-2) Kotri - Attock', origin: 'Kotri Junction', terminus: 'Attock City Junction', totalDistanceKm: 1250, stations: ['HYD', 'SKR', 'D.G. Khan', 'Mianwali', 'Attock'], trainsCount: 8 },
  { routeId: 'R-ML3', routeName: 'Main Line 3 (ML-3) Rohri - Chaman', origin: 'Rohri Junction', terminus: 'Chaman', totalDistanceKm: 523, stations: ['RRI', 'Jacobabad', 'Sibi', 'UET', 'Chaman'], trainsCount: 6 },
  { routeId: 'R-KQR', routeName: 'Karachi - Mirpur Khas Feeder', origin: 'Karachi City', terminus: 'Khokhrapar', totalDistanceKm: 380, stations: ['KHI', 'HYD', 'Mirpur Khas', 'Zero Point'], trainsCount: 4 },
  { routeId: 'R-FSD', routeName: 'Lahore - Faisalabad Loop', origin: 'Lahore Junction', terminus: 'Faisalabad', totalDistanceKm: 140, stations: ['LHR', 'Sheikhupura', 'FSD'], trainsCount: 5 }
];

export const dummyTrainsList = [
  {
    trainNumber: '1UP', trainName: 'Green Line Express', trainType: 'Express',
    sourceStation: 'Karachi Cantt', destinationStation: 'Islamabad',
    departureTime: '22:00', arrivalTime: '20:15', duration: '22h 15m', distance: 1522,
    status: 'On Time', platform: '2', fareEconomy: 4500, fareBusiness: 8500, fareAC: 11500,
    daysOfOperation: ['Daily'], route: 'Main Line 1 (ML-1)', availableSeats: 42, lastUpdated: '5 mins ago',
    intermediateStations: [
      { stationCode: 'KHI', stationName: 'Karachi Cantt', arrival: '22:00', departure: '22:00', stopDurationMinutes: 0, distanceKm: 0, platform: '2' },
      { stationCode: 'HYD', stationName: 'Hyderabad', arrival: '00:30', departure: '00:35', stopDurationMinutes: 5, distanceKm: 178, platform: '1' },
      { stationCode: 'RRI', stationName: 'Rohri Junction', arrival: '05:10', departure: '05:25', stopDurationMinutes: 15, distanceKm: 480, platform: '3' },
      { stationCode: 'MUX', stationName: 'Multan Cantt', arrival: '11:15', departure: '11:35', stopDurationMinutes: 20, distanceKm: 925, platform: '2' },
      { stationCode: 'LHR', stationName: 'Lahore Junction', arrival: '15:45', departure: '16:15', stopDurationMinutes: 30, distanceKm: 1214, platform: '1' },
      { stationCode: 'RWP', stationName: 'Rawalpindi', arrival: '19:40', departure: '19:55', stopDurationMinutes: 15, distanceKm: 1504, platform: '2' },
      { stationCode: 'ISB', stationName: 'Islamabad', arrival: '20:15', departure: '20:15', stopDurationMinutes: 0, distanceKm: 1522, platform: '1' }
    ]
  },
  {
    trainNumber: '7UP', trainName: 'Tezgam Express', trainType: 'Express',
    sourceStation: 'Karachi Cantt', destinationStation: 'Rawalpindi',
    departureTime: '17:30', arrivalTime: '19:00', duration: '25h 30m', distance: 1548,
    status: 'On Time', platform: '1', fareEconomy: 3200, fareBusiness: 6800, fareAC: 9500,
    daysOfOperation: ['Daily'], route: 'Main Line 1 (ML-1)', availableSeats: 18, lastUpdated: '2 mins ago',
    intermediateStations: [
      { stationCode: 'KHI', stationName: 'Karachi Cantt', arrival: '17:30', departure: '17:30', stopDurationMinutes: 0, distanceKm: 0, platform: '1' },
      { stationCode: 'HYD', stationName: 'Hyderabad', arrival: '20:00', departure: '20:10', stopDurationMinutes: 10, distanceKm: 178, platform: '2' },
      { stationCode: 'RRI', stationName: 'Rohri Junction', arrival: '01:30', departure: '01:50', stopDurationMinutes: 20, distanceKm: 480, platform: '2' },
      { stationCode: 'MUX', stationName: 'Multan Cantt', arrival: '08:00', departure: '08:20', stopDurationMinutes: 20, distanceKm: 925, platform: '1' },
      { stationCode: 'SWL', stationName: 'Sahiwal', arrival: '11:10', departure: '11:15', stopDurationMinutes: 5, distanceKm: 1080, platform: '1' },
      { stationCode: 'LHR', stationName: 'Lahore Junction', arrival: '13:50', departure: '14:20', stopDurationMinutes: 30, distanceKm: 1214, platform: '3' },
      { stationCode: 'GRW', stationName: 'Gujranwala', arrival: '15:25', departure: '15:30', stopDurationMinutes: 5, distanceKm: 1280, platform: '1' },
      { stationCode: 'RWP', stationName: 'Rawalpindi', arrival: '19:00', departure: '19:00', stopDurationMinutes: 0, distanceKm: 1548, platform: '1' }
    ]
  },
  {
    trainNumber: '15UP', trainName: 'Karachi Express', trainType: 'Express',
    sourceStation: 'Karachi Cantt', destinationStation: 'Lahore Junction',
    departureTime: '16:30', arrivalTime: '11:00', duration: '18h 30m', distance: 1214,
    status: 'On Time', platform: '4', fareEconomy: 3500, fareBusiness: 7200, fareAC: 10000,
    daysOfOperation: ['Daily'], route: 'Main Line 1 (ML-1)', availableSeats: 30, lastUpdated: '10 mins ago',
    intermediateStations: [
      { stationCode: 'KHI', stationName: 'Karachi Cantt', arrival: '16:30', departure: '16:30', stopDurationMinutes: 0, distanceKm: 0, platform: '4' },
      { stationCode: 'HYD', stationName: 'Hyderabad', arrival: '19:00', departure: '19:10', stopDurationMinutes: 10, distanceKm: 178, platform: '1' },
      { stationCode: 'RRI', stationName: 'Rohri Junction', arrival: '00:15', departure: '00:30', stopDurationMinutes: 15, distanceKm: 480, platform: '1' },
      { stationCode: 'MUX', stationName: 'Multan Cantt', arrival: '06:10', departure: '06:30', stopDurationMinutes: 20, distanceKm: 925, platform: '2' },
      { stationCode: 'LHR', stationName: 'Lahore Junction', arrival: '11:00', departure: '11:00', stopDurationMinutes: 0, distanceKm: 1214, platform: '4' }
    ]
  },
  {
    trainNumber: '25UP', trainName: 'Bahauddin Zakaria Express', trainType: 'Express',
    sourceStation: 'Karachi Cantt', destinationStation: 'Multan Cantt',
    departureTime: '19:00', arrivalTime: '10:00', duration: '15h 00m', distance: 925,
    status: 'Boarding Soon', platform: '2', fareEconomy: 2400, fareBusiness: 4800, fareAC: 7000,
    daysOfOperation: ['Daily'], route: 'Main Line 1 (ML-1)', availableSeats: 35, lastUpdated: '1 min ago',
    intermediateStations: [
      { stationCode: 'KHI', stationName: 'Karachi Cantt', arrival: '19:00', departure: '19:00', stopDurationMinutes: 0, distanceKm: 0, platform: '2' },
      { stationCode: 'HYD', stationName: 'Hyderabad', arrival: '21:30', departure: '21:40', stopDurationMinutes: 10, distanceKm: 178, platform: '3' },
      { stationCode: 'RRI', stationName: 'Rohri Junction', arrival: '03:10', departure: '03:30', stopDurationMinutes: 20, distanceKm: 480, platform: '2' },
      { stationCode: 'MUX', stationName: 'Multan Cantt', arrival: '10:00', departure: '10:00', stopDurationMinutes: 0, distanceKm: 925, platform: '2' }
    ]
  },
  {
    trainNumber: '39UP', trainName: 'Jaffar Express', trainType: 'Express',
    sourceStation: 'Quetta', destinationStation: 'Peshawar Cantt',
    departureTime: '09:00', arrivalTime: '20:00', duration: '35h 00m', distance: 1622,
    status: 'On Time', platform: '2', fareEconomy: 3400, fareBusiness: 6900, fareAC: 9800,
    daysOfOperation: ['Daily'], route: 'Main Line 3 (ML-3)', availableSeats: 19, lastUpdated: '12 mins ago',
    intermediateStations: [
      { stationCode: 'UET', stationName: 'Quetta', arrival: '09:00', departure: '09:00', stopDurationMinutes: 0, distanceKm: 0, platform: '2' },
      { stationCode: 'RRI', stationName: 'Rohri Junction', arrival: '18:30', departure: '19:00', stopDurationMinutes: 30, distanceKm: 386, platform: '1' },
      { stationCode: 'MUX', stationName: 'Multan Cantt', arrival: '01:15', departure: '01:35', stopDurationMinutes: 20, distanceKm: 831, platform: '3' },
      { stationCode: 'LHR', stationName: 'Lahore Junction', arrival: '07:30', departure: '08:00', stopDurationMinutes: 30, distanceKm: 1120, platform: '2' },
      { stationCode: 'RWP', stationName: 'Rawalpindi', arrival: '13:30', departure: '13:50', stopDurationMinutes: 20, distanceKm: 1410, platform: '1' },
      { stationCode: 'PEW', stationName: 'Peshawar Cantt', arrival: '20:00', departure: '20:00', stopDurationMinutes: 0, distanceKm: 1622, platform: '1' }
    ]
  }
];

// Dynamically generate remaining up to 50+ trains to ensure rich variety across Pakistan
export const generateExtraTrains = () => {
  const names = [
    { num: '9UP', name: 'Allama Iqbal Express', src: 'Karachi Cantt', dst: 'Sialkot Junction', type: 'Express', dur: '24h 10m', dist: 1340, status: 'On Time' },
    { num: '11UP', name: 'Hazara Express', src: 'Karachi City', dst: 'Havelian', type: 'Express', dur: '33h 45m', dist: 1590, status: 'Delayed' },
    { num: '45UP', name: 'Pakistan Express', src: 'Karachi Cantt', dst: 'Rawalpindi', type: 'Express', dur: '26h 00m', dist: 1510, status: 'Boarding Soon' },
    { num: '17UP', name: 'Millat Express', src: 'Karachi Cantt', dst: 'Lala Musa', type: 'Express', dur: '21h 30m', dist: 1320, status: 'On Time' },
    { num: '13UP', name: 'Awam Express', src: 'Karachi Cantt', dst: 'Peshawar Cantt', type: 'Express', dur: '34h 15m', dist: 1721, status: 'Delayed' },
    { num: '115UP', name: 'Musa Pak Express', src: 'Multan Cantt', dst: 'Lahore Junction', type: 'Express', dur: '4h 45m', dist: 312, status: 'Departed' },
    { num: '47UP', name: 'Rehman Baba Express', src: 'Karachi Cantt', dst: 'Peshawar Cantt', type: 'Express', dur: '27h 30m', dist: 1680, status: 'On Time' },
    { num: '149UP', name: 'Mehran Express', src: 'Karachi City', dst: 'Mirpur Khas', type: 'Express', dur: '4h 30m', dist: 246, status: 'On Time' },
    { num: '43UP', name: 'Shah Hussain Express', src: 'Karachi Cantt', dst: 'Lahore Junction', type: 'Express', dur: '17h 45m', dist: 1214, status: 'On Time' },
    { num: '3UP', name: 'Bolan Mail', src: 'Karachi City', dst: 'Quetta', type: 'Express', dur: '20h 30m', dist: 888, status: 'Delayed' },
    { num: '131UP', name: 'Rohi Express', src: 'Sukkur', dst: 'Khanewal Junction', type: 'Express', dur: '6h 15m', dist: 380, status: 'Arrived' },
    { num: '27UP', name: 'Shalimar Express', src: 'Karachi Cantt', dst: 'Lahore Junction', type: 'Express', dur: '18h 15m', dist: 1214, status: 'Boarding Soon' },
    { num: '103UP', name: 'Subak Kharam', src: 'Lahore Junction', dst: 'Rawalpindi', type: 'Express', dur: '4h 30m', dist: 290, status: 'On Time' },
    { num: '101UP', name: 'Subak Raftar', src: 'Lahore Junction', dst: 'Rawalpindi', type: 'Express', dur: '4h 30m', dist: 290, status: 'Departed' },
    { num: '105UP', name: 'Rawal Express', src: 'Lahore Junction', dst: 'Rawalpindi', type: 'Express', dur: '4h 15m', dist: 290, status: 'On Time' },
    { num: '113UP', name: 'Ghauri Express', src: 'Lahore Junction', dst: 'Faisalabad', type: 'Express', dur: '2h 15m', dist: 140, status: 'On Time' },
    { num: '111UP', name: 'Badar Express', src: 'Lahore Junction', dst: 'Faisalabad', type: 'Express', dur: '2h 15m', dist: 140, status: 'Boarding Soon' },
    { num: '107UP', name: 'Islamabad Express', src: 'Lahore Junction', dst: 'Rawalpindi', type: 'Express', dur: '4h 15m', dist: 290, status: 'On Time' },
    { num: '41UP', name: 'Karakoram Express', src: 'Karachi Cantt', dst: 'Lahore Junction', type: 'Express', dur: '18h 00m', dist: 1214, status: 'On Time' },
    { num: '35UP', name: 'Sir Syed Express', src: 'Karachi Cantt', dst: 'Rawalpindi', type: 'Express', dur: '21h 00m', dist: 1548, status: 'On Time' },
    { num: '119UP', name: 'Shah Rukn-e-Alam Express', src: 'Multan Cantt', dst: 'Lahore Junction', type: 'Express', dur: '5h 00m', dist: 312, status: 'Departed' },
    { num: '213UP', name: 'Mohenjo Daro Express', src: 'Kotri Junction', dst: 'Rohri Junction', type: 'Passenger', dur: '8h 30m', dist: 330, status: 'On Time' },
    { num: '145UP', name: 'Sukkur Express', src: 'Karachi City', dst: 'Jacobabad Junction', type: 'Express', dur: '11h 30m', dist: 520, status: 'On Time' },
    { num: '401UP', name: 'Thar Express', src: 'Karachi Cantt', dst: 'Zero Point', type: 'Express', dur: '7h 00m', dist: 380, status: 'On Time' },
    { num: '303UP', name: 'Chaman Passenger', src: 'Quetta', dst: 'Chaman', type: 'Passenger', dur: '4h 30m', dist: 130, status: 'On Time' },
    { num: '137UP', name: 'Farid Express', src: 'Karachi City', dst: 'Lahore Junction', type: 'Express', dur: '26h 00m', dist: 1260, status: 'Delayed' },
    { num: '211UP', name: 'Kashmore Passenger', src: 'Sukkur', dst: 'Kashmore', type: 'Passenger', dur: '4h 00m', dist: 180, status: 'On Time' },
    { num: '209UP', name: 'Faiz Ahmed Faiz Express', src: 'Lahore Junction', dst: 'Narowal', type: 'Passenger', dur: '2h 30m', dist: 98, status: 'On Time' },
    { num: '215UP', name: 'Lasan Passenger', src: 'Lahore Junction', dst: 'Sialkot', type: 'Passenger', dur: '3h 15m', dist: 134, status: 'On Time' },
    { num: '135UP', name: 'Chenab Express', src: 'Sargodha', dst: 'Lala Musa', type: 'Express', dur: '4h 15m', dist: 175, status: 'Boarding Soon' },
    { num: '121UP', name: 'Sargodha Express', src: 'Lahore Junction', dst: 'Sargodha', type: 'Express', dur: '3h 45m', dist: 172, status: 'On Time' },
    { num: '205UP', name: 'Babu Passenger', src: 'Lahore Junction', dst: 'Wazirabad', type: 'Passenger', dur: '2h 15m', dist: 100, status: 'On Time' },
    { num: '147UP', name: 'Mianwali Express', src: 'Lahore Junction', dst: 'Mari Indus', type: 'Express', dur: '9h 00m', dist: 420, status: 'On Time' },
    { num: '109UP', name: 'Dhabeji Express', src: 'Karachi City', dst: 'Dhabeji', type: 'Passenger', dur: '1h 30m', dist: 62, status: 'Departed' },
    { num: '221UP', name: 'Marvi Passenger', src: 'Mirpur Khas', dst: 'Khokhrapar', type: 'Passenger', dur: '3h 30m', dist: 135, status: 'On Time' },
    { num: '125UP', name: 'Sammi Express', src: 'Multan Cantt', dst: 'Rawalpindi', type: 'Express', dur: '11h 00m', dist: 580, status: 'On Time' },
    { num: '133UP', name: 'Kohat Express', src: 'Rawalpindi', dst: 'Kohat Cantt', type: 'Express', dur: '4h 00m', dist: 177, status: 'On Time' },
    { num: '129UP', name: 'Sandbar Express', src: 'Multan Cantt', dst: 'Lahore Junction', type: 'Express', dur: '5h 30m', dist: 312, status: 'Delayed' },
    { num: 'F-01', name: 'Laser Freight Express', src: 'Karachi Port', dst: 'Lahore Dry Port', type: 'Freight', dur: '36h 00m', dist: 1214, status: 'On Time' },
    { num: 'F-02', name: 'Goods Cargo Special', src: 'Karachi Port', dst: 'Rawalpindi Freight Yard', type: 'Freight', dur: '42h 00m', dist: 1548, status: 'On Time' },
    { num: 'F-03', name: 'Coal Freight Special', src: 'Port Qasim', dst: 'Sahiwal Power Plant', type: 'Freight', dur: '24h 00m', dist: 1020, status: 'On Time' },
    { num: 'F-04', name: 'Oil Tanker Special', src: 'Karachi Refinery', dst: 'Multan Oil Depot', type: 'Freight', dur: '22h 00m', dist: 925, status: 'On Time' },
    { num: 'F-05', name: 'Peshawar Cargo Freight', src: 'Karachi Port', dst: 'Peshawar Dry Port', type: 'Freight', dur: '48h 00m', dist: 1721, status: 'On Time' },
    { num: 'F-06', name: 'Quetta Container Freight', src: 'Karachi Port', dst: 'Quetta Dry Port', type: 'Freight', dur: '30h 00m', dist: 888, status: 'On Time' },
    { num: 'F-07', name: 'Cement Cargo Special', src: 'Daud Khel', dst: 'Karachi Port', type: 'Freight', dur: '32h 00m', dist: 1150, status: 'On Time' },
    { num: 'F-08', name: 'Grain Express Freight', src: 'Faisalabad', dst: 'Karachi Port', type: 'Freight', dur: '28h 00m', dist: 1100, status: 'On Time' },
    { num: 'F-09', name: 'Steel Cargo Express', src: 'Karachi Port', dst: 'Taxila Dry Port', type: 'Freight', dur: '40h 00m', dist: 1510, status: 'On Time' },
    { num: 'F-10', name: 'Phosphate Freight Train', src: 'Multan', dst: 'Karachi Port', type: 'Freight', dur: '24h 00m', dist: 925, status: 'On Time' }
  ];

  const fullList = [...dummyTrainsList];
  names.forEach((item, idx) => {
    const isFreight = item.type === 'Freight';
    const depHour = (6 + (idx * 2) % 18).toString().padStart(2, '0');
    fullList.push({
      trainNumber: item.num,
      trainName: item.name,
      trainType: item.type as any,
      sourceStation: item.src,
      destinationStation: item.dst,
      departureTime: `${depHour}:00`,
      arrivalTime: `${((parseInt(depHour) + 12) % 24).toString().padStart(2, '0')}:30`,
      duration: item.dur,
      distance: item.dist,
      status: item.status as any,
      platform: ((idx % 4) + 1).toString(),
      fareEconomy: isFreight ? 0 : Math.round(item.dist * 2.5),
      fareBusiness: isFreight ? 0 : Math.round(item.dist * 5.5),
      fareAC: isFreight ? 0 : Math.round(item.dist * 7.5),
      daysOfOperation: ['Daily'],
      route: 'Main Line',
      availableSeats: isFreight ? 0 : 35,
      lastUpdated: '1 min ago',
      intermediateStations: [
        { stationCode: 'SRC', stationName: item.src, arrival: `${depHour}:00`, departure: `${depHour}:00`, stopDurationMinutes: 0, distanceKm: 0, platform: '1' },
        { stationCode: 'MID', stationName: 'Midway Station', arrival: `${(parseInt(depHour)+4)%24}:15`, departure: `${(parseInt(depHour)+4)%24}:25`, stopDurationMinutes: 10, distanceKm: Math.round(item.dist / 2), platform: '2' },
        { stationCode: 'DST', stationName: item.dst, arrival: `${((parseInt(depHour) + 12) % 24).toString().padStart(2, '0')}:30`, departure: `${((parseInt(depHour) + 12) % 24).toString().padStart(2, '0')}:30`, stopDurationMinutes: 0, distanceKm: item.dist, platform: '1' }
      ]
    });
  });

  return fullList;
};

export async function seedDatabase() {
  try {
    const trainCount = await Train.countDocuments();
    if (trainCount === 0) {
      console.log('[SEED] Seeding 50+ Pakistan Railways trains...');
      const allTrains = generateExtraTrains();
      await Train.insertMany(allTrains);
      console.log(`[SEED] Successfully inserted ${allTrains.length} trains.`);
    }

    const stationCount = await Station.countDocuments();
    if (stationCount === 0) {
      console.log('[SEED] Seeding stations...');
      await Station.insertMany(dummyStations);
      console.log(`[SEED] Successfully inserted ${dummyStations.length} stations.`);
    }

    const routeCount = await Route.countDocuments();
    if (routeCount === 0) {
      console.log('[SEED] Seeding routes...');
      await Route.insertMany(dummyRoutes);
      console.log(`[SEED] Successfully inserted ${dummyRoutes.length} routes.`);
    }
  } catch (err: any) {
    console.error('[SEED-ERROR] Failed to seed database:', err);
  }
}
