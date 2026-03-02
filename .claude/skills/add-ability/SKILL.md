---
name: add-ability
description: Scaffold a new McRPG ability. Use when asked to create a new ability from scratch.
---

Create a new ability for the skill specified in $ARGUMENTS, following the McRPG ability pattern.

## Steps

1. **Create the ability class** in `src/main/java/us/eunoians/mcrpg/ability/impl/<skill>/`
   - Extend `McRPGAbility`
   - Implement the appropriate capability interface: `PassiveAbility` or `ActiveAbility`, plus `ConfigurableSkillAbility`
   - Declare `public static final NamespacedKey <ABILITY_NAME>_KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "<ability_name>");`
   - Register components in the constructor with `addActivatableComponent(component, EventClass.class, priority)`
   - Implement required methods: `activateAbility()`, `getSkillKey()`, `getDatabaseName()`, `getYamlDocument()`, `getAbilityEnabledRoute()`, `getDisplayItemRoute()`
   - Inside `activateAbility()`: fire the custom event, check `isCancelled()`, apply effect, call `abilityHolder.addCooldown(this, seconds)`

2. **Create the custom event** in `src/main/java/us/eunoians/mcrpg/event/ability/<skill>/`
   - Name it `<AbilityName>ActivateEvent`
   - Extend the appropriate base event class
   - Reference `BleedActivateEvent` as a canonical example

3. **Add config routes** to the relevant `*ConfigFile` class in `src/main/java/us/eunoians/mcrpg/configuration/file/skill/`
   - Add `static final Route` constants for all tunable values (cooldown, chance, damage, etc.)
   - Never hardcode numeric values in the ability class

4. **Register the ability** in `McRPGExpansion.getAbilityContent()`:
   ```java
   abilityContent.addContent(new MyAbility(mcRPG));
   ```

5. **Verify compilation**: `./gradlew compileJava`

## Reference Examples

- `Bleed.java` — canonical passive ability (simplest complete example)
- `ExtraOre.java` — mining passive with `ReloadableSet` for block type config
- `OreScanner.java` — active ability with ready state

## Checklist Before Finishing

- [ ] No state stored on the ability object (per-holder state goes in `AbilityData` via attributes)
- [ ] No hardcoded numeric values — all tunable values come from config via `Route`
- [ ] No reflection — attributes created via factory
- [ ] `@NotNull` on all non-null parameters and return types
- [ ] `@Override` on all overridden methods
- [ ] Custom event fired before applying effects
- [ ] Cooldown applied after activation
- [ ] Config routes defined as `static final` constants in the `*ConfigFile` class
- [ ] Ability registered in `McRPGExpansion`
