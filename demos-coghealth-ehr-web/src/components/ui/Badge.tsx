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
  const variants = {
    default: { background: 'var(--ehr-neutral-bg)', border: '1px solid var(--ehr-border)', color: 'var(--ehr-text)' },
    success: { background: 'var(--ehr-success-bg)', border: '1px solid var(--ehr-success-border)', color: 'var(--ehr-success-fg)' },
    warning: { background: 'var(--ehr-warning-bg)', border: '1px solid var(--ehr-warning-border)', color: 'var(--ehr-warning-fg)' },
    danger: { background: 'var(--ehr-critical-bg)', border: '1px solid var(--ehr-critical-border)', color: 'var(--ehr-critical-fg)' },
    info: { background: 'var(--ehr-info-bg)', border: '1px solid var(--ehr-info-border)', color: 'var(--ehr-info-fg)' },
  };

  return (
    <span 
      className={`inline-flex items-center text-[10px] px-1.5 py-0.5 font-medium ${className}`}
      style={variants[variant]}
    >
      {children}
    </span>
  );
}
