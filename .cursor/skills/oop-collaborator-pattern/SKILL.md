---
name: oop-collaborator-pattern
description: Prefer object collaborators over static utility helpers for domain behavior in McRPG.
---

## Use this skill when

- You are about to add a new helper class for gameplay/domain logic.
- You are extracting duplicate logic across slots, listeners, or managers.
- You notice a proposed `final class` with only `static` methods in non-DAO code.

## Core rule

If behavior needs runtime context (player, manager, offering, scope, config), model it as an object collaborator with instance methods and explicit dependencies.

## Preferred patterns

1. Create a small collaborator class with constructor-injected dependencies.
2. Keep the collaborator package-local unless a wider API is required.
3. Call the collaborator from owning objects (`Slot`, `Listener`, `Manager`) instead of static helpers.
4. Add/adjust focused unit tests around collaborator behavior.

## Avoid

- Static utility classes for domain logic.
- "God utility" files mixing unrelated concerns.
- Hiding dependency lookup inside static methods (calling `SomeClass.getInstance()` or `McRPG.getInstance()` inside a static method IS a hidden dependency).
- `getInstance()` singletons for per-player or per-system state. Per-player state belongs on `McRPGPlayer`; per-system state should be a `Manager` in the registry.

## Anti-pattern: per-class private static helpers

A common slip is writing a helper inside an already-stateful class and marking it `static` just because it happens not to read `this`:

**Before (smell):**
```java
public class ExperienceRewardType implements QuestRewardType {
    private final String skillName;
    private final long amount;

    // Static despite the class having instance state it logically belongs to.
    // Becomes a hidden dependency once it needs a registry or manager.
    private static String formatSkillName(@NotNull String raw) { ... }
    private static String applyVars(@NotNull String template, @NotNull Map<String, String> vars) { ... }
}
```

**After (correct):**
```java
public class ExperienceRewardType implements QuestRewardType {
    private final String skillName;
    private final long amount;

    // Instance method — honest about its relationship to the class.
    private String formatSkillName(@NotNull String raw) { ... }

    // Cross-class plumbing belongs on the manager, not duplicated per type.
    // localization.getLocalizedMessage(displayLabel, vars) replaces applyVars().
}
```

Key signals that a `static` helper should be an instance method (or pushed to a manager):
- The helper is `private` and lives inside a class that already has instance fields.
- Removing `static` compiles without any other change — the helper was never truly stateless in context.
- The same helper (or near-identical copy) exists in more than one class.

Key signal that a helper should move to a manager/service instead:
- The same logic is duplicated across multiple unrelated classes (e.g. `applyVars` in four reward types → `McRPGLocalizationManager.getLocalizedMessage(String, Map)`).

## Allowed exceptions

- DAO operations that are intentionally static by project convention.
- Pure constants or tiny stateless value-format helpers where **no domain dependency exists and the logic could plausibly live on any class without changing meaning** (e.g. a string-to-title-case utility on `McRPGMethods`). The moment the helper is private to a class with instance state, or appears in more than one class, it no longer qualifies.

## Concrete example: ComboDisplayManager refactor

**Before (anti-pattern):**
```java
// Static utility with hidden singleton dependency
public final class ComboDisplayManager {
    private ComboDisplayManager() {}
    public static void updateDisplay(Player player, List<ComboInput> sequence) {
        Component display = buildDisplay(sequence, totalLength);
        ActionBarCenterContent.getInstance().setPersistent(player.getUniqueId(), display);
    }
}

// Singleton holding per-player state in a global Map
public final class ActionBarCenterContent {
    private static final ActionBarCenterContent INSTANCE = new ActionBarCenterContent();
    private final Map<UUID, Entry> entries = new ConcurrentHashMap<>();
    public static ActionBarCenterContent getInstance() { return INSTANCE; }
}
```

**After (collaborator pattern):**
```java
// Manager registered via McRPGManagerKey.COMBO — discoverable, injectable, testable
public class ComboManager extends Manager<McRPG> {
    public void processInput(Player player, ComboInput input) { ... }
    private void updateDisplay(McRPGPlayer player, List<ComboInput> sequence) {
        ActionBarHudDisplay hud = displayManager.getOrCreateDisplay(
                player,
                ActionBarHudDisplay.class,
                p -> new ActionBarHudDisplay(p, displayManager.getHudRenderer()));
        hud.setSlot(CenterContentPriority.COMBO_STATE,
                new IndefiniteCenterContent(buildComboDisplay(sequence, totalLength)));
    }
}

// Per-player state lives on the player object — no global map, no cleanup needed
public class McRPGPlayer extends CorePlayer {
    public <T extends PlayerDisplay> Optional<T> getDisplay(Class<T> type) { ... }
    public <T extends PlayerDisplay> void setDisplay(Class<T> type, T display) { ... }
}
```

Key improvements: dependencies are explicit, per-player state lifecycle is automatic, third-party plugins can access `ComboManager` via `registryAccess()`, and unit tests pass collaborators directly instead of setting up global singletons.

## Quick checklist

- Does this logic require domain context? -> use object collaborator.
- Are dependencies explicit in constructor or method signature? -> yes.
- Can this be tested directly with mocks/fakes? -> yes.
- Is a static helper still necessary after design review? -> usually no.
- Does a static method call `getInstance()` or `McRPG.getInstance()`? -> it has a hidden dependency; refactor to collaborator.
