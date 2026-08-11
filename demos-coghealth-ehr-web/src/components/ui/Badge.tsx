import type { ReactNode } from 'react';
import { tone } from '../../styles/tone';

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
    default: tone('neutral'),
    success: tone('success'),
    warning: tone('warning'),
    danger: tone('danger'),
    info: tone('info'),
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
