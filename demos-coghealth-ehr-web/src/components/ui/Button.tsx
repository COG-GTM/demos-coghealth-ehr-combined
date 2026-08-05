import { type ButtonHTMLAttributes, forwardRef } from 'react';
import { Loader2 } from 'lucide-react';

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'danger' | 'ghost';
  size?: 'sm' | 'md' | 'lg';
  loading?: boolean;
}

const Button = forwardRef<HTMLButtonElement, ButtonProps>(
  ({ className = '', variant = 'primary', loading, children, disabled, ...props }, ref) => {
    const variantClass = {
      primary: 'ehr-button-primary',
      danger: 'ehr-button-danger',
      ghost: 'ehr-button-ghost',
      secondary: '',
    }[variant];

    return (
      <button
        ref={ref}
        className={`ehr-button ${variantClass} ${className}`}
        disabled={disabled || loading}
        {...props}
      >
        {loading && <Loader2 className="w-3 h-3 mr-1 animate-spin inline" />}
        {children}
      </button>
    );
  }
);

Button.displayName = 'Button';

export default Button;
