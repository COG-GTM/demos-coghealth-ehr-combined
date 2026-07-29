import type { ReactNode } from 'react';

interface BadgeProps {
  children: ReactNode;
  variant?: 'default' | 'success' | 'warning' | 'danger' | 'info';
  size?: 'sm' | 'md';
  className?: string;
}

export default function Badge({ 
  children, 
  variant = 'default', 
  className = '' 
}: BadgeProps) {
  return (
    <span 
      className={`ehr-badge ehr-badge-${variant} inline-flex items-center text-[10px] px-1.5 py-0.5 font-medium ${className}`}
    >
      {children}
    </span>
  );
}
