import { useEffect, useState, useSyncExternalStore, type ReactNode } from 'react';
import { ThemeContext, THEME_STORAGE_KEY, type Theme, type ResolvedTheme } from './themeContext';

const DARK_QUERY = '(prefers-color-scheme: dark)';

function getInitialTheme(): Theme {
  try {
    const stored = localStorage.getItem(THEME_STORAGE_KEY);
    if (stored === 'light' || stored === 'dark' || stored === 'system') return stored;
  } catch {
    // storage blocked; fall back to system
  }
  return 'system';
}

function subscribeToSystemTheme(onChange: () => void) {
  const mq = window.matchMedia(DARK_QUERY);
  mq.addEventListener('change', onChange);
  return () => mq.removeEventListener('change', onChange);
}

const getSystemPrefersDark = () => window.matchMedia(DARK_QUERY).matches;

export function ThemeProvider({ children }: { children: ReactNode }) {
  const [theme, setThemeState] = useState<Theme>(getInitialTheme);
  const systemPrefersDark = useSyncExternalStore(subscribeToSystemTheme, getSystemPrefersDark, () => false);
  const resolvedTheme: ResolvedTheme = theme === 'system' ? (systemPrefersDark ? 'dark' : 'light') : theme;

  useEffect(() => {
    document.documentElement.classList.toggle('dark', resolvedTheme === 'dark');
  }, [resolvedTheme]);

  const setTheme = (next: Theme) => {
    try {
      localStorage.setItem(THEME_STORAGE_KEY, next);
    } catch {
      // storage blocked; theme still applies for this session
    }
    setThemeState(next);
  };

  return (
    <ThemeContext.Provider value={{ theme, resolvedTheme, setTheme }}>
      {children}
    </ThemeContext.Provider>
  );
}
