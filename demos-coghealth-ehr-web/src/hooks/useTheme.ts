import { useCallback, useEffect, useSyncExternalStore } from 'react';

export type ThemePreference = 'light' | 'dark' | 'system';

const STORAGE_KEY = 'coghealth_theme';

function isDarkPreference(pref: ThemePreference): boolean {
  if (pref === 'dark') return true;
  if (pref === 'light') return false;
  return typeof window !== 'undefined'
    && window.matchMedia
    && window.matchMedia('(prefers-color-scheme: dark)').matches;
}

function readStoredPreference(): ThemePreference {
  if (typeof window === 'undefined') return 'light';
  const raw = window.localStorage.getItem(STORAGE_KEY);
  if (raw === 'dark' || raw === 'light' || raw === 'system') return raw;
  return 'light';
}

function applyDarkClass(isDark: boolean) {
  if (typeof document === 'undefined') return;
  const root = document.documentElement;
  if (isDark) {
    root.classList.add('dark');
  } else {
    root.classList.remove('dark');
  }
}

// ---- Shared store so all useTheme() instances stay in sync ----
const listeners = new Set<() => void>();
let currentPref: ThemePreference = readStoredPreference();

function notify() {
  for (const l of listeners) l();
}

function subscribe(listener: () => void): () => void {
  listeners.add(listener);
  return () => {
    listeners.delete(listener);
  };
}

function getSnapshot(): ThemePreference {
  return currentPref;
}

function getServerSnapshot(): ThemePreference {
  return 'light';
}

function setStoredPreference(pref: ThemePreference) {
  currentPref = pref;
  if (typeof window !== 'undefined') {
    window.localStorage.setItem(STORAGE_KEY, pref);
  }
  applyDarkClass(isDarkPreference(pref));
  notify();
}

// Sync across tabs.
if (typeof window !== 'undefined') {
  window.addEventListener('storage', (e) => {
    if (e.key !== STORAGE_KEY) return;
    const next = readStoredPreference();
    if (next !== currentPref) {
      currentPref = next;
      applyDarkClass(isDarkPreference(next));
      notify();
    }
  });
}

/**
 * Apply the stored theme as early as possible so the chrome doesn't flash.
 * Call once from main.tsx before React renders.
 */
export function initializeTheme() {
  applyDarkClass(isDarkPreference(currentPref));
}

/**
 * React hook for reading and updating the user's theme preference.
 * Persists to localStorage and reacts to OS-level theme changes when set to 'system'.
 */
export function useTheme() {
  const preference = useSyncExternalStore(subscribe, getSnapshot, getServerSnapshot);

  // React to OS changes when on 'system'.
  useEffect(() => {
    if (preference !== 'system') return;
    if (typeof window === 'undefined' || !window.matchMedia) return;
    const mql = window.matchMedia('(prefers-color-scheme: dark)');
    const handler = () => {
      applyDarkClass(mql.matches);
      notify();
    };
    mql.addEventListener('change', handler);
    return () => mql.removeEventListener('change', handler);
  }, [preference]);

  const setPreference = useCallback((pref: ThemePreference) => {
    setStoredPreference(pref);
  }, []);

  const toggle = useCallback(() => {
    setStoredPreference(isDarkPreference(currentPref) ? 'light' : 'dark');
  }, []);

  return {
    preference,
    isDark: isDarkPreference(preference),
    setPreference,
    toggle,
  };
}
