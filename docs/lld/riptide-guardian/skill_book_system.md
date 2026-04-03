# Low-Level Design: Skill Book System (LLD-3)

**Status:** Draft
**Date:** 2026-04-03
**HLD Reference:** [Riptide Guardian HLD](../../hld/riptide-guardian/riptide_guardian.md), Section 5
**Scope:** Skill book factory, consumption listener, consume event, quest reward type, localization keys, bootstrap registration

---

## Table of Contents

1. [Overview](#1-overview)
2. [Existing Infrastructure (from LLD-1)](#2-existing-infrastructure-from-lld-1)
3. [Design Decisions](#3-design-decisions)
4. [SkillBookFactory](#4-skillbookfactory)
5. [McRPGSkillBookDrop Refactor](#5-mcrpgskillbookdrop-refactor)
6. [SkillBookConsumeEvent](#6-skillbookconsumeevent)
7. [SkillBookConsumeListener](#7-skillbookconsumelistener)
8. [SkillBookRewardType](#8-skillbookrewardtype)
9. [Localization Keys](#9-localization-keys)
10. [Bootstrap Registration](#10-bootstrap-registration)
11. [Edge Cases & Graceful Degradation](#11-edge-cases--graceful-degradation)
12. [Test Plan](#12-test-plan)
13. [File Manifest](#13-file-manifest)
14. [Future LLD Notes](#14-future-lld-notes)

---

## 1. Overview

The Skill Book System provides a unified path for creating, distributing, and consuming skill book items that unlock player abilities. Skill books are physical `ItemStack`s (enchanted books with PDC tags) that players right-click to consume, unlocking the associated ability.

**This LLD produces code.** All classes, configs, and tests described here are implementation-ready.

### Boundary with LLD-1 (MythicMobs Binding System)

LLD-1 introduced `McRPGSkillBookDrop`, a MythicMobs custom drop type that creates skill book items inline. This LLD extracts the item creation logic into a shared `SkillBookFactory` and refactors `McRPGSkillBookDrop` to delegate to it. The drop type registration in MythicMobs is unchanged.

### Boundary with LLD-2 (Fishing Mob Spawn System)

LLD-2 owns mob spawning and death tracking. Loot drops (including skill books) are entirely owned by MythicMobs' drop table system. This LLD does not modify any LLD-2 classes.

### Boundary with LLD-4 (MythicMobs Example Configuration)

A separate LLD-4 will cover the bundled MythicMobs example configuration (`RiptideGuardian.yml` shipped in `src/main/resources/mythicmobs/`). This LLD defines the item format and consumption logic that the example config's drop tables will produce.

### LLD Renumbering

The HLD's original LLD-4 (UnlockCondition) becomes **LLD-5**, and the original LLD-5 (Player Abilities) becomes **LLD-6**.

---

## 2. Existing Infrastructure (from LLD-1)

These classes already exist in the codebase and are relevant to this LLD:

| Class | Location | Role in LLD-3 |
|---|---|---|
| `McRPGSkillBookDrop` | `external/mythicmobs/` | MythicMobs custom drop type — refactored to delegate to `SkillBookFactory` |
| `MythicMobsHook` | `external/mythicmobs/` | Presence check — skill book consumption works regardless of MM presence |
| `AbilityRegistry` | `ability/` | Validates ability keys during consumption |
| `AbilityUnlockedAttribute` | `ability/attribute/` | Checked/set during consumption to track unlock state |
| `AbilityUnlockEvent` | `event/ability/` | Existing event fired by unlock flow — reused by consumption |
| `OnAbilityUnlockListener` | `listener/ability/` | Existing listener that handles unlock messaging and loadout auto-add |
| `QuestRewardType` | `quest/reward/` | Interface implemented by `SkillBookRewardType` |
| `BuiltinRewardTypes` | `quest/reward/builtin/` | Registration point for `SkillBookRewardType` |
| `ItemRewardType` | `quest/reward/builtin/` | Pattern reference for quest reward type implementation |
| `LocalizationKey` | `configuration/file/localization/` | Route constants for localization — gains skill book keys |

---

## 3. Design Decisions

### 3.1 Centralized Factory for Consistent Items

All skill book sources (MythicMobs drops, quest rewards, admin commands, future NPC trades) must produce identical items. `SkillBookFactory` is the single point of item creation. `McRPGSkillBookDrop` is refactored to delegate to it rather than building items inline.

**Rationale:** Without a factory, each source would duplicate PDC tag logic. A tag format change would require updating every source independently — a maintenance hazard.

### 3.2 Ability NamespacedKey Instead of Skill Name

The existing `McRPGSkillBookDrop` stores a freeform string in `mcrpg:skill_book_skill`. This LLD introduces a new PDC key `mcrpg:skill_book_ability` that stores a proper ability `NamespacedKey` string (e.g. `"mcrpg:phase_shift"`). The old `mcrpg:skill_book_skill` key is removed.

**Rationale:** Ability keys are the canonical identifier in `AbilityRegistry`. Storing the key directly avoids ambiguous string-to-ability mapping and supports abilities from third-party content expansions that use non-`mcrpg` namespaces.

### 3.3 Consumption Reuses Existing Unlock Flow

When a skill book is consumed, the listener fires `AbilityUnlockEvent`. The existing `OnAbilityUnlockListener` handles messaging ("You have unlocked...") and auto-adding the ability to the loadout if space is available. This avoids duplicating unlock logic.

**Rationale:** The unlock flow already handles edge cases (loadout full, duplicate skill in loadout). Reusing it ensures consistent behavior regardless of how an ability is unlocked (level-up, skill book, future quest reward).

### 3.4 Physical Item Everywhere

Skill books are always physical `ItemStack`s granted to the player's inventory. Quest rewards grant the item (like `ItemRewardType`), not an instant unlock. This provides consistent UX — players always see, hold, and right-click skill books.

**Rationale:** A physical item creates a tangible reward moment. Players can trade, store, or choose when to consume. It also means the `SkillBookConsumeEvent` fires for all unlock paths, giving third-party plugins a single interception point.

### 3.5 SkillBookConsumeEvent is Separate from AbilityUnlockEvent

`SkillBookConsumeEvent` fires *before* the unlock. It is cancellable and carries the item stack. If cancelled, the item is not consumed and `AbilityUnlockEvent` is never fired. This separation lets plugins gate consumption (e.g., require a specific location, level, or currency) without interfering with the general unlock flow.

### 3.6 Item Display Text: Localized but Baked at Creation Time

Skill book display names and lore are resolved through the localization system (`McRPGLocalizationManager`) at creation time, not hardcoded. The factory has two resolution paths:

- **Player-aware path:** When a player context is available (e.g., quest rewards), `SkillBookFactory.createSkillBook(abilityKey, mcRPGPlayer, amount)` resolves text using the player's locale chain. The ability's localized display name is substituted into the `<ability>` placeholder.
- **Server-default path:** When no player is available (e.g., MythicMobs drops), `SkillBookFactory.createSkillBook(abilityKey, abilityDisplayName, amount)` resolves text using the server's default locale.

Server owners can customize skill book appearance (name, lore, colors) via the `ability.skill-book.item-name` and `ability.skill-book.item-lore` localization keys. Translators can add locale-specific versions.

**Limitation:** The resolved text is still **baked into the ItemStack** at creation time. Once the item exists, its display name and lore are frozen — they will not update if the player changes their locale, the server owner edits localization YAML, or the book is traded to a player with a different locale. This is because physical `ItemStack`s in player inventories have no re-render hook (unlike GUI items which re-resolve via `Slot.getItem(player)` on every display).

**Accepted tradeoffs:**
- Skill books have a short lifecycle (created → consumed), so stale text is unlikely in practice
- This matches `ItemRewardType`'s existing behavior — granted items also bake text at creation time
- The player-aware factory overload ensures the creating player sees text in their locale

**Future improvement:** A project-wide migration to Adventure's `GlobalTranslator` + `Component.translatable()` for physical items would enable true per-player lazy resolution at packet send time. This is tracked in [diamonddagger590/mcrpg#213](https://github.com/DiamondDagger590/McRPG/issues/213).

---

## 4. SkillBookFactory

**File:** `src/main/java/us/eunoians/mcrpg/item/skillbook/SkillBookFactory.java`

A static factory that creates skill book `ItemStack`s with the correct PDC tags, display name, and lore. All skill book sources delegate to this class.

### 4.1 PDC Tag Schema

| PDC Key | Type | Value | Purpose |
|---|---|---|---|
| `mcrpg:skill_book` | `PersistentDataType.BOOLEAN` | `true` | Marker tag — identifies the item as a skill book |
| `mcrpg:skill_book_ability` | `PersistentDataType.STRING` | Ability `NamespacedKey` string (e.g. `"mcrpg:phase_shift"`) | Identifies which ability this book unlocks |

### 4.2 Class Design

```java
package us.eunoians.mcrpg.item.skillbook;

import com.diamonddagger590.mccore.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.List;
import java.util.Map;

/**
 * Static factory for creating skill book {@link ItemStack}s.
 * <p>
 * Skill books are enchanted books tagged with PDC keys that identify them as
 * McRPG skill books and specify which ability they unlock. All skill book
 * sources (MythicMobs drops, quest rewards, commands) should delegate to
 * this factory to ensure consistent item format.
 * <p>
 * Display text is resolved through the localization system. When a player
 * context is available, the item is localized to that player's locale.
 * When no player is available (e.g., MythicMobs drops), the server default
 * locale is used.
 */
public final class SkillBookFactory {

    /**
     * PDC key applied to skill book items to identify them as McRPG skill books.
     */
    public static final NamespacedKey SKILL_BOOK_KEY =
            new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "skill_book");

    /**
     * PDC key storing the ability {@link NamespacedKey} this book unlocks.
     */
    public static final NamespacedKey SKILL_BOOK_ABILITY_KEY =
            new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "skill_book_ability");

    private SkillBookFactory() {
        // Non-instantiable
    }

    /**
     * Creates a skill book item with explicit display name and lore components.
     * <p>
     * This is the low-level overload. Prefer {@link #createSkillBook(NamespacedKey, String, int)}
     * or {@link #createSkillBook(NamespacedKey, McRPGPlayer, int)} which resolve
     * display text through the localization system.
     *
     * @param abilityKey   the {@link NamespacedKey} of the ability this book unlocks
     * @param displayName  the display name for the item
     * @param lore         the lore lines for the item
     * @param amount       the stack size (typically 1)
     * @return a fully tagged skill book {@link ItemStack}
     */
    @NotNull
    public static ItemStack createSkillBook(@NotNull NamespacedKey abilityKey,
                                            @NotNull Component displayName,
                                            @NotNull List<Component> lore,
                                            int amount) {
        ItemStack itemStack = new ItemStack(Material.ENCHANTED_BOOK, Math.max(1, amount));
        ItemMeta meta = itemStack.getItemMeta();

        meta.displayName(displayName);
        meta.lore(lore);

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(SKILL_BOOK_KEY, PersistentDataType.BOOLEAN, true);
        pdc.set(SKILL_BOOK_ABILITY_KEY, PersistentDataType.STRING, abilityKey.toString());

        itemStack.setItemMeta(meta);
        return itemStack;
    }

    /**
     * Creates a skill book with display text resolved from the localization system
     * using the given player's locale chain.
     * <p>
     * The display name and lore are resolved via {@link LocalizationKey#SKILL_BOOK_ITEM_NAME}
     * and {@link LocalizationKey#SKILL_BOOK_ITEM_LORE} with the {@code <ability>} placeholder
     * substituted with the ability's localized display name.
     *
     * @param abilityKey the {@link NamespacedKey} of the ability this book unlocks
     * @param player     the player whose locale chain is used for text resolution
     * @param amount     the stack size (typically 1)
     * @return a fully tagged skill book {@link ItemStack}
     */
    @NotNull
    public static ItemStack createSkillBook(@NotNull NamespacedKey abilityKey,
                                            @NotNull McRPGPlayer player,
                                            int amount) {
        McRPG plugin = McRPG.getInstance();
        McRPGLocalizationManager localizationManager = plugin.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION);

        String abilityDisplayName = resolveAbilityDisplayName(abilityKey, player);
        Map<String, String> placeholders = Map.of("ability", abilityDisplayName);

        Component displayName = localizationManager.getLocalizedMessageAsComponent(
                player, LocalizationKey.SKILL_BOOK_ITEM_NAME, placeholders);

        List<Component> lore = localizationManager.getLocalizedMessagesAsComponents(
                player, LocalizationKey.SKILL_BOOK_ITEM_LORE, placeholders);

        return createSkillBook(abilityKey, displayName, lore, amount);
    }

    /**
     * Creates a skill book with display text resolved from the localization system
     * using the server default locale.
     * <p>
     * Used when no player context is available (e.g., MythicMobs drops, which are
     * created before being assigned to a specific player).
     *
     * @param abilityKey       the {@link NamespacedKey} of the ability this book unlocks
     * @param abilityDisplayName a human-readable ability name for the {@code <ability>} placeholder
     * @param amount           the stack size (typically 1)
     * @return a fully tagged skill book {@link ItemStack}
     */
    @NotNull
    public static ItemStack createSkillBook(@NotNull NamespacedKey abilityKey,
                                            @NotNull String abilityDisplayName,
                                            int amount) {
        McRPG plugin = McRPG.getInstance();
        McRPGLocalizationManager localizationManager = plugin.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION);

        Map<String, String> placeholders = Map.of("ability", abilityDisplayName);

        Component displayName = localizationManager.getLocalizedMessageAsComponent(
                LocalizationKey.SKILL_BOOK_ITEM_NAME, placeholders);

        List<Component> lore = localizationManager.getLocalizedMessagesAsComponents(
                LocalizationKey.SKILL_BOOK_ITEM_LORE, placeholders);

        return createSkillBook(abilityKey, displayName, lore, amount);
    }

    /**
     * Checks whether the given item is a skill book by inspecting its PDC.
     *
     * @param itemStack the item to check
     * @return {@code true} if the item has the {@code mcrpg:skill_book} tag set to true
     */
    public static boolean isSkillBook(@NotNull ItemStack itemStack) {
        if (!itemStack.hasItemMeta()) {
            return false;
        }
        PersistentDataContainer pdc = itemStack.getItemMeta().getPersistentDataContainer();
        return Boolean.TRUE.equals(pdc.get(SKILL_BOOK_KEY, PersistentDataType.BOOLEAN));
    }

    /**
     * Reads the ability key from a skill book item.
     *
     * @param itemStack the skill book item
     * @return the ability {@link NamespacedKey} string, or {@code null} if not present
     */
    @Nullable
    public static String getAbilityKeyString(@NotNull ItemStack itemStack) {
        if (!itemStack.hasItemMeta()) {
            return null;
        }
        return itemStack.getItemMeta().getPersistentDataContainer()
                .get(SKILL_BOOK_ABILITY_KEY, PersistentDataType.STRING);
    }

    /**
     * Resolves the localized display name for an ability, falling back to a
     * formatted version of the key if the ability is not registered.
     */
    @NotNull
    private static String resolveAbilityDisplayName(@NotNull NamespacedKey abilityKey,
                                                    @NotNull McRPGPlayer player) {
        McRPG plugin = McRPG.getInstance();
        return plugin.registryAccess()
                .registry(us.eunoians.mcrpg.registry.McRPGRegistryKey.ABILITY)
                .getRegisteredAbility(abilityKey)
                .map(ability -> plugin.getMiniMessage().serialize(ability.getDisplayName(player)))
                .orElse(formatKeyAsDisplayName(abilityKey));
    }

    /**
     * Converts a NamespacedKey to a human-readable display name by replacing
     * underscores with spaces and capitalizing the first letter.
     * <p>
     * Public so that callers (e.g., {@link us.eunoians.mcrpg.quest.reward.builtin.SkillBookRewardType})
     * can use the same formatting logic when a player context is unavailable.
     */
    @NotNull
    public static String formatKeyAsDisplayName(@NotNull NamespacedKey key) {
        String raw = key.getKey().replace("_", " ");
        return raw.substring(0, 1).toUpperCase() + raw.substring(1);
    }
}
```

### 4.3 Design Notes

- **Three `createSkillBook` overloads:** (1) Low-level with explicit Components — for callers that build their own display. (2) Player-aware — resolves display name/lore via the player's locale chain and the ability's localized display name from the registry. (3) Server-default — resolves via server default locale with a provided ability display name string, used by `McRPGSkillBookDrop` and other contexts without a player reference.
- **All display text comes from localization YAML.** The `SKILL_BOOK_ITEM_NAME` and `SKILL_BOOK_ITEM_LORE` keys are resolved through `McRPGLocalizationManager`, so server owners can customize skill book appearance and translators can localize it. No hardcoded strings.
- **`resolveAbilityDisplayName` looks up the ability registry** to get the localized display name. Falls back to a formatted key string if the ability is not registered (defensive — should not happen in practice).
- **`isSkillBook` and `getAbilityKeyString` helpers:** Used by `SkillBookConsumeListener` to read items without coupling to raw PDC key constants.
- **Non-instantiable:** Pure static utility — no state beyond what's resolved at call time.

---

## 5. McRPGSkillBookDrop Refactor

**Modified file:** `src/main/java/us/eunoians/mcrpg/external/mythicmobs/McRPGSkillBookDrop.java`

The existing `McRPGSkillBookDrop` is refactored to delegate item creation to `SkillBookFactory`. The class retains its role as a MythicMobs `IItemDrop` implementation but no longer builds the item inline.

### 5.1 Changes

1. **Remove** the `SKILL_BOOK_KEY` and `SKILL_BOOK_SKILL_KEY` constants (moved to `SkillBookFactory` as `SKILL_BOOK_KEY` and `SKILL_BOOK_ABILITY_KEY`).
2. **Add** an `abilityKey` field (`NamespacedKey`) parsed from the MythicMobs config. The config `skill` parameter now expects an ability key string (e.g. `"mcrpg:phase_shift"`) instead of a bare skill name.
3. **Delegate** `getDrop()` to `SkillBookFactory.createSkillBook()`.

### 5.2 Refactored Class

```java
package us.eunoians.mcrpg.external.mythicmobs;

import io.lumine.mythic.api.adapters.AbstractItemStack;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.drops.DropMetadata;
import io.lumine.mythic.api.drops.IItemDrop;
import io.lumine.mythic.bukkit.adapters.BukkitItemStack;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.item.skillbook.SkillBookFactory;

/**
 * A custom MythicMobs drop type that generates an McRPG skill book item.
 * <p>
 * Registered as {@code mcrpg_skillbook} in MythicMobs drop tables. The ability key
 * is passed as the argument in the drop table configuration:
 * <pre>
 *   Drops:
 *     - mcrpg_skillbook{skill=mcrpg:phase_shift} 1 0.1
 * </pre>
 * <p>
 * Delegates to {@link SkillBookFactory} for item creation, ensuring all skill books
 * have consistent PDC tags and formatting regardless of source.
 */
public class McRPGSkillBookDrop implements IItemDrop {

    private final NamespacedKey abilityKey;
    private final String abilityDisplayName;

    /**
     * Creates a new skill book drop.
     *
     * @param config   the MythicMobs line config for this drop entry
     * @param argument the argument string from the drop table (used as fallback ability key)
     */
    public McRPGSkillBookDrop(@NotNull MythicLineConfig config, @NotNull String argument) {
        String keyString = config.getString("skill", argument);
        this.abilityKey = NamespacedKey.fromString(keyString);
        this.abilityDisplayName = config.getString("display-name", abilityKey.getKey()
                .replace("_", " ")
                .substring(0, 1).toUpperCase() + abilityKey.getKey().replace("_", " ").substring(1));
    }

    @Override
    @NotNull
    public AbstractItemStack getDrop(@NotNull DropMetadata dropMetadata, double amount) {
        ItemStack itemStack = SkillBookFactory.createSkillBook(
                abilityKey, abilityDisplayName, Math.max(1, (int) amount));
        return new BukkitItemStack(itemStack);
    }
}
```

### 5.3 MythicMobs Config Migration

Old format:
```yaml
Drops:
  - mcrpg_skillbook{skill=Phase Shift} 1 0.1
```

New format:
```yaml
Drops:
  - mcrpg_skillbook{skill=mcrpg:phase_shift} 1 0.1
  - mcrpg_skillbook{skill=mcrpg:phase_shift,display-name=Phase Shift} 1 0.1  # optional display name override
```

The `skill` parameter now expects a full `NamespacedKey` string. The optional `display-name` parameter overrides the auto-generated display name derived from the key.

---

## 6. SkillBookConsumeEvent

**File:** `src/main/java/us/eunoians/mcrpg/event/item/SkillBookConsumeEvent.java`

A cancellable Bukkit event fired when a player right-clicks a skill book. Fired *before* the ability is unlocked. If cancelled, the item is not consumed and no unlock occurs.

```java
package us.eunoians.mcrpg.event.item;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Fired when a player attempts to consume a skill book.
 * <p>
 * This event is fired <b>before</b> the ability is unlocked. If cancelled,
 * the item is not consumed and no {@link us.eunoians.mcrpg.event.ability.AbilityUnlockEvent}
 * is fired.
 * <p>
 * Third-party plugins can listen to this event to:
 * <ul>
 *   <li>Gate consumption behind additional conditions (location, level, currency)</li>
 *   <li>Log or track skill book usage</li>
 *   <li>Replace or modify the item before consumption</li>
 * </ul>
 */
public class SkillBookConsumeEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final NamespacedKey abilityKey;
    private final ItemStack itemStack;
    private boolean cancelled;

    /**
     * Creates a new skill book consume event.
     *
     * @param player     the player consuming the skill book
     * @param abilityKey the {@link NamespacedKey} of the ability being unlocked
     * @param itemStack  the skill book item being consumed
     */
    public SkillBookConsumeEvent(@NotNull Player player,
                                 @NotNull NamespacedKey abilityKey,
                                 @NotNull ItemStack itemStack) {
        super(player);
        this.abilityKey = abilityKey;
        this.itemStack = itemStack;
        this.cancelled = false;
    }

    /**
     * Gets the ability key that this skill book would unlock.
     *
     * @return the ability {@link NamespacedKey}
     */
    @NotNull
    public NamespacedKey getAbilityKey() {
        return abilityKey;
    }

    /**
     * Gets the skill book item stack being consumed.
     *
     * @return the item stack (not a copy — modifications affect the original)
     */
    @NotNull
    public ItemStack getItemStack() {
        return itemStack;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
```

### 6.1 Design Notes

- **Extends `PlayerEvent`** rather than a custom base class. Skill book consumption is player-centric, not ability-centric — the ability may not even exist yet (invalid key scenario).
- **Item stack is not copied.** Listeners can inspect but should not modify the item in ways that break PDC tags. The listener removes the item *after* the event completes uncancelled.

---

## 7. SkillBookConsumeListener

**File:** `src/main/java/us/eunoians/mcrpg/listener/item/SkillBookConsumeListener.java`

Listens for `PlayerInteractEvent` with a right-click action. If the player is holding a skill book, validates the ability, checks unlock state, fires `SkillBookConsumeEvent`, unlocks the ability, and removes the item.

```java
package us.eunoians.mcrpg.listener.item;

import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeRegistry;
import us.eunoians.mcrpg.ability.impl.type.UnlockableAbility;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.event.ability.AbilityUnlockEvent;
import us.eunoians.mcrpg.event.item.SkillBookConsumeEvent;
import us.eunoians.mcrpg.item.skillbook.SkillBookFactory;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Map;
import java.util.Optional;

/**
 * Listens for right-click interactions with skill book items and handles
 * the consumption flow: validation, event firing, ability unlock, and
 * item removal.
 * <p>
 * This listener is always registered — skill books can be consumed regardless
 * of whether MythicMobs is present (books may come from quest rewards or commands).
 */
public class SkillBookConsumeListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerInteract(@NotNull PlayerInteractEvent event) {
        // Only handle right-click actions
        if (event.getAction() != Action.RIGHT_CLICK_AIR
                && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        // Prevent double-fire from dual-hand events
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        ItemStack item = event.getItem();
        if (item == null || !SkillBookFactory.isSkillBook(item)) {
            return;
        }

        // Cancel the interact event to prevent placing/using the book
        event.setCancelled(true);

        Player player = event.getPlayer();
        McRPG plugin = McRPG.getInstance();

        // Read the ability key from the item
        String abilityKeyString = SkillBookFactory.getAbilityKeyString(item);
        if (abilityKeyString == null) {
            return;
        }

        NamespacedKey abilityKey = NamespacedKey.fromString(abilityKeyString);
        if (abilityKey == null) {
            return;
        }

        McRPGLocalizationManager localizationManager = plugin.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION);

        // Look up the McRPGPlayer
        Optional<McRPGPlayer> playerOptional = plugin.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.PLAYER)
                .getPlayer(player.getUniqueId());

        if (playerOptional.isEmpty()) {
            return;
        }

        McRPGPlayer mcRPGPlayer = playerOptional.get();

        // Validate the ability exists in the registry
        AbilityRegistry abilityRegistry = plugin.registryAccess()
                .registry(McRPGRegistryKey.ABILITY);

        Optional<Ability> abilityOptional = abilityRegistry.getRegisteredAbility(abilityKey);
        if (abilityOptional.isEmpty()) {
            player.sendMessage(localizationManager.getLocalizedMessageAsComponent(
                    mcRPGPlayer, LocalizationKey.SKILL_BOOK_UNKNOWN_ABILITY,
                    Map.of("ability", abilityKeyString)));
            return;
        }

        Ability ability = abilityOptional.get();

        // The ability must be unlockable
        if (!(ability instanceof UnlockableAbility unlockableAbility)) {
            player.sendMessage(localizationManager.getLocalizedMessageAsComponent(
                    mcRPGPlayer, LocalizationKey.SKILL_BOOK_UNKNOWN_ABILITY,
                    Map.of("ability", abilityKeyString)));
            return;
        }

        // Check if already unlocked
        AbilityHolder abilityHolder = mcRPGPlayer;
        boolean isUnlocked = mcRPGPlayer.getAbilityData(abilityKey)
                .map(data -> data.getAbilityAttribute(AbilityAttributeRegistry.ABILITY_UNLOCKED_ATTRIBUTE))
                .map(attr -> attr.getContent())
                .orElse(false);

        if (isUnlocked) {
            player.sendMessage(localizationManager.getLocalizedMessageAsComponent(
                    mcRPGPlayer, LocalizationKey.SKILL_BOOK_ALREADY_UNLOCKED,
                    Map.of("ability", plugin.getMiniMessage().serialize(
                            unlockableAbility.getDisplayName(mcRPGPlayer)))));
            return;
        }

        // Fire SkillBookConsumeEvent (cancellable)
        SkillBookConsumeEvent consumeEvent = new SkillBookConsumeEvent(
                player, abilityKey, item);
        Bukkit.getPluginManager().callEvent(consumeEvent);

        if (consumeEvent.isCancelled()) {
            return;
        }

        // Unlock the ability — set attribute and fire AbilityUnlockEvent
        mcRPGPlayer.getAbilityData(abilityKey).ifPresent(data -> {
            data.getAbilityAttribute(AbilityAttributeRegistry.ABILITY_UNLOCKED_ATTRIBUTE)
                    .setContent(true);
        });

        AbilityUnlockEvent unlockEvent = new AbilityUnlockEvent(abilityHolder, unlockableAbility);
        Bukkit.getPluginManager().callEvent(unlockEvent);

        // Remove one skill book from the player's hand
        item.setAmount(item.getAmount() - 1);

        // Send consumption message
        player.sendMessage(localizationManager.getLocalizedMessageAsComponent(
                mcRPGPlayer, LocalizationKey.SKILL_BOOK_CONSUMED,
                Map.of("ability", plugin.getMiniMessage().serialize(
                        unlockableAbility.getDisplayName(mcRPGPlayer)))));
    }
}
```

### 7.1 Flow Summary

```
PlayerInteractEvent (RIGHT_CLICK)
  │
  ├─ Not a skill book? → return
  │
  ├─ Cancel interact event (prevent book opening)
  │
  ├─ Read mcrpg:skill_book_ability PDC tag → NamespacedKey
  │
  ├─ Ability not in AbilityRegistry? → send unknown-ability message, return
  │
  ├─ Not an UnlockableAbility? → send unknown-ability message, return
  │
  ├─ AbilityUnlockedAttribute already true? → send already-unlocked message, return
  │
  ├─ Fire SkillBookConsumeEvent → if cancelled, return
  │
  ├─ Set AbilityUnlockedAttribute = true
  │
  ├─ Fire AbilityUnlockEvent
  │   └─ OnAbilityUnlockListener handles: unlock message + auto-add to loadout
  │
  ├─ Remove item (decrement stack by 1)
  │
  └─ Send skill-book-consumed message
```

### 7.2 Design Notes

- **`EventPriority.NORMAL`** rather than `MONITOR` because this listener cancels the `PlayerInteractEvent` to prevent the enchanted book's default behavior (opening book UI).
- **Dual-hand guard:** `event.getHand() != EquipmentSlot.HAND` prevents the listener from firing twice (once per hand) on a single right-click.
- **Always registered:** Unlike fishing mob listeners, this listener does not require MythicMobs. Skill books can originate from quest rewards, admin commands, or other sources.
- **AbilityUnlockEvent reuse:** The listener does not duplicate messaging or loadout logic. It sets the unlock attribute and fires `AbilityUnlockEvent`, which `OnAbilityUnlockListener` already handles.

---

## 8. SkillBookRewardType

**File:** `src/main/java/us/eunoians/mcrpg/quest/reward/builtin/SkillBookRewardType.java`

A `QuestRewardType` implementation that grants a physical skill book item to the player's inventory. Follows the same pattern as `ItemRewardType` — the reward is a tangible item, not an instant unlock.

### 8.1 Config Format

```yaml
rewards:
  - type: mcrpg:skill_book
    ability: "mcrpg:phase_shift"
```

The `ability` key is the full `NamespacedKey` string of the ability the skill book unlocks.

### 8.2 Class Design

```java
package us.eunoians.mcrpg.quest.reward.builtin;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.item.skillbook.SkillBookFactory;
import us.eunoians.mcrpg.quest.reward.QuestRewardType;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.HashMap;
import java.util.Map;
import java.util.OptionalLong;

/**
 * A quest reward type that grants a physical skill book item to the player.
 * <p>
 * The skill book is created via {@link SkillBookFactory} to ensure consistent
 * PDC tags and formatting. When granted, the item is added to the player's
 * inventory; overflow drops naturally at the player's location.
 * <p>
 * Config format:
 * <pre>
 * rewards:
 *   - type: mcrpg:skill_book
 *     ability: "mcrpg:phase_shift"
 * </pre>
 */
public class SkillBookRewardType implements QuestRewardType {

    /**
     * Registry key for this reward type.
     */
    public static final NamespacedKey KEY =
            new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "skill_book");

    private final NamespacedKey abilityKey;

    /**
     * Base (unconfigured) constructor for registry registration.
     */
    public SkillBookRewardType() {
        this.abilityKey = null;
    }

    /**
     * Configured constructor with a specific ability key.
     *
     * @param abilityKey the ability this skill book unlocks
     */
    private SkillBookRewardType(@NotNull NamespacedKey abilityKey) {
        this.abilityKey = abilityKey;
    }

    @Override
    @NotNull
    public NamespacedKey getKey() {
        return KEY;
    }

    @Override
    @NotNull
    public QuestRewardType parseConfig(@NotNull Section section) {
        String abilityKeyString = section.getString("ability");
        NamespacedKey parsedKey = NamespacedKey.fromString(abilityKeyString);
        if (parsedKey == null) {
            throw new IllegalArgumentException(
                    "Invalid ability key in skill_book reward: " + abilityKeyString);
        }
        return new SkillBookRewardType(parsedKey);
    }

    @Override
    public void grant(@NotNull Player player) {
        if (abilityKey == null) {
            throw new IllegalStateException("Cannot grant unconfigured SkillBookRewardType");
        }

        // Use the player-aware factory overload to localize the item
        // to the receiving player's locale
        McRPG plugin = McRPG.getInstance();
        Optional<McRPGPlayer> mcRPGPlayerOptional = plugin.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.PLAYER)
                .getPlayer(player.getUniqueId());

        ItemStack skillBook;
        if (mcRPGPlayerOptional.isPresent()) {
            skillBook = SkillBookFactory.createSkillBook(abilityKey, mcRPGPlayerOptional.get(), 1);
        } else {
            // Fallback: player not loaded yet (e.g., PendingReward on login).
            // Use server default locale with formatted key name.
            String displayName = SkillBookFactory.formatKeyAsDisplayName(abilityKey);
            skillBook = SkillBookFactory.createSkillBook(abilityKey, displayName, 1);
        }

        // Add to inventory, drop overflow naturally
        Map<Integer, ItemStack> overflow = player.getInventory().addItem(skillBook);
        overflow.values().forEach(item ->
                player.getWorld().dropItemNaturally(player.getLocation(), item));
    }

    @Override
    @NotNull
    public Map<String, Object> serializeConfig() {
        Map<String, Object> map = new HashMap<>();
        if (abilityKey != null) {
            map.put("ability", abilityKey.toString());
        }
        return map;
    }

    @Override
    @NotNull
    public QuestRewardType fromSerializedConfig(@NotNull Map<String, Object> config) {
        String abilityKeyString = (String) config.get("ability");
        NamespacedKey parsedKey = NamespacedKey.fromString(abilityKeyString);
        if (parsedKey == null) {
            throw new IllegalArgumentException(
                    "Invalid ability key in serialized skill_book reward: " + abilityKeyString);
        }
        return new SkillBookRewardType(parsedKey);
    }

    @Override
    @NotNull
    public OptionalLong getNumericAmount() {
        return OptionalLong.of(1);
    }

    @Override
    @NotNull
    public String describeForDisplay() {
        if (abilityKey == null) {
            return "Skill Book";
        }
        // Use server default locale to resolve the item name for display
        McRPG plugin = McRPG.getInstance();
        McRPGLocalizationManager localizationManager = plugin.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION);
        String abilityName = SkillBookFactory.formatKeyAsDisplayName(abilityKey);
        return localizationManager.getLocalizedMessage(
                LocalizationKey.SKILL_BOOK_ITEM_NAME, Map.of("ability", abilityName));
    }

    @Override
    @NotNull
    public String describeForDisplay(@NotNull McRPGPlayer player) {
        if (abilityKey == null) {
            return "Skill Book";
        }
        McRPG plugin = McRPG.getInstance();
        McRPGLocalizationManager localizationManager = plugin.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION);
        // Resolve the ability's localized display name for this player
        String abilityName = plugin.registryAccess()
                .registry(McRPGRegistryKey.ABILITY)
                .getRegisteredAbility(abilityKey)
                .map(ability -> plugin.getMiniMessage().serialize(ability.getDisplayName(player)))
                .orElse(SkillBookFactory.formatKeyAsDisplayName(abilityKey));
        return localizationManager.getLocalizedMessage(
                player, LocalizationKey.SKILL_BOOK_ITEM_NAME, Map.of("ability", abilityName));
    }
}
```

### 8.3 Design Notes

- **Same-item-as-drops principle:** `grant()` creates the item via `SkillBookFactory`, identical to what `McRPGSkillBookDrop` produces. Players see the same item from mob drops and quest rewards.
- **Player-aware grant:** `grant()` looks up the `McRPGPlayer` to use the player-aware factory overload, localizing the item to the receiving player's locale. Falls back to server default locale if the player isn't loaded yet (e.g., `PendingReward` on login).
- **Overflow handling:** Follows `ItemRewardType`'s pattern — items that don't fit in the inventory are dropped naturally at the player's location.
- **No amount scaling:** `withAmountMultiplier()` returns `this` unchanged (default behavior). Skill books are not scalable rewards — you get exactly one book per reward entry.
- **Localized `describeForDisplay`:** Both the no-arg (server default) and player-aware overloads resolve the display label through the localization system, reusing the `SKILL_BOOK_ITEM_NAME` key.
- **Serialization:** Uses a simple `Map<String, Object>` with the `ability` key string, matching the YAML config format. This supports `PendingReward` persistence for offline players.

---

## 9. Localization Keys

### 9.1 LocalizationKey.java Additions

**Modified file:** `src/main/java/us/eunoians/mcrpg/configuration/file/localization/LocalizationKey.java`

Add the following constants under the existing `ABILITY_HEADER` section, after the `ABILITY_UNLOCK_HEADER` block:

```java
// Skill Book messages and item display
private static final String SKILL_BOOK_HEADER = toRoutePath(ABILITY_HEADER, "skill-book");
public static final Route SKILL_BOOK_ITEM_NAME = Route.fromString(toRoutePath(SKILL_BOOK_HEADER, "item-name"));
public static final Route SKILL_BOOK_ITEM_LORE = Route.fromString(toRoutePath(SKILL_BOOK_HEADER, "item-lore"));
public static final Route SKILL_BOOK_CONSUMED = Route.fromString(toRoutePath(SKILL_BOOK_HEADER, "consumed"));
public static final Route SKILL_BOOK_ALREADY_UNLOCKED = Route.fromString(toRoutePath(SKILL_BOOK_HEADER, "already-unlocked"));
public static final Route SKILL_BOOK_UNKNOWN_ABILITY = Route.fromString(toRoutePath(SKILL_BOOK_HEADER, "unknown-ability"));
```

These produce the following YAML paths:
- `ability.skill-book.item-name`
- `ability.skill-book.item-lore`
- `ability.skill-book.consumed`
- `ability.skill-book.already-unlocked`
- `ability.skill-book.unknown-ability`

### 9.2 en_abilities.yml Additions

**Modified file:** `src/main/resources/localization/english/en_abilities.yml`

Add the following section after the existing `unlock:` block (under the `ability:` root):

```yaml
  # Configure skill book item display and messages
  # Supports <ability> as a placeholder for the ability name
  skill-book:
    # Display name for the skill book item (shown in inventory)
    item-name: "<gold>Skill Book: <ability>"
    # Lore lines for the skill book item (shown below the name in inventory)
    item-lore:
      - "<gray>A mysterious tome of knowledge."
      - "<gray>Right-click to unlock <ability><gray>."
    # The message to send when a player successfully consumes a skill book
    consumed: "<green>You consumed a skill book and learned <ability><green>!"
    # The message to send when a player tries to consume a skill book for an ability they already have
    already-unlocked: "<red>You have already unlocked <ability><red>."
    # The message to send when a skill book references an ability that doesn't exist
    unknown-ability: "<red>This skill book references an unknown ability: <ability><red>."
```

### 9.3 Placeholder Reference

| Placeholder | Context | Value |
|---|---|---|
| `<ability>` | `item-name`, `item-lore`, `consumed`, `already-unlocked` | MiniMessage-serialized display name of the ability (resolved from `AbilityRegistry` when available) |
| `<ability>` | `unknown-ability` | Raw ability key string (ability is not registered, so no display name is available) |

### 9.4 Customization Examples

Server owners can fully customize skill book appearance in their locale files:

```yaml
  # Example: Minimalist style
  skill-book:
    item-name: "<light_purple>✦ <ability>"
    item-lore:
      - ""
      - "<gray>Right-click to learn this ability."
      - ""

  # Example: Verbose style with instructions
  skill-book:
    item-name: "<gold><bold>Tome of <ability>"
    item-lore:
      - "<gray>This ancient tome contains the secrets of <ability><gray>."
      - ""
      - "<yellow>Right-click while holding to consume."
      - "<dark_gray>The tome will be destroyed upon use."
```

---

## 10. Bootstrap Registration

### 10.1 SkillBookConsumeListener Registration

**Modified file:** `src/main/java/us/eunoians/mcrpg/bootstrap/McRPGListenerRegistrar.java`

The `SkillBookConsumeListener` is registered **unconditionally** — it does not require MythicMobs. Add it alongside the existing item/entity listeners:

```java
// Skill book listener (always registered — books can come from any source)
Bukkit.getPluginManager().registerEvents(new SkillBookConsumeListener(), plugin);
```

**Placement:** After the existing `PlayerPickupItemListener` registration, before the statistic listeners block. This groups it with other item-related listeners.

### 10.2 SkillBookRewardType Registration

**Modified file:** `src/main/java/us/eunoians/mcrpg/quest/reward/builtin/BuiltinRewardTypes.java`

Add the skill book reward type instance and register it:

```java
public static final SkillBookRewardType SKILL_BOOK = new SkillBookRewardType();
```

In `registerAll()`:

```java
public static void registerAll(@NotNull QuestRewardTypeRegistry registry) {
    registry.register(EXPERIENCE);
    registry.register(COMMAND);
    registry.register(ABILITY_UPGRADE);
    registry.register(ABILITY_UPGRADE_NEXT_TIER);
    registry.register(SKILL_BOOK);  // <-- new
}
```

---

## 11. Edge Cases & Graceful Degradation

| Scenario | Behavior |
|---|---|
| Ability key in PDC doesn't exist in `AbilityRegistry` | Send `unknown-ability` message. Item is not consumed. |
| Ability exists but is not `UnlockableAbility` | Treated as unknown ability. Item is not consumed. |
| Player already unlocked the ability | Send `already-unlocked` message. Item is not consumed. |
| `SkillBookConsumeEvent` is cancelled by another plugin | Item is not consumed. No unlock occurs. No message sent (cancelling plugin is responsible for feedback). |
| Skill book with missing `mcrpg:skill_book_ability` tag | Silent no-op. The `isSkillBook()` check passes but `getAbilityKeyString()` returns null. |
| Skill book with malformed key string | `NamespacedKey.fromString()` returns null. Silent no-op. |
| Player has full inventory when receiving quest reward | Overflow items drop naturally at player's location (same as `ItemRewardType`). |
| Player is offline when quest reward is granted | `PendingReward` serialization stores the ability key. On next login, `fromSerializedConfig()` reconstructs the reward and grants the item. |
| MythicMobs not installed | `SkillBookConsumeListener` still works. Only `McRPGSkillBookDrop` is unavailable (it's only registered when MM hook is present). Quest rewards and commands still produce skill books. |
| Player right-clicks skill book on a block (e.g. crafting table) | `PlayerInteractEvent` is cancelled, preventing both the block interaction and the default enchanted book behavior. Consumption proceeds normally. |
| Player right-clicks with off-hand | `EquipmentSlot.HAND` guard prevents double-fire. Off-hand clicks are ignored. |
| Third-party content expansion registers a skill-book-unlockable ability | Works out of the box — `AbilityRegistry` lookup uses `NamespacedKey`, which supports any namespace. |

---

## 12. Test Plan

### 12.1 Unit Tests (src/test/java)

| Test Class | Tests |
|---|---|
| `SkillBookFactoryTest` | `createSkillBook` with abilityKey + displayName: correct material (ENCHANTED_BOOK), correct PDC tags (SKILL_BOOK_KEY = true, SKILL_BOOK_ABILITY_KEY = key string), correct display name and lore. `createSkillBook` with Component overload: custom display name and lore applied. `isSkillBook`: true for tagged items, false for untagged items, false for null meta. `getAbilityKeyString`: returns correct key string, returns null for untagged items. Amount parameter: stack size matches input, minimum 1. |
| `SkillBookConsumeEventTest` | Event creation with player, ability key, item stack. Cancellation: `setCancelled(true)` → `isCancelled()` returns true. Getters return constructor values. Handler list. |
| `SkillBookRewardTypeTest` | `parseConfig`: valid ability key string → configured instance. `parseConfig`: invalid key string → `IllegalArgumentException`. `serializeConfig` / `fromSerializedConfig` round-trip. `getKey` returns `mcrpg:skill_book`. `getNumericAmount` returns 1. `describeForDisplay`: unconfigured → "Skill Book", configured → "Skill Book: Phase Shift". `grant` on unconfigured instance → `IllegalStateException`. |

### 12.2 Tests Requiring MockBukkit (extend McRPGBaseTest)

| Test Class | Tests |
|---|---|
| `SkillBookConsumeListenerTest` | Right-click with skill book: ability unlocked, item removed, messages sent. Right-click with non-skill-book item: no action. Right-click with already-unlocked ability: already-unlocked message, item not consumed. Right-click with unknown ability key: unknown-ability message, item not consumed. Off-hand interaction: ignored (no double-fire). `SkillBookConsumeEvent` cancelled by test listener: item not consumed, no unlock. Left-click with skill book: no action. |

### 12.3 Manual Testing (Paper Server)

| Scenario | Verification |
|---|---|
| Kill MythicMob with skill book drop configured | Skill book drops with correct PDC tags and display |
| Right-click skill book | Ability unlocked, item consumed, unlock message + loadout message sent |
| Right-click skill book for already-unlocked ability | Already-unlocked message, item not consumed |
| Right-click skill book with invalid ability key | Unknown-ability message, item not consumed |
| Quest reward grants skill book | Item appears in inventory with correct tags |
| Quest reward with full inventory | Item drops at player's feet |
| Consume skill book when loadout has space | Ability auto-added to loadout (via existing `OnAbilityUnlockListener`) |
| Consume skill book when loadout is full | Ability unlocked but not added to loadout |
| MythicMobs not installed | Consumption listener still works. Quest reward books still work. |
| `/mcrpg admin reload` | Localization changes take effect for skill book messages |

---

## 13. File Manifest

### New Files

| File | Type | Description |
|---|---|---|
| `item/skillbook/SkillBookFactory.java` | Factory | Static factory for creating skill book ItemStacks with PDC tags |
| `event/item/SkillBookConsumeEvent.java` | Event | Cancellable event fired before skill book consumption |
| `listener/item/SkillBookConsumeListener.java` | Listener | RIGHT_CLICK handler for skill book consumption flow |
| `quest/reward/builtin/SkillBookRewardType.java` | Reward | Quest reward type that grants a physical skill book item |

All Java files under `src/main/java/us/eunoians/mcrpg/`.

### Modified Files

| File | Change |
|---|---|
| `external/mythicmobs/McRPGSkillBookDrop.java` | Remove inline item creation. Delegate to `SkillBookFactory`. Change `skill` param to accept ability `NamespacedKey` string. |
| `configuration/file/localization/LocalizationKey.java` | Add `SKILL_BOOK_CONSUMED`, `SKILL_BOOK_ALREADY_UNLOCKED`, `SKILL_BOOK_UNKNOWN_ABILITY` route constants |
| `src/main/resources/localization/english/en_abilities.yml` | Add `ability.skill-book.*` localization entries |
| `bootstrap/McRPGListenerRegistrar.java` | Register `SkillBookConsumeListener` unconditionally |
| `quest/reward/builtin/BuiltinRewardTypes.java` | Add `SKILL_BOOK` instance and register in `registerAll()` |

### Not Modified (Used As-Is)

| File | Role |
|---|---|
| `ability/AbilityRegistry.java` | Validates ability keys during consumption |
| `ability/attribute/AbilityUnlockedAttribute.java` | Checked/set during consumption |
| `event/ability/AbilityUnlockEvent.java` | Fired after successful consumption |
| `listener/ability/OnAbilityUnlockListener.java` | Handles unlock messaging and loadout auto-add |
| `quest/reward/QuestRewardType.java` | Interface implemented by `SkillBookRewardType` |
| `quest/reward/QuestRewardTypeRegistry.java` | Stores registered reward types |

---

## 14. Future LLD Notes

- **LLD-4 (MythicMobs Example Configuration):** Will define the bundled `RiptideGuardian.yml` in `src/main/resources/mythicmobs/`, including drop table entries that use `mcrpg_skillbook{skill=mcrpg:phase_shift}` with the new key format.
- **LLD-5 (UnlockCondition Refactor):** Renamed from HLD's original LLD-4. Will introduce `SkillBookUnlockCondition` that always returns `isMet() = false` — unlock happens via book consumption, not condition evaluation. The GUI will display "Obtain from [source]" for these abilities.
- **LLD-6 (Player Abilities):** Renamed from HLD's original LLD-5. Deferred until the ability system rework is complete. Skill books for these abilities (Phase Shift, Whirlpool, etc.) will use the factory and consumption system defined in this LLD.
