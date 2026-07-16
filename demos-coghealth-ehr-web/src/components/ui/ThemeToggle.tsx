import { useState } from 'react';
import { Moon, Sun } from 'lucide-react';
import { getInitialTheme, setTheme, type Theme } from '../../theme';

export function ThemeToggle() {
  const [theme, setThemeState] = useState<Theme>(getInitialTheme);

  const toggle = () => {
    const next: Theme = theme === 'dark' ? 'light' : 'dark';
    setTheme(next);
    setThemeState(next);
  };

  const isDark = theme === 'dark';

  return (
    <button
      onClick={toggle}
      title={isDark ? 'Switch to light mode' : 'Switch to dark mode'}
      aria-label={isDark ? 'Switch to light mode' : 'Switch to dark mode'}
      className="flex items-center space-x-1 hover:text-white text-blue-200"
    >
      {isDark ? <Sun className="w-3 h-3" /> : <Moon className="w-3 h-3" />}
      <span>{isDark ? 'Light' : 'Dark'}</span>
    </button>
  );
}
