import type { CSSProperties } from 'react';

export type Tone = 'neutral' | 'success' | 'warning' | 'danger' | 'info';

/**
 * Inline styles backed by the themed `--tone-*` custom properties, so status
 * colors follow light/dark mode without a re-render.
 */
export function tone(name: Tone): CSSProperties {
  return {
    background: `var(--tone-${name}-bg)`,
    color: `var(--tone-${name}-fg)`,
    border: `1px solid var(--tone-${name}-border)`,
  };
}

export function toneFill(name: Tone): CSSProperties {
  return {
    background: `var(--tone-${name}-bg)`,
    color: `var(--tone-${name}-fg)`,
  };
}
