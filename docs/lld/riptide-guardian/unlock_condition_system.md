# Low-Level Design: UnlockCondition System (LLD-5)

**Status:** Draft
**Date:** 2026-05-29
**Last Updated:** 2026-05-29
**HLD Reference:** [Riptide Guardian HLD](../../hld/riptide-guardian/riptide_guardian.md), Section 7
**Scope:** `UnlockCondition` interface, built-in implementations, `UnlockableAbility` refactor, GUI integration, login-time sweep

---

## Table of Contents

1. [Overview](#1-overview)
2. [Existing Infrastructure](#2-existing-infrastructure)
3. [Design Decisions](#3-design-decisions)
4. [UnlockCondition Interface](#4-unlockcondition-interface)
5. [Built-In Implementations](#5-built-in-implementations)
6. [UnlockableAbility Refactor](#6-unlockableability-refactor)
7. [TierableAbility Boundary](#7-tierableability-boundary)
8. [Composite Conditions](#8-composite-conditions)
9. [UnlockableAbility Migration](#9-unlockableability-migration)
10. [GUI Integration](#10-gui-integration)
11. [Login-Time Unlock Sweep](#11-login-time-unlock-sweep)
12. [Localization Keys](#12-localization-keys)
13. [Bootstrap Registration](#13-bootstrap-registration)
14. [Edge Cases & Graceful Degradation](#14-edge-cases--graceful-degradation)
15. [Test Plan](#15-test-plan)
16. [File Manifest](#16-file-manifest)
17. [Future LLD Notes](#17-future-lld-notes)

---

## 1. Overview

`UnlockableAbility` currently exposes two skill-coupled methods:

```java
int getUnlockLevel();
boolean checkIfAbilityCanBeUnlocked(SkillHolder skillHolder, Skill skill);
```

These methods bake in the assumption that the only way to unlock an ability is to reach a level in some skill. Skill books (LLD-3) already broke that assumption — books unlock abilities by consumption, not by hitting a level threshold. Future unlock paths (quest completion, achievements, time-gated events) will break it further.

This LLD replaces the skill-coupled methods with a small, polymorphic `UnlockCondition` interface. Each `UnlockableAbility` returns a single `UnlockCondition` (possibly composite). Callers ask the condition whether it is met, what to display, and how complete the player is — without caring whether the condition is level-based, item-based, quest-based, or anything else.

**This LLD produces code.** All interfaces, classes, GUI changes, and tests described here are implementation-ready.

### Why this matters now

The skill book system (LLD-3) currently has no clean way to render the unlock requirement for a skill-book ability in the ability info GUI. Today the GUI calls `getUnlockLevel()` and prints "Reach <skill> Level N" — but the abilities that LLD-3 produces books for (Phase Shift, Whirlpool, etc., scoped to LLD-6) have no skill level. This LLD provides the structured "Obtain from <source>" rendering path that LLD-6 will rely on.

It also unblocks open issue **#220** ("Check unlock conditions on player login") — a generic `condition.isMet(holder)` sweep at login replaces the current scattered level checks.

### What this LLD does NOT cover

| Out of scope | Reason |
|---|---|
| Per-tier upgrade gates (`TierableAbility.getUnlockLevelForTier(int)`) | Tier upgrades have their own mechanism — `AbilityUpgradeQuestSource` and `AbilityUpgradeQuestAttribute`. The level check inside that flow is a quest precondition, not an "unlock condition" on the ability. See [Section 7](#7-tierableability-boundary). |
| `QuestCompleteUnlockCondition` and `AchievementUnlockCondition` implementations | Listed as future implementations in the HLD. The interface is built to accept them, but the achievement system does not yet exist, and the quest-as-unlock-source pattern needs its own LLD when it lands. |
| Player abilities (Phase Shift, Whirlpool, etc.) | These are LLD-6. This LLD provides `SkillBookUnlockCondition` so LLD-6 can wire it. |
| Persistence of `UnlockCondition` state | Conditions are pure functions of `AbilityHolder` state — they read from `SkillHolderData` / `AbilityUnlockedAttribute` / etc. They do not carry their own per-player mutable state and do not need DB rows. |

---

## 2. Existing Infrastructure

These classes already exist and are relevant to this LLD:

| Class | Location | Role in LLD-5 |
|---|---|---|
| `UnlockableAbility` | `ability/impl/type/` | Interface refactored — `getUnlockLevel()` and `checkIfAbilityCanBeUnlocked()` removed and replaced by `getUnlockCondition()` |
| `TierableAbility` | `ability/impl/type/` | Extends `UnlockableAbility`. Updated to derive its `UnlockCondition` from the tier-1 unlock level (see [Section 7](#7-tierableability-boundary)) |
| `ConfigurableTierableAbility` | `ability/impl/type/configurable/` | Default `getUnlockLevelForTier(int)` implementation. Used to derive the tier-1 unlock level for the migration default |
| `AbilityUnlockedAttribute` | `ability/attribute/` | Read by `isAbilityUnlocked()` — unchanged. Conditions do **not** look at this; `isMet()` answers the eligibility question, `isAbilityUnlocked()` answers the achieved question |
| `AbilityUnlockEvent` | `event/ability/` | Existing event fired when an ability is unlocked. Still fired the same way after the refactor |
| `OnAbilityUnlockListener` | `listener/ability/` | Existing unlock messaging + loadout auto-add. Unchanged |
| `OnSkillLevelUpListener` | `listener/skill/` | Calls `getUnlockLevel()` today. Refactored to call `getUnlockCondition().isMet(holder)` |
| `AbilityLoreAppender` | `builder/item/ability/` | Fills the `{ability-unlock-level}` placeholder today. Refactored to use the condition's localized description |
| `AbilitySortType` | `gui/ability/` | Sorts by `getUnlockLevel()` for the `MOST_RELEVANT` mode. Refactored to fall back to a "by ascending progress" sort |
| `SkillBookConsumeListener` | `listener/item/` | Sets `AbilityUnlockedAttribute` directly. Continues to do so — books bypass the condition by design (see [Section 5.2](#52-skillbookunlockcondition)) |
| `McRPGLocalizationManager` | `localization/` | Used to resolve condition descriptions and labels |
| `LocalizationKey` | `configuration/file/localization/` | Gains new condition-display route constants |

---

## 3. Design Decisions

### 3.1 One condition per ability

Each `UnlockableAbility` returns exactly one `UnlockCondition` from `getUnlockCondition()`. To express "A AND B" or "A OR B", callers use the [composite conditions](#8-composite-conditions) (`AllOfUnlockCondition`, `AnyOfUnlockCondition`).

**Rationale:** Multiple returned conditions force every caller (GUI, sweep, lore) to know whether to AND or OR them. A single composite makes the combination logic the ability author's decision, encoded once at construction time. The GUI then renders one tree.

### 3.2 Conditions are pure read-only views over `AbilityHolder` state

A condition does not own per-player state and does not write to the database. `isMet(holder)` and `getProgress(holder)` derive everything from the holder's existing state (skill levels, attributes, etc.). Conditions are constructed once when the ability is constructed and shared across all holders.

**Rationale:** Ability objects are shared singletons (see CLAUDE.md anti-pattern: "No ability state stored on the ability object"). Conditions follow the same rule. This also makes conditions trivially safe to call from any thread that has a stable `AbilityHolder` reference.

### 3.3 Display is locale-aware, value access is not

`getDisplayDescription(McRPGPlayer)` and `getDisplayLabel(McRPGPlayer)` take a player so the localization manager can resolve text in the player's locale chain. `isMet(AbilityHolder)` and `getProgress(AbilityHolder)` take the broader `AbilityHolder` — these are pure logical checks usable for any holder type, including non-player holders that may exist in the future.

**Rationale:** Display is human-facing; logic is not. Coupling logic methods to `McRPGPlayer` would break the (future) ability to evaluate conditions for non-player ability holders.

### 3.4 Books bypass the condition, they don't satisfy it

A `SkillBookUnlockCondition` always returns `isMet() = false`. When the player consumes a book, `SkillBookConsumeListener` directly flips the `AbilityUnlockedAttribute` to `true` and fires `AbilityUnlockEvent`. The condition is never consulted during consumption.

**Rationale:** Skill books are an *alternate* unlock path that exists alongside the condition. Trying to model "the player has a book in their hand" as the condition itself would require the condition to inspect inventory and re-evaluate every interact — an enormous footprint for the GUI display. Treating the condition as "what the player would have to do to unlock without a book" gives the GUI a stable, descriptive answer ("Obtain from Riptide Guardian") that doesn't flicker as inventory changes. The actual unlock happens through the existing `AbilityUnlockedAttribute` flow, which is the canonical source of truth.

### 3.5 No condition registry, no condition serialization

Conditions are constructed at ability-construction time in Java. They are not registered by `NamespacedKey`, not loaded from YAML, and not serialized. Server owners customize their *display* via the localization YAML, not their *type* via config.

**Rationale:** This is consistent with how ability components are wired (in the ability constructor, by Java type), not how quest reward types or objective types are wired (by registered key, configurable from YAML). The ability author knows what the unlock requirement is — server owners do not get to change "Phase Shift is unlocked by a skill book" into "Phase Shift is unlocked at Mining level 50". That kind of swap is a content-pack decision, not a config-file decision.

If a future need arises to expose conditions as content-pack-registerable (e.g., a `ContentExpansion` that wants to add new unlock paths), the interface is forward-compatible — a registry can be added without changing the interface.

### 3.6 `isMet()` returns false until the condition's underlying state changes

A condition returns the *current* state. It does not "transition" on its own — there is no `onMet` callback, no event fired from the condition itself. The login sweep and `OnSkillLevelUpListener` are responsible for *observing* met conditions and firing `AbilityUnlockEvent`.

**Rationale:** Decouples *evaluation* from *side-effects*. The same condition can be evaluated in the GUI (for display only, no unlock should fire), at login (sweep, may fire unlock), and on skill level-up (existing flow, may fire unlock). If conditions self-fired, callers would need a "dry-run" flag.

### 3.7 Progress is a hint, not contract

`getProgress(holder)` returns a `double` in `[0.0, 1.0]` for progress-bar rendering. Conditions that have no natural progress (e.g., `SkillBookUnlockCondition`) return `0.0` until `isMet()` becomes true, then `1.0`. This is a soft API used by GUI surfaces — callers must not infer "almost unlocked" from it.

**Rationale:** Some conditions are binary (achievement, quest completed, book consumed). Some are gradual (skill level approaches threshold). The GUI can choose to render a bar or just a check; it does not depend on the condition for correctness, only for visualization.

---

## 4. UnlockCondition Interface

**New file:** `src/main/java/us/eunoians/mcrpg/ability/unlock/UnlockCondition.java`

```java
package us.eunoians.mcrpg.ability.unlock;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;

/**
 * Declares the requirement that must be satisfied before a holder may unlock
 * an {@link us.eunoians.mcrpg.ability.impl.type.UnlockableAbility}.
 * <p>
 * Conditions are constructed once when the ability is constructed and are
 * shared across every holder. They must not carry per-holder state — all
 * state lives on the {@link AbilityHolder} they evaluate against.
 * <p>
 * A condition answers three independent questions:
 * <ul>
 *   <li>{@link #isMet(AbilityHolder)} — is the holder currently eligible to
 *       unlock the ability through this path? Drives the login sweep and the
 *       skill level-up unlock flow.</li>
 *   <li>{@link #getDisplayDescription(McRPGPlayer)} and
 *       {@link #getDisplayLabel(McRPGPlayer)} — how should the requirement be
 *       rendered for the player? Used by the ability info GUI and item lore.</li>
 *   <li>{@link #getProgress(AbilityHolder)} — what fraction of the requirement
 *       is satisfied? Used by progress-bar rendering surfaces. Conditions that
 *       have no natural progress should return {@code 0.0} until met and
 *       {@code 1.0} when met.</li>
 * </ul>
 * <p>
 * Conditions never themselves cause an ability to become unlocked. The unlock
 * side-effects (setting {@code AbilityUnlockedAttribute}, firing
 * {@link us.eunoians.mcrpg.event.ability.AbilityUnlockEvent}) are owned by
 * the listeners that observe state changes (skill level-up, login sweep,
 * skill book consumption).
 */
public interface UnlockCondition {

    /**
     * Whether the holder currently satisfies this condition.
     * <p>
     * Implementations must be pure with respect to the holder — calling
     * {@code isMet} must not mutate holder state or schedule side-effects.
     *
     * @param holder the ability holder to evaluate against
     * @return {@code true} if the holder meets the requirement
     */
    boolean isMet(@NotNull AbilityHolder holder);

    /**
     * The full localized description of this requirement, suitable for
     * multi-line lore or GUI tooltip rendering. The component is resolved
     * through {@link us.eunoians.mcrpg.localization.McRPGLocalizationManager}
     * using the player's locale chain.
     *
     * @param player the player whose locale chain drives the rendering
     * @return the localized description component
     */
    @NotNull
    Component getDisplayDescription(@NotNull McRPGPlayer player);

    /**
     * A short localized label for compact rendering (e.g., one-line sort hints,
     * sidebar entries). Defaults to {@link #getDisplayDescription(McRPGPlayer)}
     * when the implementation has no shorter form.
     *
     * @param player the player whose locale chain drives the rendering
     * @return the localized label component
     */
    @NotNull
    default Component getDisplayLabel(@NotNull McRPGPlayer player) {
        return getDisplayDescription(player);
    }

    /**
     * Progress toward the requirement as a value in {@code [0.0, 1.0]}. A
     * condition with no natural progress should return {@code 0.0} until met
     * and {@code 1.0} when met. Used only by progress-bar rendering surfaces
     * — callers must not derive correctness from this value.
     *
     * @param holder the ability holder to evaluate against
     * @return progress fraction
     */
    default double getProgress(@NotNull AbilityHolder holder) {
        return isMet(holder) ? 1.0 : 0.0;
    }
}
```

### 4.1 Design Notes

- **Single-method core (`isMet`)**: the only logical contract. Display methods are presentational and have safe defaults.
- **No throws clause:** `isMet` is meant to be called from GUI render paths and login hot paths. Failures must be handled locally (e.g., a missing skill returns `false`, not throws).
- **`AbilityHolder` (not `SkillHolder`) parameter:** condition evaluation must work for any future non-player holder. Concrete conditions that need skill data (like `SkillLevelUnlockCondition`) downcast internally.

---

## 5. Built-In Implementations

This LLD ships two concrete conditions. Future LLDs add the rest.

### 5.1 SkillLevelUnlockCondition

**New file:** `src/main/java/us/eunoians/mcrpg/ability/unlock/SkillLevelUnlockCondition.java`

The behavior-preserving migration target for every ability that currently uses `getUnlockLevel()`.

```java
package us.eunoians.mcrpg.ability.unlock;

import com.diamonddagger590.mccore.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.Skill;

import java.util.Map;

/**
 * Met when a {@link SkillHolder} reaches a configured level in a target skill.
 * Drop-in replacement for the legacy {@code getUnlockLevel()} +
 * {@code checkIfAbilityCanBeUnlocked()} pair.
 */
public final class SkillLevelUnlockCondition implements UnlockCondition {

    private final NamespacedKey skillKey;
    private final int requiredLevel;

    public SkillLevelUnlockCondition(@NotNull NamespacedKey skillKey, int requiredLevel) {
        this.skillKey = skillKey;
        this.requiredLevel = requiredLevel;
    }

    @NotNull
    public NamespacedKey getSkillKey() {
        return skillKey;
    }

    public int getRequiredLevel() {
        return requiredLevel;
    }

    @Override
    public boolean isMet(@NotNull AbilityHolder holder) {
        if (!(holder instanceof SkillHolder skillHolder)) {
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

    @Override
    public double getProgress(@NotNull AbilityHolder holder) {
        if (!(holder instanceof SkillHolder skillHolder) || requiredLevel <= 0) {
            return 0.0;
        }
        Skill skill = resolveSkill();
        if (skill == null) {
            return 0.0;
        }
        return skillHolder.getSkillHolderData(skill)
                .map(data -> Math.min(1.0, data.getCurrentLevel() / (double) requiredLevel))
                .orElse(0.0);
    }

    @Override
    @NotNull
    public Component getDisplayDescription(@NotNull McRPGPlayer player) {
        McRPG plugin = McRPG.getInstance();
        McRPGLocalizationManager localizationManager = plugin.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION);
        Skill skill = resolveSkill();
        String skillName = skill != null
                ? plugin.getMiniMessage().serialize(skill.getColoredName(player))
                : skillKey.getKey();
        return localizationManager.getLocalizedMessageAsComponent(
                player,
                LocalizationKey.UNLOCK_CONDITION_SKILL_LEVEL_DESCRIPTION,
                Map.of("skill", skillName, "level", String.valueOf(requiredLevel)));
    }

    @Override
    @NotNull
    public Component getDisplayLabel(@NotNull McRPGPlayer player) {
        McRPG plugin = McRPG.getInstance();
        McRPGLocalizationManager localizationManager = plugin.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION);
        Skill skill = resolveSkill();
        String skillName = skill != null
                ? plugin.getMiniMessage().serialize(skill.getColoredName(player))
                : skillKey.getKey();
        return localizationManager.getLocalizedMessageAsComponent(
                player,
                LocalizationKey.UNLOCK_CONDITION_SKILL_LEVEL_LABEL,
                Map.of("skill", skillName, "level", String.valueOf(requiredLevel)));
    }

    private Skill resolveSkill() {
        return McRPG.getInstance().registryAccess()
                .registry(McRPGRegistryKey.SKILL)
                .getRegisteredSkill(skillKey);
    }
}
```

#### Design Notes

- **Stores the `NamespacedKey`, not the `Skill` itself.** Skills are registered at startup; resolving lazily means tests and reload flows are simpler. The lookup is a registry hit, not a database call.
- **Defensive null-skill handling.** If the skill is unregistered (a misconfigured content expansion, or a deleted skill on reload), `isMet` returns `false` and the label degrades to the raw key string. The ability remains permanently locked rather than crashing the GUI.
- **`getColoredName(player)` for the skill display** — required by the project's third-party-skill locale color rules in `CLAUDE.md`.
- **Progress saturates at 1.0** — a player past the required level renders as full, not >100%.

### 5.2 SkillBookUnlockCondition

**New file:** `src/main/java/us/eunoians/mcrpg/ability/unlock/SkillBookUnlockCondition.java`

The condition used by abilities whose only unlock path is consuming a skill book (Phase Shift, Whirlpool, etc. in LLD-6). Always returns `isMet() = false` — see [Design Decision 3.4](#34-books-bypass-the-condition-they-dont-satisfy-it).

```java
package us.eunoians.mcrpg.ability.unlock;

import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.route.Route;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Map;

/**
 * Represents an ability whose only unlock path is consuming a skill book.
 * <p>
 * Always returns {@code isMet() = false} — the actual unlock happens through
 * {@code SkillBookConsumeListener} flipping the
 * {@code AbilityUnlockedAttribute} directly. This condition exists so that
 * GUI surfaces have a stable, descriptive answer for "how do I unlock this?"
 * ("Obtain from <source>") while inventory contents shift.
 */
public final class SkillBookUnlockCondition implements UnlockCondition {

    private final Route sourceDisplayKey;

    /**
     * @param sourceDisplayKey the localization route resolving to the source
     *                         name shown to the player (e.g. "Riptide Guardian").
     *                         The route must exist in every bundled locale.
     */
    public SkillBookUnlockCondition(@NotNull Route sourceDisplayKey) {
        this.sourceDisplayKey = sourceDisplayKey;
    }

    @NotNull
    public Route getSourceDisplayKey() {
        return sourceDisplayKey;
    }

    @Override
    public boolean isMet(@NotNull AbilityHolder holder) {
        return false;
    }

    @Override
    @NotNull
    public Component getDisplayDescription(@NotNull McRPGPlayer player) {
        return resolveLocalized(player, LocalizationKey.UNLOCK_CONDITION_SKILL_BOOK_DESCRIPTION);
    }

    @Override
    @NotNull
    public Component getDisplayLabel(@NotNull McRPGPlayer player) {
        return resolveLocalized(player, LocalizationKey.UNLOCK_CONDITION_SKILL_BOOK_LABEL);
    }

    private Component resolveLocalized(@NotNull McRPGPlayer player, @NotNull Route routeKey) {
        McRPG plugin = McRPG.getInstance();
        McRPGLocalizationManager localizationManager = plugin.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION);
        String sourceName = localizationManager.getLocalizedMessage(player, sourceDisplayKey);
        return localizationManager.getLocalizedMessageAsComponent(
                player, routeKey, Map.of("source", sourceName));
    }
}
```

#### Design Notes

- **`sourceDisplayKey` is a `Route`, not a hardcoded string.** Ability authors register a localization key for their source (e.g. `ability.skill-book.source.riptide-guardian`) and pass it. Translators control the text.
- **No source `NamespacedKey` field.** The condition does not need a programmatic source identifier — only a display name. Adding a key field would imply queryable behavior the condition does not provide.

---

## 6. UnlockableAbility Refactor

**Modified file:** `src/main/java/us/eunoians/mcrpg/ability/impl/type/UnlockableAbility.java`

### 6.1 Refactored Interface

```java
package us.eunoians.mcrpg.ability.impl.type;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.AbilityData;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityUnlockedAttribute;
import us.eunoians.mcrpg.ability.unlock.UnlockCondition;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

import java.util.Set;

/**
 * Any ability whose availability is gated behind a requirement.
 * <p>
 * The requirement is described by an {@link UnlockCondition} returned from
 * {@link #getUnlockCondition()}. The condition is consulted by the login
 * sweep, the skill level-up listener, and GUI rendering surfaces. The actual
 * unlock state lives on the holder as an {@code AbilityUnlockedAttribute}
 * and is observed via {@link #isAbilityUnlocked(AbilityHolder)}.
 */
public interface UnlockableAbility extends Ability {

    /**
     * The requirement that must be satisfied before this ability may be
     * unlocked through its primary path. Skill books and other alternate
     * unlock paths bypass this condition.
     */
    @NotNull
    UnlockCondition getUnlockCondition();

    @NotNull
    @Override
    default Set<NamespacedKey> getApplicableAttributes() {
        return Set.of(AbilityAttributeRegistry.ABILITY_TOGGLED_OFF_ATTRIBUTE_KEY,
                AbilityAttributeRegistry.ABILITY_UNLOCKED_ATTRIBUTE);
    }

    /**
     * Whether this ability is currently unlocked for the holder. Reads the
     * {@code AbilityUnlockedAttribute} — the canonical source of truth.
     */
    default boolean isAbilityUnlocked(@NotNull AbilityHolder abilityHolder) {
        var abilityDataOptional = abilityHolder.getAbilityData(this);
        if (abilityDataOptional.isPresent()) {
            AbilityData abilityData = abilityDataOptional.get();
            var attributeOptional = abilityData.getAbilityAttribute(
                    AbilityAttributeRegistry.ABILITY_UNLOCKED_ATTRIBUTE);
            if (attributeOptional.isPresent()
                    && attributeOptional.get() instanceof AbilityUnlockedAttribute attribute) {
                return attribute.getContent();
            }
        }
        return false;
    }
}
```

### 6.2 What was removed

| Member | Replacement |
|---|---|
| `int getUnlockLevel()` | `getUnlockCondition()` — callers that need the numeric level cast: `if (cond instanceof SkillLevelUnlockCondition s) { ... s.getRequiredLevel() ... }` |
| `boolean checkIfAbilityCanBeUnlocked(SkillHolder, Skill)` | `getUnlockCondition().isMet(holder)` |

### 6.3 What stayed identical

- `getApplicableAttributes()` — same default
- `isAbilityUnlocked(AbilityHolder)` — same implementation, same semantics

---

## 7. TierableAbility Boundary

`TierableAbility` extends `UnlockableAbility` and currently defaults `getUnlockLevel()` to `getUnlockLevelForTier(1)`. The per-tier methods (`getUnlockLevelForTier(int)`, `getCurrentAbilityTier()`, etc.) are unaffected by this LLD — they belong to the tier-upgrade flow (`AbilityUpgradeQuestSource` + `AbilityUpgradeQuestAttribute`), which is independent of "is this ability unlocked at all."

### 7.1 Refactored TierableAbility

```java
public interface TierableAbility extends UnlockableAbility {

    int getMaxTier();

    /** Skill level required to upgrade this ability to {@code tier}. */
    int getUnlockLevelForTier(int tier);

    @NotNull
    default Optional<NamespacedKey> getUpgradeQuestKey(int tier) {
        return Optional.empty();
    }

    /**
     * The default unlock condition for a tierable skill-based ability is
     * "reach the tier-1 unlock level in the owning skill."
     * Implementations that aren't {@code SkillAbility} (or that have a
     * non-level unlock) should override this.
     */
    @Override
    @NotNull
    default UnlockCondition getUnlockCondition() {
        if (this instanceof SkillAbility skillAbility) {
            return new SkillLevelUnlockCondition(
                    skillAbility.getSkillKey(), getUnlockLevelForTier(1));
        }
        throw new UnsupportedOperationException(
                "TierableAbility " + getAbilityKey()
                + " is not a SkillAbility — getUnlockCondition() must be overridden.");
    }

    @NotNull
    @Override
    default Set<NamespacedKey> getApplicableAttributes() {
        return Set.of(AbilityAttributeRegistry.ABILITY_TOGGLED_OFF_ATTRIBUTE_KEY,
                AbilityAttributeRegistry.ABILITY_UNLOCKED_ATTRIBUTE,
                AbilityAttributeRegistry.ABILITY_TIER_ATTRIBUTE_KEY,
                AbilityAttributeRegistry.ABILITY_QUEST_ATTRIBUTE);
    }

    default int getCurrentAbilityTier(@NotNull AbilityHolder abilityHolder) {
        // unchanged
    }
}
```

### 7.2 Why the per-tier methods stay

`getUnlockLevelForTier(int)` is consulted by 8 callsites that all gate **upgrade quest eligibility** at higher tiers (see [Section 9.3](#93-callsites-that-stay-on-the-old-per-tier-api)). Promoting per-tier gates to `UnlockCondition`s would force every upgrade-quest reward type and GUI slot to do polymorphic condition checks for a question they already answer with a single integer comparison.

The two systems address different lifecycle moments:
- **`UnlockCondition`** answers "is this ability available to me at all?" — the gate between locked and unlocked.
- **`getUnlockLevelForTier(int)`** answers "have I met the skill prerequisite to start the upgrade quest for tier N?" — a precondition inside the upgrade-quest flow.

Conflating them would break the upgrade-quest contribution layer that already exists.

If a future LLD wants per-tier conditions, it can add `getUnlockConditionForTier(int)` as an additive extension without rewriting the per-tier methods.

---

## 8. Composite Conditions

**New files:**
- `src/main/java/us/eunoians/mcrpg/ability/unlock/AllOfUnlockCondition.java`
- `src/main/java/us/eunoians/mcrpg/ability/unlock/AnyOfUnlockCondition.java`

When an ability needs to express "A AND B" or "A OR B", it constructs a composite. Both composites delegate display rendering to their children with a localized join phrase ("All of:", "Any of:").

### 8.1 AllOfUnlockCondition (skeleton)

```java
package us.eunoians.mcrpg.ability.unlock;

// imports omitted

public final class AllOfUnlockCondition implements UnlockCondition {

    private final List<UnlockCondition> children;

    public AllOfUnlockCondition(@NotNull List<UnlockCondition> children) {
        if (children.isEmpty()) {
            throw new IllegalArgumentException("AllOfUnlockCondition requires at least one child");
        }
        this.children = List.copyOf(children);
    }

    @NotNull
    public List<UnlockCondition> getChildren() {
        return children;
    }

    @Override
    public boolean isMet(@NotNull AbilityHolder holder) {
        for (UnlockCondition child : children) {
            if (!child.isMet(holder)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public double getProgress(@NotNull AbilityHolder holder) {
        // Min-progress — the laggard child gates the bar
        double min = 1.0;
        for (UnlockCondition child : children) {
            min = Math.min(min, child.getProgress(holder));
        }
        return min;
    }

    @Override
    @NotNull
    public Component getDisplayDescription(@NotNull McRPGPlayer player) {
        // Builds a "All of: <child1>, <child2>" component via
        // LocalizationKey.UNLOCK_CONDITION_ALL_OF_DESCRIPTION
        // and per-child getDisplayLabel(player)
    }

    @Override
    @NotNull
    public Component getDisplayLabel(@NotNull McRPGPlayer player) {
        return getDisplayDescription(player);
    }
}
```

### 8.2 AnyOfUnlockCondition

Identical shape, but:
- `isMet` short-circuits on first met
- `getProgress` returns max-progress (the closest child)
- Display joins with the "Any of:" key

### 8.3 Why ship both now (and not on-demand)

Composite conditions are zero-cost additions and prove the interface is composable. They are also the natural way to model future content like "complete the Riptide Guardian quest AND reach Fishing level 30" — the LLD that adds quest unlock will use them, and shipping them now means that LLD doesn't have to backport the composite.

---

## 9. UnlockableAbility Migration

### 9.1 Direct callers (must change)

Three callers consume the old methods directly:

| File | Current call | Replacement |
|---|---|---|
| `listener/skill/OnSkillLevelUpListener.java:88` | `unlockableAbility.getUnlockLevel() <= skillHolderData.getCurrentLevel()` | `unlockableAbility.getUnlockCondition().isMet(skillHolder)` |
| `gui/ability/AbilitySortType.java:95-96` | `Integer.compare(a.getUnlockLevel(), b.getUnlockLevel())` | Use `Comparator.comparingDouble((UnlockableAbility u) -> u.getUnlockCondition().getProgress(holder))` with a fallback by display-label string for stable ordering |
| `builder/item/ability/AbilityLoreAppender.java:106` | `Integer.toString(tierableAbility.getUnlockLevel())` for the `{ability-unlock-level}` placeholder | Replace the placeholder with `{ability-unlock-condition}` resolved via `tierableAbility.getUnlockCondition().getDisplayDescription(player)` |

### 9.2 The `{ability-unlock-level}` locale key change

The existing `{ability-unlock-level}` placeholder is referenced in the per-ability lore strings (e.g. `ability.ability-specific-localization.<ability>.locked-lore`). The placeholder is replaced with `{ability-unlock-condition}` in each bundled locale entry. Server owners with custom locale files keep working as long as they update the placeholder — a brief migration note goes into the in-tree `CLAUDE.md` Locale section.

This is the only player-visible string change in the migration. All other migrations are behavior-preserving.

### 9.3 Callsites that stay on the old per-tier API

These 8 callsites use `getUnlockLevelForTier(int)` and remain unchanged — they are tier-upgrade preconditions, not unlock conditions ([Section 7.2](#72-why-the-per-tier-methods-stay)):

- `quest/QuestManager.java:450`
- `quest/reward/builtin/AbilityUpgradeRewardType.java:284`
- `quest/reward/builtin/AbilityUpgradeNextTierRewardType.java:140`
- `gui/ability/slot/UpgradeQuestSlot.java:284`
- `util/filter/ability/AbilityUpgradeFilter.java:49`
- `builder/item/ability/AbilityLoreAppender.java:92,95`
- `entity/player/McRPGPlayer.java:158`
- `ability/impl/type/configurable/ConfigurableTierableAbility.java:60` (the default implementation itself)

### 9.4 Per-ability migration

Every concrete `UnlockableAbility` that is not a `TierableAbility` and not a `SkillAbility` must override `getUnlockCondition()` explicitly. Every `TierableAbility` that is also a `SkillAbility` (which is all of them today) gets the right default behavior from the refactored `TierableAbility.getUnlockCondition()` and needs no change. A compile-time CI check is not needed — the default `throws UnsupportedOperationException` ensures any miss surfaces immediately at startup when the ability is first queried.

---

## 10. GUI Integration

### 10.1 Locked-ability lore

`AbilityLoreAppender` builds the locked-state lore for abilities in the info GUI. Today it appends a single line "Unlock at <skill> level <n>" using the `{ability-unlock-level}` placeholder. The refactor:

1. Replaces the placeholder with `{ability-unlock-condition}` in every bundled locale entry that mentions it.
2. Resolves the placeholder with `MiniMessage.serialize(unlockableAbility.getUnlockCondition().getDisplayDescription(player))`.
3. For multi-line composite conditions (`AllOf` / `AnyOf` with many children), the description Component may contain `\n` — the lore builder splits on newlines and produces one lore line per child, preserving the existing line-wrapping behavior.

### 10.2 Ability sort

`AbilitySortType.MOST_RELEVANT` falls back to `getUnlockLevel()` for ordering. The replacement uses condition progress descending (closer-to-unlock first), then display-label alphabetical for stable ties. This is a soft UI change — players who relied on numeric-level sort within the "MOST_RELEVANT" view will see substantially the same order for skill-leveled abilities, and skill-book abilities sort to the end (progress = 0 for any unconsumed book).

### 10.3 No new GUI screen

This LLD does not introduce a dedicated "ability unlock requirements" screen. All condition display is folded into existing surfaces (ability info lore, ability sort). A future "Codex" / "Achievements" screen could surface conditions as its own first-class entry — out of scope here.

---

## 11. Login-Time Unlock Sweep

This LLD resolves open issue **#220** by adding a one-shot per-player sweep that checks every locked `UnlockableAbility` against its condition at login and fires `AbilityUnlockEvent` for any newly-met conditions.

### 11.1 OnPlayerLoadUnlockSweepListener

**New file:** `src/main/java/us/eunoians/mcrpg/listener/ability/OnPlayerLoadUnlockSweepListener.java`

Listens for `PlayerLoadEvent` (fired by `McRPGPlayerLoadTask` after player data is fully loaded). For each `UnlockableAbility` the player has data for:

```java
@EventHandler(priority = EventPriority.MONITOR)
public void onPlayerLoad(@NotNull PlayerLoadEvent event) {
    McRPGPlayer player = event.getMcRPGPlayer();
    AbilityRegistry abilityRegistry = /* resolve */;

    for (NamespacedKey abilityKey : abilityRegistry.getRegisteredAbilityKeys()) {
        Ability ability = abilityRegistry.getRegisteredAbility(abilityKey);
        if (!(ability instanceof UnlockableAbility unlockable)) {
            continue;
        }
        if (unlockable.isAbilityUnlocked(player)) {
            continue;
        }
        if (!unlockable.getUnlockCondition().isMet(player)) {
            continue;
        }
        // Same flow as OnSkillLevelUpListener — flip attribute, fire event,
        // schedule async save
        flipAttributeAndFire(player, unlockable);
    }
}
```

The flip-and-fire helper mirrors the existing `OnSkillLevelUpListener.handlePostLevelEvent` block — set the attribute, fire `AbilityUnlockEvent`, submit a `SkillDAO.savePlayerSkillData` to the database executor. This keeps the unlock side-effect logic in exactly two places (level-up and login sweep), with consumption being a third path that owns its own listener.

### 11.2 Why this is safe at login

- `PlayerLoadEvent` fires after all skill data is loaded and the player is registered in the manager — both prerequisites for `isMet()` to read correct state.
- The sweep runs on the main thread (same as `OnSkillLevelUpListener.handlePostLevelEvent`), so `AbilityUnlockEvent` listeners that expect main-thread semantics (the existing `OnAbilityUnlockListener` does) are not surprised.
- Each fired event triggers the existing loadout auto-add and unlock messaging. The player sees the same "You unlocked X" message they would have seen at the moment the condition was met.

### 11.3 Why this can't deadlock the join path

Once issue #227 lands (player loading moves to the DB executor), this listener's body is still pure CPU on the main thread after the load completes — it does not block on JDBC. The async save scheduled per unlock is fire-and-forget on the DB executor. No new cross-thread waits are introduced.

---

## 12. Localization Keys

### 12.1 LocalizationKey.java additions

**Modified file:** `src/main/java/us/eunoians/mcrpg/configuration/file/localization/LocalizationKey.java`

Add a new `UNLOCK_CONDITION_HEADER` section under the existing `ABILITY_HEADER`:

```java
// Unlock condition rendering (used by UnlockCondition.getDisplayDescription / getDisplayLabel)
private static final String UNLOCK_CONDITION_HEADER =
        toRoutePath(ABILITY_HEADER, "unlock-condition");
public static final Route UNLOCK_CONDITION_SKILL_LEVEL_DESCRIPTION =
        Route.fromString(toRoutePath(UNLOCK_CONDITION_HEADER, "skill-level.description"));
public static final Route UNLOCK_CONDITION_SKILL_LEVEL_LABEL =
        Route.fromString(toRoutePath(UNLOCK_CONDITION_HEADER, "skill-level.label"));
public static final Route UNLOCK_CONDITION_SKILL_BOOK_DESCRIPTION =
        Route.fromString(toRoutePath(UNLOCK_CONDITION_HEADER, "skill-book.description"));
public static final Route UNLOCK_CONDITION_SKILL_BOOK_LABEL =
        Route.fromString(toRoutePath(UNLOCK_CONDITION_HEADER, "skill-book.label"));
public static final Route UNLOCK_CONDITION_ALL_OF_DESCRIPTION =
        Route.fromString(toRoutePath(UNLOCK_CONDITION_HEADER, "all-of.description"));
public static final Route UNLOCK_CONDITION_ANY_OF_DESCRIPTION =
        Route.fromString(toRoutePath(UNLOCK_CONDITION_HEADER, "any-of.description"));
```

### 12.2 en_abilities.yml additions

**Modified file:** `src/main/resources/localization/english/en_abilities.yml`

Add the `unlock-condition` block under the existing `ability:` root:

```yaml
  # Configure how ability unlock conditions are described to the player.
  # Used by the ability info GUI lore and the login unlock sweep messaging.
  # Supports the placeholders listed alongside each entry.
  unlock-condition:
    skill-level:
      # Placeholders: <skill> (colored skill name), <level> (required level int)
      description: "<body>Reach <skill> <body>level <primary><level><body>."
      label: "<skill> <primary><level>"
    skill-book:
      # Placeholders: <source> (localized source name, e.g. "Riptide Guardian")
      description: "<body>Obtain a skill book from <primary><source><body>."
      label: "<primary><source>"
    # Composite condition prefixes. The actual child rendering is built in
    # Java by joining child labels — these strings provide the prefix only.
    all-of:
      # Placeholder: <children> (already-formatted comma-joined child labels)
      description: "<body>All of: <children>"
    any-of:
      # Placeholder: <children>
      description: "<body>Any of: <children>"
```

### 12.3 Skill book source keys

For each ability in LLD-6 (Phase Shift, etc.), the source name comes from a per-source localization route. The HLD documents these as needed; this LLD reserves the namespace:

```yaml
  # In en_abilities.yml under ability.skill-book:
  source:
    riptide-guardian: "Riptide Guardian"
    # Future sources are added here by the LLD that introduces them.
```

The `Route` for `riptide-guardian` is added to `LocalizationKey` when LLD-6 (or another LLD) first uses a `SkillBookUnlockCondition` for that source. This LLD adds none — `SkillBookUnlockCondition` is fully ready, but no ability uses it yet.

### 12.4 Placeholder migration for `{ability-unlock-level}`

The existing per-ability locked-lore entries reference `<ability-unlock-level>`. They are updated in-place to `<ability-unlock-condition>` across `en_abilities.yml`:

```yaml
# Before
locked-lore:
  - "<gray>Reach <gold>{skill}</gold> level <gold>{ability-unlock-level}</gold>."
# After
locked-lore:
  - "<gray><ability-unlock-condition>"
```

Server owners with overridden locale files who relied on `<ability-unlock-level>` will see the raw placeholder text in their GUI after upgrade until they switch to `<ability-unlock-condition>`. Documented in the migration note in `CLAUDE.md`'s locale section.

---

## 13. Bootstrap Registration

This LLD adds two listener registrations:

### 13.1 Sweep listener registration

**Modified file:** `src/main/java/us/eunoians/mcrpg/bootstrap/McRPGListenerRegistrar.java`

```java
// Login-time unlock condition sweep — fires AbilityUnlockEvent for any
// conditions newly met while the player was offline.
Bukkit.getPluginManager().registerEvents(new OnPlayerLoadUnlockSweepListener(), plugin);
```

**Placement:** alongside the existing `OnAbilityUnlockListener` registration, so all ability-unlock-related listeners are grouped.

### 13.2 No registry needed

`UnlockCondition` is not registered. Conditions are constructed by ability authors in their ability's constructor, like ability components. There is no startup registration step for conditions.

---

## 14. Edge Cases & Graceful Degradation

| Scenario | Behavior |
|---|---|
| Ability's referenced skill is not registered (deleted, content-pack removed) | `SkillLevelUnlockCondition.isMet` returns `false`. The ability remains locked. The label degrades to the raw key string. No crash. |
| Holder is not a `SkillHolder` (e.g. a future non-player holder) | `SkillLevelUnlockCondition.isMet` returns `false` and `getProgress` returns `0.0`. The ability simply cannot be unlocked for that holder type. |
| `AllOfUnlockCondition` constructed with zero children | Throws `IllegalArgumentException` at construction time. Forces ability authors to be explicit. |
| Sweep finds an ability already unlocked | Short-circuits via `isAbilityUnlocked(holder)` before evaluating the condition. No duplicate events. |
| Sweep fires `AbilityUnlockEvent` for a previously-unlockable ability that the player meets via skill level (already met at last logout, missed by old code path) | One event fires, one message sent, one save scheduled. The existing `OnAbilityUnlockListener` handles loadout auto-add. This is the desired behavior for issue #220. |
| `SkillBookUnlockCondition` constructed with a `Route` that does not exist in any locale | `getDisplayDescription` raises `NoLocalizationContainsMessageException` from `McRPGLocalizationManager`. The ability author sees the failure at first GUI render — same failure mode as any other missing locale key. |
| Two condition implementations defined with the same numeric semantics (e.g. someone subclasses `SkillLevelUnlockCondition`) | No problem — `instanceof` checks in callers like `AbilitySortType` work against the base type. |
| A condition mutates holder state inside `isMet` | Violates the interface contract — anti-pattern. Documented in Javadoc. There is no enforcement; the contract is by convention. |
| Player logs in with a met condition while the database executor is still warming up | The sweep listener fires on `PlayerLoadEvent`, which is only fired after the load task completes — by definition, the database executor is already available to receive the save submission. No race. |

---

## 15. Test Plan

### 15.1 Pure unit tests (src/test/java)

| Test Class | Coverage |
|---|---|
| `SkillLevelUnlockConditionTest` | `isMet` for holder at, below, and above the required level. `isMet` for holder with no data for the skill (returns false). `isMet` for non-`SkillHolder` (returns false). `getProgress` returns linear fraction `currentLevel / requiredLevel` capped at 1.0. `getProgress` returns 0.0 for `requiredLevel <= 0`. Display description and label resolve to non-empty Components and substitute the `<skill>` and `<level>` placeholders. Skill resolution caches/lookups handle a missing skill registration gracefully (returns false / fallback display). |
| `SkillBookUnlockConditionTest` | `isMet` always returns false. `getProgress` always returns 0.0. Display description uses `<source>` placeholder resolved from the configured route. |
| `AllOfUnlockConditionTest` | Zero children → `IllegalArgumentException`. All-met → true, progress 1.0. One unmet → false, progress = min of children. Display description includes every child label. |
| `AnyOfUnlockConditionTest` | All-unmet → false, progress = max of children. One met → true, progress 1.0. Short-circuits on first met (use a child that throws if called to assert ordering). |
| `TierableAbilityUnlockConditionDefaultTest` | A test fixture `SkillAbility & TierableAbility` returns a `SkillLevelUnlockCondition` with the tier-1 unlock level. A non-`SkillAbility` `TierableAbility` triggers the `UnsupportedOperationException`. |

### 15.2 Tests requiring MockBukkit (extend `McRPGBaseTest`)

| Test Class | Coverage |
|---|---|
| `OnPlayerLoadUnlockSweepListenerTest` | Login with no eligible abilities → no events. Login with one eligible ability → one `AbilityUnlockEvent` fired, `AbilityUnlockedAttribute` set true, save submitted to DB executor. Login with an already-unlocked ability → no event fired (short-circuit). Login with an ability whose condition is `SkillBookUnlockCondition` (always false) → no event. |
| `AbilityLoreAppenderUnlockConditionTest` | Renders the `<ability-unlock-condition>` placeholder for a `SkillLevelUnlockCondition` to "Reach Swords level 15". Renders for a `SkillBookUnlockCondition` to "Obtain a skill book from Riptide Guardian". |

### 15.3 Manual testing (Paper server)

| Scenario | Verification |
|---|---|
| Lock a skill ability (admin command sets unlock attribute false), gain a skill level past the threshold | Unlock message fires, ability auto-adds to loadout if space, lore in GUI shows unlocked state |
| Lock a skill ability, log out, raise the player's skill level via admin command, log back in | Login sweep fires unlock event, player sees the unlock message right after join |
| Ability info GUI for a skill-level-locked ability | Lore reads "Reach Swords level 15" (or the localized equivalent) |
| Ability info GUI for an ability constructed with `SkillBookUnlockCondition` (test ability for the manual check) | Lore reads "Obtain a skill book from Riptide Guardian" |
| Consume a skill book for the same ability | Ability unlocks via `SkillBookConsumeListener`, condition's `isMet` is irrelevant |
| `/mcrpg admin reload` after editing the `unlock-condition` locale block | New descriptions take effect in the GUI |

---

## 16. File Manifest

### New files

| File | Type | Description |
|---|---|---|
| `ability/unlock/UnlockCondition.java` | Interface | The polymorphic unlock-requirement contract |
| `ability/unlock/SkillLevelUnlockCondition.java` | Implementation | Met when a holder reaches a level in a target skill |
| `ability/unlock/SkillBookUnlockCondition.java` | Implementation | Display-only — actual unlock happens via book consumption |
| `ability/unlock/AllOfUnlockCondition.java` | Composite | Conjunction of child conditions |
| `ability/unlock/AnyOfUnlockCondition.java` | Composite | Disjunction of child conditions |
| `listener/ability/OnPlayerLoadUnlockSweepListener.java` | Listener | Fires `AbilityUnlockEvent` for newly-met conditions on player load |

All Java files under `src/main/java/us/eunoians/mcrpg/`.

Test files mirror the structure under `src/test/java/us/eunoians/mcrpg/ability/unlock/` and `src/test/java/us/eunoians/mcrpg/listener/ability/`.

### Modified files

| File | Change |
|---|---|
| `ability/impl/type/UnlockableAbility.java` | Remove `getUnlockLevel` and `checkIfAbilityCanBeUnlocked`. Add `getUnlockCondition()`. |
| `ability/impl/type/TierableAbility.java` | Drop `default getUnlockLevel`. Add `default getUnlockCondition()` that returns a `SkillLevelUnlockCondition` for `SkillAbility` tierables. |
| `listener/skill/OnSkillLevelUpListener.java` | Replace `getUnlockLevel() <= currentLevel` with `getUnlockCondition().isMet(skillHolder)`. |
| `gui/ability/AbilitySortType.java` | Replace the `Integer.compare(getUnlockLevel(), getUnlockLevel())` branch with a `getProgress`-based comparator. |
| `builder/item/ability/AbilityLoreAppender.java` | Replace `{ability-unlock-level}` placeholder population with `{ability-unlock-condition}` resolved from `getUnlockCondition().getDisplayDescription(player)`. |
| `configuration/file/localization/LocalizationKey.java` | Add `UNLOCK_CONDITION_*` route constants. |
| `src/main/resources/localization/english/en_abilities.yml` | Add `ability.unlock-condition.*` entries. Migrate every `<ability-unlock-level>` placeholder to `<ability-unlock-condition>`. Reserve `ability.skill-book.source` block (currently empty). |
| `bootstrap/McRPGListenerRegistrar.java` | Register `OnPlayerLoadUnlockSweepListener`. |
| `CLAUDE.md` | Add `UnlockCondition` to the Domain Terminology table. Note the `<ability-unlock-level>` → `<ability-unlock-condition>` placeholder migration in the Localization section. |

### Not modified (used as-is)

| File | Role |
|---|---|
| `ability/AbilityRegistry.java` | Iterated by the login sweep |
| `ability/attribute/AbilityUnlockedAttribute.java` | Canonical source of truth for "is unlocked" |
| `event/ability/AbilityUnlockEvent.java` | Fired by sweep and skill-level-up listener |
| `listener/ability/OnAbilityUnlockListener.java` | Handles unlock messaging and loadout auto-add |
| `listener/item/SkillBookConsumeListener.java` | Continues to bypass the condition (Section 3.4) |
| `quest/QuestManager.java`, `quest/reward/builtin/AbilityUpgrade*.java`, `gui/ability/slot/UpgradeQuestSlot.java`, `util/filter/ability/AbilityUpgradeFilter.java`, `entity/player/McRPGPlayer.java` | All callers of `getUnlockLevelForTier(int)`. Per-tier flow is out of scope (Section 7). |

---

## 17. Future LLD Notes

- **LLD-6 (Player Abilities)** — uses `SkillBookUnlockCondition` shipped in this LLD. Each new player-ability constructor reads roughly:
  ```java
  @Override
  public UnlockCondition getUnlockCondition() {
      return new SkillBookUnlockCondition(LocalizationKey.SKILL_BOOK_SOURCE_RIPTIDE_GUARDIAN);
  }
  ```
  The source route key is added in the LLD-6 locale section when that LLD introduces it.

- **`QuestCompleteUnlockCondition`** — when a future LLD introduces "complete this quest to unlock this ability," it adds a third implementation alongside `SkillLevelUnlockCondition` and `SkillBookUnlockCondition`. The login sweep already handles it without changes (any condition that becomes `isMet` after login fires the unlock).

- **`AchievementUnlockCondition`** — same shape. Requires the achievement system to exist first.

- **Per-tier conditions (`getUnlockConditionForTier(int)`)** — if the tier-upgrade flow ever needs to express non-skill-level preconditions ("upgrade Bleed tier 4 requires Vampire to be unlocked"), the additive method goes on `TierableAbility` without touching the base `UnlockCondition` interface.

- **Condition registry for content-pack extensibility** — if a future need arises to let `ContentExpansion`s register `UnlockCondition` types keyed by `NamespacedKey` (for YAML-driven condition wiring), the registry can be added without changing the interface. None of the current callers depend on instance-of checks for correctness — they depend on the interface methods.

- **Issue #220 closure** — this LLD's login sweep ([Section 11](#11-login-time-unlock-sweep)) directly resolves the open issue.
