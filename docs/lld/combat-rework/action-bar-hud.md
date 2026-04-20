# Action Bar HUD System Design

> **Status:** Implemented
> **Scope:** Per-player action bar rendering, priority-based center content, HP/mana zones, third-party extensibility
> **Related LLDs:** [Stat System](stat-system.md) (HP/mana visual contract), [Ability System](ability-system.md) (cooldown feedback sources)

---

## Table of Contents

1. [Goals](#1-goals)
2. [Problem & Context](#2-problem--context)
3. [Architecture Overview](#3-architecture-overview)
4. [Layout & Rendering](#4-layout--rendering)
5. [Center Content Priority System](#5-center-content-priority-system)
6. [Lifecycle & Tick Driver](#6-lifecycle--tick-driver)
7. [Extensibility (Events)](#7-extensibility-events)
8. [Configuration](#8-configuration)
9. [Persistent Pool Toggle (Disabled Mode)](#9-persistent-pool-toggle-disabled-mode)
10. [Performance](#10-performance)
11. [Testing Strategy](#11-testing-strategy)
12. [Backlog / Future Work](#12-backlog--future-work)

---

## 1. Goals

- Provide a **stable, non-jittery** action bar showing HP and mana at all times by default, with the hotbar pulled in tightly between the two zones.
- Host a **single shared center zone** any feature (combo dots, ability cooldowns, XP gains, safe-zone flashes, third-party integrations) can write into without stomping each other.
- Make every non-trivial center message **dynamic** (e.g. cooldown counters tick down in real time, not a static snapshot).
- Keep the HUD **extensible**: third-party plugins can observe and mutate center slots via Bukkit events, and the persistent HP/mana layer can be disabled entirely without breaking the rest of the system.
- Preserve the **OOP collaborator pattern** used elsewhere in McRPG — no static utility helpers owning mutable state.

Non-goals:

- Replacing the boss-bar or titles pipelines (handled by sibling `PlayerDisplay` subclasses).
- Supporting a scoreboard surface (explicitly out of scope — the display type registry is generic but scoreboard is not planned).

---

## 2. Problem & Context

Before this work, McRPG's action bar was rendered by a pair of static utility methods called from three different places (combo ticker, XP display, safe-zone listener). Each caller could blindly overwrite the frame the others had just sent, producing three visible issues:

- **HP "jumping"** when combo dots rendered — the dots were measured in characters, not pixels, so HP slid horizontally every dot update.
- **HP/mana glued to the hotbar edges**, which flanked combos/XP by a visually uncomfortable gap.
- **Static cooldown text** ("Ability on cooldown for 5s") which never updated during its lifetime.

On top of the visual bugs, the lack of any shared ownership of the center zone made it difficult to add new message sources cleanly. That directly blocked work in the combat-rework ticket `TODO(#214)`.

This LLD captures the replacement system, centred on a per-player `ActionBarHudDisplay` and a priority-based slot model inside it.

---

## 3. Architecture Overview

```
  ┌──────────────────────────┐       ┌──────────────────────────┐
  │   DisplayManager         │       │   HudConfigFile          │
  │  (shared coordinator)    │◄─────►│  action-bar.*            │
  │                          │       └──────────────────────────┘
  │   • ActionBarHudRenderer │
  │   • FontWidthTable       │       ┌──────────────────────────┐
  │   • ReloadableContent<Boolean>   │  Bukkit PluginManager    │
  │                          │ ────► │  (custom events)         │
  │   get/has/set/remove/    │       │  ActionBarSlotSetEvent   │
  │   getOrCreateDisplay     │       │  ActionBarSlotClearEvent │
  │   getOrCreateActionBarHud│       └──────────────────────────┘
  └────────────┬─────────────┘
               │ delegates container ops
               ▼
        ┌──────────────┐
        │ McRPGPlayer  │
        │ Map<Class<?  │      ┌────────────────────────────────┐
        │  extends PD>,│ ──►  │ ActionBarHudDisplay            │
        │     PD>      │      │   TreeMap<priority, content>   │
        └──────────────┘      │   drainToWinner(tick)          │
                              │   tick(tick, seconds)          │
                              └────────────────────────────────┘
                                        │
                                        ▼
                              ┌────────────────────────────────┐
                              │ ActionBarCenterContent         │
                              │ • TimedCenterContent           │
                              │ • IndefiniteCenterContent      │
                              │ • CountdownCooldownCenterContent│
                              │ • <third-party impls>          │
                              └────────────────────────────────┘
```

Key collaborators:

- **`DisplayManager`** — owns shared resources (`ActionBarHudRenderer`, `FontWidthTable`, the `ReloadableContent<Boolean>` persistent-pool flag). Offers a generic `get/has/set/remove/getOrCreateDisplay` API and the HUD-specific `getOrCreateActionBarHud(player)` convenience that most features use.
- **`McRPGPlayer#displays`** — a `Map<Class<? extends PlayerDisplay>, PlayerDisplay>` storing each player's registered displays keyed by their base class. `DisplayManager` does not own this state; it delegates to `McRPGPlayer` for every container operation.
- **`ActionBarHudDisplay`** — the per-player display that owns the center-zone slot map and the tick loop that composes HP/mana + center content into the final frame.
- **`ActionBarCenterContent`** — the pluggable content interface with three first-party implementations (`TimedCenterContent`, `IndefiniteCenterContent`, `CountdownCooldownCenterContent`). Third parties can supply their own.
- **`ActionBarHudRenderer`** — stateless collaborator responsible for composing `Component`s. It holds a reference to the `FontWidthTable` and the persistent-pool flag but no per-player state.
- **`FontWidthTable` / `MinecraftDefaultFontWidthTable`** — precise pixel-width table for vanilla Minecraft's font, covering ASCII plus the Unicode symbols this HUD uses. Enables fixed-pixel padding rather than character-count approximations.

---

## 4. Layout & Rendering

The action bar is divided into three horizontal zones:

```
 [HP XX/YY] [      center content (padded)      ] [MN XX/YY]
 ◄────────► ◄─────────── 96 px ───────────────► ◄─────────►
```

- **HP zone** — red `♥ current/max`.
- **Mana zone** — aqua `✦ current/max`.
- **Center zone** — fixed `96 px` wide, padded with spaces so any content is horizontally centred. Fixed width is what stops HP/mana from sliding as center length changes.

`ActionBarHudRenderer` exposes two composition entry points:

- `buildFull(hpCurrent, hpMax, hpSymbol, manaCurrent, manaMax, manaSymbol, centerContent, centerWidth)` — full frame, used when the persistent pool is enabled.
- `buildCenterOnly(centerContent)` — center-only frame, used when the pool is disabled and there is content to show.

Both variants take an explicit `centerContentWidth`. Callers pass the value cached on the winning `ActionBarCenterContent` (see [Performance](#10-performance)), so the renderer never re-flattens a component just to measure it.

---

## 5. Center Content Priority System

The slot map lives on `ActionBarHudDisplay` as:

```java
TreeMap<Integer, ActionBarCenterContent> slots = new TreeMap<>(Comparator.reverseOrder());
```

- **Keys** are single-digit integers `1..4`, exposed as named constants on `CenterContentPriority`:

  | Constant                          | Value | Typical caller                          |
  |-----------------------------------|-------|-----------------------------------------|
  | `AMBIENT_FEEDBACK`                | `1`   | XP gain messages                        |
  | `COMBO_STATE`                     | `2`   | Active combo dots (indefinite)          |
  | `SAFE_ZONE_TRANSITION`            | `3`   | Safe-zone entry/exit flash              |
  | `ABILITY_FEEDBACK`                | `4`   | Ability cooldown / not-enough-mana      |

- Reverse ordering means `TreeMap#entrySet().iterator()` yields highest priority first.
- Each slot holds exactly one `ActionBarCenterContent`. `setSlot(priority, content)` replaces any existing content at that priority after firing `ActionBarSlotSetEvent`.

### Resolve algorithm (`drainToWinner`)

On every tick / resolve call:

1. Walk the slot map from highest to lowest priority.
2. Ask the current entry's content for `render(currentTick)`.
3. If `render` returns a component → that's the winner; **stop**.
4. If `render` returns empty → evict the entry, fire `ActionBarSlotClearEvent`, continue to the next priority.
5. If the walk ends without a winner → return `null` (no content this tick).

This gives two important emergent behaviours:

- **Natural revelation.** If a higher-priority timed message expires (say `ABILITY_FEEDBACK`), the next tick automatically reveals a lower-priority survivor (say `COMBO_STATE` dots) without any explicit "restore" logic.
- **Lazy eviction.** Expired slots don't need a dedicated sweep — they are cleared in the same pass that picks the winner.

### Content shapes

- `TimedCenterContent(component, expiryTick)` — renders `component` until `currentTick >= expiryTick`, then renders empty. Use for one-shot messages.
- `IndefiniteCenterContent(component)` — renders `component` forever. Use for persistent state (combo dots). Callers are responsible for clearing the slot when the state ends.
- `CountdownCooldownCenterContent(textFormat, remainingSecondsSupplier, expiryTick)` — renders a live-counting "X seconds" message. Memoizes the rendered component per remaining-seconds value to keep allocations bounded even on a 2-tick HUD cadence.

---

## 6. Lifecycle & Tick Driver

- `McRPGListenerRegistrar` schedules a recurring task at `hud.action-bar.update-interval-ticks` (default 2 ticks, 10 Hz) that calls `DisplayManager#tickDisplays`.
- `DisplayManager#tickDisplays` iterates every online player, materialises their `ActionBarHudDisplay` if the persistent pool is enabled, snapshots their displays into a reusable `ArrayList` buffer, and invokes `tick` on any `TickablePlayerDisplay`.
- `ActionBarHudDisplay#tick`:
  1. Ticks `PlayerCombatData` regen (mana regen piggybacks on the HUD cadence).
  2. Calls `drainToWinner` to get the active content, if any.
  3. Depending on the persistent-pool flag, builds either a full frame or a center-only frame and sends it via `Audience#sendActionBar`.
  4. If the flag is off *and* there's no content, emits a single empty component on the *transition* frame so the last frame fades correctly, then goes silent.

Displays removed from the player's map mid-tick remain safe because the iterator is backed by the snapshot buffer, not the live map.

---

## 7. Extensibility (Events)

Third-party plugins integrate by:

- **Listening to `ActionBarSlotSetEvent`** — fired before any `setSlot` write. Listeners can:
  - `setCancelled(true)` to veto the write (previous content, if any, remains).
  - `setNewContent(...)` to substitute a wrapped / translated / decorated variant before the write lands.
- **Listening to `ActionBarSlotClearEvent`** — fired after a slot is evicted (either by `clearSlot` or by lazy eviction during `drainToWinner`). Use it to react to content disappearing (analytics, cleanup).
- **Implementing `ActionBarCenterContent` directly** — plugins can register their own content types against any priority. The contract is just `render(tick)` and optional `getPixelWidth(widths, tick)` for caching.

McRPG's own call sites do not listen to these events. The cancel/mutate contract exists purely for third-party use.

---

## 8. Configuration

HUD configuration lives in its own file, `hud_configuration.yml`, separated from combo and other domain files so the HUD can be reloaded and reasoned about independently:

```yaml
config-version: 1

action-bar:
  update-interval-ticks: 2
  persistent-pool-display: true
```

- `update-interval-ticks` controls how often `DisplayManager#tickDisplays` fires. Lower = smoother, higher = cheaper.
- `persistent-pool-display` toggles continuous HP/mana rendering (see next section).

Both values are consumed via the McCore `ReloadableContent` utility so `/mcrpg reload` picks them up without a restart.

---

## 9. Persistent Pool Toggle (Disabled Mode)

When `persistent-pool-display` is `false`:

- HP and mana are **not** rendered on the action bar at all by default.
- Center-zone content (combo dots, XP, cooldown, safe-zone, third-party) continues to surface. The HUD renders only those frames via `buildCenterOnly`.
- On the tick where center content disappears, `ActionBarHudDisplay#tick` sends exactly one empty `Component` to clear the previous line, then goes quiet until new content arrives. This lets Minecraft's native action-bar auto-fade handle the visual decay without lingering text.
- `DisplayManager#tickDisplays` skips the "eagerly create HUD for every online player" branch — HUDs are created lazily the first time a caller writes a slot, matching the behaviour of XP, cooldowns, etc.

Flipping the toggle at runtime is safe because `ReloadableContent<Boolean>` is consulted at tick time, not at HUD construction time.

---

## 10. Performance

Three optimisations keep the HUD cheap even at the 2-tick / 10 Hz default cadence:

1. **Reusable snapshot buffer.** `DisplayManager#tickDisplays` owns a single `ArrayList<PlayerDisplay>` and passes it to `McRPGPlayer#snapshotDisplaysInto(Collection)` each pass, clearing between players. No per-tick list allocation.
2. **Cached content pixel widths.** Every `ActionBarCenterContent` can memoize its rendered width via `getPixelWidth(FontWidthTable, long)`. The first-party implementations all cache (immutable content → cache indefinitely; countdown → cache keyed by remaining-seconds). `ActionBarHudDisplay#tick` asks the winner for its cached width and passes it to `ActionBarHudRenderer#buildFull`, so the renderer never flattens components for measurement.
3. **Memoized countdown rendering.** `CountdownCooldownCenterContent#render` memoizes both the rendered `Component` and the computed width by `remainingSeconds`. A 5-second countdown at 10 Hz therefore produces 5 `Component` allocations total, not 50.

These let the HUD hold `O(onlinePlayers)` allocations per tick in the steady state, with the only per-tick allocation being the HP/mana stat-zone `Component.text(...)` calls inside the renderer — which are unavoidable because those values change every tick.

---

## 11. Testing Strategy

Tests live under `src/test/java/us/eunoians/mcrpg/display/...` and follow the project-wide "Given / When / Then" `@DisplayName` convention with `action_outcome_whenCondition` method names.

Coverage map:

| Behaviour                                                                 | Test class                              |
|---------------------------------------------------------------------------|-----------------------------------------|
| Font width math (ASCII, bold, Unicode symbols)                            | `FontWidthTableTest`                    |
| Fixed-pixel center padding independent of content length                  | `ActionBarHudRendererTest`              |
| Persistent-pool default when `ReloadableContent` resolves to `null`       | `ActionBarHudRendererTest`              |
| Priority resolution, lazy eviction, clearing                              | `ActionBarHudDisplayTest`               |
| `ActionBarSlotSetEvent` cancel and `setNewContent` replace paths          | `ActionBarHudDisplayTest`               |
| `tick()` frame mode (full / center-only / clear-frame)                    | `ActionBarHudDisplayTest`               |
| Countdown live tick-down                                                  | `CountdownCooldownCenterContentTest`    |
| Timed content expiry                                                      | `TimedCenterContentTest`                |
| Indefinite content never expiring                                         | `IndefiniteCenterContentTest`           |
| `McRPGPlayer` display container contract                                  | `McRPGPlayerDisplayContainerTest`       |
| `DisplayManager` generic API + `getOrCreateActionBarHud`                  | `DisplayManagerTest`                    |

Time-dependent tests use the bootstrap-provided `TimeProvider` spy (`Mockito.when(timeProvider.now()).thenReturn(...)` with an `@AfterEach` `Mockito.reset(timeProvider)`) rather than constructing their own clocks.

---

## 12. Backlog / Future Work

- **`DisplayManager#getAllDisplays(player)` API** — iterate every registered display for a player without allocating a snapshot. Low priority; tracked in the backlog.
- **Generic per-surface renderer interface** — if another visual surface (e.g. title) needs the same multi-source slot pattern, promote `ActionBarHudDisplay`'s resolve/eviction logic into a shared base. Defer until at least one additional consumer exists.
- **Per-player HUD preferences** — a player setting to pick a layout variant (numeric vs. bar-style HP). Gated behind data-pack / setting work.
