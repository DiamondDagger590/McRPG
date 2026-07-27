# Cursor Setup Guide for McRPG Contributors

This guide covers what's needed to get a productive Cursor environment for working on McRPG.

> **Note:** The canonical AI steering content for this repo lives in `CLAUDE.md` and `.claude/` (skills, agents, hooks) — maintained for Claude Code, which is the primary AI tool for this project. Cursor sessions get the always-on conventions from `.cursor/rules/core.mdc`; for anything deeper, read `CLAUDE.md` and the relevant `.claude/skills/<name>/SKILL.md`.

---

## Prerequisites

| Tool | Version | Notes |
|------|---------|-------|
| Java | 21 | Required by the build; must be on `PATH` |
| Gradle | via wrapper (`./gradlew`) | No separate install needed |
| Git | Any recent | Must be on `PATH` |
| GitHub CLI (`gh`) | Latest | Required for GitHub issue/PR operations — see below |

---

## GitHub CLI

The agent uses the GitHub CLI for creating issues, opening PRs, and other repository operations.

**Install:** https://cli.github.com

**Authenticate after installing:**
```bash
gh auth login
```
Choose "GitHub.com" → "HTTPS" → "Login with a web browser" and follow the prompts.

**Verify:**
```bash
gh auth status
```

---

## Project AI Steering Files

Everything is in-repo — no external skill installs needed.

| Location | Purpose |
|----------|---------|
| `CLAUDE.md` | Full architecture guide, domain terminology, naming conventions, anti-patterns — the canonical reference |
| `.cursor/rules/core.mdc` | Always-applied core conventions for Cursor sessions (mirror of CLAUDE.md essentials) |
| `.claude/skills/add-ability/`, `add-skill/` | Step-by-step scaffolding workflows with code templates and references |
| `.claude/skills/review-*/` | Per-concern review checklists (architecture, concurrency, error-handling, extensibility, gui-ux, performance, security, server-owner, testing) |
| `.claude/skills/oop-collaborator-pattern/` | Collaborator-over-static-helper design guidance |
| `.claude/agents/review-*.md` | Thin CI review lenses used by the PR review workflow |
| `PALETTE.md` | Canonical GUI color palette specification |

---

## Build Commands

```bash
./gradlew verifiedShadowJar   # clean + test + build (recommended before committing)
./gradlew fastShadowJar       # clean + build (skip tests, faster iteration)
./gradlew test                # run tests only
```

Output jar: `build/libs/McRPG-<version>-<git-hash>.jar`
