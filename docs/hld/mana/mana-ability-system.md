# Mana & Ability Activation System

> **Last Updated:** 2026-05-07
> **Status:** Phases 1–3 implemented; Phase 4 proposed
> **Scope:** Mana as universal activation resource, combo-only activation for all active abilities, ready-state removal, config consolidation, Parser-based formula scaling, balance framework

---

## Architecture Overview

The mana system replaces the legacy ready-state activation model with a unified combo-based activation path gated by mana. Every active ability is activated via click-combo sequences (RRR, RRL, RLR). Mana is consumed on activation (with a cancellable `PlayerStatConsumeEvent` fired before each consumption); a small anti-spam cooldown prevents accidental double-casts. Passive abilities retain their event-driven activation but gain optional mana cost support in the infrastructure. Player stats are registered through the `ContentExpansion` pack system and managed by `PlayerStatRegistry` (no separate `StatManager`).

```mermaid
flowchart TD
    subgraph input [Player Input]
        Click[Left/Right Click]
    end

    subgraph comboLayer [Combo System]
        CIL[OnComboInputListener]
        CM[ComboManager]
        PCS[PlayerComboState]
        CP[ComboPattern RRR/RRL/RLR]
    end

    subgraph activation [Activation Gate]
        CCE[ComboCompleteEvent]
        OCL[OnComboCompleteListener]
        ManaCheck{Mana >= Cost?}
        CDCheck{Cooldown Ready?}
    end

    subgraph resource [Resource System]
        CSR[PlayerStatRegistry]
        PCD[PlayerStatData]
        CSI[PlayerStatInstance Mana]
        CSCE[PlayerStatConsumeEvent]
    end

    subgraph ability [Ability Execution]
        CA[ComboActivatable.comboActivate]
        Cooldown[Apply Anti-Spam Cooldown]
    end

    subgraph display [HUD]
        DM[DisplayManager]
        ABHD[ActionBarHudDisplay]
        Renderer[ActionBarHudRenderer]
    end

    Click --> CIL
    CIL --> CM
    CM --> PCS
    PCS --> CP
    CP -->|"pattern complete"| CCE
    CCE --> OCL
    OCL --> CDCheck
    CDCheck -->|"on cooldown"| Feedback1[Action Bar + Chat Feedback]
    CDCheck -->|"ready"| ManaCheck
    ManaCheck -->|"insufficient"| Feedback2[Action Bar + Chat Feedback]
    ManaCheck -->|"sufficient"| CA
    CA --> Cooldown

    CSR --> PCD
    PCD --> CSI
    CSI -->|"fire PlayerStatConsumeEvent"| CSCE
    CSCE -->|"consume/tickRegen"| ManaCheck
    CSI -->|"current/max"| ABHD
    ABHD --> Renderer
    Renderer -->|"action bar frame"| DM
```

**HP display:** The action bar HP zone mirrors vanilla health directly (20/20 by default). The renderer is future-proofed to show custom HP values when a custom HP pool is introduced, but no custom HP pool exists in this system.

---

## Core Concepts

### 1. Mana Pool

Mana is a per-player resource pool tracked via `PlayerStatInstance` keyed by `McRPGPlayerStat.MANA`. It has a base maximum, a flat passive regeneration rate, and support for modifiers (flat and percentage bonuses from gear or abilities in the future).

**Base values:**

| Property | Value | Notes |
|----------|-------|-------|
| Base max mana | 100 | Lower than the spike's 220; tighter resource feel |
| Regen rate | 3/sec | Flat passive, always active |
| Regen driver | HUD tick | `ActionBarHudDisplay.tick()` calls `PlayerStatData.tickRegen()` |
| Persistence | Logout/save only | Restored on login; see Resolved Design Decisions |

**Design target:** A player should be able to cast 2-3 abilities in quick succession, then wait ~10-15 seconds to refill for another burst. With 100 max mana and 3/sec regen, a player who spends 60 mana on a burst needs ~20 seconds for a full refill or ~7 seconds to afford a 20-cost ability again.

**Modifier system:** `PlayerStatModifier` is an extensible class (not a record) keyed by `NamespacedKey` for third-party collision safety. The base class provides fixed flat/percent bonuses. Subclasses can override virtual methods for dynamic scaling (e.g., stacking modifiers) and time-based expiration:

- `getEffectiveFlatBonus()` / `getEffectivePercentBonus()` — base returns raw values; subclasses override for scaling (e.g., `perStack * currentStacks`)
- `tick(double secondsElapsed)` — no-op in base; subclasses use for duration countdown or stack falloff
- `isExpired()` — returns `false` in base; subclasses return `true` when duration elapses or stacks reach zero

