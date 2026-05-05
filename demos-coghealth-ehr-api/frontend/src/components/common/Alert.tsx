import { AlertCircle, AlertTriangle, CheckCircle, Info, X } from 'lucide-react';
import { cn } from '../../utils';

interface AlertProps {
  variant?: 'success' | 'warning' | 'danger' | 'info';
  title?: string;
  children: React.ReactNode;
  onClose?: () => void;
  className?: string;
}

export function Alert({ variant = 'info', title, children, onClose, className }: AlertProps) {
  const variants = {
    success: 'alert-success',
    warning: 'alert-warning',
    danger: 'alert-danger',
    info: 'alert-info',
  };

  const icons = {
    success: CheckCircle,
    warning: AlertTriangle,
    danger: AlertCircle,
    info: Info,
  };

  const Icon = icons[variant];

  return (
    <div className={cn('alert', variants[variant], className)}>
      <Icon className="w-5 h-5 flex-shrink-0" />
      <div className="flex-1">
        {title && <div className="font-medium mb-1">{title}</div>}
        <div>{children}</div>
      </div>
      {onClose && (
        <button onClick={onClose} className="p-1 hover:bg-black/5 rounded">
          <X className="w-4 h-4" />
        </button>
      )}
    </div>
  );
}
