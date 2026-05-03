# McRPG Stat System Design

> **Status:** Design Spike -- Partially Shipped
> **Last Updated:** 2026-05-03
> **Scope:** Primary stats, secondary combat stats, Fitness skill, loadout system, HP implementation

---

## Spike Status

This document was written as a **design spike** exploring a full stat system for McRPG's combat rework. It is not a formal feature specification -- it was used to explore the design space and inform a PoC implementation.

### What shipped from this spike

The following concepts were implemented as a PoC and are being formalized into production systems:

- **Mana pool** -- a per-player resource pool (base max, passive regen) tracked in memory via `CombatStatInstance`
- **Mana as the combo activation resource** -- combo abilities consume mana on activation; insufficient mana denies activation with feedback
- **Action bar HUD** -- three-zone layout (HP / center / mana) with pixel-width anchoring, priority-based center content, and persistent pool display (see [Action Bar HUD LLD](action-bar-hud.md) for the production design)
- **Combat stat infrastructure** -- `CombatStat`, `CombatStatRegistry`, `StatManager`, `PlayerCombatData`, and the modifier system (`CombatStatModifier`)

### What is NOT shipping from this document

The following sections remain as design exploration only. They have **not** been implemented and are **not** planned for immediate work. If any of these move forward, they will get their own HLD/LLD:

- **Custom HP pool** (Sections 4, 10) -- the 100-275 HP range, percentage-mapped hearts, damage pipeline takeover. **Decision (2026-05-03):** HP on the action bar will mirror vanilla health directly for now. The renderer is future-proofed to show custom values later, but no custom HP pool exists.
- **Defense stat and DR formula** (Section 5) -- the `Defense / (Defense + K)` formula and armor penetration math
- **Attack Power** (Section 2) -- outgoing damage scaling stat
- **Mana Regen as a visible stat** (Section 2) -- currently mana regen is a flat config value, not a player-facing stat
- **Secondary combat stats** (Section 6) -- Tenacity, Armor Pen (flat and percentage)
- **Stat passives and the loadout stat slot tradeoff** (Sections 7, 8) -- the 3-active/5-passive loadout model with stat passives competing for slots
- **Fitness skill** (Section 9) -- the damage-based leveling skill with innate HP scaling and milestone passives
- **HP growth sources** (Section 10) -- the multi-source additive HP model
- **Shield layer** (Section 11) -- the crafting expansion's second HP pool on the boss bar

### Forward references

- **[Mana & Ability System HLD](../../hld/mana-ability-system.md)** -- the production plan for formalizing mana, migrating all abilities to combo activation, and removing the ready-state system
- **[Action Bar HUD LLD](action-bar-hud.md)** -- the implemented HUD system (production quality)
- **[Ability System Design](ability-system.md)** -- the companion spike doc for ability design (combo activation, cast times, CC system, base abilities)

---

## Table of Contents

