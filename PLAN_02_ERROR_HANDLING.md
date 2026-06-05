# Plan 02 — Error Handling Fixes (E1-E7)

## E1 — High: DB migration version bump on failure
**Problem:** `QuestChainCompletionLogDAO.attemptUpdateTable()` and `QuestChainStateDAO.attemptUpdateTable()` call `TableVersionHistoryDAO.setTableVersion(connection, TABLE_NAME, 2)` outside the try-catch for the ALTER TABLE. If ALTER fails, version still bumps to 2 and column is never added on retry.
**Fix:** Move `setTableVersion` inside the try block, after `executeUpdate()` succeeds.
**Files:**
- `src/main/java/us/eunoians/mcrpg/database/table/quest/chain/QuestChainCompletionLogDAO.java`
- `src/main/java/us/eunoians/mcrpg/database/table/quest/chain/QuestChainStateDAO.java`

## E2 — Medium: retryCounters eviction not wired
**Problem:** `clearRetryCountersForPlayer` exists but no listener calls it on disconnect.
**Fix:** Verify if an existing player unload path calls it. If not, wire it into `QuestChainManager.unloadPlayer()` or the player quit listener.
**Files:**
- `src/main/java/us/eunoians/mcrpg/quest/chain/QuestChainManager.java`

## E3 — Medium: Offline players missed by reconcileOnStartup
**Problem:** `reconcileOnStartup()` only processes online players. Offline chains stay ACTIVE.
**Fix:** This is a design limitation — document it as a known constraint. Offline chains get reconciled on next login via `reResolveOnLogin`. Add a Javadoc note.
**Files:**
- `src/main/java/us/eunoians/mcrpg/quest/availability/AvailabilityWindowChecker.java`

## E4/E5 — Medium: Silent event skip for offline players
**Problem:** `handleExpireRetry` and `handleExpireRestartChain` silently skip events when player is offline.
**Fix:** Tied to X9/X10 — make `QuestChainRestartEvent` and `QuestChainStepRetryEvent` accept `@Nullable Player` (matching other chain events). Then always fire the event.
**Files:**
- `src/main/java/us/eunoians/mcrpg/event/quest/chain/QuestChainRestartEvent.java`
- `src/main/java/us/eunoians/mcrpg/event/quest/chain/QuestChainStepRetryEvent.java`
- `src/main/java/us/eunoians/mcrpg/quest/chain/QuestChainManager.java`

## E6 — Low: WindowClosePolicy.fromString() silent parse failure
**Fix:** Add `Logger.log(Level.WARNING, ...)` when the value is unrecognized, or document that callers must handle empty.
**Files:**
- `src/main/java/us/eunoians/mcrpg/quest/chain/availability/WindowClosePolicy.java`

## E7 — Low: resolveAbilityName removed warning log
**Fix:** Add a FINE-level log when ability is not found in registry (distinguishes soft-disable from genuine corruption).
**Files:**
- `src/main/java/us/eunoians/mcrpg/quest/objective/type/builtin/AbilityObjectiveFilter.java`

## Checklist
- [ ] E1: `setTableVersion` only runs after successful ALTER TABLE
- [ ] E2: `clearRetryCountersForPlayer` wired to player disconnect
- [ ] E3: Javadoc documents offline limitation
- [ ] E4/E5: Events fire for offline players with nullable Player
- [ ] E6: Unrecognized policy logged at WARNING
- [ ] E7: FINE-level log on ability-not-found
- [ ] Compiles clean: `./gradlew clean compileJava`
