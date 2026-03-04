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
| `./gradlew test` | Run tests only |
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

---

## Project Structure

```
src/main/java/us/eunoians/mcrpg/
├── McRPG.java                      # Plugin main class (extends CorePlugin)
├── ability/
│   ├── Ability.java                # Core ability interface
│   ├── BaseAbility.java            # Component registration logic
│   ├── AbilityData.java            # Per-holder attribute container (DTO)
│   ├── AbilityRegistry.java        # Registry of all registered abilities
│   ├── attribute/                  # Typed ability attribute definitions
│   ├── component/                  # Reusable activation/cancel/ready components
│   │   ├── activatable/            # EventActivatableComponent implementations
│   │   ├── cancel/                 # EventCancellingComponent implementations
│   │   └── readyable/              # EventReadyableComponent implementations
│   ├── impl/                       # Concrete ability implementations by skill
│   │   ├── swords/                 # Bleed, DeeperWound, Vampire, etc.
│   │   ├── mining/                 # ExtraOre, ItsATriple, OreScanner, etc.
│   │   ├── herbalism/              # MassHarvest, InstantIrrigation, etc.
│   │   └── woodcutting/            # ExtraLumber, HeavySwing, etc.
│   └── impl/type/                  # Ability capability interfaces (ActiveAbility, PassiveAbility, etc.)
├── skill/
│   ├── Skill.java                  # Core skill interface
│   ├── impl/                       # Concrete skill implementations (Swords, Mining, etc.)
│   └── impl/type/                  # Skill capability interfaces (ConfigurableSkill, HeldItemBonusSkill)
├── entity/
│   ├── holder/
│   │   ├── AbilityHolder.java      # Base: entity with abilities
│   │   ├── LoadoutHolder.java      # Restricts to loadout abilities
│   │   └── SkillHolder.java        # AbilityHolder with levelable skills
│   └── player/
│       └── McRPGPlayer.java        # Concrete player (extends CorePlayer)
├── expansion/
│   ├── ContentExpansion.java       # Base class for content modules
│   └── McRPGExpansion.java         # Native content registration (all built-in abilities/skills)
├── listener/ability/
│   ├── AbilityListener.java        # Interface with activateAbilities() / readyAbilities() defaults
│   └── <skill>/                    # Per-skill Bukkit event listeners
├── event/ability/                  # Custom McRPG Bukkit events (one per ability activation)
├── database/table/                 # DAO classes (static JDBC methods)
├── configuration/
│   ├── FileType.java               # Enum of all config file types
│   └── file/                       # YAML config file wrappers (one per skill/system)
├── registry/                       # McRPGRegistryKey, McRPGManagerKey, ability/skill registries
└── util/
    └── McRPGMethods.java           # Namespace, MiniMessage, PAPI utilities
```

---

## Domain Terminology

| Term | Meaning |
|------|---------|
| **Ability** | An action or passive effect an entity can use. Can be active (player-triggered) or passive (auto-fires on event). |
| **Skill** | A leveling system (e.g., Swords, Mining). Leveling a skill unlocks and scales its abilities. |
| **AbilityHolder** | McRPG's wrapper around any entity that can hold/use abilities. Non-player entities use this base type. |
| **LoadoutHolder** | An AbilityHolder restricted to only the abilities in their active loadout. Players are always loadout holders. |
| **SkillHolder** | An AbilityHolder that also has levelable skills. Players are SkillHolders. |
| **McRPGPlayer** | Concrete player object — implements SkillHolder, LoadoutHolder, and McCore's CorePlayer. |
| **Tier** | Enhancement level of an ability. Higher tiers change ability mechanics (not just stat scaling). |
| **Ready State** | A "charged" intermediate state some abilities enter before activating (e.g., right-click to ready, then attack to fire). ReadyData is shared across all abilities that use the same tool to ready, auto-expires after ~3 seconds. |
| **Cooldown** | Time lock applied to an ability after it activates. Managed via AbilityHolder's cooldown tracking. |
| **Component** | A modular, reusable piece of activation/cancel/ready logic registered on an ability. Components are priority-ordered; first failing component stops the chain. |
| **Attribute** | A typed `AbilityAttribute<T>` stored in `AbilityData` — contains per-holder ability state (tier, cooldown, toggle, etc.). Created via factory (no reflection). |
| **ContentExpansion** | A module that bundles skills, abilities, player settings, and localization into a single registration unit. |
| **DAO** | Data Access Object — static JDBC methods for reading/writing ability and skill data. |

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