1. [Philosophy](#1-philosophy)
2. [Primary Stats](#2-primary-stats)
3. [Action Bar HUD Layout](#3-action-bar-hud-layout)
4. [Custom HP Implementation](#4-custom-hp-implementation)
5. [Defense Math](#5-defense-math)
6. [Secondary Combat Stats](#6-secondary-combat-stats)
7. [Loadout System](#7-loadout-system)
8. [Stat Passives](#8-stat-passives)
9. [Fitness Skill](#9-fitness-skill)
10. [HP Growth Sources](#10-hp-growth-sources)
11. [Design Constraints](#11-design-constraints)
12. [Future Work / Open Design Questions](#12-future-work--open-design-questions)

---

## 1. Philosophy

Stats should support abilities, not compete with them for player attention. A casual player should be able to ignore the numbers entirely and still have a meaningful experience. A hardcore player should find real levers to pull without being required to optimise.

Three rules:

- **One job per stat.** Every stat has a single, plainly describable effect. If it cannot be explained in one sentence to a new player, it is too complex.
- **Stats inform, abilities decide.** Stats set the floor and ceiling of what is possible; skill expression through abilities determines outcomes within that range.
- **The optimal build is never pure stats.** Loadout slots spent on stat passives are slots not spent on interesting abilities. This tension is structural, not enforced by tuning alone.

---

## 2. Primary Stats

Five stats form the primary system. Three are always visible in the action bar. Two live in inventory tooltips.

| Stat | Display | One-line description |
|------|---------|----------------------|
| **HP** | Action bar | How much damage you absorb before dying |
| **Mana** | Action bar | Resource consumed by combo abilities; regenerates passively |
| **Defense** | Action bar | Reduces incoming damage (feeds DR formula — see Section 4) |
| **Attack Power** | Tooltip | Scales outgoing melee and ability damage |
| **Mana Regen** | Tooltip | Passive mana recovery rate per tick |

Action bar display:
```
❤ 147/200   ✦ 180/220   ⚔ 640
```

**HP and Mana** show current/max. **Defense** shows the raw number — the actual percentage reduction is calculated internally via the DR formula. This keeps the display clean while preserving depth for players who want to understand the math.

**Attack Power and Mana Regen** are not shown constantly because they do not change during combat the way HP and Mana do. They are visible in the inventory or character sheet for gear comparison decisions.

---

## 3. Action Bar HUD Layout

The action bar is the primary real-time stat display. It uses a three-zone layout with pixel-width anchoring so that HP and Mana remain visually locked regardless of what the center zone displays.

### Zone Layout

```
[HP Zone]  [Left Pad]  [Center Zone]  [Right Pad]  [Mana Zone]
```

| Zone | Content | Example |
|------|---------|---------|
| HP (left) | `❤ current/max` | `❤ 147/200` |
| Center | Combo progress, status messages, or empty | `⬤ ⬤ ○` / `On Cooldown (33s)` / (empty) |
| Mana (right) | `✦ current/max` | `✦ 180/220` |

### Pixel-Width Anchoring

Minecraft's action bar is center-aligned and uses a proportional bitmap font. To keep HP and Mana at fixed screen positions:

1. The renderer computes the pixel width of the center zone content using a character-width lookup table for the default Minecraft font (most characters = 6px, space = 4px, `i`/`l` = 2px, etc.).
2. Padding spaces are inserted on both sides of the center content so that the total width of `leftPad + center + rightPad` equals a constant (`FIXED_CENTER_ZONE_WIDTH`).
3. Because `HP + fixedCenterZone + Mana` always sums to the same total pixel width, and the bar is center-aligned, HP and Mana never shift.

### Center Zone States

The center zone displays one of the following (in priority order):

| State | Display | Duration |
|-------|---------|----------|
| Combo in progress | `⬤ ⬤ ○` (gold/aqua circles) | Until combo completes, times out, or dead-ends |
| Cooldown failure | `On Cooldown (33s)` in red, countdown updates live | ~3 seconds (configurable), counts down each HUD tick |
| Insufficient mana | `Not Enough Mana` in red | ~3 seconds (configurable) |
| Idle | Empty (padding only) | Default state |

### Feedback Layering

Combo activation failures produce two layers of feedback:

- **Action bar (center zone):** Terse, fixed-width-safe, no ability name (ability names are unbounded and would break the pixel budget). Updates live for countdowns.
- **Chat message:** Sent once at the moment of failure. Includes the ability name and specifics (e.g., `Cleave is on cooldown! (33s remaining)` or `Not enough mana to use Cleave! (need 30, have 12)`).

### Mana as the Combo Activation Resource

Mana replaces vanilla hunger as the resource consumed by combo abilities. Each combo ability has a configurable `mana-cost`. The mana pool has a base maximum (default 220) and regenerates passively at a configurable rate (default 5/sec). Mana is tracked in memory per player with no database persistence in the PoC.

---

## 4. Custom HP Implementation

### The Problem

Vanilla HP is 20. Allowing HP to scale beyond 20 produces compressed hearts — dozens of tiny hearts that are visually unreadable. But keeping HP at exactly 20 means late-game players have the same survivability floor as new players, eliminating meaningful HP scaling.

### The Solution

`GENERIC_MAX_HEALTH` **never changes. It stays at 20.**

A custom HP pool is tracked per player separately. The vanilla heart display is used as a **percentage indicator**, not a literal HP readout. The action bar provides the real numbers.

```
customHP    = 147
maxCustomHP = 200

vanillaHP = (customHP / maxCustomHP) × 20 = 14.7  →  ~7 full hearts
```

Hearts always occupy the same space and always look clean. As a player's custom HP pool grows from 100 to 200, the hearts still show 0–10 — they just represent a larger underlying value.

### Implementation Requirements

**Take over the full damage pipeline.** All damage — melee, abilities, fall damage, environmental, potions — is intercepted, calculated against the custom HP pool, and then synced back to vanilla HP as a percentage. Vanilla damage events are cancelled; the plugin handles all HP math.

**Minimum vanilla HP floor.** When syncing, always use `max(1, percentage × 20)`. A player at 1/200 HP (0.5%) displays one half-heart, not zero — which would trigger a vanilla death event before the custom death check runs.

**Cancel vanilla regen entirely.** Vanilla regen ticks restore vanilla HP directly and would desync the percentage display. All regen — out-of-combat passive tick, Herbal Remedy procs, potions, abilities — is managed by the plugin and then synced.

**Intercept everything:**
- `EntityDamageEvent` / `EntityDamageByEntityEvent` — main damage pipeline
- `EntityRegainHealthEvent` — cancel and replace with custom regen
- Potion effects that restore HP — translate to custom pool
- Absorption hearts — intercept and translate or disable

**Death condition.** Death triggers when custom HP ≤ 0, not when vanilla HP reaches 0. The vanilla HP percentage sync must never reach 0 while the player is alive.

**Damage display numbers.** Vanilla floating damage numbers show small values since vanilla HP is always ≤ 20. Suppress vanilla damage indicators and emit custom ones showing real damage values.

---

## 5. Defense Math

### Formula

```
Damage Reduction % = Defense / (Defense + K)
```

`K` is a constant tuned during balancing. Reference points for two candidate values:

| Defense | DR % (K=500) | DR % (K=1000) |
|---------|-------------|---------------|
| 100 | 17% | 9% |
| 500 | 50% | 33% |
| 1000 | 67% | 50% |
| 2000 | 80% | 67% |
| 5000 | 91% | 83% |

Properties that make this formula correct:
- **Never reaches 100%** — no mathematically immune builds, no arbitrary hard cap
- **Early investment is efficient** — the first 500 Defense is worth more than the next 500. Encourages gearing for defense without making it mandatory.
- **Naturally self-limiting** — stacking infinite Defense approaches but never reaches immunity, so Armor Pen always has value against high-Defense targets

### Applying Damage

```
effectiveDefense = playerDefense × (1 - percentArmorPen) - flatArmorPen
dr = effectiveDefense / (effectiveDefense + K)
finalDamage = rawDamage × (1 - dr)
```

Order of operations: percentage armor pen reduces effective Defense first, then flat armor pen subtracts, then the DR formula runs.

---

## 6. Secondary Combat Stats

Secondary stats are not universal gear affixes. They come from specific ability passives or item affixes and are situational — strong in specific matchups, not mandatory in all builds. They do not appear in the action bar.

| Stat | Source | Effect |
|------|--------|--------|
| **Tenacity** | Ability passives, consumables | Reduces CC duration received. 50 Tenacity = 50% shorter CC. Does not eliminate CC. |
| **Armor Pen (Flat)** | Ability passives, item affixes | Ignores a fixed amount of target's Defense before DR calculation. Better against low-Defense targets. |
| **Armor Pen (%)** | Item affixes (Epic+) | Ignores a percentage of target's Defense before DR calculation. Better against high-Defense targets. |

**Why both types of Armor Pen:** Flat pen is a better value against lightly armored targets — 150 flat pen against a 200-Defense player is a massive proportion. Percentage pen scales with the target's investment and is the dedicated counter to tank builds. They are not interchangeable; they answer different problems.

**Visibility.** Secondary stats are visible in the character sheet and item tooltips. Players who care about matchups find them; players who do not are not burdened by them.

**Expansion pattern.** Other secondary stats — lifesteal, evasion, CDR — may be added by expansion packs following the same rules: sourced from specific abilities or item affixes, not universal gear slots, not in the action bar.

---

## 7. Loadout System

A loadout is a cross-skill selection of abilities drawn from everything the player has unlocked. It is not per-skill — a player freely mixes Swords, Unarmed, Axes, Herbalism, Fitness, and any other unlocked skill in the same loadout.

### Slot Structure

**3 active slots** — mapped to the three combo patterns (RRR, RRL, RLR). Each holds one combo-activated ability from any skill.

**5 passive slots** — always-on effects. Each holds one passive ability from any skill, including stat passives.

| Slot type | Count | Contains | Examples |
|-----------|-------|----------|---------|
| Active | 3 | Combo-activated abilities | Haymaker, Execute, Berserker's Cry, Nature's Grasp |
| Passive | 5 | Always-on passives or stat passives | Quick Reflexes, Bleed, Iron Will, Fitness HP passive |

### Core vs. Optional Passives

Some passives are the defining identity of a skill (e.g., Bleed for Swords, Iron Fist for Unarmed). Whether these require a passive slot or are always-on when a skill is invested in is determined per-skill at design time. Generally: the single defining identity passive of a skill should not cost a slot; supplementary synergy passives do.

### Build Decisions

With 5 passive slots and a large cross-skill pool of interesting passives available, players will never fit everything they have unlocked. This scarcity is intentional — every passive slot is a real decision:

- **Stat passive** (HP, Defense bonus) — raw survivability investment
- **Cross-skill synergy passive** — enables the combo loops active abilities rely on
- **Defensive reaction passive** — Quick Reflexes, Parry, Iron Curtain
- **CC utility passive** — Iron Will, Battle-Hardened, Grappler

A player who fills 2 of 5 passive slots with HP stat passives has made a deliberate identity choice and given up 2 synergy or utility passives to make it. That tradeoff should always be visible and felt.

---

## 8. Stat Passives

Stat passives are loadout items that provide a pure numerical stat bonus with no other effect. They allow players to reinforce a playstyle identity at the cost of a passive slot that could hold an interesting ability.

### Tier Structure

All stat passives follow the standard tier upgrade system:

| Tier | HP Example | Notes |
|------|-----------|-------|
| 1 | +20 HP | Base unlock |
| 2 | +27 HP | Requires investment to upgrade |
| 3 | +35 HP | Maximum |

### Design Rules

**Hard limit per stat.** Only a small number of stat passives exist for any given stat. For HP: 2 slottable HP passives exist in Fitness. That is the ceiling from stat passives regardless of how many skills the player has invested in.

**No multiplicative stacking.** Two maxed HP passives give +35 + +35 = +70. They do not compound with each other or with other HP sources multiplicatively.

**The optimal build is never all stat passives.** If a pure stat-stick build is demonstrably stronger than a mixed ability build, the numbers are too high. The ceiling from stat passives should represent a deliberate identity choice — the player who wants to be the tankiest possible — not a universally correct optimisation path.

**Design intent statement:** Stat passives reinforce identity, not efficiency. A player running zero stat passives and filling all 5 passive slots with synergy passives should have comparable power through a different means. Outcomes should differ; power levels should not diverge enough to make one approach obviously wrong.

---

## 9. Fitness Skill

Fitness is a combat-universal skill. It does not belong to a weapon type or playstyle — it levels through the act of taking damage, meaning any combat-engaged player develops it naturally regardless of what skills they use offensively. This makes it the extensible, skill-agnostic path to base survivability growth.

### Experience Source

Fitness XP is gained by taking damage from any source — mobs, players, environmental.

**Anti-farming guards:**
- **Minimum damage threshold** — hits below X damage grant no Fitness XP. Prevents tickling.
- **Per-source cooldown** — each entity can only grant Fitness XP once every N seconds. Prevents stationary farming partners.
- **Reduced XP from player damage** — PvP damage grants less Fitness XP than mob damage. Encourages organic PvE engagement as the primary levelling path.

### Innate HP Scaling

Every Fitness player gets this automatically — no slot cost, no decision required. Max HP scales continuously with Fitness level from level 1, contributing approximately **+25 HP at max level**.

The growth curve is sqrt-shaped: early Fitness levels yield meaningful HP gains; later levels yield smaller increments. This rewards early combat engagement without making the final levels feel mandatory.

### Milestone Passives

Unlocked automatically at level thresholds. No slot cost.

| Level | Passive | Effect |
|-------|---------|--------|
| 25 | **Threshold** | Hits below a minimum damage value are reduced further — a secondary DR floor specifically for chip damage and weak attacks |
| 50 | **Recovery** | Out-of-combat HP regen rate increases |
| 75 | **Conditioning** | Fitness base Defense contribution increases significantly |
| 100 | **Iron Constitution** | Both the innate HP scaling and base Defense contribution reach their maximum values |

### Slottable Stat Passives

Two HP stat passives are available in Fitness, each requiring a passive loadout slot:

| Passive | Tier 1 | Tier 2 | Tier 3 |
|---------|--------|--------|--------|
| **Endurance** | +20 HP | +27 HP | +35 HP |
| **Fortitude** | +20 HP | +27 HP | +35 HP |

A player who slots both at Tier 3 gains +70 HP from stat passives but has filled 40% of their passive loadout with pure stats. Three of their five passive slots remain for actual ability passives.

### Base Defense Contribution

Fitness level feeds a base Defense value that scales with level, independent of gear. This represents a veteran fighter's ability to roll with hits and tighten their guard — something learned through experience, not bought. The base Defense is added to gear Defense before the DR formula runs.

---

## 10. HP Growth Sources

HP is 100 at baseline for all players. Multiple sources contribute additively:

| Source | Max Contribution | How Earned |
|--------|----------------|------------|
| Base | 100 HP | Always present |
| Fitness innate | ~+25 HP | Level Fitness through taking damage |
| Fitness slottable passives (×2 at T3) | +70 HP | Invest 2 of 5 passive slots |
| Unarmed built-in HP growth | ~+25 HP | Level Unarmed |
| Passive nodes from other skills | +20–35 HP | Broad skill investment |
| Items (crafting expansion) | +20–50 HP | Gear progression |
| **Practical ceiling** | **~250–275 HP** | Full investment across all sources |

**The range matters more than the ceiling.** A new player at 100 HP and a veteran at 220 HP fight differently, but the veteran is not operating in a different game. The DR formula creates more survivability differentiation than the HP differential alone.

**Unarmed HP growth** comes from a built-in passive scaling with Unarmed level. Thematically: a barehanded fighter has no weapon to absorb blows and must condition their body to take hits directly. This stacks with Fitness innate HP. An Unarmed fighter who also levels Fitness is a natural tank-brawler archetype — they are developing both traits simultaneously through ordinary combat.

**Passive nodes from other skills** exist where thematically appropriate. A skill designer creating an expansion combat skill can include an HP node in that skill's passive set if it fits the skill's identity. The HP system reads aggregate stat values — it does not need to know which skills contributed them. This is the extensibility mechanism: new skills plug in without requiring changes to the HP system.

---

## 11. Design Constraints

### Stat Complexity Ceiling

Base McRPG targets 5 primary stats and 2–3 secondary stats. Expansion packs may add secondary stats following the same sourcing rules (from specific ability passives or item affixes, not as universal gear slots). No expansion should introduce new primary stats without a full design review — primary stats affect every player and every build.

### HP Ceiling Governance

The effective HP ceiling should be revisited if:
- One-shot kills become common between players at similar investment levels
- The gap between minimum and maximum HP makes early-game PvP feel hopeless
- DR tuning alone cannot resolve survivability imbalance

The two primary levers are item HP contributions (easiest to adjust post-launch) and the Fitness innate curve (affects the baseline all players share).

### The Shield Layer

The crafting expansion introduces a Shield pool from Epic+ gear — a second HP layer displayed on the boss bar. This stacks on top of the custom HP pool but is visually and mechanically distinct. Base McRPG does not include Shield.

Combined effective HP for a fully geared endgame player: ~250 custom HP (hearts, percentage display) + significant Shield pool (boss bar). These are displayed across two clearly distinct channels with different visual language. Players understand they have two layers without the displays conflicting.

---

## 12. Future Work / Open Design Questions

Items identified during the PoC stat HUD implementation that require design decisions before production:

### Mana Consumption Event

A cancellable `CombatStatConsumeEvent` should be fired before `CombatStatInstance.consume()` so third-party plugins can modify mana costs, cancel consumption, or implement mana-drain/mana-shield effects. The event should carry the `AbilityHolder`, the stat key, the requested amount, and allow modification of the effective cost.

### Registry Access Keys

`McRPGManagerKey.STAT` and `McRPGRegistryKey.COMBAT_STAT` constants are needed so addon developers can access `StatManager` and `CombatStatRegistry` through the standard `registryAccess()` pattern. Without these, the stat system has no supported public API surface for extensions.

### Action Bar Center Zone Priority

The action bar center content API on `McRPGPlayer` needs a priority or layering system so third-party plugins can write to the center zone without stomping McRPG's combo display (and vice versa). Higher-priority content wins; ties resolve by recency. This is required before any addon attempts to use the center zone for custom feedback messages.