Future subclasses (not yet implemented): `StackablePlayerStatModifier`, `TimedPlayerStatModifier`, `TimedStackablePlayerStatModifier`.

```
effectiveMax = (baseMana + sumEffectiveFlatBonuses) * (1 + sumEffectivePercentBonuses)
```

This is implemented in `PlayerStatInstance.getEffectiveMax()`, which calls the virtual methods on each modifier. `PlayerStatInstance.tickRegen()` also ticks all modifiers and auto-removes expired ones before applying regen.

**Configuration:**

```yaml
# config.yml
stats:
  mana:
    base-max: 100
    regen-per-second: 3
    minimum-ability-cost: 1
```

### 2. Combo Activation

All active abilities are activated exclusively via click-combo sequences. Three combo slots map to three input patterns:

| Slot | Pattern | Input Sequence |
|------|---------|----------------|
| 1 | RRR | Right, Right, Right |
| 2 | RRL | Right, Right, Left |
| 3 | RLR | Right, Left, Right |

All patterns start with Right so plain left-clicks (mining, combat) do not start a combo unintentionally.

**Slot assignment:** A player's `ComboActivatable` abilities are drawn from their loadout's ordered abilities. The Nth `ComboActivatable` in loadout order maps to slot N. This is deterministic and requires no per-player combo binding configuration.

**Timing window:** Configurable ticks between inputs before the sequence resets. Default 30 ticks (~1.5 seconds at 20 TPS).

**Allowed items:** Only configured held items (swords, axes, pickaxes, shovels, hoes, bows, crossbows, trident, mace, or empty hand) can register combo inputs. The list is reloadable.

**Configuration:**

```yaml
# config.yml
configuration:
  gameplay:
    combo:
      allowed-items:
        - WOODEN_SWORD
        - STONE_SWORD
        - IRON_SWORD
        - GOLDEN_SWORD
        - DIAMOND_SWORD
        - NETHERITE_SWORD
        # ... axes, pickaxes, shovels, hoes, bows, etc.
      timing:
        window-ticks: 30
      failure-feedback:
        sound: BLOCK_NOTE_BLOCK_BASS
        volume: 1.0
        pitch: 0.5
```

### 3. Activation Gates

When a combo pattern completes, `OnComboCompleteListener` enforces two gates in order:

1. **Cooldown check** -- if the ability implements `CooldownableAbility` and is on cooldown, deny with feedback (action bar countdown + chat message with ability name and remaining time).
2. **Mana check** -- call `PlayerStatInstance.consume(manaCost)`. If it returns false, deny with feedback (action bar "Not Enough Mana" + chat message with ability name, cost, and current mana).

If both gates pass, `comboActivate(abilityHolder)` fires and the anti-spam cooldown is applied.

**Anti-spam cooldown:** Most abilities have a 0.5-1 second cooldown purely to prevent lag-induced accidental double-casts. This is configured per-ability per-tier via the existing `cooldown` key in `tier-configuration`. Strong abilities (SerratedStrikes, OreScanner) can use longer cooldowns (5-30 seconds) as a balance lever.

**Feedback layering:**

| Channel | Content | Duration |
|---------|---------|----------|
| Action bar (center zone) | `On Cooldown (Xs)` or `Not Enough Mana` | ~3 seconds (configurable), countdowns update live |
| Chat | `{Ability} is on cooldown! (Xs remaining)` or `Not enough mana for {Ability}! (need X, have Y)` | One-shot at failure |
| Sound | Configurable failure sound | Immediate |

### 4. Mana Cost Configuration

Mana costs are defined in each ability's `tier-configuration` block within its skill config file, following the same `all-tiers` / `tier-N` override pattern that `cooldown`, `unlock-level`, and `pulse-radius` already use.

**All config values that vary by tier -- including `mana-cost` -- are read as strings and evaluated via the McCore `Parser` framework with `tier` as a variable.** This is the same approach `ConfigurableActiveAbility.getCooldown()`, `ConfigurableTierableAbility.getUnlockLevelForTier()`, and `MassHarvest.getRadius()` already use. Abilities that currently read tier-config values as raw integers or doubles (e.g., `getInt()` / `getDouble()`) must be migrated to `getString()` + `Parser` as part of this effort.

This means server owners have two ways to configure per-tier values:

1. **Formula in `all-tiers`** -- a single expression evaluated for any tier, including tiers above `amount-of-tiers`:

