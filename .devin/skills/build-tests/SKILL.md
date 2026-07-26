---
name: build-tests
description: Create tests for the changes made in the current session and run them
allowed-tools:
  - read
  - edit
  - grep
  - glob
  - exec
permissions:
  allow:
    - Exec(git diff)
    - Exec(git status)
    - Exec(npm test)
    - Exec(npm run)
    - Exec(mvn test)
---

Write and run tests covering the changes from the current session.

1. Identify what changed:
   - Use the changes already discussed in this conversation as the primary source.
   - Cross-check with `git status` and `git diff` (plus `git diff --staged`) to see all uncommitted changes.
2. For each changed file, decide what needs test coverage: new functions/components, changed logic, edge cases, and bug fixes (write a test that would have failed before the fix).
3. Before writing tests, look at existing tests to match conventions:
   - Frontend (`demos-coghealth-ehr-web`): find existing `*.test.*` / `*.spec.*` files and mirror their framework, file placement, naming, and mocking patterns.
   - Backend (`demos-coghealth-ehr-api`): find existing tests under `src/test/java` and mirror their JUnit/Mockito style and package structure.
4. Write focused tests only for the changed behavior — do not add broad unrelated coverage or rewrite existing tests.
5. Run the relevant tests:
   - Frontend: `npm test` from `demos-coghealth-ehr-web` (scope to the new test files if the runner supports it).
   - Backend: `mvn test -Dtest=<TestClass>` from `demos-coghealth-ehr-api`, only if backend files changed.
6. If tests fail, determine whether the test or the change under test is wrong. Fix tests freely; only fix source code if it's clearly a bug introduced by the session's changes.
7. Report: which tests were added (file paths), what they cover, and pass/fail results.
