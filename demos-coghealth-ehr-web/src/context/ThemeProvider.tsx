import { useCallback, useEffect, useState, useSyncExternalStore, type ReactNode } from 'react';
import {
  ThemeContext,
  THEME_STORAGE_KEY,
  type ResolvedTheme,
  type Theme,
} from './themeContext';

const darkQuery = () => window.matchMedia('(prefers-color-scheme: dark)');

function subscribeToSystemTheme(onChange: () => void) {
  const mq = darkQuery();
  mq.addEventListener('change', onChange);
  return () => mq.removeEventListener('change', onChange);
}

function getInitialTheme(): Theme {
  const stored = localStorage.getItem(THEME_STORAGE_KEY) as Theme | null;
  if (stored === 'light' || stored === 'dark' || stored === 'system') return stored;
  return 'system';
}

export function ThemeProvider({ children }: { children: ReactNode }) {
  const [theme, setThemeState] = useState<Theme>(getInitialTheme);
  const systemDark = useSyncExternalStore(
    subscribeToSystemTheme,
    () => darkQuery().matches,
    () => false,
  );

  const resolvedTheme: ResolvedTheme =
    theme === 'system' ? (systemDark ? 'dark' : 'light') : theme;

  useEffect(() => {
    document.documentElement.classList.toggle('dark', resolvedTheme === 'dark');
  }, [resolvedTheme]);

  const setTheme = useCallback((next: Theme) => {
    localStorage.setItem(THEME_STORAGE_KEY, next);
    setThemeState(next);
  }, []);

  return (
    <ThemeContext.Provider value={{ theme, resolvedTheme, setTheme }}>
      {children}
    </ThemeContext.Provider>
  );
}
