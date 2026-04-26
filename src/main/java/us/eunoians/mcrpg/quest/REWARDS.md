# Quest Rewards

How to configure built-in reward types in YAML and implement custom reward types as a third-party developer.

**Key source files:**
- [`QuestRewardType.java`](reward/QuestRewardType.java)
- [`quest/reward/builtin/`](reward/builtin/) — all built-in implementations
- [`ItemRewardType.java`](reward/builtin/ItemRewardType.java)
- [`RewardFallback.java`](board/template/condition/RewardFallback.java)
- [`QuestRewardEntry.java`](board/template/condition/QuestRewardEntry.java)
- [`DistributionRewardEntry.java`](board/distribution/DistributionRewardEntry.java)

---

## 1. Overview

Every reward type has two forms: a **base** instance (stored in the registry, holds no config data) and a **configured** instance (produced at parse time, holds the values that define what the reward actually does).

The lifecycle is:

1. A base `QuestRewardType` instance is registered in `QuestRewardTypeRegistry` at startup via a `ContentExpansion`.
2. When a quest YAML is loaded, `parseConfig(Section)` is called on the base instance, which returns a new **configured** instance holding the parsed values (amount, skill name, commands, etc.).
3. At quest, phase, stage, or objective completion, `grant(Player)` is called on the configured instance to deliver the reward.
4. If a player is offline at grant time, `serializeConfig()` writes the configured state to a `Map<String, Object>` that is persisted in the pending reward queue. When the player next logs in, `fromSerializedConfig(Map)` rebuilds the configured instance from that map and calls `grant`.

The configured instance must be **immutable** — it is stored directly in the quest definition and may be referenced by multiple active quest instances simultaneously.

---

## 2. The `QuestRewardType` Interface

All reward types implement [`QuestRewardType`](reward/QuestRewardType.java), which extends `McRPGContent`.

```java
public interface QuestRewardType extends McRPGContent {

    // Unique identifier for this reward type (e.g. new NamespacedKey("myplugin", "money")).
    // Shared between the base instance and all configured instances.
    @NotNull NamespacedKey getKey();

    // Reads reward-specific config from YAML and returns a NEW configured instance.
    // The receiving instance (this) is the base; never mutate it.
    @NotNull QuestRewardType parseConfig(@NotNull Section section);

    // Applies this configured reward to the player.
    void grant(@NotNull Player player);

    // Serializes this configured instance's state to a map for the pending reward queue.
    // Keys must match what fromSerializedConfig expects.
    @NotNull Map<String, Object> serializeConfig();

    // Reconstructs a configured instance from a previously serialized map.
    // No Section is available here -- read directly from the map.
    @NotNull QuestRewardType fromSerializedConfig(@NotNull Map<String, Object> config);

    // Returns a new instance with numeric amount scaled by the multiplier.
    // Used by SPLIT_EVEN / SPLIT_PROPORTIONAL distribution tiers.
    // Default returns `this` (non-scalable). Override for numeric rewards.
    @NotNull default QuestRewardType withAmountMultiplier(double multiplier) { return this; }

    // Returns the numeric amount for remainder calculations in split-mode tiers.
    // Default returns empty. Override for numeric rewards.
    @NotNull default OptionalLong getNumericAmount() { return OptionalLong.empty(); }

    // Human-readable description fallback (no player context).
    @NotNull default String describeForDisplay() { ... }

    // Localized description resolved through the player's locale chain.
    // Default delegates to describeForDisplay(). Override to attempt locale resolution first.
    @NotNull default String describeForDisplay(@NotNull McRPGPlayer player) { return describeForDisplay(); }
}
```

---

## 3. Built-in Reward Types

McRPG ships six reward types registered by `McRPGExpansion`.

