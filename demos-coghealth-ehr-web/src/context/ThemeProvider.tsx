import { useCallback, useEffect, useMemo, useState, type ReactNode } from 'react';
import {
  applyTheme,
  nextTheme,
  readStoredTheme,
  resolveTheme,
  storeTheme,
  type Theme,
} from './theme';
import { ThemeContext } from './useTheme';

const DARK_QUERY = '(prefers-color-scheme: dark)';

function prefersDark(): boolean {
  if (typeof window === 'undefined') return false;
  return window.matchMedia(DARK_QUERY).matches;
}

export function ThemeProvider({ children }: { children: ReactNode }) {
  const [theme, setThemeState] = useState<Theme>(() =>
    readStoredTheme(typeof window === 'undefined' ? undefined : window.localStorage)
  );
  const [systemDark, setSystemDark] = useState<boolean>(prefersDark);

  const resolvedTheme = resolveTheme(theme, systemDark);

  useEffect(() => {
    applyTheme(document.documentElement, resolvedTheme);
  }, [resolvedTheme]);

  useEffect(() => {
    const mq = window.matchMedia(DARK_QUERY);
    const handler = (event: MediaQueryListEvent) => setSystemDark(event.matches);
    mq.addEventListener('change', handler);
    return () => mq.removeEventListener('change', handler);
  }, []);

  const setTheme = useCallback((next: Theme) => {
    storeTheme(window.localStorage, next);
    setThemeState(next);
  }, []);

  const cycleTheme = useCallback(() => setTheme(nextTheme(theme)), [setTheme, theme]);

  const value = useMemo(
    () => ({ theme, resolvedTheme, setTheme, cycleTheme }),
    [theme, resolvedTheme, setTheme, cycleTheme]
  );

  return <ThemeContext.Provider value={value}>{children}</ThemeContext.Provider>;
}
