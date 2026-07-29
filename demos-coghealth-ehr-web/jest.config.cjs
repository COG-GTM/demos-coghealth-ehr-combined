module.exports = {
  preset: 'ts-jest',
  testEnvironment: 'node',
  testMatch: ['**/tests/**/*.test.ts'],
  testTimeout: 30000,
  transform: {
    '^.+\\.tsx?$': '<rootDir>/tests/jest-transformer.cjs',
  },
};
