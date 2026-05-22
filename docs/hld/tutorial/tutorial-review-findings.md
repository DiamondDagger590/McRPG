# Tutorial Quest System -- Persona Review Findings

> **Last Updated:** 2026-05-22
> **Status:** Unresolved -- address before or during implementation
> **Source:** Four-persona review of the tutorial system design (GUI/UX, Server Owner, Architecture, Extensibility)

These are the consolidated findings from the cross-persona review. Items are ordered by criticality. Each item includes the review persona that raised it, the concern, and a suggested resolution direction.

---

## Critical (address before implementation)

### 1. Event ownership for PreQuestStartEvent + QuestStartEvent
**Persona:** Architecture
**Concern:** Both events must fire from `QuestManager`, not from `QuestInstance.start()`. If an API consumer bypasses the manager and calls `QuestInstance.start()` directly, the pre-event is skipped. This is the same pattern as Bukkit's `BlockPlaceEvent` (fired by the handler, not by `Block.setType()`).
**Resolution:** `QuestInstance.start()` becomes a pure state mutation. `QuestManager.startQuest()` fires `PreQuestStartEvent`, checks cancellation, then delegates to `QuestInstance.start()`, then fires `QuestStartEvent`.

### 2. Auto-complete rapid-fire UX
**Persona:** GUI/UX
**Concern:** A veteran player who already has levels, abilities, and loadout entries could auto-complete Q1 through Q5 in rapid succession on first join. Getting 5 quest completion messages in 2 seconds is a terrible experience.
**Resolution:** Chain manager detects auto-complete cascades and batches them. On-start messages for auto-completed steps are suppressed. A single summary message is sent after the cascade settles, listing completed steps. Only the final non-auto-completed step's on-start message is delivered.

