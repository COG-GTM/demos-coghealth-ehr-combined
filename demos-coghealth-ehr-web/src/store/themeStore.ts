import { create } from 'zustand';

export type ThemeMode = 'light' | 'dark' | 'system';

const STORAGE_KEY = 'coghealth_theme';
const SETTINGS_KEY = 'coghealth_settings';

const prefersDark = (): boolean =>
  typeof window !== 'undefined' &&
  typeof window.matchMedia === 'function' &&
  window.matchMedia('(prefers-color-scheme: dark)').matches;

const resolve = (mode: ThemeMode): 'light' | 'dark' =>
  mode === 'system' ? (prefersDark() ? 'dark' : 'light') : mode;

const isThemeMode = (value: unknown): value is ThemeMode =>
  value === 'light' || value === 'dark' || value === 'system';

const readStoredMode = (): ThemeMode => {
  const stored = localStorage.getItem(STORAGE_KEY);
  if (isThemeMode(stored)) return stored;
  try {
    const raw = localStorage.getItem(SETTINGS_KEY);
    if (raw) {
      const theme = (JSON.parse(raw) as { appearance?: { theme?: unknown } })?.appearance?.theme;
      if (isThemeMode(theme)) return theme;
    }
  } catch {
    // ignore malformed settings
  }
  return 'light';
};

const applyClass = (mode: ThemeMode): void => {
  document.documentElement.classList.toggle('dark', resolve(mode) === 'dark');
};

interface ThemeState {
  mode: ThemeMode;
  resolved: 'light' | 'dark';
  setMode: (mode: ThemeMode) => void;
  toggle: () => void;
}

export const useThemeStore = create<ThemeState>((set, get) => ({
  mode: 'light',
  resolved: 'light',
  setMode: (mode) => {
    localStorage.setItem(STORAGE_KEY, mode);
    applyClass(mode);
    set({ mode, resolved: resolve(mode) });
  },
  toggle: () => {
    get().setMode(get().resolved === 'dark' ? 'light' : 'dark');
  },
}));

export function initTheme(): void {
  const mode = readStoredMode();
  applyClass(mode);
  useThemeStore.setState({ mode, resolved: resolve(mode) });

  if (typeof window !== 'undefined' && typeof window.matchMedia === 'function') {
    window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', () => {
      if (useThemeStore.getState().mode === 'system') {
        applyClass('system');
        useThemeStore.setState({ resolved: resolve('system') });
      }
    });
  }
}