| Class | Key | Config Fields | Scalable |
|-------|-----|--------------|----------|
| `ExperienceRewardType` | `mcrpg:experience` | `skill`, `amount` | Yes |
| `CommandRewardType` | `mcrpg:command` | `commands` (list), `display` | No |
| `ScalableCommandRewardType` | `mcrpg:scalable_command` | `command`, `base-amount`, `display` | Yes (`{amount}` token) |
| `AbilityUpgradeRewardType` | `mcrpg:ability_upgrade` | `ability`, `tier` | No |
| `AbilityUpgradeNextTierRewardType` | `mcrpg:ability_upgrade_next_tier` | `ability` | No |
| `ItemRewardType` | `mcrpg:item` | `item` (map: material, amount, enchantments, name, lore, custom-model-data, glowing), top-level `amount` | Partial (top-level `amount` only) |

**Scalable** means the type correctly implements `withAmountMultiplier` and `getNumericAmount`, making it safe to use in `SPLIT_EVEN` / `SPLIT_PROPORTIONAL` distribution tiers. Non-scalable types used in split tiers fall back to `ALL` behavior (every qualifier receives the full reward) and log a warning.

### YAML Snippets

```yaml
# mcrpg:experience
# Grants McRPG skill XP to the player.
# skill: name or namespaced key of the McRPG skill (e.g. MINING, SWORDS, mcrpg:mining)
xp-reward:
  type: mcrpg:experience
  skill: MINING
  amount: 500

# mcrpg:command
# Executes commands as console. {player} is replaced with the player's name.
# display: optional human-readable label shown in GUIs
command-reward:
  type: mcrpg:command
  display: "5 Diamonds"
  commands:
    - "give {player} diamond 5"
    - "broadcast {player} just completed a quest!"

# mcrpg:scalable_command
# Single command with an {amount} token that is replaced with the scaled value.
# Use this instead of mcrpg:command when the reward lives in a split-mode distribution tier.
scalable-reward:
  type: mcrpg:scalable_command
  command: "eco give {player} {amount}"
  base-amount: 1000
  display: "$1000"

# mcrpg:ability_upgrade
# Upgrades a specific ability to a fixed tier.
# ability: namespaced key of the ability
# tier: the target tier (1-based integer)
upgrade-reward:
  type: mcrpg:ability_upgrade
  ability: mcrpg:bleed
  tier: 2

# mcrpg:ability_upgrade_next_tier
# Upgrades a specific ability to whatever its next tier is.
next-tier-reward:
  type: mcrpg:ability_upgrade_next_tier
  ability: mcrpg:bleed

# mcrpg:item
# Grants an ItemStack built from McCore's ItemBuilder.
# Top-level 'amount' is optional and scalable (used by distribution tiers and template reward-multiplier).
# Nested 'item.amount' is the actual item stack size (not scaled).
item-reward:
  type: mcrpg:item
  amount: 3
  item:
    material: DIAMOND_PICKAXE
    amount: 1
    name: "<gold>Miner's Pickaxe"
    lore:
      - "<gray>A reward for dedicated miners"
    enchantments:
      efficiency: 3
      unbreaking: 2
    glowing: true
```

---

## 4. YAML Configuration

Rewards appear in two contexts in quest YAML. **Inline rewards** are granted directly to the completing player -- they answer "what does the player get for finishing this?" **Distribution rewards** are for multi-player (scoped) quests where a shared pool of rewards is divided among contributors -- they answer "how is the reward split across the group?"

### 4a. Inline Rewards

The `rewards:` block can appear at the **quest, phase, stage, or objective** level. On completion of that level, every qualifying player receives each listed reward in full -- no splitting, no contribution math. This is the most common reward format and the only one relevant for solo (`mcrpg:single_player` scope) quests.

Rewards within the block are named map keys (the label is for human organization only; it has no effect on behavior):

```yaml
rewards:
  mining-xp:
    type: mcrpg:experience
    skill: MINING
    amount: 500
  bonus-command:
    type: mcrpg:command
    display: "5 Diamonds"
    commands:
      - "give {player} diamond 5"
```

At runtime each entry is wrapped in a [`QuestRewardEntry`](board/template/condition/QuestRewardEntry.java), which optionally carries a `RewardFallback` (see section 5).

### 4b. Distribution Rewards

The `reward-distribution:` block (also available at quest, phase, stage, or objective level) is designed for scoped quests where multiple players contribute work toward a shared goal. Rather than every player getting everything, contributions are tracked per player and rewards are allocated based on who did what.

