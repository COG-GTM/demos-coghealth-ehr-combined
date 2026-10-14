import {
  THEME_STORAGE_KEY,
  THEMES,
  applyResolvedTheme,
  getStorage,
  isTheme,
  readStoredTheme,
  resolveTheme,
  toggledTheme,
} from '../../src/context/theme';

const storageWith = (value: string | null) => ({ getItem: jest.fn(() => value) });

const fakeRoot = () => {
  const classes = new Set<string>();
  return {
    classes,
    classList: {
      toggle: (token: string, force?: boolean) => {
        const on = force ?? !classes.has(token);
        if (on) classes.add(token);
        else classes.delete(token);
        return on;
      },
    },
    style: { colorScheme: '' },
  };
};

describe('theme utilities', () => {
  describe('isTheme', () => {
    test.each(THEMES)('accepts %s', (theme) => {
      expect(isTheme(theme)).toBe(true);
    });

    test.each([null, undefined, '', 'Dark', 'blue', 1])('rejects %p', (value) => {
      expect(isTheme(value)).toBe(false);
    });
  });

  describe('readStoredTheme', () => {
    test('reads the coghealth-theme key', () => {
      const storage = storageWith('dark');
      expect(readStoredTheme(storage)).toBe('dark');
      expect(storage.getItem).toHaveBeenCalledWith(THEME_STORAGE_KEY);
      expect(THEME_STORAGE_KEY).toBe('coghealth-theme');
    });

    test('defaults to system when nothing is stored', () => {
      expect(readStoredTheme(storageWith(null))).toBe('system');
    });

    test('defaults to system for invalid stored values', () => {
      expect(readStoredTheme(storageWith('purple'))).toBe('system');
    });

    test('defaults to system when storage is unavailable or throws', () => {
      expect(readStoredTheme(undefined)).toBe('system');
      const throwing = { getItem: () => { throw new Error('SecurityError'); } };
      expect(readStoredTheme(throwing)).toBe('system');
    });
  });

  describe('resolveTheme', () => {
    test('explicit themes ignore the OS preference', () => {
      expect(resolveTheme('light', true)).toBe('light');
      expect(resolveTheme('dark', false)).toBe('dark');
    });

    test('system follows the OS preference', () => {
      expect(resolveTheme('system', true)).toBe('dark');
      expect(resolveTheme('system', false)).toBe('light');
    });
  });

  describe('toggledTheme', () => {
    test('flips between light and dark', () => {
      expect(toggledTheme('light')).toBe('dark');
      expect(toggledTheme('dark')).toBe('light');
    });
  });

  describe('applyResolvedTheme', () => {
    test('adds the dark class and dark color-scheme', () => {
      const root = fakeRoot();
      applyResolvedTheme(root, 'dark');
      expect(root.classes.has('dark')).toBe(true);
      expect(root.style.colorScheme).toBe('dark');
    });

    test('removes the dark class when switching back to light', () => {
      const root = fakeRoot();
      applyResolvedTheme(root, 'dark');
      applyResolvedTheme(root, 'light');
      expect(root.classes.has('dark')).toBe(false);
      expect(root.style.colorScheme).toBe('light');
    });
  });

  describe('getStorage', () => {
    it('returns localStorage when accessible', () => {
      const storage = {} as Storage;
      expect(getStorage({ localStorage: storage })).toBe(storage);
    });

    it('returns undefined when accessing localStorage throws', () => {
      const win = Object.defineProperty({}, 'localStorage', {
        get: () => {
          throw new DOMException('blocked', 'SecurityError');
        },
      }) as { localStorage: Storage };
      expect(getStorage(win)).toBeUndefined();
      expect(readStoredTheme(getStorage(win))).toBe('system');
    });
  });
});
