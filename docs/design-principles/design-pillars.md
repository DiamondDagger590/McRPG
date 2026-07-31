# McRPG Design Pillars

A living record of what McRPG's combat design is *for*. The [design rubric](design-rubric.md) judges whether a mechanic is well-made; this file judges whether it belongs. Pillars are extracted through designer-led sessions (see `/design-workshop`), and this file is owned and worded by the **designer, not AI** — an AI partner may propose entries, but nothing lands here without the designer approving the wording.

Game-level pillars are few, stable, and expensive to change. Skill identity cards are cheaper — each new skill adds one, and only strains on the game tier reopen it.

---

## Game-Level Pillars

### 1. The loadout is the McRPG experience

Every player builds a loadout — there is no meaningful McRPG experience that avoids the loadout GUI, and none is being designed for. Loadouts mix abilities across skills freely, and **cross-skill synergy is intentional design surface**, not an accident to contain (e.g., Axes' Whirlwind Strike knocking a target away so Rage Spike can chase them down). Because the loadout is the substrate, loadout slots are where all opt-in complexity lives.

### 2. Complexity is opt-in; the baseline is "attack"

The mandatory layer of any skill — its innate — demands zero ongoing management. The baseline player's skill expression is "attack," supported by a simplistic yet moderately powerful set of abilities, **including simple actives that empower that style** — set-and-forget loadouts are a legitimate, complete way to play, not a degraded one. Derived requirement: every combat skill ships at least one genuinely low-decision active.

### 3. Every skill has a level-scaled innate

Each skill carries at least one innate ability that scales off the skill's level all the way through the cap, rewarding investment in the skill even from players who never spend a loadout slot on it.

### 4. Vanilla-native presentation

The base version of McRPG must never feel like "modded" Minecraft. Ability states and payoffs are read through vanilla channels — the potion HUD, hearts, particles, sounds — and the base experience carries no resource-pack or custom-UI dependency. (Expansion packs may opt into custom resources; the base may not.)

### 5. Every combat style answers shields

Modern vanilla combat is axes, crits, and shields — and shields render every other combat style inept. Without a per-skill anti-shield mechanic, shields stay THE meta and every player is mandated to carry an axe. So **each combat skill must have some anti-shield answer, expressed in its own flavor**: dashing around a shielding player, disabling a shield when a strong active is used, dealing heavy durability damage to win the attrition war by breaking the shield first, DOT effects that work around a shield, and so on. Axes are the exception that proves the rule — an axe deals with shields by nature, and that weapon-level perk is Axes' explicit differentiator.

### 6. Red lines

Things McRPG combat never becomes, regardless of how well-designed the ability proposing them is:

- **No combat style reducible to a single "perfect" macro press.** Resource engines (Haste bands, mana) exist partly to keep optimal play state-dependent — a rotation must branch on game state a macro can't read. This is about *full automation of a style*, not macro-assistability of an element: a design isn't banned just because one input in it could be timed by a macro, so long as perfect execution still requires human aim, targeting, and movement ("you can't click one macro to have perfect automated execution — you still have to hit the target"). Bare autoclicker abuse is an anticheat concern, not a design constraint.
- **No one-shotting a player in full diamond/netherite.** Hard ceiling on burst scaling.
- **No invisible CC.** Any control effect on a player requires a visual indicator (no freezing a player in place with nothing on screen).
- **No stale meta.** Steer away from designs that collapse loadout variety into one dominant answer. (Pillar 5 is this red line's first enforcement.)

---

## Skill Identity Cards

### Swords

- **Brag:** abilities that link together — a kit built around combos that feel fluid, or a rotation-heavy gameplay style.
- **Engine:** the Haste/momentum system *is* the combo fantasy's implementation — build stacks through sword tempo, read them off the vanilla potion HUD, spend them through consume actives. Haste state plus mana management is what keeps rotations reactive rather than macro-able.
- **The clip:** dash in, smack the opponent around, send them flying backwards, dash in again for another flurry of strikes; overwhelming an opponent with powerful ability rotations that require good micro management.
- **What it is not:** less focused on debuffing the opponent or raw damage than its neighbors.
- **Knockback boundary:** Swords knockback is *displacement in service of the combo* (send them flying → chase); *control* belongs to Axes.
- **Shields:** owes an in-flavor anti-shield answer (pillar 5). Note: Bleed's removal deleted the kit's only incidental shield workaround, so this is currently an open hole in the rework. Specific ability TBD.

### Axes (stub)

- Low attack frequency, high damage. Powerful attacks resulting in debuffs — an identity branching from the weapon's vanilla shield-disabling behavior — and can carry knockback/CC as genuine control.
- Anti-shield answer: the weapon itself.
- Sketched, uncommitted: Whirlwind Strike (heavy knockback).

### Unarmed (stub)

- Frequent attacks with low-to-moderate damage. A "tricky" playstyle focused on disarming an opponent (making them drop an item), debuffs, and debuff resistances. A jack-of-all-trades combat style.
- Anti-shield answer: TBD.

---

## Extraction Notes

Probes that missed, kept because the miss fixed the asker's model:

- The original complexity-floor question assumed a "player who never opens the loadout GUI" persona. The designer's correction — that persona doesn't exist, the GUI is the game — produced pillar 1 and reframed the complexity floor in units of *ongoing attention* rather than menu avoidance.