The block is organized in **tiers**. Each tier has two independent configuration axes:

**Axis 1 — Who qualifies?** Set by the tier's `type` (a `RewardDistributionType`):
- `mcrpg:top_players` — the top N contributors by total contribution (use `top-player-count`)
- `mcrpg:contribution_threshold` — any player whose contribution is at least X% of the total (use `min-contribution-percent`)
- `mcrpg:participated` — any player who made at least one contribution
- `mcrpg:membership` — any player who was a member of the scope entity (e.g. land) at completion, regardless of contribution
- `mcrpg:quest_acceptor` — resolves exclusively to the player who accepted the scoped quest (the "quest owner"). Restricted to scoped quests; on non-scoped quests the tier resolves to no players. Use this to give bonus rewards to the player who initiated the group quest.

A player can match multiple tiers — all matched tiers stack.

**Axis 2 — What formula determines each player's share?** Set by `split-mode` on the tier:
- `INDIVIDUAL` (default) — no formula; every qualifier receives each reward at its full, unscaled amount. All per-reward pot settings (`pot-behavior`, `remainder-strategy`, `min-scaled-amount`, `top-count`) are **ignored** in this mode.
- `SPLIT_EVEN` — computes a uniform `1/N` multiplier shared by all qualifiers
- `SPLIT_PROPORTIONAL` — computes a per-player multiplier equal to `playerContribution / totalContribution`; falls back to `SPLIT_EVEN` if no contribution data exists

`split-mode` only defines the formula. Whether any given reward actually uses it is controlled per reward by `pot-behavior`.

**Per-reward fields** (only consulted when `split-mode` is `SPLIT_EVEN` or `SPLIT_PROPORTIONAL`):

**`pot-behavior`** — does this specific reward participate in the tier's split formula?

| Value | Behavior |
|-------|----------|
| `SCALE` (default) | Apply the split multiplier to this reward's amount. Example: 5000 XP with `SPLIT_EVEN` / 4 players = 1250 XP each. |
| `ALL` | Every qualifier receives the full unscaled amount, bypassing the formula entirely. Example: 5000 XP with `ALL` = 5000 XP for everyone. Use for participation badges, achievement flags, or any reward that must not be divided. |
| `TOP_N` | Only the top N contributors receive the full unscaled amount; all other qualifiers receive nothing for this reward. Use `top-count` to set N (defaults to 1). |

The ability to mix `pot-behavior` values within a single tier is the main design point: a `SPLIT_EVEN` tier can simultaneously have one reward that scales (XP pot), one that everyone receives in full (achievement badge), and one that only the top contributor gets (rare item) — all resolved in one pass against the same qualifying player set.

**`remainder-strategy`** — only applies to `SPLIT_EVEN` + `SCALE`. Determines where the integer truncation remainder goes. Example: 10 diamonds / 3 players = 3 each (9 total), 1 leftover.

| Value | Behavior |
|-------|----------|
| `DISCARD` (default) | The remainder is lost. |
| `TOP_CONTRIBUTOR` | The remainder goes to the #1 contributor; ties broken by UUID ordering. |
| `RANDOM` | The remainder goes to a random qualifier. |

Note: `SPLIT_PROPORTIONAL` does not have a remainder pass — each player's amount is computed independently from their contribution fraction, so there is no shared integer to divide.

**`min-scaled-amount`** — a floor applied after scaling. If the scaled result is below this value, the reward is skipped for that player entirely. Set to `0` to disable the minimum. Default is `1`.

**`top-count`** — the number of recipients for `TOP_N` pot behavior (defaults to 1).

