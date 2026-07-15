import React, { useEffect, useState } from 'react';
import { View, Text, StyleSheet, Dimensions, ActivityIndicator, TouchableOpacity } from 'react-native';
import MapView, { Marker, Polyline, PROVIDER_GOOGLE } from 'react-native-maps';
import { useQuery } from '@tanstack/react-query';
import { api } from '../services/apiService';
import { socketService } from '../services/socketService';
import { useAuthStore } from '../store/useAuthStore';

interface LiveStatusScreenProps {
  route: any;
  navigation: any;
}

export const LiveStatusScreen: React.FC<LiveStatusScreenProps> = ({ route }) => {
  const trainNumber = route?.params?.trainNumber || '7UP';
  const token = useAuthStore((state) => state.accessToken);

  // Live positions
  const [coords, setCoords] = useState<{ latitude: number; longitude: number } | null>(null);
  const [speed, setSpeed] = useState<number>(0);
  const [delay, setDelay] = useState<number>(0);

  // Fetch initial REST baseline status
  const { data: initialStatus, isLoading } = useQuery({
    queryKey: ['liveStatus', trainNumber],
    queryFn: async () => {
      const res = await api.get(`/trains/live-status/${trainNumber}`);
      return res.data;
    },
  });

  useEffect(() => {
    if (initialStatus) {
      setCoords({
        latitude: initialStatus.currentLatitude,
        longitude: initialStatus.currentLongitude,
      });
      setSpeed(initialStatus.currentSpeedKmh);
      setDelay(initialStatus.delayMinutes);
    }
  }, [initialStatus]);

  // Connect and listen to real-time events via Socket.IO
  useEffect(() => {
    if (token) {
      socketService.connect(token);
      socketService.subscribeToTrain(trainNumber, (telemetry) => {
        console.log('Real-time position stream packet received:', telemetry);
        if (telemetry.latitude && telemetry.longitude) {
          setCoords({
            latitude: telemetry.latitude,
            longitude: telemetry.longitude,
          });
        }
        if (telemetry.speedKmh !== undefined) {
          setSpeed(telemetry.speedKmh);
        }
        if (telemetry.delayMinutes !== undefined) {
          setDelay(telemetry.delayMinutes);
        }
      });
    }

    return () => {
      socketService.unsubscribeFromTrain(trainNumber);
    };
  }, [trainNumber, token]);

  if (isLoading || !coords) {
    return (
      <View style={styles.loaderContainer}>
        <ActivityIndicator size="large" color="#0F7A3E" />
        <Text style={styles.loaderText}>Establishing Secure GPS Connection...</Text>
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <MapView
        provider={PROVIDER_GOOGLE}
        style={styles.map}
        initialRegion={{
          latitude: coords.latitude,
          longitude: coords.longitude,
          latitudeDelta: 0.12,
          longitudeDelta: 0.12,
        }}
      >
        <Marker
          coordinate={coords}
          title={initialStatus?.trainName || `Train ${trainNumber}`}
          description={`Speed: ${speed} km/h | Delay: ${delay} mins`}
        />
      </MapView>

      {/* Floating HUD status cards */}
      <View style={styles.overlayContainer}>
        <View style={styles.hudCard}>
          <View style={styles.row}>
            <View>
              <Text style={styles.trainName}>{initialStatus?.trainName || 'Tezgam Express'}</Text>
              <Text style={styles.trainNumber}>Locomotive Number: {trainNumber}</Text>
            </View>
            <View style={styles.speedBadge}>
              <Text style={styles.speedValue}>{speed}</Text>
              <Text style={styles.speedUnit}>KM/H</Text>
            </View>
          </View>

          <View style={styles.divider} />

          <View style={styles.row}>
            <View>
              <Text style={styles.metaTitle}>PREVIOUS STATION</Text>
              <Text style={styles.metaValue}>{initialStatus?.previousStation || 'Karachi Cantt'}</Text>
            </View>
            <View style={{ alignItems: 'flex-end' }}>
              <Text style={styles.metaTitle}>NEXT STATION</Text>
              <Text style={styles.metaValue}>{initialStatus?.nextStation || 'Hyderabad Jn'}</Text>
            </View>
          </View>

          <View style={styles.progressContainer}>
            <View style={[styles.progressBar, { width: `${(initialStatus?.journeyProgress || 0.15) * 100}%` }]} />
          </View>

          <View style={styles.row}>
            <Text style={styles.metaTitle}>DELAY STATUS</Text>
            <Text style={[styles.metaValue, { color: delay > 15 ? '#E53935' : '#4CAF50' }]}>
              {delay > 0 ? `${delay} Minutes Late` : 'On Schedule'}
            </Text>
          </View>
        </View>
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    ...StyleSheet.absoluteFillObject,
    justifyContent: 'flex-end',
    alignItems: 'center',
  },
  map: {
    ...StyleSheet.absoluteFillObject,
  },
  loaderContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#121212',
  },
  loaderText: {
    color: '#FFFFFF',
    marginTop: 16,
    fontSize: 15,
    fontWeight: 'bold',
  },
  overlayContainer: {
    position: 'absolute',
    bottom: 24,
    left: 16,
    right: 16,
  },
  hudCard: {
    backgroundColor: '#121212',
    borderRadius: 16,
    padding: 16,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.3,
    shadowRadius: 8,
    elevation: 8,
    borderWidth: 1,
    borderColor: '#2A2A2A',
  },
  row: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  trainName: {
    color: '#FFFFFF',
    fontSize: 18,
    fontWeight: 'bold',
  },
  trainNumber: {
    color: '#8E8E93',
    fontSize: 12,
    marginTop: 2,
  },
  speedBadge: {
    backgroundColor: '#0F7A3E',
    borderRadius: 12,
    paddingHorizontal: 12,
    paddingVertical: 8,
    alignItems: 'center',
  },
  speedValue: {
    color: '#FFFFFF',
    fontSize: 18,
    fontWeight: 'bold',
  },
  speedUnit: {
    color: '#FFFFFF',
    fontSize: 9,
    fontWeight: '500',
  },
  divider: {
    height: 1,
    backgroundColor: '#2A2A2A',
    marginVertical: 12,
  },
  metaTitle: {
    color: '#8E8E93',
    fontSize: 10,
    fontWeight: 'bold',
    letterSpacing: 0.5,
  },
  metaValue: {
    color: '#FFFFFF',
    fontSize: 14,
    fontWeight: '600',
    marginTop: 2,
  },
  progressContainer: {
    height: 6,
    backgroundColor: '#2A2A2A',
    borderRadius: 3,
    marginVertical: 14,
    overflow: 'hidden',
  },
  progressBar: {
    height: '100%',
    backgroundColor: '#0F7A3E',
    borderRadius: 3,
  },
});
