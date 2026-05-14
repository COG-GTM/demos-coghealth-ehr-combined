import { create } from 'zustand';

type Theme = 'light' | 'dark' | 'system';

interface ThemeState {
  theme: Theme;
  setTheme: (theme: Theme) => void;
}

function applyTheme(theme: Theme) {
  const root = document.documentElement;
  const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
  const isDark = theme === 'dark' || (theme === 'system' && prefersDark);

  if (isDark) {
    root.classList.add('dark');
  } else {
    root.classList.remove('dark');
  }
}

function getStoredTheme(): Theme {
  try {
    const stored = localStorage.getItem('coghealth_settings');
    if (stored) {
      const data = JSON.parse(stored);
      if (data.appearance?.theme) return data.appearance.theme as Theme;
    }
  } catch {
    // ignore
  }
  return 'light';
}

export const useThemeStore = create<ThemeState>((set) => {
  const initial = getStoredTheme();
  applyTheme(initial);

  window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', () => {
    const current = useThemeStore.getState().theme;
    if (current === 'system') applyTheme('system');
  });

  return {
    theme: initial,
    setTheme: (theme: Theme) => {
      applyTheme(theme);
      set({ theme });
    },
  };
});
