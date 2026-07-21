# Phase 2 Audit Findings: Combat State & Statistics Platform

> Audited changeset: uncommitted working-tree implementation of
> [phase-2-combat-state-and-statistics-platform.md](phase-2-combat-state-and-statistics-platform.md).
> Implementation status: all code written, `./gradlew verifiedShadowJar` green, 2474 tests passing.
> Audit method: 2 independent Sonnet subagents per persona (18 total) from `.claude/commands/review-*.md`,
> each given the full diff and the LLD for context. Findings below are de-duplicated and cross-referenced —
> "(2/2 passes)" means both independent reviewers for that persona caught it, which is a stronger signal
> than a single pass.
>
> **Proposed fixes** were added to every finding after the audit.
>
> **Status: all findings applied.** Findings 1–7 and 9–15 have been implemented in the working tree;
> #8 (the breaking-change changelog note) was consciously skipped — no addons exist against Phase 1,
> so the practical blast radius is zero. #4/#5 took the *thorough* option (tracked write futures with
> per-entity ordering and a bounded shutdown drain) rather than the pragmatic sync-on-logout one; that
> is now recorded as **D14** in the Phase 2 LLD. The "one thing the auditors disagreed on" was
> resolved by reading McCore: `PlayerJoinListener` schedules `McRPGPlayerLoadTask` with `runTask()`
> (sync), so `RepeatableCoreTask` uses `runTaskTimer` and the DB read runs on the **main thread** —
> audit pass 2 was correct. This section is kept as a historical record of the audit; descriptions
> below reflect the code *as audited*, not as it stands now.

## How to read this

- **Fix-worthy** = a real defect: wrong behavior, silent data loss/corruption, or a test that can't catch
  the bug it claims to test. I'd recommend addressing these before merging.
- **Worth a decision** = a legitimate design tradeoff the LLD didn't fully account for. Not obviously
  "wrong," but you should consciously decide rather than let it stand by accident.
- **Polish** = real but low-stakes: doc comments, test hygiene, minor duplication.
- **Clean** = personas that found nothing (included so you know they actually ran, not skipped).
- **Proposed fix** = the concrete change I'd make for that finding, with a code sketch where it helps.
- **Liveness** = whether the finding can actually trigger *in Phase 2* — see the note directly below.

### Phase 2 liveness note (read before prioritizing)

