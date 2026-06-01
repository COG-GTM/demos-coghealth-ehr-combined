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
    default: { background: 'var(--ehr-elevated)', border: '1px solid var(--ehr-border-strong, #999)', color: 'var(--ehr-text, #333)' },
    success: { background: '#d4edda', border: '1px solid #28a745', color: '#155724' },
    warning: { background: '#fff3cd', border: '1px solid #cc9900', color: '#664d00' },
    danger: { background: '#ffcccc', border: '1px solid #cc0000', color: '#990000' },
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