```yaml
# herbalism_configuration.yml (modern approach -- preferred)
ability-configuration:
  mass-harvest:
    enabled: true
    amount-of-tiers: 5
    tier-configuration:
      all-tiers:
        upgrade-point-cost: 1
        unlock-level: 1*tier
        pulse-radius: 5+(1.6*tier)
        cooldown: 30-(3.5*tier)
        mana-cost: 50-(7*tier)
```

2. **Explicit per-tier overrides** -- override individual tiers for hand-tuned curves:

```yaml
# swords_configuration.yml (explicit approach -- also valid)
ability-configuration:
  rage-spike:
    enabled: true
    amount-of-tiers: 5
    tier-configuration:
      all-tiers:
        upgrade-point-cost: 1
        cooldown: 1
        mana-cost: 50-(7*tier)
      tier-1:
        unlock-level: 50
        mana-cost: 35
      tier-3:
        unlock-level: 250
        mana-cost: 25
      tier-5:
        unlock-level: 500
        mana-cost: 15
```

Resolution order: tier-specific value wins if present; otherwise the `all-tiers` formula is evaluated with the current tier. This is the existing `ConfigurableTierableAbility` pattern -- no new resolution logic is needed.

**Overtier future-proofing:** Because `all-tiers` formulas accept any tier value, abilities implicitly support tiers above `amount-of-tiers` if a future system (e.g., temporary tier boosts or "overtier" buffs) passes a tier higher than 5. Explicit per-tier overrides for tiers 6+ are optional and will work if present. The ability code must use the formula resolution path rather than capping at `maxTier` when computing mana costs, cooldowns, or other tier-scaled values.

**Minimum mana cost floor:** A configurable floor (default `1`) is enforced after formula evaluation to prevent overtier formulas from producing zero or negative costs. For example, `50-(7*tier)` at tier 8 evaluates to `-6`, which would be clamped to the floor. The floor is defined globally in `config.yml`:

```yaml
# config.yml
stats:
  mana:
    base-max: 100
    regen-per-second: 3
    minimum-ability-cost: 1
```

The floor is applied in the `getManaCost()` resolution path after Parser evaluation: `Math.max(floor, (int) parser.getValue())`. Server owners can set it to `0` to allow free casts at extreme tiers if they choose.

**Parser backport:** As part of Phase 1, all ability config reads that currently use `getInt()` or `getDouble()` for tier-varying values must be migrated to the `getString()` + `Parser.setVariable("tier", tier)` pattern. The `ConfigurableActiveAbility.getCooldown()` and `MassHarvest.getRadius()` implementations are the reference for this pattern. This is a mechanical change -- the YAML format is already compatible since BoostedYAML stores unquoted numbers as valid strings.

**Tier scaling philosophy:** Mana cost decreases with tier. Tier 1 costs ~1.5x a "base" value; Tier 5 costs ~0.6x. Higher tiers feel more fluid, not just stronger. Server owners choose their preferred approach -- formulas for automatic scaling or explicit values for hand-tuned curves.

**Cost ranges (reference for balance):**

| Category | Mana Cost Range | Examples |
|----------|----------------|---------|
| Cheap utility | 10-15 | Gathering buffs, quick repositioning |
| Standard offensive | 20-30 | RageSpike |
| Powerful AoE/buff | 35-50 | SerratedStrikes, VerdantSurge, OreScanner |

### 5. Passive Mana Cost (Infrastructure)

The activation pipeline supports optional mana costs on passive abilities. No current passives use this -- it is pure infrastructure for future design space.

**Contract:** `PassiveAbility` (or equivalent interface point in the activation chain) gains a `getManaCost(AbilityHolder)` default method returning `0`. `AbilityListener#activateAbilities` checks this value before firing a passive ability. When cost is `> 0`, it attempts `PlayerStatInstance.consume(cost)` and skips the ability silently on failure (no feedback -- passive procs should not spam the player).

**Configuration:** A future passive could opt in by adding `mana-cost` to its tier-configuration or ability-configuration block. The pipeline requires no further changes.

### 6. HP Display

The action bar HP zone shows vanilla health directly:

```
❤ 20/20   [center content]   ✦ 87/100
```

`ActionBarHudDisplay` reads `Player.getHealth()` and `Player.getAttribute(GENERIC_MAX_HEALTH).getValue()` and renders them as integers. The current spike scales vanilla HP against a custom max -- this will be replaced with direct vanilla values.

