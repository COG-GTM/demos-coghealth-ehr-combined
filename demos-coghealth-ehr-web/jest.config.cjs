module.exports = {
  preset: 'ts-jest',
  testEnvironment: 'node',
  testMatch: ['**/tests/**/*.test.ts'],
  // e2e drives a real browser against the dev server; run it via `npm run test:e2e`.
  testPathIgnorePatterns: ['/node_modules/', '<rootDir>/tests/e2e'],
  testTimeout: 30000,
  transform: {
    '^.+\\.tsx?$': ['ts-jest', {
      tsconfig: {
        module: 'commonjs',
        esModuleInterop: true,
      }
    }]
  },
};