1. Ability registered in `AbilityRegistry` via `McRPGExpansion.getAbilityContent()`
2. Added to holder's available abilities (`abilityHolder.addAvailableAbility(ability)`)
3. Player triggers a Bukkit event (e.g., `EntityDamageByEntityEvent`)
4. A skill listener implementing `AbilityListener` calls `activateAbilities(uuid, event)` or `readyAbilities(uuid, event)`
5. Components are checked in priority order — first failing component stops activation
6. Cooldown is validated (ability is skipped if on cooldown)
7. `ability.activateAbility(abilityHolder, event)` is called
8. Inside `activateAbility()`: fire the ability's custom event, check `isCancelled()`, perform effect, apply cooldown
9. For active-duration abilities: call `abilityHolder.addActiveAbility(ability, seconds)` for auto-cleanup

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

Three component types:
- `EventActivatableComponent` — must pass (`shouldActivate()`) for activation to proceed
- `EventReadyableComponent` — must pass (`shouldReady()`) for readying to proceed
- `EventCancellingComponent` — if `shouldCancel()` returns true, cancels the underlying Bukkit event

### Ready State Pattern

```java
// In constructor, register both a readying and an activating component
addReadyingComponent(MyComponents.READY_COMPONENT, PlayerInteractEvent.class, 0);
addActivatableComponent(MyComponents.ACTIVATE_COMPONENT, PlayerInteractEvent.class, 1);

// activateAbility() must clear the ready state:
abilityHolder.unreadyHolder();
```

Ready state auto-expires after ~3 seconds via a scheduled task in `AbilityHolder.readyAbility()`.

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

### Registering New Content

Add to `McRPGExpansion`:

```java
// In getAbilityContent()
abilityContent.addContent(new MyAbility(mcRPG));

// In getSkillContent()
skillContent.addContent(new MySkill(mcRPG));
```

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
| DTOs | `Data` suffix | `AbilityData`, `ReadyData`, `SkillHolderData` |
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
- **No direct entity casting without guard** — use `instanceof` pattern matching: `if (entity instanceof Player player) { ... }`
- **No ability state stored on the ability object** — ability state is per-holder, stored in `AbilityData`/`AbilityAttribute`; ability objects are shared singletons
- **Don't put McRPG-specific logic in McCore** — McCore changes affect all downstream plugins

---

## Coding Standards

### Code Style

- 4-space indentation, K&R brace style (standard Java)
- Meaningful variable names — avoid single-letter names except loop counters
- Prefer `var` for local variables when the declared type is long/nested and would be more distracting than helpful; otherwise prefer explicit types
- Keep methods focused and short — split logic into private helpers rather than long method bodies
- Javadoc on all public methods with `@param` and `@return` semantics

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

---

## Key Utilities

- `McRPGMethods.getMcRPGNamespace()` — returns the `"mcrpg"` namespace string for `NamespacedKey` construction
- `McRPGMethods.getMiniMessage()` — MiniMessage instance for component parsing
- `McRPG.getInstance()` — static plugin singleton (prefer injected instance where possible)
- `McRPG.getInstance().registryAccess()` — entry point for all registries and managers

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

### Key Files

| File | Purpose |
|------|---------|
| `McRPGLocalizationManager.java` | Manager implementation; locale chain logic |
| `McRPGLocalization.java` | Interface for a locale source (implement to add a new locale) |
| `BundledLocale.java` | Enum of JAR-bundled locales (folder name + file list) |
| `DynamicLocale.java` | Runtime-discovered locale from the data folder |
| `LocalizationKey.java` | All `Route` constants for locale keys — the canonical index |
| `src/main/resources/localization/english/` | Bundled English locale YAML files |

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
| CI review file-pattern for a new domain | `.github/workflows/pr-review.yml` detect-changes step |

These files are the project's living technical contract — stale steering files produce stale AI output.
