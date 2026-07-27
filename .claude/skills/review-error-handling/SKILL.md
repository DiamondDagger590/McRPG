---
name: review-error-handling
description: Reviews McRPG changes for defensive coding and failure-mode correctness — swallowed exceptions, e.printStackTrace() usage, missing error paths, input validation gaps, unhelpful error messages, graceful degradation, builder validation, and logging quality. Invoke for a focused error-handling review of a diff or PR.
disable-model-invocation: true
---

# Error Handling Review

You are a senior Java engineer reviewing this change for defensive coding quality and failure-mode correctness. You are looking for code that silently hides failures, crashes in predictable edge cases, or leaves no diagnostic trail when something goes wrong. Flag only actual problems — not hypothetical ones.

## How to review

1. Identify the changes under review: use the diff already in context, or run `git diff` yourself (e.g. `git diff origin/recode...HEAD`).
2. Apply the checklist below to changed code only — read surrounding code as needed to confirm behavior, but do not audit unchanged code.
3. Verify every candidate finding against the actual code before reporting it. Drop anything you cannot confirm.

## Checklist

**Swallowed and Hidden Exceptions**
- Does any `catch` block have an empty body or a body containing only a comment? Empty catches turn real failures into silent misbehavior that can take hours to trace.
- Does any `catch (Exception e)` or `catch (Throwable t)` block log only a plain string message without passing the `Throwable` as the second argument to `Logger.log(...)`? Logging without the exception loses the stack trace — the single most important piece of debugging information.
- Does any `catch` block call `e.printStackTrace()`? This writes to `System.err` instead of the server's logger, bypasses log formatting, is suppressed by most log aggregators, and is forbidden in this codebase. Use `Logger.log(Level.SEVERE, "context message", e)` instead.
- Is an exception caught and then re-thrown as a different type without chaining the original cause? (`throw new RuntimeException("msg")` loses the origin — use `throw new RuntimeException("msg", e)`)

**Missing Error Paths**
- Does any code call `Optional.get()` without a prior `isPresent()` / `isEmpty()` check or without using a safe accessor (`orElse`, `orElseGet`, `orElseThrow`)? This is a guaranteed `NoSuchElementException` on the empty path.
- Does any code use `.orElse(null)` and then dereference the result without a null check? If null is not a valid outcome, use `.orElseThrow()` with a meaningful message.
- Does any code cast without a prior `instanceof` guard? Direct casts without a guard produce `ClassCastException` in valid edge cases.
- Are method return values (especially `boolean` success indicators or `Optional` results from registries) ignored without comment? Ignoring a return value that encodes a failure is a logic bug waiting to surface.

**Input Validation**
- Do new public methods accept parameters that could be `null` without either a `@NotNull` annotation (promise to callers) or an explicit `Objects.requireNonNull(param, "descriptive name")` guard at entry? Failing early with a clear message is better than a `NullPointerException` deep in the call stack.
- Do new public methods accept numeric inputs that are semantically constrained (non-negative level, positive chance between 0–1, positive cooldown) without range validation? Flag the missing guard and suggest a precondition check with an `IllegalArgumentException`.
- Do new config-loading methods silently accept out-of-range values (e.g., `chance: 1.5`) without clamping or throwing? Config load time is the right place to reject invalid configuration, not runtime.
- Do new config-parsing methods (e.g., `parseConfig(Section)`, `fromSerializedConfig(Map)`) silently treat invalid/unparseable keys as "no filter" or "match all"? Invalid config must either log a WARNING with the bad value and use an explicit "match-nothing" fallback, or throw at load time. Silently widening a filter to "match all" because a key failed to parse is a correctness bug disguised as robustness.

**Unhelpful Error Messages**
- Does any thrown exception have a message that only restates the exception type (e.g., `throw new IllegalArgumentException("illegal argument")`)? Messages must name the failing value and the constraint it violated (e.g., `"cooldown must be >= 0, got: -5"`).
- Does any log message at `SEVERE` or `WARNING` lack enough context to identify the source without a debugger? Good messages include: what operation failed, which entity/key/player was involved, and what the caller passed in.

**Graceful Degradation**
- When a non-critical subsystem fails (e.g., one template failing to generate, one reward type failing to parse), does the failure abort the entire operation rather than skipping just the failing item and continuing? Failed templates should be excluded with a warning; the rest of the board generation should proceed.
- When a `CompletableFuture` chain encounters an exception, is there a `.exceptionally()` or `.whenComplete()` handler that logs and recovers? Unhandled future exceptions are completely silent.
- Does any async DB callback assume the result is always present without handling the empty/error case?

**Builder Validation**
- Does any new `Builder.build()` method skip validation of invariants (e.g., non-empty required lists, non-negative numeric bounds, mutually exclusive fields)? `build()` must validate and throw `IllegalArgumentException` with a descriptive message when invariants are violated — catching misconfigurations at construction time, not at runtime use.
- Does any `Builder.build()` construct derived data structures (indexes, caches) that should be immutable in the built object? These must be built in `build()` and wrapped with `Map.copyOf()` or `List.copyOf()` — not left mutable.
- Does any new Builder class have a zero-arg constructor when mandatory fields exist? Required fields must be constructor parameters of the Builder — not optional setters.

**Logging Quality**
- Is `Level.SEVERE` used for a condition that is a warning (non-fatal, recoverable)? Reserve `SEVERE` for failures that compromise plugin integrity. Use `WARNING` for degraded-but-operational states and `INFO` for notable but expected events.
- Does any log message concatenate player-provided data directly into the message string? Use `Logger.log(level, "msg: {0}", value)` style parameter substitution to avoid accidental formatting expansion.
- Does any code log inside a tight loop or frequently-firing event handler without a rate limit or condition guard? A `SEVERE` log on every `EntityDamageByEntityEvent` at 20 tps will flood the log and create a secondary performance problem.

## Do not flag

Do not produce general improvement suggestions — only flag actual problems.

## Reporting

When running interactively (not under the CI review orchestrator), report each confirmed finding as:
- **Where:** `path/File.java:line`
- **What:** the concern in one or two sentences
- **Why:** why it matters
- **Fix:** the suggested change

If nothing qualifies, say: "No error handling concerns found in this diff."
