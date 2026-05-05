import { Sun, Moon, Monitor } from 'lucide-react';
import { useTheme } from '../../context/themeStore';
import type { ThemeMode } from '../../context/themeStore';

const NEXT_THEME: Record<ThemeMode, ThemeMode> = {
  light: 'dark',
  dark: 'system',
  system: 'light',
};

const LABEL: Record<ThemeMode, string> = {
  light: 'Light theme (click for dark)',
  dark: 'Dark theme (click for system)',
  system: 'System theme (click for light)',
};

export function ThemeToggle() {
  const { theme, setTheme } = useTheme();

  const Icon = theme === 'light' ? Sun : theme === 'dark' ? Moon : Monitor;

  return (
    <button
      type="button"
      onClick={() => setTheme(NEXT_THEME[theme])}
      title={LABEL[theme]}
      aria-label={LABEL[theme]}
      className="ehr-toolbar-button flex items-center text-blue-100 hover:text-white"
    >
      <Icon className="w-3 h-3" />
    </button>
  );
}