### 3. On-start rewards field on QuestDefinition -- backward compat
**Persona:** Architecture
**Concern:** Adding a field to `QuestDefinition` could break existing constructors (it's immutable with builder or all-args constructor). Need to ensure no breaking API change.
**Resolution:** Existing constructors preserved with new field defaulting to empty list. New constructors/factory methods added for callers that supply on-start rewards.

### 4. PreQuestStartEvent should carry McRPGPlayer, not just UUID
**Persona:** Extensibility
**Concern:** Third-party listeners frequently need the `McRPGPlayer` to check abilities, stats, permissions. Having to do the registry lookup in every listener is boilerplate-heavy and error-prone.
**Resolution:** Event carries both `Player` (Bukkit), `McRPGPlayer`, and `QuestSource`.

### 5. Chain state table migration
**Persona:** Server Owner
**Concern:** New SQL table needs `UpdateTableFunction` registration so it auto-creates on first startup. Also needs to be documented in the admin migration notes.
**Resolution:** `QuestChainStateDAO.attemptCreateTable()` registered in the existing `UpdateTableFunction` pipeline. Config version bumped.

---

## Important (address during implementation)

### 6. McRPGGuiOpenEvent fire point
**Persona:** Architecture
**Concern:** Firing from every GUI class (20+ classes) is unmaintainable. Needs a single centralized point.
**Resolution:** Fire from `GuiManager.trackPlayerGui()` which all GUI opens already pass through.

### 7. GuiOpenObjectiveType enum stability
**Persona:** Extensibility
**Concern:** Using a Java enum for `gui-type` means third-party plugins can't register custom GUIs. If someone adds a custom GUI, they can't make a tutorial step for it.
**Resolution:** Use `NamespacedKey` instead of enum. Bundled GUIs register with `mcrpg:home`, `mcrpg:loadout_selection`, etc. Third-party GUIs register their own keys. The `McRPGGuiOpenEvent` carries the GUI's `NamespacedKey`.

### 8. LoadoutAbilityEquipEvent centralization
**Persona:** Architecture
**Concern:** Loadout equip currently happens in multiple callsites (GUI slots, commands). Need a centralized method to avoid missed event fires.
**Resolution:** Introduce `LoadoutManager.equipAbility()` that wraps `Loadout` mutation + event firing. Retrofit all callsites.

### 9. Tutorial rewards too generous or too stingy
**Persona:** Server Owner
**Concern:** 4,300 boosted XP + 1,500 redeemable XP + 2 redeemable levels is a fixed value. Different server economies will disagree on whether this is too much or too little.
**Resolution:** All reward amounts are configurable in the quest YAML files. Server owners can override without modifying the chain structure. Document recommended ranges in a comment block.

### 10. Tutorial opt-out confirmation UX
**Persona:** GUI/UX
**Concern:** Toggling "Disable Tutorial" should show a confirmation because it permanently abandons the chain (no restart). Accidental toggle = missed rewards.
**Resolution:** Reuse existing `ConfirmationManager` pattern. Show a confirmation GUI with clear messaging about permanence.

### 11. Permission-based tutorial skip
**Persona:** Server Owner
**Concern:** Staff, alts, and test accounts should skip the tutorial without needing the player setting. Need a permission node.
**Resolution:** `mcrpg.tutorial.bypass` permission (default: op) checked in `QuestChainFirstJoinListener` before starting the chain.

### 12. Chain config hot-reload
**Persona:** Server Owner
**Concern:** If a server owner edits `chain.yml` or tutorial quest files, they'll expect `/mcrpg reload` to pick them up.
**Resolution:** `QuestChainRegistry` supports reload. On reload, active chains re-resolve their current step against the new definition. If the current quest was removed, advance to the first uncompleted step.

### 13. MessageRewardType describeForDisplay()
**Persona:** GUI/UX
**Concern:** Messages as rewards don't translate well to the Quest Detail GUI's reward slot lore. Showing the full MiniMessage string would look terrible.
**Resolution:** `describeForDisplay()` returns a short summary like `"<body>Instructional message"` rather than the full message list. The actual messages are only sent in chat.

### 14. Auto-complete statistic key resolution
**Persona:** Architecture
**Concern:** Auto-complete checks need to know which statistics correspond to ability unlocks, combo casts, etc. These aren't currently tracked as statistics in all cases.
**Resolution:** Ability unlock can be checked by scanning `AbilityData` for unlocked attributes. Combo activations are tracked by `ActiveAbility.getActivationStatisticKey()`. Loadout equip state can be checked by scanning `LoadoutHolder.getLoadout()`.

### 15. Quest source abandonability enforcement
**Persona:** GUI/UX
**Concern:** `TutorialQuestSource.isAbandonable()` returns `false`, but the abandon confirmation GUI still needs to handle this gracefully (show a deny message, not just silently ignore the click).
**Resolution:** `QuestAbandonConfirmGui` already checks `isAbandonable()`. Add a `<body>Tutorial quests cannot be abandoned.` lore line and deny sound + action bar feedback.

---

## Lower Priority (address in polish pass)

### 16. Chain YAML validation at load time
**Persona:** Server Owner
**Concern:** If a chain references a quest definition that doesn't exist, it should fail loudly at startup with a descriptive error, not silently skip.
**Resolution:** `QuestChainConfigLoader` validates all quest references against `QuestDefinitionRegistry` at load time. Missing references logged as `SEVERE` with the chain key and missing quest key.

### 17. Tutorial quests in Active Quest GUI ordering
**Persona:** GUI/UX
**Concern:** Tutorial quests should appear prominently in the Active Quest GUI, not buried among board quests.
**Resolution:** `ActiveQuestGui` sorts by source priority. `TutorialQuestSource` gets a high priority value so tutorial quests appear first.

### 18. Chain state admin commands
**Persona:** Server Owner
**Concern:** Server owners need a way to reset or advance a player's chain state for testing and support.
**Resolution:** `/mcrpg admin tutorial reset <player>` and `/mcrpg admin tutorial advance <player>` commands gated behind `mcrpg.admin.tutorial.reset`.

### 19. QuestChainContentPack registration order
**Persona:** Extensibility
**Concern:** Chains reference quest definitions, so chains must load after all quest definitions are registered.
**Resolution:** Document and enforce load order: `QuestContentPack` -> `QuestChainContentPack`. Content expansion `getExpansionContent()` returns packs in order; the handler pipeline processes them sequentially.

### 20. Tutorial quest visual treatment in Active Quest GUI
**Persona:** GUI/UX
**Concern:** Tutorial quests should be visually distinct from regular quests.
**Resolution:** `TutorialQuestSource` provides a distinct material (`KNOWLEDGE_BOOK`) and lore prefix. The overview slot checks `source` to apply treatment.

### 21. Chain state cleanup on quest definition deletion
**Persona:** Architecture
**Concern:** If a tutorial quest is removed from the definitions, chain state referencing it becomes orphaned.
**Resolution:** Chain reload resolves current quest against new definition. If missing, advance to next uncompleted step. If all steps completed, mark chain as COMPLETED.

### 22. Time-gate condition timezone handling
**Persona:** Server Owner
**Concern:** `TimeGateChainCondition` uses ISO-8601 with timezone, but server owners will mess up timezones.
**Resolution:** Default to server timezone when no offset specified. Document clearly in the config comments.

### 23. MessageRewardType placeholder support
**Persona:** Extensibility
**Concern:** Messages should support standard placeholders like `<player>`, `<quest>`, `<skill>`, etc.
**Resolution:** `MessageRewardType.grant()` builds a placeholder map from the grant context and passes it through `McRPGLocalizationManager`'s placeholder resolution.

### 24. Chain lifecycle events for third-party hooks
**Persona:** Extensibility
**Concern:** Third-party plugins may want to react to chain start, step advance, and chain complete.
**Resolution:** Fire `QuestChainStartEvent`, `QuestChainStepAdvanceEvent`, `QuestChainCompleteEvent`. All extend `Event` and carry the chain definition, player, and relevant state.

### 25. Tutorial quest locale key organization
**Persona:** Architecture
**Concern:** Tutorial messages need dedicated locale keys. Where do they go?
**Resolution:** New section in `LocalizationKey.java` under `TUTORIAL_HEADER`. Keys like `tutorial.first-steps.welcome`, `tutorial.explore-menu.hint`, etc. Entries added to `en_quest.yml`.

### 26. Admin tutorial reset -- should it re-grant rewards?
**Persona:** Server Owner
**Concern:** If an admin resets a player's tutorial chain, do they get rewards again on re-completion?
**Resolution:** Reset clears chain state only. Quest completion log entries for tutorial quests are also cleared. Player gets rewards again on re-completion. This is intentional for testing, and the command is admin-only.
