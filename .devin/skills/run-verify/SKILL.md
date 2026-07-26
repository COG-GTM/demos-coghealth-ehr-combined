---
name: run-verify
description: How to build and verify both frontend and backend
---

1. From `demos-coghealth-ehr-web`, compile and build the frontend.
   // turbo
   ```bash
   npm run build
   ```
2. From `demos-coghealth-ehr-api`, compile the backend.
   // turbo
   ```bash
   mvn clean compile
   ```
3. From `demos-coghealth-ehr-web`, run unit tests if relevant.
   // turbo
   ```bash
   npm test
   ```
4. From `demos-coghealth-ehr-web`, run e2e tests if the change affects browser behavior.
   // turbo
   ```bash
   npm run test:e2e
   ```
5. Smoke-test the running API latency/health.
   // turbo
   ```bash
   curl -w "Time: %{time_total}s\n" -s -o /dev/null "http://localhost:8080/api/v1/patients/search?q=a&page=0&size=20"
   ```
