# Chain Backlog Audit Results

Generated: 2026-06-04

---

## Concurrency (3 Critical, 2 High, 1 Medium)

All 6 findings stem from `AvailabilityWindowChecker` running async but performing main-thread-only operations.

**Root cause fix:** Split into async computation phase (determine transitions) + main-thread callback (fire events, mutate state, iterate players).

### C1 — Critical: Bukkit event fired from async thread
`expireActiveChainInstances()` fires `QuestChainExpireEvent` from async thread. Bukkit events must fire on main thread.
**Where:** `AvailabilityWindowChecker.expireActiveChainInstances()`

### C2 — Critical: Non-volatile state mutation from async thread
Same method mutates `QuestChainPlayerState` fields (non-volatile) from async thread while main thread reads them.
**Where:** `AvailabilityWindowChecker.expireActiveChainInstances()`

### C3 — Critical: QuestInstance.cancel() from async thread
`cancelActiveQuestInstances()` calls `QuestInstance.cancel()` from async thread, triggering cascading Bukkit events.
**Where:** `AvailabilityWindowChecker.cancelActiveQuestInstances()`

### C4 — High: HashMap accessed from multiple threads
`activeGraceTasks` is a `HashMap` written from async thread and read/cleared from main thread.
**Where:** `AvailabilityWindowChecker.activeGraceTasks`

### C5 — High: Player collection iterated from async thread
Iterates `playerManager.getAllPlayers()` from async thread while main thread modifies the player set on join/quit.
**Where:** `AvailabilityWindowChecker.expireActiveChainInstances()`

### C6 — Medium: Method called from both thread contexts
`expireActiveChainInstances()` is called from both async timer and main-thread grace callbacks with no thread guard.
**Where:** `AvailabilityWindowChecker.expireActiveChainInstances()` / `cancelActiveQuestInstances()`

---

## Architecture (10 findings)

### A1 — AvailabilityWindowChecker has too many responsibilities
485 lines covering chain transitions, quest transitions, grace scheduling, startup reconciliation, and eviction.
**Where:** `AvailabilityWindowChecker`

### A2 — Raw Bukkit scheduler for grace periods
Uses `Bukkit.getScheduler().runTaskLater()` instead of `CancelableCoreTask` (violates anti-pattern).
**Where:** `AvailabilityWindowChecker.startChainGracePeriod()`

### A3 — 17-branch instanceof chain in describeContent()
Not open for extension. Every new content type requires a code change.
**Where:** `ContentKeysCommand.describeContent()`

### A4 — Duplicated suffix-stripping logic
"ContentPack" suffix stripping duplicated across two commands.
**Where:** `ContentKeysCommand.stripContentPackSuffix()` / `ContentPacksCommand.getPackTypeName()`

### A5 — QuestTemplate needs a builder
5 telescoping constructors (15 params). Project convention requires a builder at 6+ params.
**Where:** `QuestTemplate`

### A6 — retryCounters missing eviction Javadoc
No Javadoc documenting eviction strategy (violates anti-pattern).
**Where:** `QuestChainManager.retryCounters`

### A7 — QuestChainManager at 1178 lines
Expire handlers should extract into a `ChainExpirationHandler` collaborator.
**Where:** `QuestChainManager` — `handleExpire*`, `clearRetryCounters*`, async DB methods

### A8 — Mixed Instant and raw long in grantPendingRewards
`PendingRewardDAO` boundary still uses raw longs after the Instant migration.
**Where:** `QuestInstance.grantPendingRewards()`

### A9 — McRPG.getInstance() in content commands
Uses singleton where instance injection was possible. Consistent with existing pattern, minor.
**Where:** `ContentExpansionsCommand`, `ContentPacksCommand`, `ContentKeysCommand`

### A10 — Class name matching for content packs
`sendKeyList()` matches packs by `getClass().getSimpleName()` — soft reflection.
**Where:** `ContentKeysCommand.sendKeyList()`

---

## Error Handling (7 findings)

### E1 — High: DB migration bumps version on ALTER TABLE failure
Version incremented even when ALTER TABLE fails. Column never added on retry.
**Where:** `QuestChainCompletionLogDAO.attemptUpdateTable()` / `QuestChainStateDAO.attemptUpdateTable()`

### E2 — Medium: retryCounters eviction not wired
`clearRetryCountersForPlayer` exists but no visible listener wiring calls it on disconnect.
**Where:** `QuestChainManager`

### E3 — Medium: Offline players missed by reconcileOnStartup
`reconcileOnStartup()` only processes online players. Offline chains stay ACTIVE indefinitely.
**Where:** `AvailabilityWindowChecker`

### E4 — Medium: handleExpireRetry silently skips event for offline players
`QuestChainStepRetryEvent` never fires for offline retries, no log.
**Where:** `QuestChainManager.handleExpireRetry()`

### E5 — Medium: handleExpireRestartChain same silent skip
`QuestChainRestartEvent` silently dropped for offline players.
**Where:** `QuestChainManager.handleExpireRestartChain()`

### E6 — Low: WindowClosePolicy.fromString() swallows parse failure
Returns `Optional.empty()` on unrecognized string with no diagnostic.
**Where:** `WindowClosePolicy.fromString()`