The renderer's `buildFull()` method accepts `hpCurrent`/`hpMax` as parameters, so swapping to custom HP later requires only changing the values passed in, not the rendering pipeline.

### 7. Ready-State Removal

**Completed in Phase 2.** The legacy ready-state activation model has been fully removed.

**Deleted:**
- `ReadyAbility`, `ReadyData`, `SwordReadyData`, `MiningReadyData`, `HerbalismReadyData`, `WoodcuttingReadyData` — all ReadyData subclasses deleted alongside the base class
- All `EventReadyableComponent` infrastructure: `EventReadyableComponent`, `EventReadyableComponentAttribute`, `RightClickReadyComponent`
- `AbilityHolderReadyEvent`, `AbilityHolderUnreadyEvent` and their listeners
- Ready-state exception classes
- `AbilityListener#readyAbilities()` and all call sites
- Ready-state fields and methods from `AbilityHolder` and `BaseAbility`
- `RequireEmptyOffhandSetting` deprecated (`forRemoval = true`); removed from settings GUI registration

**Config removed:**
- `configuration.gameplay.require-empty-off-hand-to-ready` from `config.yml`
- All ready/unready localization keys from `LocalizationKey.java` and `en_abilities.yml`

**Abilities migrated:**
- `SerratedStrikes` — `ReadyAbility` removed, `ComboActivatable` added, `comboActivate()` implemented
- `RageSpike` — ready path removed, combo path kept, `unreadyHolder()` call removed
- `OreScanner` — ready path removed, combo path retained from Phase 1
- `MassHarvest` — ready path removed, combo path retained from Phase 1
- `VerdantSurge` — ready path removed; `ComboActivatable` added in Phase 3 with per-tier mana costs

---

## Current State (Spike Inventory)

The following was shipped as PoC quality code and needs formalization:

### Active Abilities (post-Phase 3 state)

Shockwave and Cleave were spike-only PoC abilities deleted in Phase 1. The remaining active abilities after Phase 3:

| Ability | Skill | Activation Path | Mana Source | Notes |
|---------|-------|-----------------|-------------|-------|
| RageSpike | Swords | Combo-only | Per-tier formula in `swords_configuration.yml` | Ready path removed in Phase 2; `MANA_COST` placeholder added in Phase 3 |
| SerratedStrikes | Swords | Combo-only | Per-tier formula in `swords_configuration.yml` | Migrated from ready-only in Phase 2; `MANA_COST` placeholder added in Phase 3 |
| OreScanner | Mining | Combo-only | Per-tier formula in `mining_configuration.yml` | Parser backport for `getRange()`, scan message localized in Phase 3 |
| MassHarvest | Herbalism | Combo-only | Per-tier formula in `herbalism_configuration.yml` | `MANA_COST` placeholder added in Phase 3 |
| VerdantSurge | Herbalism | Combo-only | Per-tier formula `"42-(5.5*tier)"` in `herbalism_configuration.yml` | `ComboActivatable` added in Phase 3 |

### Config Debt

- ~~`combo_configuration.yml` holds both system settings (timing, items) and per-ability params~~ — **resolved in Phase 1:** migrated to `config.yml` and skill config files; `ComboConfigFile.java` and `FileType.COMBO_CONFIG` deleted
- ~~Per-ability mana costs are flat values, not per-tier~~ — **resolved in Phase 1:** all costs read via `getString()` + Parser with `tier` variable
- `ComboManager` timeout is hardcoded to 14 ticks despite `TIMING_WINDOW_TICKS` route existing in config — **Phase 1 leftover; still pending**
- ~~`McRPGCombatStat.HEALTH` defaults to 200 max~~ — **resolved in Phase 1:** Health base value is vanilla 20
- ~~Combo ability names (`getName()`, `getDisplayName()`) are hardcoded strings~~ — **resolved in Phase 1:** stat display routed through localization
- ~~Shockwave and Cleave are spike-only PoC classes~~ — **deleted in Phase 1**
- ~~`require-empty-off-hand-to-ready` config key~~ — **removed in Phase 2**
- ~~Ready/unready localization keys and YAML entries~~ — **removed in Phase 2**

### Infrastructure (production quality)

