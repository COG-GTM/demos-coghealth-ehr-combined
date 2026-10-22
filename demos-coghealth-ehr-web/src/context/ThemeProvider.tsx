import { useEffect, useState, type ReactNode } from 'react';
import { ThemeContext, type Theme } from './theme';

const STORAGE_KEY = 'coghealth-theme';
const DARK_QUERY = '(prefers-color-scheme: dark)';

function getInitialTheme(): Theme {
  let stored: string | null = null;
  try {
    stored = localStorage.getItem(STORAGE_KEY);
  } catch {
    // storage blocked; fall back to system preference
  }
  return stored === 'light' || stored === 'dark' || stored === 'system' ? stored : 'system';
}

export function ThemeProvider({ children }: { children: ReactNode }) {
  const [theme, setThemeState] = useState<Theme>(getInitialTheme);
  const [systemDark, setSystemDark] = useState(() => window.matchMedia(DARK_QUERY).matches);
  const resolvedTheme = theme === 'system' ? (systemDark ? 'dark' : 'light') : theme;

  useEffect(() => {
    const mq = window.matchMedia(DARK_QUERY);
    const handler = (e: MediaQueryListEvent) => setSystemDark(e.matches);
    mq.addEventListener('change', handler);
    return () => mq.removeEventListener('change', handler);
  }, []);

  useEffect(() => {
    const root = document.documentElement;
    root.classList.toggle('dark', resolvedTheme === 'dark');
    root.style.colorScheme = resolvedTheme;
  }, [resolvedTheme]);

  const setTheme = (next: Theme) => {
    try {
      localStorage.setItem(STORAGE_KEY, next);
    } catch {
      // storage blocked; keep choice in memory only
    }
    setThemeState(next);
  };

  return (
    <ThemeContext.Provider value={{ theme, resolvedTheme, setTheme }}>
      {children}
    </ThemeContext.Provider>
  );
}
