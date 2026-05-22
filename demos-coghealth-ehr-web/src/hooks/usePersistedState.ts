import { useEffect, useState } from 'react';

export function usePersistedState<T>(key: string, defaultValue: T) {
  const [value, setValue] = useState<T>(() => {
    try {
      const stored = localStorage.getItem(key);
      return stored ? { ...defaultValue, ...JSON.parse(stored) } : defaultValue;
    } catch {
      return defaultValue;
    }
  });

  useEffect(() => {
    localStorage.setItem(key, JSON.stringify(value));
  }, [key, value]);

  return [value, setValue] as const;
}

export function usePersistedArrayState<T>(key: string, defaultValue: T[]) {
  const [value, setValue] = useState<T[]>(() => {
    try {
      const stored = localStorage.getItem(key);
      return stored ? JSON.parse(stored) : defaultValue;
    } catch {
      return defaultValue;
    }
  });

  useEffect(() => {
    localStorage.setItem(key, JSON.stringify(value));
  }, [key, value]);

  return [value, setValue] as const;
}

export function usePersistedSetState<T>(key: string, defaultValue: T[]) {
  const [value, setValue] = useState<Set<T>>(() => {
    try {
      const stored = localStorage.getItem(key);
      return new Set(stored ? JSON.parse(stored) : defaultValue);
    } catch {
      return new Set(defaultValue);
    }
  });

  useEffect(() => {
    localStorage.setItem(key, JSON.stringify(Array.from(value)));
  }, [key, value]);

  return [value, setValue] as const;
}
