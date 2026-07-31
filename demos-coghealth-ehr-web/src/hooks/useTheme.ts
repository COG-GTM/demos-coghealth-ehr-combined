import { useCallback, useSyncExternalStore } from 'react';

export type Theme = 'light' | 'dark' | 'system';

const STORAGE_KEY = 'coghealth_theme';

const prefersDark = () =>
  typeof window !== 'undefined' &&
  window.matchMedia('(prefers-color-scheme: dark)').matches;

export function getStoredTheme(): Theme {
  if (typeof window === 'undefined') return 'system';
  const stored = localStorage.getItem(STORAGE_KEY);
  return stored === 'light' || stored === 'dark' || stored === 'system'
    ? stored
    : 'system';
}

export function resolveTheme(theme: Theme): 'light' | 'dark' {
  if (theme === 'system') return prefersDark() ? 'dark' : 'light';
  return theme;
}

export function applyTheme(theme: Theme) {
  document.documentElement.classList.toggle('dark', resolveTheme(theme) === 'dark');
}

/**
 * Shared external store so every `useTheme` consumer stays in sync and so OS
 * color-scheme changes trigger a re-render. The snapshot encodes both the chosen
 * theme and the resolved theme, so `system` -> OS flip changes the snapshot even
 * though the chosen theme is unchanged.
 */
const listeners = new Set<() => void>();
let snapshot = '';

function computeSnapshot(): string {
  const theme = getStoredTheme();
  return `${theme}:${resolveTheme(theme)}`;
}

function emit() {
  const theme = getStoredTheme();
  applyTheme(theme);
  const next = computeSnapshot();
  if (next !== snapshot) {
    snapshot = next;
    listeners.forEach((listener) => listener());
  }
}

if (typeof window !== 'undefined') {
  snapshot = computeSnapshot();
  window
    .matchMedia('(prefers-color-scheme: dark)')
    .addEventListener('change', emit);
  window.addEventListener('storage', (e) => {
    if (e.key === STORAGE_KEY) emit();
  });
}

function subscribe(callback: () => void): () => void {
  listeners.add(callback);
  return () => listeners.delete(callback);
}

function getSnapshot(): string {
  return snapshot;
}

function getServerSnapshot(): string {
  return 'system:light';
}

export function setTheme(next: Theme) {
  localStorage.setItem(STORAGE_KEY, next);
  emit();
}

export function toggleTheme() {
  setTheme(resolveTheme(getStoredTheme()) === 'dark' ? 'light' : 'dark');
}

export function useTheme() {
  const snap = useSyncExternalStore(subscribe, getSnapshot, getServerSnapshot);
  const theme = snap.split(':')[0] as Theme;
  return {
    theme,
    resolvedTheme: resolveTheme(theme),
    setTheme: useCallback((next: Theme) => setTheme(next), []),
    toggleTheme: useCallback(() => toggleTheme(), []),
  };
}
