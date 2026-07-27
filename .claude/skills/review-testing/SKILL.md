---
name: review-testing
description: "Reviews McRPG changes for test coverage gaps, unhandled edge cases, TimeProvider usage, and McRPGBaseTest/MockBukkit test structure. Invoke for a focused testing review of a diff or PR."
disable-model-invocation: true
---

# Testing Review

You are a test engineer reviewing whether this change is adequately tested and whether tests are structured correctly. Flag coverage gaps and structural problems — not style preferences.

## How to review

1. Identify the changes under review: use the diff already in context, or run `git diff` yourself (e.g. `git diff origin/recode...HEAD`).
2. Apply the checklist below to changed code only — read surrounding code as needed to confirm behavior, but do not audit unchanged code.
3. Verify every candidate finding against the actual code before reporting it. Drop anything you cannot confirm.

## Checklist

**Coverage Completeness**
- For every new public method with non-trivial logic (>3 lines), is there a corresponding test?
- For every ability component change, does a test cover both the pass branch and the fail branch of `shouldActivate()`?
- Are edge cases covered: empty collections, zero values, already-on-cooldown, invalid input? Do NOT flag missing null-input tests — null parameters are guarded by `@NotNull` annotations and are not expected runtime states.
- For config-driven values, is the code path tested with a value of `0` and at the maximum?
- If a bug was fixed, is there a regression test?
- Does the diff add non-Bukkit logic with zero corresponding test additions?

**Unhandled Edge Cases**
- Boundary values: is new logic tested with an empty collection, `0`, a negative value, and the maximum the type allows (`Integer.MAX_VALUE`, max tier, max level)? Mid-range "happy" inputs hide overflow and clamping bugs.
- Collaborator failure paths: when a dependency returns `Optional.empty()` (e.g., `getPlayer(uuid)` for an offline player), a registry lookup misses, or a DAO read finds no row, is that branch exercised — not just the found/success branch?
- State machine transitions: for stateful types (`QuestState`, `QuestChainState`, `BoardOffering` state, cooldown state), is there a test for an invalid transition — double-start, completing an already-`COMPLETED` instance, progressing a `CANCELLED` quest? Testing only the legal path leaves illegal transitions with undefined behavior.
- Off-by-one: for any range, pagination, loadout-slot-index, or per-tier loop, is the exact boundary tested — first index, last index, one-past-last, single-element page, exactly-full page?
- Parser formula edge cases: for any config formula evaluated via `Parser` (mana cost, cooldown, experience curves), is there a test where the formula divides by zero, produces a negative result (a `tier` high enough to push `base - scale*tier` below 0), or exceeds expected bounds? The minimum-cost floor only protects paths that are actually covered.
- Concurrent mutation during iteration: does tested code iterate a collection that a callback, event handler, or scheduled task can mutate mid-iteration (e.g., iterating loadout abilities while an activation modifies the loadout)? A test that mutates during iteration catches `ConcurrentModificationException` before a player does.
- Time boundaries: for cooldown/expiration checks, is the exact boundary instant tested (`now == expiresAt` via a fixed `TimeProvider`), not just clearly-before and clearly-after? Inclusive-vs-exclusive comparison bugs live exactly on the boundary.
- Config-driven edge cases: is the load path tested when a YAML key is missing, the value is an empty string, or a non-numeric string sits where a number is expected — not only against the bundled defaults?
- Ordering assumptions: does any test assert on iteration order of a `HashSet`/`HashMap`-backed collection? Such tests pass by accident of hashing; assert order-insensitively or use an ordered structure in production.

**TimeProvider Usage**
- Does any new or modified code call `System.currentTimeMillis()` or `Instant.now()` directly? All time-based logic must go through `TimeProvider` so tests can inject a fixed clock.
- Do tests that assert time-dependent behavior (cooldowns, duration abilities, rested experience timers) inject a mock or fixed `TimeProvider` rather than depending on wall-clock time?
- If a test modifies `TimeProvider` state, is that state reset in `@AfterEach` to prevent cross-test pollution?

**McRPG Test Structure**
- Do all tests requiring MockBukkit server interaction OR McRPGPlayer infrastructure extend `McRPGBaseTest`? Direct calls to `MockBukkit.mock()` / `MockBukkit.load()` outside of `McRPGBaseTest` are a structural violation.
- Are shared test helpers and fixtures placed in `src/testFixtures/java/` — not duplicated across test classes?
- Does any test call `MockBukkit.unmock()` in `@AfterEach`? `McRPGBaseTest` manages this at suite level; per-test unmocking corrupts shared state.
- Is `McRPGBaseTest.addPlayerToServer()` used when join-event side effects matter OR when simulating player behaviour on the server — not bare `PlayerMock` construction in those scenarios?
- When a test needs an `McRPGPlayer` instance, does it use `@ExtendWith(McRPGPlayerExtension.class)` with a method parameter instead of manually constructing the player and registering the `McRPGPlayerManager`? The extension handles creation, `spy()` wrapping, manager registration, and cleanup.

