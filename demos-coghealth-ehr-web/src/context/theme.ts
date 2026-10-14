export type Theme = 'light' | 'dark' | 'system';
export type ResolvedTheme = 'light' | 'dark';

export const THEME_STORAGE_KEY = 'coghealth-theme';
export const THEMES: readonly Theme[] = ['light', 'dark', 'system'];

export function isTheme(value: unknown): value is Theme {
  return typeof value === 'string' && (THEMES as readonly string[]).includes(value);
}

export function readStoredTheme(storage: Pick<Storage, 'getItem'> | undefined): Theme {
  try {
    const stored = storage?.getItem(THEME_STORAGE_KEY);
    return isTheme(stored) ? stored : 'system';
  } catch {
    return 'system';
  }
}

export function resolveTheme(theme: Theme, systemPrefersDark: boolean): ResolvedTheme {
  if (theme === 'system') return systemPrefersDark ? 'dark' : 'light';
  return theme;
}

export function toggledTheme(resolved: ResolvedTheme): Theme {
  return resolved === 'dark' ? 'light' : 'dark';
}

interface ThemeRoot {
  classList: Pick<DOMTokenList, 'toggle'>;
  style: { colorScheme: string };
}

export function applyResolvedTheme(root: ThemeRoot, resolved: ResolvedTheme): void {
  root.classList.toggle('dark', resolved === 'dark');
  root.style.colorScheme = resolved;
}
