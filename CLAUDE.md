# McRPG — Claude Code Guide

McRPG is a Minecraft RPG plugin (Paper 1.21, Java 21) built as a modern replacement for mcMMO. It adds a skill and ability system where players level skills by performing actions and unlock/activate abilities tied to those skills. Content is modular via a `ContentExpansion` system. The plugin depends on **McCore** — an owned shared library — and can be modified when changes would benefit multiple projects.

---

## McCore Relationship

McCore (`com.diamonddagger590:McCore:1.0.0.17-SNAPSHOT`) is an **owned project** that provides the plugin framework shared across all related plugins.

**When to modify McCore instead of McRPG:**
- Adding a new registry/manager abstraction that other plugins would use
- New task utilities (extending CoreTask / DelayableCoreTask patterns)
- New player/entity wrapper abstractions
- Parser enhancements for equation evaluation
- Any reusable utility that isn't McRPG-specific

**Key McCore abstractions used by McRPG:**
- `CorePlugin` / `CoreBootstrap` — plugin lifecycle base classes
- `Registry<K, V>` / `Manager<K, V>` — typed registry system
- `RegistryAccess` / `RegistryKey` — access point for all registries
- `Statistic` / `StatisticRegistry` / `PlayerStatisticData` — typed statistic tracking (registration, storage, dirty-tracking, events)
- `DelayableCoreTask` — Bukkit scheduler wrapper with second-based delays
- `Parser` — math equation evaluator (used for scaling formulas like experience curves)
- `CustomItemWrapper` — item abstraction for material/custom item detection

McCore is shaded and relocated to `us.eunoians.mcrpg.mccore` in the final jar.

---

## Build & Run

| Command | Description |
|---------|-------------|
| `./gradlew verifiedShadowJar` | Clean → test → build shaded jar **(recommended)** |
| `./gradlew fastShadowJar` | Clean → build shaded jar (skips tests) |
| `./gradlew test` | Run tests only (also generates JaCoCo report) |
| `./gradlew jacocoTestReport` | Generate coverage report from last test run |
| `./gradlew shadowJar` | Build shaded jar (no clean) |

Output jar: `build/libs/McRPG-<version>-<git-hash>.jar`

**Stack:** Java 21, Paper API 1.21.11, Gradle Kotlin DSL (`build.gradle.kts`)

---

## Testing

- **Framework:** JUnit 6 (junit-bom), MockBukkit v1.21, Mockito 3
- **Base class:** Extend `McRPGBaseTest` (found in `src/testFixtures/`)
- **Fixtures:** Shared test helpers live in `src/testFixtures/java/`
- **Structure:** Test files mirror the main source package structure under `src/test/java/`
- There are no integration tests — validation of gameplay behavior is done manually on a running Paper server
- **Config coverage:** All concrete `ConfigurableTierableAbility` implementations must carry `@ParserConfigKeys` declaring their Parser-backed tier-config YAML keys. The `ParserConfigKeysPresenceTest` enforces this at CI time, and `ParserConfigCoverageTest` validates those keys exist in the bundled YAML defaults.

---

## Project Structure

