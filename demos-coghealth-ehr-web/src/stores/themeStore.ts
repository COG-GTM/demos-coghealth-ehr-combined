import { create } from 'zustand';

export type ThemePreference = 'light' | 'dark' | 'system';

const STORAGE_KEY = 'coghealth_theme';

const getStoredPreference = (): ThemePreference => {
  if (typeof localStorage === 'undefined') return 'system';
  const stored = localStorage.getItem(STORAGE_KEY);
  if (stored === 'light' || stored === 'dark' || stored === 'system') {
    return stored;
  }
  return 'system';
};

const prefersDark = (): boolean =>
  typeof window !== 'undefined' &&
  typeof window.matchMedia === 'function' &&
  window.matchMedia('(prefers-color-scheme: dark)').matches;

export const resolveTheme = (preference: ThemePreference): 'light' | 'dark' =>
  preference === 'system' ? (prefersDark() ? 'dark' : 'light') : preference;

const applyThemeClass = (preference: ThemePreference) => {
  if (typeof document === 'undefined') return;
  const isDark = resolveTheme(preference) === 'dark';
  document.documentElement.classList.toggle('dark', isDark);
  document.documentElement.style.colorScheme = isDark ? 'dark' : 'light';
};

interface ThemeState {
  preference: ThemePreference;
  resolved: 'light' | 'dark';
  setPreference: (preference: ThemePreference) => void;
  toggle: () => void;
}

export const useThemeStore = create<ThemeState>((set, get) => ({
  preference: getStoredPreference(),
  resolved: resolveTheme(getStoredPreference()),
  setPreference: (preference) => {
    if (typeof localStorage !== 'undefined') {
      localStorage.setItem(STORAGE_KEY, preference);
    }
    applyThemeClass(preference);
    set({ preference, resolved: resolveTheme(preference) });
  },
  toggle: () => {
    const next = get().resolved === 'dark' ? 'light' : 'dark';
    get().setPreference(next);
  },
}));

/**
 * Applies the persisted theme to the document and keeps it in sync with the OS
 * appearance while the preference is set to "system". Call once at app start.
 */
export const initTheme = () => {
  const preference = getStoredPreference();
  applyThemeClass(preference);
  useThemeStore.setState({ preference, resolved: resolveTheme(preference) });

  if (typeof window !== 'undefined' && typeof window.matchMedia === 'function') {
    const media = window.matchMedia('(prefers-color-scheme: dark)');
    media.addEventListener('change', () => {
      if (useThemeStore.getState().preference === 'system') {
        applyThemeClass('system');
        useThemeStore.setState({ resolved: resolveTheme('system') });
      }
    });
  }
};