- `PlayerStat` (abstract base, `stat/`), `ResourcePoolPlayerStat` / `FlatPlayerStat` / `ConfigurableResourcePoolPlayerStat` (`stat/impl/`), `PlayerStatInstance` / `PlayerStatData` / `PlayerStatModifier` (`stat/instance/`) — renamed from `CombatStat*`, reorganized into subpackages, fully tested. `StatManager` merged into `PlayerStatRegistry`. Modifiers are `NamespacedKey`-keyed classes with virtual methods supporting future stacking/timed subclasses.
- `ComboManager`, `ComboPattern`, `PlayerComboState`, `ComboInput` -- clean, tested. `ComboManager` supports third-party combo item registration via `registerAllowedItemSet()` and `addAllowedItem()`.
- `ActionBarHudDisplay`, `ActionBarHudRenderer`, `DisplayManager`, `FontWidthTable` -- production quality, fully tested, documented in [Action Bar HUD LLD](../lld/combat-rework/action-bar-hud.md). HUD uses player-aware localized stat display symbols.
- `OnComboInputListener`, `OnComboCompleteListener` -- production quality; cooldown-on-cancel bug fixed in Phase 3

---

## Config Consolidation

### Migration Plan

`combo_configuration.yml` is deleted. Its contents migrate to two destinations:

**System settings -> `config.yml`:**

| Source (`combo_configuration.yml`) | Destination (`config.yml`) |
|---|---|
| `stats.mana.base-max` | `stats.mana.base-max` |
| `stats.mana.regen-per-second` | `stats.mana.regen-per-second` |
| `combo.allowed-items` | `configuration.gameplay.combo.allowed-items` |
| `combo.timing.window-ticks` | `configuration.gameplay.combo.timing.window-ticks` |
| `combo.failure-feedback.*` | `configuration.gameplay.combo.failure-feedback.*` |

**Per-ability params -> skill config files:**

| Source | Destination |
|---|---|
| `combo.abilities.rage-spike.mana-cost` | `swords_configuration.yml` `ability-configuration.rage-spike` (merge) |
| `combo.abilities.ore-scanner.mana-cost` | `mining_configuration.yml` `ability-configuration.ore-scanner` (merge) |
| `combo.abilities.mass-harvest.mana-cost` | `herbalism_configuration.yml` `ability-configuration.mass-harvest` (merge) |

**Spike ability params discarded:** `combo.abilities.shockwave.*` and `combo.abilities.cleave.*` are not migrated -- those abilities are deleted.

**`hud_configuration.yml` stays.** It owns HUD rendering concerns (update interval, persistent pool toggle) and is explicitly scoped by the [Action Bar HUD LLD](../lld/combat-rework/action-bar-hud.md) as a separate file. No migration needed.

### Files Affected

- **Delete:** `src/main/resources/combo_configuration.yml`, `ComboConfigFile.java`, `FileType.COMBO_CONFIG`
- **Delete:** `Shockwave.java`, `Cleave.java`, and all related references (ability registration, events, localization keys, `ComboConfigFile` routes)
- **Modify:** `config.yml` -- add `stats` and `combo` sections
- **Modify:** `MainConfigFile.java` -- add Route constants for combo and stats settings
- **Modify:** `swords_configuration.yml` -- merge RageSpike mana cost into tiers
- **Modify:** `mining_configuration.yml` -- merge OreScanner mana cost into tiers
- **Modify:** `herbalism_configuration.yml` -- merge MassHarvest mana cost into tiers
- **Modify:** `SwordsConfigFile.java`, `MiningConfigFile.java`, `HerbalismConfigFile.java` -- add Route constants for new/migrated keys; remove Shockwave/Cleave routes
- **Modify:** All 3 remaining combo ability classes -- update config reads from skill config routes instead of `ComboConfigFile` routes
- **Modify:** `PlayerStatRegistry` (formerly `StatManager` / `CombatStatRegistry`) -- `StatManager` merged; `PlayerStatData` resolves registry on demand
- **Modify:** `ComboManager` -- read allowed items and timing from `MainConfigFile` routes; use config value instead of hardcoded 14-tick timeout
- **Modify:** `OnComboCompleteListener` -- read failure feedback from `MainConfigFile` routes

### Health Stat Removal

`stats.health.base-max` is removed from config entirely. The action bar HP zone reads vanilla health directly -- there is no custom HP pool to configure. `McRPGPlayerStat.HEALTH` registration is retained in the registry (the `PlayerStatInstance` for health provides display metadata and future modifier support) but its base max reflects vanilla 20, not a configurable custom value.

---

## Extension Points

### For Third-Party Plugins

