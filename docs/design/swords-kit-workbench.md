# Swords Kit Workbench

Working notes for the Swords rework — the designer's iteration surface, not a spec. Everything here is provisional until it graduates to a treatment and then an LLD. Owned and edited by the designer; sessions append, the designer prunes.

Context: Bleed and its family (Enhanced Bleed, Deeper Wound, old Vampire, Serrated Strikes) are removed. The ramping Haste engine is Swords' core identity; Haste itself is the cross-skill currency (see `../design-principles/design-pillars.md` and the superseded-Phase-4 note in `../hld/combat/combat-tracker-and-ramping-frenzy.md`).

---

## Settled architecture

- **Ramping Frenzy (innate):** sword hits build stacks, stacks map to Haste bands, idle decays one stack per shed interval. Truly innate — no tiers, no unlock. Both knobs scale off `swords_level` across the full 1–1000 range: max stacks ~6 → 15 (Haste V band opening around ~930), shed interval continuous ~0.9s → 1.5s. Zero ongoing management; the vanilla potion HUD is the meter.
- **Haste is the currency, stacks are Swords-internal bookkeeping.** Consume abilities read/spend the Haste *level* (I–V), not the stack count. Core-level consume service + `PlayerHasteConsumeEvent`; Ramping Frenzy listens and zeroes its session stacks on consume. Any skill can ship generators or spenders.
- **External Haste seeds the ramp** (beacons, potions, Super Breaker) via the resolver floor — floor is the *bottom* of the band, and self-applied Haste is excluded from the floor (see the old Phase 4 LLD in git history for the deadlock analysis).
- **Soft links over hard links:** abilities combo through shared engine state, never by naming each other. The Bleed-family deletion cascade is the standing argument.
- **Sword→axe window is protected behavior:** the shed does *not* pause while holding a non-sword; riding Haste on an axe is a deliberate decaying-window play.

## Settled kit (pre-workshop candidates)

| Ability | Type | One-liner | Notes |
|---|---|---|---|
| Ramping Frenzy | Innate | Hit to build, idle to shed, Haste as the meter | Settled above |
| Rage Spike | Active | Existing dash, unchanged in feel | **+** each enemy struck during the dash grants a stack |
| Vampire (reborn) | Passive | Sword hits heal, scaled by current Haste band | Watch the loop with "healing grants stacks" idea below — needs a cap |
| Crescendo | Passive | While at Haste band X, hits deal bonus damage | **OPEN:** %-damage payoff concern — basically Strength, unfeelable at MC numbers. Redesign undecided (crit-particle direction was floated, not chosen) |
| Reckless Momentum | Passive | While inside the rebuild window opened by consuming, hits grant 2 stacks | No cooldown; new window replaces active window |
| Frenzy Strike | Active (spender) | Consume all Haste → next melee hit within ~5s deals bonus damage scaled by consumed level | Item-agnostic — the empowered hit can be an axe swing |
| Purging Frenzy | Active (spender) | Consume all Haste → reduce remaining negative effect durations 25–100% by consumed level; short immunity at top end | Designer's version |

## Matrix session batch — statuses

From the trigger × payoff matrix walk (see the Sheets matrix). Kill-test results and rulings from the convergent session:

| # | Sketch | Status | Notes |
|---|---|---|---|
| 1 | Knocked back by CC ability → gain Resistance scaled by Haste band | Survivor | Fix the fantasy to own its engine-dependence (Resistance scales off Haste ⇒ not actually skill-agnostic). Value grows as more skills ship displacement; mirror-match wrinkle: opponent's Rage Spike charges it |
| 2 | Healing grants RF stacks | Survivor | Cap the Vampire feedback loop. Only sketch directly feeding the rider archetype |
| 3 | Opponent equipment breaks → stacks instantly max | **Dead** | Killed by the dead-letter test (its founding corpse) |
| 4 | CC'd → gain RF stacks | Survivor | Coexists with #1: different archetype (ramp-conversion vs defense-conversion), passives carry half budgets |
| 5 | Shed frozen while crouching, drains mana | Survivor — **feel test** | Question: does crouch-hold read as a stance (deliberate, tense) or an exploit (crouch-spam bookkeeping)? |
| 6 | Shed frozen briefly after shield block | Survivor — **feel test** | Question: does block-freeze read at all without a custom indicator? Competes with #5 for the shed-management niche |
| 7 | Stacks naturally shedding → restore mana | Survivor | Deliberate floor-raiser for less consistent players. **Number as a floor:** exchange rate must stay below the mana cost of building the stacks, or it becomes tempo→mana arbitrage |
| 8 | Perfectly-timed attack at Haste level → gain mana | Survivor | Red-line challenge resolved: still requires human aim/targeting; refined red line now in pillars |
| 9 | Active hits at Haste levels → mana | Survivor (converted) | Converted from regen-rate buff to instant mana per designer. **Open ruling:** relationship to #8 (two passives / one with two clauses / one subsumes). Rate-modifier payoffs stay on the axis until they die a second time (prune-by-corpse-count) |
| 10 | Opponent blocks → bonus shield durability damage by Haste level | Survivor — anti-shield mandate | mcMMO armor-grind precedent: players learn break timing. Possible T4/T5 rider: mana on hitting blockers (legitimate per tier definition — tiers may change mechanics). Merge deferred until passive budget framework exists |
| 11 | Active: consume stacks → AoE shield disable, duration by count | **Parked (actives lot)** | Deliberate thought experiment; strong third spender candidate ("siege-break" verb) |

## Open rulings

- #8 vs #9 relationship (merge, coexist, subsume)
- Greybox cut: which 5–6 survivors get built rough for feel testing
- Crescendo payoff redesign
- Combo-identity gap: nothing in the batch *directly rewards sequences* — the combo half of the Swords identity is only fed indirectly (shed-freezes as combo glue). Standing generation prompt for a future session.

## Parking lot

- **AoE shield-disable spender** (#11) — third consume verb: damage (Frenzy Strike) / cleanse (Purging Frenzy) / siege-break
- Misfiled ideas for other skills go here with a "for Axes / for Unarmed" tag as they appear

## Owed frameworks

- **Passive power budget** — second need confirmed (#4's half-budget premise, #10's merge question). To be built via the budget lesson: currency enumeration + pairwise benchmark ladder, designer-drafted, after treatments produce real numbers to calibrate against.
