import { Moon, Sun } from 'lucide-react';
import { useTheme } from '../../context/useTheme';

interface ThemeToggleProps {
  className?: string;
}

export default function ThemeToggle({ className = '' }: ThemeToggleProps) {
  const { resolvedTheme, toggleTheme } = useTheme();
  const isDark = resolvedTheme === 'dark';
  const label = isDark ? 'Switch to light mode' : 'Switch to dark mode';

  return (
    <button
      type="button"
      onClick={toggleTheme}
      title={label}
      aria-label={label}
      className={`flex items-center justify-center hover:text-white text-blue-200 ${className}`}
    >
      {isDark ? <Sun className="w-3 h-3" /> : <Moon className="w-3 h-3" />}
    </button>
  );
}
