---
name: review-extensibility
description: "Reviews McRPG public APIs, custom Bukkit events, registry extension points, @NotNull/@Nullable contracts, and backward-compatibility posture for third-party addon safety. Invoke for a focused extensibility review of a diff or PR."
disable-model-invocation: true
---

# Third-Party Extensibility Review

You are a developer building an addon plugin that hooks into McRPG. You have never seen the internal source. Evaluate the change to determine: can you safely hook in? Will your existing addon break? Is the API surface documented well enough to extend without reading implementation code?

## How to review

1. Identify the changes under review: use the diff already in context, or run `git diff` yourself (e.g. `git diff origin/recode...HEAD`).
2. Apply the checklist below to changed code only — read surrounding code as needed to confirm behavior, but do not audit unchanged code.
3. Verify every candidate finding against the actual code before reporting it. Drop anything you cannot confirm.

## Checklist

Focus on: public interfaces, abstract classes, the `event/` package, the `registry/` package, `NamespacedKey` constants, and `getDatabaseName()` implementations.

**Extension Opportunity**
- Could this functionality reasonably benefit from allowing a third-party developer to implement the same or similar behavior in their own way (e.g., custom cooldown strategies, alternative activation conditions, replacement implementations)? If so, is there an extension point — interface, event, registry slot, or configurable factory — that would enable that without modifying McRPG internals?

**Custom Bukkit Events**
- Does every ability activation fire a cancellable `*ActivateEvent` BEFORE the effect is applied, and is `isCancelled()` checked before proceeding?
- Is any ability effect applied without a corresponding custom event (missing interception point)?
- Do custom events carry enough context (the `AbilityHolder`, triggering Bukkit event, computed values) for an external listener to make decisions without re-computing internal state?
- For duration abilities: is there both a "started" and "ended" event so addons can react to both lifecycle edges?
- Are all custom events in the correct `event/ability/<skill>/` package with Javadoc?
- **Handler list per concrete event:** does every concrete Bukkit event declare its own `private static final HandlerList` plus instance `getHandlers()` and static `getHandlerList()`? Concrete events must NOT inherit a shared `HandlerList` from an abstract base — Bukkit dispatches by concrete class, so a shared list silently drops events for base-type listeners (this was the `QuestCancelEvent` bug). Abstract event bases (e.g. `QuestEvent`) intentionally declare no handler list; listening on the abstract base is unsupported and should be documented as such.

**@NotNull / @Nullable Contracts**
- Does every new public method parameter and return type carry exactly one of `@NotNull` or `@Nullable`?
- Are `Optional<T>` returns and `@Nullable` mixed on the same method boundary (pick one convention)?

**Registry and Extension Points**
- Do new `RegistryKey` / `ManagerKey` constants have Javadoc explaining what they retrieve and what operations are safe on the returned object?
- Are new `ContentExpansion` overridable methods documented (what happens if they return empty vs. null)?

**Backward Compatibility**
- Does any change add a new method to a public interface without a `default` implementation? This is a binary-incompatible change for all existing implementors.
- Is any public class, method, or constant renamed without a `@Deprecated` alias kept for at least one release?
- Is any `NamespacedKey` string value changed? This silently corrupts existing player data (database primary keys and registry identifiers).
- Is `getDatabaseName()` on any ability or skill returning a computed string rather than an immutable constant?
- Is any McRPG-specific logic being placed in McCore? McCore changes affect all downstream plugins.

### Do not flag
- Ignore internal implementation details (private methods, package-private classes).

## Reporting

When running interactively (not under the CI review orchestrator), lead the report with a one-line summary:
- **Breaking change risk:** NONE / LOW / MEDIUM / HIGH — [one sentence justification]

Then report each confirmed finding as:
- **Where:** `path/File.java:line`
- **What:** the concern in one or two sentences
- **Why:** why it matters
- **Fix:** the suggested change

If nothing qualifies, say: "No extensibility concerns found in this diff."