```yaml
reward-distribution:
  # SPLIT_EVEN tier: top 3 contributors split a 10000 XP pot evenly.
  # XP uses SCALE (1/3 each = 3333 XP; 1 XP remainder to top contributor).
  # Achievement title uses TOP_N (only #1 gets it).
  # Consolation badge uses ALL (all 3 get it in full regardless of split).
  top-three:
    type: mcrpg:top_players
    top-player-count: 3
    split-mode: SPLIT_EVEN
    rewards:
      xp-pot:
        type: mcrpg:experience
        skill: MINING
        amount: 10000
        pot-behavior: SCALE
        remainder-strategy: TOP_CONTRIBUTOR
        min-scaled-amount: 100
      first-place-title:
        type: mcrpg:command
        commands:
          - "title grant {player} mining_champion"
        display: "Mining Champion Title"
        pot-behavior: TOP_N
        top-count: 1
      consolation-badge:
        type: mcrpg:command
        commands:
          - "badge grant {player} top_miner"
        display: "Top Miner Badge"
        pot-behavior: ALL

  # INDIVIDUAL tier: everyone who participated gets 250 XP in full — no split.
  # Per-reward pot settings are ignored in INDIVIDUAL mode.
  participants:
    type: mcrpg:participated
    split-mode: INDIVIDUAL
    rewards:
      participation-xp:
        type: mcrpg:experience
        skill: MINING
        amount: 250

  # SPLIT_PROPORTIONAL tier: a 5000 XP pot divided by contribution fraction.
  # Player with 60% contribution gets 3000 XP; player with 40% gets 2000 XP.
  # No remainder pass for SPLIT_PROPORTIONAL.
  proportional-pot:
    type: mcrpg:participated
    split-mode: SPLIT_PROPORTIONAL
    rewards:
      proportional-xp:
        type: mcrpg:experience
        skill: MINING
        amount: 5000
        pot-behavior: SCALE
        min-scaled-amount: 50
```

At runtime each reward entry within a tier is wrapped in a [`DistributionRewardEntry`](board/distribution/DistributionRewardEntry.java), which also optionally carries a `RewardFallback` (see section 5).

---

## 5. Reward Fallback

A `RewardFallback` lets you attach a conditional substitute to any reward. When the reward is about to be granted, the fallback's `condition` is evaluated for the recipient. If it returns `true`, the **fallback reward** is granted instead of the primary. If it returns `false`, the primary reward is granted as normal.

The typical use case is avoiding duplicate or meaningless grants — for example, granting a title command as the primary reward, but substituting bonus XP if the player already has the title (detected via a permission check).

```yaml
rewards:
  title-reward:
    type: mcrpg:command
    commands:
      - "title grant {player} hero"
    display: "Hero Title"
    fallback:
      condition:
        # Condition evaluates to true when the player already has the title.
        # When true, fallback reward is granted instead of the primary.
        permission: "mcrpg.title.hero"
      reward:
        type: mcrpg:experience
        skill: MINING
        amount: 1000
```

The `fallback:` block can appear on **any** reward entry — both inline rewards and distribution reward entries support it.

The `condition` block supports all built-in condition shorthand (`permission`, `chance`, `rarity-at-least`, `min-completions`, `variable`), compound conditions (`all`, `any`), and any registered custom condition via `type`. See [`CONDITIONS.md`](CONDITIONS.md) for the full condition reference.

**Localizing reward display labels**

`mcrpg:command` and `mcrpg:scalable_command` rewards support the following display resolution order:

1. **Auto-derived locale route** — McRPG automatically generates a route from the quest key and reward label, e.g. `quests.mcrpg.daily_ore_rush.rewards.hero_title`. Add an entry at that path in `en_quest.yml` (or a `DynamicLocale` file for custom languages) to provide a localized label.
2. **Inline `display` field** — a literal string on the reward definition, used as a fallback when no locale entry exists.
3. **Generic type fallback** — the `quest-reward-types.command.fallback-display` locale key, used when neither of the above resolves.

Quest YAML usage:

```yaml
rewards:
  hero_title:
    type: mcrpg:command
    commands:
      - "title grant {player} hero"
    display: "Hero Title"   # inline fallback if no locale entry exists
```

Locale entry in `en_quest.yml`:

```yaml
quests:
  mcrpg:
    daily_ore_rush:
      rewards:
        hero_title: "Hero Title"
```

A French server owner would add the same route in `plugins/McRPG/localization/french/fr_quest.yml`:

```yaml
quests:
  mcrpg:
    daily_ore_rush:
      rewards:
        hero_title: "Titre de Héros"
```