```
src/main/java/us/eunoians/mcrpg/
├── McRPG.java                          # Plugin main class (extends CorePlugin)
├── ability/
│   ├── Ability.java                    # Core ability interface
│   ├── BaseAbility.java                # Component registration logic
│   ├── AbilityData.java                # Per-holder attribute container (DTO)
│   ├── AbilityRegistry.java            # Registry of all registered abilities
│   ├── attribute/                      # Typed ability attribute definitions
│   ├── combo/                          # Combo input system (ComboManager, ComboPattern, PlayerComboState)
│   ├── component/                      # Reusable activation/cancel logic registered on an ability (priority-ordered)
│   │   ├── activatable/                # EventActivatableComponent implementations
│   │   └── cancel/                     # EventCancellingComponent implementations
│   ├── impl/                           # Concrete ability implementations by skill
│   │   ├── swords/                     # Bleed, DeeperWound, Vampire, etc.
│   │   ├── mining/                     # ExtraOre, ItsATriple, OreScanner, etc.
│   │   ├── herbalism/                  # MassHarvest, InstantIrrigation, etc.
│   │   └── woodcutting/                # ExtraLumber, HeavySwing, etc.
│   └── impl/type/                      # Ability capability interfaces (ActiveAbility, PassiveAbility, etc.)
├── skill/
│   ├── Skill.java                      # Core skill interface
│   ├── impl/                           # Concrete skill implementations (Swords, Mining, etc.)
│   └── impl/type/                      # Skill capability interfaces (ConfigurableSkill, HeldItemBonusSkill)
├── entity/
│   ├── holder/
│   │   ├── AbilityHolder.java          # Base: entity with abilities
│   │   ├── LoadoutHolder.java          # Restricts to loadout abilities
│   │   └── SkillHolder.java            # AbilityHolder with levelable skills
│   └── player/
│       └── McRPGPlayer.java            # Concrete player (extends CorePlayer)
├── stat/
│   ├── PlayerStat.java                 # Abstract base for player stats (mana, health, future stats)
│   ├── PlayerStatRegistry.java         # Registry of all player stat definitions
│   ├── impl/                           # Stat type subclasses (ResourcePoolPlayerStat, FlatPlayerStat, ConfigurableResourcePoolPlayerStat)
│   └── instance/                       # Per-player mutable state (PlayerStatInstance, PlayerStatData, PlayerStatModifier)
├── statistic/
│   └── McRPGStatistic.java             # Global statistic constants only (blocks mined, damage dealt, etc.)
├── expansion/
│   ├── ContentExpansion.java           # Base class for content modules
│   ├── McRPGExpansion.java             # Native content registration (all built-in content)
│   └── content/                        # McRPGContentPack sub-types (AbilityContentPack, StatisticContentPack, QuestContentPack, etc.)
├── quest/
│   ├── QuestManager.java               # Central manager: loading, starting, progressing, completing quests
│   ├── definition/                     # Immutable quest blueprints (definition layer)
│   │   ├── QuestDefinition.java        # Full quest blueprint: phases, scope, rewards, repeat mode
│   │   ├── QuestDefinitionRegistry.java
│   │   ├── QuestPhaseDefinition.java   # Ordered stage group; ALL or ANY completion mode
│   │   ├── QuestStageDefinition.java   # Group of objectives; all must complete
│   │   ├── QuestObjectiveDefinition.java
│   │   └── QuestRepeatMode.java        # ONCE, LIMITED, COOLDOWN, COOLDOWN_LIMITED
│   ├── impl/                           # Mutable runtime state
│   │   ├── QuestInstance.java          # Runtime quest: state, scope, timestamps, stage instances
│   │   ├── QuestState.java             # NOT_STARTED, IN_PROGRESS, COMPLETED, CANCELLED
│   │   ├── stage/                      # QuestStageInstance, QuestStageState
│   │   ├── objective/                  # QuestObjectiveInstance, QuestObjectiveState
│   │   └── scope/                      # QuestScope, QuestScopeProvider, QuestScopeProviderRegistry
│   │       └── impl/                   # SinglePlayer, Permission, Land scope types
│   ├── objective/type/                 # Extensible objective type system
│   │   ├── QuestObjectiveType.java     # Interface: behavior + progress-tracking for an objective category
│   │   ├── QuestObjectiveTypeRegistry.java
│   │   ├── QuestObjectiveProgressContext.java
│   │   └── builtin/                    # BlockBreakObjectiveType, MobKillObjectiveType, etc.
│   ├── reward/                         # Extensible reward type system
│   │   ├── QuestRewardType.java        # Interface: how a reward is granted
│   │   ├── QuestRewardTypeRegistry.java
│   │   ├── PendingReward.java          # Serialized reward queued for offline player
│   │   └── builtin/                    # Command, ScalableCommand, Experience, AbilityUpgrade, etc.
│   ├── source/                         # How quests are obtained
│   │   ├── QuestSource.java            # Extensible: board, ability upgrade, manual, etc.
│   │   └── QuestSourceRegistry.java
│   └── board/                          # Quest board system (see Quest Board System section)
│       ├── QuestBoard.java
│       ├── QuestBoardManager.java
│       ├── QuestBoardTerminator.java
│       ├── BoardOffering.java
│       ├── BoardMetadata.java
│       ├── category/                   # BoardSlotCategory config and registry
│       ├── configuration/              # ReloadableRarityConfig, ReloadableTemplateConfig
│       ├── distribution/               # RewardDistributionConfig, resolver, granter, built-in types
│       ├── generation/                 # PersonalOfferingGenerator, QuestPool, SlotGenerationLogic
│       ├── rarity/                     # QuestRarity, QuestRarityRegistry
│       └── template/                   # QuestTemplate, QuestTemplateEngine, conditions, variables
├── gui/
│   ├── board/                          # Quest board GUI
│   │   ├── QuestBoardGui.java          # Main board inventory GUI
│   │   ├── OfferingLoreBuilder.java    # Builds offering item lore from quest definition
│   │   ├── BoardGuiMode.java           # Enum: SHARED / PERSONAL view modes
│   │   ├── ScopedEntitySelectorGui.java
│   │   └── slot/                       # BoardOfferingSlot, ScopedOfferingSlot, ScopedTabSlot, etc.
│   └── quest/                          # Active/history quest GUIs
│       ├── ActiveQuestGui.java
│       ├── QuestDetailGui.java
│       ├── QuestHistoryGui.java
│       ├── QuestAbandonConfirmGui.java
│       └── slot/                       # Detail, overview, reward, phase, abandon, history slots
├── listener/
│   ├── ability/                        # Per-skill ability Bukkit event listeners
│   │   ├── AbilityListener.java        # Interface with activateAbilities() default and passive mana checks
│   │   └── <skill>/
│   ├── statistic/                      # Statistic tracking listeners
│   │   ├── AbilityStatisticListener.java   # Tracks ability activation stats
│   │   └── SkillStatisticListener.java     # Tracks skill XP, level-up, and gameplay stats
│   └── quest/                          # Quest progress and lifecycle listeners
│       ├── QuestProgressListener.java  # Base interface for progress event handling
│       ├── BlockBreakQuestProgressListener.java
│       ├── MobKillQuestProgressListener.java
│       ├── QuestStartListener.java
│       ├── QuestCompleteListener.java
│       ├── QuestCancelListener.java
│       ├── QuestFeedbackListener.java
│       └── AbilityUpgradeQuestListener.java
├── event/
│   ├── ability/                        # Custom Bukkit events per ability activation
│   └── quest/                          # QuestStartEvent, QuestCompleteEvent, QuestCancelEvent, etc.
├── database/table/
│   ├── SkillDAO.java                   # Skill data persistence
│   ├── LoadoutAbilityDAO.java          # Loadout slot persistence
│   ├── quest/                          # Quest instance, stage, objective, contribution, pending reward DAOs
│   │   ├── QuestInstanceDAO.java
│   │   ├── QuestStageInstanceDAO.java
│   │   ├── QuestObjectiveInstanceDAO.java
│   │   ├── QuestObjectiveContributionDAO.java
│   │   ├── QuestCompletionLogDAO.java
│   │   ├── PendingRewardDAO.java
│   │   └── scope/                      # Per-scope-type DAOs (SinglePlayer, Permission, Land)
│   └── board/                          # Board rotation, offering, cooldown, personal tracking DAOs
│       ├── BoardRotationDAO.java
│       ├── BoardOfferingDAO.java
│       ├── BoardCooldownDAO.java
│       ├── PlayerBoardStateDAO.java
│       ├── PersonalOfferingTrackingDAO.java
│       └── ScopedBoardStateDAO.java
├── configuration/
│   ├── FileType.java                   # Enum of all config file types
│   └── file/                           # YAML config file wrappers (one per skill/system)
├── registry/                           # McRPGRegistryKey, McRPGManagerKey, ability/skill registries
└── util/
    └── McRPGMethods.java               # Namespace, MiniMessage, PAPI utilities
```

---

## Domain Terminology

### Ability & Skill System

