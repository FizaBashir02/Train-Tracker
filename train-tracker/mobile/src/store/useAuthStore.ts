import { create } from 'zustand';
import AsyncStorage from '@react-native-async-storage/async-storage';

interface User {
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  role: 'passenger' | 'admin' | 'conductor';
}

interface AuthState {
  user: User | null;
  accessToken: string | null;
  refreshToken: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  setSession: (user: User, accessToken: string, refreshToken: string) => Promise<void>;
  updateAccessToken: (accessToken: string) => Promise<void>;
  clearSession: () => Promise<void>;
  restoreSession: () => Promise<void>;
}

export const useAuthStore = create<AuthState>((set) => ({
  user: null,
  accessToken: null,
  refreshToken: null,
  isAuthenticated: false,
  isLoading: true,

  setSession: async (user, accessToken, refreshToken) => {
    try {
      await AsyncStorage.setItem('accessToken', accessToken);
      await AsyncStorage.setItem('refreshToken', refreshToken);
      await AsyncStorage.setItem('user', JSON.stringify(user));
      set({ user, accessToken, refreshToken, isAuthenticated: true, isLoading: false });
    } catch (e) {
      console.error('Failed to persist login session credentials', e);
    }
  },

  updateAccessToken: async (accessToken) => {
    try {
      await AsyncStorage.setItem('accessToken', accessToken);
      set({ accessToken });
    } catch (e) {
      console.error('Failed to update access token session', e);
    }
  },

  clearSession: async () => {
    try {
      await AsyncStorage.removeItem('accessToken');
      await AsyncStorage.removeItem('refreshToken');
      await AsyncStorage.removeItem('user');
      set({ user: null, accessToken: null, refreshToken: null, isAuthenticated: false, isLoading: false });
    } catch (e) {
      console.error('Failed to clear persisted auth session', e);
    }
  },

  restoreSession: async () => {
    try {
      const accessToken = await AsyncStorage.getItem('accessToken');
      const refreshToken = await AsyncStorage.getItem('refreshToken');
      const userStr = await AsyncStorage.getItem('user');

      if (accessToken && refreshToken && userStr) {
        set({
          accessToken,
          refreshToken,
          user: JSON.parse(userStr),
          isAuthenticated: true,
          isLoading: false
        });
      } else {
        set({ isLoading: false });
      }
    } catch (e) {
      console.error('Failed to restore login session', e);
      set({ isLoading: false });
    }
  }
}));
