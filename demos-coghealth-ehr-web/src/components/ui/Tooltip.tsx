import { type ReactNode } from 'react';

interface TooltipProps {
  label: string;
  children: ReactNode;
}

const Tooltip = ({ label, children }: TooltipProps) => {
  return (
    <span className="relative inline-block group/tooltip">
      {children}
      <span
        role="tooltip"
        className="pointer-events-none absolute left-1/2 -translate-x-1/2 bottom-full mb-1 px-1.5 py-0.5 bg-gray-900 text-white text-[10px] whitespace-nowrap opacity-0 group-hover/tooltip:opacity-100 transition-opacity duration-100 z-50 shadow-md"
      >
        {label}
      </span>
    </span>
  );
};

export default Tooltip;
