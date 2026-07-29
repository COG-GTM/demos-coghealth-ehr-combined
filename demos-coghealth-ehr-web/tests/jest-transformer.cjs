const { TsJestTransformer } = require('ts-jest');

// `import.meta.env` is Vite-only syntax that cannot run in Jest's CommonJS
// modules, so rewrite it to a global that tests may populate.
const rewriteImportMeta = (src) =>
  src.replace(/import\.meta\.env/g, '((globalThis as any).__VITE_ENV__ || {})');

class ViteEnvAwareTransformer extends TsJestTransformer {
  process(sourceText, sourcePath, options) {
    return super.process(rewriteImportMeta(sourceText), sourcePath, options);
  }

  processAsync(sourceText, sourcePath, options) {
    return super.processAsync(rewriteImportMeta(sourceText), sourcePath, options);
  }
}

module.exports = new ViteEnvAwareTransformer({
  tsconfig: {
    module: 'commonjs',
    esModuleInterop: true,
  },
});
