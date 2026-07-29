# Ability Design Rubric

A living toolkit for designing McRPG abilities: the kill-tests used to judge ideas and the generative prompts used to produce them. This file is owned and edited by the **designer, not AI** — an AI design partner may propose additions at the end of a workshop session, but nothing lands here without the designer approving and wording it. If a section stops being used, delete it; a rubric only works if it stays short enough to actually run.

Companion files: [`design-pillars.md`](design-pillars.md) (what each skill is *for* — judges fit), this file (judges mechanics), and the `/design-workshop` command (the session protocol that uses both).

---

## Kill-Tests

A kill-test is a one-sentence question that reliably kills a whole *class* of bad ideas. Run every surviving idea through all of them by name during the convergent phase.

| # | Test | The question | First kill | Known exceptions |
|---|------|-------------|------------|------------------|
| 1 | **Goalpost** | Does progression (skill level, tier, or gear) ever make this ability *worse* or harder to trigger? | Crescendo v1 — cap-gated bonus meant higher Swords level = harder to reach cap = worse uptime | — |
| 2 | **Redundancy** | Does it only function in the fight shapes where its function isn't needed? Check all four: mob swarm, 1v1 player, boss, kiting/ranged harass. | Killing Pace — kill-triggered shed freeze worked only in swarms, where frequent hits already made it unnecessary | — |
| 3 | **Potion** | Could a consumable do this? An ability whose payoff a potion can replicate is a consumable with extra steps. | Surging Rush — consumed Haste to grant literal Speed + Strength effects | — |
| 4 | **Feelability** | Is the payoff visible in a vanilla feedback channel — hearts, particles, the potion HUD, sound? Percentages under ~15% on Minecraft's damage scale are spreadsheet numbers, not payoffs. | Crescendo v2 — +5% damage is +0.35 on a diamond sword hit; imperceptible | — |
| 5 | **Decision** | Does using it ask *when*, or only *whether you can*? An ability that's always correct to press when available is a rotation, not a decision. | Surging Rush (partial) | Deliberate comfort picks are legitimate — some players want rhythmic, low-decision playstyles, and a kit should include one or two *on purpose*. The test's job is ensuring low-decision is chosen, not accidental. |

### Maintaining the tests

- **Birth rule:** a test is born when an idea dies for a reason that generalizes — if the one-sentence "why" would kill other ideas too, name it and add a row. Record the first kill; a test with no corpse attached is speculation.
- **Exception rule:** when a test fires and the designer's pushback generalizes, the pushback gets written into the Exceptions column. Exceptions are first-class — a test without its known exceptions over-kills.
- **Prune rule:** a test that hasn't fired in several workshop sessions gets reviewed for deletion.
- **Size cap:** ~12 tests. Past that it's a bureaucracy, not a checklist.

---

## Generative Prompts

Ranked by how much they actually matter. Compelling is a *feeling* — you iterate to it, you don't reason your way there — which is why the top of this list is playtesting, not technique.

1. **Playtest pain.** Design *at* the moments that feel bad in hand. After any ability lands, spend 30 minutes on a test server with a notepad; the notes are next session's prompt list.
2. **Reference + twist through constraints.** Steal a mechanic you love, then force it through McRPG's constraints until it deforms into something new. Precedent: Lethal Tempo + "no custom UI without resource packs" = Haste-as-the-meter — the constraint did the creative work.
3. **Highlight-clip-first.** Write the ten-second clip a player would share, then design backward from the moment. Not every ability needs a clip, but a kit with zero clips has no identity.
4. **Problem-first.** List the kit's bad moments — what does this skill's player feel anxious or bored about? — and aim ideas at the pain.
5. **The matrix.** When the well is dry, walk the grid below and force a two-minute sketch for every empty cell. Most sketches will be garbage; that's the point — it defeats the blank page and your own taste-ruts.

### Trigger × Payoff matrix (combat abilities)

**Triggers** (things the game can notice): land a hit · land a full-charge hit · kill · take a hit · shield block · weapon swap · consume a resource · reach a threshold (Haste band, HP%) · sprint/movement · crouch.

**Payoffs** (things an ability can give): damage · sustain/healing · mobility · defense · control (slow, knockback) · mana economy · resource manipulation (stacks, shed) · cleanse.

Mark which cells the current kit occupies before generating — the empty columns are where the un-obvious ideas live.

### Divergence rules

The skill of divergence is suppressing self-judgment, nothing more. When generating:

- Timer on: **15 minutes**.
- Target: **10 ideas minimum**, one line each.
- **Deleting is forbidden.** Editing is forbidden. Judging is for the convergent phase.
- It will feel stupid. The feeling of stupidity is the editor complaining that it's been unplugged — that's how you know it's working.
