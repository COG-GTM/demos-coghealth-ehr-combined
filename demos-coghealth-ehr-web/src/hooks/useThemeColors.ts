import { useEffect } from 'react';
import { getPalette, applyColorPaletteToDocument, type ColorPaletteName } from '../utils/themeColors';

const COLOR_STORAGE_KEY = 'coghealth_primary_color';

export function useThemeColors(paletteName: ColorPaletteName | null = null) {
  useEffect(() => {
    const colorToApply = paletteName || (localStorage.getItem(COLOR_STORAGE_KEY) as ColorPaletteName) || 'blue';
    const palette = getPalette(colorToApply);
    applyColorPaletteToDocument(palette);
  }, [paletteName]);
}

export function savePrimaryColor(color: ColorPaletteName): void {
  localStorage.setItem(COLOR_STORAGE_KEY, color);
}

export function getPrimaryColor(): ColorPaletteName {
  return (localStorage.getItem(COLOR_STORAGE_KEY) as ColorPaletteName) || 'blue';
}