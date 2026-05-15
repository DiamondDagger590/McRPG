# McRPG Color Palette

> **Canonical reference for all player-facing colors in McRPG.**
> Also documented in `.cursor/rules/core.mdc` (AI enforcement) and configurable by server owners in `config.yml`.

---

## How It Works

Palette colors are **runtime-resolvable placeholders**. Locale YAML files use semantic names like `<primary>` instead of raw hex codes. The localization pipeline replaces them with configured MiniMessage values before MiniMessage parses the string.

```yaml
# Locale YAML — what developers write:
title: "<primary>Viewing Abilities"
lore:
  - "<body>Mana Cost: <mana>30"
  - "<hint>Right-click to configure"
```

Server owners customize colors in `config.yml` — one change rethemes the entire plugin:

```yaml
# config.yml — server-owner customizable:
palette:
  primary: "<color:#D4A76A>"
  hint: "<color:#E8C97A>"
  mana: "<color:#5EA8FF>"
  # ... etc
```

---

## Primary Colors

| Role | Placeholder | Default Value | Hex | When to Use |
|------|-------------|---------------|-----|-------------|
| **primary** | `<primary>` | `<color:#D4A76A>` | `#D4A76A` | GUI titles, navigation item names (back/sort buttons), stat value highlights, skill names in lore, section headers, item name accents |
| **hint** | `<hint>` | `<color:#E8C97A>` | `#E8C97A` | Click hints, calls-to-action ("Left-click to toggle"), interactive prompts |
| **mana** | `<mana>` | `<color:#5EA8FF>` | `#5EA8FF` | Mana cost values, mana-related lore |

## Ability Type Colors

| Role | Placeholder | Default Value | Hex | When to Use |
|------|-------------|---------------|-----|-------------|
| **ability-active** | `<ability-active>` | `<color:#FF7B5E>` | `#FF7B5E` | Active (ComboActivatable) ability names and type tags |
| **ability-passive** | `<ability-passive>` | `<color:#7FB87F>` | `#7FB87F` | Passive ability names and type tags |
| **ability-innate** | `<ability-innate>` | `<color:#9E9E9E>` | `#9E9E9E` | Innate (no unlock) ability names, disabled/inactive states, unavailable items |

## Standard Minecraft Colors

| Role | Placeholder | Default Value | When to Use |
|------|-------------|---------------|-------------|
| **body** | `<body>` | `<gray>` | All descriptive/body lore text, labels before highlighted values |
| **positive** | `<positive>` | `<green>` | Enabled toggles, success messages, quest accepted, positive status |
| **negative** | `<negative>` | `<red>` | Disabled toggles, error messages, cooldown active, quest abandon, deny feedback |
| **warning** | `<warning>` | `<yellow>` | Caution states, expiration warnings, approaching limits |

---

## Usage Rules

1. **Titles**: Always `<primary>` — never bare `<gold>`, `<black>`, or `<red>`
2. **Back button names**: Always `<primary>` — label pattern is `"Back to [Parent]"`
3. **Stat values in lore**: Always `<primary>` — `<body>Skill: <primary>Herbalism`
4. **Click prompts**: Always `<hint>` — `<hint>Right-click to configure`
5. **Ability item names**: Color by type — `<ability-active>`, `<ability-passive>`, or `<ability-innate>`
6. **Mana costs in ability lore**: Always `<mana>` — `<body>Mana Cost: <mana>30`
7. **Body text / labels**: Always `<body>` — `<body>Activation Chance:`
8. **Toggle on/off status**: `<positive>` / `<negative>` — `<positive>Enabled` / `<negative>Disabled`
9. **Error messages**: Always `<negative>` — `<negative>Not enough mana`
10. **Warnings**: Always `<warning>` — `<warning>Quest expires soon`

---

## What NOT to Use

| Deprecated | Replacement | Why |
|------------|-------------|-----|
| `<gold>` | `<primary>` | `<gold>` is #FFAA00 — harsh saturated orange that clashes with the inventory background |
| `<red>` for item names | `<primary>` or ability type placeholder | Red on item names creates a hostile "error" feel for normal navigation and content |
| `<black>` for titles | `<primary>` | Black is invisible on the dark inventory title bar |
| Raw hex codes | Palette placeholder | Raw hex defeats the one-place-to-change benefit — always use `<primary>`, `<mana>`, etc. |
