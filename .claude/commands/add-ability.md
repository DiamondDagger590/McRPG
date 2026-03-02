You are scaffolding a new ability for McRPG. Work through the checklist below step by step. Ask for any missing information before writing code, then produce all required files in the correct locations.

---

## Step 1 — Gather requirements

Ask the user for:
- **Ability name** (e.g. `DeepStrike`)
- **Skill** it belongs to (Swords / Mining / Herbalism / Woodcutting / Axes / Unarmed / Acrobatics / Repair / Fishing / Taming / Excavation)
- **Type**: passive (auto-fires on Bukkit event), active (player-triggered, no ready state), or active-with-ready (two-step: ready → activate)
- **Mechanics**: what it does, which Bukkit event(s) trigger it, configurable values (cooldown, chance, damage, etc.)
- **Tier count** (default: 4)

---

## Step 2 — Choose interfaces

Based on ability type, determine which interfaces the class will implement:

| Scenario | Interfaces |
|----------|-----------|
| Passive | `PassiveAbility` |
| Active, no ready | `ActiveAbility` |
| Active with ready | `ActiveAbility` (register both readying and activating components) |
| Has a cooldown | also `CooldownableAbility` |
| Reads from config | also `ConfigurableSkillAbility` |
| Config reloads at runtime | also `ReloadableContentAbility` |

---

## Step 3 — Read existing examples first

Before writing any code, read at least one existing ability in the same skill's package at
`src/main/java/us/eunoians/mcrpg/ability/impl/<skill>/` to confirm component patterns and naming in use.

---

## Step 4 — Create the ability class

**Location:** `src/main/java/us/eunoians/mcrpg/ability/impl/<skill>/<AbilityName>.java`

Requirements:
- Extend `McRPGAbility`
- `public static final NamespacedKey <NAME>_KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "<name>");`
- Constructor calls `super(mcRPG, <NAME>_KEY)` then registers components via `addActivatableComponent()` / `addReadyingComponent()` with ascending integer priorities
- `activateAbility(AbilityHolder holder, Event event)`:
  1. Fire `new <Name>ActivateEvent(holder, event)`
  2. Return if `isCancelled()`
  3. Apply the ability effect
  4. Apply cooldown if `CooldownableAbility`
- For active-with-ready abilities, call `abilityHolder.unreadyHolder()` inside `activateAbility()`
- All tunable values must be read from config via `Route` — no hard-coded numbers
- Annotate parameters with `@NotNull`; annotate `@Override` on all overridden methods

---

## Step 5 — Create the custom event

**Location:** `src/main/java/us/eunoians/mcrpg/event/ability/<AbilityName>ActivateEvent.java`

- Extend the appropriate base McRPG event class
- Store the `AbilityHolder` and the triggering Bukkit event as fields
- Follow existing event class conventions in that package

---

## Step 6 — Add config routes

**Location:** `src/main/java/us/eunoians/mcrpg/configuration/file/<Skill>ConfigFile.java`

Add `public static final Route` constants for every configurable value:

```java
public static final Route MY_ABILITY_COOLDOWN = Route.from("<ability-name>", "cooldown");
public static final Route MY_ABILITY_CHANCE   = Route.from("<ability-name>", "chance");
```

---

## Step 7 — Add YAML config entries

**Location:** `src/main/resources/config/<skill>.yml`

Add entries under the ability's key with sensible defaults that match the `Route` constants:

```yaml
<ability-name>:
  cooldown: 30
  chance: 0.15
```

---

## Step 8 — Register in McRPGExpansion

**Location:** `src/main/java/us/eunoians/mcrpg/expansion/McRPGExpansion.java`

In `getAbilityContent()`:

```java
abilityContent.addContent(new <AbilityName>(mcRPG));
```

---

## Step 9 — Wire the listener

**Location:** `src/main/java/us/eunoians/mcrpg/listener/ability/<skill>/`

- If a listener already handles the triggering event, add the ability's key to its activation call.
- If no listener exists yet, create one implementing `AbilityListener` with `@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)`.
- Call `activateAbilities(uuid, event)` (passive / active) or `readyAbilities(uuid, event)` (ready step).

---

## Step 10 — Write unit tests

**Location:** `src/test/java/us/eunoians/mcrpg/ability/impl/<skill>/<AbilityName>Test.java`

- Extend `McRPGBaseTest`
- Cover: component pass/fail conditions, cooldown application, any non-trivial logic
- Do not test Bukkit event dispatching directly — focus on pure-Java logic

---

## Step 11 — Final checklist

Run through this before declaring done:

- [ ] `NamespacedKey` constant defined on the ability class
- [ ] Ability class registered in `McRPGExpansion.getAbilityContent()`
- [ ] `Route` constants added to `<Skill>ConfigFile`
- [ ] YAML config entries added with defaults
- [ ] Custom activate event created
- [ ] Listener wired up
- [ ] Unit tests written
- [ ] `./gradlew test` passes with no failures
