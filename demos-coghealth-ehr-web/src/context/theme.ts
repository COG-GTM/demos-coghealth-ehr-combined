export type Theme = 'light' | 'dark' | 'system';
export type ResolvedTheme = 'light' | 'dark';

export const THEME_STORAGE_KEY = 'coghealth-theme';
export const THEMES: readonly Theme[] = ['light', 'dark', 'system'];

interface ThemeStorage {
  getItem(key: string): string | null;
  setItem(key: string, value: string): void;
}

interface ThemeTarget {
  classList: {
    add(token: string): void;
    remove(token: string): void;
  };
}

export function isTheme(value: string | null): value is Theme {
  return value !== null && (THEMES as readonly string[]).includes(value);
}

export function readStoredTheme(storage: ThemeStorage | undefined): Theme {
  if (!storage) return 'system';
  try {
    const stored = storage.getItem(THEME_STORAGE_KEY);
    return isTheme(stored) ? stored : 'system';
  } catch {
    return 'system';
  }
}

export function storeTheme(storage: ThemeStorage | undefined, theme: Theme): void {
  if (!storage) return;
  try {
    storage.setItem(THEME_STORAGE_KEY, theme);
  } catch {
    // storage unavailable (private mode, quota) — theme stays in memory only
  }
}

export function resolveTheme(theme: Theme, prefersDark: boolean): ResolvedTheme {
  if (theme === 'system') return prefersDark ? 'dark' : 'light';
  return theme;
}

export function applyTheme(target: ThemeTarget, resolved: ResolvedTheme): void {
  if (resolved === 'dark') {
    target.classList.add('dark');
  } else {
    target.classList.remove('dark');
  }
}

export function nextTheme(theme: Theme): Theme {
  return THEMES[(THEMES.indexOf(theme) + 1) % THEMES.length];
}
