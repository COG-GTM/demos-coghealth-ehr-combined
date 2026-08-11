import { Moon, Sun } from 'lucide-react';
import { useTheme } from '../../context/theme';

export default function ThemeToggle() {
  const { resolvedTheme, setTheme } = useTheme();
  const nextTheme = resolvedTheme === 'dark' ? 'light' : 'dark';

  return (
    <button
      type="button"
      onClick={() => setTheme(nextTheme)}
      title={`Switch to ${nextTheme} mode`}
      aria-label={`Switch to ${nextTheme} mode`}
      className="flex items-center space-x-1 text-blue-200 hover:text-white"
    >
      {resolvedTheme === 'dark' ? <Sun className="w-3 h-3" /> : <Moon className="w-3 h-3" />}
      <span className="capitalize">{nextTheme}</span>
    </button>
  );
}
