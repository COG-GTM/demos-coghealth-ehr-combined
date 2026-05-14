import { Moon, Sun, Monitor } from 'lucide-react';
import { useTheme } from '../../context/ThemeContext';

export default function ThemeToggle() {
  const { theme, setTheme } = useTheme();

  const cycle = () => {
    const next = theme === 'light' ? 'dark' : theme === 'dark' ? 'system' : 'light';
    setTheme(next);
  };

  return (
    <button
      onClick={cycle}
      className="flex items-center space-x-1 hover:text-white text-blue-200"
      title={`Theme: ${theme}`}
    >
      {theme === 'light' && <Sun className="w-3 h-3" />}
      {theme === 'dark' && <Moon className="w-3 h-3" />}
      {theme === 'system' && <Monitor className="w-3 h-3" />}
      <span className="text-[10px]">{theme === 'system' ? 'Auto' : theme === 'dark' ? 'Dark' : 'Light'}</span>
    </button>
  );
}