Custom `QuestRewardType` implementations that want the same localization support should override `describeForDisplay(McRPGPlayer)` and attempt locale resolution via the auto-derived route before falling back to `describeForDisplay()`.

**Runtime classes:**
- [`QuestRewardEntry`](board/template/condition/QuestRewardEntry.java) — wraps an inline reward + optional fallback; exposes `resolveForPlayer(ConditionContext)` which returns either primary or fallback
- [`DistributionRewardEntry`](board/distribution/DistributionRewardEntry.java) — same for distribution tier rewards; also carries `PotBehavior`, `RemainderStrategy`, `minScaledAmount`, `topCount`
- [`RewardFallback`](board/template/condition/RewardFallback.java) — the condition + fallback reward pair

---

## 6. Template Reward Multiplier

When a quest is generated from a template, the template engine applies the rarity's `reward-multiplier` to scale reward amounts. The engine now scales **both** the `amount` and `base-amount` fields on rewards. This means:

- `mcrpg:experience` rewards have their `amount` scaled as before.
- `mcrpg:scalable_command` rewards in templates correctly have their `base-amount` scaled by the rarity's reward multiplier, producing appropriately scaled commands at higher rarities.
- `mcrpg:item` rewards have their top-level `amount` scaled (the nested `item.amount` stack size is not affected).

The multiplier is applied during the `buildDefinition` phase of template generation (see [`TEMPLATES.md`](TEMPLATES.md) section 6 for the template-side perspective).

---

## 7. Registering a Custom Reward Type

Custom reward types are registered via the `ContentExpansion` system. The steps are:

1. **Implement `QuestRewardType`** with a unique `NamespacedKey` (see section 8 for a full example).
2. **Create a `ContentExpansion` subclass** if your plugin doesn't already have one.
3. **Create a `QuestRewardTypeContentPack`** and add your type to it with `addContent`.
4. **Return the pack** from `getExpansionContent()`.
5. **Register the expansion** with `ContentExpansionManager` during your plugin's `onEnable`.

```java
// Step 2-4: your ContentExpansion subclass
public class MyPluginExpansion extends ContentExpansion {

    public static final NamespacedKey EXPANSION_KEY =
            new NamespacedKey("myplugin", "expansion");

    public MyPluginExpansion() {
        super(EXPANSION_KEY);
    }

    @Override
    @NotNull
    public Set<McRPGContentPack<? extends McRPGContent>> getExpansionContent() {
        QuestRewardTypeContentPack rewardPack = new QuestRewardTypeContentPack(this);
        rewardPack.addContent(new MoneyRewardType());
        return Set.of(rewardPack);
    }
}

// Step 5: in your plugin's onEnable
ContentExpansionManager expansionManager = McRPG.getInstance().registryAccess()
        .registry(RegistryKey.MANAGER)
        .manager(McRPGManagerKey.CONTENT_EXPANSION);
expansionManager.registerContentExpansion(new MyPluginExpansion());
```

Register your expansion **before** `QuestManager` loads quest definitions (i.e., early in `onEnable`, before any McRPG quests are loaded). If quests are loaded first, YAML files referencing your type will log "unknown reward type" warnings and skip those entries.

The `McRPGExpansion` class is the canonical example of this pattern — see [`McRPGExpansion.java`](../expansion/McRPGExpansion.java) for how all built-in types are registered.

---

## 8. Worked Example — Custom Money Reward

This example implements a `myplugin:money` reward type that calls a hypothetical `EconomyAPI`.

