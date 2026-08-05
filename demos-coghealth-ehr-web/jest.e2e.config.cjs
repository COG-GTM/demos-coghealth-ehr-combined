const base = require('./jest.config.cjs');

module.exports = {
  ...base,
  testMatch: ['**/tests/e2e.test.ts', '**/tests/e2e/**/*.test.ts'],
  testPathIgnorePatterns: ['/node_modules/'],
};
