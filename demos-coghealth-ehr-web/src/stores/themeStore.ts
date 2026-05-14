import { create } from 'zustand';

type Theme = 'light' | 'dark' | 'system';

interface ThemeState {
  theme: Theme;
  setTheme: (theme: Theme) => void;
  resolvedTheme: 'light' | 'dark';
}

function getSystemTheme(): 'light' | 'dark' {
  return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
}

function resolve(theme: Theme): 'light' | 'dark' {
  return theme === 'system' ? getSystemTheme() : theme;
}

function applyTheme(resolved: 'light' | 'dark') {
  document.documentElement.classList.toggle('dark', resolved === 'dark');
}

function loadTheme(): Theme {
  try {
    const raw = localStorage.getItem('coghealth_settings');
    if (raw) {
      const data = JSON.parse(raw);
      const t = data?.appearance?.theme;
      if (t === 'light' || t === 'dark' || t === 'system') return t;
    }
  } catch { /* ignore */ }
  return 'light';
}

function persistTheme(theme: Theme) {
  try {
    const raw = localStorage.getItem('coghealth_settings');
    const data = raw ? JSON.parse(raw) : {};
    data.appearance = { ...data.appearance, theme };
    localStorage.setItem('coghealth_settings', JSON.stringify(data));
  } catch { /* ignore */ }
}

const initial = loadTheme();
const initialResolved = resolve(initial);
applyTheme(initialResolved);

export const useThemeStore = create<ThemeState>((set) => ({
  theme: initial,
  resolvedTheme: initialResolved,
  setTheme: (theme) => {
    const resolved = resolve(theme);
    applyTheme(resolved);
    persistTheme(theme);
    set({ theme, resolvedTheme: resolved });
  },
}));

if (typeof window !== 'undefined') {
  window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', () => {
    const state = useThemeStore.getState();
    if (state.theme === 'system') {
      const resolved = getSystemTheme();
      applyTheme(resolved);
      useThemeStore.setState({ resolvedTheme: resolved });
    }
  });
}