```java
package com.example.myplugin.quest.reward;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.reward.QuestRewardType;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;

public class MoneyRewardType implements QuestRewardType {

    public static final NamespacedKey KEY =
            new NamespacedKey("myplugin", "money");

    // Amount of money to grant. 0 means unconfigured (base instance).
    private final long amount;

    /** Base (unconfigured) instance for registration. */
    public MoneyRewardType() {
        this.amount = 0;
    }

    /** Configured instance. Always constructed via parseConfig or fromSerializedConfig. */
    private MoneyRewardType(long amount) {
        this.amount = amount;
    }

    @Override
    @NotNull
    public NamespacedKey getKey() {
        return KEY;
    }

    @Override
    @NotNull
    public MoneyRewardType parseConfig(@NotNull Section section) {
        // Return a NEW instance -- never mutate the base instance.
        return new MoneyRewardType(section.getLong("amount", 0L));
    }

    @Override
    public void grant(@NotNull Player player) {
        if (amount <= 0) {
            return;
        }
        EconomyAPI.deposit(player, amount);
    }

    // serializeConfig and fromSerializedConfig must be symmetric.
    // These are used for the pending reward queue (offline player delivery).

    @Override
    @NotNull
    public Map<String, Object> serializeConfig() {
        return Map.of("amount", amount);
    }

    @Override
    @NotNull
    public MoneyRewardType fromSerializedConfig(@NotNull Map<String, Object> config) {
        long amt = config.containsKey("amount")
                ? ((Number) config.get("amount")).longValue()
                : 0L;
        return new MoneyRewardType(amt);
    }

    // Implement withAmountMultiplier and getNumericAmount so this type
    // works correctly in SPLIT_EVEN / SPLIT_PROPORTIONAL distribution tiers.

    @Override
    @NotNull
    public MoneyRewardType withAmountMultiplier(double multiplier) {
        long scaled = Math.max(1, Math.round(amount * multiplier));
        return new MoneyRewardType(scaled);
    }

    @Override
    @NotNull
    public OptionalLong getNumericAmount() {
        return OptionalLong.of(amount);
    }

    @Override
    @NotNull
    public String describeForDisplay() {
        return "$" + amount;
    }

    @Override
    @NotNull
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.of(MyPluginExpansion.EXPANSION_KEY);
    }
}
```

**YAML usage — inline reward:**

```yaml
quests:
  myplugin:daily_mining:
    scope: mcrpg:single_player
    phases:
      mine:
        stages:
          main:
            objectives:
              break-blocks:
                key: myplugin:break_blocks
                type: mcrpg:block_break
                required-progress: 64
    rewards:
      money:
        type: myplugin:money
        amount: 500
```

**YAML usage — distribution reward with split:**

```yaml
reward-distribution:
  top-earner:
    type: mcrpg:top_players
    top-player-count: 3
    split-mode: SPLIT_PROPORTIONAL
    rewards:
      money-pot:
        type: myplugin:money
        amount: 10000
        pot-behavior: SCALE
        remainder-strategy: TOP_CONTRIBUTOR
        min-scaled-amount: 10
```

**Registration (inside your `ContentExpansion`):**

```java
QuestRewardTypeContentPack pack = new QuestRewardTypeContentPack(this);
pack.addContent(new MoneyRewardType());
```

---

## 9. Common Pitfalls

- **Missing `serializeConfig` / `fromSerializedConfig` implementations** — these methods have no default. If you leave them returning empty maps or throwing, pending rewards for offline players will be silently lost or fail to reconstruct on login.

- **Missing `withAmountMultiplier` override** — the default returns `this` (the same amount). If your type is used inside a `SPLIT_EVEN` or `SPLIT_PROPORTIONAL` tier without overriding this method, every qualifying player receives the full unreduced amount. The resolver logs a warning but does not throw.

- **Returning `this` from `parseConfig`** — `parseConfig` is called on the base instance. If you return `this` instead of a new instance, every quest that uses this reward type shares the same object and will have the same (last-parsed) values. Always return `new YourRewardType(parsedValues)`.

- **Mutable state in configured instances** — a configured instance is stored in the quest definition and referenced by every active quest instance that uses that definition simultaneously. Any mutable field (e.g. a counter, cached value) will be shared across all those quests. Keep configured instances immutable.

- **Forgetting `getExpansionKey()`** — `QuestRewardType` extends `McRPGContent`, which requires `getExpansionKey()`. If it returns `Optional.empty()` or the wrong key, content tracking and localization resolution may behave unexpectedly. Always return your expansion's `NamespacedKey`.

- **Registering after quest load** — if your expansion is registered after `QuestManager` has already loaded quest definitions from YAML, any quest file referencing your type will have skipped that reward entry with a warning. Register your expansion early in `onEnable`, before McRPG processes quest files.