- **Custom `PlayerStat` registration:** Plugins register additional player stats via `PlayerStatRegistry` (accessible through `registryAccess()` as `McRPGRegistryKey.PLAYER_STAT`). The registry owns stat definitions; per-player `PlayerStatData` is constructed via a no-arg constructor that resolves the registry on demand -- there is no separate `StatManager`.
- **`PlayerStatConsumeEvent`:** A cancellable event fired before every `PlayerStatInstance.consume()` call. Carries the `AbilityHolder`, the stat key (`NamespacedKey`), the requested amount, and allows modification of the effective cost or outright cancellation. This enables third-party plugins to implement mana-drain, mana-shield, cost reduction, or stat consumption logging. Fired on the combo activation path (`OnComboCompleteListener`), the passive mana-check path (`AbilityListener#activateAbilities`), and any future consumption site.
- **`PlayerStatContentPack`:** Player stats are registered through the `ContentExpansion` system via a `PlayerStatContentPack`, following the same pattern as `AbilityContentPack`, `StatisticContentPack`, and other content packs. Third-party expansions add custom stats by including a `PlayerStatContentPack` in their `getExpansionContent()`.
- **Custom `ComboActivatable` abilities:** Third-party abilities implementing `ComboActivatable` work with the combo system automatically if added to a player's loadout.
- **Extensible combo allowed-item list:** `ComboManager` supports two extensibility paths for third-party plugins to add custom held items that can trigger combo inputs: (1) `registerAllowedItemSet(ReloadableSet<CustomItemWrapper>)` for config-backed item sets that update on reload, and (2) `addAllowedItem(CustomItemWrapper)` for individual programmatic entries. Both use concurrent collections (`CopyOnWriteArrayList`, `CopyOnWriteArraySet`) for thread safety.
- **`PlayerStatModifier` subclassing:** `PlayerStatModifier` is a non-final class with virtual methods (`getEffectiveFlatBonus()`, `getEffectivePercentBonus()`, `tick()`, `isExpired()`). Third-party plugins can subclass it to implement custom modifier behaviors (stacking, timed decay, conditional bonuses) and register them on any `PlayerStatInstance` via `addModifier()`.
- **Stat display localization:** `PlayerStat` display names and symbols are resolved through the localization system via convention-based routes (`stat.<key>.display-name`, `stat.<key>.display-symbol`). Third-party stats get localized automatically if locale entries exist; otherwise the constructor-provided fallback string is used.
- **`ComboCompleteEvent`:** Already cancellable -- plugins can intercept combo completions.
- **Action bar center content:** The priority-based slot system (`ActionBarSlotSetEvent`, `ActionBarSlotClearEvent`) allows plugins to write to the center zone without stomping McRPG's feedback.

### For Content Expansions

New abilities added via `ContentExpansion` that implement `ComboActivatable` are automatically combo-eligible. They read mana costs from their own skill config files following the same `tier-configuration.mana-cost` pattern.

New player stats are added via `PlayerStatContentPack` in a `ContentExpansion`. The stat definition, default base value, regen rate, and resource-pool flag are all declared in the `PlayerStat` object and registered into `PlayerStatRegistry` during expansion loading.

---

## Ability Inventory (Migration Reference)

### All Active Abilities (5 total)

| Ability | Skill | State (post-Phase 3) | Remaining Work |
|---------|-------|----------------------|----------------|
| **RageSpike** | Swords | Combo-only ✓ | Balance pass (Phase 4) |
| **SerratedStrikes** | Swords | Combo-only ✓ | Balance pass (Phase 4) |
| **OreScanner** | Mining | Combo-only ✓ | Balance pass (Phase 4) |
| **MassHarvest** | Herbalism | Combo-only ✓ | Balance pass (Phase 4) |
| **VerdantSurge** | Herbalism | Combo-only ✓ | Balance pass (Phase 4) |

**Deleted spike abilities:** Shockwave and Cleave were PoC-only combo abilities with no tiers, no localization, and hardcoded names. They are deleted as part of Phase 1 cleanup rather than promoted to production.

### All Passive Abilities (13 total -- no migration needed)

| Ability | Skill | Notes |
|---------|-------|-------|
| Bleed | Swords | Core identity passive |
| EnhancedBleed | Swords | Tiered bleed enhancement |
| DeeperWound | Swords | Tiered bleed duration |
| Vampire | Swords | Tiered lifesteal on bleed |
| ExtraOre | Mining | Proc on ore break |
| ItsATriple | Mining | Tiered triple-drop chance |
| RemoteTransfer | Mining | Tiered remote chest transfer |
| InstantIrrigation | Herbalism | Passive with cooldown |
| TooManyPlants | Herbalism | Proc on harvest |
| ExtraLumber | Woodcutting | Proc on log break |
| HeavySwing | Woodcutting | Tiered AoE wood break |
| DryadsGift | Woodcutting | Tiered bonus drops |
| NymphsVitality | Woodcutting | Tiered biome bonus |

