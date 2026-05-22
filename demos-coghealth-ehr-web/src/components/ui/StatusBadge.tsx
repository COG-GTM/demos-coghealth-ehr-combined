import type { ReactNode } from 'react';

interface StatusBadgeProps {
  children: ReactNode;
  status: 'active' | 'inactive' | 'deceased' | 'planned' | 'arrived' | 'triaged' | 'in-progress' | 'on-hold' | 'finished' | 'cancelled' | 'critical' | 'waiting' | 'roomed' | 'ready-discharge' | 'final' | 'preliminary' | 'pending';
  size?: 'sm' | 'md';
  className?: string;
}

const statusConfig: Record<string, { label?: string; bg: string; color: string; border: string; fontWeight?: string }> = {
  'active': { bg: '#d4edda', color: '#155724', border: '1px solid #28a745' },
  'inactive': { bg: '#e8e8e8', color: '#333', border: '1px solid #999' },
  'deceased': { bg: '#e8e8e8', color: '#333', border: '1px solid #999' },
  'planned': { bg: '#e8e8e8', color: '#666', border: '1px solid #999' },
  'arrived': { bg: '#e8e8e8', color: '#666', border: '1px solid #999' },
  'triaged': { bg: '#e8e8e8', color: '#666', border: '1px solid #999' },
  'roomed': { bg: '#e8e8e8', color: '#666', border: '1px solid #999' },
  'in-progress': { bg: '#e8e8e8', color: '#333', border: '1px solid #999' },
  'on-hold': { bg: '#fff3cd', color: '#664d00', border: '1px solid #cc9900' },
  'finished': { bg: '#e8e8e8', color: '#666', border: '1px solid #999' },
  'cancelled': { bg: '#e8e8e8', color: '#666', border: '1px solid #999' },
  'critical': { bg: '#fff3cd', color: '#664d00', border: '1px solid #cc9900', fontWeight: 'bold' },
  'waiting': { bg: '#e8e8e8', color: '#666', border: '1px solid #999' },
  'ready-discharge': { bg: '#e8e8e8', color: '#666', border: '1px solid #999' },
  'final': { bg: '#d4edda', color: '#155724', border: '1px solid #28a745' },
  'preliminary': { bg: '#fff3cd', color: '#664d00', border: '1px solid #cc9900' },
  'pending': { bg: '#e8e8e8', color: '#666', border: '1px solid #999' },
};

export default function StatusBadge({ 
  children, 
  status, 
  size = 'sm',
  className = '' 
}: StatusBadgeProps) {
  const config = statusConfig[status] || statusConfig['pending'];
  
  const sizes = {
    sm: 'text-[9px] px-1 py-0.5',
    md: 'text-[10px] px-1.5 py-0.5',
  };

  return (
    <span 
      className={`inline-flex items-center font-medium ${sizes[size]} ${className}`}
      style={{ 
        background: config.bg, 
        color: config.color, 
        border: config.border,
        fontWeight: config.fontWeight || 'normal'
      }}
    >
      {children}
    </span>
  );
}
