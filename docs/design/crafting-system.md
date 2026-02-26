# McRPG Crafting System Design

> **Status:** Brainstorm / Early Design
> **Scope:** Alchemy, Blacksmithing, and other crafting disciplines. Expansion pack territory — requires custom items and custom blocks.

---

## Table of Contents

1. [Philosophy and Goals](#1-philosophy-and-goals)
2. [Quality Tier System](#2-quality-tier-system)
3. [Success Rate as Economy Lever](#3-success-rate-as-economy-lever)
4. [The Crafting Process](#4-the-crafting-process)
5. [Living Recipe and Mastery](#5-living-recipe-and-mastery)
6. [Server Seed System](#6-server-seed-system)
7. [Cross-Skill Integration](#7-cross-skill-integration)
8. [Lifestyle Play Viability](#8-lifestyle-play-viability)
9. [Crafting Disciplines](#9-crafting-disciplines)
10. [Expansion Pack Integration](#10-expansion-pack-integration)

---

## 1. Philosophy and Goals

### The Core Problem with Existing Crafting Systems

Most crafting systems fail in one of three ways:

- **Pure RNG** — quality is determined by luck. Frustrating because failure feels like betrayal, not consequence.
- **Pure determinism** — quality is fixed for given inputs. Boring because there are no interesting decisions. Players follow a guide and execute it mechanically.
- **Recipe acquisition** — the skill is finding recipes, not crafting. Creates a checklist, not a craft.

The target is a system where **quality is earned through knowledge and preparation, failure is a calculated and understood risk, and deep knowledge of the craft produces meaningfully better outcomes than guide-following.**

### Design Goals

1. **Transparent risk.** Players know their success chance before committing materials. Failure is never a surprise — it's a calculated decision they accepted.

2. **Preparation is the skill expression.** The question before every craft is "have I done everything I can to improve my odds?" Players who invest more in preparation get better results. After a failure, they know what they could have done differently.

3. **Knowledge compounds.** Personal mastery of a specific recipe over time raises your baseline success rate for that recipe. This is non-tradeable — it rewards commitment to the craft.

4. **Server-specific discovery.** A layer of crafting knowledge is unique to each server's seed. General guides transfer between servers; deep expertise doesn't. This prevents the "just follow the optimal guide" problem while keeping general crafting skill meaningful.

5. **Lifestyle viability.** Dedicated crafters should be genuinely more capable than combat players who dabble, through exclusive quality tier access and accumulated recipe mastery. Crafting should feel like a real identity, not just a side activity.

---

## 2. Quality Tier System

Ten quality tiers, directly inspired by Reincarnation of the Strongest Sword God. The critical distinction between lower and upper tiers is how stats are expressed.

| Tier | Name | Stat Type | Notes |
|------|------|-----------|-------|
| 1 | Common | Flat | Accessible to all crafters |
| 2 | Mysterious Iron | Flat | Noticeably better than Common |
| 3 | Secret Silver | Flat | Requires recipe mastery and material quality |
| 4 | Fine Gold | Flat | Strong; requires significant investment |
| 5 | Dark Gold | Flat + % (transitional) | Bridge tier; mix of both |
| 6 | Epic | % | Aspirational; gated by dedicated crafter progression |
| 7 | Fragmented Legendary | % | Requires rare materials + near-maxed mastery |
| 8 | Legendary | % | Near-ceiling; collaborative crafts required |
| 9 | Fragmented Divine | % | Server-first tier content |
| 10 | Divine Artifact | % | Effectively unique; once per server history |

### Flat vs. Percentage Stats

**Why this matters:** A `+15 Attack` bonus is equally useful to a new player and a veteran. A `+8% Attack` bonus scales with the player's base stats — it's worth more to someone who has invested heavily in combat skills and gear.

This creates a natural, self-balancing power curve:
- Lower-tier crafted items are universally useful and genuinely good at their stage of the game
- Upper-tier items become aspirational for strong players without being mandatory for weaker ones
- There is no point where everyone *needs* Epic gear or falls behind — flat stat items remain relevant for players at the appropriate progression stage
- The system does not require constant rebalancing as the playerbase grows

### Quality Ceiling by Crafter Investment

A player who has not meaningfully invested in a crafting discipline cannot access upper tiers regardless of materials:

| Crafter Investment Level | Maximum Accessible Tier |
|--------------------------|--------------------------|
| Casual (dabbling) | Fine Gold |
| Dedicated (committed skill tree) | Epic |
| Master (deep specialisation) | Fragmented Legendary |
| Collaborative (multi-crafter projects) | Legendary+ |

This ensures top-tier crafted items remain associated with genuine crafting identity, not combat players with the right materials.

---

## 3. Success Rate as Economy Lever

### The Key Reframe: Opacity is the Problem, Not Chance

Pure RNG crafting feels bad because failure is *opaque* — you didn't know the odds, so failure feels like betrayal. The fix is not to remove chance but to make it fully transparent.

**The success rate is displayed prominently before any materials are committed.** Players see exactly what they're risking before they decide to proceed. Failure is then a calculated risk they accepted, not something that happened to them.

After a failure, the player's natural response becomes: "I was at 68% — I should have completed that tempering step first" rather than "the game cheated me." This is a completely different emotional experience.

### What Determines Success Rate

Success rate is the aggregate of multiple contributing factors, all visible in the crafting UI:

| Factor | Contribution | Notes |
|--------|-------------|-------|
| Crafter skill level | Base rate foundation | Low skill = low base rate |
| Material quality | +% per tier above minimum | Using Pristine materials vs. Standard |
| Preparation steps completed | +% per step | See Section 4 |
| Recipe mastery level | +% for familiar recipes | See Section 5 |
| Crafting station quality | +% from upgraded station | See Section 10 |
| Server-specific knowledge | +% from discovered synergies | See Section 6 |
| Consumables | +% one-time boost | Item sink; tradeable |

A player who has done everything possible — mastered the recipe, prepared thoroughly, used high-quality materials, upgraded their station, discovered relevant server synergies, and used a consumable — approaches but never quite reaches 100%. The remaining uncertainty is the economy lever.

### Success Rate Consumables as Item Sink

Consumables that boost success rate are a deliberate item sink for server economies:

- Tradeable, creating crafter-to-economy flow
- Finite supply tied to gathering/boss drops
- Increasingly valuable the higher the quality tier (a 5% boost on a 95% Legendary craft is still worth a lot)
- Named and themed per discipline: *Blacksmith's Focus Tonic*, *Alchemist's Clarity Draft*

### On Failure

Materials are consumed. No partial product. No consolation prize.

This sounds harsh but it's correct: the success rate was displayed, the risk was accepted, the economy pressure is real and intentional. A consolation "one tier lower" product undermines the risk calculation and softens the economy lever.

What players *do* get on failure: a small amount of crafting experience (they learned something even from the failure) and a log entry noting what the attempt was, useful for tracking their own improvement.

---

## 4. The Crafting Process

Crafting is not a single button press. It is a multi-step preparation sequence followed by a single commitment moment.

### Structure of a Craft

```
1. Gather base materials (gathering skills feed this)
2. Open crafting station
3. Review base success rate for current recipe + skill level
4. Complete optional preparation steps (each raises success rate)
5. Review final success rate
6. Commit — success or failure is resolved
```

### Preparation Steps

Each crafting discipline has a set of preparation steps specific to it. Steps are optional but each completed step raises the success rate. The player decides how many steps to complete before committing.

**Blacksmithing example:**
- *Preheat the forge* — spend fuel to bring the forge to optimal temperature (+8% success)
- *Refine the ore* — process raw ore into refined ingots using a refining station (+12% success, also improves material quality tier)
- *Temper the blank* — an intermediate heat + cool cycle before final shaping (+10% success)
- *Apply flux* — optional chemical treatment before quenching (+6% success)

**Alchemy example:**
- *Grind ingredients* — process raw herbs into powders/extracts (+8% success)
- *Stabilise the base* — add a stabilising compound before primary ingredients (+10% success)
- *Regulate heat* — calibrate the alchemical flame before beginning (+7% success)
- *Timing the addition* — add secondary ingredients at the right phase of the reaction (+% variable, server seed dependent — see Section 6)

The preparation decision is the core skill expression: "How much time and secondary materials am I willing to invest in this craft?" A player in a hurry crafts at 60%. A patient, thorough player crafts at 88%. Both are valid choices with understood consequences.

### Process-Based Steps vs. Passive Steps

Some preparation steps are purely passive (spend the material, get the bonus). Others involve a brief interactive moment:

- A timing window indicated by sound + action bar (like the cast mechanics in the ability system)
- Getting it right gives the full bonus; missing the window gives a partial bonus
- These interactive steps are the highest value steps to reward the engaged player

This creates a spectrum: passive steps for consistent reliable bonuses, interactive steps for high-skilled players to squeeze more.

---

## 5. Living Recipe and Mastery

Recipes evolve as you work with them. Your version of a recipe, over time, becomes meaningfully better than the base version anyone can learn. This is the primary reward for committing to the crafting identity.

### Mastery Phases

| Phase | Craft Count | Quality Ceiling | Base Success Rate Modifier |
|-------|-------------|-----------------|---------------------------|
| Novice | 1–10 | Common | 0% |
| Apprentice | 11–30 | Mysterious Iron | +5% |
| Journeyman | 31–60 | Secret Silver | +12% |
| Artisan | 61–100 | Fine Gold | +20% |
| Master | 101–200 | Dark Gold | +30% |
| Grandmaster | 200+ | Epic (with full investment) | +40% |

Phase transitions are milestone moments — the game notifies the player, possibly with a special visual or sound effect. These feel like earned progressions.

### Recipe Annotations

Through repeated crafting and experimentation, players discover "annotations" — personal refinements to their version of the recipe. Annotations are non-tradeable personal knowledge.

Examples:
- *"Heats better when the forge is at maximum temperature before adding the secondary material"* (+3% success rate permanently attached to your recipe)
- *"Refined ore from Deepslate-tier veins produces tighter grain structure"* (unlocks material quality bonus invisible to unannotated crafters)
- *"Secondary ingredients bond faster if the base is cooled to room temperature first"* (+preparation step option that wasn't visible before)

Annotations are discovered by:
- Simply crafting the recipe repeatedly (some annotations unlock at milestones)
- Experimenting with ingredient variations (trying different material sources)
- Cross-skill knowledge (see Section 7)
- Rarely, from Craft Journals found as drops in the world

Some annotations are universal (available to anyone who reaches the milestone). Some are server-seed dependent (the specific annotation varies by server — see Section 6). This is how server-specific expertise emerges naturally.

### Innovation

At Grandmaster mastery, a player can attempt to "innovate" on a recipe — experimenting to create a variant with different properties.

- Innovation consumes materials but the failure state is: craft produces the base recipe version, no extra loss
- Success creates a personal recipe variant only the innovator (and players they explicitly teach) can craft
- Variants have different stat distributions within the same quality tier — not strictly better, but different
- A blacksmith might innovate a sword variant that trades raw damage for lifesteal; an alchemist might create a potion variant that lasts longer but is weaker

Innovation keeps Grandmaster crafters engaged beyond the progression wall.

---

## 6. Server Seed System

### The Problem It Solves

In a fully documented crafting system, players follow guides. General crafting skills become trivially executable and there is no skill expression at the server level. Two crafters with the same skill level following the same guide produce identical results.

The server seed system creates a layer of knowledge that is specific to each server's procedurally generated configuration. General crafting knowledge (learned from guides and cross-server experience) gets you most of the way there. The remainder requires server-specific discovery.

### What the Server Seed Determines

On first startup, each server generates a UUID-based crafting seed. This seed deterministically varies certain crafting parameters:

**Ingredient Synergies**
Some ingredient combinations have enhanced synergy on this server. The synergy exists as a bonus to success rate and quality ceiling for those specific pairings. Guides might note that "Wolfsbane and Iron Ore have mild synergy in general" but on Server A the bonus is +3% and on Server B it is +11%.

**Preparation Step Timing Windows**
For interactive preparation steps, the optimal timing window shifts slightly per server. The window exists on all servers but its position in the cycle varies. A player from another server knows *that* there is a timing window; they need to find it on this server.

**Material Source Preferences**
Which biome types or vein characteristics tend to produce higher quality materials on this server. A universal truth: deep stone veins produce better ore than shallow veins. A server-specific truth: on this server, ore near igneous intrusions is particularly high quality.

**Recipe Annotation Discovery Order**
Some annotations unlock in a different order per server. A recipe might reveal its most valuable annotation at craft #45 on one server and craft #80 on another. The annotation is the same; the discovery milestone varies.

### What the Server Seed Does NOT Affect

Universal knowledge that is portable between servers:

- The existence of synergies (every server has them; which ones vary)
- The quality tier system and stat design
- The general preparation process and step types
- Material quality tiers and their base contributions
- The mastery phase system and milestone counts
- Consumable mechanics

**The balance point:** A guide-following crafter achieves roughly 65–70% of optimal. A server-specialist crafter who has discovered local synergies and preferences achieves 85–90%. The gap is real and meaningful but not punishing — following a guide is not worthless.

### Discovery and Community Knowledge

Server-specific knowledge is discovered through experimentation and shared through player community:

- Players who systematically try ingredient combinations note which ones outperform expectations
- Crafting communities on a server build local knowledge bases ("on this server, Deepwood Oak is notably better than Ashwood for hafts")
- Experienced crafters on a specific server are genuinely valuable because they hold hard-won local knowledge
- Starting fresh on a new server means rediscovering local parameters — your general skill transfers, your local knowledge does not

### Implementation

Seed variation is generated deterministically:

```
synergy_bonus = hash(server_seed + ingredient_a_key + ingredient_b_key) % max_synergy_value
timing_window_offset = hash(server_seed + recipe_key + step_index) % max_offset_ticks
material_preference_weight = hash(server_seed + material_key + biome_key) % weight_range
```

Every value is fixed per server but appears random to players discovering it. No database overhead — values are computed on demand from the seed.

---

## 7. Cross-Skill Integration

The crafting system's relationship to other McRPG skills is what makes lifestyle play feel like a real identity rather than a side activity.

### Gathering Skills Feed Material Quality

High-level gathering skills occasionally produce Superior or Pristine quality materials. These are required inputs for higher quality crafts:

| Skill | Crafting Discipline | What It Provides |
|-------|---------------------|-----------------|
| Mining | Blacksmithing, Jewellery | Higher quality ore, gemstones |
| Herbalism | Alchemy | Pristine herb harvests, rare ingredient variants |
| Excavation | Alchemy, Artificing | Rare earths, ancient materials |
| Woodcutting | Artificing, Fletching | Superior timber, rare wood types |

A crafter who also invests in the relevant gathering skill gets better material quality from their own harvests. They also unlock cross-skill annotations invisible to pure crafters.

### Cross-Skill Annotations

Some recipe annotations can only be discovered by a crafter who has also invested in a related gathering skill:

- A blacksmith with high Mining knows which ore vein characteristics produce higher quality metal — an annotation unlocks: *"Use ore from igneous contact zones for +4% success rate and material quality tier upgrade."* A pure blacksmith cannot see or discover this annotation.
- An alchemist with high Herbalism knows optimal harvest conditions for key ingredients — an annotation unlocks: *"Nightshade harvested at dawn in humid biomes has significantly better potency."*

This rewards multi-skill investment without requiring it. A pure blacksmith is still capable; a blacksmith-miner is meaningfully better at their craft in specific ways.

### Crafted Items Empowering Other Skills

Crafted items should interact with non-crafting skill systems:

- Alchemical potions that temporarily boost passive ability proc rates or extend ability combo windows
- Blacksmith-crafted weapons with properties that interact with specific sword or axe ability mechanics (e.g., a crafted blade that increases Bleed stack duration)
- Jewellery that reduces mana costs for combo ability activation
- Crafted tools that increase the chance of Pristine material drops during gathering

This creates genuine cross-system value: a combat player benefits from having a good alchemist in their community. The lifestyle player contributes to combat outcomes without fighting.

---

## 8. Lifestyle Play Viability

### Structural Choices That Make It Real

**Exclusive quality floors.** Epic and above are inaccessible to players who dabble. No amount of rare material acquisition bypasses the recipe mastery requirement. Top-tier items are associated with genuine crafting investment.

**Maintenance and services.** Crafted items above Fine Gold degrade slightly over time — not destructively, but a small loss of their bonus that a crafter can reverse. This creates an ongoing relationship rather than a one-time transaction. A player doesn't buy an Epic sword and never interact with their blacksmith again.

**Signature items.** At Grandmaster mastery, a crafter can sign items. Signed items grow slightly stronger as the commissioner uses them over time — the crafter's understanding of exactly what this person needs, expressed in the item. Creates long-term crafter-commissioner relationships.

**Personalised recipe variants.** Only the innovating crafter (and players they explicitly teach) can produce their invented recipe variants. Unique items tied to specific craftspeople create a reputation economy.

**Non-combat rewards.** Crafters unlock things that make their lifestyle feel like a real identity:
- Custom crafting station aesthetics
- Maker's marks visible in item tooltips ("Forged by [Player]")
- Named shop establishment in server towns
- Passive workshop production of lower-tier items (offline income)

### The Collaboration Requirement

Legendary+ crafts should require collaboration between multiple crafters or between a crafter and skilled gatherers. Not optional collaboration — structural requirement:

- A Legendary weapon requires: a Grandmaster Blacksmith + a Master-level ingredient prepared by an Alchemist + a rare gemstone set by a Jeweller
- No single player, no matter how invested, can solo-produce Legendary items
- This makes top-tier crafting a community achievement and gives lifestyle players a reason to build relationships

---

## 9. Crafting Disciplines

Disciplines are separate skill trees within the crafting expansion. Each has its own recipe mastery, its own preparation steps, its own cross-skill integrations.

### Blacksmithing

**Products:** Weapons, armour, tools, metal components
**Primary gathering integration:** Mining
**Key preparation steps:** Preheat, refine ore, temper blank, apply flux
**Unique mechanic:** *Forge Calibration* — the crafting station (custom forge block) can be upgraded, and its current heat level (managed between crafts) affects success rate. A blacksmith who keeps their forge properly maintained gets a persistent bonus.

### Alchemy

**Products:** Potions, elixirs, catalysts, crafting consumables (success rate boosts)
**Primary gathering integration:** Herbalism
**Key preparation steps:** Grind, stabilise, regulate heat, timing the addition
**Unique mechanic:** *Reagent Resonance* — some ingredient combinations have resonance that amplifies effect strength, discovered through experimentation. Resonance is partially server-seed dependent.

### Artificing / Inscription

**Products:** Jewellery, ability-enhancing items, enchanted tools, rune-inscribed armour
**Primary gathering integration:** Excavation, Mining (gemstones)
**Key preparation steps:** Attune the focus, inscribe the base pattern, channel the binding
**Unique mechanic:** *Affinity Matching* — some inscriptions have natural affinity with certain ability types. A crafter with cross-skill investment in Swords can inscribe weapons with sword-ability enhancing properties invisible to a pure artificer.

### Woodworking / Fletching

**Products:** Bows, crossbows, staves, hafted weapons, furniture/cosmetics
**Primary gathering integration:** Woodcutting
**Key preparation steps:** Season the wood, shape the blank, cure the joint
**Unique mechanic:** *Grain Reading* — a cross-skill annotation (Woodcutting investment) reveals which timber sources on the server produce better structural grain, a server-seed dependent variable.

---

## 10. Expansion Pack Integration

The crafting system is expansion-pack territory because it depends on:

- **Custom blocks** for crafting stations (forge, alchemical bench, inscription table, loom)
- **Custom items** for crafted products, quality catalysts, success rate consumables, crafting components
- **Custom models** for crafted products to visually distinguish quality tiers (a Fine Gold sword looks notably different from a Common one)

### Crafting Station Blocks

Each discipline has a custom block crafting station with upgrade tiers. Station upgrades:
- Increase the passive success rate bonus the station provides
- Unlock higher-tier preparation steps
- Improve the quality ceiling accessible at that station
- Are expensive and require materials from multiple gathering skills

Stations are persistent world objects — a player's crafting station at their base is part of their investment. Loss of a high-tier station is meaningful.

### Visual Quality Differentiation

Crafted items should be visually distinguishable by quality tier. Using custom models (ModelEngine), this could mean:
- Lower tiers: standard Minecraft item textures with slight modification
- Fine Gold+: distinct custom model shapes
- Epic+: animated elements (subtle glow, particle emission from the item)
- Legendary+: dramatic visuals that signal "this person has something rare"

The visual differentiation makes quality tier a social signal, not just a stat. Players recognise an Epic sword when they see one. This makes crafted items aspirational in a community sense, not just a mechanical one.

### Asset Reuse Principle

Apply the same primitive reuse philosophy from the ability system: define a small number of custom block primitives (forge block, bench block, vessel block) and vary their appearance through texture/model variants rather than entirely new assets per discipline. A Blacksmith's Forge and an Alchemist's Bench share underlying block behaviour logic; only their appearance and discipline-specific mechanics differ.