All passives retain their current event-driven activation. The mana infrastructure supports optional `mana-cost` on passives but none are configured in this phase.

---

## Mana Balance Framework

### Design Constraints

1. **Burst window:** 2-3 abilities before mana runs dry on a full pool
2. **Recovery cadence:** ~10-15 seconds from empty to usable; ~33 seconds for full refill
3. **Tier reward:** Higher tiers are meaningfully cheaper, making progression feel like mastery
4. **Gathering parity:** Non-combat abilities (OreScanner, MassHarvest) have comparable costs to combat abilities -- mana is a universal resource, not a "combat currency"

### Reference Balance Table

| Ability | Category | T1 Cost | T3 Cost | T5 Cost | Cooldown |
|---------|----------|---------|---------|---------|----------|
| RageSpike | Standard offensive | 35 | 25 | 15 | 1s anti-spam |
| SerratedStrikes | Powerful buff | 50 | 35 | 22 | 15s (buff duration) |
| OreScanner | Powerful utility | 45 | 30 | 20 | 20s (scan CD) |
| MassHarvest | Powerful utility | 45 | 30 | 20 | 15s (harvest CD) |
| VerdantSurge | Standard utility | 35 | 25 | 15 | 10s (regen CD) |

These are reference values for the HLD. Final tuning happens during the Phase 4 balance pass, with actual values set in per-skill config files.

### Regen Model

Flat 3/sec passive regen, always active regardless of combat state.

**Future consideration:** In-combat vs. out-of-combat regen rates (e.g., 1/sec in combat, 5/sec out) would create more interesting mana management decisions. This requires a combat state tracker (not currently implemented) and is deferred. When built, the config would extend:

```yaml
stats:
  mana:
    base-max: 100
    regen-per-second: 5          # out-of-combat rate
    combat-regen-per-second: 1   # in-combat rate
    combat-timeout-seconds: 8    # seconds since last damage to leave combat
```

---

## Resolved Design Decisions

### Mana Pool Persistence

Mana is persisted on **player logout and full player saves only** -- not on every mana change. On login, the player's mana pool is restored to the last saved value. If no saved value exists (new player or pre-persistence data), the pool initializes to the configured base max.

This avoids per-tick write overhead while still preventing the "log out and return with full mana" exploit on PvP servers. Crash recovery is an accepted loss -- the pool refills fast enough (33 seconds from empty) that it is a minor inconvenience, not a gameplay issue.

**Implementation:** `PlayerStatDAO` stores per-player stat values generically (keyed by `NamespacedKey`). Written during `McRPGPlayer` save events (logout, periodic server save). Read during `McRPGPlayer` construction to seed `PlayerStatInstance.setCurrent()` after `new PlayerStatData()` resolves the registry and sets base values from config.

### Loadout Active Slot Count

The active loadout slot count is **hardcoded to 3**, matching the 3 combo patterns (RRR, RRL, RLR). The `max-active-loadout-size` config key is removed. If a future system adds more combo patterns (e.g., 4-input sequences), both the combo pattern set and the hardcoded slot count would be updated together as a coordinated change. This is explicitly not planned -- 3 slots is the design target.

---

## Open Design Questions

_None at this time. All prior questions have been resolved (see Resolved Design Decisions above)._

---

## Proposed Implementation Phases

Each phase gets its own LLD when implementation begins.

### Phase 1: Infrastructure & Config Cleanup

