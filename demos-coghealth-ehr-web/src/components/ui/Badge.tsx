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
    default: { background: 'var(--ehr-raised)', border: '1px solid var(--color-gray-500)', color: 'var(--color-gray-800)' },
    success: { background: 'var(--ehr-success-bg)', border: '1px solid #28a745', color: 'var(--ehr-success-fg)' },
    warning: { background: 'var(--ehr-warning-bg)', border: '1px solid #cc9900', color: 'var(--ehr-warning-fg)' },
    danger: { background: 'var(--ehr-error-bg)', border: '1px solid #cc0000', color: 'var(--ehr-error-fg)' },
    info: { background: 'var(--ehr-info-bg)', border: '1px solid #0066cc', color: 'var(--ehr-info-fg)' },
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
