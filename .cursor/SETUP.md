# Cursor Setup Guide for McRPG Contributors

This guide covers everything needed to get a productive Cursor + AI agent environment for working on McRPG.

---

## Prerequisites

| Tool | Version | Notes |
|------|---------|-------|
| Java | 21 | Required by the build; must be on `PATH` |
| Gradle | via wrapper (`./gradlew`) | No separate install needed |
| Git | Any recent | Must be on `PATH` |
| GitHub CLI (`gh`) | Latest | Required for GitHub issue/PR skills — see below |

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

## Recommended Agent Skills

Skills extend what the AI agent can do. Install them globally with `npx skills add <skill> -g -y`.

The majority of recommended skills come from the [sickn33/antigravity-awesome-skills](https://github.com/sickn33/antigravity-awesome-skills) collection — a curated library of 1,200+ battle-tested agent skills. You can install the entire collection at once with:

```bash
npx antigravity-awesome-skills --cursor
```

Or install only the skills listed below individually.

### GitHub

```bash
npx skills add github/awesome-copilot@github-issues -g -y
npx skills add github/awesome-copilot@create-github-issues-feature-from-implementation-plan -g -y
```

| Skill | What it unlocks |
|-------|----------------|
| `github-issues` | Create, update, and manage GitHub issues |
| `create-github-issues-feature-from-implementation-plan` | Turn a feature plan into a set of tracked GitHub issues |

### General Workflow

```bash
npx skills add sickn33/antigravity-awesome-skills@concise-planning -g -y
npx skills add sickn33/antigravity-awesome-skills@git-pushing -g -y
npx skills add sickn33/antigravity-awesome-skills@lint-and-validate -g -y
npx skills add sickn33/antigravity-awesome-skills@kaizen -g -y
npx skills add sickn33/antigravity-awesome-skills@find-skills -g -y
```

| Skill | What it unlocks |
|-------|----------------|
| `concise-planning` | Generates clear, atomic, actionable implementation checklists |
| `git-pushing` | Stages, commits, and pushes with conventional commit messages |
| `lint-and-validate` | Automatic quality checks after every code change |
| `kaizen` | Continuous improvement, refactoring, and standardization guidance |
| `find-skills` | Searches the skills ecosystem when you need a new capability |

### Minecraft / Bukkit

```bash
npx skills add sickn33/antigravity-awesome-skills@minecraft-bukkit-pro -g -y
```

| Skill | What it unlocks |
|-------|----------------|
| `minecraft-bukkit-pro` | Deep Paper/Bukkit API knowledge — event system, commands, scheduling, performance |

### Cursor IDE

```bash
npx skills add sickn33/antigravity-awesome-skills@create-rule -g -y
npx skills add sickn33/antigravity-awesome-skills@create-skill -g -y
```

| Skill | What it unlocks |
|-------|----------------|
| `create-rule` | Author new `.cursor/rules/*.mdc` files following best practices |
| `create-skill` | Author new agent skills in the correct `SKILL.md` format |

> **Tip:** If you're ever unsure whether a skill exists for a given task, ask the agent: `"find a skill for <topic>"` — it will search the registry and suggest install commands.

---

## Project-Specific Skills

These skills live inside the repository and are automatically available to anyone with the project open — no install needed.

| Skill | Location | Purpose |
|-------|----------|---------|
| `oop-collaborator-pattern` | `.cursor/skills/oop-collaborator-pattern/` | Guides the agent to prefer object collaborators over static utility helpers for domain behavior |

---

## Build Commands

```bash
./gradlew verifiedShadowJar   # clean + test + build (recommended before committing)
./gradlew fastShadowJar       # clean + build (skip tests, faster iteration)
./gradlew test                # run tests only
```

Output jar: `build/libs/McRPG-<version>-<git-hash>.jar`

---

## Key Reading

Before making changes, familiarise yourself with:

- **`CLAUDE.md`** — full architecture guide, domain terminology, naming conventions, and anti-patterns
- **`.cursor/rules/core.mdc`** — always-applied coding rules the agent enforces automatically
- **`.cursor/rules/persona-*.mdc`** — role-specific review lenses (testing, GUI/UX, extensibility, server-owner)