| Term | Meaning |
|------|---------|
| **Ability** | An action or passive effect an entity can use. Can be active (player-triggered) or passive (auto-fires on event). |
| **Skill** | A leveling system (e.g., Swords, Mining). Leveling a skill unlocks and scales its abilities. |
| **AbilityHolder** | McRPG's wrapper around any entity that can hold/use abilities. Non-player entities use this base type. |
| **LoadoutHolder** | An AbilityHolder restricted to only the abilities in their active loadout. Players are always loadout holders. |
| **SkillHolder** | An AbilityHolder that also has levelable skills. Players are SkillHolders. |
| **McRPGPlayer** | Concrete player object — implements SkillHolder, LoadoutHolder, and McCore's CorePlayer. |
| **Tier** | Enhancement level of an ability. Higher tiers change ability mechanics (not just stat scaling). |
| **Mana** | Per-player resource pool consumed on active ability activation. Tracked via `PlayerStatInstance` keyed by `McRPGPlayerStat.MANA`. Regenerates passively at a flat rate. |
| **Combo Activation** | Click-combo sequences (RRR, RRL, RLR) that trigger active abilities. All active abilities activate exclusively via combos gated by mana. Managed by `ComboManager`. |
| **ComboActivatable** | Interface marking an active ability as combo-eligible. Extends `ManaAbility`. Provides `comboActivate(AbilityHolder)` returning boolean (true=executed, false=cancelled internally). |
| **ManaAbility** | Interface declaring `getManaCost(AbilityHolder)`. Implemented by `ComboActivatable` and `ConfigurableActiveAbility`. Passive abilities can opt in but none currently do. |
| **PlayerStat** | Abstract base for per-player tracked stats (mana, health). Registered in `PlayerStatRegistry`. Instance state in `PlayerStatData`/`PlayerStatInstance`. |
| **PlayerStatModifier** | Extensible class keyed by `NamespacedKey` for flat/percent bonuses to a stat's effective max. Supports virtual methods for stacking and timed expiration. |
| **PlayerStatConsumeEvent** | Cancellable event fired before every `PlayerStatInstance.consume()` call. Allows third-party cost modification or cancellation. |
| **Cooldown** | Time lock applied to an ability after it activates. Managed via AbilityHolder's cooldown tracking. |
| **Component** | A modular, reusable piece of activation/cancel logic registered on an ability. Components are priority-ordered; first failing component stops the chain. |
| **Attribute** | A typed `AbilityAttribute<T>` stored in `AbilityData` — contains per-holder ability state (tier, cooldown, toggle, etc.). Created via factory (no reflection). |
| **ContentExpansion** | A module that bundles skills, abilities, statistics, player settings, and localization into a single registration unit. |
| **StatisticContent** | Wrapper that pairs a McCore `Statistic` with an expansion's `NamespacedKey` for content-pack registration. |
| **StatisticContentPack** | A `McRPGContentPack` that collects `StatisticContent` entries — one per expansion. Registered alongside skill/ability packs in `ContentExpansion.getExpansionContent()`. |
| **McRPGStatistic** | Constants-only class holding global statistic definitions (blocks mined, damage dealt, etc.). Per-skill keys come from `Skill.getExperienceStatisticKey()` / `getMaxLevelStatisticKey()`; per-ability keys come from `ActiveAbility.getActivationStatisticKey()`. |
| **DAO** | Data Access Object — static JDBC methods for reading/writing ability and skill data. |

### Quest System

| Term | Meaning |
|------|---------|
| **QuestDefinition** | Immutable blueprint describing a complete quest: its phases, scope type, optional expiration, rewards, and repeat mode. Shared across all runtime instances. Registered in `QuestDefinitionRegistry`. |
| **QuestPhaseDefinition** | Immutable definition of an ordered group of stages within a quest. Supports `ALL` (every stage must complete) or `ANY` (first stage to complete advances the quest) completion modes. Not persisted as an instance — phase state is computed from child stage states at runtime. |
| **QuestStageDefinition** | Immutable definition of a single stage, containing one or more objectives. All objectives must complete for the stage to be considered done. May carry stage-level rewards. |
| **QuestObjectiveDefinition** | Immutable definition of a single trackable objective (e.g., "break 50 stone blocks"). Carries the `QuestObjectiveType` key and configuration. |
| **QuestInstance** | Mutable runtime object created from a `QuestDefinition`. Tracks `QuestState`, start/end/expiration timestamps, the active `QuestScope`, and child `QuestStageInstance` / `QuestObjectiveInstance` trees. Persisted to SQL. |
| **QuestState** | Enum for a `QuestInstance`'s lifecycle: `NOT_STARTED`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`. |
| **QuestScope** | The set of players participating in a specific `QuestInstance` (e.g., a single player, a permission group, a Lands territory). Each quest instance has exactly one scope. |
| **QuestScopeProvider** | Abstract factory responsible for creating new and loading persisted `QuestScope` instances for a specific scope type. Registered in `QuestScopeProviderRegistry` by `NamespacedKey`. Extensible via `QuestScopeProviderContentPack`. |
| **QuestSource** | Describes how a quest was obtained (board acceptance, ability upgrade trigger, manual grant, etc.). Registered in `QuestSourceRegistry`. Controls whether the quest is player-abandonable. Extensible via `QuestSourceContentPack`. |
| **QuestObjectiveType** | Extensible interface defining the behavior and progress-tracking logic for a category of objectives (e.g., `BlockBreakObjectiveType`, `MobKillObjectiveType`). Registered in `QuestObjectiveTypeRegistry`. Extensible via `QuestObjectiveTypeContentPack`. |
| **QuestRewardType** | Extensible interface defining how a specific reward is granted (e.g., `CommandRewardType`, `ExperienceRewardType`, `AbilityUpgradeRewardType`). Registered in `QuestRewardTypeRegistry`. Extensible via `QuestRewardTypeContentPack`. |
| **PendingReward** | A serialized reward queued for a player who was offline at the time of grant. Stored in the DB and granted at next login; expires after a configurable duration. |

---

## Architecture Overview

### Entity Hierarchy

```
AbilityHolder          — can hold and use abilities
  └── LoadoutHolder    — restricted to loadout abilities
        └── SkillHolder — also has levelable skills (McRPGPlayer implements all three)
```

### Registry Access Pattern

All managers and registries are accessed through `RegistryAccess`. Never instantiate managers directly.

```java
// Get a manager
EntityManager entityManager = mcRPG.registryAccess()
    .registry(RegistryKey.MANAGER)
    .manager(McRPGManagerKey.ENTITY);

// Get a McRPG-specific registry
AbilityRegistry abilityRegistry = mcRPG.registryAccess()
    .registry(McRPGRegistryKey.ABILITY);

// Get the file manager and retrieve a config
YamlDocument config = mcRPG.registryAccess()
    .registry(RegistryKey.MANAGER)
    .manager(McRPGManagerKey.FILE)
    .getFile(FileType.SWORDS_CONFIG);
```

### Ability Lifecycle

#### Active Abilities (Combo-Based)

1. Ability registered in `AbilityRegistry` via `McRPGExpansion.getAbilityContent()`
2. Added to holder's available abilities and loadout
3. Player performs a click-combo (RRR, RRL, or RLR) while holding an allowed item
4. `OnComboInputListener` feeds inputs to `ComboManager` → `PlayerComboState` → pattern completion → `ComboCompleteEvent`
5. `OnComboCompleteListener` resolves the ability from loadout slot index
6. Gate 1: Cooldown check — if on cooldown, deny with feedback (action bar + chat + sound)
7. Gate 2: Mana check — fire `PlayerStatConsumeEvent`, then `PlayerStatInstance.consume(cost)`. If insufficient, deny with feedback
8. `ability.comboActivate(abilityHolder)` is called
9. Inside `comboActivate()`: fire the ability's custom event, check `isCancelled()`, perform effect
10. If `comboActivate()` returns `false`: mana is refunded via `restore(effectiveCost)`, no cooldown applied
11. If `comboActivate()` returns `true`: cooldown applied (if `CooldownableAbility`)

#### Passive Abilities (Event-Driven)

