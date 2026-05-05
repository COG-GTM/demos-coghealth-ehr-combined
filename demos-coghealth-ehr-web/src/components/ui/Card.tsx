import type { ReactNode } from 'react';

interface CardProps {
  children: ReactNode;
  className?: string;
  padding?: 'none' | 'sm' | 'md' | 'lg';
}

export default function Card({ children, className = '', padding = 'md' }: CardProps) {
  const paddings = {
    none: '',
    sm: 'p-2',
    md: 'p-3',
    lg: 'p-4',
  };

  return (
    <div className={`ehr-panel ${paddings[padding]} ${className}`}>
      {children}
    </div>
  );
}

interface CardHeaderProps {
  title: string;
  subtitle?: string;
  action?: ReactNode;
}

export function CardHeader({ title, subtitle, action }: CardHeaderProps) {
  return (
    <div className="ehr-header flex items-center justify-between mb-2">
      <div>
        <span className="text-[11px] font-semibold">{title}</span>
        {subtitle && <span className="text-[10px] ml-2 opacity-80">{subtitle}</span>}
      </div>
      {action && <div>{action}</div>}
    </div>
  );
}
