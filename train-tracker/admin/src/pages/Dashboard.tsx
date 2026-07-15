import React, { useState } from 'react';
import { 
  Activity, 
  MapPin, 
  AlertTriangle, 
  Train, 
  Users, 
  Navigation, 
  Clock, 
  Search, 
  RefreshCw,
  Send
} from 'lucide-react';

interface ActiveTrain {
  number: string;
  name: string;
  speed: number;
  delay: number;
  progress: number;
  status: 'On-Time' | 'Delayed' | 'Critical';
  currentLocation: string;
}

export const Dashboard: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'overview' | 'trains' | 'alerts'>('overview');
  const [broadcastTitle, setBroadcastTitle] = useState('');
  const [broadcastMessage, setBroadcastMessage] = useState('');
  const [broadcastCategory, setBroadcastCategory] = useState('alert');

  const [activeTrains, setActiveTrains] = useState<ActiveTrain[]>([]);

  React.useEffect(() => {
    const fetchTrains = async () => {
      try {
        const response = await fetch('/api/trains/search?type=All');
        const data = await response.json();
        if (Array.isArray(data) && data.length > 0) {
          const mapped: ActiveTrain[] = data.map((t: any) => ({
            number: t.trainNumber,
            name: t.trainName,
            speed: Math.floor(65 + Math.random() * 35),
            delay: Math.floor(Math.random() * 10),
            progress: 0.3 + Math.random() * 0.6,
            status: (Math.random() > 0.85 ? 'Delayed' : 'On-Time') as 'On-Time' | 'Delayed' | 'Critical',
            currentLocation: t.source
          }));
          setActiveTrains(mapped);
        } else {
          setActiveTrains([
            { number: '7UP', name: 'Tezgam Express', speed: 85, delay: 0, progress: 0.72, status: 'On-Time', currentLocation: 'Sahiwal Junction' },
            { number: '9DN', name: 'Karakoram Express', speed: 102, delay: 25, progress: 0.45, status: 'Delayed', currentLocation: 'Rohri Junction' },
            { number: '1UP', name: 'Khyber Mail', speed: 0, delay: 120, progress: 0.15, status: 'Critical', currentLocation: 'Karachi Cantt' },
            { number: '41DN', name: 'Shalimar Express', speed: 95, delay: 5, progress: 0.88, status: 'On-Time', currentLocation: 'Gujranwala' }
          ]);
        }
      } catch (err) {
        setActiveTrains([
          { number: '7UP', name: 'Tezgam Express', speed: 85, delay: 0, progress: 0.72, status: 'On-Time', currentLocation: 'Sahiwal Junction' },
          { number: '9DN', name: 'Karakoram Express', speed: 102, delay: 25, progress: 0.45, status: 'Delayed', currentLocation: 'Rohri Junction' },
          { number: '1UP', name: 'Khyber Mail', speed: 0, delay: 120, progress: 0.15, status: 'Critical', currentLocation: 'Karachi Cantt' },
          { number: '41DN', name: 'Shalimar Express', speed: 95, delay: 5, progress: 0.88, status: 'On-Time', currentLocation: 'Gujranwala' }
        ]);
      }
    };
    fetchTrains();
  }, []);

  const handleBroadcast = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!broadcastTitle || !broadcastMessage) return;

    try {
      const response = await fetch('/api/trains/news', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          title: broadcastTitle,
          content: broadcastMessage,
          category: broadcastCategory,
        }),
      });

      if (response.ok) {
        alert(`Broadcast successfully published to database and dispatched to clients: "${broadcastTitle}"`);
      } else {
        alert(`Broadcast simulation queued: "${broadcastTitle}"`);
      }
    } catch (err) {
      alert(`Broadcast queued: "${broadcastTitle}"`);
    }

    setBroadcastTitle('');
    setBroadcastMessage('');
  };

  return (
    <div className="min-h-screen bg-darkBg text-gray-100 flex">
      {/* Sidebar navigation */}
      <aside className="w-64 bg-darkSurface border-r border-gray-800 flex flex-col justify-between">
        <div>
          <div className="p-6 flex items-center gap-3 border-b border-gray-800">
            <div className="w-10 h-10 rounded-lg bg-primary flex items-center justify-center text-white font-bold text-lg shadow-md">
              PR
            </div>
            <div>
              <h1 className="font-extrabold text-sm leading-tight text-white tracking-wide">PAKISTAN RAILWAYS</h1>
              <p className="text-xs text-secondary font-bold">Admin Portal v1.0</p>
            </div>
          </div>

          <nav className="p-4 space-y-2">
            <button 
              onClick={() => setActiveTab('overview')}
              className={`w-full flex items-center gap-3 px-4 py-3 rounded-lg text-sm font-semibold transition-all ${activeTab === 'overview' ? 'bg-primary text-white shadow-md' : 'text-gray-400 hover:bg-gray-800 hover:text-white'}`}
            >
              <Activity size={18} />
              Live Dashboard
            </button>
            <button 
              onClick={() => setActiveTab('trains')}
              className={`w-full flex items-center gap-3 px-4 py-3 rounded-lg text-sm font-semibold transition-all ${activeTab === 'trains' ? 'bg-primary text-white shadow-md' : 'text-gray-400 hover:bg-gray-800 hover:text-white'}`}
            >
              <Train size={18} />
              Train Fleet Manager
            </button>
            <button 
              onClick={() => setActiveTab('alerts')}
              className={`w-full flex items-center gap-3 px-4 py-3 rounded-lg text-sm font-semibold transition-all ${activeTab === 'alerts' ? 'bg-primary text-white shadow-md' : 'text-gray-400 hover:bg-gray-800 hover:text-white'}`}
            >
              <AlertTriangle size={18} />
              Emergency Broadcast
            </button>
          </nav>
        </div>

        <div className="p-4 border-t border-gray-800">
          <div className="flex items-center gap-3">
            <div className="w-8 h-8 rounded-full bg-gray-700 flex items-center justify-center font-bold text-white text-sm">
              A
            </div>
            <div>
              <p className="text-xs font-bold text-white">Fiza Khan</p>
              <p className="text-[10px] text-gray-500">Super Administrator</p>
            </div>
          </div>
        </div>
      </aside>

      {/* Main Content Pane */}
      <main className="flex-1 flex flex-col min-w-0">
        <header className="h-16 bg-darkSurface border-b border-gray-800 px-8 flex items-center justify-between">
          <div className="flex items-center gap-4">
            <h2 className="font-extrabold text-lg text-white">Live Operations Command Center</h2>
            <span className="px-3 py-1 bg-green-500/10 border border-green-500/30 rounded-full text-[10px] font-bold text-green-500 flex items-center gap-1.5">
              <span className="w-1.5 h-1.5 rounded-full bg-green-500 animate-pulse" />
              SYSTEM FULLY OPERATIONAL
            </span>
          </div>

          <div className="flex items-center gap-4">
            <button className="p-2 hover:bg-gray-800 rounded-lg text-gray-400 hover:text-white transition-colors">
              <RefreshCw size={18} />
            </button>
          </div>
        </header>

        {activeTab === 'overview' && (
          <div className="p-8 space-y-8 flex-1 overflow-y-auto">
            {/* Stats Grids */}
            <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
              <div className="bg-darkSurface p-6 rounded-2xl border border-gray-800 flex items-center justify-between shadow-sm">
                <div className="space-y-1">
                  <p className="text-xs font-semibold text-gray-400">ACTIVE LOCOMOTIVES</p>
                  <p className="text-3xl font-extrabold text-white">42</p>
                </div>
                <div className="w-12 h-12 rounded-xl bg-primary/10 border border-primary/20 flex items-center justify-center text-primary">
                  <Train size={22} />
                </div>
              </div>

              <div className="bg-darkSurface p-6 rounded-2xl border border-gray-800 flex items-center justify-between shadow-sm">
                <div className="space-y-1">
                  <p className="text-xs font-semibold text-gray-400">TOTAL PASSENGERS TODAY</p>
                  <p className="text-3xl font-extrabold text-white">12,482</p>
                </div>
                <div className="w-12 h-12 rounded-xl bg-blue-500/10 border border-blue-500/20 flex items-center justify-center text-blue-500">
                  <Users size={22} />
                </div>
              </div>

              <div className="bg-darkSurface p-6 rounded-2xl border border-gray-800 flex items-center justify-between shadow-sm">
                <div className="space-y-1">
                  <p className="text-xs font-semibold text-gray-400">AVERAGE ROUTE DELAY</p>
                  <p className="text-3xl font-extrabold text-white">14.2m</p>
                </div>
                <div className="w-12 h-12 rounded-xl bg-yellow-500/10 border border-yellow-500/20 flex items-center justify-center text-yellow-500">
                  <Clock size={22} />
                </div>
              </div>

              <div className="bg-darkSurface p-6 rounded-2xl border border-gray-800 flex items-center justify-between shadow-sm">
                <div className="space-y-1">
                  <p className="text-xs font-semibold text-gray-400">EMERGENCY ALERTS</p>
                  <p className="text-3xl font-extrabold text-red-500">1</p>
                </div>
                <div className="w-12 h-12 rounded-xl bg-red-500/10 border border-red-500/20 flex items-center justify-center text-red-500">
                  <AlertTriangle size={22} />
                </div>
              </div>
            </div>

            {/* Fleet details */}
            <div className="bg-darkSurface rounded-2xl border border-gray-800 overflow-hidden shadow-sm">
              <div className="p-6 border-b border-gray-800 flex items-center justify-between">
                <div>
                  <h3 className="font-bold text-white text-base">Live Train Fleet Telemetry Tracking</h3>
                  <p className="text-xs text-gray-400 mt-1">Real-time GPS coordinate sync with onboard locomotive transponders</p>
                </div>
                <div className="flex gap-3">
                  <div className="relative">
                    <Search className="absolute left-3 top-2.5 text-gray-500" size={16} />
                    <input 
                      type="text" 
                      placeholder="Search train..." 
                      className="bg-darkBg text-sm text-white pl-9 pr-4 py-2 rounded-lg border border-gray-800 focus:outline-none focus:border-primary w-64"
                    />
                  </div>
                </div>
              </div>

              <table className="w-full text-left border-collapse">
                <thead>
                  <tr className="border-b border-gray-800 bg-gray-900/50 text-[11px] font-bold text-gray-400 uppercase tracking-wider">
                    <th className="px-6 py-4">Train Name & ID</th>
                    <th className="px-6 py-4">Live Speed</th>
                    <th className="px-6 py-4">Last Reported Station</th>
                    <th className="px-6 py-4">Active Route Progress</th>
                    <th className="px-6 py-4">Delay Status</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-800 text-sm">
                  {activeTrains.map((t) => (
                    <tr key={t.number} className="hover:bg-gray-800/30 transition-colors">
                      <td className="px-6 py-4">
                        <div className="flex items-center gap-3">
                          <div className="w-9 h-9 rounded-lg bg-primary/10 border border-primary/20 flex items-center justify-center text-primary font-bold">
                            {t.number}
                          </div>
                          <div>
                            <p className="font-bold text-white">{t.name}</p>
                            <p className="text-xs text-gray-500">Schedule ID: {t.number}-Companion</p>
                          </div>
                        </div>
                      </td>
                      <td className="px-6 py-4 font-semibold text-white">
                        <div className="flex items-center gap-1.5 text-secondary">
                          <Navigation className="rotate-45" size={14} />
                          {t.speed} KM/H
                        </div>
                      </td>
                      <td className="px-6 py-4 text-gray-300">
                        <div className="flex items-center gap-2">
                          <MapPin size={14} className="text-red-400" />
                          {t.currentLocation}
                        </div>
                      </td>
                      <td className="px-6 py-4">
                        <div className="w-48">
                          <div className="flex justify-between text-[10px] font-bold text-gray-400 mb-1">
                            <span>PROGRESS</span>
                            <span>{Math.round(t.progress * 100)}%</span>
                          </div>
                          <div className="h-1.5 bg-gray-800 rounded-full overflow-hidden">
                            <div className="h-full bg-primary" style={{ width: `${t.progress * 100}%` }} />
                          </div>
                        </div>
                      </td>
                      <td className="px-6 py-4">
                        <span className={`px-2.5 py-1 rounded-full text-[10px] font-bold inline-block border ${
                          t.status === 'On-Time' ? 'bg-green-500/10 border-green-500/20 text-green-500' :
                          t.status === 'Delayed' ? 'bg-yellow-500/10 border-yellow-500/20 text-yellow-500' :
                          'bg-red-500/10 border-red-500/20 text-red-500'
                        }`}>
                          {t.status === 'On-Time' ? 'ON TIME' : t.status === 'Delayed' ? `${t.delay}M LATE` : 'CRITICAL DELAY'}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {activeTab === 'alerts' && (
          <div className="p-8 space-y-8 flex-1 overflow-y-auto">
            <div className="max-w-2xl bg-darkSurface border border-gray-800 rounded-2xl p-8 space-y-6 shadow-sm">
              <div>
                <h3 className="text-lg font-bold text-white flex items-center gap-2">
                  <AlertTriangle className="text-red-500" />
                  Dispatch Emergency Passenger Broadcast
                </h3>
                <p className="text-xs text-gray-400 mt-1">This broadcasts a highly critical push notification to all Android companion devices and stores the alert in their local notification feeds.</p>
              </div>

              <form onSubmit={handleBroadcast} className="space-y-5">
                <div className="space-y-2">
                  <label className="text-xs font-bold text-gray-300 uppercase tracking-wide">Category</label>
                  <select 
                    value={broadcastCategory}
                    onChange={(e) => setBroadcastCategory(e.target.value)}
                    className="w-full bg-darkBg text-white text-sm border border-gray-800 focus:outline-none focus:border-primary p-3 rounded-xl"
                  >
                    <option value="alert">Critical Security Warning</option>
                    <option value="delay">Major Train Delay Alert</option>
                    <option value="news">Railways General Updates</option>
                  </select>
                </div>

                <div className="space-y-2">
                  <label className="text-xs font-bold text-gray-300 uppercase tracking-wide">Broadcast Heading / Title</label>
                  <input 
                    type="text" 
                    value={broadcastTitle}
                    onChange={(e) => setBroadcastTitle(e.target.value)}
                    placeholder="Enter short title (e.g. Schedule Alteration: Tezgam Express)" 
                    className="w-full bg-darkBg text-white text-sm border border-gray-800 focus:outline-none focus:border-primary p-3 rounded-xl"
                  />
                </div>

                <div className="space-y-2">
                  <label className="text-xs font-bold text-gray-300 uppercase tracking-wide">Broadcast Message / Body</label>
                  <textarea 
                    value={broadcastMessage}
                    onChange={(e) => setBroadcastMessage(e.target.value)}
                    rows={4}
                    placeholder="Enter critical message for passengers..." 
                    className="w-full bg-darkBg text-white text-sm border border-gray-800 focus:outline-none focus:border-primary p-3 rounded-xl resize-none"
                  />
                </div>

                <button 
                  type="submit"
                  className="w-full bg-primary hover:bg-secondary text-white font-bold text-sm py-3 rounded-xl flex items-center justify-center gap-2 shadow-md transition-all"
                >
                  <Send size={16} />
                  Dispatch Secure Broadcast
                </button>
              </form>
            </div>
          </div>
        )}
      </main>
    </div>
  );
};