1. Ability registered in `AbilityRegistry` via `McRPGExpansion.getAbilityContent()`
2. A Bukkit event fires (e.g., `EntityDamageByEntityEvent`, `BlockBreakEvent`)
3. A skill listener implementing `AbilityListener` calls `activateAbilities(uuid, event)`
4. Components are checked in priority order — first failing component stops activation
5. Cooldown is validated (ability is skipped if on cooldown)
6. Optional mana check: if ability implements `ManaAbility` and cost > 0, mana is consumed (skipped silently on insufficient mana)
7. `ability.activateAbility(abilityHolder, event)` is called
8. If returns `false`: mana refunded (if consumed). If returns `true`: mana stays consumed

### Component System

Components are registered in the ability's constructor and sorted by priority (lowest first). The chain stops at the first failure.

```java
public class MyAbility extends McRPGAbility implements PassiveAbility, ConfigurableSkillAbility {

    public static final NamespacedKey MY_ABILITY_KEY =
        new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "my_ability");

    public MyAbility(@NotNull McRPG mcRPG) {
        super(mcRPG, MY_ABILITY_KEY);
        // Priority 0 runs first; if it fails, priorities 1+ are skipped
        addActivatableComponent(SharedComponents.HOLDING_SWORD_COMPONENT, EntityDamageByEntityEvent.class, 0);
        addActivatableComponent(MyAbilityComponents.CHANCE_CHECK_COMPONENT, EntityDamageByEntityEvent.class, 1);
    }
}
```

Two component types used by passive abilities:
- `EventActivatableComponent` — must pass (`shouldActivate()`) for activation to proceed
- `EventCancellingComponent` — if `shouldCancel()` returns true, cancels the underlying Bukkit event

### Combo Activation Pattern

Active abilities implement `ComboActivatable` (which extends `ManaAbility`):

```java
public final class MyActiveAbility extends McRPGAbility implements ConfigurableActiveAbility,
        ConfigurableSkillAbility, ComboActivatable {

    @Override
    public boolean comboActivate(@NotNull AbilityHolder abilityHolder) {
        var playerOpt = RegistryAccess.registryAccess().registry(McRPGRegistryKey.MANAGER)
                .manager(McRPGManagerKey.PLAYER).getPlayer(abilityHolder.getUUID());
        if (playerOpt.isEmpty()) {
            return false;
        }
        MyActivateEvent event = new MyActivateEvent(abilityHolder, ...);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }
        // Apply effect
        return true;
    }
}
```

Mana costs are configured per-ability in the skill config's `tier-configuration.all-tiers.mana-cost` as a Parser formula string with `tier` variable. `ConfigurableActiveAbility` provides the default `getManaCost()` implementation that resolves this. Cooldowns follow the same pattern via `tier-configuration.all-tiers.cooldown`.

The combo listener (`OnComboCompleteListener`) handles mana consumption, refund on cancellation, cooldown application, and failure feedback. Abilities never call `putHolderOnCooldown()` in their own `comboActivate()` method.

### Configuration (boostedyaml)

Config values are accessed via `Route` objects defined as constants in `*ConfigFile` classes:

```java
// Route constants live in the config file class
Route route = SwordsConfigFile.BLEED_BASE_DAMAGE;
double damage = getYamlDocument().getDouble(route);

// Dynamic route construction
Route dynamicRoute = Route.fromString("material-modifiers." + materialKey);
```

For config values that must update without a server restart, implement `ReloadableContentAbility` on the ability class and use `ReloadableSet<T>` fields:

```java
public final class MyAbility extends McRPGAbility implements PassiveAbility, ReloadableContentAbility {

    private final ReloadableSet<CustomBlockWrapper> VALID_BLOCK_TYPES;

    public MyAbility(@NotNull McRPG mcRPG) {
        super(mcRPG, MY_ABILITY_KEY);
        this.VALID_BLOCK_TYPES = new ReloadableSet<>(
            getYamlDocument(),
            MyConfigFile.VALID_BLOCKS,
            strings -> strings.stream().map(CustomBlockWrapper::new).collect(Collectors.toSet())
        );
    }

    @Override
    public Set<ReloadableContent<?>> getReloadableContent() {
        return Set.of(VALID_BLOCK_TYPES);
    }
}

// Access the current value anywhere:
VALID_BLOCK_TYPES.getContent().contains(block);
```

### Mana Cost Configuration

Mana costs follow the `getString()` + Parser pattern with `tier` as a variable:

```yaml
# In skill_configuration/<skill>_configuration.yml
ability-configuration:
  my-ability:
    tier-configuration:
      all-tiers:
        mana-cost: "50-(5.5*tier)"   # Formula evaluated at runtime
        cooldown: "20-(1.5*tier)"     # Same pattern for cooldowns
      tier-3:
        mana-cost: 25                 # Explicit override (optional)
```

Resolution order: tier-specific value wins if present; otherwise `all-tiers` formula is evaluated. A global minimum floor (`config.yml` → `stats.mana.minimum-ability-cost`) is applied after evaluation.

### Mana Balance Philosophy

Active abilities follow a **"Slow Regen, High Stakes"** balance framework. All mana costs and cooldowns are tuned against these parameters:

- **Pool:** 100 max mana, 2/sec passive regen, 50s full recovery from empty
- **Minimum cost floor:** 1 (enforced after Parser formula evaluation)
- **Three cost buckets:**

| Bucket | T1 Cost | T5 Cost | Cooldown | When to Use |
|--------|---------|---------|----------|-------------|
| Light | 28-32 | 12-16 | 1-2s anti-spam | Combat, mobility, quick single-use effects |
| Medium | 42-50 | 25-33 | 8-15s | Buffs, sustain, moderate utility |
| Heavy | 70-80 | 55-60 | 12-20s | Powerful utility, resource generation, AoE gathering |

**Design principle:** Mana is the primary gate, not cooldowns. A player with 3 active ability slots would be incentivized to swap loadouts if cooldowns are the real bottleneck — mana is shared across the pool regardless of loadout, so it is the swap-proof gate. Tandem utility pairs (e.g., VerdantSurge + MassHarvest) are designed so their combined T1 cost exceeds 100 (forces sequencing) and combined T5 cost is ~85-95 (the tandem tier reward).

**Formula pattern:** All costs/cooldowns use `getString()` + Parser with `tier` variable: `"baseCost - (scaleFactor * tier)"`.

**Full framework:** See `.cursor/rules/mana-balance-philosophy.mdc` for the cookie-cutter classification decision tree, per-bucket formula ranges, validation checklist, and agent workflow instructions (agents must present balance options to the user rather than choosing values autonomously).

### Registering New Content

Add to `McRPGExpansion`:

```java
// In getAbilityContent()
abilityContent.addContent(new MyAbility(mcRPG));

// In getSkillContent()
skillContent.addContent(new MySkill(mcRPG));
```

