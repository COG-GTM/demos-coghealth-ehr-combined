import { NavLink, useParams } from 'react-router-dom';
import {
  LayoutDashboard,
  Calendar,
  ClipboardList,
  Pill,
  AlertTriangle,
  Activity,
  FileText,
  ShoppingCart,
  FlaskConical,
  Syringe,
  FolderOpen,
  History,
  Users,
  CreditCard,
  Plus,
  MessageSquare,
} from 'lucide-react';
import { cn } from '../../utils';

const chartNavItems = [
  { to: '', icon: LayoutDashboard, label: 'Summary', end: true },
  { to: 'encounters', icon: Calendar, label: 'Encounters' },
  { to: 'problems', icon: ClipboardList, label: 'Problems' },
  { to: 'medications', icon: Pill, label: 'Medications' },
  { to: 'allergies', icon: AlertTriangle, label: 'Allergies' },
  { to: 'vitals', icon: Activity, label: 'Vitals' },
  { to: 'notes', icon: FileText, label: 'Notes' },
  { to: 'orders', icon: ShoppingCart, label: 'Orders' },
  { to: 'results', icon: FlaskConical, label: 'Results' },
  { to: 'immunizations', icon: Syringe, label: 'Immunizations' },
  { to: 'documents', icon: FolderOpen, label: 'Documents' },
  { to: 'history', icon: History, label: 'History' },
  { to: 'care-team', icon: Users, label: 'Care Team' },
  { to: 'insurance', icon: CreditCard, label: 'Insurance' },
];

export function ChartSidebar() {
  const { patientId } = useParams();
  const basePath = `/patients/${patientId}`;

  return (
    <aside className="w-56 bg-white border-r border-slate-200 flex flex-col h-full">
      <div className="p-3 border-b">
        <h3 className="text-xs font-semibold text-slate-500 uppercase tracking-wider mb-2">
          Chart
        </h3>
        <nav className="space-y-0.5">
          {chartNavItems.map((item) => (
            <NavLink
              key={item.to}
              to={`${basePath}/${item.to}`}
              end={item.end}
              className={({ isActive }) =>
                cn(
                  'flex items-center gap-2 px-2 py-1.5 text-sm rounded-md transition-colors',
                  isActive
                    ? 'bg-primary-50 text-primary-700 font-medium'
                    : 'text-slate-600 hover:bg-slate-100 hover:text-slate-900'
                )
              }
            >
              <item.icon className="w-4 h-4" />
              {item.label}
            </NavLink>
          ))}
        </nav>
      </div>

      <div className="p-3 border-t mt-auto">
        <h3 className="text-xs font-semibold text-slate-500 uppercase tracking-wider mb-2">
          Quick Actions
        </h3>
        <div className="space-y-1">
          <NavLink
            to={`${basePath}/notes/new`}
            className="flex items-center gap-2 px-2 py-1.5 text-sm text-slate-600 hover:bg-slate-100 hover:text-slate-900 rounded-md"
          >
            <Plus className="w-4 h-4" />
            New Note
          </NavLink>
          <NavLink
            to={`${basePath}/orders/new`}
            className="flex items-center gap-2 px-2 py-1.5 text-sm text-slate-600 hover:bg-slate-100 hover:text-slate-900 rounded-md"
          >
            <Plus className="w-4 h-4" />
            New Order
          </NavLink>
          <NavLink
            to={`${basePath}/message`}
            className="flex items-center gap-2 px-2 py-1.5 text-sm text-slate-600 hover:bg-slate-100 hover:text-slate-900 rounded-md"
          >
            <MessageSquare className="w-4 h-4" />
            Message
          </NavLink>
        </div>
      </div>
    </aside>
  );
}
