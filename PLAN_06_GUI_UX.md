# Plan 06 — GUI/UX Fixes (G1-G4)

## G1 — MiniMessage rendering bug
**Problem:** `GatedChainStepSlot.onClick()` sends raw String via `resolvePaletteColors()` — players see unrendered MiniMessage markup in chat.
**Fix:** Change to `localizationManager.getLocalizedMessageAsComponent(mcRPGPlayer, LocalizationKey.CHAIN_PREVIEW_LOCKED_TITLE, placeholders)` and use `player.sendMessage(Component)`.
**Files:**
- `src/main/java/us/eunoians/mcrpg/gui/quest/chain/slot/GatedChainStepSlot.java`

## G2 — Dead localization key
**Problem:** `CHAIN_PREVIEW_REWARDS_HIDDEN` registered in LocalizationKey and en_quest.yml but never referenced.
**Fix:** Either wire it into `GatedChainStepSlot.buildDefinitionPreview()` as a lore line (parallel to objectives-hidden), or remove the dead key. Decide based on design intent.
**Files:**
- `src/main/java/us/eunoians/mcrpg/gui/quest/chain/slot/GatedChainStepSlot.java` (if wiring in)
- `src/main/java/us/eunoians/mcrpg/configuration/file/localization/LocalizationKey.java` (if removing)
- `src/main/resources/localization/english/en_quest.yml` (if removing)

## G3 — GatedChainStepSlot never instantiated
**Problem:** No GUI class constructs this slot — it's dead code.
**Fix:** This slot was likely created for future use in a chain preview GUI. Either:
1. Wire it into `QuestChainHistoryDetailGui` for gated/future steps, OR
2. Leave as-is with a Javadoc note that it's prepared for the chain preview GUI

## G4 — previewItem branch bypasses localization
**Problem:** When `step.previewItem()` is non-null, the cloned ItemStack bypasses palette resolution.
**Fix:** Run `resolvePaletteColors()` on the preview item's display name and lore before returning.
**Files:**
- `src/main/java/us/eunoians/mcrpg/gui/quest/chain/slot/GatedChainStepSlot.java`

## Checklist
- [ ] G1: onClick sends Component, not raw String
- [ ] G2: Decision made on dead key (wire in or remove)
- [ ] G3: Decision made (wire into GUI or document as future)
- [ ] G4: previewItem path resolves palette colors
- [ ] Compiles clean: `./gradlew clean compileJava`