Statistics are automatically composed from three sources in `getStatisticContent()`:
1. **Global statistics** — `McRPGStatistic.ALL_STATIC_STATISTICS` (blocks mined, damage dealt, total XP, etc.)
2. **Per-skill statistics** — generated by `Skill.getDefaultStatistics()` (e.g., swords_experience, swords_max_level)
3. **Per-ability statistics** — generated by `Ability.getDefaultStatistics()` (e.g., bleed_activations for active abilities)

Third-party expansions should follow the same pattern — include their own `StatisticContentPack` in `getExpansionContent()` with global, per-skill, and per-ability statistics.

**New tierable abilities** must carry `@ParserConfigKeys` (even if the array is empty) and add a corresponding entry to `ParserConfigCoverageTest`'s registry. The `ParserConfigKeysPresenceTest` will fail CI if the annotation is missing.

#### Third-party ability locale color requirements

Every ability's `name:` field in its locale YAML file must use one of the three semantic type palette placeholders — **not** `<red>`, `<gold>`, or any raw hex color.

| Ability type | Placeholder to use |
|---|---|
| `ComboActivatable` (active, player-triggered) | `<ability-active>` |
| `PassiveAbility` + `ABILITY_UNLOCKED_ATTRIBUTE` (tierable/upgradable passive) | `<ability-passive>` |
| All others (always-on innate passives, no unlock gate) | `<ability-innate>` |

Example locale YAML for a third-party innate ability:

```yaml
ability:
  ability-specific-localization:
    my-ability:
      display-item:
        name: "<ability-innate><ability>"
        lore:
          - "<body>Some description text"
```

The `AbilityNameColorConsistencyTest` enforces this rule for all bundled abilities at CI time. Third-party expansions should ship a similar test for their own locale YAML files.

#### Third-party skill locale color requirements

Every skill's `name:` field in its locale YAML file must use the per-skill palette placeholder — **not** `<gold>`, `<primary>`, or any raw hex color. Bundled skills ship with these:

| Skill | Placeholder |
|---|---|
| Swords | `<skill-swords>` |
| Mining | `<skill-mining>` |
| Herbalism | `<skill-herbalism>` |
| Woodcutting | `<skill-woodcutting>` |

Third-party skills should add a `skill-{key}: "<color:#RRGGBB>"` entry to `config.yml`'s `palette:` section and use `<skill-{key}>` in their locale's `name:` field. The `Skill.getColoredName(McRPGPlayer)` method resolves to this colored name at runtime — all callsites that show a skill name in player-facing text must call `getColoredName()`, not `getName()`. The only exception is when the name is used as a raw command argument (e.g., `player.performCommand`), where plain text is required.

Example locale YAML for a third-party skill:

```yaml
skills:
  my-skill:
    display-item:
      name: '<skill-my-skill><skill></skill-my-skill>'
      skill-name: 'My Skill'
```

The `SkillNameColorConsistencyTest` enforces this rule for all bundled skills at CI time. Third-party expansions should ship a similar test for their own locale YAML files.

New global statistics go in `McRPGStatistic` as `static final` constants. Per-skill statistics are constructed in `McRPGSkill.getDefaultStatistics()` using the key-derivation methods `Skill.getExperienceStatisticKey()` and `Skill.getMaxLevelStatisticKey()`. Per-ability activation statistics are constructed in `ActiveAbility.getDefaultStatistics()` using `ActiveAbility.getActivationStatisticKey()`. Third-party skills and abilities should override these default methods if they need custom key conventions.

### DAO Pattern

```java
// Static creation at startup
SkillDAO.attemptCreateTable(connection, database);

// CRUD — always static, always take Connection as first arg
Optional<SkillData> data = SkillDAO.getSkillData(connection, playerUUID);
SkillDAO.saveSkillData(connection, playerUUID, skillData);
```

Use `BatchTransaction` and `FailSafeTransaction` helpers from McCore for multi-statement operations.

---

## Naming Conventions

| Type | Convention | Example |
|------|-----------|---------|
| Abstract base | `Base` prefix | `BaseAbility`, `BaseSkill` |
| McRPG native impl | `McRPG` prefix | `McRPGAbility`, `McRPGPlayer`, `McRPGSkill` |
| DTOs | `Data` suffix | `AbilityData`, `SkillHolderData` |
| DAOs | `DAO` suffix | `SkillDAO`, `LoadoutAbilityDAO` |
| Registries | `Registry` suffix | `AbilityRegistry`, `SkillRegistry` |
| Custom events | `Event` suffix | `BleedActivateEvent`, `SkillLevelUpEvent` |
| Bukkit listeners | `On` + action | `OnAttackAbilityListener` |
| Components | `Component` suffix | `EventActivatableComponent`, `BleedEligibleForTargetComponent` |
| Attributes | `Attribute` suffix | `AbilityTierAttribute`, `AbilityCooldownAttribute` |
| Config file wrappers | `ConfigFile` suffix | `SwordsConfigFile`, `MainConfigFile` |
| Component groupings | `Components` suffix | `BleedComponents`, `SwordsComponents` |

Ability `NamespacedKey` constants are `static final` fields on the ability class itself:
```java
public static final NamespacedKey BLEED_KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "bleed");
```

---

## Required Annotations

- `@NotNull` (IntelliJ annotations v12) on all non-null return types and parameters
- `@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)` on Bukkit event handlers
- `@Override` on all overridden methods

---

## Anti-Patterns to Avoid

