# GUI/UX System & Color Palette

> **Last Updated:** 2026-05-10
> **Status:** Phase 0 (documentation) complete; Phases 1–4 pending implementation
> **Scope:** Unified GUI color palette, navigation standardization, ability display improvements, upgrade quest slot separation

---

## Architecture Overview

McRPG's GUI system is built on McCore's `BaseGui<McRPGPlayer>` and `PaginatedGui<McRPGPlayer>` abstractions. All GUIs use the BoostedYAML-backed localization system for player-facing text, materials, and lore — server owners can customize every string via locale YAML files. Slot classes implement `McRPGSlot` and resolve their display items through `LocalizationKey` route constants.

```mermaid
flowchart TD
    subgraph baseLayer [McCore Base Classes]
        BG["BaseGui (McCore)"]
        PG["PaginatedGui (McCore)"]
    end

    subgraph mcrpgLayer [McRPG Abstractions]
        MPG[McRPGPaginatedGui]
        PSAG[PaginatedSortedAbilityGui]
        PSSG[PaginatedSortedSkillGui]
    end

    subgraph concreteGuis [Concrete GUIs]
        HG[HomeGui]
        EBG[ExperienceBankGui]
        AAEG[AbilityAttributeEditGui]
        LSG[LoadoutSelectionGui]
        QBG[QuestBoardGui]
        AQG[ActiveQuestGui]
        QHG[QuestHistoryGui]
        QDG[QuestDetailGui]
        PSG[PlayerSettingGui]
        AG[AbilityGui]
        LG[LoadoutGui]
        SG[SkillGui]
    end

    BG --> PG
    PG --> MPG
    MPG --> PSAG
    MPG --> PSSG
    HG --> BG
    EBG --> BG
    AAEG --> BG
    LSG --> MPG
    QBG --> MPG
    AQG --> MPG
    QHG --> MPG
    QDG --> MPG
    PSG --> MPG
    AG --> PSAG
    LG --> PSAG
    SG --> PSSG
```

### Navigation Flow

```mermaid
flowchart LR
    Home[Home GUI]
    Home --> Abilities[Ability GUI]
    Home --> Skills[Skill GUI]
    Home --> Loadouts[Loadout Selection]
    Home --> Settings[Settings GUI]
    Home --> ExpBank[Experience Bank]
    Home --> Quests[Active Quests]
    Home --> Board[Quest Board]

    Abilities -->|"right-click ability"| AbilityEdit[Ability Edit GUI]
    AbilityEdit -->|"click quest slot"| QuestDetail[Quest Detail GUI]
    AbilityEdit -->|"remote transfer"| RemoteTransfer[Remote Transfer GUI]

    Loadouts --> LoadoutEdit[Loadout Edit GUI]
    LoadoutEdit --> LoadoutDisplay[Loadout Display GUI]
    LoadoutEdit -->|"click slot"| AbilitySelect[Ability Select GUI]
    LoadoutDisplay --> DisplayItemInput[Display Item Input GUI]

    ExpBank --> RedeemSkillSelect[Redeem Skill Select]
    RedeemSkillSelect --> RedeemExp[Redeem Experience GUI]
    RedeemSkillSelect --> RedeemLevels[Redeem Levels GUI]

    Quests --> QuestDetail
    Quests --> QuestHistory[Quest History]
    QuestHistory --> QuestDetail

    Board --> QuestDetail
    Board -->|"group quests"| ScopedSelector[Scoped Entity Selector]
    ScopedSelector --> Board

    QuestDetail -->|"abandon"| AbandonConfirm[Abandon Confirm GUI]
```

---

## Current Problems (Audit Findings)

### Color Inconsistencies

