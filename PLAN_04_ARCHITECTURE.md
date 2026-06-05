# Plan 04 — Architecture Fixes (A1-A10)

## A1 — AvailabilityWindowChecker SRP
**Problem:** 485 lines covering too many concerns.
**Fix:** Addressed partially by Plan 01 (concurrency split). After that fix, evaluate if further extraction is needed. Grace period scheduling could become a collaborator if the class is still too large.
**Depends on:** Plan 01

## A2 — Raw Bukkit scheduler for grace periods
**Problem:** Uses `Bukkit.getScheduler().runTaskLater()` instead of `CancelableCoreTask`.
**Fix:** Replace with `DelayableCoreTask` or `CancelableCoreTask`. Store task references instead of raw Bukkit task IDs.
**Files:**
- `src/main/java/us/eunoians/mcrpg/quest/availability/AvailabilityWindowChecker.java`
**Depends on:** Plan 01

## A3 — 17-branch instanceof chain
**Fix:** Track for later. Adding `getContentKey()` to `McRPGContent` interface is an additive API change that should be its own PR.

## A4 — Duplicated suffix-stripping
**Fix:** Extract shared helper method. Low effort.
**Files:**
- `src/main/java/us/eunoians/mcrpg/command/admin/content/ContentKeysCommand.java`
- `src/main/java/us/eunoians/mcrpg/command/admin/content/ContentPacksCommand.java`

## A5 — QuestTemplate needs a builder
**Problem:** 5 telescoping constructors, 15 params.
**Fix:** Add `QuestTemplate.Builder` inner class per project convention. Deprecate old constructors.
**Files:**
- `src/main/java/us/eunoians/mcrpg/quest/board/template/QuestTemplate.java`
- All callsites constructing QuestTemplate

## A6 — retryCounters missing eviction Javadoc
**Fix:** Add Javadoc documenting: cleared per-chain on fail/restart, cleared per-player on disconnect, reset on server restart.
**Files:**
- `src/main/java/us/eunoians/mcrpg/quest/chain/QuestChainManager.java`

## A7 — QuestChainManager at 1178 lines
**Fix:** Extract `ChainExpirationHandler` collaborator containing `handleExpireFail`, `handleExpireRetry`, `handleExpireSkip`, `handleExpireRestartChain`, `clearRetryCountersForChain`, `clearRetryCountersForPlayer`, `clearCompletionLogAsync`, `logStepSkippedAsync`.
**Files:**
- `src/main/java/us/eunoians/mcrpg/quest/chain/QuestChainManager.java`
- New: `src/main/java/us/eunoians/mcrpg/quest/chain/ChainExpirationHandler.java`

## A8 — Mixed Instant/long in grantPendingRewards
**Fix:** Track for later. Requires migrating `PendingRewardDAO` to accept `Instant` — separate concern from chain backlog.

## A9 — getInstance() in content commands
**Fix:** Skip — consistent with all existing commands. Not a regression.

## A10 — Class name matching for content packs
**Fix:** Track for later — tied to A3.

## Checklist
- [ ] A2: Grace periods use CoreTask hierarchy (after Plan 01)
- [ ] A4: Shared suffix-stripping helper
- [ ] A5: QuestTemplate.Builder added
- [ ] A6: retryCounters Javadoc added
- [ ] A7: ChainExpirationHandler extracted
- [ ] Compiles clean: `./gradlew clean compileJava`
