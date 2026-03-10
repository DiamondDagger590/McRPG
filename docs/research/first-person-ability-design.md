# Deep Dive: First-Person Ability Design for McRPG

Research document covering feedback & juice, first-person-specific techniques, counterplay & PvP balance, and tier mechanical escalation.

---

## Table of Contents
1. [Feedback & Juice in First Person](#1-feedback--juice-in-first-person)
2. [The Minecraft Feedback Toolkit](#2-the-minecraft-feedback-toolkit)
3. [Counterplay & PvP Balance](#3-counterplay--pvp-balance)
4. [Tier Mechanical Escalation](#4-tier-mechanical-escalation)
5. [Applying It All: McRPG Ability Redesign Sketches](#5-applying-it-all-mcrpg-ability-redesign-sketches)
6. [Sources & Further Reading](#6-sources--further-reading)

---

## 1. Feedback & Juice in First Person

### 1.1 The First-Person Feedback Problem

In third-person games, you can see your character's full body — every animation, every pose, every visual effect wrapping around them. In first-person, you see *hands and a weapon*. This drastically limits your feedback channels. Per [Game Design Framework](https://gamedesignframework.net/building-character-feel-in-a-first-person-game/):

> "The weapon/tool is always present on the screen as a stable visual element and serves as the primary conduit for character action feedback."

**The Aiming Zone principle**: Players' eyes spend most of their time near the crosshair/center of screen. The most important feedback should happen *there*, not at screen edges. This means particle effects near the target are more noticeable than effects around the player's feet.

**The Combat Corridor**: The center area of the screen must never be blocked by weapon models or HUD elements during combat. Ability feedback that clutters this area *hurts* more than it helps.

### 1.2 The Three Feedback Channels (Ranked for First Person)

In first-person, the channels have a clear priority order:

| Priority | Channel | Why It's Ranked Here | Minecraft Tools |
|----------|---------|---------------------|-----------------|
| **1st** | **Audio** | Omnidirectional — works even when the player isn't looking at the effect. Most reliable channel in first-person. | `player.playSound()`, positional sounds, ambient loops |
| **2nd** | **Visual (center screen)** | Particle effects near the crosshair/target, action bar text, title overlays, boss bars | `spawnParticle()`, `sendActionBar()`, `showTitle()`, `showBossBar()` |
| **3rd** | **Visual (peripheral)** | Particles at player feet/around body. Easily missed in first-person but visible to nearby players/spectators. | `spawnParticle()` at player location, potion effect overlays |
| **4th** | **Kinesthetic** | Velocity changes, knockback, forced view rotation. Very impactful but use sparingly — can feel like loss of control. | `setVelocity()`, `setRotation()`, potion effects (Speed for FOV) |

**Key insight**: Audio is *more important* than visuals in first-person because it reaches the player regardless of where they're looking. A sword clang, a magical chime, or a sizzling bleed sound communicates more reliably than a particle effect behind the player.

### 1.3 The Juice Calibration Scale

Not every ability deserves the same feedback intensity. Calibrate based on importance:

| Ability Importance | Feedback Budget | Example |
|-------------------|----------------|---------|
| **Minor passive proc** | Subtle: 1 sound + small particle burst at target | ExtraOre double drop — quiet sparkle + soft "cha-ching" |
| **Significant passive** | Moderate: Sound + particles + brief action bar text | Bleed activation — sizzle sound + red particles on victim + "[Bleed] activated!" |
| **Active ability activation** | Full: Sound + particles at player AND target + action bar or boss bar + kinesthetic if appropriate | RageSpike — whoosh + flame trail + camera momentum + impact particles |
| **Tier-up milestone** | Celebratory: Sound + title text + particle burst + optional firework | Reaching Bleed Tier 3 — dramatic sound + title "Bleed III Unlocked" + unique particle effect |

The principle from [Game Developer — Don't Over-Juice](https://www.gamedeveloper.com/design/video-indies-resist-the-urge-to-juice-it-or-lose-it-): "Juice for the sake of juice can distract the player from the main goal. Consider the context."

### 1.4 The "Two-Audience" Feedback Rule

Every ability effect should work for **two audiences simultaneously**:

1. **The user (first-person)**: Needs center-screen visual + audio confirmation. Can't see their own body.
2. **Nearby players/spectators (third-person view of the user)**: Need to see particles/effects around the user's model. This is how opponents read what's happening.

**Practical implication**: Spawn particles both at the **target location** (user sees these in their crosshair area) and at the **user's location** (opponents/spectators see these). Use different particle types or sizes for each to avoid clutter.

### 1.5 Specific First-Person Feedback Techniques

| Technique | Description | Best For | Caution |
|-----------|-------------|----------|---------|
| **Impact particles at crosshair** | Spawn damage/crit particles at the entity the player hit | Hit confirmation (Bleed, Vampire) | Keep count low (5-15 particles) to avoid lag |
| **Directional particle trail** | Line of particles from player to target | Ranged abilities, dash abilities (RageSpike) | Spawn server-side, despawns naturally |
| **Expanding ring** | Circle of particles expanding outward from player | AOE abilities (MassHarvest, OreScanner pulse) | Stagger spawning over 2-3 ticks for smoothness |
| **Persistent aura** | Repeating particle task around player | Buff active states (SerratedStrikes active) | Use `spawnParticle` with force=false so distant players don't render it |
| **Screen tint via potion** | Brief potion effect (no actual gameplay effect) for screen overlay | Taking damage, low health warning | Very short duration (1-2 seconds), use sparingly |
| **Action bar timer** | Show remaining duration/cooldown in action bar | Active abilities with duration, cooldown notification | Overwriting other action bar messages — need priority system |
| **Boss bar for active abilities** | Show ability name + remaining time as boss bar | Long-duration actives, channeled abilities | Maximum 1-2 boss bars at a time to avoid clutter |
| **Positional sound** | Play sound at *target* location so it has 3D directionality | Enemy abilities hitting you, bleed source | Minecraft's sound engine gives natural distance falloff |
| **Rising pitch sequence** | Play same sound at increasing pitch for combo/buildup | Combo abilities, charging, pulse waves | Easy to implement: `playSound(loc, sound, vol, pitch)` with pitch 0.5→2.0 |
| **Impact freeze (pseudo)** | Brief Slowness I (1-2 ticks) on both attacker and target | Heavy hits, critical strikes | Can feel like lag if overused — 1-2 ticks MAX |

---

## 2. The Minecraft Feedback Toolkit

### 2.1 Available Server-Side Feedback APIs

All of these require **zero client mods** — pure server-side, works on vanilla clients:

| API | Method | Use Case | Notes |
|-----|--------|----------|-------|
| **Particles** | `player.spawnParticle(Particle, Location, count, offsetX, offsetY, offsetZ, extra)` | Visual effects at any location | Per-player targeting with `spawnParticle(type, loc, count, ..., data, force)` |
| **Sound** | `player.playSound(Location, Sound, SoundCategory, volume, pitch)` | Audio feedback | Positional + per-player. Pitch range 0.5-2.0 for variety |
| **Action Bar** | `player.sendActionBar(Component)` | Transient status text above hotbar | Ideal for cooldowns, proc notifications, combo counters |
| **Boss Bar** | `Audience.showBossBar(BossBar)` | Persistent status display (duration bars, charge meters) | Color + style + progress animatable. [PaperMC Docs](https://docs.papermc.io/adventure/bossbar/) |
| **Title/Subtitle** | `Audience.showTitle(Title.title(main, sub, times))` | Major events (tier unlocks, skill level ups) | Configurable fade-in/stay/fade-out. Use sparingly — covers center screen |
| **Potion Effects** | `player.addPotionEffect(new PotionEffect(...))` | Screen tint, FOV changes, movement modification | Ambient=true hides particles. Use for subtle screen effects |
| **Entity Glow** | `entity.setGlowing(true)` via packets or scoreboard teams | Highlight entities through walls | Per-player via protocol hacking or team color. OreScanner already uses this |
| **Velocity** | `player.setVelocity(Vector)` | Knockback, dashes, launches | Powerful but can feel like loss of control. Always in player's forward direction |
| **Block Changes** | `player.sendBlockChange(Location, BlockData)` | Temporary visual block changes (fake walls, indicators) | Client-side only — doesn't affect server state |
| **Item Display Entities** | Display entities (1.19.4+) | Floating text, 3D markers, ability indicators | More complex but very flexible for custom visuals |

### 2.2 Particle Budget Guidelines

Minecraft clients can struggle with excessive particles. Recommended budgets:

| Context | Max Particles Per Tick | Reasoning |
|---------|----------------------|-----------|
| **Single ability proc** | 15-30 particles, 1-2 ticks | Quick burst, noticeable but not laggy |
| **Active ability sustained** | 5-10 particles/tick for duration | Aura/trail effect, kept light |
| **AOE pulse wave** | 30-50 particles, staggered over 3-5 ticks | Expanding ring/sphere effect |
| **Major event (tier unlock)** | 50-100 particles, 1 tick burst | Rare event, brief spike is acceptable |
| **Multiple nearby players** | Halve all budgets | Particles from multiple sources compound |

**Performance tip**: Use `force=false` in `spawnParticle()` to respect client particle distance settings. Only use `force=true` for critical feedback that *must* be seen.

### 2.3 Recommended Particle Types by Ability Category

| Category | Particle Type | Why |
|----------|--------------|-----|
| **Damage/Combat** | `DAMAGE_INDICATOR`, `CRIT`, `CRIT_MAGIC`, `SWEEP_ATTACK` | Players already associate these with combat |
| **Bleed/DOT** | `BLOCK` (redstone dust data), `DRIPPING_LAVA`, custom red dust | Red = damage over time is universal |
| **Healing** | `HEART`, `VILLAGER_HAPPY`, `COMPOSTER` | Green/heart = healing is intuitive |
| **Buff/Enchant** | `ENCHANT`, `SPELL_MOB`, `END_ROD` | Magical/sparkle = enhancement |
| **Mining/Earth** | `BLOCK` (stone data), `FALLING_DUST`, `DUST_PLUME` | Material-matching particles feel grounded |
| **Nature/Herbalism** | `COMPOSTER`, `FALLING_SPORE_BLOSSOM`, `CHERRY_LEAVES`, `HAPPY_VILLAGER` | Green/organic particles |
| **Fire/Rage** | `FLAME`, `SOUL_FIRE_FLAME`, `LAVA`, `SMALL_FLAME` | Fire = aggressive, rage, power |
| **Utility/Scanning** | `END_ROD`, `ELECTRIC_SPARK`, `SONIC_BOOM` | Clean, informational feel |

### 2.4 Sound Design Principles for Abilities

| Principle | Implementation | Example |
|-----------|---------------|---------|
| **Every ability has a signature sound** | Pick a unique vanilla sound per ability | Bleed = `ENTITY_PLAYER_HURT_SWEET_BERRY_BUSH`, RageSpike = `ENTITY_ENDER_DRAGON_FLAP` |
| **Match intensity to importance** | Volume 0.3-0.5 for passives, 0.7-1.0 for actives | ExtraOre proc = quiet sparkle; OreScanner = loud ping |
| **Use pitch variation** | Same sound at different pitches prevents auditory fatigue | Bleed ticks: pitch cycles 0.8 → 1.0 → 1.2 |
| **Positional audio for opponents** | Play sound at ability location, not globally | Enemy hears bleed sizzle from *your* direction |
| **Ready-state audio cue** | Distinct "charging" sound during ready window | Sword ready = `BLOCK_ANVIL_LAND` at pitch 2.0 (high metallic ring) |
| **Cooldown-ready notification** | Brief sound when cooldown expires | `BLOCK_NOTE_BLOCK_CHIME` at pitch 1.5 |

---

## 3. Counterplay & PvP Balance

### 3.1 Lessons from VALORANT and Overwatch

Both games treat counterplay as a **core design pillar**, not an afterthought:

**VALORANT's approach** ([How We Balance VALORANT](https://playvalorant.com/en-us/news/dev/how-we-balance-valorant/)):
> "We want to allow gunplay to do the talking, while players use abilities strategically and intentionally to gain advantages."

Key VALORANT principles for McRPG:
- **Core gameplay first**: Sword swinging, mining, farming should feel good *without* abilities. Abilities augment — they don't replace.
- **Audio/visual telegraph everything**: When Riot found players weren't reacting to Raze's grenade audio cues, they didn't remove counterplay — they made the cues more obvious AND reduced grenade charges.
- **The Competitive Dialogue**: Round-to-round (or fight-to-fight) adaptation. If one strategy always works with no counter, it kills the dialogue.

**Overwatch's approach** ([Prototyping Illari's Abilities](https://overwatch.blizzard.com/en-gb/news/24003136/catching-sunlight-prototyping-illari-s-abilities-with-the-hero-design-team/)):
> "They decided a full-screen blind was too much for the action flow... the user didn't feel a noticeable impact, but the blinded player felt too much."

Key Overwatch principles:
- **The dual-feel test**: An ability must feel impactful to the user AND fair to the target. If either side is unsatisfied, redesign.
- **Counterplay as design constraint, not afterthought**: "Are there enough options for counterplay?" is asked *during prototyping*, not after.
- **Time-limited windows**: Doomfist's Empowered Punch was changed to have a duration because holding it indefinitely "created situations with limited counterplay."

### 3.2 The Counterplay Framework for McRPG Abilities

Every McRPG ability should answer these four questions:

#### Question 1: "Can the opponent see it coming?"
**Telegraph** — A signal before the effect lands.

| Ability Type | Telegraph Method | Example |
|--------------|-----------------|---------|
| Passive proc (Bleed) | Brief particle/sound on attacker *before* first tick | Red spark on sword swing → then bleed starts next tick |
| Ready-state active | Visible particle aura during 3-sec ready window | Swirling particles around held sword while ready |
| Active with windup | Sound cue + particle buildup during cast time | RageSpike: whoosh buildup before launch |
| AOE ability | Ground indicator before effect | MassHarvest: green ring appears → then crops break |

#### Question 2: "Can the opponent respond?"
**Counterplay options** — What can the victim do?

| Counter Type | Description | McRPG Example |
|-------------|-------------|---------------|
| **Avoidance** | Move out of range/area | Dodge RageSpike's linear dash path |
| **Interruption** | Stop the ability during telegraph | Hit the player during ready-state to cancel it |
| **Mitigation** | Reduce the ability's effect | Shield/armor reduces bleed damage; milk cleanses DOTs |
| **Punish window** | Exploit the recovery phase | Attack after RageSpike ends while user is briefly slowed |
| **Trade** | Accept the hit but deal damage back | Tank through SerratedStrikes buff and outlast the duration |

#### Question 3: "Does the opponent understand what happened?"
**Clarity** — After being affected, can the player identify what hit them?

Recommendations:
- **Victim notification**: Brief action bar message: `"<red>[Bleed] You are bleeding! (3s remaining)"`
- **Consistent visual language**: Every DOT shows red particles; every buff shows gold particles; every debuff shows purple
- **Sound per ability**: Unique sounds mean experienced players can identify abilities by ear alone

#### Question 4: "Is the counter interesting?"
**Depth** — Multiple valid responses, not just "run away."

Bad counterplay: Only option is to disengage → boring, rewards passive play.
Good counterplay: Multiple options with different risk/reward profiles:
- Disengage (safe, lose position)
- Tank it and trade damage (risky, potentially win the fight)
- Use your own ability to counter (skill-expressive, resource cost)
- Change positioning to reduce effectiveness (tactical, rewards game sense)

### 3.3 The RNG Problem: Passive Procs in PvP

McRPG's biggest counterplay problem is **invisible RNG**. Most passives activate on a random chance with zero telegraph:

> Player A swings sword → 15% chance → Bleed activates → Player B starts losing health with no prior warning.

This fails all four counterplay questions. The opponent can't see it coming, can't respond, may not understand what happened, and has no interesting counter.

**Three approaches to fix invisible RNG:**

**Approach A: Telegraphed RNG**
Keep the random roll but add a brief telegraph before the effect applies:
- Sword glows red on the swing that procs Bleed → 0.5 second delay → Bleed starts
- Opponent sees the glow and can try to disengage or prepare

**Approach B: Deterministic Buildup**
Replace random chance with a counter/buildup system:
- Every sword hit adds 1 "Bleed Stack" (shown as red particles on target)
- At 3 stacks, Bleed automatically activates
- Opponent can see stacks accumulating and knows when Bleed will trigger
- Adds skill expression: attacker wants to land 3 hits quickly; defender wants to disengage before 3

**Approach C: Conditional Activation**
Ability activates under specific conditions instead of random chance:
- Bleed activates when the attacker hits a target from behind (backstab)
- Bleed activates when the attacker hits a target below 50% health (finishing blow)
- Bleed activates on critical hits only (player-controlled via jump-attacks)

Each approach has trade-offs:

| Approach | Skill Expression | Excitement | Complexity | Counterplay |
|----------|-----------------|------------|------------|-------------|
| **A: Telegraphed RNG** | Low (still random) | High (surprise factor) | Low (add visual to existing system) | Moderate (brief react window) |
| **B: Deterministic Buildup** | High (attacker combo tracking) | Moderate (predictable but tense) | Medium (new counter system) | High (opponent tracks stacks) |
| **C: Conditional** | High (attacker positioning/timing) | Moderate (conditional mastery) | Medium (new condition checks) | High (opponent avoids conditions) |

These aren't mutually exclusive — different abilities could use different approaches. The key is that *at least some* abilities move away from pure invisible RNG.

### 3.4 The "Frustration Budget"

Every ability has a frustration cost to the opponent. Budget it carefully:

| Frustration Factor | Cost Level | Examples |
|-------------------|------------|---------|
| Losing health with no warning | **Very High** | Current Bleed, current Vampire |
| Being moved against your will | **High** | RageSpike knockback |
| Missing out on loot/resources | **Moderate** | None currently (but consider: steal abilities) |
| Being slowed/debuffed | **Moderate** | Potential future abilities |
| Seeing opponent get buffed | **Low** | SerratedStrikes (visible buff) |
| Opponent gets bonus resources | **Very Low** | ExtraOre, ExtraLumber |

**Rule of thumb**: The higher the frustration cost, the more telegraph and counterplay an ability needs. ExtraOre needs zero counterplay (it doesn't affect opponents). Bleed needs significant counterplay (it kills people).

### 3.5 Ability vs. Skill Expression: The VALORANT Tension

VALORANT's biggest ongoing design challenge ([TalkEsport — Aim vs Ability Debate](https://www.talkesport.com/editorials/valorants-aim-vs-ability-debate-riots-biggest-design-challenge/)):

> "The balance philosophy of VALORANT seems to be the complete removal of any potential threats instead of refining them."

McRPG faces the same tension: if abilities are too powerful, vanilla PvP skill doesn't matter. If abilities are too weak, why bother leveling?

**The sweet spot**: Abilities should create *advantages* that skilled players can capitalize on, not *guarantees* that bypass skill.

| Too Weak | Sweet Spot | Too Strong |
|----------|-----------|-----------|
| Bleed does 0.5 hearts total | Bleed does meaningful DOT but is survivable with food/pots | Bleed kills in 3 ticks through any healing |
| RageSpike moves you 2 blocks | RageSpike gaps distance but you must aim the follow-up | RageSpike auto-targets and one-shots |
| SerratedStrikes adds 1% bleed chance | SerratedStrikes meaningfully boosts proc rate for a window | SerratedStrikes guarantees bleed on every hit |

---

## 4. Tier Mechanical Escalation

### 4.1 The Problem with "+N% Per Tier"

From [GDKeys — Meaningful Skill Trees](https://gdkeys.com/keys-to-meaningful-skill-trees/):

> "One of the most repeated complaints against skill trees is when games require skill points to go into stat modifiers. While stat modifiers can stack over time, it's often difficult to notice any direct effect they have on the game."

And from [Game Wisdom — Power Curves](https://game-wisdom.com/critical/3-forms-power-curves-game-design):

> "Scaling progression is essentially 'getting bigger numbers' — once the player loses interest in it, they're not going to want to continue."

McRPG's current tier system is purely numerical: Tier 1 Bleed does X damage, Tier 2 does X+Y damage. The player's *experience* of using the ability doesn't change. This is the weakest form of progression.

### 4.2 The Mechanical Escalation Framework

Instead of each tier being "+N to damage", each tier should introduce a **new verb or modifier**:

| Escalation Type | Description | Player Experience |
|----------------|-------------|-------------------|
| **Quantitative** (current) | More damage, longer duration, bigger radius | "I do more of the same thing" |
| **Qualitative** | New behavior, new interaction, new visual | "I can do something I couldn't before" |
| **Synergistic** | Unlocks interaction with another ability or system | "This ability now combos with something else" |
| **Situational** | Ability gains a bonus in specific contexts | "I'm rewarded for smart positioning/timing" |

**The ideal tier structure uses all four**, front-loading qualitative changes to create excitement:

### 4.3 Tier Design Template

For each ability with N tiers:

| Tier | Focus | Design Goal |
|------|-------|-------------|
| **Tier 1 (Unlock)** | Core mechanic | Teach the player what the ability *does*. Simple, clean, one clear effect. |
| **Tier 2** | Qualitative upgrade | Add a new *visible* behavior. Player should think "oh, it does THAT now?" |
| **Tier 3** | Synergistic/Situational | Create interaction with another ability, skill, or game system. Rewards mastery. |
| **Tier 4 (Capstone)** | Identity-defining | The ability becomes a signature part of the player's identity. Dramatic visual + mechanical change. |

### 4.4 Worked Examples: Tier Redesign Sketches

#### Bleed (Swords) — Currently: More damage per tier

| Tier | Current | Redesigned |
|------|---------|-----------|
| **T1** | X damage/tick | Base DOT. Red particles on victim. Sizzle sound per tick. |
| **T2** | X+Y damage/tick | **Hemorrhage**: Bleed victims leave a red particle trail on the ground for the duration. Attacker can *track* bleeding opponents. (New verb: tracking) |
| **T3** | X+2Y damage/tick | **Blood Scent**: While a bleed victim is within 15 blocks, attacker gets a subtle directional indicator (particles at screen edge pointing toward victim). Synergy with tracking from T2. |
| **T4** | X+3Y damage/tick | **Crimson Cascade**: If a bleeding entity dies, the remaining bleed ticks spread to the nearest entity within 3 blocks. Visual: blood particles burst outward on death. (New verb: spreading) |

Each tier adds something *new to do* or *new to perceive*, not just bigger numbers.

#### OreScanner (Mining) — Currently: Bigger radius per tier

| Tier | Current | Redesigned |
|------|---------|-----------|
| **T1** | R-block scan radius | Base scan. Highlights ores with glow. Single ping sound. |
| **T2** | R+N block radius | **Deep Scan**: Also reveals ores behind *other solid blocks* (not just in line-of-sight). Extends scan depth. (Qualitative: removes LOS restriction) |
| **T3** | R+2N block radius | **Resonance**: Each ore type produces a distinct pitch when scanned. Experienced players can identify ore types by sound alone without looking. (New channel: audio identification. Synergy with game knowledge.) |
| **T4** | R+3N block radius | **Motherlode Sense**: After scanning, the richest cluster gets a persistent glow for 30 seconds and a pulsing beacon particle column visible from distance. Also shares scan results with nearby party members. (Social/cooperative element + persistent utility) |

#### RageSpike (Swords) — Currently: More damage per tier

| Tier | Current | Redesigned |
|------|---------|-----------|
| **T1** | X damage, V velocity | Dash forward, damage nearby enemies. Flame trail particles. |
| **T2** | X+Y damage | **Piercing Rush**: Enemies hit are briefly stunned (Slowness II, 0.5s). Creates a punish window for follow-up attacks. (New verb: stun/setup) |
| **T3** | X+2Y damage | **Momentum**: If RageSpike hits at least 1 enemy, cooldown is reduced by 30%. Rewards aggressive use. (Conditional reset — synergy with fight flow) |
| **T4** | X+3Y damage | **Aftershock**: At the end of the dash, a small shockwave knocks back nearby enemies (3-block radius). Creates space for the user to choose: follow up or disengage. (New verb: AOE control. New decision point.) |

#### MassHarvest (Herbalism) — Currently: Bigger radius per tier

| Tier | Current | Redesigned |
|------|---------|-----------|
| **T1** | R-block radius pulse | Harvests crops in radius. Green expanding ring particles. |
| **T2** | R+N radius | **Replant**: Harvested crops are automatically replanted (sets block back to age 0). Saves the tedious replant step. (New verb: replanting. Quality-of-life upgrade.) |
| **T3** | R+2N radius | **Bountiful Harvest**: Each pulse has a chance to produce bonus seeds/crops (synergy with TooManyPlants if active). Growing particles on replanted crops. (Synergy: connects two abilities) |
| **T4** | R+3N radius | **Gaia's Embrace**: The final pulse also grows all replanted crops by 1 age stage instantly. Visual: golden light sweeps outward. (New verb: accelerated growth. Full harvest-replant-grow cycle in one activation.) |

### 4.5 Tier Visual Escalation

Each tier should have a *visible* difference so spectators and opponents can gauge threat level:

| Tier | Visual Escalation Principle | Example (Bleed) |
|------|---------------------------|-----------------|
| **T1** | Base effect — minimal particles | Small red particles on victim |
| **T2** | Expanded effect — more particles + new particle type | Red particles + dripping particle trail |
| **T3** | Distinct aura/glow | Victim has faint red glow + trail + directional indicator for attacker |
| **T4** | Dramatic signature — unmistakable from distance | Intense red aura + trail + death burst spreading to nearby enemies |

This serves counterplay too: opponents can *see* what tier of Bleed they're facing and adjust their response accordingly.

### 4.6 Avoiding "Obligation Tiers"

From [The Gamer — Good Skill Trees](https://www.thegamer.com/good-skill-trees-rpgs/):

> "At their worst, skill trees are bloated menus of +5 percent damage upgrades that you click through without thinking."

The rule: **Every tier must pass the "Would I be excited to unlock this?" test.** If the answer is "I guess the numbers are bigger," redesign it.

From [Fortress of Doors — Upgrades](https://www.fortressofdoors.com/upgrades-equipment-and-skill-trees/):

> "Hard upgrades represent permanent improvements to the player's ability to play the game. Unlike scaling elements, these upgrades fundamentally alter the game."

---

## 5. Applying It All: McRPG Ability Redesign Sketches

### 5.1 The Full-Stack Ability Design Checklist

For every new or redesigned ability, answer:

**Feedback (Section 1-2):**
- [ ] What sound plays on activation? On each tick? On expiration?
- [ ] What particles appear at the user? At the target? In the world?
- [ ] What does the action bar / boss bar show during the ability?
- [ ] Is feedback calibrated to importance (minor proc vs major active)?
- [ ] Does the effect work in both first-person (user) and third-person (spectator)?

**Counterplay (Section 3):**
- [ ] Can the opponent see it coming? (Telegraph)
- [ ] Can the opponent respond? (At least 2 counter options)
- [ ] Does the opponent understand what happened? (Clarity notification)
- [ ] Is the counter interesting? (Multiple valid responses)
- [ ] Is the frustration budget appropriate for the ability's impact?

**Tier Escalation (Section 4):**
- [ ] Does each tier add a new verb/behavior, not just +N%?
- [ ] Is there a visual difference between tiers?
- [ ] Does at least one tier create synergy with another ability?
- [ ] Would the player be excited to unlock each tier?
- [ ] Does the capstone (highest tier) feel identity-defining?

### 5.2 Priority Improvements for Existing Abilities

If I were to recommend a phased approach:

**Phase 1 — Add feedback to everything (biggest bang for lowest effort):**
- Add sounds to all ability activations
- Add particles to all ability activations
- Add action bar notifications for procs
- Add ready-state visual indicators (particles around tool during 3-sec window)

**Phase 2 — Add opponent awareness:**
- Victim notifications for hostile abilities (action bar: "You are bleeding!")
- Visual indicators on affected entities (particles on bleed victims, buff aura on buffed players)
- Ready-state visible to opponents (so they can see when someone is "charged up")

**Phase 3 — Counterplay mechanisms:**
- Bleed cleansing (milk, golden apple, or specific counter-ability)
- Ready-state interruptible (taking damage during ready cancels it)
- Recovery windows on active abilities (brief vulnerability after use)

**Phase 4 — Tier mechanical escalation:**
- Redesign tier progression to include qualitative changes
- Add visual tier indicators so opponents can gauge threat level
- Create cross-ability synergies at higher tiers

---

## 6. Sources & Further Reading

### First-Person Feel & Feedback
- [Game Design Framework — Building Character Feel in First-Person](https://gamedesignframework.net/building-character-feel-in-a-first-person-game/)
- [Medium — Impact of HUD Elements on Gameplay Perception](https://medium.com/@MaxKosyakoff/the-impact-of-hud-elements-on-core-gameplay-perception-0a737d64c471)
- [Karl Lewis — Designing an Immersive FPS](https://karllewisdesign.com/unreal-immersive-fps-part1/)
- [GDQuest — Juicing Up Your Game Attacks](https://www.gdquest.com/library/juicy_attack/)
- [RPG Playground — Making a Juicy Game](https://rpgplayground.com/research-making-a-juicy-game/)
- [Game Developer — Don't Over-Juice](https://www.gamedeveloper.com/design/video-indies-resist-the-urge-to-juice-it-or-lose-it-)
- [Game Developer — Squeezing More Juice Out of Your Design](https://www.gamedeveloper.com/design/squeezing-more-juice-out-of-your-game-design-)
- [Designing Game Feel — Academic Survey (PDF)](https://arxiv.org/pdf/2011.09201)
- [Cornell — Game Polish Lecture (PDF)](https://www.cs.cornell.edu/courses/cs4154/2015fa/sessions/lecture14.pdf)

### Combat Design & Counterplay
- [GDKeys — Anatomy of an Attack](https://gdkeys.com/keys-to-combat-design-1-anatomy-of-an-attack/)
- [Rivals Workshop — Anticipation, Action, Recovery](https://www.rivalslib.com/workshop_guide/art/anticipation_action_recovery.html)
- [Game Design Skills — Combat Design](https://gamedesignskills.com/game-design/combat-design/)
- [Polydin — Game Combat Design: Making Every Hit Count](https://polydin.com/game-combat-design/)
- [Game Developer — Fundamental Pillars of a Combat System](https://www.gamedeveloper.com/design/the-fundamental-pillars-of-a-combat-system)
- [Mythcreants — Learning From Successful Combat Systems](https://mythcreants.com/blog/learning-from-successful-combat-systems/)
- [zfesta — PvP Spectator Clarity](https://zfesta.com/pvp-spectator-interest-grows-with-clearer-visual-feedback/)

### VALORANT & Overwatch Design Philosophy
- [Riot Games — How We Balance VALORANT](https://playvalorant.com/en-us/news/dev/how-we-balance-valorant/)
- [TheSpike — VALORANT Design Philosophy](https://www.thespike.gg/valorant/news/valorant-s-design-philosophy/41)
- [TalkEsport — Aim vs Ability Debate](https://www.talkesport.com/editorials/valorants-aim-vs-ability-debate-riots-biggest-design-challenge/)
- [GamingBolt — VALORANT Dev Diary on Design Philosophy](https://gamingbolt.com/valorant-dev-diary-explores-philosophy-behind-game-design)
- [GBHBL — How Riot Built VALORANT's Aesthetics](https://www.gbhbl.com/how-riot-built-one-of-gamings-coolest-aesthetics-with-valorant/)
- [Riot Games — VALORANT Shaders and Gameplay Clarity](https://technology.riotgames.com/news/valorant-shaders-and-gameplay-clarity)
- [Overwatch — Prototyping Illari's Abilities](https://overwatch.blizzard.com/en-gb/news/24003136/catching-sunlight-prototyping-illari-s-abilities-with-the-hero-design-team/)
- [Overwatch — Balance Design and the Experimental Card](https://overwatch.blizzard.com/en-us/news/23652236/inside-overwatch-balance-design-and-the-experimental-card/)
- [Overwatch — PvP Beta Analysis](https://overwatch.blizzard.com/en-gb/news/23787377/overwatch-2-pvp-beta-analysis-how-data-and-community-feedback-inform-game-balance/)
- [Game Design Skills — Video Game Balance Guide](https://gamedesignskills.com/game-design/game-balance/)

### Progression & Tier Design
- [GDKeys — Meaningful Skill Trees](https://gdkeys.com/keys-to-meaningful-skill-trees/)
- [Game Design Skills — Game Progression](https://gamedesignskills.com/game-design/game-progression/)
- [Game Wisdom — 3 Forms of Power Curves](https://game-wisdom.com/critical/3-forms-power-curves-game-design)
- [Game Wisdom — Upgrade Design](https://game-wisdom.com/critical/upgrade-design)
- [Game Wisdom — Progression Models](https://game-wisdom.com/critical/progression-models)
- [Medium — Mechanics Progression of Character Skills (Design Pattern)](https://ryha2000.medium.com/deriving-a-design-pattern-from-the-mechanics-progression-of-character-skills-5574b65c3aa6)
- [Game Developer — Fundamentals of Game Progression](https://www.gamedeveloper.com/design/the-fundamentals-of-game-progression)
- [Game Developer — Procession of Progression](https://www.gamedeveloper.com/design/the-procession-of-progression-in-game-design)
- [Game Developer — Craft of Game Systems: Tuning RPG Content](https://www.gamedeveloper.com/design/the-craft-of-game-systems-tuning-rpg-content)
- [Fortress of Doors — Upgrades, Equipment, and Skill Trees](https://www.fortressofdoors.com/upgrades-equipment-and-skill-trees/)
- [The Gamer — What Goes Into Good Skill Trees](https://www.thegamer.com/good-skill-trees-rpgs/)
- [TTRPG Games — Skill Trees: Basics and Design](https://www.ttrpg-games.com/blog/skill-trees-in-ttrpgs-basics-and-design/)
- [Game Designing — Game Design Skill Trees (Beginners Guide)](https://gamedesigning.org/learn/skill-trees/)
- [Douglas Underhill — Advancement Systems in RPG Design](https://douglasunderhill.wordpress.com/2017/03/28/advancement-systems-in-rpg-design/)
- [TechTimes — 7 RPGs That Perfected Character Progression](https://www.techtimes.com/articles/314818/20260225/7-rpgs-that-perfected-character-progression-exploring-best-rpg-systems-ever-made.htm)

### Minecraft-Specific Feedback APIs
- [PaperMC Docs — Boss Bars (Adventure)](https://docs.papermc.io/adventure/bossbar/)
- [Paper API — Player Javadoc](https://jd.papermc.io/paper/1.21.11/org/bukkit/entity/Player.html)
- [GitHub — ParticleEffects Library](https://github.com/4kills/ParticleEffects)
- [Minecraft Wiki — PvP Tutorial](https://minecraft.wiki/w/Tutorial:PvP_(Java_Edition))

### RPG & FPS Design (General)
- [Game Design Skills — 15 Game Design Principles](https://gamedesignskills.com/game-design/concepts/)
- [Game Design Skills — FPS Fundamentals](https://gamedesignskills.com/game-design/fps/)
- [Celia Hodent — The Gamer's Brain: UX of Engagement (GDC17)](https://celiahodent.com/gamers-brain-part-3-ux-engagement-immersion-retention-gdc17-talk/)
- [Ashes of Creation Forums — PvP and Human Psychology](https://forums.ashesofcreation.com/discussion/62629/pvp-human-psychology-and-making-punishing-systems-feel-good)
- [Kaylriene — Additive vs Subtractive Ability Design](https://kaylriene.com/2022/01/03/ability-design-and-rpgs-the-difference-of-additive-and-subtractive-designs/)
