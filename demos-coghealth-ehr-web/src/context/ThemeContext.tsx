import { useCallback, useEffect, useState, useSyncExternalStore } from 'react';
import type { ReactNode } from 'react';
import { ThemeContext } from './themeStore';
import type { ThemeMode, ResolvedTheme } from './themeStore';

const STORAGE_KEY = 'coghealth_theme';

function readStoredTheme(): ThemeMode {
  if (typeof window === 'undefined') return 'system';
  try {
    const stored = window.localStorage.getItem(STORAGE_KEY);
    if (stored === 'light' || stored === 'dark' || stored === 'system') {
      return stored;
    }
  } catch {
    // ignore storage errors (e.g. SSR, blocked localStorage)
  }
  return 'system';
}

function subscribeMedia(cb: () => void): () => void {
  if (typeof window === 'undefined' || typeof window.matchMedia !== 'function') {
    return () => {};
  }
  const mql = window.matchMedia('(prefers-color-scheme: dark)');
  mql.addEventListener('change', cb);
  return () => mql.removeEventListener('change', cb);
}

function getSystemSnapshot(): ResolvedTheme {
  if (typeof window === 'undefined' || typeof window.matchMedia !== 'function') {
    return 'light';
  }
  return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
}

function getServerSnapshot(): ResolvedTheme {
  return 'light';
}

interface ThemeProviderProps {
  children: ReactNode;
}

export function ThemeProvider({ children }: ThemeProviderProps) {
  const [theme, setThemeState] = useState<ThemeMode>(() => readStoredTheme());
  const systemTheme = useSyncExternalStore(subscribeMedia, getSystemSnapshot, getServerSnapshot);
  const resolvedTheme: ResolvedTheme = theme === 'system' ? systemTheme : theme;

  useEffect(() => {
    document.documentElement.setAttribute('data-theme', resolvedTheme);
    document.documentElement.style.colorScheme = resolvedTheme;
  }, [resolvedTheme]);

  const setTheme = useCallback((next: ThemeMode) => {
    setThemeState(next);
    try {
      window.localStorage.setItem(STORAGE_KEY, next);
    } catch {
      // ignore storage errors
    }
  }, []);

  return (
    <ThemeContext.Provider value={{ theme, resolvedTheme, setTheme }}>
      {children}
    </ThemeContext.Provider>
  );
}