- **GUI titles**: 16 GUIs use `<gold>` (#FFAA00), 4 Experience Bank GUIs use `<black>`, 1 uses `<red>` — no unified scheme
- **Item names**: Home GUI slots use `<red>`, ability names use `<red>`, settings use `<gold>`, sort buttons use `<red>` — mixing red and gold arbitrarily
- **Value highlights**: All stat values in lore use `<gold>` — harsh saturated orange that clashes with the dark inventory background

### Navigation Inconsistencies

- **Back button labels**: 14 different label strings across GUIs ("Return to Home Menu", "Return to Previous GUI", "Return to Ability Selection", "Return to loadout selection", "Back to Active Quests", etc.)
- **Verb inconsistency**: Some say "Return to", others say "Back to"
- **All back buttons**: Use BARRIER material + `<red>` text — creates a hostile "error" feel for normal navigation

### Ability GUI Gaps

- **No type indicator**: No way to know if an ability is Active (combo), Passive (event-triggered), or Innate (always-on)
- **No toggle state**: Only enchantment glint distinguishes enabled/disabled — easily missed, especially with items that already have enchant glint
- **No click hints**: Players don't know left-click toggles and right-click opens the edit GUI
- **Upgrade quest crammed onto tier item**: In the Ability Edit GUI, quest progress bar is appended as lore on the tier attribute item rather than being a dedicated slot

### Bugs

- **Broken MiniMessage tag** in redeem GUI back button lore: `./gray>` instead of `</gray>`
- **Loadout GUI back button lore** is a YAML scalar string, not a list like every other back button
- **Title typo**: "Home Gui" with lowercase "Gui"
- **Naming mismatch**: Ability Edit back button says "Return to Ability Selection" but target GUI title is "Viewing Abilities"

---

## Semantic Color Palette

The palette is the **single source of truth** for all player-facing colors across McRPG GUIs and (in a future pass) chat messages. It is documented in three locations:

1. **[PALETTE.md](../../PALETTE.md)** — human-readable quick-reference card at repo root
2. **[.cursor/rules/core.mdc](../../.cursor/rules/core.mdc)** — AI agent enforcement rules
3. **`config.yml` palette section** — runtime-configurable color definitions that server owners can customize

### Runtime Palette Placeholders

Palette colors are **runtime-resolvable placeholders**, not hardcoded hex codes in locale files. Locale YAML files use semantic names like `<primary>`, and the localization pipeline replaces them with their configured MiniMessage values before MiniMessage parses the string.

**Locale YAML** (what developers and server owners write):
```yaml
title: "<primary>Viewing Abilities"
lore:
  - "<body>Mana Cost: <mana>30"
  - "<hint>Right-click to configure"
```

**Config section** (server-owner customizable, in `config.yml`):
```yaml
palette:
  primary: "<color:#D4A76A>"
  hint: "<color:#E8C97A>"
  mana: "<color:#5EA8FF>"
  ability-active: "<color:#FF7B5E>"
  ability-passive: "<color:#7FB87F>"
  ability-innate: "<color:#9E9E9E>"
  body: "<gray>"
  positive: "<green>"
  negative: "<red>"
  warning: "<yellow>"
```

**Resolution pipeline**: `McRPGLocalizationManager` maintains a palette `Map<String, String>` loaded from config. Before every placeholder resolution call, the palette entries are merged into the placeholder map (palette entries have lowest priority — per-call placeholders override them if keys collide). The existing `String.replace("<" + key + ">", value)` mechanism handles substitution with no new infrastructure needed.

```
Locale YAML string:  "<primary>Skill: <primary><skill>"
After palette merge:  "<color:#D4A76A>Skill: <color:#D4A76A><skill>"
After per-call map:   "<color:#D4A76A>Skill: <color:#D4A76A>Herbalism"
MiniMessage parses:   Component with warm amber "Skill: Herbalism"
```

### Primary Colors (3 custom hex)

| Role | Placeholder | Default Value | Hex | When to Use |
|------|-------------|---------------|-----|-------------|
| **primary** | `<primary>` | `<color:#D4A76A>` | `#D4A76A` | GUI titles, navigation item names, stat value highlights, skill names in lore, section headers, item name accents |
| **hint** | `<hint>` | `<color:#E8C97A>` | `#E8C97A` | Click hints, calls-to-action, interactive prompts |
| **mana** | `<mana>` | `<color:#5EA8FF>` | `#5EA8FF` | Mana cost values, mana-related lore |

### Ability Type Colors (3 custom hex)

| Role | Placeholder | Default Value | Hex | When to Use |
|------|-------------|---------------|-----|-------------|
| **ability-active** | `<ability-active>` | `<color:#FF7B5E>` | `#FF7B5E` | Active (ComboActivatable) ability names and type tags |
| **ability-passive** | `<ability-passive>` | `<color:#7FB87F>` | `#7FB87F` | Passive ability names and type tags |
| **ability-innate** | `<ability-innate>` | `<color:#9E9E9E>` | `#9E9E9E` | Innate (no unlock) ability names, disabled/inactive states, unavailable items |

### Standard Minecraft Colors (4 vanilla)

| Role | Placeholder | Default Value | When to Use |
|------|-------------|---------------|-------------|
| **body** | `<body>` | `<gray>` | All descriptive/body lore text, labels before values |
| **positive** | `<positive>` | `<green>` | Enabled toggles, success messages, quest accepted |
| **negative** | `<negative>` | `<red>` | Disabled toggles, error messages, cooldown active, quest abandon |
| **warning** | `<warning>` | `<yellow>` | Caution states, expiration warnings, approaching limits |

### Usage Rules

1. **Titles**: Always `<primary>` — never bare `<gold>`, `<black>`, or `<red>`
2. **Back button names**: Always `<primary>` — pattern is `"Back to [Parent]"`
3. **Stat values in lore**: Always `<primary>` — e.g., `<body>Skill: <primary>Herbalism`
4. **Click prompts**: Always `<hint>` — e.g., `<hint>Right-click to configure`
5. **Ability item names**: Color by type — `<ability-active>`, `<ability-passive>`, or `<ability-innate>`
6. **Mana costs in ability lore**: Always `<mana>` — e.g., `<body>Mana Cost: <mana>30`
7. **Body text / labels**: Always `<body>` — e.g., `<body>Activation Chance:`
8. **Toggle on/off status**: `<positive>` / `<negative>` — `<positive>Enabled` / `<negative>Disabled`
9. **Error messages**: Always `<negative>` — e.g., `<negative>Not enough mana`
10. **Warnings**: Always `<warning>` — e.g., `<warning>Quest expires soon`

### What NOT to Use

- `<gold>` — replaced by `<primary>` everywhere
- `<red>` for item names (back buttons, ability names, home GUI slots) — use `<primary>` or ability type placeholder
- `<black>` for titles — use `<primary>`
- Raw hex codes in locale files — always use palette placeholders so server owners can retheme in one place

---

## Core Concepts

### 1. Ability Type Classification

The GUI needs to communicate ability type to the player. Classification uses existing interfaces:

| Type | Detection | Placeholder | Player Description |
|------|-----------|-------------|--------------------|
| **Active** | `ability instanceof ComboActivatable` | `<ability-active>` | Triggered by click combos, consumes mana |
| **Passive** | `ability instanceof PassiveAbility` and has `ABILITY_UNLOCKED_ATTRIBUTE` | `<ability-passive>` | Activates automatically on events, can be toggled |
| **Innate** | No `ABILITY_UNLOCKED_ATTRIBUTE` in `AbilityData` | `<ability-innate>` | Always active, no unlock requirement |

There is no `InnateAbility` interface — "innate" is a data-level concept (no unlock attribute), not a type-level one. The existing `InnateAbilityFilter` uses this same detection.

### 2. Ability Item Lore Structure (Target State)

After this system is implemented, ability items in the Viewing Abilities GUI will display:

```
[Ability Name]                          ← colored by type
Description line 1                      ← from en_abilities.yml
Description line 2

Type: Active/Passive/Innate             ← new, colored by type
Status: Enabled/Disabled                ← new, if toggleable
Left-click to enable/disable            ← new click hint, if toggleable
Right-click to configure                ← new click hint, if has editable attributes

Skill: Herbalism                        ← existing stat lines from en_abilities.yml
Activation Chance: 0.5
Mana Cost: 30                           ← sky blue color

Upgrade Quest Progress: ████████░░      ← existing, from AbilityLoreAppender
```

### 3. Upgrade Quest Slot (Ability Edit GUI)

Currently, upgrade quest progress is crammed onto the tier attribute item via `AbilityLoreAppender`. This system introduces a dedicated `UpgradeQuestSlot`:

- **Has active quest**: Shows progress bar, quest name, "Click to view quest details" — click navigates to `QuestDetailGui`
- **No active quest**: Shows disabled state item ("No Active Upgrade Quest")

The tier attribute item (`AbilityTierAttribute.getSlot()`) stops calling `AbilityLoreAppender.getAppendLore()` for quest progress — it only shows tier number and upgrade eligibility.

### 4. Navigation Standardization

All back buttons follow one pattern:
- **Material**: `BARRIER` (recognizable Minecraft convention)
- **Name color**: `<primary>` (warm amber) — not `<red>`
- **Label pattern**: `"Back to [Parent GUI Name]"`
- **Lore**: Single line `"<body>Click to go back."`

---

## Extension Points

### For Third-Party Plugins

- **Custom ability types**: Third-party abilities implementing `ComboActivatable` or `PassiveAbility` automatically get the correct type color in the Ability GUI. Innate detection works via attribute presence, no interface needed.
- **Custom palette**: Server owners redefine any palette color in `config.yml` — one change retemes the entire plugin.
- **Custom GUI slots**: Third-party slots implementing `McRPGSlot` follow the same palette by using `<primary>`, `<body>`, etc. in their locale entries — the palette map is merged automatically.

### For Content Expansions

- New abilities registered via `ContentExpansion` automatically appear in the Ability GUI with correct type coloring if they implement the standard type interfaces.
- The `UpgradeQuestSlot` works with any `TierableAbility` that has an `AbilityUpgradeQuestAttribute`, including third-party abilities.

---

## Proposed Implementation Phases

Each phase gets its own LLD when implementation begins.

### Phase 1: Palette Infrastructure + GUI Locale Sweep

- Add `palette` config section to `config.yml` with all 10 default entries
- Add `Route` constants for each palette key in the config file wrapper
- Extend `McRPGLocalizationManager` to load the palette map from config and merge it into every placeholder resolution call (palette entries have lowest priority — per-call placeholders win on key collision)
- Update all GUI titles to `<primary>` placeholder
- Standardize all back button labels to `"Back to [Parent]"` pattern with `<primary>`
- Update home GUI slot names and sort button names from `<red>` to `<primary>`
- Replace `<gold>` with `<primary>` across en_gui.yml value highlights
- Fix bugs: broken MiniMessage tag, loadout lore scalar, title typo, naming mismatch

### Phase 2: Ability Display Overhaul

- Change ability name colors in en_abilities.yml to type placeholders (`<ability-active>`, `<ability-passive>`, `<ability-innate>`)
- Add type tag, toggle status, and click hint lore lines to AbilitySlot
- New locale keys in en_abilities.yml and LocalizationKey.java
- Replace `<gold>` with `<primary>` in en_abilities.yml stat values
- Use `<mana>` placeholder for mana cost values in ability lore

### Phase 3: Ability Edit GUI Quest Slot

- Create `UpgradeQuestSlot` class with quest detail navigation
- Add to `AbilityAttributeEditGui` layout
- Remove quest progress lore from `AbilityTierAttribute` tier item
- New locale keys in en_gui.yml

### Phase 4: Remaining Locale Sweep

- Update loadout GUI colors in en_gui.yml
- Sweep remaining locale files (en.yml, en_skills.yml, en_commands.yml, en_quest.yml, en_stats.yml) to replace `<gold>` and raw hex with palette placeholders

---

## Related Documents

- **[PALETTE.md](../../PALETTE.md)** — Quick-reference color card
- **[.cursor/rules/core.mdc](../../.cursor/rules/core.mdc)** — AI agent enforcement rules (Palette Governance section)
- **[.cursor/rules/persona-gui-ux.mdc](../../.cursor/rules/persona-gui-ux.mdc)** — GUI/UX review persona checklist