**Bukkit-Dependent vs. Pure-Java Separation**
- Does any class mix pure logic (math, data transformation) with Bukkit API calls, with only the pure logic tested? Extract the pure logic into a testable helper.
- Does any test extend `McRPGBaseTest` but use neither MockBukkit server interaction nor McRPGPlayer tracking? In that narrow case, a plain JUnit test would suffice — but this check only applies if truly neither is needed.
- **NOT a violation:** Tests that use simple Bukkit data classes (`NamespacedKey`, `Location`, `ItemStack`, `Material`) without extending `McRPGBaseTest` are valid — these classes work with MockBukkit on the test classpath and do NOT require `MockBukkit.mock()` or a running server. Do NOT flag these as needing `McRPGBaseTest`.

**MockBukkit Usage**
- Is Mockito used to mock a Bukkit class where MockBukkit already provides a real implementation (e.g., `PlayerMock`)? Use the MockBukkit implementation, not `@Mock`.
- Does any test that depends on join-event side effects or server-side player behavior use `server.addPlayer()` rather than constructing `PlayerMock` directly?

**Test Quality**
- Does every test method have at least one assertion (`assertEquals`, `assertNotNull`, `assertTrue`, `verify()`)? A test with no assertion cannot fail.
- Does every test method follow the `action_outcome_whenCondition` naming convention (e.g., `register_throwsIllegalArgumentException_whenSkillAlreadyRegistered`, `activate_appliesCooldown_whenAbilityFires`)? The `_whenCondition` suffix is optional when the context is obvious from the action and outcome alone. See `BaseAbilityTest` for a reference.
- Does every test method carry a `@DisplayName` annotation with a descriptive label that clearly communicates the test's intent (e.g., `@DisplayName("Given a registered key, when getting by key, then returns the statistic")`, `@DisplayName("throws when the manager is already registered")`)?
- Are time-dependent tests using the bootstrap-provided spy'd `TimeProvider` (via `McRPG.getInstance().getTimeProvider()` and `when(timeProvider.now()).thenReturn(...)`) rather than hand-rolling a `Clock` subclass or constructing a new `TimeProvider`? The spy is wired up in `TestBootstrap#getTimeProvider` for exactly this purpose.

### Known Infrastructure Guarantees (do NOT flag these)

The following patterns are correct by design. Flagging them produces false positives:

1. **`RegistryAccess.registryAccess().register()` overwrites existing entries.** Tests that call `register()` in `@BeforeEach` do NOT need `@AfterEach` cleanup — re-registering in the next test overwrites the previous entry. Do not flag missing cleanup for registry re-registration.

2. **`TestBootstrap` pre-wires mocked managers.** `McRPGLocalizationManager`, `TimeProvider`, `FileManager`, and others are already spy'd/mocked and wired into `RegistryAccess` by `TestBootstrap`. Tests that retrieve these managers from the registry and stub methods on them are correct — do not flag them as "constructing mocks instead of using the real implementation."

3. **`server.getScheduler().cancelTasks(plugin)` cancels ALL tasks for that plugin.** This is the correct way to reset scheduler state between tests in a `@BeforeEach`. It works regardless of which holder or object created the task. Do not flag it as incomplete cleanup.

4. **Simple domain classes instantiated fresh per test are fine.** Classes like `EntityManager`, `AbilityHolder`, `QuestHolder` are Map-based trackers that do not register Bukkit listeners or interact with global state. Creating a new instance per test provides complete isolation. Do not flag them as needing shared setup or MockBukkit integration.

5. **`McRPGPlayerExtension` creates a new spy'd player per test method.** Each test gets an isolated `McRPGPlayer` instance with its own UUID. The extension handles `McRPGPlayerManager` registration and cleanup. Do not flag tests using this extension as needing manual player management.

6. **`server.getPluginManager().clearEvents()` resets event history.** Tests that assert on fired events use `clearEvents()` in `@BeforeEach` to prevent cross-test pollution. This is the standard pattern — do not flag it as unnecessary.

## Reporting

When running interactively (not under the CI review orchestrator), report each confirmed finding as:
- **Where:** `path/File.java:line`
- **What:** the concern in one or two sentences
- **Why:** why it matters
- **Fix:** the suggested change

If nothing qualifies, say: "No testing concerns found in this diff."
