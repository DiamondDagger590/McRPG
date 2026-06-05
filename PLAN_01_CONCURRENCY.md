# Plan 01 — Concurrency Fixes (C1-C6)

**Scope:** All 6 findings share one root cause — `AvailabilityWindowChecker` runs async but performs main-thread-only operations.

## Root Cause
`AvailabilityWindowChecker` extends `CancelableCoreTask` and is registered as async. Its `onIntervalComplete()` runs on Bukkit's async scheduler thread, but it:
- Fires Bukkit events (C1, C3)
- Mutates non-volatile player state (C2)
- Iterates the player manager collection (C5)
- Uses a non-thread-safe `HashMap` from both threads (C4)
- Has methods callable from both async and main thread (C6)

## Fix Strategy
Split the checker into two phases:

### Phase 1 — Async computation (stays in `onIntervalComplete`)
- Snapshot which chains/quests need transitions based on `AvailabilityConfig` and current time
- Pure computation against immutable config objects and `ZonedDateTime` — safe on any thread
- Produce a list of `(chainKey, policy, playerUUIDs)` tuples

### Phase 2 — Main-thread callback
- `Bukkit.getScheduler().runTask(plugin, () -> { ... })` with the computed transitions
- Inside callback: iterate players, fire events, mutate state, schedule grace periods
- All Bukkit API, player iteration, and state mutation happens on main thread

### Additional fixes
- C4: Replace `HashMap<NamespacedKey, Integer> activeGraceTasks` with `ConcurrentHashMap` OR move all access to main thread
- C6: Add thread assertion or redesign so each method only runs on one thread context

## Files to modify
- `src/main/java/us/eunoians/mcrpg/quest/availability/AvailabilityWindowChecker.java`

## Checklist
- [ ] Async phase computes transitions only — no Bukkit API, no player iteration, no state mutation
- [ ] Main-thread callback does all side effects
- [ ] `activeGraceTasks` is either ConcurrentHashMap or main-thread-only
- [ ] No Bukkit event fired from async thread
- [ ] No non-volatile field mutation from async thread
- [ ] `playerManager.getAllPlayers()` only called from main thread
- [ ] Compiles clean: `./gradlew clean compileJava`
