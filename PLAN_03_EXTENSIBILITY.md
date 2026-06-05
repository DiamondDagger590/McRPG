# Plan 03 — Extensibility Fixes (X1-X12)

## X1-X8 — Breaking signature changes
**Decision needed:** These are all part of the `long` to `Instant` migration. The chain system is brand new (tutorial branch). If no third-party addons exist yet, these are acceptable without deprecation bridges.
**Action:** No code change needed — document as intentional in PR description. If addons do exist, add deprecated bridge methods.

## X9 — QuestChainRestartEvent requires @NotNull Player
**Problem:** Offline restarts silently invisible to addon listeners. Inconsistent with `QuestChainFailEvent`/`QuestChainExpireEvent` which accept nullable.
**Fix:** Change constructor to accept `@Nullable Player`. Add `Optional<Player> getPlayer()` getter. Update `QuestChainManager.handleExpireRestartChain()` to always fire event.
**Files:**
- `src/main/java/us/eunoians/mcrpg/event/quest/chain/QuestChainRestartEvent.java`
- `src/main/java/us/eunoians/mcrpg/quest/chain/QuestChainManager.java`

## X10 — QuestChainStepRetryEvent same @NotNull issue
**Fix:** Same as X9.
**Files:**
- `src/main/java/us/eunoians/mcrpg/event/quest/chain/QuestChainStepRetryEvent.java`
- `src/main/java/us/eunoians/mcrpg/quest/chain/QuestChainManager.java`

## X11 — softDisableAbility fires AbilityUnregisterEvent
**Problem:** Addons can't distinguish soft-disable from permanent removal.
**Fix:** Either add a `boolean isSoftDisable()` method to `AbilityUnregisterEvent`, or fire a new `AbilitySoftDisableEvent` instead. Prefer the boolean flag on the existing event to avoid proliferating event types.
**Files:**
- Event class for AbilityUnregisterEvent (in McCore or McRPG depending on location)
- `src/main/java/us/eunoians/mcrpg/ability/AbilityRegistry.java`

## X12 — describeContent() falls back to class name
**Problem:** Third-party content types show raw `getClass().getSimpleName()`.
**Fix:** Track for later — tied to A3. Adding `getContentKey()` to `McRPGContent` interface would fix both.

## Checklist
- [ ] X9: `QuestChainRestartEvent` accepts `@Nullable Player`
- [ ] X10: `QuestChainStepRetryEvent` accepts `@Nullable Player`
- [ ] X9/X10: Manager always fires events regardless of player online status
- [ ] X11: Decision made on soft-disable event distinguishability
- [ ] X1-X8: Documented as intentional in PR description
- [ ] Compiles clean: `./gradlew clean compileJava`
