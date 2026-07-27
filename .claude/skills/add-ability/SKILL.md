---
name: add-ability
description: Scaffold a new McRPG ability end-to-end — a passive (event-driven) or combo-activated active ability — including components, activate event, config, mana balance, localization, statistics, and registration. Use when creating a new ability or wiring one into a skill.
---

# Add Ability

Scaffold a new McRPG ability. Follow every step in order — do not skip steps.

Two reference files accompany this skill:
- `references/ability-patterns.md` — full code templates (passive + combo-active), component types, cooldowns, attributes, listener authoring
- `references/mana-balance-philosophy.md` — the balance framework for mana costs and cooldowns, including the agent workflow for presenting options

## Step 0 — Gather inputs

Ask (or infer from context) before writing any code:

1. **Ability name** — PascalCase class name (e.g. `Bleed`)
2. **Owning skill** — one of the existing skills: **Swords, Mining, Herbalism, Woodcutting** (determines the package `ability/impl/<skill>/` and the config file)
3. **Type** — **passive** (auto-fires on a Bukkit event) or **active** (player-triggered). All active abilities activate exclusively via click-combo sequences (RRR/RRL/RLR) gated by mana — implement `ComboActivatable`.
4. **Trigger event** (passive only) — the Bukkit event class (e.g. `EntityDamageByEntityEvent`, `BlockBreakEvent`)
5. **Tierable?** — does the ability upgrade through tiers? Tierable abilities need tier configuration, `@ParserConfigKeys`, and a `ParserConfigCoverageTest` entry.
6. **Namespace key** — snake_case identifier (defaults to the lower-case ability name)

## Step 1 — Read the canonical examples

- Passive: `src/main/java/us/eunoians/mcrpg/ability/impl/swords/Bleed.java` (components, config routes, activate event)
- Active (combo): any `ComboActivatable` implementation in `ability/impl/` (e.g. OreScanner or MassHarvest)
- Components: the owning skill's `<Skill>Components` class

## Step 2 — Create the ability class

Create `src/main/java/us/eunoians/mcrpg/ability/impl/<skill>/<Name>.java`:

- Extend `McRPGAbility`; implement the relevant type interfaces (`PassiveAbility`, `ConfigurableActiveAbility` + `ComboActivatable`, `ConfigurableSkillAbility`, `CooldownableAbility`, etc.)
- Declare `public static final NamespacedKey <NAME>_KEY` on the class
- Register components in the constructor (passive only — combo abilities need no activation components)
- Implement `activateAbility()` / `comboActivate()`, `getSkillKey()`, `getDatabaseName()`, `getYamlDocument()`, `getAbilityEnabledRoute()`, `getDisplayItemRoute()`

Use the templates in `references/ability-patterns.md`. Key rules:
- `comboActivate()` and `activateAbility()` return `boolean` — `false` means internally cancelled (mana refunded, no cooldown)
- Never call `putHolderOnCooldown()` inside `comboActivate()` — the combo listener applies cooldowns
- Ability objects are shared singletons: per-holder state lives in `AbilityData`/`AbilityAttribute`, never on the ability

## Step 3 — Components (passive only)

Add reusable checks as `EventActivatableComponent` / `EventCancellingComponent` constants in the skill's `<Skill>Components` class (or a new `<Name>Components` class for ability-specific checks), then register them in the constructor with explicit priorities. Priority 0 runs first; the first failing component stops the chain.

## Step 4 — Create the activate event

Create `src/main/java/us/eunoians/mcrpg/event/ability/<skill>/<Name>ActivateEvent.java` (note the per-skill subpackage). Fire it at the top of `activateAbility()`/`comboActivate()` and honor `isCancelled()` before applying the effect — third-party plugins intercept here.

## Step 5 — Config entries

Add the ability's section to `src/main/resources/skill_configuration/<skill>_configuration.yml` and route constants to the skill's `ConfigFile` class (`configuration/file/skill/<Skill>ConfigFile.java`):

- `enabled`, unlock level, and any effect tunables (never hard-code behavior values)
- Tierable abilities: `tier-configuration` with `all-tiers` Parser formulas
- Active abilities: `mana-cost` and `cooldown` under `tier-configuration.all-tiers`

**Balance (active abilities):** do NOT pick mana cost or cooldown values autonomously. Follow the Agent Workflow in `references/mana-balance-philosophy.md`: classify the ability into a bucket (Light/Medium/Heavy), compute 2-3 concrete options with their gameplay implications, and present them to the user before finalizing. Add the balance comment block above `mana-cost` per the YAML documentation convention.

## Step 6 — Parser config coverage (tierable only)

- Annotate the ability class with `@ParserConfigKeys` declaring every Parser-backed tier-config YAML key (an empty array if none) — `ParserConfigKeysPresenceTest` fails CI without it
- Add the ability's entry to `ParserConfigCoverageTest`'s registry so the declared keys are validated against the bundled YAML defaults

## Step 7 — Localization

1. Add a display-item route in `configuration/file/localization/LocalizationKey.java` under the ability section header
2. Add the locale entry to the bundled English YAML (`src/main/resources/localization/english/`), using the correct semantic color placeholder for the `name:` field — never `<red>`, `<gold>`, or raw hex:

| Ability type | Placeholder |
|---|---|
| `ComboActivatable` (active) | `<ability-active>` |
| Tierable/upgradable passive (`ABILITY_UNLOCKED_ATTRIBUTE`) | `<ability-passive>` |
| Always-on innate passive (no unlock gate) | `<ability-innate>` |

`AbilityNameColorConsistencyTest` enforces this at CI time.

## Step 8 — Listener wiring (passive only)

If the owning skill has no listener for the trigger event yet, add one under `listener/ability/<skill>/` implementing `AbilityListener` and call `activateAbilities(uuid, event)` — see the listener template in `references/ability-patterns.md`. Handlers use `@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)`.

## Step 9 — Register in McRPGExpansion

```java
// In McRPGExpansion.getAbilityContent():
abilityContent.addContent(new <Name>(mcRPG));
```

Statistics are automatic: `ActiveAbility.getDefaultStatistics()` generates the activation-count statistic. Passive abilities return an empty list by default — override only for custom statistics.

## Step 10 — Verify

```
./gradlew verifiedShadowJar
```

The entire suite must pass — including `ParserConfigKeysPresenceTest`, `ParserConfigCoverageTest`, and `AbilityNameColorConsistencyTest`.

## Checklist

- [ ] Ability class in `ability/impl/<skill>/` with `NamespacedKey` constant and correct type interfaces
- [ ] Components registered with explicit priorities (passive) / no cooldown call inside `comboActivate()` (active)
- [ ] `<Name>ActivateEvent` in `event/ability/<skill>/`, fired and cancellation-checked
- [ ] Config section + route constants added; no hard-coded tunables
- [ ] Mana cost/cooldown chosen by the user from presented options (active)
- [ ] `@ParserConfigKeys` + `ParserConfigCoverageTest` entry (tierable)
- [ ] Locale entry with the correct `<ability-*>` placeholder
- [ ] Registered in `McRPGExpansion.getAbilityContent()`
- [ ] `./gradlew verifiedShadowJar` passes
