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
    default: { background: '#e8e8e8', border: '1px solid #999', color: '#333' },
    success: { background: '#d4edda', border: '1px solid #28a745', color: '#155724' },
    warning: { background: 'var(--ehr-abnormal-bg)', border: '1px solid var(--ehr-abnormal-border)', color: 'var(--ehr-abnormal-fg)' },
    danger: { background: 'var(--ehr-critical-bg)', border: '1px solid var(--ehr-critical-border)', color: 'var(--ehr-critical-fg)' },
    info: { background: '#cce5ff', border: '1px solid #0066cc', color: '#004085' },
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