### E7 — Low: resolveAbilityName removed warning log
Quiet `registered()` check returns raw key without logging for unexpected cases.
**Where:** `AbilityObjectiveFilter.resolveAbilityName()`

---

## Extensibility (12 findings)

**Breaking change risk: MEDIUM** — `long` to `Instant` migration across public APIs without deprecation bridges.

### X1-X8 — Breaking signature changes (acceptable if no third-party addons exist yet)
- X1: `QuestChainStartCondition.evaluate()` gained mandatory `Instant` param
- X2: `QuestChainPlayerState.getLastCompletedAt()` changed `Optional<Long>` to `Optional<Instant>`
- X3: `QuestInstance` timestamp accessors changed from `long` to `Instant`
- X4: `QuestChainStep` record gained `previewItem` component
- X5: `CompletionRecord.completedAt` / `ChainCompletionRun.completedAt` changed to `Instant`
- X6: `getCompletedQuestKeys()` renamed to `getNonSkippedCompletedQuestKeys()`
- X7: `McRPGLocalizationManager.formatDisplayDate()` param changed to `Instant`
- X8: `QuestPool` constructor gained mandatory `TimeProvider` param

### X9 — QuestChainRestartEvent requires @NotNull Player
Offline restarts silently invisible to addon listeners. Inconsistent with QuestChainFailEvent/QuestChainExpireEvent which accept nullable.
**Where:** `QuestChainRestartEvent`

### X10 — QuestChainStepRetryEvent same @NotNull Player inconsistency
**Where:** `QuestChainStepRetryEvent`

### X11 — softDisableAbility fires AbilityUnregisterEvent
Addons can't distinguish soft-disable from permanent removal.
**Where:** `AbilityRegistry.softDisableAbility()`

### X12 — describeContent() falls back to class name for unknown types
Third-party content types show raw `getClass().getSimpleName()`.
**Where:** `ContentKeysCommand.describeContent()`

---

## Testing (13 coverage gaps)

Production files: 63 | Test files: 16 | New production files: 23 | New test files: 1

### T1-T7 — Availability window subsystem has zero tests
- T1: `AvailabilityConfig.isCurrentlyAvailable()` — central decision method
- T2: `AvailabilityWindowDefinition.isActive()` — year-wrapping logic
- T3: `WindowBoundary.Fixed/Recurring.toZonedDateTime()` — foundation of window system
- T4: `TimeGateCondition.evaluate()` — timezone conversion edge cases
- T5: `TimeGateChainConditionType.parse()` — 3 validation paths
- T6: `WindowClosePolicy.fromString()` — enum parsing with normalization
- T7: `FixedWindowBoundaryType.parse()` / `RecurringWindowBoundaryType.parse()` — config deserialization

### T8 — QuestPool.filterByAvailability() untested
QuestPoolTest modified but no new test for availability filtering.
**Where:** `QuestPoolTest`

### T9 — Expire handlers untested
`handleExpireRetry/Skip/RestartChain` — ~150 lines of branching logic.
**Where:** `QuestChainManager` / `QuestChainManagerTest`

### T10 — AbilityRegistry soft-disable untested
`softDisableAbility()/reEnableAbility()` — new public API with event firing + multi-map bookkeeping.
**Where:** `AbilityRegistry`

### T11 — ContentExpansionManager introspection untested
`getRegisteredExpansions()/getContentPacks()` — new public API methods.
**Where:** `ContentExpansionManager`

### T12 — QuestManager.warnStaleDefinitions() untested
Iteration logic over active quests and definition map lookups.
**Where:** `QuestManager`

### T13 — Tests use Instant.now() directly (low severity)
Convention violation in 5 test instances. Tests don't assert time-dependent behavior so impact is minimal.
**Where:** `QuestChainCompletionLogDAOTest`, `QuestCompletionLogDAOTest`, `QuestInstanceDAOTest`, `QuestChainManagerTest`

---

## GUI/UX (4 findings)

### G1 — MiniMessage rendering bug in GatedChainStepSlot
`onClick()` sends raw String via `resolvePaletteColors()` instead of Component — players see unrendered MiniMessage markup.
**Where:** `GatedChainStepSlot` line 69

### G2 — Dead localization key
`CHAIN_PREVIEW_REWARDS_HIDDEN` registered but never referenced in Java code.
**Where:** `LocalizationKey` / `en_quest.yml`

### G3 — GatedChainStepSlot never instantiated
No GUI class constructs this slot — dead code.
**Where:** `GatedChainStepSlot`

### G4 — previewItem branch bypasses localization
`step.previewItem()` path returns cloned ItemStack with no palette/localization resolution.
**Where:** `GatedChainStepSlot.getItem()` lines 97-98

---

## Priority Summary

| Priority | Items |
|----------|-------|
| **Fix now** | C1-C6 (thread safety — one root cause), E1 (DB migration) |
| **Fix before merge** | T1-T8 (availability tests), G1 (MiniMessage bug), X9-X10 (event nullable consistency) |
| **Address soon** | A5 (QuestTemplate builder), A7 (extract ChainExpirationHandler), T9-T10 (more tests) |
| **Track for later** | A3-A4, X1-X8 (breaking changes OK if no addons), E3, remaining items |