- **No reflection** — use attribute factory pattern (`attribute.create(value)`) instead of `Class.forName()` or `getDeclaredField()`
- **No hard-coded behavior values** — all tunable values (damage, cooldown, chance) must come from YAML config via `Route`
- **No deep inheritance** — compose behavior by implementing multiple interfaces (`PassiveAbility`, `CooldownableAbility`, `ConfigurableSkillAbility`); avoid 3+ level hierarchies
- **No mutable global static state** — use registries accessed via `RegistryAccess`; the only acceptable static access is `McRPG.getInstance()` when no instance is available
- **No `getInstance()` singletons for domain state** — per-player state belongs on the player object (`McRPGPlayer`); per-system state should be a `Manager` registered in the registry and accessed via `registryAccess()`. `getInstance()` singletons hide coupling, prevent constructor injection, and force tests to set up global state instead of passing parameters.
- **No static utility classes for domain logic** — a method that calls any global accessor (`SomeClass.getInstance()`, `McRPG.getInstance()`, or accesses a singleton) has a dependency even if it does not appear in the parameter list. If a static method would break when the global state it reaches for is null, it has a hidden dependency. Model it as an object collaborator with injected state.
- **No direct entity casting without guard** — use `instanceof` pattern matching: `if (entity instanceof Player player) { ... }`
- **No ability state stored on the ability object** — ability state is per-holder, stored in `AbilityData`/`AbilityAttribute`; ability objects are shared singletons
- **Don't put McRPG-specific logic in McCore** — McCore changes affect all downstream plugins
- **No fully-qualified type references in method bodies** — always declare a top-level `import` statement for the type; writing `org.bukkit.Location loc` inline is forbidden even when it compiles
- **No `e.printStackTrace()`** — always use `Logger.log(Level.SEVERE, "context message", e)` so stack traces route through the server logger and are preserved in log aggregators
- **No `Optional.get()` without a guard** — always use `orElse`, `orElseGet`, `orElseThrow`, or check `isPresent()` first; bare `.get()` is a guaranteed crash on the empty path
- **No Bukkit API calls from async threads** — any world, entity, or inventory mutation must be scheduled on the main thread via `Bukkit.getScheduler().runTask(plugin, () -> { ... })`
- **No blocking `.get()` on a `CompletableFuture` from the main thread** — this deadlocks if the future's completion path needs the main thread scheduler
- **No entity or player object references in long-lived collections** — store `UUID` instead; holding `Entity`/`Player` objects prevents garbage collection of unloaded entities
- **No unbounded `Map` or `Set` fields without a documented eviction strategy** — insert-only caches are memory leaks; document the cleanup lifecycle event in Javadoc
- **No `putHolderOnCooldown()` inside `comboActivate()`** — the combo listener (`OnComboCompleteListener`) manages cooldown application for combo-activated abilities. Calling it inside the ability causes double-cooldown
- **No void-return `comboActivate()` or `activateAbility()`** — both return `boolean` (`true` = executed, `false` = internally cancelled). The boolean enables mana refund and conditional cooldown in callers
- **No roadmap or LLD phase references in Javadoc or comments** — labels like "Phase 1", "Phase 2 LLD", or "Future phases" rot immediately: they are meaningless to engineers who weren't present during planning. Describe the *what* and *why* of the code instead. For extension points, name the type or mechanism (e.g. `ContentExpansion`, `QuestTemplate`). For deprecated code, explain what changed architecturally rather than which delivery phase removed it. The only acceptable "phase" language is inside inline comments that label the sequential steps of a single async operation (e.g. `// Phase 1 (DB executor): load`, `// Phase 2 (main thread): generate`).

---

## Coding Standards

### Code Style

- 4-space indentation, K&R brace style (standard Java)
- Meaningful variable names — avoid single-letter names except loop counters
- Prefer `var` for local variables when the declared type is long/nested and would be more distracting than helpful; otherwise prefer explicit types
- Keep methods focused and short — split logic into private helpers rather than long method bodies
- Prefer instance collaborators over static helpers when encoding domain behavior
- No section-divider comments (`// --- Section name ---` or `// ===== Section =====`) — if a class needs labeled sections, it is too large or has too many concerns. Extract a collaborator or use natural method ordering instead
- Javadoc on all methods (public and private) with `@param` and `@return` semantics

**Third-party developer mindset:** McRPG is designed to be extensible by external plugins. Any change to a public API, event, or registry should be made as if you were a third-party developer hooking in. Prefer additive, non-breaking changes; fire Bukkit events wherever an external plugin would reasonably want to intercept; document extension points clearly.

### Commit Messages

- Imperative mood, sentence case: `"Add mass harvest block type validation"`
- Reference the GitHub issue or PR in parentheses when applicable: `"Fix bleed DOT threshold (#145)"`
- Keep subject line under 72 characters

### Pull Requests

- One logical change per PR — don't bundle unrelated fixes
- PR title mirrors the commit message style
- All new abilities/skills must include corresponding config entries in the same PR
- New non-Bukkit logic must have unit test coverage before the PR is raised
- Manual test on a running Paper server before marking ready for review

### Testing

- New utility classes and non-Bukkit logic belong in `src/test/java/` (mirrors main package structure)
- Extend `McRPGBaseTest` for any test that requires Bukkit or MockBukkit setup
- Shared test helpers and fixtures go in `src/testFixtures/java/`
- **The entire test suite must pass before a task is considered complete** — run `./gradlew verifiedShadowJar` (or `./gradlew test`) and verify zero failures across all test classes, not just tests related to the current change. Regressions in unrelated tests still block completion.

#### Test Naming Conventions

- **`@DisplayName` format:** Short descriptive label — not a Given/When/Then sentence. Examples: `"getBaseValue returns constructor value"`, `"DISABLED cycles to ENABLED"`, `"fromString is case-insensitive"`
- **Method naming:** `action_outcome_whenCondition` — the `_whenCondition` suffix is optional when the context is obvious. Examples: `getNextSetting_disabled_cyclesToEnabled`, `getBaseValue_returnsDefault`, `fromString_unknownValue_returnsEmpty`
- **`@Nested` classes:** Use `@Nested` with `@DisplayName` to group tests by class-under-test or logical section (e.g., `@DisplayName("FlatPlayerStat")`)
- **Parameterized tests:** Prefer `@ParameterizedTest` with `@EnumSource` over manual loops when testing all enum variants

---

## Key Utilities

- `McRPGMethods.getMcRPGNamespace()` — returns the `"mcrpg"` namespace string for `NamespacedKey` construction
- `McRPGMethods.getMiniMessage()` — MiniMessage instance for component parsing
- `McRPG.getInstance()` — static plugin singleton (prefer injected instance where possible)
- `McRPG.getInstance().registryAccess()` — entry point for all registries and managers
- `PlayerStatRegistry` — accessed via `registryAccess().registry(McRPGRegistryKey.PLAYER_STAT)` — stat definitions
- `PlayerStatData` — per-player stat instances, accessed via `McRPGPlayer`
- `ComboManager` — accessed via `registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.COMBO)` — combo input handling

---

## Soft Dependencies (optional integrations)

WorldGuard, Geyser (Bedrock), LunarClient (Apollo), LandsAPI, PlaceholderAPI (PAPI), mcMMO (jar in `libs/`)

---

## Localization System

All player-facing text in McRPG is routed through `McRPGLocalizationManager`, which extends McCore's `LocalizationManager`. **Never send MiniMessage strings directly to a player** — always resolve through the manager so the player's locale is respected.

### Locale Chain

When resolving a message for a player, the manager walks a chain until it finds a translation:

1. Player's chosen `LocaleSetting` (e.g. `fr`)
2. Player's client-reported locale
3. Server's configured default locale
4. `Locale.ENGLISH` (guaranteed fallback — always covered by `BundledLocale.ENGLISH`)

If the entire chain is exhausted without a match, `NoLocalizationContainsMessageException` is thrown.

### Locale Source Types

| Type | When to use |
|------|-------------|
| `BundledLocale` | Locale files shipped inside the McRPG JAR (English is the only current bundled locale). Each entry names a folder and one or more `.yml` files. |
| `DynamicLocale` | Locale files placed by server owners or third-party plugins at runtime in `plugins/McRPG/localization/<language>/`. Discovered automatically at startup. |

