---
trigger: glob
globs: demos-coghealth-ehr-web/**
description: Frontend conventions — auto-activates when touching the React app
---

- Stack: React 19, Vite, TypeScript, Tailwind CSS v4, React Router v7, Zustand, TanStack Query, react-hook-form + zod, lucide-react.
- Preserve the dense EHR desktop style: small fonts, compact layouts, existing CSS variables in `src/index.css`.
- Reuse primitives from `src/components/ui/` and `src/components/patient/`; use `lucide-react` for icons.
- All HTTP goes through the `api` wrapper in `src/services/api.ts` (`VITE_API_URL`, default `http://localhost:8080/api`).
- Frontend audit events live in `sessionStorage` under `coghealth_audit_log`.
- Do not add dependencies unless clearly necessary.
- New pages go in `src/pages/`, with routes and nav items in `src/App.tsx`. Validate with `npm run build` from `demos-coghealth-ehr-web`.
