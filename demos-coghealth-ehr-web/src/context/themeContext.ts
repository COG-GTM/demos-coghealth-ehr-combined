import { createContext, useContext } from 'react';

export type Theme = 'light' | 'dark' | 'system';
export type ResolvedTheme = 'light' | 'dark';

export interface ThemeContextValue {
  theme: Theme;
  resolvedTheme: ResolvedTheme;
  setTheme: (theme: Theme) => void;
}

export const THEME_STORAGE_KEY = 'coghealth-theme';
export const THEMES: Theme[] = ['light', 'dark', 'system'];

export const ThemeContext = createContext<ThemeContextValue | null>(null);

export function readStoredTheme(): Theme {
  try {
    const stored = localStorage.getItem(THEME_STORAGE_KEY);
    if (stored && (THEMES as string[]).includes(stored)) return stored as Theme;
  } catch {
    // Storage may be blocked (privacy mode); fall back to system preference.
  }
  return 'system';
}

export function writeStoredTheme(theme: Theme): void {
  try {
    localStorage.setItem(THEME_STORAGE_KEY, theme);
  } catch {
    // Non-fatal: theme still applies for this page load.
  }
}

export function useTheme(): ThemeContextValue {
  const ctx = useContext(ThemeContext);
  if (!ctx) throw new Error('useTheme must be used within ThemeProvider');
  return ctx;
}
