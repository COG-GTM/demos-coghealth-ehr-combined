import {
  THEME_STORAGE_KEY,
  THEMES,
  applyTheme,
  isTheme,
  nextTheme,
  readStoredTheme,
  resolveTheme,
  storeTheme,
} from '../src/context/theme';

function fakeStorage(initial: Record<string, string> = {}) {
  const data = { ...initial };
  return {
    data,
    getItem: (key: string) => (key in data ? data[key] : null),
    setItem: (key: string, value: string) => {
      data[key] = value;
    },
  };
}

function fakeRoot() {
  const classes = new Set<string>();
  return {
    classes,
    classList: {
      add: (token: string) => void classes.add(token),
      remove: (token: string) => void classes.delete(token),
    },
  };
}

describe('isTheme', () => {
  test.each(THEMES)('accepts %s', (theme) => {
    expect(isTheme(theme)).toBe(true);
  });

  test('rejects unknown values', () => {
    expect(isTheme('solarized')).toBe(false);
    expect(isTheme(null)).toBe(false);
  });
});

describe('readStoredTheme', () => {
  test('returns the stored theme', () => {
    const storage = fakeStorage({ [THEME_STORAGE_KEY]: 'dark' });
    expect(readStoredTheme(storage)).toBe('dark');
  });

  test('falls back to system when nothing or garbage is stored', () => {
    expect(readStoredTheme(fakeStorage())).toBe('system');
    expect(readStoredTheme(fakeStorage({ [THEME_STORAGE_KEY]: 'neon' }))).toBe('system');
    expect(readStoredTheme(undefined)).toBe('system');
  });

  test('falls back to system when storage throws', () => {
    const storage = {
      getItem: () => {
        throw new Error('storage disabled');
      },
      setItem: () => {},
    };
    expect(readStoredTheme(storage)).toBe('system');
  });
});

describe('storeTheme', () => {
  test('persists under the theme key', () => {
    const storage = fakeStorage();
    storeTheme(storage, 'dark');
    expect(storage.data[THEME_STORAGE_KEY]).toBe('dark');
  });

  test('ignores storage failures', () => {
    const storage = {
      getItem: () => null,
      setItem: () => {
        throw new Error('quota exceeded');
      },
    };
    expect(() => storeTheme(storage, 'light')).not.toThrow();
  });
});

describe('resolveTheme', () => {
  test('explicit themes ignore the system preference', () => {
    expect(resolveTheme('light', true)).toBe('light');
    expect(resolveTheme('dark', false)).toBe('dark');
  });

  test('system follows the OS preference', () => {
    expect(resolveTheme('system', true)).toBe('dark');
    expect(resolveTheme('system', false)).toBe('light');
  });
});

describe('applyTheme', () => {
  test('adds the dark class for dark and removes it for light', () => {
    const root = fakeRoot();
    applyTheme(root, 'dark');
    expect(root.classes.has('dark')).toBe(true);
    applyTheme(root, 'light');
    expect(root.classes.has('dark')).toBe(false);
  });
});

describe('nextTheme', () => {
  test('cycles light -> dark -> system -> light', () => {
    expect(nextTheme('light')).toBe('dark');
    expect(nextTheme('dark')).toBe('system');
    expect(nextTheme('system')).toBe('light');
  });
});
