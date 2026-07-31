# Game Balance Primer

Reference curriculum, not project decisions — mentor-authored crash-course notes on game balance and power budgeting, kept here so the concepts survive between design sessions. The designer-owned documents ([design-rubric.md](design-rubric.md), [design-pillars.md](design-pillars.md)) apply these ideas to McRPG; this file just holds the ideas. Annotate freely; replace sections with better wording as understanding grows.

---

## I. What balance is

- **Balance is not equality — it's the absence of dead choices and mandatory choices.** A system is balanced when multiple options stay viable at high-level play and none is dominant (Sirlin). Every option should be *picked sometimes and skipped sometimes*: an ability nobody takes is dead weight, an ability everybody takes is a tax.
- **The unit of balance is the choice, not the ability.** An ability has no intrinsic power level — only power relative to the alternatives at its decision point (loadout screen, mid-fight press). "How strong should X be?" is unanswerable alone and instantly answerable as "compared to what it competes with?"
- **Deliberate slight imbalance is healthy** ("perfect imbalance"). A perfectly flat meta is sterile; small imbalances drive discovery and meta churn. The goal is bounded imbalance: nothing outside the viable band, plenty of texture inside it.

## II. The seven currencies

Lenses that apply to any ability in any game:

1. **Expected value (EV):** magnitude × frequency. The universal first-order estimate. A 100-damage proc at 1% is a 1-damage-per-hit ability in a costume.
2. **Opportunity cost:** the true price of a pick is the best forgone alternative. Slot-limited systems price primarily through the slot — *but only while demand exceeds supply*. Scarcity is the engine; if live options don't outnumber slots, opportunity cost prices nothing (see §V).
3. **Delivery shape — sustained vs. burst:** equal-EV effects are not equal. Burst front-loads power and opens kill windows (worth more than its EV in kill-oriented PvP); sustained accumulates (worth more in attrition PvE). Spikiness is itself a currency.
4. **Reliability vs. variance:** a guaranteed effect beats an equal-EV random one because it can be planned around. Variance structurally favors the weaker player — RNG is a power transfer toward the underdog, plus drama. Spend it when the proc moment is exciting; never as a scaling crutch (see §VI).
5. **Counterplay discount:** effects the opponent can see and answer (wind-ups, telegraphs, conditions) cost less budget than uncounterable equivalents — the opponent's response option is part of the price. "Cast time is part of the power budget."
6. **Tempo vs. value:** power now versus larger power later is an exchange rate. Snowball mechanics buy tempo with future risk; comeback mechanics sell tempo for insurance.
7. **Action economy:** anything granting extra actions/procs/resets multiplies every other number in the system — its power scales with the rest of the kit instead of being fixed. Historically, the most broken designs in every genre are action-economy violations. Treat action-granting as the most expensive currency there is.

## III. Passive vs. active budgets

Compare what each pays. An **active** pays an input (attention), a resource, a cooldown, a timing decision, and whiff risk. An **always-on passive** pays nothing at runtime — 100% application reliability, zero attention, zero decision risk. Power follows effort, so the passive must buy less.

- **Magnitude rule of thumb:** an always-on passive lands around **1/3–1/2** the impact of an equal-tier active — but the qualitative rule matters more:
- **The role rule:** *passives change the texture of play; actives change its moments.* A passive should essentially never decide a fight by itself; an active is allowed to. If "that passive is why they won the exchange" is sayable, it's over budget; if removing it wouldn't change how fighting feels, it's under budget.
- **The always-on paradox:** 100% uptime compounds small numbers — a modest 10% passive can out-deliver a flashy active over a long fight. Budget passives against their **full-fight integral**, never their per-moment size. Small-looking passives are where hidden overbudgeting hides.
- **Conditional passives sit between tiers:** each condition (and each real cost) claws back budget toward active-tier payoffs. A passive with a hard condition and a resource cost is honorarily an active.

## IV. Estimating without experience

Nobody estimates accurately a priori — balance is a measurement-and-correction discipline, not a prediction discipline. The skill is being wrong cheaply and recoverably:

1. **Start weak, buff later.** Players celebrate buffs and revolt at nerfs (loss aversion), so ship new content ~20% under-tuned and raise it. Inexperience costs nothing under this rule — errors land in the forgiving direction.
2. **Big knobs first.** Early tuning moves in 25–50% steps (binary search for the right neighborhood); 5% adjustments are meaningless until you're in it.
3. **Estimate by comparison, never in absolute.** Humans are bad at absolute judgment and decent at relative judgment — hence anchors and pairwise ladders. "Stronger or weaker than the thing I understand?" is answerable by a novice; "is this a 7?" by no one.
4. **Fermi resolution is full resolution on paper.** "Roughly every hit / roughly once a fight / roughly never" sorts abilities into correct budget tiers; anything finer belongs to playtesting. Estimates need to be not-off-by-10×, nothing more.
5. **Balance against the exploiter, not the average.** Ask "what is the most abusive build containing this?" and tune against that. The average case takes care of itself.
6. **Then measure.** Pick rates and usage are the ultimate audit: slotted by everyone = over budget; slotted by no one = under — regardless of what paper said. Paper proposes; play disposes.

## V. Slot economies and kit size

- **Opportunity cost needs scarcity:** slots only price options while live options outnumber slots. In a cross-skill loadout system, scarcity comes from the *union* of all skills' live options — no single skill's kit needs to fill a loadout by itself.
- **Kit size is an output, not an input.** Quota-driven kits ("N actives + N passives per skill") manufacture dead abilities that exist to hit a number. Instead, a kit is complete when: each archetype its identity promises can actually be built; mandated coverage exists; a comfort pick exists. Whatever count that derives *is* the size, and unevenness across skills is fine.
- **The complaint you want is "I can't fit everything I like."** Meaningful exclusion is the point of a loadout. Diagnostic: "loadout too small" complaints *alongside dead abilities* = option-quality deficit, not slot deficit. The same complaint with everything alive = the system working (or, at extreme volume, a genuine slot-count question).

## VI. Scaling small numbers in chunky systems

Integer-scaled games (hearts, small damage values) make smooth tier scaling hard; %-proc-chance scaling solves the math but pays two currencies for nothing: **feelability** (a 20%→30% activation change is statistically invisible within a single fight, so tier-ups feel hollow) and **variance** (randomness spent without buying drama). Deterministic alternatives, in rough preference order:

1. **Frequency, not chance:** "every 5th hit" → "every 3rd hit"; shrinking internal cooldowns. Countable by the player mid-fight.
2. **Duration/uptime:** longer windows, buffs, effects per tier.
3. **Condition loosening:** the tier changes *when* it works, not how hard.
4. **Riders at high tiers:** tiers add mechanics rather than numbers.
5. **Scope:** targets hit, radius.
6. **Civilized RNG, if RNG stays:** pseudo-random distribution (Dota-style PRD) or pity counters — variance collapses, EV stays, perceived fairness rises.
7. **Fractional banking:** accumulate sub-integer bonuses into a bank that pays out whole units.

Governing principle: **RNG buys drama and underdog variance; when you need a continuous scaling knob, reach for frequency, duration, or condition first.**

## VII. Reading list

- **Ian Schreiber — "Game Balance Concepts"** (free 10-week online course: gamebalanceconcepts.wordpress.com) and **Schreiber & Romero, *Game Balance* (2021)** — the textbook for this entire file. First purchase.
- **David Sirlin — "Balancing Multiplayer Games"** article series — viable options, dominant strategies, the §I definitions. Free, short, dense.
- **Extra Credits — "Perfect Imbalance"** — why flat balance is the wrong goal, in ten minutes.
- **Mark Rosewater — "Twenty Years, Twenty Lessons"** (GDC, free) and his design columns — cost budgets *and* the separate complexity budget.
- **Jesse Schell — *The Art of Game Design: A Book of Lenses*** — the broader design-thinking companion.
- **GDC Vault** — search "balancing" plus your genre; fighting-game and MOBA talks transfer well to melee combat design.
