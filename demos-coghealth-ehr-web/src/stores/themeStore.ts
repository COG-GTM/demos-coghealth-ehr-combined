import { create } from 'zustand';

export type ThemeMode = 'light' | 'dark' | 'system';
export type ResolvedTheme = 'light' | 'dark';

const STORAGE_KEY = 'coghealth_theme';

function getSystemTheme(): ResolvedTheme {
  return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
}

function resolve(mode: ThemeMode): ResolvedTheme {
  return mode === 'system' ? getSystemTheme() : mode;
}

function applyTheme(resolved: ResolvedTheme): void {
  const root = document.documentElement;
  root.classList.toggle('dark', resolved === 'dark');
  root.style.colorScheme = resolved;
}

function readStoredMode(): ThemeMode {
  const stored = localStorage.getItem(STORAGE_KEY);
  if (stored === 'light' || stored === 'dark' || stored === 'system') {
    return stored;
  }
  return 'light';
}

interface ThemeState {
  mode: ThemeMode;
  resolvedTheme: ResolvedTheme;
  setMode: (mode: ThemeMode) => void;
  toggle: () => void;
}

const initialMode = readStoredMode();

export const useThemeStore = create<ThemeState>((set, get) => ({
  mode: initialMode,
  resolvedTheme: resolve(initialMode),
  setMode: (mode) => {
    localStorage.setItem(STORAGE_KEY, mode);
    const resolvedTheme = resolve(mode);
    applyTheme(resolvedTheme);
    set({ mode, resolvedTheme });
  },
  toggle: () => {
    get().setMode(get().resolvedTheme === 'dark' ? 'light' : 'dark');
  },
}));

export function initTheme(): void {
  applyTheme(resolve(readStoredMode()));
  window
    .matchMedia('(prefers-color-scheme: dark)')
    .addEventListener('change', () => {
      const { mode } = useThemeStore.getState();
      if (mode === 'system') {
        const resolvedTheme = getSystemTheme();
        applyTheme(resolvedTheme);
        useThemeStore.setState({ resolvedTheme });
      }
    });
}
