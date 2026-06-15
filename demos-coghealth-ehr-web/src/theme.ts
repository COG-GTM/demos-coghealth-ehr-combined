import { createContext, useContext } from 'react';

export type Theme = 'light' | 'dark' | 'system';

export const THEME_STORAGE_KEY = 'coghealth_settings';

export function readStoredTheme(): Theme {
  try {
    const raw = localStorage.getItem(THEME_STORAGE_KEY);
    if (raw) {
      const data = JSON.parse(raw);
      const theme = data?.appearance?.theme;
      if (theme === 'light' || theme === 'dark' || theme === 'system') {
        return theme;
      }
    }
  } catch {
    // ignore malformed settings
  }
  return 'light';
}

export function persistTheme(theme: Theme) {
  try {
    const raw = localStorage.getItem(THEME_STORAGE_KEY);
    const data = raw ? JSON.parse(raw) : {};
    data.appearance = { ...(data.appearance ?? {}), theme };
    localStorage.setItem(THEME_STORAGE_KEY, JSON.stringify(data));
  } catch {
    // ignore storage errors
  }
}

function prefersDark(): boolean {
  return window.matchMedia?.('(prefers-color-scheme: dark)').matches ?? false;
}

export function resolveTheme(theme: Theme): 'light' | 'dark' {
  if (theme === 'system') {
    return prefersDark() ? 'dark' : 'light';
  }
  return theme;
}

export function applyTheme(theme: Theme) {
  const resolved = resolveTheme(theme);
  document.documentElement.classList.toggle('dark', resolved === 'dark');
}

export interface ThemeContextValue {
  theme: Theme;
  resolvedTheme: 'light' | 'dark';
  setTheme: (theme: Theme) => void;
  toggleTheme: () => void;
}

export const ThemeContext = createContext<ThemeContextValue | undefined>(undefined);

export function useTheme(): ThemeContextValue {
  const ctx = useContext(ThemeContext);
  if (!ctx) {
    throw new Error('useTheme must be used within a ThemeProvider');
  }
  return ctx;
}
