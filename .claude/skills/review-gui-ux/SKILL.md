---
name: review-gui-ux
description: "Reviews McRPG inventory GUIs for slot ergonomics, navigation consistency, localization key completeness, command-driven flows, and player-facing feedback quality. Invoke for a focused GUI/UX review of a diff or PR."
disable-model-invocation: true
---

# GUI/UX Review

You are reviewing McRPG inventory interfaces as a player who has never read the source. Find ergonomic problems, confusing navigation, missing text, and broken formatting. Be direct — report concerns only, no general praise.

## How to review

1. Identify the changes under review: use the diff already in context, or run `git diff` yourself (e.g. `git diff origin/recode...HEAD`).
2. Apply the checklist below to changed code only — read surrounding code as needed to confirm behavior, but do not audit unchanged code.
3. Verify every candidate finding against the actual code before reporting it. Drop anything you cannot confirm.

## Checklist

**Slot Layout and Safety**
- If a slot's `onClick()` returns `false`, is there a documented reason? `false` allows item movement/theft in some contexts — flag it if the return value seems incorrect for the slot's purpose or if returning `true` would be safer. (Note: `false` can be intentional, so only flag when there is a genuine concern.)
- Are action slots and navigation slots (next/previous page, back) separated by a filler buffer row? Adjacency causes accidental clicks.
- Do paginated GUIs place `McRPGNextPageSlot` / `McRPGPreviousPageSlot` in row 6 (slots 45–53), consistent with all other paginated GUIs?
- Is `McRPGPreviousGuiSlot` present in every non-home GUI? Players must always be able to go back.
- Is empty/null state handled (empty loadout, no abilities unlocked, zero results) — does the player see a clear indicator rather than a blank GUI?

**McRPG-Specific Patterns**
- Does every paginated GUI extend `McRPGPaginatedGui` (not raw `PaginatedGui` from McCore)?
- Does every slot class implement the `McRPGSlot` marker interface?
- Does every GUI that uses filler implement `FillerItemGui` and call filler painting in `paintInventory()`?
- Is `GuiManager.trackPlayerGui()` called before `paintInventory()` and before `openInventory()`?

**Command-Driven Navigation**
- Is there a command to open the GUI directly (not only reachable via another GUI)? Direct commands let server owners override flows with custom UIs.
- Does clicking the back / previous-GUI slot emit the command to open the previous GUI rather than calling the previous GUI's open method directly? Emit the command, not the internal call.

**Localization and MiniMessage**
- Does every new slot's display item resolve via the localization system — either from `en_gui.yml` or from the feature-specific YAML for that GUI or feature set — never hardcoded strings?
- Is every player-facing string rendered through the localization manager? MiniMessage must never be called directly, even via `McRPGMethods.getMiniMessage()` — all text rendering must go through the localization manager so translations, overrides, and caching work correctly.
- Is every player-facing string using MiniMessage with the semantic palette placeholders (`<primary>`, `<body>`, `<hint>`, etc. — see `PALETTE.md`) — not legacy `§` color codes, and not deprecated raw tags like `<gold>`?
- Do lore lines stay under ~40 visible characters to avoid client-side truncation?
- Are placeholder tokens (e.g., `<skill-level>`, `<redeemable-experience>`) documented in BOTH a `#` comment above the YAML key AND in the slot class's Javadoc?

**Player Feedback**
- When a slot click produces no visible effect (e.g., toggling a setting), does the player receive BOTH a visual confirmation (chat or action bar) AND an audible sound effect?
- If a slot is locked or inactive, does clicking it explain *why* — not silently fail?
- Do `PlayerSettingSlot` overrides provide distinct materials or names for enabled vs. disabled toggle state?

### Do not flag
- Do not produce general improvement suggestions — only flag actual problems.

## Reporting

When running interactively (not under the CI review orchestrator), report each confirmed finding as:
- **Where:** `path/File.java:line`
- **What:** the concern in one or two sentences
- **Why:** why it matters
- **Fix:** the suggested change

If nothing qualifies, say: "No GUI/UX concerns found in this diff."
