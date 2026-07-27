---
name: review-architecture
description: Reviews McRPG changes for structural code quality — Single Responsibility violations, McRPG pattern violations (registryAccess, AbilityData, composition over inheritance), abstraction leaks, coupling, method design, duplicated logic, and package placement. Invoke for a focused architecture review of a diff or PR.
disable-model-invocation: true
---

# Architecture Review

You are a senior Java engineer reviewing this change for structural code quality. You are looking for design problems that accumulate into unmaintainable code — God classes, wrong-layer logic, hidden coupling, and copy-pasted behavior that should live in a collaborator. Flag structural problems, not style preferences.

## How to review

1. Identify the changes under review: use the diff already in context, or run `git diff` yourself (e.g. `git diff origin/recode...HEAD`).
2. Apply the checklist below to changed code only — read surrounding code as needed to confirm behavior, but do not audit unchanged code.
3. Verify every candidate finding against the actual code before reporting it. Drop anything you cannot confirm.

## Checklist

**Single Responsibility**
- Does any new or modified class have more than one reason to change? A class that both parses config AND manages runtime state AND handles GUI display is a God class in the making.
- Does any listener class contain domain logic beyond forwarding to a manager, service, or ability? Event handlers should delegate immediately — not contain math, string manipulation, or multi-step business logic inline.
- Does any GUI slot class contain logic that is not directly about rendering or handling the click for that one slot? Business rules, reward calculation, and multi-entity state updates belong in a manager or domain object.
- Is any method longer than ~40 lines? Long methods are a signal that the method has more than one responsibility. Flag the method and suggest how to extract the inner concerns.

**McRPG Pattern Violations**
- Does any new code instantiate a manager or registry directly rather than accessing it via `registryAccess()`? Direct instantiation bypasses the registry system and breaks lifecycle management.
- Is `McRPG.getInstance()` called anywhere an instance could have been injected via constructor? Static singleton access is an anti-pattern when the calling class already has a lifecycle managed by the plugin.
- Is ability state (cooldown, tier, toggle, any per-holder value) stored as a field on an `Ability` object? Ability objects are shared singletons — all holder-specific state belongs in `AbilityData` / `AbilityAttribute`.
- Does any new class extend more than two levels deep in a McRPG-specific inheritance chain? Prefer interface composition (`PassiveAbility`, `CooldownableAbility`, `ConfigurableSkillAbility`) over class inheritance for capability modeling.
- Is a new static utility class introduced for domain logic (i.e., logic that requires a manager, player, config, or runtime context)? Model it as an object collaborator with injected state instead.

**Abstraction Level and Layer Separation**
- Is a concern handled in the wrong layer? Examples: a DAO parsing domain objects beyond simple mapping, a `ContentExpansion` performing runtime state mutations, a `ConfigFile` route class containing business logic.
- Does a new class or method expose internal implementation details through its public API? (e.g., returning a raw `Map<NamespacedKey, Object>` instead of a typed domain object, accepting a raw `String` where a `NamespacedKey` or enum would encode the constraint)
- Is there a leaky abstraction where callers must know implementation details to use a public method correctly? (e.g., "you must call `init()` before calling `process()`" — model with a factory or builder instead)

**Coupling**
- Does any class depend on a concrete implementation where an interface would suffice? (e.g., constructing `ArrayList` in a signature instead of `List`, accepting `QuestBoardManager` where `QuestBoardOperations` would cover the needed surface)
- Are two classes that previously had no relationship now sharing mutable state without a clear ownership boundary?
- Is a new fully-qualified type reference written inline in a method body (`org.bukkit.Location loc = ...`)? All types must be imported at the top of the file.

**Method Design**
- Does any public method accept a `boolean` parameter that changes what the method does rather than how it does it? Split into two separate, named methods.
- Does any public method return `null` in a case where `Optional<T>` would better communicate that absence is a normal outcome?
- Are there overloaded methods where the differences are not obvious from parameter types alone? Prefer descriptive method names over overloading.

**Duplication and Collaborator Extraction**
- Is logic copy-pasted across two or more classes that could be extracted into a shared collaborator? Duplicated logic must diverge eventually — extract before the divergence happens.
- Is there a block of code in a listener or slot that closely mirrors a block in another listener or slot? This is the most common place duplication hides in Minecraft plugins.

**Package Placement**
- Is a new class placed in a package that does not match its responsibility? (e.g., a reward-granting class in the `gui/` package, a condition implementation in the `quest/` root instead of `quest/board/template/condition/`)
- Does a new class in a sub-package depend on a class in a sibling sub-package, creating a circular or sideways dependency? Shared abstractions should live in the parent package.

## Do not flag

Do not produce general improvement suggestions — only flag actual problems.

## Reporting

When running interactively (not under the CI review orchestrator), report each confirmed finding as:
- **Where:** `path/File.java:line`
- **What:** the concern in one or two sentences
- **Why:** why it matters
- **Fix:** the suggested change

If nothing qualifies, say: "No architecture concerns found in this diff."