- Migrate combo system settings from `combo_configuration.yml` to `config.yml`
- Migrate per-ability params from `combo_configuration.yml` to respective skill config files
- Delete `combo_configuration.yml`, `ComboConfigFile.java`, `FileType.COMBO_CONFIG`
- Delete `Shockwave.java`, `Cleave.java`, and all related references (ability registration in `McRPGExpansion`, events, localization keys, `ComboConfigFile` routes)
- Add `mana-cost` as a recognized key in `tier-configuration` for all abilities, supporting both formula strings and explicit values
- Backport all tier-config reads (`getInt()` / `getDouble()`) to `getString()` + `Parser.setVariable("tier", tier)` pattern (reference: `ConfigurableActiveAbility.getCooldown()`, `MassHarvest.getRadius()`)
- Wire `ComboActivatable.getManaCost()` to read from tier-config instead of flat values, using the Parser formula path
- Wire optional mana cost into passive ability activation path (default 0, no current passives use it)
- Merge `StatManager` into `CombatStatRegistry` (see Extension Points); register combat stats via expansion pack system
- Fire `CombatStatConsumeEvent` before all `CombatStatInstance.consume()` calls (see Extension Points)
- Update base mana values: 100 max, 3/sec regen
- Update HP display: renderer shows vanilla health directly
- Fix `ComboManager` to read timeout from config instead of hardcoded 14 ticks
- ~~Update `CombatStatRegistry` (formerly `StatManager`)~~ — **done in Phase 1:** `StatManager` merged into `PlayerStatRegistry`; `PlayerStatData` resolves registry on demand
- Add mana persistence: `CombatStatDAO` (or column on existing player table) to store `current_mana`; write on logout/full save, restore on login
- Hardcode active loadout slot count to 3; remove `max-active-loadout-size` config key

### Phase 2: Ready-State Removal & Swords Migration ✓ (Implemented)

- ~~Delete `ReadyAbility`, `ReadyData`, `SwordReadyData`, all readying components~~ — done; all `ReadyData` subclasses deleted in this phase (not retained as dead code)
- ~~Remove `AbilityListener#readyAbilities()` and all calls to it~~ — done
- ~~Remove `require-empty-off-hand-to-ready` config~~ — done; localization keys and YAML entries also removed
- ~~Port SerratedStrikes to `ComboActivatable` with per-tier mana costs + moderate cooldown~~ — done; `LivingEntity` removed from `SerratedStrikesActivateEvent`
- ~~Clean up RageSpike: remove ready path, keep combo path, add per-tier mana costs~~ — done; `RageSpikeComponents.java` deleted
- ~~Verify passive Swords abilities work correctly~~ — verified; Bleed/EnhancedBleed/DeeperWound/Vampire do not implement `ManaAbility`
- **Note:** VerdantSurge was temporarily unactivatable after this phase (ready path removed; `ComboActivatable` added in Phase 3)

### Phase 3: Mining & Herbalism Migration ✓ (Implemented)

- ~~Port OreScanner: remove ready path, keep combo, add per-tier mana costs in mining config~~ — done (Phase 1/2); parser backport for `getRange()` and scan message localization done in Phase 3
- ~~Port MassHarvest: remove ready path, keep combo, add per-tier mana costs in herbalism config~~ — done (Phase 1/2); `MANA_COST` placeholder added in Phase 3
- ~~Port VerdantSurge: add `ComboActivatable`, `comboActivate()`, per-tier mana costs in herbalism config~~ — done; formula `"42-(5.5*tier)"`
- ~~InstantIrrigation (passive with cooldown) -- no changes needed~~ — verified unaffected
- ~~Add localization keys for all migrated abilities~~ — done; `ORE_SCANNER_BLOCK_DETECTED` key added
- ~~Add `MANA_COST` to `AbilityItemPlaceholderKeys` and wire into all 5 combo abilities~~ — done
- ~~Fix `OnComboCompleteListener` cooldown-on-cancel bug~~ — done; early return after mana refund when `!activated`

### Phase 4: Woodcutting & Balance Pass

- Port Woodcutting actives (currently no active abilities in code; design doc lists Timber Rush, Log Javelin -- implementation if ready, otherwise skip)
- Full balance pass on mana costs across all skills
- Validate 100 base mana + 3/sec regen supports intended cast frequency through playtesting
- Tune anti-spam cooldowns (0.5-1s default; longer for strong abilities)
- Document final balance reference table
- Update steering docs and skill system documentation (`CLAUDE.md`, `.cursor/rules/*.mdc`, ability-system spike doc) to incorporate the mana system as a core concept: mana cost as an activation gate, formula-based tier scaling, `CombatStatConsumeEvent`, `CombatStatContentPack`, and the removal of the ready-state model. This is done in Phase 4 so the documentation reflects the finalized balance paradigm rather than interim values

---

## Related Documents

- **[Stat System Design Spike](../lld/combat-rework/stat-system.md)** -- the original design spike this HLD formalizes (partially shipped)
- **[Ability System Design Spike](../lld/combat-rework/ability-system.md)** -- companion spike doc for ability design, CC system, cast times, base abilities
- **[Action Bar HUD LLD](../lld/combat-rework/action-bar-hud.md)** -- the implemented HUD system (production quality)
- **[Crafting System Design Spike](../lld/combat-rework/crafting-system.md)** -- references mana potions as a craftable product