Third-party plugins add custom locale files via:
```java
mcRPGLocalizationManager.registerLanguageFile(myMcRPGLocalization);
```

### Adding a New Locale Key

1. Define a `public static final Route` constant in `LocalizationKey` under the appropriate section header constant:
```java
private static final String MY_SECTION_HEADER = toRoutePath(PARENT_HEADER, "my-section");
public static final Route MY_NEW_KEY = Route.fromString(toRoutePath(MY_SECTION_HEADER, "my-key"));
```
2. Add the corresponding entry to every bundled locale YAML (`en.yml`, `en_gui.yml`, etc. — whichever file owns that section).
3. **Always add new keys in the same PR as the feature that uses them.**

### Sending a Message

```java
// Resolve a plain string
String msg = plugin.registryAccess()
    .registry(RegistryKey.MANAGER)
    .manager(McRPGManagerKey.LOCALIZATION)
    .getLocalizedMessage(mcRPGPlayer, LocalizationKey.MY_NEW_KEY);

// Resolve as a MiniMessage Component (preferred for display)
Component component = plugin.registryAccess()
    .registry(RegistryKey.MANAGER)
    .manager(McRPGManagerKey.LOCALIZATION)
    .getLocalizedMessageAsComponent(mcRPGPlayer, LocalizationKey.MY_NEW_KEY, Map.of("placeholder", value));

// Resolve without a player (uses server default locale)
String serverMsg = plugin.registryAccess()
    .registry(RegistryKey.MANAGER)
    .manager(McRPGManagerKey.LOCALIZATION)
    .getLocalizedMessage(LocalizationKey.MY_NEW_KEY);
```

### Player-facing numbers

Numeric values shown in localized strings, GUI placeholders, ability lore, PAPI output, and similar surfaces should use `McRPGDisplayDecimalFormatter.formatDisplayDecimal(...)` (overloads for `McRPGPlayer`, `Audience`, or an explicit `Locale`, plus `float` variants) **instead of** `Float.toString` / `Double.toString`, which ignore locale conventions and often expose unwanted floating-point precision. Obtain the formatter via `localizationManager.getDisplayDecimalFormatter()`.

