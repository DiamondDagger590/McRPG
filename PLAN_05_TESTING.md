# Plan 05 — Testing Gaps (T1-T13)

## T1-T7 — Availability window subsystem (zero tests)
All pure-Java, no Bukkit dependency needed. Plain JUnit tests.

### T1: AvailabilityConfig.isCurrentlyAvailable()
- Test with window currently open → returns true
- Test with window currently closed → returns false
- Test with multiple windows, one open → returns true
- Test with empty window list → returns false (or true depending on design)

### T2: AvailabilityWindowDefinition.isActive()
- Normal range (March 1 - March 31) — inside and outside
- Year-wrapping range (December 1 - January 31) — inside Dec, inside Jan, outside Feb
- Exact boundary equality

### T3: WindowBoundary.Fixed/Recurring.toZonedDateTime()
- Fixed: converts to expected ZonedDateTime
- Recurring: yearOffset arithmetic produces correct year
- Recurring: different timezones

### T4: TimeGateCondition.evaluate()
- Before gate → false
- After gate → true
- Exact boundary
- Different timezone from UTC

### T5: TimeGateChainConditionType.parse()
- Missing `after` field → IllegalArgumentException
- Invalid date format → IllegalArgumentException
- Invalid timezone → IllegalArgumentException
- Happy path

### T6: WindowClosePolicy.fromString()
- Known values (case insensitive)
- Kebab-case normalization (`expire-active` → `EXPIRE_ACTIVE`)
- Unknown value → Optional.empty()

### T7: FixedWindowBoundaryType.parse() / RecurringWindowBoundaryType.parse()
- Missing fields → Optional.empty()
- Invalid format → Optional.empty()
- Happy path for each

## T8 — QuestPool.filterByAvailability()
- Template with closed window filtered out
- Template with open window kept
- Template with no availability config kept
**File:** `src/test/java/us/eunoians/mcrpg/quest/board/generation/QuestPoolTest.java`

## T9 — Expire handlers in QuestChainManager
- handleExpireRetry: retry counter incremented, step quest restarted, event fired
- handleExpireRetry: max retries exceeded → falls through to fail
- handleExpireSkip: step skipped, log entry created, chain advanced
- handleExpireRestartChain: chain restarted, completion log cleared, event fired
**File:** `src/test/java/us/eunoians/mcrpg/quest/chain/QuestChainManagerTest.java`

## T10 — AbilityRegistry soft-disable
- softDisableAbility: removes from abilities map, fires AbilityUnregisterEvent
- reEnableAbility: adds back, ability functional again
- getSoftDisabledAbilities: returns correct set
**File:** New test or add to existing AbilityRegistry test

## T11 — ContentExpansionManager
- getRegisteredExpansions: returns registered expansions
- getContentPacks: returns packs, handles null
**File:** New test

## T12 — QuestManager.warnStaleDefinitions()
- No active quests → no warnings
- Active quest with valid definition → no warning
- Active quest with missing definition → warning logged
**File:** Existing QuestManager test

## T13 — Instant.now() in tests (low severity)
- Replace with injected TimeProvider where applicable
- Low priority — tests don't assert on timestamps

## Checklist
- [ ] T1-T7: Availability window test class created
- [ ] T8: QuestPool availability test added
- [ ] T9: Expire handler tests added to QuestChainManagerTest
- [ ] T10: AbilityRegistry soft-disable tests added
- [ ] T11: ContentExpansionManager tests added
- [ ] T12: warnStaleDefinitions test added
- [ ] All tests pass: `./gradlew clean compileJava` (test suite has pre-existing failures)