Phase 2 ships the combat-state/persistence machinery but **registers zero state types** (the
`CombatStateTypeContentPack` is empty; Ramping Frenzy's persistent/resolved state is Phase 4) and has
**no in-tree caller** of `reportHealing()`, `registerStateType()`, or `registerSessionStatisticKey()`
(verified by grep — the only references are Javadoc links). That means several findings describe code
paths that are correct-by-construction *dormant* today and can only misbehave once a state type is
registered or a heal ability calls `reportHealing()`. Each fix below is tagged:

- **Live now** — reachable by the stat-tracking pipeline that Phase 2 turns on (damage/kill listeners,
  cumulative feed), or a general code-quality/test issue that's always relevant.
- **Dormant** — cannot execute until a `CombatStateType` is registered or a `reportHealing()` caller
  exists. Safe to schedule alongside Phase 4, though most are cheap enough to fix now while the code is
  fresh.

This is *not* a reason to skip them — a dormant bug shipped in a released API is still a bug a third-party
plugin can trip on the moment they register a state type. It's a reason to sequence them sensibly.

---

## Fix-worthy

### 1. `HEALING_RECEIVED` can be double-counted (Architecture, pass 2)

`CombatTrackerManager.reportHealing(healer, target, amount)` increments `HEALING_RECEIVED` on the
target's session explicitly. Independently, `OnCombatHealingStatListener` increments the *same* key on
*every* `EntityRegainHealthEvent` for any entity with an active session, regardless of cause. The LLD's
own D5 design decision documents that heal abilities are expected to call `reportHealing()` "before or
after applying the heal" — but applying a real heal through Bukkit APIs is exactly what fires
`EntityRegainHealthEvent`. Any ability that does both (applies the heal *and* calls `reportHealing()` for
attribution, which is the documented calling convention) double-counts `HEALING_RECEIVED` for that
session, and the inflated total then flows into the cumulative `McRPGStatistic.HEALING_RECEIVED` via
`OnCombatSessionEndStatUpdateListener`.

This is the same double-counting failure mode that D7 explicitly ruled out for `DAMAGE_DEALT`/
`DAMAGE_TAKEN` — it was simply missed for the healing-attribution path. Note this traces back to the LLD
itself: flow 4.4 shows `reportHealing()` writing `HEALING_RECEIVED` and the passive listener writing it
too, without reconciling the case where the attributed heal *is* the event that fires.

**Where:** `CombatTrackerManager.reportHealing()`, `OnCombatHealingStatListener.onEntityRegainHealth()`

**Liveness:** Dormant — no in-tree ability calls `reportHealing()` yet, but the passive listener half is
already live, so the double-count activates the instant the first heal-attribution caller lands.

**Proposed fix:** Make `reportHealing()` the authority for **dealt** only, and let the passive listener
remain the sole authority for **received** — which is exactly D5's stated division of labor. Drop the
target-side `HEALING_RECEIVED` increment:

```java
public void reportHealing(@NotNull UUID healerUUID, @NotNull UUID targetUUID, double amount) {
    requireMainThread();
    getSession(healerUUID).ifPresent(session ->
            session.getStatistics().incrementDouble(CombatSessionStatisticKey.HEALING_DEALT, amount));
    // HEALING_RECEIVED is captured by OnCombatHealingStatListener when the heal fires
    // EntityRegainHealthEvent, so reportHealing must NOT also write it — see D5 / audit #1.
}
```

Then update `reportHealing`'s Javadoc, the `targetUUID` param doc (it's now only used to locate the
target's session for *nothing* — so the param can arguably be dropped too; keep it if you want the
signature stable for future use, but document why it's currently unused), and LLD flow 4.4.

*Trade-off to decide:* a heal that does **not** fire `EntityRegainHealthEvent` (pure absorption hearts, or
a plugin that mutates health without the event) won't be counted as received. That's acceptable and
arguably more correct (absorption isn't "health regained"). If you'd rather keep `reportHealing()`
authoritative for received, the alternative is a short-lived "pending attribution" set (target UUID +
amount, cleared next tick) that the passive listener consults to suppress the double — more machinery, not
recommended. I'd take the drop-the-increment option.

---

### 2. Two tests in `OnCombatDamageStatListenerTest` can't catch the bug they name (Testing, pass 1 — verified directly)

`doesNotIncrement_whenSourceHasNoSession` gives the source no session and a real target session, then
asserts `targetSession.getStatistics().getDouble(DAMAGE_DEALT) == 0.0`. But per the listener's actual
code, `DAMAGE_DEALT` is *only ever* written to the **source's** session — never the target's, under any
code path, correct or broken. The mirrored test (`doesNotIncrement_whenTargetHasNoSession`) has the same
defect in reverse, checking `sourceSession`'s `DAMAGE_TAKEN` (which is never written to a source session
either way). I confirmed this by reading the listener and test side by side: both assertions would pass
even if the `ifPresent` guards being "tested" were deleted entirely.

**Where:** `src/test/java/us/eunoians/mcrpg/listener/combat/OnCombatDamageStatListenerTest.java:93-119`

**Liveness:** Live now — test-quality issue, always relevant.

**Proposed fix:** The real behavior worth guarding is "when one side has no session, the listener doesn't
crash and the *other* side's stats are still recorded." Rewrite both to assert on the side that actually
receives writes, and add a no-throw assertion:

```java
@Test
@DisplayName("records target stats and does not throw when the source has no session")
void handlesSourceWithoutSession() {
    PlayerMock source = server.addPlayer();
    Zombie target = spawnEntity(Zombie.class);
    when(manager.getSession(source.getUniqueId())).thenReturn(Optional.empty());
    CombatSession targetSession = session(target.getUniqueId());
    when(manager.getSession(target.getUniqueId())).thenReturn(Optional.of(targetSession));

    assertDoesNotThrow(() -> listener.onEntityDamageByEntity(damageEvent(source, target, 5.0)));

    // Target path is unaffected by the source being session-less.
    assertEquals(5.0, targetSession.getStatistics().getDouble(CombatSessionStatisticKey.DAMAGE_TAKEN));
    assertEquals(1L, targetSession.getStatistics().getLong(CombatSessionStatisticKey.HITS_RECEIVED));
}

@Test
@DisplayName("records source stats and does not throw when the target has no session")
void handlesTargetWithoutSession() {
    PlayerMock source = server.addPlayer();
    Zombie target = spawnEntity(Zombie.class);
    CombatSession sourceSession = session(source.getUniqueId());
    when(manager.getSession(source.getUniqueId())).thenReturn(Optional.of(sourceSession));
    when(manager.getSession(target.getUniqueId())).thenReturn(Optional.empty());

    assertDoesNotThrow(() -> listener.onEntityDamageByEntity(damageEvent(source, target, 5.0)));

    assertEquals(5.0, sourceSession.getStatistics().getDouble(CombatSessionStatisticKey.DAMAGE_DEALT));
    assertEquals(1L, sourceSession.getStatistics().getLong(CombatSessionStatisticKey.HITS_LANDED));
}
```

These now fail if either `ifPresent` guard is dropped (unconditional `.get()` on the absent side throws,
tripping `assertDoesNotThrow`).

---

### 3. Third-party serializer/deserializer/resolver callbacks are invoked with no exception boundary (Error Handling, 2/2 passes)

`CombatStateType.resolved()`/`persistent()` are explicitly third-party extension points — a plugin
supplies the resolver/serializer/deserializer function. None of the call sites guard against them
throwing:

- `CombatSession.getState()`, `createStateSnapshot()`/`resolveForSnapshot()`
- `CombatTrackerManager.collectPersistentEntries()`/`serializeEntry()` — reached from `endSession()`,
  `savePersistentStateAsync()`, and `saveAllPersistentStateSync()`
- `CombatTrackerManager.applyDeserializedState()` — reached from `initializeNewSession()`, i.e. every
  ordinary combat interaction that creates a session

Concretely: a throwing resolver/serializer during `endSession()` propagates *before*
`activeSessions.remove(...)` and the event fire — the session leaks permanently in `activeSessions`. In
`shutdown()`, the `for (UUID ownerUUID : sessionOwners) { endSession(...); }` loop aborts on the first
exception, skipping `endSession(PLUGIN)` for every remaining session plus condition-task cancellation and
timeout-task shutdown — defeating the exact safety net `shutdown()` exists to provide. A throwing
deserializer on the load path breaks session creation for that entity on *every* subsequent hit until
restart, since the bad cached value is never cleared.

This is a real regression relative to the codebase's own established convention:
`CombatTrackerManager.isHeldOpenByCondition()` — same class, same "third-party extensible" character —
already wraps its equivalent call (`condition.isInCombat(player)`) in try/catch, logs a `WARNING` with
context, and continues. The new state-type call sites don't follow that precedent.

**Where:** `CombatSession.java` (`getState`, `createStateSnapshot`, `resolveForSnapshot`),
`CombatTrackerManager.java` (`collectPersistentEntries`, `serializeEntry`, `applyDeserializedState`,
`endSession`, `saveAllPersistentStateSync`)

**Liveness:** Dormant — no resolver/serializer/deserializer exists until a state type is registered.
Cheap to harden now; must be done before Phase 4 (or any third party) ships a state type.

**Proposed fix:** Wrap every third-party callback invocation in a try/catch that logs a `WARNING` naming
the offending state-type key and falls back safely, mirroring `isHeldOpenByCondition()`. The fallback per
site:

- **resolver** (`getState`, `resolveForSnapshot`): on throw, fall back to the raw value.
- **serializer** (`serializeEntry`): on throw, skip that entry (don't persist it) — better to lose one
  type's persistence than abort the whole session-end/shutdown flush.
- **deserializer** (`applyDeserializedState`): on throw, skip re-attachment (leave the type at its
  default) and log so the corrupt cached value is visible.

Sketch for the serializer path (the others follow the same shape):

```java
@NotNull
private <T> Optional<String> serializeEntry(@NotNull CombatStateType<T> type, @NotNull Object rawValue) {
    try {
        return type.getSerializer().map(serializer -> serializer.apply((T) rawValue));
    } catch (Exception e) {
        plugin().getLogger().log(Level.WARNING, "Combat state type " + type.getKey()
                + " serializer threw while persisting state; skipping this entry", e);
        return Optional.empty();
    }
}
```

Because `endSession()` calls the snapshot/serialize code before `activeSessions.remove()`, isolating the
throw here (rather than letting it unwind) is what keeps a single bad state type from leaking the session
or aborting `shutdown()`. Consider a focused test per fallback: register a state type whose
resolver/serializer/deserializer throws, and assert the session still ends / snapshot still builds / next
session still starts.

---

### 4. Fast-relog race between async save and load can silently revert persistent state (Concurrency, 2/2 passes)

`savePersistentStateAsync()` is fire-and-forget — it submits a write to
`database.getDatabaseExecutorService()` with no `Future`/callback retained. `PlayerLeaveListener.handleQuit()`
triggers this via `endSession()` and then immediately calls `clearPersistentStateCache()`. If the player
reconnects before the queued write lands, `McRPGPlayerLoadTask.loadPersistentCombatState()` performs an
independent DB read with nothing ordering it after the pending write. On a fast relog (network hiccup,
resource-pack disconnect, deliberate quick rejoin), the read can win the race, seeding the new session
with stale persistent state — which then gets re-serialized and written back at the *next* session end,
permanently discarding whatever changed in the final moments before the original logout.

Pass 2 adds: the same fire-and-forget pattern also means **no per-entity write ordering** — back-to-back
session churn for the same entity (death → immediate re-engagement, or rapid timeout/condition-driven
session cycling) can submit two writes for the same DB row with no guarantee the later-submitted one
completes last, so a stale write can silently overwrite a fresher one even without a relog involved.

**Where:** `CombatTrackerManager.savePersistentStateAsync()`, `cachePersistentState()` racing
`McRPGPlayerLoadTask.loadPersistentCombatState()`

**Liveness:** Dormant — no persistent state types exist, so no writes are ever submitted. Fix before
Phase 4 ships a persistent type.

**Proposed fix (recommended — pragmatic):** On the logout path, flush **synchronously** instead of
fire-and-forget. `PlayerLeaveListener.handleQuit()` runs on the main thread, and a single quitting
player's persistent state is a handful of small rows — the same shape `saveAllPersistentStateSync()`
already flushes synchronously at shutdown. Add a `savePersistentStateSync(CombatSession)` (or a
`boolean synchronous` variant) and call it from the logout teardown *before* `clearPersistentStateCache()`.
That removes the relog race entirely (the DB is guaranteed written before the player can rejoin) and the
per-entity ordering race for the logout case, at the cost of a small synchronous DB write on quit — bounded
and acceptable for a per-player logout.

**Proposed fix (thorough — if you want to keep logout writes async):** Track in-flight writes so both the
load path and shutdown can observe them. Have `savePersistentStateAsync()` return a `CompletableFuture<Void>`
and register it in a per-entity map (`Map<UUID, CompletableFuture<Void>>`, replacing/chaining on
resubmission so writes for the same entity serialize). Then:
- `PlayerLeaveListener` chains `clearPersistentStateCache()` after the future completes.
- `McRPGPlayerLoadTask.loadPersistentCombatState()` (or `cachePersistentState`) awaits/【prefers】 any
  outstanding write for that UUID before reading, or simply prefers a still-present in-memory cache entry
  over the DB read.

The pragmatic sync-on-logout option is simpler and closes both #4 and #5's logout window; the thorough
option also fixes the session-churn ordering race. Given the paths are dormant in Phase 2, I'd land the
sync-on-logout fix now (small, obviously-correct) and revisit the churn-ordering case if/when a persistent
type with rapid session cycling actually appears.

---

### 5. Shutdown doesn't wait for in-flight async writes before the DB closes (Concurrency, 2/2 passes)

The `shuttingDown` guard only stops `endSession()` calls made *during* `shutdown()`'s own end-session loop
from re-submitting a duplicate async save — it does not cover a `savePersistentStateAsync` task that was
already submitted moments *before* `shutdown()` was called (e.g. a player quitting seconds before
`/stop`). `McRPGBootstrap.stop()` calls `database.shutdown()` shortly after `CombatTrackerManager.shutdown()`,
which closes the connection pool and then calls `databaseExecutorService.shutdown()` with no
`awaitTermination()`. An in-flight task from just before shutdown can hit a closed connection pool — the
failure is caught and only logged at `WARNING`, so that entity's just-flushed combat state is silently
dropped, undercutting `saveAllPersistentStateSync()`'s own stated goal ("ensure no persistent state is
lost").

**Where:** `CombatTrackerManager.shutdown()`/`savePersistentStateAsync()` interaction with
`McRPGBootstrap.stop()` → `Database.shutdown()`

**Liveness:** Dormant — same as #4, no writes are submitted until a persistent type exists.

**Proposed fix:** Falls out of #4's chosen approach:
- If you take **sync-on-logout** (#4 pragmatic): logout writes are never async, so the only async writes
  are the death/timeout/despawn session-end paths. Those are far rarer immediately-pre-shutdown, but to
  fully close the gap, `shutdown()` can await outstanding write futures (see below) — or accept the
  narrowed, warning-logged window as documented behavior.
- If you take **tracked futures** (#4 thorough): at the *top* of `shutdown()`, before ending sessions and
  before `saveAllPersistentStateSync()`, drain the in-flight write map:
  `CompletableFuture.allOf(pendingWrites.values().toArray(...)).get(someTimeout, SECONDS)` (with a bounded
  timeout + `WARNING` on timeout, so a stuck executor can't hang `/stop`). That guarantees no write is
  in flight when `database.shutdown()` later closes the pool.

Either way, add a short comment in `shutdown()` explaining that the ordering (drain in-flight → sync flush
remaining → end sessions with `shuttingDown=true`) is what makes "no persistent state lost" actually true,
so a future maintainer doesn't reorder it.

---

## Worth a decision

### 6. Unregistered persistent/resolved `CombatStateType`s silently no-op (Extensibility, pass 2)

`CombatStateType.persistent()`'s Javadoc unconditionally promises: "saved to the DB on session end and
re-loaded on the next session start." In reality, that only happens if the type is *also* registered in
`CombatStateTypeRegistry`. `CombatSession.getState()`/`setState()`/`modifyState()` work perfectly fine on
an unregistered type — but `CombatTrackerManager.collectPersistentEntries()` and
`applyCachedPersistentState()` look the type up **by key** in the registry, and silently skip the entry
if it isn't found (no exception, no log). A developer who reads only the `CombatStateType`/`CombatSession`
Javadoc and calls `session.setState(myPersistentType, value)` without also calling
`combatTrackerManager.registerStateType(myPersistentType)` will see their state work all session long and
then silently vanish on session end/restart, with nothing to debug from. The same gap affects `resolved()`
types: an unregistered resolved type's state-end snapshot silently returns the *raw* value instead of the
resolved one, diverging from what `session.getState()` returned live.

**Where:** `CombatStateType.java` (`persistent()`/`resolved()` Javadoc),
`CombatTrackerManager.java` (`collectPersistentEntries`, `applyCachedPersistentState`),
`CombatSession.java` (`createStateSnapshot`)

**Liveness:** Dormant — only reachable once a state type is registered.

**Proposed fix:** Do both halves — make the contract explicit *and* fail loudly when it's violated:

1. **Contract (Javadoc):** In `persistent()` and `resolved()`, add a sentence: "The value is only
   persisted / resolved on session boundaries if this type is registered via
   `CombatStateTypeContentPack` or `CombatTrackerManager.registerStateType(...)`. Session-scoped
   `getState`/`setState` work without registration; persistence and end-of-session resolution do not."
2. **Loud signal:** in `collectPersistentEntries()`, when a stored state key maps to *no* registered
   type but its raw value differs from... (you don't have the type to know the default) — simpler: log a
   one-time `WARNING` per unknown key encountered at save time: "session for {uuid} holds state {key}
   with no registered CombatStateType; it will not be persisted." That converts the silent drop into a
   debuggable log line. Guard it against log-spam (e.g. a `Set<NamespacedKey>` of already-warned keys).

This keeps the "you can use session-scoped state without ceremony" ergonomics (D8's spirit) while making
the persistence/resolution requirement discoverable.

---

### 7. No type validation on `CombatStateChangeEvent.setNewValue()` / `CombatSession.setState()` (Architecture 2/2, Error Handling 1/2, Extensibility 2/2 — 5 independent mentions)

Bukkit events can't be generic, so `CombatStateChangeEvent` carries `oldValue`/`newValue` as raw `Object`.
`CombatStateType<T>` already carries a `Class<T> type` token specifically for this situation, but nothing
ever calls `type.getType().isInstance(...)` before `stateStore.put(type.getKey(), event.getNewValue())`.
A listener that gets the type wrong (forgets the `event.getStateType().equals(MY_TYPE)` guard the class's
own Javadoc shows as required) silently corrupts the state store. The failure doesn't surface at the
mistake — it surfaces later as an unchecked `ClassCastException`, at a completely unrelated call site
(another plugin's `getState()`, or `createStateSnapshot()`/`serializeEntry()` at session end), which is
hard to trace back to the actual offending listener.

**Where:** `CombatSession.setState()`, `CombatStateChangeEvent.setNewValue()`

**Liveness:** Dormant — no state types, so `setState` is never called in Phase 2.

**Proposed fix:** Validate in `CombatSession.setState()` after the event resolves the final value, right
before the store write, so it catches both a bad initial value and a bad listener-substituted value:

```java
public <T> void setState(@NotNull CombatStateType<T> type, @NotNull T value) {
    T oldValue = getRawState(type);
    CombatStateChangeEvent event = new CombatStateChangeEvent(this, type, oldValue, value);
    Bukkit.getPluginManager().callEvent(event);
    if (event.isCancelled()) {
        return;
    }
    Object newValue = event.getNewValue();
    if (!type.getType().isInstance(newValue)) {
        throw new IllegalArgumentException("CombatStateChangeEvent for " + type.getKey()
                + " produced a " + newValue.getClass().getName()
                + " but the state type expects " + type.getType().getName()
                + " — a listener likely called setNewValue with the wrong type");
    }
    stateStore.put(type.getKey(), newValue);
}
```

`isInstance` handles boxed primitives correctly (`Integer.class.isInstance(anInteger)` is true), and values
are `@NotNull`, so no null edge case. Throwing here surfaces the failure *at the offending write* with the
listener-blaming message, instead of a distant `ClassCastException`. Add a test: a listener that
`setNewValue`s a wrong-typed object, assert `IllegalArgumentException` from `setState`.

---

### 8. `CombatSession` participant mutators go public → package-private with no deprecation shim (Extensibility, 2/2 passes)

`addParticipant()`, `removeParticipant()`, `recordActivity()`, `recordParticipantInteraction()` drop from
`public` to package-private (per LLD decision D1, which is architecturally sound — bypassing these skips
`CombatParticipantAddEvent`/`CombatParticipantRemoveEvent`). But `CombatSession` is a class third parties
routinely obtain (`CombatTrackerManager.getSession()`, `CombatStateChangeEvent.getSession()`, etc.). Any
addon compiled against Phase 1 that called these directly — legitimate at the time, since they were public
— fails to compile against the new jar, and a precompiled addon jar throws `IllegalAccessError`/
`NoSuchMethodError` at runtime if dropped onto a server running this version without recompiling. Same
category of break applies to `CombatSessionEndEvent`'s constructor, which grew from 5 to 7 required
parameters with no back-compat overload.

This is called out because it's a real compatibility break, not because it's wrong — D1's rationale (route
mutation through the manager, which now exposes `removeParticipantFromSession(...)`) is correct. This is
purely a "how loudly do we want to announce this to third-party devs" question — a changelog note or
major-version bump signal, not a code change.

**Where:** `CombatSession.java` (four methods), `CombatSessionEndEvent.java` (constructor)

**Liveness:** Live now (it's a compatibility fact of this jar), but only affects external addons compiled
against Phase 1 — none exist in this repo.

**Proposed fix:** No code change — do *not* add shims. A `@Deprecated` public forwarding method for the
mutators would re-open the exact event-bypass hole D1 closes, and a back-compat `CombatSessionEndEvent`
5-arg constructor would have to fabricate empty snapshots (misleading). Instead, announce it:

- Add a **BREAKING CHANGES** entry to the release notes / changelog for this version, e.g.:
  > `CombatSession` participant mutators (`addParticipant`, `removeParticipant`, `recordActivity`,
  > `recordParticipantInteraction`) are now internal. Use
  > `CombatTrackerManager.removeParticipantFromSession(owner, participant, reason)` and the
  > `reportCombatActivity`/`reportConditionActivity` entry points instead.
  > `CombatSessionEndEvent`'s constructor gained `statistics` and `combatState` parameters.
- If the project follows semver-ish signalling, this is a minor/major-bump-worthy change; note it wherever
  API compatibility is tracked. Since McRPG is pre-`recode`-branch and has no released Phase 1 addons,
  the practical blast radius is zero today — the note is insurance for future addon authors.

---

### 9. `registerSessionStatisticKey(NamespacedKey, boolean isDouble)` uses a behavior-selecting boolean (Architecture, 2/2 passes)

Both architecture passes independently flagged this: the boolean picks between two entirely different
internal storage paths (`registeredDoubleStatKeys` vs. `registeredLongStatKeys`), which is exactly the
"boolean parameter that changes what a method does" pattern the project's own conventions warn against.
Call sites read as `registerSessionStatisticKey(key, true)` with no self-evident meaning. The rest of this
same feature already gets this right — `CombatSessionStatistics`/`CombatSessionStatisticsSnapshot` both
expose distinct `incrementDouble`/`incrementLong`, `getDouble`/`getLong` pairs — making this one method an
inconsistent outlier.

**Where:** `CombatTrackerManager.registerSessionStatisticKey()`

**Liveness:** Dormant — no in-tree caller. Best fixed now, while there are zero call sites to migrate.

**Proposed fix:** Split into two intention-revealing methods. Since there are no external callers yet, just
replace outright (no deprecated bridge needed):

```java
/** Registers a double-valued custom per-session statistic key so it appears in every new session. */
public void registerDoubleSessionStatisticKey(@NotNull NamespacedKey key) {
    requireMainThread();
    registeredDoubleStatKeys.add(key);
}

/** Registers a long-valued custom per-session statistic key so it appears in every new session. */
public void registerLongSessionStatisticKey(@NotNull NamespacedKey key) {
    requireMainThread();
    registeredLongStatKeys.add(key);
}
```

Update the LLD's §2.2 method list and the `CombatTrackerManagerTest.RegisterSessionStatisticKey` tests
(currently `registerSessionStatisticKey(key, true/false)`) to the new names.

---

## Polish

### 10. `OnCombatSessionEndStatUpdateListener` takes an unused `CombatTrackerManager` constructor dependency (Architecture, pass 1)

The field is declared, set in the constructor, and never read — config is read directly via
`mcRPG.registryAccess()` instead. This matches the LLD's own class diagram (which lists both `mcRPG` and
`combatTrackerManager` as fields), so it's a spec artifact rather than an implementation slip — worth
either using the field for something real or dropping it from the constructor.

**Where:** `OnCombatSessionEndStatUpdateListener.java`

**Liveness:** Live now (cosmetic).

**Proposed fix:** Drop the field and constructor param, and update the wiring in `McRPGListenerRegistrar`
from `new OnCombatSessionEndStatUpdateListener(plugin, combatTrackerManager)` to
`new OnCombatSessionEndStatUpdateListener(plugin)`. Update the LLD §1.15 constructor signature to match.
(If there's a concrete near-future use for the manager here — e.g. reading a registered-stat-key set to
drive the cumulative mapping dynamically — keep it and add that use; otherwise remove.)

---

### 11. `registerStateType()` on the manager duplicates the registry-ownership boundary the class's own Javadoc draws (Architecture, pass 1)

`CombatTrackerManager`'s class Javadoc states the sibling `CombatConditionRegistry` is "independent... the
manager reads from it and coordinates task lifecycle, but does not own it" — and the codebase follows
that: `ContentHandlerType.COMBAT_CONDITION` registers directly on the registry and only calls into the
manager for the manager-owned concern (`startConditionTask`). `registerStateType()` breaks that same
boundary for `CombatStateTypeRegistry`, producing two divergent ways to register a state type (direct
registry access vs. this manager pass-through) with no behavioral difference — just inconsistent surface
area. This is debatable (the LLD explicitly specs this convenience method for standalone plugins), but
worth a conscious choice rather than an accident.

**Where:** `CombatTrackerManager.registerStateType()`

**Liveness:** Dormant — no caller.

**Proposed fix (recommended — keep + reword):** Unlike conditions, state types have no per-type task for
the manager to coordinate, so the convenience wrapper adds little. But since the LLD specs it and it's
harmless, keep it and reword its Javadoc so it doesn't imply the manager *owns* the registry — frame it
explicitly as a thin convenience over `registryAccess().registry(McRPGRegistryKey.COMBAT_STATE_TYPE).register(...)`
for standalone plugins that don't want to reach for the registry directly. **Alternative (remove):** delete
`registerStateType()` and point standalone registrants at the registry directly (matching how conditions
work, minus the task-start step they don't need). I lean keep+reword — it's a documented API and pulling it
would itself be a (tiny) breaking change to the just-written surface.

---

### 12. Duplicated projectile/self-damage resolution between `OnCombatDamageListener` and `OnCombatDamageStatListener` (Architecture, pass 2)

The `damager instanceof Projectile` → `getShooter()` unwrap, the `LivingEntity` casting guard, and the
self-damage check are copy-pasted line-for-line between the two listeners. They now silently depend on
staying in sync — a shared collaborator (a resolver method on the manager, or a package-private helper)
would remove the duplication risk.

**Where:** `OnCombatDamageListener.onEntityDamageByEntity()`, `OnCombatDamageStatListener.onEntityDamageByEntity()`

**Liveness:** Live now — both listeners run on every hit; the maintenance-drift risk is real today.

**Proposed fix:** Extract the shared resolution into one package-private static helper in the
`listener.combat` package (or a small `CombatDamageResolution` util), returning the resolved combatant pair
or empty when a guard rejects the event:

```java
// package us.eunoians.mcrpg.listener.combat;
final class CombatDamageResolution {
    /** Resolves (sourceUUID, targetUUID) from a damage event: unwraps projectile shooters, requires both
     *  to be LivingEntity, and rejects self-damage. Empty when any guard fails. */
    static Optional<CombatantPair> resolve(EntityDamageByEntityEvent event) { ... }
    record CombatantPair(UUID sourceUUID, UUID targetUUID,
                         LivingEntity source, LivingEntity target) {}
}
```

Both listeners call `CombatDamageResolution.resolve(event)` and act on the result — `OnCombatDamageListener`
passes the entities to `handleCombatInteraction`, `OnCombatDamageStatListener` increments stats. One place
to change if the resolution rules ever move.

---

### 13. Config/statistic documentation gaps for server owners (Server Owner, both passes)

- `combat_configuration.yml`'s header comment wasn't updated to mention the new
  `per-session-statistics` section (pass 1, minor).
- `HEALING_RECEIVED` (both per-session and cumulative) includes *all* health regeneration that happens
  while an entity is merely tagged in-combat — natural regen, saturation, potions, beacons — not just
  combat/ability healing, for up to `timeout-seconds` (8s default) after any hit. The in-code description
  ("Total healing received") doesn't disclose this, so leaderboards/PAPI placeholders built on it may not
  measure what an admin assumes (pass 2).
- `COMBAT_KILLS` and the pre-existing `MOBS_KILLED` both increment from the same mob-kill event but with
  different, undocumented boundaries (session-scoped + PvP-inclusive vs. unconditional + mob-only) — an
  admin picking between two similarly-named "kills" stats has no in-code signal for which is which
  (pass 2).

**Where:** `combat_configuration.yml` (header), `McRPGStatistic.java` (`HEALING_RECEIVED`, `COMBAT_KILLS`)

**Liveness:** Live now — the stats are populated by the Phase 2 pipeline and visible via existing
statistic commands/PAPI.

**Proposed fix:**

1. Extend the `combat_configuration.yml` top-of-file comment to mention statistics config, e.g.
   "Controls per-player combat session tracking, timeout behavior, participant management, **and how
   per-session combat statistics feed into cumulative totals.**"
2. Widen the `HEALING_RECEIVED` description to disclose scope:
   `doubleStat("healing_received", "Healing Received", "Total health regained while in combat — includes natural regen, saturation, potions, and beacons, not just ability/PvP healing")`.
   Keep it admin-facing plain English (these descriptions aren't localized, per `McRPGStatistic`'s note).
3. Add a Javadoc sentence on `COMBAT_KILLS` cross-referencing `MOBS_KILLED`: "Counts every kill credited to
   an active combat session — mobs and players. Distinct from `MOBS_KILLED`, which is a global,
   session-independent, non-player-only counter. A mob killed during a session increments both."
   (The LLD's D7 already explains this; the fix is just surfacing it where an admin/dev browsing the
   constants would see it.)

---

### 14. Test hygiene (Testing, both passes)

- `CombatStateTypeTest` and `CombatSessionStatisticKeyTest` extend `McRPGBaseTest` without needing any
  MockBukkit/server interaction, inconsistent with sibling pure-logic tests added in the same diff
  (`CombatStateTypeRegistryTest`, `CombatStateSnapshotTest`, `CombatSessionStatisticsTest`, etc.), which
  correctly stay plain JUnit.
- `CombatStateTypeTest.duplicateKeyOnRegistry_throwsIllegalState` duplicates coverage already in
  `CombatStateTypeRegistryTest.register_throwsIllegalState_onDuplicateKey`.
- `CombatTrackerManager.savePersistentStateAsync()` and `saveAllPersistentStateSync()` — the actual
  serialize/cache/DB-write logic — have no behavioral coverage. The one test that touches
  `savePersistentStateAsync` replaces its entire body with `doNothing()` (verifies it's *called*, not what
  it does); `saveAllPersistentStateSync` isn't exercised past its empty early-return branch by any test.
- `OnCombatEntityDeathListenerTest` (new file, first-ever test for this listener) only covers the new
  kill-tracking branch — it never asserts the pre-existing `endSession()`/
  `removeParticipantFromAllSessions()` calls happen.
- `OnCombatEntityDeathListenerTest` uses raw `mock(LivingEntity.class)` for the mob/victim instead of
  `spawnEntity(Zombie.class)` (the `McRPGBaseTest` helper), inconsistent with every sibling combat-listener
  test including the new `OnCombatDamageStatListenerTest` in the same diff.
- `CombatSessionStatistics.incrementDouble`/`incrementLong` document support for negative deltas
  ("may be negative for decrements") but no test exercises a negative increment.

**Liveness:** Live now — test-quality, always relevant.

**Proposed fix (per bullet):**

1. Drop `extends McRPGBaseTest` from `CombatStateTypeTest` and `CombatSessionStatisticKeyTest` — they only
   build plain objects and static keys. (Sanity-check `CombatStateTypeTest` doesn't construct a
   `CombatSession` anywhere; if a case needs one, split that case out rather than keeping the base class
   for the whole file.)
2. Delete `duplicateKeyOnRegistry_throwsIllegalState` from `CombatStateTypeTest`; it's registry behavior,
   already covered in `CombatStateTypeRegistryTest`.
3. Add real behavioral tests for `savePersistentStateAsync`/`saveAllPersistentStateSync` following the
   repo's `QuestManagerCacheTtlTest` pattern: mock `Database` + its `ThreadPoolExecutor`/`Connection`,
   capture the submitted `Runnable` (or run it synchronously), register a real persistent `CombatStateType`,
   set state on a session, and assert (a) the value is serialized and written via
   `CombatPersistentStateDAO.savePersistentState`, and (b) `persistentStateCache` is updated. For
   `saveAllPersistentStateSync`, register a persistent type + set state, then call `shutdown()` and assert
   the synchronous batch write happened.
4. In `OnCombatEntityDeathListenerTest`, add `verify(manager).endSession(deadUUID, CombatSessionEndReason.DEATH)`
   and `verify(manager).removeParticipantFromAllSessions(deadUUID, ParticipantRemovalReason.DEATH)` to at
   least one test, so all three of `onEntityDeath`'s effects are covered.
5. Replace `mock(LivingEntity.class)` with `spawnEntity(Zombie.class)` for the dead entity (the helper
   returns a real MockBukkit entity; use `spy()` as already done for the PvP-victim case if `getKiller()`
   must be stubbed).
6. Add a `CombatSessionStatisticsTest` case: `incrementDouble(key, 5.0)` then `incrementDouble(key, -2.0)`,
   assert `3.0`; likewise for `incrementLong`. Guards the documented decrement path.

---

### 15. Two state transitions bypass `CombatStateChangeEvent` entirely, undocumented (Extensibility, pass 1)

Persistent-state re-attachment at session start (`applyDeserializedState()` → `CombatSession.setRawState()`)
and the full wipe at session end (`clearSessionState()`) both skip the event. A third party treating
`CombatStateChangeEvent` as the single source of truth for "this session's state changed" (e.g. to keep an
external scoreboard/boss-bar in sync) would miss both moments. The LLD's flow section documents "no event
fired" for these paths, but that caveat isn't visible anywhere a third-party developer would actually read
it (not in the event's Javadoc, not in the registry key's Javadoc).

**Where:** `CombatStateChangeEvent.java` (Javadoc), `McRPGRegistryKey.COMBAT_STATE_TYPE` (Javadoc),
`CombatSession.setRawState()`/`clearSessionState()`

**Liveness:** Dormant — no state types, so neither transition carries data in Phase 2.

**Proposed fix (recommended — doc-only):** Add a "Not fired for:" note to `CombatStateChangeEvent`'s class
Javadoc: "This event is fired only by `setState`/`modifyState`. It is **not** fired when persistent state
is re-attached at session start (raw restore from the DB) or when session-scoped state is cleared at
session end — consumers that mirror state externally should also observe `CombatSessionStartEvent` /
`CombatSessionEndEvent` (whose `getCombatState()` snapshot captures end-of-session values)." Mirror a
one-line pointer in the `COMBAT_STATE_TYPE` registry-key Javadoc. **Alternative (heavier, not recommended
now):** introduce dedicated re-attach/clear events — only worth it if a concrete consumer needs to react to
those transitions, which none does in Phase 2.

---

## Clean — no findings

- **GUI/UX** (2/2 passes): expected — this is a backend-only feature, no `gui/` files, no locale YAML, no
  player-facing text anywhere in the diff.
- **Performance** (2/2 passes): hot-path listeners are O(1) per event; `persistentStateCache` and the
  registered-stat-key sets have symmetric, bounded lifecycles (join/quit-paired, registration-time-only);
  no unbounded growth, no `Entity`/`Player` refs in long-lived collections, no new scheduler tasks.
- **Security** (2/2 passes): `CombatPersistentStateDAO` is fully parameterized (only the constant table
  name is concatenated); no MiniMessage/command-dispatch/permission-node changes anywhere in the diff; the
  deserializer-invocation path was traced and found to have no player-controlled input reaching it in this
  changeset (both reviewers noted the *robustness* angle belongs to error-handing, not injection — see
  finding #3 above, which covers it).

---

## One thing the auditors disagreed on

Both concurrency passes examined which thread `McRPGPlayerLoadTask.loadPersistentCombatState()` (the new
DB-read phase I added) executes on — pass 1 concluded it runs in a different execution context than the DB
executor thread the writes use; pass 2 concluded the whole `loadPlayer()` pipeline runs on the main thread
via `RepeatableCoreTask`/`Bukkit.getScheduler().runTaskTimer(...)`. I didn't independently resolve this.
It's worth noting that my new method is structurally identical to the six other `UpdatePlayerDataSyncFunction`
loaders already in `McRPGPlayerLoadTask` (skills, loadouts, settings, experience extras, statistics,
board-quest-count) — so if there's a threading concern here, it's a pre-existing characteristic of that
class's design that predates this diff, not something introduced by it. Worth a quick manual check before
treating it as either a confirmed finding or a non-issue.

**Proposed fix:** Resolve the ambiguity by reading `McRPGPlayerLoadTask`'s scheduling (`runTask()` →
`RepeatableCoreTask`) against McCore's `PlayerLoadTask` base to confirm the actual thread, then add a
one-line comment at the top of `loadPersistentCombatState()` stating which thread the DB read runs on
(matching the six sibling loaders). If it turns out the read genuinely runs off the main thread, the sync
functions' main-thread `updateData()` callback already handles the hand-back — no change needed, just the
clarifying comment. This is a "confirm and document," not a code fix.

---

## Suggested sequencing

Split by liveness rather than a flat priority list:

**Fix before this merges (live in Phase 2, cheap):**
- #2 — fix the two tautological damage-stat tests (they give false confidence today).
- #10 — drop the unused constructor dependency.
- #12 — extract the duplicated damage-resolution helper.
- #13 — the config/statistic doc clarifications (admins see these stats now).
- #14 — test hygiene (esp. the missing `endSession`/`removeParticipantFromAllSessions` assertions and the
  real `savePersistentState*` coverage).

**Fix now while the code is fresh, even though dormant (they harden the just-written public API before any
consumer exists):**
- #1 — the healing double-count (drop `reportHealing`'s received increment) — one-line change, prevents a
  guaranteed bug the moment a heal ability wires in.
- #9 — split the boolean-flag stat-key method (zero callers to migrate).
- #3 — wrap the third-party callbacks in the `isHeldOpenByCondition`-style guard.
- #7 — add the type-validation check in `setState`.

**Schedule alongside Phase 4 (dormant, and the "right" fix depends on how Phase 4's persistent state
actually behaves):**
- #4 / #5 — the async save/load race and shutdown-drain. Land the sync-on-logout fix now if you want it out
  of the way; otherwise these can't fire until Phase 4 registers a persistent type.

**Decisions to make (no wrong answer, just pick):**
- #6 — Javadoc contract + loud-log for unregistered persistent/resolved types.
- #8 — changelog/BREAKING note for the visibility + constructor changes (no code).
- #11 — keep+reword vs. remove `registerStateType`.
- #15 — doc-only note that re-attach/clear bypass the state-change event.