**Translations vs numeric typography:** Resolving a message key walks the **full** [Locale Chain](#locale-chain) until a translation is found. `NumberFormat` is independent of that walk: overloads that take a `McRPGPlayer` use **only the first locale in that player's chain** as the format locale (grouping and decimal separators). `formatDisplayDecimal(Audience, ...)` uses the chain head of a **loaded** `McRPGPlayer` when the audience is that `Player`; otherwise it uses the **first locale of the server default chain** (console and non-player audiences). Overloads that take `Locale` use that locale directly. Locale resolution is internal to `McRPGDisplayDecimalFormatter`; it reads the locale chain head via `McRPGLocalizationManager.getLocaleChain(McRPGPlayer)` and falls back to the server default locale for non-player audiences.

**Caching and thread-safety:** `McRPGDisplayDecimalFormatter` stores **one** `NumberFormat` instance per `Locale` in a concurrent map. `NumberFormat` is mutable and not thread-safe, so each call **sets** minimum and maximum fraction digits on that shared instance and invokes `format` **while synchronized on that same instance** — there is **no** separate cache entry per digit tuple. Overloads without digit parameters use 1 minimum and 2 maximum fraction digits; overloads with two `int` parameters set custom bounds (non-negative, minimum ≤ maximum).

### Key Files

| File | Purpose |
|------|---------|
| `McRPGLocalizationManager.java` | Manager implementation; locale chain logic; exposes `getDisplayDecimalFormatter()` |
| `McRPGDisplayDecimalFormatter.java` | Locale-aware `NumberFormat` cache and all `formatDisplayDecimal` overloads (`Locale`, `McRPGPlayer`, `Audience`, `float`/`double`) |
| `McRPGLocalization.java` | Interface for a locale source (implement to add a new locale) |
| `BundledLocale.java` | Enum of JAR-bundled locales (folder name + file list) |
| `DynamicLocale.java` | Runtime-discovered locale from the data folder |
| `LocalizationKey.java` | All `Route` constants for locale keys — the canonical index |
| `src/main/resources/localization/english/` | Bundled English locale YAML files |
| `PALETTE.md` | Canonical color palette — all GUI hex codes, roles, and usage rules |

### GUI Color Palette

All player-facing colors follow the **Warm Fantasy RPG** palette defined in [`PALETTE.md`](PALETTE.md). Colors are **runtime-resolvable placeholders** — locale YAML files use semantic names like `<primary>`, and `McRPGLocalizationManager` replaces them with configured MiniMessage values before parsing. Server owners customize colors in `config.yml`'s `palette` section. The palette defines 11 semantic roles: `<gui-title>` (GUI inventory titles), `<primary>` (nav, values, section headers), `<hint>` (click hints — verb only), `<mana>` (mana costs), `<ability-active>`/`<ability-passive>`/`<ability-innate>` (ability type colors), `<body>` (lore text), `<positive>`/`<negative>`/`<warning>` (status). `<gold>` is deprecated — use `<primary>` instead. `<primary>` for titles is deprecated — use `<gui-title>` instead. See `PALETTE.md` for the full specification and `.cursor/rules/core.mdc` for enforcement rules.

**Click-hint format:** All click instructions in GUI lore use the verb-only `<hint>` format: `<hint>Left-click <body>to edit` — `<hint>` colors only the click-type verb, `<body>` covers the rest. Compound click types are hyphenated (`Left-click`, `Right-click`, `Shift-click`). Destructive actions use `<negative>` on the verb instead; acceptance/positive actions use `<positive>`. Never color the entire hint line a single tag.

---

## Quest Board System

The quest board is a daily/weekly rotating board of quest offerings that players can accept from a GUI. It supports both hand-crafted quest definitions and procedurally generated quests via a template engine.

### Key Concepts

| Term | Meaning |
|------|---------|
| **QuestBoard** | Top-level container holding rotations, offerings, and board config. Default board key is `mcrpg:default`. |
| **BoardOffering** | A single quest slot on the board (shared or personal). Tracks state: `AVAILABLE` → `ACCEPTED` → `COMPLETED`/`EXPIRED`. |
| **BoardRotation** | A time-windowed rotation (daily or weekly). Offerings belong to a rotation and expire with it. |
| **QuestRarity** | Rarity tier assigned to offerings (e.g., COMMON, RARE, LEGENDARY). Affects template scaling and visual display. |
| **BoardSlotCategory** | Configurable category defining slot counts, visibility (SHARED/PERSONAL), and refresh type. |
| **QuestTemplate** | Declarative YAML blueprint for procedurally generating quest definitions. |
| **QuestTemplateEngine** | Stateless engine that resolves variables, evaluates conditions, and builds a `QuestDefinition` from a template. |
| **TemplateCondition** | Extensible condition interface for gating template elements (phases/stages/objectives) during generation. |
| **QuestRewardEntry** | Wrapper around `QuestRewardType` that optionally carries a `RewardFallback` for conditional reward substitution. |
| **RewardDistributionConfig** | Configuration for splitting rewards among contributors in scoped quests. |
| **ScopedBoardAdapter** | Plugin-provided adapter enabling group-based board offerings (e.g., Lands). |

### Package Structure

```
quest/board/
├── QuestBoardManager.java          # Central manager: rotations, offerings, generation
├── QuestBoard.java                 # Board state container
├── BoardOffering.java              # Offering record with state transitions
├── BoardRotation.java              # Rotation window
├── BoardMetadata.java              # Board-specific metadata on QuestDefinition
├── category/                       # Slot category config and registry
├── rarity/                         # QuestRarity, QuestRarityRegistry
├── generation/                     # PersonalOfferingGenerator, QuestPool, SlotGenerationLogic
├── template/                       # QuestTemplate, QuestTemplateEngine, serialization
│   ├── condition/                  # TemplateCondition, ConditionContext, built-in conditions
│   └── variable/                   # PoolVariable, RangeVariable, ResolvedVariableContext
├── distribution/                   # RewardDistributionConfig, resolver, granter, types
└── refresh/                        # DailyRefreshType, WeeklyRefreshType
```

### Template Generation Pipeline

1. `QuestTemplateEngine.generate(template, rarityKey, random)` validates rarity support
2. Variables resolved (pools first, then ranges scaled by difficulty)
3. `ConditionContext` built (shared or personal depending on context)
4. Phases/stages/objectives filtered by conditions; weighted selection applied
5. `QuestDefinition` built with resolved objectives, rewards, and metadata
6. Serialized to JSON via `GeneratedQuestDefinitionSerializer` for persistence
7. Returns `GeneratedQuestResult` (definition + template key + JSON)

### Extensibility Points

- **Custom quest definitions**: Register via `QuestContentPack` in a `ContentExpansion`
- **Custom objective types**: Register via `QuestObjectiveTypeContentPack`
- **Custom reward types**: Register via `QuestRewardTypeContentPack`
- **Custom scope providers**: Register via `QuestScopeProviderContentPack`
- **Custom quest sources**: Register via `QuestSourceContentPack`
- **Custom rarities**: Register via `QuestRarityContentPack`
- **Custom distribution types**: Register via `RewardDistributionTypeContentPack`
- **Custom conditions**: Register via `TemplateConditionContentPack`
- **Custom scope adapters**: Register via `ScopedBoardAdapterContentPack`
- **Custom templates**: Register via `QuestTemplateContentPack` or `QuestTemplateRegistry.registerTemplateDirectory()`
- **Board events**: `BoardRotationEvent`, `BoardOfferingAcceptEvent`, `PersonalOfferingGenerateEvent`, `TemplateQuestGenerateEvent` (cancellable)

### Key Files

| File | Purpose |
|------|---------|
| `QuestBoardManager.java` | Rotation scheduling, offering generation, acceptance, state management |
| `QuestTemplateEngine.java` | Template → QuestDefinition generation |
| `GeneratedQuestDefinitionSerializer.java` | JSON round-trip for generated definitions |
| `ConditionContext.java` | Unified context for all condition evaluation sites |
| `QuestRewardEntry.java` | Reward wrapper with optional fallback |
| `DistributionRewardEntry.java` | Distribution tier reward with pot behavior |
| `QuestRewardDistributionResolver.java` | Pure stateless distribution math |
| `BoardConfigFile.java` | Route constants for `board.yml` |
| `quest/REWARDS.md` | Developer guide for reward types and configuration |

---

## Keeping This File Current

After any commit or PR that introduces one of the following, **update `CLAUDE.md` and the relevant `.cursor/rules/*.mdc` files** before or alongside the change:

| Change type | What to update |
|-------------|----------------|
| New architectural pattern established | `CLAUDE.md` Architecture Overview + relevant `.mdc` |
| New domain term introduced | `CLAUDE.md` Domain Terminology table |
| New naming convention | `CLAUDE.md` Naming Conventions table + `core.mdc` |
| New anti-pattern discovered | `CLAUDE.md` Anti-Patterns to Avoid + `core.mdc` |
| Build command changes | `CLAUDE.md` Build & Run table + `core.mdc` |
| New McCore abstraction used | `CLAUDE.md` McCore Relationship section |
| New coding standard adopted | `CLAUDE.md` Coding Standards section |
| New ability/skill type interface added | `CLAUDE.md` + `ability-system.mdc` or `skill-system.mdc` |
| Entity hierarchy changed (new holder type or composition) | `CLAUDE.md` Architecture Overview + `entity-system.mdc` |
| Localization system changed (new source type, chain order) | `CLAUDE.md` Localization System section |
| New locale key section added | `LocalizationKey.java` + bundled locale YAMLs |
| New GUI slot pattern or anti-pattern found | `persona-gui-ux.mdc` + `.claude/commands/review-gui-ux.md` |
| New server owner config concern identified | `persona-server-owner.mdc` + `.claude/commands/review-server-owner.md` |
| New public API pattern or breaking-change rule | `persona-extensibility.mdc` + `.claude/commands/review-extensibility.md` |
| New test structural pattern or anti-pattern | `persona-testing.mdc` + `.claude/commands/review-testing.md` |
| New structural design anti-pattern found (SRP, coupling, wrong layer) | `persona-architecture.mdc` + `.claude/commands/review-architecture.md` |
| New error handling anti-pattern found (swallowed exception, bad logging) | `persona-error-handling.mdc` + `.claude/commands/review-error-handling.md` + `core.mdc` |
| New performance anti-pattern found (hot path, unbounded collection, leak) | `persona-performance.mdc` + `.claude/commands/review-performance.md` |
| New concurrency anti-pattern found (thread boundary, race, future handling) | `persona-concurrency.mdc` + `.claude/commands/review-concurrency.md` + `core.mdc` |
| CI review file-pattern for a new domain | `.github/workflows/pr-review.yml` detect-changes step |
| Quest board system changed (new condition, distribution type, template feature) | `CLAUDE.md` Quest Board System section + `quest-board-system.mdc` |
| Mana balance parameters changed (pool size, regen rate, bucket ranges) | `CLAUDE.md` Mana Balance Philosophy section + `mana-balance-philosophy.mdc` + `core.mdc` |
| GUI color palette changed (new role, hex value, usage rule) | `PALETTE.md` + `core.mdc` GUI Color Palette section + `docs/hld/gui-ux-system.md` |

These files are the project's living technical contract — stale steering files produce stale AI output.
