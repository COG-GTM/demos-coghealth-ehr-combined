import type { ReactNode } from 'react';

interface FlagChipProps {
  children: ReactNode;
  flag: 'fall-risk' | 'allergy' | 'isolation' | 'dnr' | 'vip' | 'difficult-iv' | 'interpreter' | 'wheelchair' | 'new-patient' | string;
  size?: 'sm' | 'md';
  className?: string;
}

const flagConfig: Record<string, { label: string; bg: string; color: string }> = {
  'fall-risk': { label: 'FALL', bg: '#e8e8e8', color: '#333' },
  'allergy': { label: 'ALLERGY', bg: '#e8e8e8', color: '#333' },
  'isolation': { label: 'ISO', bg: '#e8e8e8', color: '#333' },
  'dnr': { label: 'DNR', bg: '#d8d8d8', color: '#333' },
  'vip': { label: 'VIP', bg: '#f0f0f0', color: '#666' },
  'difficult-iv': { label: 'DIFF IV', bg: '#f0f0f0', color: '#666' },
  'interpreter': { label: 'INTERP', bg: '#f0f0f0', color: '#666' },
  'wheelchair': { label: 'WC', bg: '#f0f0f0', color: '#666' },
  'new-patient': { label: 'NEW', bg: '#f0f0f0', color: '#666' },
};

export default function FlagChip({ 
  children, 
  flag, 
  size = 'sm',
  className = '' 
}: FlagChipProps) {
  const config = flagConfig[flag] || { label: flag.toUpperCase(), bg: '#f0f0f0', color: '#666' };
  
  const sizes = {
    sm: 'text-[9px] px-0.5 py-0',
    md: 'text-[10px] px-1 py-0.5',
  };

  return (
    <span 
      className={`inline-flex items-center font-medium ${sizes[size]} ${className}`}
      style={{ background: config.bg, color: config.color }}
    >
      {children}
    </span>
  );
}
