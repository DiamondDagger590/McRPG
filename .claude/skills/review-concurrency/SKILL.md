---
name: review-concurrency
description: Reviews McRPG changes for async correctness and thread safety — thread-boundary violations (Bukkit API from async threads), race conditions, CompletableFuture error handling gaps, shared mutable state, deadlock risk, and listener/task lifecycle issues. Invoke for a focused concurrency review of a diff or PR.
disable-model-invocation: true
---

# Concurrency Review

You are a senior Java engineer reviewing this change for async correctness and thread safety. McRPG uses a database executor thread pool, `CompletableFuture` chains, `ConcurrentHashMap`s, and Bukkit's main-thread scheduler — the boundary between these execution contexts is the primary source of concurrency bugs in this codebase. Flag only actual problems, not theoretical risks on code that never crosses a thread boundary.

## How to review

1. Identify the changes under review: use the diff already in context, or run `git diff` yourself (e.g. `git diff origin/recode...HEAD`).
2. Apply the checklist below to changed code only — read surrounding code as needed to confirm behavior, but do not audit unchanged code.
3. Verify every candidate finding against the actual code before reporting it. Drop anything you cannot confirm.

## Checklist

Identify which execution context each piece of code runs in before evaluating thread-safety concerns.

**Thread-Boundary Violations**
- Does any code inside a `database.getDatabaseExecutorService().submit(...)` block, a `CompletableFuture` callback (`.thenApply`, `.thenAccept`, `.thenRun`), or any other off-main-thread context call a Bukkit API that is documented as main-thread-only? Examples: `world.getBlockAt()`, `player.sendMessage()`, `entity.teleport()`, `Bukkit.getOnlinePlayers()`, inventory mutations. These calls are not thread-safe and produce undefined behavior or server crashes.
- Does any async callback mutate world state, entity state, or player inventory directly? All world/entity/inventory mutations must hop to the main thread via `Bukkit.getScheduler().runTask(plugin, () -> { ... })`.
- Is the return value of `Bukkit.getScheduler().runTask(...)` or `runTaskLater(...)` used inside a `CompletableFuture` chain to schedule a main-thread hop? Verify the hop is unconditional — not guarded by a condition that could be evaluated on the wrong thread.

**Race Conditions**
- Does any code perform a check-then-act pattern on a `ConcurrentHashMap` using separate `containsKey()` + `put()` calls? Use `computeIfAbsent()`, `putIfAbsent()`, or `compute()` to make the operation atomic.
- Does any code read a field, compute a new value based on the old value, and then write back — without synchronization or an `AtomicReference`? This is a classic read-modify-write race.
- Does any code access a non-thread-safe collection (`HashMap`, `ArrayList`, `HashSet`) from both the main thread and an async thread, even for reads only? Concurrent reads during a structural modification cause `ConcurrentModificationException`.
- Is there a window between an offering being marked `ACCEPTED` and the corresponding `QuestInstance` being created where a second acceptance attempt could succeed? The project uses per-offering `synchronized (offeringLocks.computeIfAbsent(...))` locks to prevent this — verify new acceptance paths use the same pattern.

**CompletableFuture Error Handling**
- Does any new `CompletableFuture` chain terminate without a `.exceptionally(ex -> ...)` or `.whenComplete((result, ex) -> ...)` handler? Exceptions in future chains are completely silent — the failure disappears without logging or recovery.
- Does any code call `.get()` or `.join()` on a `CompletableFuture` from the main thread? Blocking the main thread on a future that itself needs to schedule work on the main thread (via `runTask`) is a deadlock.
- Does any code call `.get()` without wrapping in a try/catch for `ExecutionException` and `InterruptedException`? Unchecked `.get()` calls produce `InterruptedException` or wrap unexpected exceptions silently.
- When a `CompletableFuture` chain transitions from an async executor back to the main thread (via `thenAcceptAsync(..., mainThreadExecutor)` or `runTask`), does it handle the case where the transition itself is skipped (e.g., player went offline, plugin disabled)?

**Shared Mutable State**
- Is a new non-`final` field that is written from both the main thread and an async thread missing a `volatile` modifier, `AtomicReference`, or synchronization block?
- Is a new `ReloadableContent` / `ReloadableSet` / `ReloadableBoolean` field updated by a reload operation (which may run on the main thread) while concurrently being read from an async DB callback?
- Are there new `static` mutable fields (other than the allowed `McRPG.getInstance()` singleton)? Static mutable fields are the hardest class of concurrency bug to diagnose.

**Deadlock Risk**
- Does any code acquire two or more locks in a nested fashion? Check that all call sites acquire the same locks in the same order to prevent deadlock.
- Does any `synchronized` block call out to an external method or event dispatch while the lock is held? External calls under a lock are a deadlock risk if the callee tries to acquire the same lock.
- Does the scoped offering acceptance path still use the per-offering `synchronized (offeringLocks.computeIfAbsent(...))` pattern? New acceptance code paths that bypass this lock create a race.

**Listener and Task Lifecycle**
- Are new Bukkit event listeners registered in `onEnable()` without a corresponding `HandlerList.unregisterAll(listener)` call in `onDisable()`? Listeners that survive plugin disable will fire on a half-initialized plugin.
- Does any listener handle a Bukkit async event (annotated `@EventHandler` on an event whose `isAsynchronous()` returns `true`) and then call synchronous Bukkit API without scheduling a main-thread hop?
- Is a `BukkitRunnable` that holds a reference to a plugin manager or player reused across server reloads without being re-created? Stale plugin references in running tasks cause errors after `/reload`.

## Do not flag

Do not flag theoretical risks on code that never crosses a thread boundary — only flag actual problems.

## Reporting

When running interactively (not under the CI review orchestrator), report each confirmed finding as:
- **Where:** `path/File.java:line`
- **What:** the concern in one or two sentences
- **Why:** why it matters
- **Fix:** the suggested change

If nothing qualifies, say: "No concurrency concerns found in this diff."
