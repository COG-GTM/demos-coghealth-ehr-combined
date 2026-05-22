export type ColorPaletteName = 'blue' | 'green' | 'purple' | 'orange' | 'red' | 'pink' | 'teal' | 'indigo';

export interface ColorPalette {
  50: string;
  100: string;
  200: string;
  300: string;
  400: string;
  500: string;
  600: string;
  700: string;
  800: string;
  900: string;
  950: string;
}

export const colorPalettes: Record<ColorPaletteName, ColorPalette> = {
  blue: {
    50: '#e8f4fc',
    100: '#d1e9f9',
    200: '#a3d3f3',
    300: '#75bded',
    400: '#47a7e7',
    500: '#1a91e1',
    600: '#1574b4',
    700: '#105787',
    800: '#0a3a5a',
    900: '#051d2d',
    950: '#020e16',
  },
  green: {
    50: '#f0fdf4',
    100: '#dcfce7',
    200: '#bbf7d0',
    300: '#86efac',
    400: '#4ade80',
    500: '#22c55e',
    600: '#16a34a',
    700: '#15803d',
    800: '#166534',
    900: '#14532d',
    950: '#052e16',
  },
  purple: {
    50: '#faf5ff',
    100: '#f3e8ff',
    200: '#e9d5ff',
    300: '#d8b4fe',
    400: '#c084fc',
    500: '#a855f7',
    600: '#9333ea',
    700: '#7e22ce',
    800: '#6b21a8',
    900: '#581c87',
    950: '#3b0764',
  },
  orange: {
    50: '#fff7ed',
    100: '#ffedd5',
    200: '#fed7aa',
    300: '#fdba74',
    400: '#fb923c',
    500: '#f97316',
    600: '#ea580c',
    700: '#c2410c',
    800: '#9a3412',
    900: '#7c2d12',
    950: '#431407',
  },
  red: {
    50: '#fef2f2',
    100: '#fee2e2',
    200: '#fecaca',
    300: '#fca5a5',
    400: '#f87171',
    500: '#ef4444',
    600: '#dc2626',
    700: '#b91c1c',
    800: '#991b1b',
    900: '#7f1d1d',
    950: '#450a0a',
  },
  pink: {
    50: '#fdf2f8',
    100: '#fce7f3',
    200: '#fbcfe8',
    300: '#f9a8d4',
    400: '#f472b6',
    500: '#ec4899',
    600: '#db2777',
    700: '#be185d',
    800: '#9d174d',
    900: '#831843',
    950: '#500724',
  },
  teal: {
    50: '#f0fdfa',
    100: '#ccfbf1',
    200: '#99f6e4',
    300: '#5eead4',
    400: '#2dd4bf',
    500: '#14b8a6',
    600: '#0d9488',
    700: '#0f766e',
    800: '#115e59',
    900: '#134e4a',
    950: '#042f2e',
  },
  indigo: {
    50: '#eef2ff',
    100: '#e0e7ff',
    200: '#c7d2fe',
    300: '#a5b4fc',
    400: '#818cf8',
    500: '#6366f1',
    600: '#4f46e5',
    700: '#4338ca',
    800: '#3730a3',
    900: '#312e81',
    950: '#1e1b4b',
  },
};

export const defaultColorPalette: ColorPaletteName = 'blue';

export function getPalette(name: ColorPaletteName): ColorPalette {
  return colorPalettes[name] || colorPalettes[defaultColorPalette];
}

export function applyColorPaletteToDocument(palette: ColorPalette): void {
  const root = document.documentElement;
  Object.entries(palette).forEach(([shade, color]) => {
    root.style.setProperty(`--custom-primary-${shade}`, color);
  });
}

export function removeColorPaletteFromDocument(): void {
  const root = document.documentElement;
  for (let i = 50; i <= 950; i += 50) {
    root.style.removeProperty(`--custom-primary-${i}`);
  }
}