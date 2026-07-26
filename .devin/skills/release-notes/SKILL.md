---
name: release-notes
description: Generate clinician-friendly release notes from recent git history (runs on a fast model)
argument-hint: "[since-ref, e.g. a tag or 'last 20 commits']"
model: swe
---

Generate release notes for CogHealth EHR from git history.

1. Determine the range: use the ref/range the user gave, otherwise the last 20 commits (`git log --oneline -20`).
2. Inspect the interesting commits with `git show --stat` to understand what actually changed.
3. Write release notes with two audiences in mind:
   - **For clinical staff** — plain-language bullets about visible changes ("Patient search now shows insurance status"), no file names, no jargon.
   - **For engineering** — grouped bullets (Frontend / Backend / Database / Security) with PR-style summaries; call out any new Flyway migrations explicitly.
4. Output as Markdown to the chat only — do not create a file unless asked.
