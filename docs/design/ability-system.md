# McRPG Ability System Design

> **Status:** Brainstorm / Early Design
> **Scope:** Combo activation, cast mechanics, CC system, base abilities, expansion packs

---

## Table of Contents

1. [Ability Activation — Click Combos](#1-ability-activation--click-combos)
2. [Resource System — Hunger](#2-resource-system--hunger)
3. [Cast Times and Charge Mechanics](#3-cast-times-and-charge-mechanics)
4. [CC System](#4-cc-system)
5. [Base McRPG Abilities](#5-base-mcrpg-abilities)
6. [Expansion Pack Abilities](#6-expansion-pack-abilities)
7. [Design Principles](#7-design-principles)

---

## 1. Ability Activation — Click Combos

Inspired by Wynncraft. Players activate abilities by inputting a sequence of right/left clicks in the air (not targeting a block or entity). Three combo slots are defined by pattern:

| Slot | Pattern |
|------|---------|
| 1    | R R R   |
| 2    | R R L   |
| 3    | R L R   |

Patterns are validated progressively — any input that cannot lead to a valid completion immediately fails and resets. A configurable timing window (default ~14 ticks / ~0.7s) governs how long the player has between inputs.

### Slot Assignment

A player's available `ComboActivatable` abilities are sorted alphabetically by their `NamespacedKey` string. The first ability maps to Slot 1, second to Slot 2, third to Slot 3. This is deterministic across server restarts and requires no per-player configuration.

### Failure Feedback

On a failed or expired pattern the player receives an audio cue (configurable). On a successful pattern that cannot fire (hunger, cooldown) the player receives a distinct cue and an action bar message.

---

## 2. Resource System — Hunger

Hunger is the primary activation resource. Cooldowns are a secondary optional gate per ability.

**Why hunger over mana:**
- Already exists on every player with no new UI
- Creates natural tension with combat (fighting while hungry is risky)
- Encourages resource management and build synergy (Herbal Remedy passive helps sustain)
- Avoids mana bar UI complexity while being universally understood

**Cost deduction** happens at fire time (after cast completes), not at cast start. If a cast is interrupted the hunger cost is refunded.

**Tier progression** reduces both hunger cost and cast time simultaneously — see Section 3.

---

## 3. Cast Times and Charge Mechanics

Instant-cast abilities remove counterplay. Introducing cast phases creates a three-layer counterplay structure:

1. **See the click pattern** → predict what's coming
2. **See the cast indicator** → interrupt or reposition
3. **React to the fired ability** → dodge or absorb

Not all abilities have cast times. The type of cast is part of the ability's design identity.

### Cast Types

**Wind-up** — fixed delay after combo completes. Player is committed. Good for high-damage melee abilities. Examples: Haymaker, Execute, Executioner's Swing, Earthen Slam.

**Chargeable** — player holds after the final combo input. Power scales with hold duration up to a defined maximum, after which it auto-fires at peak power. Examples: Shockwave (knockback force/radius scales), Icicle Rain (projectile count scales), Seismic Pulse (detection range scales), Haymaker.

**Channel** — effect occurs *during* the cast rather than at the end. Interrupted the moment input stops or the player takes hard CC. Examples: Ore Rush, Blizzard Stance, Spore Cloud.

**Instant (always)** — reaction abilities and movement abilities must never have cast times; they lose their purpose if delayed. Examples: Parry, Counter, Quick Reflexes proc, Shadow Step, Berserker's Cry.

### Tier Progression and Cast Time

Both hunger cost and cast time reduce with tier. This makes progression feel like mastery rather than just a stat bump — higher-tier players are more fluid, not just stronger.

| Tier | Hunger Cost | Cast Time |
|------|-------------|-----------|
| 1    | High        | ~1.5s     |
| 2    | Medium-High | ~1.0s     |
| 3    | Medium      | ~0.5s     |
| 4    | Low         | ~0.2s     |

Cast time never fully reaches zero to preserve some counterplay even at max tier.

Tier upgrades can also grant CC resistance as part of the wind-up:
- **Tier 2:** Soft CC no longer interrupts the cast
- **Tier 3:** Tenacity buff active during the wind-up
- **Tier 4:** Final portion of wind-up gains an Unstoppable window

### Interrupted Casts

- **Hunger:** Refunded in full on interrupt (cost deducts at fire time)
- **Combo pattern:** Not refunded — wasted input + positioning is the punishment
- **Partial charge:** Interrupted before minimum threshold → nothing fires, full refund. Interrupted after minimum threshold → fires a reduced version at current charge level (rewards partial commitment)

### Visual Telegraph (Vanilla Only)

| Signal | Use |
|--------|-----|
| Action bar countdown | `Haymaker... 0.8s` in yellow, green on complete |
| Particle halo | Orbiting particles increasing in speed/density as charge builds |
| Sound ramp | Pitch rises through the cast duration |
| Boss bar | Long channels where action bar is too transient |

The particle halo is particularly effective for charges — a half-charged Haymaker has a sparse slow ring; max charge has a dense fast halo. Enemies read threat level at a glance without any custom models.

---

## 4. CC System

### CC Type Taxonomy

Not all CC interrupts casts. The distinction between hard and soft CC is intentional and creates interesting tradeoffs.

**Hard CC** — interrupts active casts and charges:

| Type | Description |
|------|-------------|
| Stun | Full lockdown — no movement, no abilities |
| Airborne / Knockup | Upward velocity, always interrupts |
| Displacement | Forced movement (knockback, pull) — interrupts above a threshold |
| Silence | Specifically blocks ability activation, cancels active casts |

**Soft CC** — does NOT interrupt casts:

| Type | Description | Design Note |
|------|-------------|-------------|
| Root / Snare | No movement, cast continues | Rooted casters still fire — you locked them in place, now dodge it |
| Slow | Repositioning pressure only | |
| Blind | Blocks auto-attacks, abilities unaffected | |

The Root distinction is deliberate: a player who roots a charging Haymaker user made a skill choice. The ability fires, but from a predictable fixed position. The root player has their own counterplay obligation.

### Displacement Threshold

Not every small knockback should interrupt a cast or the system becomes oppressive:

- **Micro-displacement** (minor nudge) → no interrupt, repositioning only
- **Standard displacement** (Shockwave-tier) → interrupts wind-up and charge, does not interrupt abilities already in their fire frame
- **Heavy displacement** (dedicated interrupt tool, full knockup) → interrupts everything including mid-channel

### Status Effect Priority Stack

```
CC Immune  >  Unstoppable  >  Normal State  >  (Tenacity reduces duration)
```

**Unstoppable:**
- Displacement does not move the player (velocity eaten)
- Active cast/charge continues uninterrupted
- Player is still damageable — not invincibility
- Short duration, tied to specific ability windows (not a persistent state)
- Does NOT prevent damage-based cast interruption unless the ability explicitly specifies

**CC Immune:**
- All incoming CC effects are absorbed and ignored
- Rare, should come from deliberate active choices (Berserker's Cry, a Cleanse)
- Should not be passive or indefinite

### Unstoppable Windows by Ability

The Unstoppable window is a narrow phase at the *end* of a cast, not the whole thing. The wind-up remains interruptible.

| Ability | Interruptible Phase | Unstoppable Phase |
|---------|---------------------|-------------------|
| Haymaker | Full wind-up | 0.2s swing release |
| Reckless Charge | Pre-movement wind-up | During the dash |
| Ground Slam | Leap wind-up | Airborne phase |
| Executioner's Swing | Wind-up | Actual swing arc |
| Berserker's Cry | N/A (instant) | Full duration (this IS the CC-immune ability) |

This creates a counterplay window that rewards knowledge: *you had to interrupt Reckless Charge before he started moving.* That distinction is learnable and meaningful.

### Tenacity

A multiplier on CC duration received. Does not eliminate CC — a 2-second stun at 50 Tenacity becomes 1 second. Sources:

- **Iron Will** (Unarmed passive) — scales with skill level, fits the brawler fantasy
- **Battle-Hardened** (Axes passive) — bonus Tenacity when below 50% HP
- **Consumable/potion** — accessible to players outside these skill trees

### Anti-CC Options

Counterplay to CC needs to exist or CC-heavy builds become oppressive:

| Ability | Type | Description |
|---------|------|-------------|
| Cleanse | Active | Removes one CC effect, high hunger cost |
| Combat Instincts | Passive | First CC in a fight auto-triggers 1s CC immunity, once per engagement |
| Resilience | Passive (Unarmed) | Chance to break out of roots/slows early |
| Unstoppable Rage | Active (Axes, Tier 4 Berserker's Cry) | Full CC negation during duration |

### Dedicated Interrupt Abilities

Some abilities exist specifically to interrupt casters — high-value PvP picks:

- **Concussive Strike** (Unarmed) — quick jab, 0.5s micro-stun, interrupts any active cast. Low damage, pure utility
- **Hamstring** (Swords upgrade) — bleed stacks at high count apply Slow instead of just damage. Soft CC from the existing bleed system
- **Void Grasp** (Expansion — Shadow) — pulls target to you. Displacement = interrupt. Caveat: pulling a Shockwave bomber toward you is a risk. Counterplay to the counterplay

---

## 5. Base McRPG Abilities

Base McRPG has no ModelEngine dependency. All VFX use vanilla Bukkit: particles, sounds, velocity manipulation, potion effects, damage. Depth comes from **synergy between abilities**, not visual complexity.

### Design Philosophy for Base Abilities

- **Passives generate states, actives consume them** — Bleed is a state, Execute consumes it. Quick Reflexes generates a dodge window, Counter consumes it
- **Every active has a role: setup or finisher** — not just "deal damage"
- **Cross-skill synergies reward investment in multiple trees**

---

### Swords

#### Passives

| Ability | Description |
|---------|-------------|
| **Bleed** | Hits apply a stacking DoT (custom damage ticks, red particles). Core Swords status effect — many abilities interact with it |
| **Momentum** | Consecutive hits on the same target build a damage multiplier (up to 3-4 stacks, resets on miss or weapon swap) |
| **Parry** | Tiny reaction window after taking a hit. Right-clicking successfully plays a clang sound, reduces incoming damage, and opens a Counter window |
| **Duelist's Focus** | Bonus damage when only one enemy is nearby. Breaks when surrounded. Rewards 1v1 positioning |
| **Last Stand** | Damage ramps up sharply below ~25% HP. Synergises with Execute |

#### Actives (Combo)

| Ability | Type | Description |
|---------|------|-------------|
| **Whirlwind** | Wind-up | SWEEP_ATTACK AOE spin. Applies one Bleed stack to all targets hit |
| **Rupture** | Instant | Prevents target from regenerating HP for 4-5 seconds. Low hunger cost. The setup ability |
| **Execute** | Wind-up | Bonus damage scaling with target's missing HP. Pairs devastatingly with Bleed + Rupture |
| **Counter** | Instant | Only usable within 1 second of a successful Parry. Guaranteed high-damage strike with armor penetration |

**Core loop:** Bleed (passive procs) → Rupture (lock healing) → Whirlwind (spread bleeds) → Execute (finish low targets)

---

### Mining

#### Passives

| Ability | Description |
|---------|-------------|
| **Vein Sense** | Highlights ore within ~3 blocks passively (glowing effect). Range scales with skill level |
| **Experienced Extraction** | Bonus drops from ores mined frequently. Rewards specialisation |
| **Stone Whisperer** | Mining stone has a small chance to spawn a hidden ore block nearby |
| **Grip Mastery** | Pickaxe durability barely decreases |

#### Actives (Combo)

| Ability | Type | Description |
|---------|------|-------------|
| **Seismic Pulse** | Chargeable | Ground ripple that reveals all ores within 12-15 blocks (glowing effect) for ~10 seconds. Charge extends range |
| **Precision Drill** | Wind-up | Instantly mines a 1x1x5 tunnel in the direction you're looking |
| **Ore Rush** | Channel | All ore drops doubled for 8 seconds while channelling |
| **Vein Collapse** | Wind-up | Mines an entire connected vein at once. High hunger cost |

**Core loop:** Seismic Pulse → Vein Collapse (scout then harvest efficiently)

---

### Herbalism

#### Passives

| Ability | Description |
|---------|-------------|
| **Green Thumb** | Crops grow faster in your presence (slow bonemeal aura) |
| **Herbal Remedy** | Eating food triggers brief Regeneration I on top of normal saturation |
| **Forager** | Chance to find bonus drops (herbs, rare seeds) from natural vegetation |
| **Symbiosis** | If multiple Herbalism combo abilities are active simultaneously, hunger costs are reduced |

#### Actives (Combo)

| Ability | Type | Description |
|---------|------|-------------|
| **Nature's Grasp** | Wind-up | Roots nearby enemies (web + vine particles, Slowness). Setup ability for cross-skill combos |
| **Spore Burst** | Wind-up | Toxic AOE applying Nausea + Weakness briefly. Area denial |
| **Bountiful Harvest** | Instant | All harvests yield doubled drops for 15 seconds |
| **Regrowth** | Instant | Instantly grows all nearby crops to full |

**Cross-skill combo:** Nature's Grasp (root, soft CC — does not cancel casts) + Swords Execute is extremely strong in a duo

---

### Excavation

#### Passives

| Ability | Description |
|---------|-------------|
| **Archaeologist's Eye** | Chance to find buried artifacts in dirt/gravel/sand (special loot) |
| **Earth Sense** | Detects structures or dungeons within ~20 blocks (directional audio cue) |
| **Sifter** | Gravel and sand yield bonus drops |

#### Actives (Combo)

| Ability | Type | Description |
|---------|------|-------------|
| **Dust Storm** | Wind-up | Eruption of sand/dirt particles, AOE Blindness + Slowness |
| **Earthen Launch** | Instant | Velocity spike upward. Escape tool and repositioning |
| **Buried Cache** | Instant | 30% chance to discover a buried loot chest in the block below. High hunger cost, gambling mechanic |

---

### Unarmed

#### Passives

| Ability | Description |
|---------|-------------|
| **Iron Fist** | Bare-hand attacks scale damage with skill level, competitive with swords at high levels |
| **Toughened Skin** | Flat damage reduction when not holding a weapon |
| **Quick Reflexes** | Chance to dodge a hit entirely (ghost particles). Generates an empowerment window for follow-up abilities |
| **Grappler** | Hits have a chance to briefly root the target |
| **Iron Will** | Passive Tenacity scaling with Unarmed skill level |

#### Actives (Combo)

| Ability | Type | Description |
|---------|------|-------------|
| **Haymaker** | Chargeable | Massive single hit. Damage, knockback, and brief Slowness scale with charge duration. Wind-up gains Unstoppable window at Tier 4 |
| **Ground Slam** | Wind-up | Leap forward, AOE knockback in all horizontal directions (SWEEP particles). Unstoppable during airborne phase |
| **Iron Curtain** | Instant | Short window where a portion of melee damage taken is returned to the attacker |
| **Concussive Strike** | Instant | Quick jab, 0.5s micro-stun, interrupts any active cast. Dedicated interrupt tool |

**Core loop:** Quick Reflexes dodge → empowered Haymaker → Iron Curtain to punish retaliation

---

### Axes

#### Passives

| Ability | Description |
|---------|-------------|
| **Shield Shatter** | Attacks that hit a blocking player briefly reduce shield effectiveness |
| **Cleaving Edge** | Bonus armor penetration against heavily armored targets |
| **Heavy Blow** | Chance to apply Slowness on hit |
| **Intimidation** | Aura: nearby enemies receive a subtle attack speed debuff within 5 blocks |
| **Battle-Hardened** | Bonus Tenacity when below 50% HP |

#### Actives (Combo)

| Ability | Type | Description |
|---------|------|-------------|
| **Executioner's Swing** | Wind-up | Wide AOE cleave, high damage + defense debuff on targets for 5 seconds. Unstoppable during swing arc |
| **Reckless Charge** | Wind-up | Forward dash hitting everything in the path. Unstoppable during the dash |
| **Berserker's Cry** | Instant | Speed II + damage boost for 8 seconds, but also take 10% increased damage. Grants CC Immunity during duration |

---

### Woodcutting

#### Passives

| Ability | Description |
|---------|-------------|
| **Lumberjack** | Connected trees have a chance to fall at once on the first hit |
| **Bark Hide** | Wearing no chestplate grants a natural armor bonus |
| **Wood Identification** | Rare/special tree types are highlighted |

#### Actives (Combo)

| Ability | Type | Description |
|---------|------|-------------|
| **Timber Rush** | Instant | 10-second window of dramatically increased woodcutting speed. Lumberjack triggers 100% during this window |
| **Log Javelin** | Wind-up | Rip a log from the nearest tree and hurl it as a high-knockback projectile |

---

### Notable Cross-Skill Synergies

| Combo | Effect |
|-------|--------|
| Herbalism Nature's Grasp + Swords Execute | Root locks healing window open, Execute capitalises |
| Unarmed Quick Reflexes + Swords Parry/Counter | Both generate a Counter window; investing in both trees gives more trigger sources |
| Swords Bleed + Unarmed Haymaker | Haymaker damage scales off missing HP; Bleed drains it |
| Axes Heavy Blow + Swords Rupture | Slowed + can't regen = optimal Execute setup |
| Mining Seismic Pulse + Excavation Earthen Launch | Survey from height, launch to optimal harvest position |

---

## 6. Expansion Pack Abilities

Expansion packs can require ModelEngine. This enables custom entities, animations, and projectile models. The key design constraint is **asset reuse** — define a small set of primitive entity types and build many abilities on top of them.

### Shared Asset Primitives

| Primitive | Used For |
|-----------|---------|
| **Shard** (angular spike model) | Icicle Rain, Crystal Ward, Earth Spike, Shadow Lance |
| **Orb** (glowing sphere) | Void Orb, Ember Ball, Nature Seed, Soul Sphere |
| **Ring / Wave** (expanding flat ring) | Frost Nova, Thunder Ring, Bloom Ring, Seismic Ring |
| **Tendril** (snaking rope entity) | Vine Grasp, Shadow Tendrils, Lightning Arc |

"Icicle Rain" and "Crystal Shower" are the same spawning system with a material swap on the Shard primitive. The budget for each expansion pack is one or two new primitives at most; everything else reuses existing ones.

### Skill Books

Expansion abilities can be obtained from boss drops as Skill Books — a player who defeats a boss unlocks a weaker version of its signature attack. This creates:
- Organic progression tied to world content
- Lore: "I learned this from watching the Glacier Warden"
- Natural power ceiling (the player version is always weaker than the boss version)

---

### Expansion: Frostbound

**Theme:** Ice and cold
**New primitives:** Icicle Shard entity, Frost Nova ring
**Reused:** Existing particle effects (snowflake, frost)

| Ability | Skill | Cast Type | Description |
|---------|-------|-----------|-------------|
| **Icicle Rain** | Swords | Chargeable | Volley of Icicle Shards falls on target area. Charge increases projectile count and area. Damage + Slowness |
| **Frost Nova** | Unarmed | Wind-up | Expanding ice ring freezes (Slowness V + brief levitation cancel) everything it touches |
| **Blizzard Stance** | Axes | Channel | Aura that continuously slows all nearby enemies at cost of ongoing hunger drain |
| **Glacial Armor** | Any (passive) | — | Chance to crystallize briefly when hit, reducing damage for 1 second |
| **Ice Lance** | Swords | Wind-up | Hurled Icicle Shard that shatters on impact for AoE fragments |

**Boss:** *The Glacier Warden* — ice golem in a frozen dungeon. Attacks use the Icicle Shard entity. Drops **Frost Nova** or **Blizzard Stance** Skill Books.

---

### Expansion: Verdant Wrath

**Theme:** Nature's wrath, aggressive herbalism
**New primitives:** Vine Tendril entity, Bloom effect
**Reused:** Spore Cloud (from base Herbalism), Seed projectile (Orb primitive)

| Ability | Skill | Cast Type | Description |
|---------|-------|-----------|-------------|
| **Overgrowth** | Herbalism | Wind-up | Explosive vine burst from under the player. Lifts and damages nearby enemies |
| **Spore Cloud** | Herbalism | Channel | Persistent cloud entity that lingers and progressively applies Weakness stacks |
| **Thorn Armor** | Herbalism (passive) | — | Reflected melee damage + vine wrap particles when struck |
| **Verdant Strike** | Swords | Wind-up | Sword infused with nature energy. Hits plant seeds that grow into roots 2 seconds after impact |

**Boss:** *The Ancient Treant* — giant walking tree. Stomp uses the Vine Tendril asset. Drops **Overgrowth** Skill Book.

---

### Expansion: Shadowcraft

**Theme:** Dark, phantom, soul magic
**New primitives:** Shadow Orb, Phantom Silhouette entity
**Reused:** Tendril (from Verdant), Void Rift (Ring primitive with dark material)

| Ability | Skill | Cast Type | Description |
|---------|-------|-----------|-------------|
| **Phantom Step** | Swords/Unarmed | Instant | Short-range blink. Leaves a shadow decoy at origin for 2 seconds |
| **Soul Rend** | Swords | Wind-up | Slash dealing separate "soul damage" that ignores armor. Shadow particles. Contributes to Bleed synergies |
| **Void Grasp** | Unarmed | Instant | Dark tendril pulls nearest enemy toward you. Displacement = interrupt. Risk: you pulled them into melee range |
| **Shadow Echo** | Swords (passive) | — | 2 seconds after a swing, a phantom version repeats at half damage |

**Boss:** *The Revenant Duelist* — skeleton boss using shadow sword abilities. Drops **Phantom Step** or **Shadow Echo** Skill Books.

---

### Expansion: Storm Forged

**Theme:** Lightning and thunder
**New primitives:** Lightning Bolt entity, Static Orb
**Reused:** Ring primitive (Thunder Ring), Tendril (Lightning Arc)

| Ability | Skill | Cast Type | Description |
|---------|-------|-----------|-------------|
| **Thunderstrike** | Axes | Wind-up | Axe slam sends a Lightning Bolt entity forward along the ground |
| **Static Field** | Axes (passive) | — | Hitting enemies builds static charge. Releasing charge with a heavy attack fires a Thunder Ring |
| **Storm Dash** | Unarmed | Instant (Unstoppable during dash) | Lightning-trail dash hitting all enemies in path with static |
| **Chain Lightning** | Swords | Wind-up | Attack bounces a lightning bolt between up to 3 nearby enemies |

**Boss:** *The Storm Titan* — flying boss using all storm assets. Drops **Thunderstrike** or **Chain Lightning** Skill Books.

---

## 7. Design Principles

### Ability Roles
Every active should have a clear role as either a **setup** ability or a **finisher**. Not just "deal damage." Setup abilities enable something; finishers cash in a prior state.

### State Economy
Passives generate states. Actives consume them.

> Bleed (passive) is a state. Execute (active) is a consumer.
> Quick Reflexes (passive) generates a dodge window. Counter (active) consumes it.

This gives the click-combo inputs meaningful timing — the question is not just *which* ability but *when* the state is ready to be consumed.

### Power Budget and Cast Time
Cast times are part of the power budget. An ability with a 2-second wind-up can be more powerful than its instant-cast equivalent because the counterplay is baked in. This gives ability designers a lever beyond raw number tuning.

### Expansion Abilities vs. Base Abilities
Expansion abilities should be **power-equivalent** to base abilities, not superior. The hook is VFX, fantasy, and variety — not a pay-to-win power gap. A base Whirlwind and an expansion Frost Nova should trade roughly evenly at equivalent tiers.

### Boss Drop Lore Loop
Ability Skill Books dropped from bosses create organic progression and narrative. A player who has never encountered the Glacier Warden simply doesn't have Frost Nova yet. The ability is a memento of an encounter. Design boss attacks with this in mind — the player version should be a recognisable weaker echo of what the boss used against them.

### Counterplay Layers in PvP

A well-designed PvP exchange involves multiple overlapping counterplay decisions:

1. **See the combo input** → predict the ability
2. **React to the cast indicator** → interrupt or dodge
3. **React to the fired effect** → absorb, cleanse, or counter

All three layers should be accessible to a skilled player. No single ability should eliminate all three simultaneously without significant cost (high hunger, long cooldown, self-debuff like Berserker's Cry).
