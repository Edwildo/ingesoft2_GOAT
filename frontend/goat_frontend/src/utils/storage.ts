import { STORAGE_KEYS } from './constants';
import { User } from '../types/auth.types';

export const storage = {
  get: <T>(key: string): T | null => {
    try {
      const item = localStorage.getItem(key);
      return item ? (JSON.parse(item) as T) : null;
    } catch {
      return null;
    }
  },

  set: <T>(key: string, value: T): void => {
    try {
      localStorage.setItem(key, JSON.stringify(value));
    } catch {
      // Silently fail - localStorage puede estar deshabilitado o lleno
    }
  },

  remove: (key: string): void => {
    try {
      localStorage.removeItem(key);
    } catch {
      // Silently fail
    }
  },

  clear: (): void => {
    try {
      localStorage.clear();
    } catch {
      // Silently fail
    }
  },
};

export const authStorage = {
  getToken: (): string | null => {
    return localStorage.getItem(STORAGE_KEYS.AUTH_TOKEN);
  },

  setToken: (token: string): void => {
    localStorage.setItem(STORAGE_KEYS.AUTH_TOKEN, token);
  },

  removeToken: (): void => {
    localStorage.removeItem(STORAGE_KEYS.AUTH_TOKEN);
  },

  getUser: (): User | null => {
    return storage.get<User>(STORAGE_KEYS.USER_DATA);
  },

  setUser: (user: User): void => {
    storage.set(STORAGE_KEYS.USER_DATA, user);
    if (user.email) {
      localStorage.setItem(STORAGE_KEYS.USER_EMAIL, user.email);
    }
    if (user.id) {
      localStorage.setItem(STORAGE_KEYS.USER_ID, user.id);
    }
  },

  removeUser: (): void => {
    storage.remove(STORAGE_KEYS.USER_DATA);
    localStorage.removeItem(STORAGE_KEYS.USER_EMAIL);
    localStorage.removeItem(STORAGE_KEYS.USER_ID);
  },

  clear: (): void => {
    authStorage.removeToken();
    authStorage.removeUser();
  },
};

