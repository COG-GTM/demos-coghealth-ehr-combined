import { NavLink } from 'react-router-dom';
import {
  Calendar,
  Users,
  MessageSquare,
  CheckSquare,
  BarChart3,
  Settings,
  X,
} from 'lucide-react';
import { cn } from '../../utils';

interface SidebarProps {
  isOpen: boolean;
  onClose: () => void;
}

const navItems = [
  { to: '/schedule', icon: Calendar, label: 'Schedule' },
  { to: '/patients', icon: Users, label: 'Patients' },
  { to: '/inbox/messages', icon: MessageSquare, label: 'Messages' },
  { to: '/inbox/tasks', icon: CheckSquare, label: 'Tasks' },
  { to: '/reports', icon: BarChart3, label: 'Reports' },
];

export function Sidebar({ isOpen, onClose }: SidebarProps) {
  return (
    <>
      {isOpen && (
        <div
          className="fixed inset-0 bg-black/50 z-40 lg:hidden"
          onClick={onClose}
        />
      )}

      <aside
        className={cn(
          'fixed lg:static inset-y-0 left-0 z-50 w-64 bg-white border-r border-slate-200 transform transition-transform duration-200 ease-in-out lg:transform-none',
          isOpen ? 'translate-x-0' : '-translate-x-full lg:translate-x-0'
        )}
      >
        <div className="flex flex-col h-full">
          <div className="flex items-center justify-between p-4 lg:hidden">
            <span className="font-semibold text-primary-600">MedChart EHR</span>
            <button
              onClick={onClose}
              className="p-2 text-slate-500 hover:text-slate-700 hover:bg-slate-100 rounded-md"
            >
              <X className="w-5 h-5" />
            </button>
          </div>

          <nav className="flex-1 p-4 space-y-1">
            {navItems.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                onClick={onClose}
                className={({ isActive }) =>
                  cn('sidebar-link', isActive && 'active')
                }
              >
                <item.icon className="w-5 h-5" />
                {item.label}
              </NavLink>
            ))}
          </nav>

          <div className="p-4 border-t">
            <NavLink
              to="/settings"
              onClick={onClose}
              className={({ isActive }) =>
                cn('sidebar-link', isActive && 'active')
              }
            >
              <Settings className="w-5 h-5" />
              Settings
            </NavLink>
          </div>
        </div>
      </aside>
    </>
  );
}
