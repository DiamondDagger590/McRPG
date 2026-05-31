# Low-Level Design: UnlockCondition System (LLD-5)

**Status:** Implemented
**Date:** 2026-05-29
**Last Updated:** 2026-05-29
**HLD Reference:** [Riptide Guardian HLD](../../hld/riptide-guardian/riptide_guardian.md), Section 7
**Scope:** Registry-backed `UnlockConditionType` system (unified prototype + configured instance, matching `QuestObjectiveType`), built-in types shipped via McRPG's native `ContentExpansion`, third-party-extensible content pack, `UnlockableAbility` refactor, config-driven composition, current-progress display, rendered-lore contract, GUI integration, login-time sweep

---

## Table of Contents

1. [Overview](#1-overview)
2. [Existing Infrastructure](#2-existing-infrastructure)
3. [Design Decisions](#3-design-decisions)
4. [UnlockConditionType Interface](#4-unlockconditiontype-interface)
5. [Registry and Resolution Manager](#5-registry-and-resolution-manager)
6. [Built-In Types](#6-built-in-types)
7. [Config Shape and Rendered Lore](#7-config-shape-and-rendered-lore)
8. [UnlockableAbility Refactor](#8-unlockableability-refactor)
9. [TierableAbility Boundary](#9-tierableability-boundary)
10. [Auto-Unlock Semantics](#10-auto-unlock-semantics)
11. [Current-Progress Display](#11-current-progress-display)
12. [Server-Owner Display Hints](#12-server-owner-display-hints)
13. [Empty-Display Startup Warning](#13-empty-display-startup-warning)
14. [Migration](#14-migration)
15. [GUI Integration](#15-gui-integration)
16. [Login-Time Unlock Sweep](#16-login-time-unlock-sweep)
17. [Localization Keys](#17-localization-keys)
18. [Bootstrap Registration & Content Pack](#18-bootstrap-registration--content-pack)
19. [PAPI Soft-Dependency Handling](#19-papi-soft-dependency-handling)
20. [Edge Cases & Graceful Degradation](#20-edge-cases--graceful-degradation)
21. [Test Plan](#21-test-plan)
22. [File Manifest](#22-file-manifest)
23. [Future LLD Notes](#23-future-lld-notes)

---

## 1. Overview

`UnlockableAbility` currently exposes two skill-coupled methods:

```java
int getUnlockLevel();
boolean checkIfAbilityCanBeUnlocked(SkillHolder skillHolder, Skill skill);
```

These bake in the assumption that the only way to unlock an ability is to reach a level in some skill. Skill books already broke that assumption — books unlock abilities by consumption, not by hitting a level threshold. Server owners want more: "unlock at Swords level 250 **or** buy it from Epic Crates," "unlock once you've mined 10,000 blocks," "unlock when your economy balance crosses 50,000." None of these fit a single integer.

This LLD replaces the skill-coupled methods with a **registry-backed, content-pack-extensible condition type system**, modeled directly on the existing `QuestObjectiveType` / `QuestRewardType` pattern. The shape:

- **`UnlockConditionType`** is a single interface that serves *both* as the registered prototype (parses YAML into a configured copy of itself) and as the configured, evaluable instance (`isMet`, `getDisplayDescription`, `getProgress`). This is the exact pattern `QuestObjectiveType` uses — see [Section 3.2](#32-one-interface-not-two-matching-questobjectivetype). The registered no-arg instance carries empty defaults; `parseConfig(Section)` returns a new instance of the same type with the parsed fields set.
- **McRPG ships the six built-in types through its own native `ContentExpansion`** — `McRPGExpansion` — using a `UnlockConditionTypeContentPack`, the exact same path third-party expansions use. There is no internal back door: the native built-ins are registered the same way someone else's `MyCoolPlugin` registers its types ([Section 18](#18-bootstrap-registration--content-pack)).
- **`UnlockableAbility.getUnlockConditions()`** returns a `List<UnlockConditionType>`. The top-level list is **OR** — meeting *any* path unlocks the ability. AND is expressed by wrapping children in the `mcrpg:all_of` composite.

Server owners compose conditions in YAML like building blocks, using **hard-coded, named paths** (friendly to non-technical owners) rather than typed arrays:

```yaml
ability-configuration:
  vampire:
    unlock-conditions:
      swords-mastery:
        type: mcrpg:skill_level
        skill: mcrpg:swords
        level: 250
      epic-crates:
        type: mcrpg:display_hint
        text: "<body>Can be unlocked from <primary>Epic Crates<body>!"
```

**This LLD produces code.** All interfaces, classes, the registry, the resolution manager, the content pack, GUI changes, localization, and tests described here are implementation-ready.

### Why this matters now

The skill book system currently has no clean way to render the unlock requirement for a skill-book ability in the ability info GUI. Today the GUI calls `getUnlockLevel()` and prints "Reach <skill> Level N" — but a book-only ability has no skill level. The `mcrpg:display_hint` type gives the GUI a stable, descriptive answer ("Obtain from Riptide Guardian"), and the broader type system means server owners can advertise *their own* unlock paths ("Epic Crates!") that McRPG has no programmatic hook for.

It also unblocks open issue **#220** ("Check unlock conditions on player login") — a generic OR-sweep at login replaces the scattered level checks.

### What this LLD does NOT cover

| Out of scope | Reason |
|---|---|
| Per-tier upgrade gates (`TierableAbility.getUnlockLevelForTier(int)`) | Tier upgrades have their own mechanism — `AbilityUpgradeQuestSource` and `AbilityUpgradeQuestAttribute`. The level check inside that flow is a quest precondition, not an "unlock condition." See [Section 9](#9-tierableability-boundary). |
| `mcrpg:quest_complete` / `mcrpg:achievement` condition types | The type system accepts them trivially (register a new `UnlockConditionType`), but the achievement system does not yet exist and quest-as-unlock-source needs its own LLD. The composites and registry make these additive. |
| Defining brand-new abilities purely from config | This LLD lays the groundwork (registry-backed extensible types, `parseConfig`, content-pack registration, composability) but does not deliver config-authored abilities. See [Section 23](#23-future-lld-notes). |
| Persistence of `UnlockCondition` state | Conditions are pure functions of holder state. They carry no per-player mutable state and need no DB rows. |

---

## 2. Existing Infrastructure

### 2.1 The pattern this LLD mirrors

The quest system already implements exactly the shape this LLD needs. We copy it deliberately so the codebase has one consistent extensibility idiom.

| Class | Location | What we copy |
|---|---|---|
| `QuestObjectiveType` | `quest/objective/type/` | `McRPGContent` interface, `getKey()`, `parseConfig(Section)` returning a configured copy of itself (prototype pattern), built-ins + third-party registration |
| `QuestObjectiveTypeRegistry` | `quest/objective/type/` | `implements Registry<T>`, `register` / `get` / `getOrThrow` / `isRegistered` / `getRegisteredKeys` |
| `QuestObjectiveTypeContentPack` | `expansion/content/` | `extends McRPGContentPack<T>` — content-pack registration via `ContentExpansion` |
| `BlockBreakObjectiveType` | `quest/objective/type/builtin/` | No-arg base ctor for registry, private ctor holding parsed config, `parseConfig` returns a new configured instance, `getExpansionKey()` → `McRPGExpansion.EXPANSION_KEY` |

### 2.2 McRPG classes touched or used

| Class | Location | Role in LLD-5 |
|---|---|---|
| `UnlockableAbility` | `ability/impl/type/` | Refactored — `getUnlockLevel()` / `checkIfAbilityCanBeUnlocked()` removed, replaced by `getUnlockConditions()` + `getDefaultUnlockConditions()` returning `List<UnlockConditionType>` |
| `TierableAbility` | `ability/impl/type/` | Default `getDefaultUnlockConditions()` derives a single configured `SkillLevelUnlockConditionType` from the tier-1 unlock level for `SkillAbility` tierables |
| `ConfigurableTierableAbility` | `ability/impl/type/configurable/` | Provides the tier-1 unlock level the default derives from |
| `AbilityUnlockedAttribute` | `ability/attribute/` | Canonical "is unlocked" source of truth — unchanged. Conditions answer *eligibility*; this answers *achieved* |
| `AbilityUnlockEvent` | `event/ability/` | Still fired the same way after the refactor |
| `OnAbilityUnlockListener` | `listener/ability/` | Unlock messaging + loadout auto-add — unchanged |
| `OnSkillLevelUpListener` | `listener/skill/` | Calls `getUnlockLevel()` today → calls `isAnyConditionMet(holder)` |
| `AbilityLoreAppender` | `builder/item/ability/` | Fills `{ability-unlock-level}` today → renders the OR-list of condition descriptions |
| `AbilitySortType` | `gui/ability/` | Sorts by `getUnlockLevel()` → sorts by best-condition progress |
| `SkillBookConsumeListener` | `listener/item/` | Sets `AbilityUnlockedAttribute` directly. Continues to bypass conditions (see [3.5](#35-books-bypass-conditions-they-dont-satisfy-them)) |
| `McRPGLocalizationManager` | `localization/` | Resolves condition descriptions through the player's locale chain |
| `McRPGDisplayDecimalFormatter` | `localization/` | Formats `<current>` / `<required>` numbers per the player's locale |
| `McRPGMethods` | `util/` | `applyPapi(String, OfflinePlayer)` for the PAPI condition; `parseNamespacedKey(String)` for parsing config keys |
| `PlayerStatisticData` (McCore) | accessed via `McRPGPlayer.getStatisticData()` | `getLongValue(NamespacedKey)` for the statistic condition |
| `McRPGExpansion` | `expansion/` | Registers the built-in `UnlockConditionType`s via a new content pack |
| `LocalizationKey` | `configuration/file/localization/` | Gains the condition-display route constants |

---

## 3. Design Decisions

### 3.1 Registry-backed, content-pack-extensible types

`UnlockConditionType` is registered by `NamespacedKey` in a `UnlockConditionTypeRegistry`, distributed via a `UnlockConditionTypeContentPack`, exactly like quest objective/reward types. Server owners reference types by key in YAML; `ContentExpansion`s add new types.

**Rationale.** The previous draft hard-coded conditions in Java and explicitly forbade a registry. That made the common server-owner request — "unlock this from my crate plugin," "gate it behind a PAPI stat" — impossible without a code change. The quest system already proved the registry idiom works and is understood by the team and third-party developers. Reusing it gives unlock conditions the same extensibility for free and keeps one mental model in the codebase.

### 3.2 One interface, not two — matching `QuestObjectiveType`

`UnlockConditionType` is a **single** interface carrying both the prototype concerns (`getKey`, `parseConfig`, `serializeConfig`, `getExpansionKey`) and the instance concerns (`isMet`, `getDisplayDescription`, `getDisplayLabel`, `getProgress`, `isDisplayOnly`). The registered no-arg instance is the "base prototype" with empty defaults; `parseConfig(Section)` returns a new instance of the same type with its fields populated. Java-authored defaults construct configured instances directly via a public constructor that takes the same fields `parseConfig` would set. Composites hold `List<UnlockConditionType>` of already-configured children.

**Rationale.** This is the pattern `QuestObjectiveType` already uses across the codebase (see `BlockBreakObjectiveType` — no-arg base ctor, private configured ctor, `parseConfig` returns a new configured copy of itself), and unifying gives us the same idiom for free. An earlier draft of this LLD split prototype and configured instance into two interfaces for a "cleaner contract." On review the split bought nothing concrete: composites work the same with `List<UnlockConditionType>` of configured children, Java defaults work the same with a public constructor, and the only real upside — preventing `isMet()` from being callable on the unconfigured registry prototype — is solved trivially by each impl returning `false` when its config fields are empty (the same way `BlockBreakObjectiveType.processProgress` handles an empty `validBlocks`). The split's cost (an extra interface, a parallel `condition/` directory, a `getType()` method on every instance) wasn't worth it. One interface, one registry, one content-pack type — same shape as quest objectives.

**Implication for the unconfigured base.** Every built-in's `isMet` checks for its key fields being empty/null and returns `false` if so. Calling `isMet` on the registry prototype is harmless: it reports "not met" and renders a degraded but stable label. No footgun, but also no spurious unlocks.

### 3.3 Top-level list is OR; AND is a composite

`getUnlockConditions()` returns a list whose semantics are **any-of**: the ability unlocks when *any* condition is met. To require multiple conditions together, an author or server owner wraps them in `mcrpg:all_of`.

**Rationale.** The overwhelmingly common case is "there are several independent ways to get this ability" (reach a level, OR consume a book, OR buy from a crate). OR-by-default makes that the zero-ceremony case. AND is rarer and is made explicit by a named composite, so the combination logic is encoded once in config rather than smeared across every caller. The GUI renders one OR-list; composites render as nested groups.

### 3.4 Java defaults are *replaced* by config, and the replacement is logged

A programmatic ability provides `getDefaultUnlockConditions()`. If the ability's config declares an `unlock-conditions` section, the parsed list **replaces** the Java default entirely, and McRPG logs a warning recording that the override happened.

**Rationale (per the product owner's decision).** Replace-on-override is the least surprising semantic for a server owner: "what I wrote in YAML is what I get." Merging would force a `remove:`/`disable:` sub-syntax to undo a default, which is exactly the non-technical-unfriendly complexity we are avoiding. The warning exists because a silent full-replace can hide mistakes (e.g. an owner who meant to *add* a path and accidentally dropped the skill-level default); the log line names the ability and the count so the owner can confirm the override was intended.

### 3.5 Books bypass conditions, they don't satisfy them

A `mcrpg:display_hint` describing a book source always returns `isMet() = false`. When the player consumes a book, `SkillBookConsumeListener` flips `AbilityUnlockedAttribute` directly and fires `AbilityUnlockEvent`. The condition is never consulted during consumption.

**Rationale.** Modeling "the player has a book in hand" as a *met* condition would force the condition to inspect inventory and re-evaluate on every interact — an enormous footprint for what is fundamentally a display string. Treating the hint as "here is how you'd obtain this" gives the GUI a stable answer that doesn't flicker as inventory changes, while the actual unlock flows through the canonical `AbilityUnlockedAttribute`. The same reasoning applies to *any* externally-driven source ("Epic Crates") — McRPG advertises it but does not own the unlock mechanism.

### 3.6 One `display_hint` type for all externally-driven sources

Rather than a book-specific type, McRPG ships a single `mcrpg:display_hint` type: a display-only, never-met condition carrying either a translatable `locale-key` or an inline `text` MiniMessage string. "Obtain from Riptide Guardian" and "Can be unlocked from Epic Crates!" are both instances of it.

**Rationale.** From the condition's perspective these are identical: display-only, never met by McRPG, unlocked (if at all) by some external mechanism. Collapsing them into one type avoids a proliferation of near-identical classes. If book-specific behavior is ever needed (e.g. a clickable "where do books drop?" tooltip), a dedicated `mcrpg:skill_book_hint` can be registered alongside without disturbing anything — that is the whole point of the registry.

### 3.7 Conditions are pure read-only views over `AbilityHolder` state

A condition owns no per-player state and never writes to the database. `isMet(holder)` and `getProgress(holder)` derive everything from the holder's existing state. Configured `UnlockConditionType` instances are immutable and shared across all holders.

**Rationale.** Ability objects are shared singletons (CLAUDE.md: "No ability state stored on the ability object"). The configured condition list is *config*, not per-holder state — it lives on the resolution manager's cache, exactly like other reloadable config. This keeps conditions trivially thread-safe to evaluate against any stable holder reference.

### 3.8 Display is locale-aware and shows current progress; logic is not locale-coupled

`getDisplayDescription(McRPGPlayer)` / `getDisplayLabel(McRPGPlayer)` take a player so the localization manager resolves text in the player's locale chain **and** so the description can interpolate the player's `<current>` value. `isMet(AbilityHolder)` / `getProgress(AbilityHolder)` take the broader holder — pure logical checks usable for any future non-player holder.

**Rationale.** Display is human-facing and benefits from "(currently 187/250)" feedback; logic is not and must remain evaluable for non-player holders. Numbers in the description route through `McRPGDisplayDecimalFormatter` per the project's locale-typography rule.

### 3.9 Conditions never self-fire unlocks

A condition reports current state only. There is no `onMet` callback. The login sweep and `OnSkillLevelUpListener` *observe* met conditions and fire `AbilityUnlockEvent`.

**Rationale.** Decouples evaluation from side-effects. The same condition is evaluated for display (GUI, no unlock should fire), at login (sweep, may fire unlock), and on level-up (may fire unlock). If conditions self-fired, every read site would need a dry-run flag.

### 3.10 Progress is a rendering hint, not a contract

`getProgress(holder)` returns `[0.0, 1.0]` for progress bars. Binary conditions (`display_hint`) return `0.0` until met and `1.0` when met. Callers must not infer "almost unlocked" for correctness.

---

## 4. UnlockConditionType Interface

**New file:** `src/main/java/us/eunoians/mcrpg/ability/unlock/UnlockConditionType.java`

One interface, both prototype and configured instance — exactly the `QuestObjectiveType` pattern. The registered no-arg instance carries empty/default fields; `parseConfig(Section)` returns a new instance of the same concrete type with its fields populated. Java-authored defaults bypass `parseConfig` via a public configured constructor on each impl.

```java
package us.eunoians.mcrpg.ability.unlock;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.expansion.content.McRPGContent;

/**
 * A type of unlock condition that gates an {@link us.eunoians.mcrpg.ability.impl.type.UnlockableAbility}.
 * <p>
 * Mirrors the {@link us.eunoians.mcrpg.quest.objective.type.QuestObjectiveType} pattern: a base
 * (unconfigured) instance is registered in the {@link UnlockConditionTypeRegistry};
 * {@link #parseConfig(Section)} is called once per config entry to produce a new immutable
 * configured instance of the same concrete type. Java-authored defaults skip {@code parseConfig}
 * and use a public constructor that accepts the same fields the YAML would set.
 * <p>
 * A configured instance answers three independent questions:
 * <ul>
 *   <li>{@link #isMet(AbilityHolder)} — is the holder currently eligible? Drives the login
 *       sweep and the skill level-up flow.</li>
 *   <li>{@link #getDisplayDescription(McRPGPlayer)} / {@link #getDisplayLabel(McRPGPlayer)} —
 *       how is the requirement rendered, including the player's current progress?</li>
 *   <li>{@link #getProgress(AbilityHolder)} — what fraction is satisfied, for progress bars?</li>
 * </ul>
 * <p>
 * Calling {@code isMet} on the unconfigured registry prototype is harmless: every built-in
 * checks for empty config fields and returns {@code false} ({@code getDisplayDescription}
 * renders a degraded but stable label). The prototype is never expected to be evaluated; this
 * defensive contract simply prevents accidents.
 * <p>
 * Extends {@link McRPGContent} so types are distributable through the
 * {@link us.eunoians.mcrpg.expansion.ContentExpansion} system — McRPG's built-ins are
 * registered through the native {@code McRPGExpansion} via a
 * {@link us.eunoians.mcrpg.expansion.content.UnlockConditionTypeContentPack}, the same path
 * third-party expansions use.
 */
public interface UnlockConditionType extends McRPGContent {

    /**
     * Unique key identifying this condition type (e.g. {@code mcrpg:skill_level}).
     *
     * @return the namespaced key
     */
    @NotNull
    NamespacedKey getKey();

    /**
     * Parses one condition's type-specific config into a new configured instance of this
     * concrete type. The section is the body under a single named condition entry — the
     * {@code type} key has already been consumed by the resolution manager and is guaranteed
     * to match {@link #getKey()}. Composite types ({@code mcrpg:all_of} / {@code mcrpg:any_of})
     * recurse via the {@link UnlockConditionManager}.
     *
     * @param section the config section for this entry
     * @return a configured instance of the same concrete type
     * @throws UnlockConditionParseException if the section is missing required keys or
     *         contains invalid values
     */
    @NotNull
    UnlockConditionType parseConfig(@NotNull Section section);

    /**
     * Inverse of {@link #parseConfig(Section)} — writes this configured instance back into a
     * config section. Default implementation throws; types that support admin-tool
     * serialization (the groundwork for config-authored abilities) override it.
     *
     * @param section the destination section to populate
     */
    default void serializeConfig(@NotNull Section section) {
        throw new UnsupportedOperationException(
                "UnlockConditionType " + getKey() + " does not support serializeConfig");
    }

    /**
     * Whether the holder currently satisfies this configured condition. Must be pure — must
     * not mutate holder state or schedule side-effects. Never throws; failure modes (missing
     * skill, PAPI absent, called on the unconfigured registry prototype) return {@code false}.
     *
     * @param holder the holder to evaluate against
     * @return {@code true} if the holder meets the requirement
     */
    boolean isMet(@NotNull AbilityHolder holder);

    /**
     * Full localized description for lore / tooltip rendering, resolved through the player's
     * locale chain and interpolating the player's current progress via the {@code <current>}
     * placeholder where the type supports it.
     *
     * @param player the player whose locale chain and state drive rendering
     * @return the localized description component
     */
    @NotNull
    Component getDisplayDescription(@NotNull McRPGPlayer player);

    /**
     * Short label for compact rendering (sidebar entries, sort hints). Defaults to the
     * description.
     *
     * @param player the player whose locale chain and state drive rendering
     * @return the localized label component
     */
    @NotNull
    default Component getDisplayLabel(@NotNull McRPGPlayer player) {
        return getDisplayDescription(player);
    }

    /**
     * Progress toward the requirement in {@code [0.0, 1.0]}. Binary conditions return
     * {@code 0.0} until met, {@code 1.0} when met. Rendering-only.
     *
     * @param holder the holder to evaluate against
     * @return progress fraction
     */
    default double getProgress(@NotNull AbilityHolder holder) {
        return isMet(holder) ? 1.0 : 0.0;
    }

    /**
     * Whether this condition is purely informational — it can never be met by McRPG
     * (e.g. {@code mcrpg:display_hint}). Used by the empty-display startup warning and by
     * the GUI to suppress the progress bar on a hint.
     *
     * @return {@code true} if this condition is display-only
     */
    default boolean isDisplayOnly() {
        return false;
    }
}
```

### 4.1 UnlockConditionParseException

**New file:** `src/main/java/us/eunoians/mcrpg/ability/unlock/UnlockConditionParseException.java`

A `RuntimeException` thrown by `parseConfig` when a section is malformed (missing `skill`, non-numeric `level`, unknown nested `type`, etc.). Caught by the resolution manager, which logs the offending ability + condition id and skips that single entry rather than aborting startup ([Section 20](#20-edge-cases--graceful-degradation)).

---

## 5. Registry and Resolution Manager

### 5.1 UnlockConditionTypeRegistry

**New file:** `src/main/java/us/eunoians/mcrpg/ability/unlock/UnlockConditionTypeRegistry.java`

A direct analogue of `QuestObjectiveTypeRegistry` — `implements com.diamonddagger590.mccore.registry.Registry<UnlockConditionType>` with `register` / `get(NamespacedKey)` / `getOrThrow(NamespacedKey)` / `isRegistered(NamespacedKey)` / `getRegisteredKeys()` / `registered(UnlockConditionType)`. Registered under a new `McRPGRegistryKey.UNLOCK_CONDITION_TYPE`.

```java
public class UnlockConditionTypeRegistry implements Registry<UnlockConditionType> {

    private final Map<NamespacedKey, UnlockConditionType> types = new HashMap<>();

    public void register(@NotNull UnlockConditionType type) {
        NamespacedKey key = type.getKey();
        if (types.containsKey(key)) {
            throw new IllegalStateException("UnlockConditionType already registered with key: " + key);
        }
        types.put(key, type);
    }

    @NotNull
    public Optional<UnlockConditionType> get(@NotNull NamespacedKey key) {
        return Optional.ofNullable(types.get(key));
    }

    @NotNull
    public UnlockConditionType getOrThrow(@NotNull NamespacedKey key) {
        UnlockConditionType type = types.get(key);
        if (type == null) {
            throw new IllegalArgumentException("No UnlockConditionType registered with key: " + key);
        }
        return type;
    }

    public boolean isRegistered(@NotNull NamespacedKey key) {
        return types.containsKey(key);
    }

    @NotNull
    public Set<NamespacedKey> getRegisteredKeys() {
        return Set.copyOf(types.keySet());
    }

    @Override
    public boolean registered(@NotNull UnlockConditionType type) {
        return types.containsKey(type.getKey());
    }
}
```

### 5.2 UnlockConditionManager (resolution + caching + warnings)

**New file:** `src/main/java/us/eunoians/mcrpg/ability/unlock/UnlockConditionManager.java`

A `Manager` registered under `McRPGManagerKey.UNLOCK_CONDITION`. It owns:

- **Resolution.** `resolve(UnlockableAbility)` reads the ability's `unlock-conditions` config section. If present and non-empty → parse it (logging the Java-default override warning, [3.4](#34-java-defaults-are-replaced-by-config-and-the-replacement-is-logged)). Otherwise → use `ability.getDefaultUnlockConditions()`.
- **Caching.** Results are cached by ability `NamespacedKey`. The cache is the home for the resolved config-state (keeping ability singletons stateless per CLAUDE.md). `reload()` clears the entire cache; `resolveAll()` repopulates it eagerly afterward. This is a manager-level cache, **not** per-field `ReloadableContent`. `ReloadableContent` is designed for a single `(YamlDocument, Route, callback)` triple — one config file, one path, one parsed value. The unlock condition cache is cross-cutting: it aggregates named sections from *multiple* skill config files with a per-ability Java-default fallback, so the `ReloadableContent` model doesn't fit. The manager's `reload()` / `resolveAll()` pair is the correct granularity.
- **Recursive parsing.** `parseSection(Section)` turns one `unlock-conditions` (or composite `conditions`) section into a `List<UnlockConditionType>`, used both at the top level and by the composite types.
- **Startup validation.** `resolveAll()` is called once during bootstrap (after types are registered, abilities are registered, and configs are loaded) to populate the cache and emit the [empty-display warning](#13-empty-display-startup-warning).

```java
@NotNull
public List<UnlockConditionType> resolve(@NotNull UnlockableAbility ability) {
    return cache.computeIfAbsent(ability.getAbilityKey(), key -> {
        Optional<Section> sectionOptional = getUnlockConditionsSection(ability);
        if (sectionOptional.isPresent() && !sectionOptional.get().getRoutesAsStrings(false).isEmpty()) {
            List<UnlockConditionType> fromConfig = parseSection(sectionOptional.get());
            if (!ability.getDefaultUnlockConditions().isEmpty()) {
                logger.warning(() -> "Ability " + key + " declares unlock-conditions in config; "
                        + "this REPLACES its " + ability.getDefaultUnlockConditions().size()
                        + " programmatic default condition(s).");
            }
            return List.copyOf(fromConfig);
        }
        return List.copyOf(ability.getDefaultUnlockConditions());
    });
}

@NotNull
public List<UnlockConditionType> parseSection(@NotNull Section parent) {
    List<UnlockConditionType> conditions = new ArrayList<>();
    for (Object rawId : parent.getRoutesAsStrings(false)) {
        String conditionId = String.valueOf(rawId);
        Optional<Section> entryOptional = parent.getOptionalSection(conditionId);
        if (entryOptional.isEmpty()) {
            continue;
        }
        Section entry = entryOptional.get();
        NamespacedKey typeKey = McRPGMethods.parseNamespacedKey(entry.getString("type"));
        if (typeKey == null) {
            logger.warning("Unlock condition '" + conditionId + "' is missing a 'type' key; skipping.");
            continue;
        }
        Optional<UnlockConditionType> typeOptional = typeRegistry.get(typeKey);
        if (typeOptional.isEmpty()) {
            logger.warning("Unknown unlock condition type '" + typeKey + "' for entry '"
                    + conditionId + "'; skipping. Is the providing expansion installed?");
            continue;
        }
        try {
            conditions.add(typeOptional.get().parseConfig(entry));
        } catch (UnlockConditionParseException e) {
            logger.log(Level.WARNING, "Failed to parse unlock condition '" + conditionId
                    + "' of type " + typeKey + "; skipping.", e);
        }
    }
    return conditions;
}
```

**Why a manager and not fields on the ability.** Conditions need the type registry (a global lookup) to parse, need reload invalidation, need a single place to emit the override/empty warnings, and must not live as mutable state on the shared ability singleton. A `Manager` is the project's blessed home for exactly this kind of per-system, registry-accessed, reloadable state — and it lets `UnlockableAbility` remain a pure interface (no required base class) for third-party authors.

---

## 6. Built-In Types

All six built-ins live under `ability/unlock/builtin/`. Each is a single class — the prototype + configured form combined — following the exact shape of `BlockBreakObjectiveType`: a no-arg base ctor (for registry registration), a private configured ctor (used by `parseConfig` and by `Java-author` callers via a sibling public constructor), and pure-function `isMet` / `getDisplayDescription` / `getProgress` that safely no-op when the type's config fields are empty.

### 6.1 `mcrpg:skill_level` — SkillLevelUnlockConditionType

Behavior-preserving migration target for every ability that used `getUnlockLevel()`.

**Config keys:** `skill` (namespaced, default `mcrpg:<owning skill>`), `level` (int).
**Met when:** the `SkillHolder`'s level in `skill` ≥ `level`.
**Progress:** `min(1.0, current / level)`.
**Display placeholders:** `<skill>` (colored via `getColoredName(player)`), `<required>` (the level), `<current>` (the player's current level).

```java
public final class SkillLevelUnlockConditionType implements UnlockConditionType {

    public static final NamespacedKey KEY =
            new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "skill_level");

    private final NamespacedKey skillKey;
    private final int requiredLevel;

    /** Registry base instance — unconfigured prototype. */
    public SkillLevelUnlockConditionType() {
        this(null, 0);
    }

    /** Public configured constructor — used by Java-authored defaults (e.g. TierableAbility). */
    public SkillLevelUnlockConditionType(@Nullable NamespacedKey skillKey, int requiredLevel) {
        this.skillKey = skillKey;
        this.requiredLevel = requiredLevel;
    }

    @NotNull
    @Override
    public NamespacedKey getKey() {
        return KEY;
    }

    @NotNull
    @Override
    public UnlockConditionType parseConfig(@NotNull Section section) {
        NamespacedKey skill = McRPGMethods.parseNamespacedKey(section.getString("skill"));
        if (skill == null) {
            throw new UnlockConditionParseException("mcrpg:skill_level requires a 'skill' key");
        }
        if (!section.contains("level")) {
            throw new UnlockConditionParseException("mcrpg:skill_level requires a 'level' key");
        }
        return new SkillLevelUnlockConditionType(skill, section.getInt("level"));
    }

    @Override
    public boolean isMet(@NotNull AbilityHolder holder) {
        if (skillKey == null || !(holder instanceof SkillHolder skillHolder)) {
            return false;
        }
        Skill skill = resolveSkill();
        if (skill == null) {
            return false;
        }
        return skillHolder.getSkillHolderData(skill)
                .map(data -> data.getCurrentLevel() >= requiredLevel)
                .orElse(false);
    }

    public int getRequiredLevel() {
        return requiredLevel; // exposed for the legacy "what level?" query path
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.of(McRPGExpansion.EXPANSION_KEY);
    }
}
```

Notice the `skillKey == null` guard in `isMet` — calling `isMet` on the registry base prototype simply returns `false`, exactly as `BlockBreakObjectiveType.processProgress` returns `0` for an empty `validBlocks`. No footgun.

### 6.2 `mcrpg:statistic` — StatisticUnlockConditionType

**Config keys:** `statistic` (namespaced McCore statistic key), `threshold` (long), optional display source (`locale-key` **or** `text`, see [Section 12](#12-server-owner-display-hints)).
**Met when:** `player.getStatisticData().getLongValue(statistic) >= threshold`.
**Progress:** `min(1.0, current / threshold)`.
**Display placeholders:** `<statistic>` (the key, or owner-supplied phrasing), `<required>`, `<current>` — e.g. *"Mine 1,000 blocks (847/1,000)"*.

```java
@Override
public boolean isMet(@NotNull AbilityHolder holder) {
    return resolvePlayer(holder)
            .map(player -> player.getStatisticData().getLongValue(statisticKey).orElse(0L) >= threshold)
            .orElse(false);
}
```

`resolvePlayer(holder)` returns the holder directly if it is an `McRPGPlayer`, otherwise looks it up in the player manager by UUID, otherwise empty. The `<current>` value comes from the same `getLongValue(...).orElse(0L)`.

### 6.3 `mcrpg:papi` — PapiUnlockConditionType

**Config keys:** `placeholder` (a `%...%` string), `operator` (one of `>=`, `>`, `==`, `<=`, `<`, `!=`), `value` (number or string), optional display source.
**Met when:** the resolved placeholder satisfies `operator value`. Numeric comparison when both sides parse as numbers; otherwise string equality / inequality.
**Progress:** for a numeric `>=`/`>` comparison, `min(1.0, current / value)`; otherwise binary.
**Display placeholders:** `<placeholder>`, `<operator>`, `<required>` (the value), `<current>` (the resolved placeholder) — e.g. *"Reach 50,000 economy balance (currently 12,300)"*.

```java
@Override
public boolean isMet(@NotNull AbilityHolder holder) {
    Optional<OfflinePlayer> playerOptional = resolveOfflinePlayer(holder);
    if (playerOptional.isEmpty()) {
        return false;
    }
    String resolved = McRPGMethods.applyPapi(placeholder, playerOptional.get());
    if (resolved.equals(placeholder)) {
        return false; // PAPI absent or placeholder unregistered — never met
    }
    return comparison.test(resolved, value);
}
```

PAPI is a soft dependency; `applyPapi` returns the input unchanged when PAPI is absent, so an unresolved placeholder is treated as "not met" rather than throwing. See [Section 19](#19-papi-soft-dependency-handling).

### 6.4 `mcrpg:display_hint` — DisplayHintUnlockConditionType

The single display-only type. Covers book sources, crate plugins, achievement systems, and any other unlock route McRPG has no programmatic hook for.

**Config keys:** exactly one of —
- **`locale-key`** (a `Route` string, **translatable through the player's locale chain** — the preferred form for bundled / multi-language content like book sources), **or**
- **`text`** (inline MiniMessage, single-language, palette-resolved — the right tool for one-server-only advertising like "Epic Crates!").

**Met when:** never (`isMet` always `false`, `isDisplayOnly()` returns `true`).
**Progress:** always `0.0`.
**Display:** the resolved `locale-key` or parsed `text` verbatim. No `<current>` — a hint has no live state.

```java
public final class DisplayHintUnlockConditionType implements UnlockConditionType {

    public static final NamespacedKey KEY =
            new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "display_hint");

    private final Route localeKey;   // exactly one of these is non-null on a configured instance
    private final String inlineText;

    public DisplayHintUnlockConditionType() {
        this.localeKey = null;
        this.inlineText = null;
    }

    public DisplayHintUnlockConditionType(@NotNull Route localeKey) {
        this.localeKey = localeKey;
        this.inlineText = null;
    }

    public DisplayHintUnlockConditionType(@NotNull String inlineText) {
        this.localeKey = null;
        this.inlineText = inlineText;
    }

    @NotNull
    @Override
    public UnlockConditionType parseConfig(@NotNull Section section) {
        boolean hasKey = section.contains("locale-key");
        boolean hasText = section.contains("text");
        if (hasKey == hasText) {
            throw new UnlockConditionParseException(
                    "mcrpg:display_hint requires exactly one of 'locale-key' or 'text'");
        }
        return hasKey
                ? new DisplayHintUnlockConditionType(Route.fromString(section.getString("locale-key")))
                : new DisplayHintUnlockConditionType(section.getString("text"));
    }

    @Override
    public boolean isMet(@NotNull AbilityHolder holder) {
        return false;
    }

    @Override
    public boolean isDisplayOnly() {
        return true;
    }

    @NotNull
    @Override
    public Component getDisplayDescription(@NotNull McRPGPlayer player) {
        if (localeKey != null) {
            return localization().getLocalizedMessageAsComponent(player, localeKey);
        }
        if (inlineText != null) {
            return McRPG.getInstance().getMiniMessage()
                    .deserialize(inlineText, paletteTagResolver());
        }
        return Component.empty(); // unconfigured prototype
    }
}
```

The `locale-key` form is the natural fit for the "Obtain from Riptide Guardian" book source case — the bundled English file translates `ability.unlock-condition.source.riptide-guardian`, and other locale packs translate it without touching ability config. The `text` form is for the "Can be unlocked from Epic Crates!" case where the server owner wants their own wording on their own server and translation isn't a concern.

### 6.5 `mcrpg:all_of` / `mcrpg:any_of` — composites

**Config key:** `conditions` — a nested keyed map in the same shape as the top-level `unlock-conditions`.
**`all_of` met when:** every child is met; **progress** = min child progress.
**`any_of` met when:** any child is met; **progress** = max child progress.
**Children:** `List<UnlockConditionType>` of already-configured children, parsed once and held immutably.

```java
public final class AllOfUnlockConditionType implements UnlockConditionType {

    public static final NamespacedKey KEY =
            new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "all_of");

    private final List<UnlockConditionType> children;

    public AllOfUnlockConditionType() {
        this(List.of());
    }

    public AllOfUnlockConditionType(@NotNull List<UnlockConditionType> children) {
        this.children = List.copyOf(children);
    }

    @NotNull
    @Override
    public UnlockConditionType parseConfig(@NotNull Section section) {
        Section nested = section.getOptionalSection("conditions").orElseThrow(() ->
                new UnlockConditionParseException("mcrpg:all_of requires a 'conditions' section"));
        UnlockConditionManager manager = McRPG.getInstance().registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.UNLOCK_CONDITION);
        List<UnlockConditionType> parsed = manager.parseSection(nested);
        if (parsed.isEmpty()) {
            throw new UnlockConditionParseException("mcrpg:all_of requires at least one child");
        }
        return new AllOfUnlockConditionType(parsed);
    }

    @Override
    public boolean isMet(@NotNull AbilityHolder holder) {
        if (children.isEmpty()) {
            return false;
        }
        for (UnlockConditionType child : children) {
            if (!child.isMet(holder)) {
                return false;
            }
        }
        return true;
    }
}
```

`any_of` is identical but short-circuits on first met and joins with the "any of" prefix. Both reuse the manager's recursive `parseSection`, so arbitrary nesting works.

---

## 7. Config Shape and Rendered Lore

### 7.1 Named-path map, not typed array

Each condition is a **named child** under `unlock-conditions.<id>`, where `<id>` is a server-owner-chosen identifier. The body carries a `type` plus type-specific keys. This is the hard-coded-path shape — `ability-configuration.vampire.unlock-conditions.swords-mastery.type` — which reads naturally and lets owners reference and reason about individual entries by name.

### 7.2 Vampire default (OR with two paths)

Vampire is a tierable Swords passive that unlocks at **Swords level 250** (its tier-1 `unlock-level`). With no config, the Java default applies (one `SkillLevelUnlockConditionType` at level 250 — [Section 9](#9-tierableability-boundary)). A server owner who *also* sells Vampire from a crate adds an OR path and an advertising hint:

```yaml
ability-configuration:
  vampire:
    enabled: true
    amount-of-tiers: 5
    tier-configuration:
      tier-1:
        unlock-level: 250
      # ... existing tier config unchanged ...
    unlock-conditions:
      # Path A: the original skill-level gate, now explicit in config.
      swords-mastery:
        type: mcrpg:skill_level
        skill: mcrpg:swords
        level: 250
      # Path B: advertise the crate. McRPG can't unlock this itself — the crate
      # plugin runs the unlock command — so it's a display-only hint.
      epic-crates:
        type: mcrpg:display_hint
        text: "<body>Can be unlocked from <primary>Epic Crates<body>!"
```

Because this section is present, it **replaces** Vampire's Java default and McRPG logs at startup:

```
[McRPG] Ability mcrpg:vampire declares unlock-conditions in config; this REPLACES its 1 programmatic default condition(s).
```

### 7.3 Hint with `locale-key` (translatable book source)

When the hint is something McRPG ships bundled — a book source like "Riptide Guardian" — the `locale-key` form is the right choice. The text lives in `en_abilities.yml` (and any other locale pack the server runs), so French players see "Obtenu auprès du Gardien des Marées" without ability config changes:

```yaml
ability-configuration:
  whirlpool:        # an LLD-6 book-only ability
    unlock-conditions:
      book-source:
        type: mcrpg:display_hint
        locale-key: ability.unlock-condition.source.riptide-guardian
```

…and in `en_abilities.yml`:

```yaml
ability:
  unlock-condition:
    source:
      riptide-guardian: "<body>Obtain from <primary>Riptide Guardian"
```

The locale chain resolves `riptide-guardian` per-player; French / Spanish / etc. locale packs translate it independently. Compare to inline `text:` (§7.2 above), which is the right form when the source is server-specific and translation isn't a concern.

### 7.4 Standalone `mcrpg:all_of` (AND-only path)

Sometimes a single AND path is the whole unlock — no OR, no hints. Example: a server owner gates a tier-2-derived custom ability behind "Swords level 250 AND 50 player kills" with nothing else:

```yaml
ability-configuration:
  vampire:
    unlock-conditions:
      mastery:
        type: mcrpg:all_of
        conditions:
          level:
            type: mcrpg:skill_level
            skill: mcrpg:swords
            level: 250
          kills:
            type: mcrpg:statistic
            statistic: mcrpg:player_kills
            threshold: 50
            text: "<body>Defeat <primary><required><body> players <hint>(<primary><current><hint>/<primary><required><hint>)"
```

The top-level list has one entry (`mastery`). That entry is an `all_of` whose two children must *both* be met. There is no OR — the player must satisfy both children to unlock.

### 7.5 Nested AND inside OR (the previous mixed example)

The richer pattern from §7.4 in an OR context — "earn it via level+kills, OR buy from a crate":

```yaml
    unlock-conditions:
      earned:
        type: mcrpg:all_of
        conditions:
          level:
            type: mcrpg:skill_level
            skill: mcrpg:swords
            level: 250
          kills:
            type: mcrpg:statistic
            statistic: mcrpg:player_kills
            threshold: 50
            text: "<body>Defeat <primary><required><body> players <hint>(<primary><current><hint>/<primary><required><hint>)"
      epic-crates:
        type: mcrpg:display_hint
        text: "<body>Can be unlocked from <primary>Epic Crates<body>!"
```

`earned` and `epic-crates` are OR-ed (top level); inside `earned`, `level` and `kills` are AND-ed.

### 7.6 What the player actually sees — rendered lore

The locked-state lore is built by `AbilityLoreAppender` from the `{ability-unlock-condition}` placeholder ([Section 15.1](#151-locked-ability-lore)). Below: the exact lore lines a player sees for each config shape above, assuming the player is at Swords level **187** with **12** player kills (so neither met-able condition is satisfied yet). Lore lines wrap at the GUI's standard width; bullets are MiniMessage list markers from the bundled template.

**Default — single skill-level (Vampire vanilla, no config)**

One condition, no OR header:

```
Reach Swords level 250 (currently 187)
```

**OR with two paths (§7.2 — level + crates hint)**

Multiple entries → an OR header + one bullet per path:

```
Unlock via any of:
  · Reach Swords level 250 (currently 187)
  · Can be unlocked from Epic Crates!
```

**Standalone all_of (§7.4 — AND-only, level + kills)**

One top-level entry, but that entry is a composite → no OR header, one nested AND group:

```
All of:
  · Reach Swords level 250 (currently 187)
  · Defeat 50 players (12/50)
```

**Nested AND inside OR (§7.5 — full mixed example)**

Top-level OR header; the `earned` composite renders as an indented "All of:" group; the hint renders as a sibling:

```
Unlock via any of:
  · All of:
      · Reach Swords level 250 (currently 187)
      · Defeat 50 players (12/50)
  · Can be unlocked from Epic Crates!
```

**Rendering rules.**

- One top-level condition that is *not* a composite → render its `getDisplayDescription` as a single line, no header.
- One top-level condition that *is* a composite (`all_of`/`any_of`) → render the composite's own header + indented children, no OR header above it.
- Two or more top-level conditions → render `ability.unlock-condition.list-header` ("Unlock via any of:") followed by one bulleted line per entry.
- A composite child of a composite → indent one further level, repeating the composite's header + bullets.
- Each bullet line is one component returned by the child's `getDisplayLabel(player)` (or `getDisplayDescription` for a composite, since labels collapse multi-line shapes).

Composite descriptions are produced by joining child labels via the localized header keys (`ability.unlock-condition.all-of.header`, `ability.unlock-condition.any-of.header`) — server owners customize the prefixes in YAML.

---

## 8. UnlockableAbility Refactor

**Modified file:** `src/main/java/us/eunoians/mcrpg/ability/impl/type/UnlockableAbility.java`

```java
public interface UnlockableAbility extends Ability {

    /**
     * The programmatic default unlock conditions for this ability, used when the
     * ability's config declares no {@code unlock-conditions} section. The returned
     * list is OR-combined. Defaults to empty — an ability with no Java default and
     * no config is "undiscoverable" and triggers the startup warning (Section 13).
     */
    @NotNull
    default List<UnlockConditionType> getDefaultUnlockConditions() {
        return List.of();
    }

    /**
     * The effective unlock conditions: the config override if present, otherwise
     * {@link #getDefaultUnlockConditions()}. OR semantics — meeting any condition
     * makes the ability eligible to unlock. Resolved and cached by the
     * {@link UnlockConditionManager}.
     */
    @NotNull
    default List<UnlockConditionType> getUnlockConditions() {
        return McRPG.getInstance().registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.UNLOCK_CONDITION)
                .resolve(this);
    }

    /**
     * Whether the holder meets ANY unlock condition. Drives the login sweep and
     * the skill level-up unlock flow. Display-only conditions never contribute.
     */
    default boolean isAnyConditionMet(@NotNull AbilityHolder holder) {
        for (UnlockConditionType condition : getUnlockConditions()) {
            if (condition.isMet(holder)) {
                return true;
            }
        }
        return false;
    }

    @NotNull
    @Override
    default Set<NamespacedKey> getApplicableAttributes() {
        return Set.of(AbilityAttributeRegistry.ABILITY_TOGGLED_OFF_ATTRIBUTE_KEY,
                AbilityAttributeRegistry.ABILITY_UNLOCKED_ATTRIBUTE);
    }

    /**
     * Whether this ability is currently unlocked for the holder. Reads the
     * {@code AbilityUnlockedAttribute} — the canonical source of truth. Unchanged.
     */
    default boolean isAbilityUnlocked(@NotNull AbilityHolder abilityHolder) {
        // ... unchanged from current implementation ...
    }
}
```

### 8.1 Removed members

| Member | Replacement |
|---|---|
| `int getUnlockLevel()` | `getUnlockConditions()`; callers needing the number find the first `SkillLevelUnlockConditionType` and read `getRequiredLevel()` |
| `boolean checkIfAbilityCanBeUnlocked(SkillHolder, Skill)` | `isAnyConditionMet(holder)` |

---

## 9. TierableAbility Boundary

`TierableAbility extends UnlockableAbility`. The per-tier methods (`getUnlockLevelForTier(int)`, `getCurrentAbilityTier`, etc.) are **unchanged** — they belong to the tier-upgrade flow (`AbilityUpgradeQuestSource`), which answers "have I met the prerequisite to start the upgrade quest for tier N?", not "is this ability unlocked at all?".

The only change: `TierableAbility` provides the default *first*-tier unlock condition.

```java
@Override
@NotNull
default List<UnlockConditionType> getDefaultUnlockConditions() {
    if (this instanceof SkillAbility skillAbility) {
        return List.of(new SkillLevelUnlockConditionType(
                skillAbility.getSkillKey(), getUnlockLevelForTier(1)));
    }
    // Non-skill tierables fall back to "config or undiscoverable" — the empty
    // list triggers the Section 13 warning unless config supplies conditions.
    return List.of();
}
```

For Vampire (a `SkillAbility` tierable), this yields exactly one configured `SkillLevelUnlockConditionType` at Swords level 250 — behavior-identical to the old `getUnlockLevel()` path, constructed directly via the public configured constructor without touching `parseConfig`. Non-`SkillAbility` tierables previously threw `UnsupportedOperationException`; the new model returns an empty list and relies on config + the startup warning, which is gentler and config-overridable.

**Vampire is fully configurable from YAML without any Java changes.** The Java default in `getDefaultUnlockConditions()` is only a *fallback*. As described in [Section 5.2](#52-unlockconditionmanager-resolution--caching--warnings), `UnlockConditionManager.resolve()` checks for a config `unlock-conditions` section first. If that section is present and non-empty, it **replaces** the Java default entirely (with a logged warning). So a server owner can change Vampire's unlock requirement to "Mining level 500 AND have 10,000 blocks mined" purely by adding an `unlock-conditions` block to the swords config — the Java `SkillLevelUnlockConditionType(swords, 250)` default is never consulted.

### 9.1 Why per-tier methods stay

`getUnlockLevelForTier(int)` is consulted by 8 upgrade-quest-eligibility callsites ([Section 14.3](#143-callsites-that-stay-on-the-old-per-tier-api)). Promoting per-tier gates to conditions would force every upgrade-quest reward type and GUI slot into polymorphic condition checks for a question they answer today with one integer compare. A future LLD can add `getUnlockConditionsForTier(int)` additively without touching the base interface.

---

## 10. Auto-Unlock Semantics

- **Top-level list is OR.** `isAnyConditionMet` returns true if *any* condition is met.
- **AND is `mcrpg:all_of`.** A composite condition is one entry in the OR-list whose own `isMet` requires all children.
- **Display-only conditions never auto-unlock.** `mcrpg:display_hint` always returns `false`, so a crate/book hint advertises a path without McRPG ever firing an unlock for it. The external system (crate plugin / `SkillBookConsumeListener`) flips `AbilityUnlockedAttribute` directly.

This OR-by-default is what makes the Vampire example work: "Swords 250 **or** Epic Crates" — reaching the level fires the unlock through the sweep/level-up flow; the crate path is advertised but driven externally.

### 10.1 Evaluation walkthrough — `OnSkillLevelUpListener` and login sweep

Both the skill level-up handler and the login sweep call `isAnyConditionMet(holder)`. The evaluation is a short-circuiting recursive walk:

1. `isAnyConditionMet` iterates the top-level `List<UnlockConditionType>` (**OR**). On the first `condition.isMet(holder) == true`, it returns `true` immediately (short-circuit). If every entry returns `false`, the ability stays locked.

2. Each entry calls its own `isMet(holder)`:
   - **Leaf types** (`skill_level`, `statistic`, `papi`): evaluate directly against the holder's state (current skill level, statistic value, resolved PAPI string) and return `true` / `false`.
   - **`display_hint`**: always returns `false` — never contributes to auto-unlock.
   - **`all_of` (composite AND)**: iterates its children; returns `true` only if *every* child returns `true`. Short-circuits on the first `false`.
   - **`any_of` (composite OR)**: iterates its children; returns `true` if *any* child returns `true`. Short-circuits on the first `true`.

3. Composites can be nested, forming an arbitrary boolean tree. Evaluation is recursive but bounded by the config depth — practically two or three levels deep.

**Example.** Vampire configured with:

```yaml
unlock-conditions:
  swords-mastery:
    type: mcrpg:skill_level
    skill: mcrpg:swords
    level: 250
  mining-veteran:
    type: mcrpg:all_of
    conditions:
      blocks-mined:
        type: mcrpg:statistic
        statistic: mcrpg:blocks_mined
        required: 10000
      mining-level:
        type: mcrpg:skill_level
        skill: mcrpg:mining
        level: 100
  epic-crates:
    type: mcrpg:display_hint
    text: "<body>Can be unlocked from <primary>Epic Crates<body>!"
```

When a player hits Swords level 250, `OnSkillLevelUpListener` calls `isAnyConditionMet(player)`:

1. Check `swords-mastery` → `SkillLevelUnlockConditionType.isMet()` → player's Swords level ≥ 250? **Yes → return `true`**. Done — ability unlocks.

If the player had NOT reached Swords 250 but logged in with 12,000 blocks mined and Mining 105, the login sweep would call `isAnyConditionMet`:

1. Check `swords-mastery` → Swords level < 250 → `false`.
2. Check `mining-veteran` → `AllOfUnlockConditionType.isMet()`:
   - Child `blocks-mined` → `StatisticUnlockConditionType.isMet()` → 12,000 ≥ 10,000 → `true`.
   - Child `mining-level` → `SkillLevelUnlockConditionType.isMet()` → 105 ≥ 100 → `true`.
   - All children met → `all_of` returns `true`. **Top-level returns `true`** — ability unlocks.
3. `epic-crates` is never evaluated (short-circuit), and would return `false` anyway (`display_hint`).

---

## 11. Current-Progress Display

Every progress-bearing type interpolates a `<current>` placeholder (alongside `<required>`) at render time, resolved against the player's state and formatted through `McRPGDisplayDecimalFormatter` for locale-correct grouping/decimals.

| Type | Template (default, server-customizable) | `<current>` source |
|---|---|---|
| `mcrpg:skill_level` | `Reach <skill> level <primary><required> <hint>(currently <primary><current><hint>)` | holder's current level in the skill |
| `mcrpg:statistic` | owner `text`/`locale-key`, else `Reach <primary><required> <body><statistic> <hint>(<primary><current><hint>/<primary><required><hint>)` | `getLongValue(statistic)` |
| `mcrpg:papi` | owner `text`/`locale-key`, else `Requirement: <primary><placeholder> <operator> <required> <hint>(currently <primary><current><hint>)` | resolved PAPI value |
| `mcrpg:display_hint` | the `text` / `locale-key` content verbatim | n/a — `<current>` omitted |
| `mcrpg:all_of` / `mcrpg:any_of` | prefix + per-child labels | per child |

Rendering helper (shared shape across types):

```java
@NotNull
private Component render(@NotNull McRPGPlayer player, @NotNull Route template, long current, long required) {
    McRPGLocalizationManager localization = /* via registryAccess */;
    var formatter = localization.getDisplayDecimalFormatter();
    return localization.getLocalizedMessageAsComponent(player, template, Map.of(
            "required", formatter.formatDisplayDecimal(player, required),
            "current", formatter.formatDisplayDecimal(player, current)));
}
```

Because `getDisplayDescription` takes the `McRPGPlayer`, the `<current>` value is always that player's live progress — the GUI shows "(187/250)" for one player and "(250/250)" (met) for another.

---

## 12. Server-Owner Display Hints

Hints — and the optional display text on `statistic` / `papi` — carry text two ways, owner's choice per entry:

| Form | Resolution | When to use |
|---|---|---|
| `locale-key: ability.unlock-condition.source.riptide-guardian` | Through the full locale chain — translatable | Bundled / multi-language sources |
| `text: "<body>Can be unlocked from <primary>Epic Crates<body>!"` | MiniMessage parse with the palette tag resolver — single language | Server-owner-specific advertising |

Exactly one is allowed per entry (`parseConfig` throws if both or neither). Inline `text` is **not** routed through the locale chain (it is the owner's literal string) but **is** palette-resolved, so `<primary>`, `<body>`, etc. obey the server's configured colors. This is the "Can be unlocked from Epic Crates!" path: an unlock route with no programmatic backing that the owner advertises in their own words.

---

## 13. Empty-Display Startup Warning

`UnlockConditionManager.resolveAll()` runs once at bootstrap after all types, abilities, and configs are loaded. For each registered `UnlockableAbility`, it resolves the effective condition list; if the list is **empty** (no real conditions and no hints — the "unlockable but undiscoverable" case), it logs:

```
[McRPG] UnlockableAbility mcrpg:<ability> resolved to zero unlock conditions and zero hints — players have no advertised way to unlock it. Add an 'unlock-conditions' entry or a programmatic default.
```

Note the asymmetry: an ability with only a `display_hint` (and no met-able condition) is **not** warned — it is discoverable, just not auto-unlockable by McRPG (a valid crate-driven setup). An ability with only a real condition and no advertised bonus source is also fine — a bonus source going unadvertised never warns. Only the *primary* path being entirely absent triggers it.

---

## 14. Migration

### 14.1 Direct callers (must change)

| File | Current call | Replacement |
|---|---|---|
| `listener/skill/OnSkillLevelUpListener.java` | `unlockableAbility.getUnlockLevel() <= skillHolderData.getCurrentLevel()` | `unlockableAbility.isAnyConditionMet(skillHolder)` |
| `gui/ability/AbilitySortType.java` | `Integer.compare(a.getUnlockLevel(), b.getUnlockLevel())` | `Comparator.comparingDouble((UnlockableAbility u) -> bestProgress(u, holder)).reversed()`, tie-broken by display-label string. `bestProgress` = max `getProgress` over the OR-list |
| `builder/item/ability/AbilityLoreAppender.java` | `{ability-unlock-level}` ← `getUnlockLevel()` | `{ability-unlock-condition}` ← rendered OR-list (Section 15) |

### 14.2 The `{ability-unlock-level}` → `{ability-unlock-condition}` locale change

The per-ability `locked-lore` strings reference `<ability-unlock-level>`. Each bundled entry is migrated in place to `<ability-unlock-condition>`. This is the only player-visible string change; all other migrations are behavior-preserving. A migration note goes in `CLAUDE.md`'s localization section: server owners with custom locale files must update the placeholder.

### 14.3 Callsites that stay on the old per-tier API

These 8 use `getUnlockLevelForTier(int)` and are unchanged (tier-upgrade preconditions, [Section 9.1](#91-why-per-tier-methods-stay)):

- `quest/QuestManager.java`
- `quest/reward/builtin/AbilityUpgradeRewardType.java`
- `quest/reward/builtin/AbilityUpgradeNextTierRewardType.java`
- `gui/ability/slot/UpgradeQuestSlot.java`
- `util/filter/ability/AbilityUpgradeFilter.java`
- `builder/item/ability/AbilityLoreAppender.java` (the per-tier upgrade lines)
- `entity/player/McRPGPlayer.java`
- `ability/impl/type/configurable/ConfigurableTierableAbility.java`

### 14.4 Per-ability migration

Every `TierableAbility` that is also a `SkillAbility` (all of them today, including Vampire) gets correct behavior from the default `getDefaultUnlockConditions()` with **zero** changes. A non-`SkillAbility` `UnlockableAbility` either overrides `getDefaultUnlockConditions()` or declares `unlock-conditions` in config; otherwise the [startup warning](#13-empty-display-startup-warning) flags it. No CI compile-check is needed — the warning surfaces misses at boot.

---

## 15. GUI Integration

### 15.1 Locked-ability lore

`AbilityLoreAppender` builds the locked-state lore. The refactor:

1. Replaces `{ability-unlock-level}` with `{ability-unlock-condition}` in every bundled locale entry.
2. Resolves it by rendering the OR-list: a localized "Unlock via any of:" header (omitted when the list has exactly one entry), then each condition's `getDisplayDescription(player)`. Composite (`all_of`/`any_of`) descriptions are multi-line and indented.
3. Splits the resulting Component on `\n` so each line becomes its own lore line, preserving existing wrapping.

A skill-level condition renders "Reach Swords level 250 (currently 187)"; a display hint renders "Can be unlocked from Epic Crates!"; the two appear as alternative paths under the header.

### 15.2 Ability sort

`AbilitySortType.MOST_RELEVANT` sorts by best-condition progress descending (closest-to-unlock first), tie-broken alphabetically. Display-only-only abilities (progress 0) sort to the end. Skill-leveled abilities keep substantially the same order as the old numeric-level sort.

### 15.3 No new screen

All display folds into existing surfaces (lore, sort). A future "Codex" screen could surface conditions first-class — out of scope.

---

## 16. Login-Time Unlock Sweep

Resolves issue **#220**. **New file:** `src/main/java/us/eunoians/mcrpg/listener/ability/OnPlayerLoadUnlockSweepListener.java`.

Listens for `PlayerLoadEvent` (after player data is fully loaded). For each registered `UnlockableAbility` the player has data for and has not unlocked, it checks `isAnyConditionMet(player)` and, if met, runs the same flip-and-fire used by `OnSkillLevelUpListener` — set `AbilityUnlockedAttribute`, fire `AbilityUnlockEvent`, schedule the async save.

```java
for (NamespacedKey abilityKey : abilityRegistry.getRegisteredAbilityKeys()) {
    if (!(abilityRegistry.getRegisteredAbility(abilityKey) instanceof UnlockableAbility unlockable)) {
        continue;
    }
    if (unlockable.isAbilityUnlocked(player) || !unlockable.isAnyConditionMet(player)) {
        continue;
    }
    flipAttributeAndFire(player, unlockable);
}
```

This keeps the unlock side-effect in exactly two observer locations (level-up, login sweep), with book/crate consumption as the third externally-driven path. Safety (main-thread, post-load, no JDBC block, no duplicate events) is unchanged from the prior design's reasoning.

---

## 17. Localization Keys

### 17.1 LocalizationKey.java additions

**Modified file:** `configuration/file/localization/LocalizationKey.java`. A new `UNLOCK_CONDITION_HEADER` under `ABILITY_HEADER`:

```java
private static final String UNLOCK_CONDITION_HEADER = toRoutePath(ABILITY_HEADER, "unlock-condition");
public static final Route UNLOCK_CONDITION_SKILL_LEVEL_DESCRIPTION =
        Route.fromString(toRoutePath(UNLOCK_CONDITION_HEADER, "skill-level.description"));
public static final Route UNLOCK_CONDITION_SKILL_LEVEL_LABEL =
        Route.fromString(toRoutePath(UNLOCK_CONDITION_HEADER, "skill-level.label"));
public static final Route UNLOCK_CONDITION_STATISTIC_DESCRIPTION =
        Route.fromString(toRoutePath(UNLOCK_CONDITION_HEADER, "statistic.description"));
public static final Route UNLOCK_CONDITION_PAPI_DESCRIPTION =
        Route.fromString(toRoutePath(UNLOCK_CONDITION_HEADER, "papi.description"));
public static final Route UNLOCK_CONDITION_ALL_OF_HEADER =
        Route.fromString(toRoutePath(UNLOCK_CONDITION_HEADER, "all-of.header"));
public static final Route UNLOCK_CONDITION_ANY_OF_HEADER =
        Route.fromString(toRoutePath(UNLOCK_CONDITION_HEADER, "any-of.header"));
public static final Route UNLOCK_CONDITION_LIST_HEADER =
        Route.fromString(toRoutePath(UNLOCK_CONDITION_HEADER, "list-header"));
```

### 17.2 en_abilities.yml additions

**Modified file:** `src/main/resources/localization/english/en_abilities.yml`, under `ability:`:

```yaml
  unlock-condition:
    # Shown above the OR-list when an ability has more than one unlock path.
    list-header: "<body>Unlock via any of:"
    skill-level:
      # Placeholders: <skill> (colored), <required> (level), <current> (player level)
      description: "<body>Reach <skill> <body>level <primary><required> <hint>(currently <primary><current><hint>)"
      label: "<skill> <primary><current><body>/<primary><required>"
    statistic:
      # Default phrasing when an entry supplies no 'text'/'locale-key'.
      # Placeholders: <statistic>, <required>, <current>
      description: "<body>Reach <primary><required> <body><statistic> <hint>(<primary><current><hint>/<primary><required><hint>)"
    papi:
      # Placeholders: <placeholder>, <operator>, <required>, <current>
      description: "<body>Requirement: <primary><placeholder> <operator> <required> <hint>(currently <primary><current><hint>)"
    all-of:
      header: "<body>All of:"
    any-of:
      header: "<body>Any of:"
    # Reserved namespace for translatable display-hint sources referenced by
    # 'locale-key'. Bundled sources are added here as abilities adopt them.
    source: {}
```

### 17.3 `{ability-unlock-level}` migration

Each bundled `locked-lore` entry referencing `<ability-unlock-level>` becomes `<ability-unlock-condition>`. Documented in `CLAUDE.md`.

---

## 18. Bootstrap Registration & Content Pack

### 18.1 Content pack

**New file:** `src/main/java/us/eunoians/mcrpg/expansion/content/UnlockConditionTypeContentPack.java`

```java
public final class UnlockConditionTypeContentPack extends McRPGContentPack<UnlockConditionType> {
    public UnlockConditionTypeContentPack(@NotNull ContentExpansion contentExpansion) {
        super(contentExpansion);
    }
}
```

### 18.2 McRPGExpansion wiring — built-ins ship through the native ContentExpansion

McRPG ships the six built-in types **through its own `ContentExpansion`** — the native `McRPGExpansion` — using the exact same `UnlockConditionTypeContentPack` mechanism a third-party expansion would use. There is no special internal back door: McRPG's built-ins are just the first registered pack, alongside `AbilityContentPack`, `SkillContentPack`, `QuestObjectiveTypeContentPack`, and the rest. A new expansion shipping its own `mcrpg:my_quest_complete` condition type follows the same three lines:

```java
// In McRPGExpansion.getExpansionContent():
UnlockConditionTypeContentPack unlockConditionTypes = new UnlockConditionTypeContentPack(this);
unlockConditionTypes.addContent(new SkillLevelUnlockConditionType());
unlockConditionTypes.addContent(new StatisticUnlockConditionType());
unlockConditionTypes.addContent(new PapiUnlockConditionType());
unlockConditionTypes.addContent(new DisplayHintUnlockConditionType());
unlockConditionTypes.addContent(new AllOfUnlockConditionType());
unlockConditionTypes.addContent(new AnyOfUnlockConditionType());
expansionContent.addContentPack(unlockConditionTypes);
```

A third-party expansion's `getExpansionContent()` does the same with its own types — the registry doesn't distinguish "native" from "third-party" packs at all. Each type's `getExpansionKey()` returns the key of the pack that registered it, which is how the rest of the system knows where each came from (and how a misconfigured expansion's unknown-type error message can point at the right plugin).

### 18.3 Registration order

The bootstrap must register, in order:

1. `UnlockConditionTypeRegistry` (under `McRPGRegistryKey.UNLOCK_CONDITION_TYPE`) and `UnlockConditionManager` (under `McRPGManagerKey.UNLOCK_CONDITION`).
2. Built-in + expansion `UnlockConditionType`s (via the content pack flow that already handles quest types).
3. Abilities and their skill configs.
4. **After** all of the above: `UnlockConditionManager.resolveAll()` to warm the cache and emit the [empty-display warnings](#13-empty-display-startup-warning). This ordering guarantees `parseConfig` never hits an unregistered type for a correctly-installed expansion (mis-ordered third-party expansions degrade gracefully — the unknown-type branch in `parseSection` logs and skips).

### 18.4 Listener registration

`McRPGListenerRegistrar` registers `OnPlayerLoadUnlockSweepListener` alongside `OnAbilityUnlockListener`.

### 18.5 Reload

`UnlockConditionManager` participates in the existing reload flow: `/mcrpg admin reload` clears its cache, so edited `unlock-conditions` and localization templates take effect without restart.

---

## 19. PAPI Soft-Dependency Handling

PlaceholderAPI is an optional soft dependency. The `mcrpg:papi` type never hard-references PAPI for evaluation — it goes through `McRPGMethods.applyPapi(String, OfflinePlayer)`, which:

- Returns the resolved string when the PAPI plugin hook is present.
- Returns the **input unchanged** when PAPI is absent.

The condition treats an unchanged string (still containing `%...%`) as "not met," so on a server without PAPI a `mcrpg:papi` condition is simply never satisfied — no `ClassNotFoundException`, no crash. The display renders the raw placeholder for the `<current>` value, making the misconfiguration visible to the owner. A `parseConfig`-time `logger.info` notes when a `mcrpg:papi` condition is registered while PAPI is not installed, so the owner understands why it never unlocks.

---

## 20. Edge Cases & Graceful Degradation

| Scenario | Behavior |
|---|---|
| Config references an unknown `type` | `parseSection` logs a warning naming the entry + type, skips that one entry, keeps the rest. |
| Condition section malformed (missing `skill`/`level`, both `text`+`locale-key`, etc.) | `parseConfig` throws `UnlockConditionParseException`; manager logs and skips that entry. |
| Referenced skill not registered | `SkillLevelUnlockConditionType.isMet` returns `false`; label degrades to the raw key; ability stays locked. |
| Holder is not an `McRPGPlayer` (statistic/papi need player state) | `isMet` returns `false`, progress `0.0`. |
| `all_of`/`any_of` with empty `conditions` | `parseConfig` throws → entry skipped with a warning. |
| Config `unlock-conditions` present but empty map | Treated as "no override" → Java default used (avoids accidentally wiping the default to nothing). |
| Resolved list empty (no default, no config, no hint) | [Startup warning](#13-empty-display-startup-warning); ability is permanently locked until config/code supplies a path. |
| PAPI absent for a `mcrpg:papi` condition | Never met; raw placeholder shown; info-logged at registration. |
| Sweep finds an already-unlocked ability | Short-circuits via `isAbilityUnlocked` before evaluating — no duplicate event. |
| Server owner overrides Java default | Override applied; warning logged with the default count for confirmation. |
| Condition mutates holder state in `isMet` | Contract violation; documented in Javadoc, by convention (no enforcement). |

---

## 21. Test Plan

### 21.1 Pure unit tests (`src/test/java`)

| Test Class | Coverage |
|---|---|
| `SkillLevelUnlockConditionTest` | `isMet` at/below/above level; no skill data → false; non-`SkillHolder` → false; progress linear capped at 1.0 and 0.0 for `level <= 0`; description/label substitute `<skill>`/`<required>`/`<current>`; missing skill registration degrades gracefully. |
| `StatisticUnlockConditionTest` | `isMet` from `getLongValue`; missing statistic → 0 → below threshold; progress fraction; `<current>`/`<required>` substituted; owner `text` vs default template. |
| `PapiUnlockConditionTest` | Numeric and string comparisons per operator; PAPI-absent (unchanged string) → not met; `<current>` reflects resolved value. |
| `DisplayHintUnlockConditionTest` | `isMet` always false; `isDisplayOnly` true; `locale-key` vs inline `text`; both-or-neither → parse exception. |
| `AllOfUnlockConditionTest` / `AnyOfUnlockConditionTest` | Empty children → parse exception; all-met/any-met truth tables; progress min/max; short-circuit ordering; nested composites. |
| `UnlockConditionTypeRegistryTest` | register/get/getOrThrow/isRegistered/duplicate-key throws. |
| `UnlockConditionManagerParseTest` | Named-map parsing; unknown type skipped; malformed entry skipped; empty map → default; config present → override + warning logged; recursive composite parsing. |
| `TierableAbilityUnlockConditionDefaultTest` | `SkillAbility` tierable yields one `mcrpg:skill_level` at tier-1 level; non-`SkillAbility` yields empty list. |

### 21.2 MockBukkit tests (extend `McRPGBaseTest`)

| Test Class | Coverage |
|---|---|
| `OnPlayerLoadUnlockSweepListenerTest` | No eligible → no events; one eligible → one `AbilityUnlockEvent` + attribute set + save submitted; already-unlocked → no event; display-hint-only ability → no event. |
| `AbilityLoreAppenderUnlockConditionTest` | Renders single skill-level path ("Reach Swords level 250 (currently 0)"); renders OR-list with header for multi-path; renders display hint verbatim. |
| `UnlockConditionEmptyDisplayWarningTest` | Ability with empty resolved list logs the warning; hint-only ability does not. |
| `VampireUnlockConditionMigrationTest` | Vampire with no config resolves to one `mcrpg:skill_level` at 250; with config OR-list, resolves to the configured list + logs override. |

### 21.3 Manual testing (Paper server)

| Scenario | Verification |
|---|---|
| Vampire default, gain Swords levels past 250 | Unlock message + loadout auto-add + GUI shows unlocked |
| Vampire default, raise level offline, log in | Login sweep fires unlock on join |
| Add `unlock-conditions` OR-list with a crate `display_hint`, reload | GUI shows "Unlock via any of: Reach Swords level 250 (currently N) / Can be unlocked from Epic Crates!"; override warning in console |
| `mcrpg:statistic` condition | Lore shows "(current/threshold)"; unlock fires when threshold crossed at login |
| `mcrpg:papi` condition with PAPI installed / absent | Met when placeholder satisfies; never met + raw placeholder shown when PAPI absent |
| Composite `all_of` inside the OR-list | Both children required for that path; other paths independent |

---

## 22. File Manifest

### New files

| File | Type | Description |
|---|---|---|
| `ability/unlock/UnlockConditionType.java` | Interface | Unified prototype + configured instance — registered, content-pack-distributable, evaluable |
| `ability/unlock/UnlockConditionTypeRegistry.java` | Registry | `Registry<UnlockConditionType>` |
| `ability/unlock/UnlockConditionManager.java` | Manager | Resolution, caching, override + empty-display warnings, recursive parse |
| `ability/unlock/UnlockConditionParseException.java` | Exception | Thrown by `parseConfig` on malformed config |
| `ability/unlock/builtin/SkillLevelUnlockConditionType.java` | Type | `mcrpg:skill_level` |
| `ability/unlock/builtin/StatisticUnlockConditionType.java` | Type | `mcrpg:statistic` |
| `ability/unlock/builtin/PapiUnlockConditionType.java` | Type | `mcrpg:papi` |
| `ability/unlock/builtin/DisplayHintUnlockConditionType.java` | Type | `mcrpg:display_hint` (locale-key **or** inline text) |
| `ability/unlock/builtin/AllOfUnlockConditionType.java` | Type | `mcrpg:all_of` composite |
| `ability/unlock/builtin/AnyOfUnlockConditionType.java` | Type | `mcrpg:any_of` composite |
| `expansion/content/UnlockConditionTypeContentPack.java` | Content pack | `McRPGContentPack<UnlockConditionType>` — McRPG's built-ins ship through `McRPGExpansion`, third parties register their own through the same class |
| `listener/ability/OnPlayerLoadUnlockSweepListener.java` | Listener | Login OR-sweep |

Test files mirror the structure under `src/test/java/.../ability/unlock/` and `.../listener/ability/`.

### Modified files

| File | Change |
|---|---|
| `ability/impl/type/UnlockableAbility.java` | Remove `getUnlockLevel`/`checkIfAbilityCanBeUnlocked`; add `getDefaultUnlockConditions`, `getUnlockConditions`, `isAnyConditionMet` |
| `ability/impl/type/TierableAbility.java` | Add default `getDefaultUnlockConditions()` deriving one `mcrpg:skill_level` for `SkillAbility` tierables |
| `listener/skill/OnSkillLevelUpListener.java` | `getUnlockLevel() <= level` → `isAnyConditionMet(holder)` |
| `gui/ability/AbilitySortType.java` | Numeric-level compare → best-condition-progress comparator |
| `builder/item/ability/AbilityLoreAppender.java` | `{ability-unlock-level}` → `{ability-unlock-condition}` rendered OR-list |
| `configuration/file/localization/LocalizationKey.java` | Add `UNLOCK_CONDITION_*` routes |
| `src/main/resources/localization/english/en_abilities.yml` | Add `ability.unlock-condition.*`; migrate `<ability-unlock-level>` → `<ability-unlock-condition>`; reserve `ability.unlock-condition.source` |
| `expansion/McRPGExpansion.java` | Register `UnlockConditionTypeContentPack` with the six built-ins |
| `registry/McRPGRegistryKey.java` | Add `UNLOCK_CONDITION_TYPE` |
| `registry/manager/McRPGManagerKey.java` | Add `UNLOCK_CONDITION` |
| `bootstrap/McRPGRegistryRegistrar.java` (or equivalent) | Register registry + manager; call `resolveAll()` after content + config load |
| `bootstrap/McRPGListenerRegistrar.java` | Register `OnPlayerLoadUnlockSweepListener` |
| `CLAUDE.md` | Add `UnlockConditionType` to Domain Terminology + Quest-style extensibility list; note the `<ability-unlock-level>` migration |

### Not modified (used as-is)

`AbilityRegistry`, `AbilityUnlockedAttribute`, `AbilityUnlockEvent`, `OnAbilityUnlockListener`, `SkillBookConsumeListener` (still bypasses conditions), and all `getUnlockLevelForTier(int)` callers ([Section 14.3](#143-callsites-that-stay-on-the-old-per-tier-api)).

---

## 23. Future LLD Notes

- **Abilities authored purely from config.** This LLD deliberately lays the groundwork without delivering it: a registry-backed extensible type, `parseConfig`/`serializeConfig`, `ContentExpansion`-registerable packs, and recursive composition are precisely the pieces a "define an ability in YAML" feature would extend to *activation conditions* and *effects*. The `serializeConfig` default (throwing today) is the seam an admin tool would implement to round-trip conditions back to disk. No design is attempted here.

- **`mcrpg:quest_complete` / `mcrpg:achievement` types.** Additive: register a new `UnlockConditionType`. The login sweep already fires for any condition that becomes met. Achievement requires the achievement system to exist first.

- **Per-tier conditions (`getUnlockConditionsForTier(int)`).** If the tier-upgrade flow ever needs non-level preconditions, add the method to `TierableAbility` additively without touching `UnlockConditionType`.

- **Clickable / hover hints.** If display hints ever need interactivity (e.g. "where do books drop?"), register a richer hint type alongside `mcrpg:display_hint` — the registry makes it non-breaking.

- **Issue #220 closure.** The login sweep ([Section 16](#16-login-time-unlock-sweep)) directly resolves it.
