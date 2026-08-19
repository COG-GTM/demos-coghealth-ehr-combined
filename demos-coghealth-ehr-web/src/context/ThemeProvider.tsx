import { useCallback, useEffect, useMemo, useState, useSyncExternalStore, type ReactNode } from 'react';
import { THEME_STORAGE_KEY, ThemeContext, type Theme } from './theme';

const darkQuery = () => window.matchMedia('(prefers-color-scheme: dark)');

function subscribeToSystemTheme(onChange: () => void) {
  const mq = darkQuery();
  mq.addEventListener('change', onChange);
  return () => mq.removeEventListener('change', onChange);
}

function readStoredTheme(): string | null {
  try {
    return localStorage.getItem(THEME_STORAGE_KEY);
  } catch {
    return null;
  }
}

function getInitialTheme(): Theme {
  const stored = readStoredTheme();
  return stored === 'light' || stored === 'dark' || stored === 'system' ? stored : 'system';
}

export function ThemeProvider({ children }: { children: ReactNode }) {
  const [theme, setThemeState] = useState<Theme>(getInitialTheme);
  const systemPrefersDark = useSyncExternalStore(
    subscribeToSystemTheme,
    () => darkQuery().matches,
    () => false,
  );

  const resolvedTheme = theme === 'system' ? (systemPrefersDark ? 'dark' : 'light') : theme;

  useEffect(() => {
    document.documentElement.classList.toggle('dark', resolvedTheme === 'dark');
  }, [resolvedTheme]);

  const setTheme = useCallback((next: Theme) => {
    try {
      localStorage.setItem(THEME_STORAGE_KEY, next);
    } catch {
      // Storage may be blocked; the theme still applies for this session.
    }
    setThemeState(next);
  }, []);

  const value = useMemo(() => ({ theme, resolvedTheme, setTheme }), [theme, resolvedTheme, setTheme]);

  return <ThemeContext.Provider value={value}>{children}</ThemeContext.Provider>;
}
