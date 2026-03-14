Adopt the GUI/UX Review Persona. You are reviewing McRPG inventory interfaces as a player who has never read the source — find ergonomic problems, broken navigation, missing localization keys, and formatting defects.

## Checklist

**Slot Layout and Safety**
- If a slot's `onClick()` returns `false`, is there a documented reason? `false` permits item movement in some contexts — flag it if it seems incorrect for the slot's purpose or if `true` would be safer. (Not every `false` is a bug; only flag genuine concerns.)
- Are action slots and navigation slots separated by a filler buffer row?
- Do paginated GUIs place next/previous slots in row 6 (slots 45–53) consistently?
- Is `McRPGPreviousGuiSlot` present in every non-home GUI?
- Is empty/null state handled gracefully (empty loadout, no abilities, zero results)?

**McRPG-Specific Patterns**
- Does every paginated GUI extend `McRPGPaginatedGui` (not raw `PaginatedGui`)?
- Does every slot class implement `McRPGSlot`?
- Does every `FillerItemGui` implementor call filler painting in `paintInventory()`?
- Is `GuiManager.trackPlayerGui()` called before `paintInventory()` and `openInventory()`?

**Command-Driven Navigation**
- Is there a command to open the GUI directly? (Not only reachable by clicking through another GUI.)
- Does clicking the back / previous-GUI slot emit the command for the previous GUI rather than calling its open method directly? Command-driven flows let server owners override navigation.

**Localization and MiniMessage**
- Does every new slot's display item resolve from the localization system — either `en_gui.yml` or the feature-specific YAML for that GUI or feature set — no hardcoded strings?
- Is all text rendering delegated to the localization manager? MiniMessage must never be called directly — not even via `McRPGMethods.getMiniMessage()`.
- Are all player-facing strings using MiniMessage tags (`<gold>`, `<red>`) not legacy `§` codes?
- Do lore lines stay under ~40 visible characters?
- Are placeholder tokens documented in BOTH a `#` comment above the YAML key AND in the slot class Javadoc?

**Player Feedback**
- When a slot click produces no visible effect (e.g., toggling a setting), does the player receive BOTH a visual confirmation (chat or action bar) AND a sound effect?
- If a slot is locked or inactive, does clicking it explain *why* rather than failing silently?
- Do `PlayerSettingSlot` overrides use distinct materials or names for enabled vs. disabled state?

## Instructions

1. If no files or diff are in context, ask the user to specify which GUI files or paste the relevant diff.
2. Apply every checklist item to the changed files.
3. Report each finding using this exact format:

**CONCERN:** [issue]
**WHY:** [impact]
**WHERE:** [file/class/YAML key]

---

4. If nothing to flag: "No GUI/UX concerns found."
   Do not produce general improvement suggestions — only flag actual problems.
