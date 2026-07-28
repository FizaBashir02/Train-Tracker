import { Request, Response } from 'express';

const DUMMY_ROUTES = [
  { id: 'ml1', routeName: 'Main Line 1 (ML-1)', sourceCity: 'Karachi', destinationCity: 'Peshawar', totalDistanceKm: 1687, totalStationsCount: 184, keyViaStations: ['Hyderabad', 'Rohri', 'Multan', 'Lahore', 'Rawalpindi'], majorTrains: ['Green Line', 'Karakoram Express', 'Tezgam', 'Khyber Mail'] },
  { id: 'ml2', routeName: 'Main Line 2 (ML-2)', sourceCity: 'Kotri', destinationCity: 'Attock City', totalDistanceKm: 1250, totalStationsCount: 92, keyViaStations: ['Dadu', 'Larkana', 'Jacobabad', 'Dera Ghazi Khan', 'Kundian'], majorTrains: ['Mehr Express', 'Khushal Khan Khattak Express'] },
  { id: 'ml3', routeName: 'Main Line 3 (ML-3)', sourceCity: 'Spezand', destinationCity: 'Taftan', totalDistanceKm: 520, totalStationsCount: 32, keyViaStations: ['Nushki', 'Dalbandin', 'Nok Kundi'], majorTrains: ['Zahedan Passenger'] }
];

export const getRoutes = async (req: Request, res: Response) => {
  return res.status(200).json(DUMMY_ROUTES);
};

export const getRouteDetails = async (req: Request, res: Response) => {
  const { id } = req.params;
  const route = DUMMY_ROUTES.find(r => r.id === id || r.routeName.toLowerCase().includes(id.toLowerCase())) || DUMMY_ROUTES[0];
  return res.status(200).json(route);
};
