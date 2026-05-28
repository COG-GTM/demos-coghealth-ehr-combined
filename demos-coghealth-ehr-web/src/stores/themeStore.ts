import { create } from 'zustand';

type ThemePreference = 'light' | 'dark' | 'system';

interface ThemeState {
  preference: ThemePreference;
  setPreference: (preference: ThemePreference) => void;
  isDark: () => boolean;
}

const STORAGE_KEY = 'coghealth_theme';

function getStoredPreference(): ThemePreference {
  try {
    const stored = localStorage.getItem(STORAGE_KEY);
    if (stored === 'light' || stored === 'dark' || stored === 'system') return stored;
  } catch {
    // ignore
  }
  return 'light';
}

function resolveIsDark(preference: ThemePreference): boolean {
  if (preference === 'dark') return true;
  if (preference === 'light') return false;
  return window.matchMedia('(prefers-color-scheme: dark)').matches;
}

export function applyThemeClass(preference: ThemePreference) {
  const dark = resolveIsDark(preference);
  document.documentElement.classList.toggle('dark', dark);
}

export const useThemeStore = create<ThemeState>((set, get) => ({
  preference: getStoredPreference(),
  setPreference: (preference: ThemePreference) => {
    localStorage.setItem(STORAGE_KEY, preference);
    applyThemeClass(preference);
    set({ preference });
  },
  isDark: () => resolveIsDark(get().preference),
}));
