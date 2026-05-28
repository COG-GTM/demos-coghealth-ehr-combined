import { create } from 'zustand';

type ThemeMode = 'light' | 'dark' | 'system';

interface ThemeState {
  mode: ThemeMode;
  setMode: (mode: ThemeMode) => void;
  isDark: () => boolean;
}

const STORAGE_KEY = 'coghealth_theme';

function getSystemPrefersDark(): boolean {
  return window.matchMedia('(prefers-color-scheme: dark)').matches;
}

function resolveIsDark(mode: ThemeMode): boolean {
  if (mode === 'system') return getSystemPrefersDark();
  return mode === 'dark';
}

function applyThemeClass(mode: ThemeMode) {
  const dark = resolveIsDark(mode);
  document.documentElement.classList.toggle('dark', dark);
}

function loadStoredMode(): ThemeMode {
  try {
    const stored = localStorage.getItem(STORAGE_KEY);
    if (stored === 'light' || stored === 'dark' || stored === 'system') return stored;
  } catch { /* ignore */ }
  return 'light';
}

const initialMode = loadStoredMode();
applyThemeClass(initialMode);

export const useThemeStore = create<ThemeState>((set, get) => ({
  mode: initialMode,
  setMode: (mode: ThemeMode) => {
    localStorage.setItem(STORAGE_KEY, mode);
    applyThemeClass(mode);
    set({ mode });
  },
  isDark: () => resolveIsDark(get().mode),
}));

// Listen for system preference changes when in 'system' mode
window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', () => {
  const { mode } = useThemeStore.getState();
  if (mode === 'system') applyThemeClass(mode);
});
