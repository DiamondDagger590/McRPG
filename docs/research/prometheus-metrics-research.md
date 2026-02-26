# McRPG Metrics Aggregation Research: Prometheus & Alternatives

## Executive Summary

McRPG needs a system to collect game balance metrics (XP rates, ability usage, level progression, etc.) from many independent Minecraft servers and aggregate them centrally for analysis. This research evaluates Prometheus and alternatives for this unique use case — **crowdsourced telemetry from untrusted, NAT'd servers you don't control**.

**Key finding: Prometheus is NOT the right tool for this.** Its pull-based architecture requires network access to every monitored server, which is fundamentally incompatible with thousands of independent Minecraft servers behind NATs and firewalls. A **push-based architecture** is required.

**Recommended approach:** OpenTelemetry Java SDK (in-plugin instrumentation) → Custom validation API gateway → VictoriaMetrics or Grafana Mimir (time-series backend) → Grafana (visualization/dashboards).

---

## Table of Contents

1. [Current State of McRPG](#1-current-state-of-mcrpg)
2. [Why Prometheus Doesn't Fit](#2-why-prometheus-doesnt-fit)
3. [Recommended Architecture](#3-recommended-architecture)
4. [Backend Options Comparison](#4-backend-options-comparison)
5. [What Metrics to Collect](#5-what-metrics-to-collect)
6. [Loadout Composition & Ability Co-Occurrence Metrics](#6-loadout-composition--ability-co-occurrence-metrics)
7. [PvP Win/Loss Tracking](#7-pvp-winloss-tracking)
8. [Security & Trust Model](#8-security--trust-model)
9. [Privacy & GDPR Compliance](#9-privacy--gdpr-compliance)
10. [How Other Games Do This](#10-how-other-games-do-this)
11. [Statistical Approaches for Balance Analysis](#11-statistical-approaches-for-balance-analysis)
12. [Implementation Roadmap](#12-implementation-roadmap)

---

## 1. Current State of McRPG

### bStats Status
- **Dependency included** (`org.bstats:bstats-bukkit:2.2.1`) and shaded in `build.gradle.kts`
- **Never initialized** — no `new Metrics()` call anywhere in the codebase
- The library is available but zero metrics are being collected

### What bStats Can and Cannot Do

bStats is the de facto standard for Minecraft plugin telemetry, but it's designed for **plugin adoption tracking**, not game balance analytics:

| What bStats Does Well | What bStats Cannot Do |
|---|---|
| Server count, player count | Histograms / distributions |
| Minecraft/Java version tracking | Sub-30-minute granularity |
| Simple pie charts, single-value lines | Complex multi-dimensional queries |
| Public dashboards | Private/internal analytics |
| Easy integration (few lines of code) | Event-level or per-action tracking |
| | Cross-server aggregation queries |
| | Custom PromQL/SQL-style analysis |

**Verdict:** Keep bStats for standard plugin adoption metrics (server count, version distribution). Build a **separate system** for detailed balance telemetry.

### Existing Data Infrastructure

McRPG already tracks rich per-player data in SQLite:
- **Skill data**: Total XP, current level per skill (Swords, Mining, Woodcutting, Herbalism)
- **Ability data**: Tier, cooldown, unlock status per ability
- **Experience modifiers**: Boosted XP, rested XP, redeemable XP/levels
- **Login/logout times**: Player session tracking
- **Configurable XP rates**: Per-material multipliers, spawn reason modifiers, shield blocking modifiers, XP multiplier caps

The plugin's event system fires events for every XP gain (`SkillGainExpEvent`), level up (`SkillGainLevelEvent`), ability activation, and more — these are natural instrumentation points.

---

## 2. Why Prometheus Doesn't Fit

### The Pull Model Problem

Prometheus uses a **pull-based** model: the Prometheus server initiates HTTP requests to scrape metrics from target endpoints at configured intervals. This means:

1. **The Prometheus server must reach every target** — impossible when targets are thousands of Minecraft servers behind NATs, firewalls, and consumer routers
2. **Server operators would need to expose HTTP endpoints** — a non-starter for security and usability
3. **Service discovery doesn't work** — there's no central registry of every McRPG server's IP/port

### What About Pushgateway?

The Prometheus Pushgateway accepts pushed metrics, but the Prometheus project **explicitly recommends against** using it for the NAT/firewall use case:
- It's a single point of failure
- Pushed series never expire (stale metrics persist forever)
- You lose the `up` metric for health monitoring
- No high availability support (instances don't sync)
- The only officially recommended use case is batch job outcomes

### What About Federation?

Prometheus federation allows Prometheus servers to scrape from other Prometheus servers. This assumes:
- You control all the Prometheus instances
- Each Minecraft server runs its own Prometheus
- Both are unrealistic for third-party servers

### What About Remote Write?

The Prometheus Remote Write spec explicitly states that push-based applications sending metrics was **"not a design goal."** It's designed for Prometheus-to-storage forwarding.

### Prometheus Java Client

The `prometheus/client_java` library (v1.0.0+) is excellent for instrumenting Java applications with Counters, Gauges, Histograms, and Summaries. However, it's designed around exposing an HTTP `/metrics` endpoint for scraping — not for pushing to a remote backend. You *could* use it for local metric computation and then serialize/push separately, but at that point you're fighting the tool rather than using it as intended.

**Bottom line:** Prometheus is built for monitoring infrastructure you own and operate. McRPG's use case — crowdsourcing data from thousands of servers you don't control — needs a push-first architecture.

---

## 3. Recommended Architecture

```
┌──────────────────────────────────────────────────────┐
│                    McRPG Plugin                       │
│                                                       │
│  Game Events ──► Local Aggregation ──► Pre-aggregated │
│  (XP gains,      (per-skill hourly      metrics       │
│   ability use,    counters, histo-       (JSON/OTLP)  │
│   level ups)      grams, etc.)              │         │
│                                             │         │
│                              HTTPS POST ────┘         │
└──────────────────────────────────────────────────────┘
                         │
                         ▼
┌──────────────────────────────────────────────────────┐
│              Validation API Gateway                   │
│                                                       │
│  • API key authentication                             │
│  • Rate limiting (per-key, per-IP)                    │
│  • Schema validation                                  │
│  • Range checking (plausibility)                      │
│  • Server trust scoring                               │
│  • GDPR consent verification                          │
│                                                       │
└──────────────────────────────────────────────────────┘
                         │
                         ▼
┌──────────────────────────────────────────────────────┐
│           Time-Series Backend                         │
│     (VictoriaMetrics / Grafana Mimir)                │
│                                                       │
│  • Long-term metric storage                           │
│  • PromQL-compatible queries                          │
│  • Downsampling / retention policies                  │
│  • Multi-tenant isolation (optional)                  │
│                                                       │
└──────────────────────────────────────────────────────┘
                         │
                         ▼
┌──────────────────────────────────────────────────────┐
│                    Grafana                             │
│                                                       │
│  • Balance dashboards (XP rates, ability usage)       │
│  • Anomaly detection alerts                           │
│  • Cross-server comparison views                      │
│  • Trend analysis over time                           │
│                                                       │
└──────────────────────────────────────────────────────┘
```

### Why This Architecture

1. **Plugin does local pre-aggregation** — sends hourly summaries, not raw events. This reduces bandwidth, increases privacy (no per-player data leaves the server), and makes spoofing harder (faking a plausible statistical distribution is much harder than faking a single number).

2. **Validation API gateway** — the critical security layer. Every submission is authenticated, rate-limited, validated against plausible ranges, and scored for trustworthiness. This is where you solve the "bad actor" problem.

3. **Push-based by design** — the plugin initiates all connections outbound. No inbound ports needed. Works behind any NAT/firewall.

4. **Backend-agnostic instrumentation** — if you use OpenTelemetry SDK in the plugin, you can change the backend later without modifying the plugin.

---

## 4. Backend Options Comparison

### Option A: VictoriaMetrics (Recommended for Simplicity)

| Aspect | Details |
|---|---|
| **Push support** | Native — accepts InfluxDB line protocol, Prometheus remote write, OTLP, JSON, DataDog, Graphite |
| **Deployment** | Single binary, minimal config. Also has cluster mode for scale |
| **Resource usage** | 2-7x less CPU/RAM/disk than Prometheus for equivalent workloads |
| **Query language** | MetricsQL (PromQL superset with extra functions) |
| **Grafana support** | First-class Prometheus data source compatible |
| **Cost** | Free (open-source), or paid enterprise/cloud |
| **Best for** | Small-to-medium scale, low operational overhead |

### Option B: Grafana Mimir (Recommended for Scale)

| Aspect | Details |
|---|---|
| **Push support** | Prometheus remote write, OTLP native |
| **Deployment** | Microservices (complex) or monolithic mode (simpler, up to ~1M active series) |
| **Multi-tenancy** | Built-in via `X-Scope-OrgID` header |
| **Query language** | PromQL |
| **Grafana support** | Native (same vendor) |
| **Cost** | Free (open-source), or Grafana Cloud (free tier: 10K series) |
| **Best for** | Large scale, multi-tenant, if you want managed cloud option |

### Option C: InfluxDB

| Aspect | Details |
|---|---|
| **Push support** | Native HTTP write API |
| **Deployment** | Single binary or cloud |
| **Query language** | InfluxQL / Flux |
| **Grafana support** | Via InfluxDB data source |
| **Cost** | Free (OSS v2), paid cloud |
| **Best for** | If you want SQL-like queries or already use InfluxDB |

### Option D: Custom REST API + PostgreSQL/TimescaleDB

| Aspect | Details |
|---|---|
| **Push support** | You build it |
| **Flexibility** | Maximum control over validation, storage, queries |
| **Deployment** | Your API service + database |
| **Cost** | Hosting costs only |
| **Best for** | If you want total control and are willing to build/maintain it |

### Recommendation

**Start with VictoriaMetrics** for simplicity. A single binary handles ingestion, storage, and querying. If you outgrow it, migrate to Grafana Mimir. Either way, put a **custom validation API** in front of it — this is non-negotiable for crowdsourced data from untrusted servers.

---

## 5. What Metrics to Collect

### Balance-Critical Metrics

These are the metrics that actually help you understand game balance across servers:

#### Skill Progression Metrics
```
# Per skill, per hour, pre-aggregated on the plugin side
mcrpg_skill_xp_gained_total{skill="swords"}          # Total XP gained across all players
mcrpg_skill_xp_gained_histogram{skill="mining"}       # Distribution of XP gained per player-hour
mcrpg_skill_levelups_total{skill="herbalism"}          # Number of level-up events
mcrpg_skill_levelup_time_hours{skill="woodcutting"}    # Average hours between level-ups
mcrpg_skill_active_players{skill="swords"}             # Players who gained XP in this skill
```

#### Ability Metrics
```
mcrpg_ability_activations_total{ability="bleed"}       # Times ability was activated
mcrpg_ability_success_rate{ability="extra_ore"}         # Activation vs trigger ratio
mcrpg_ability_damage_dealt{ability="bleed"}             # Total damage from ability
mcrpg_ability_tier_distribution{ability="mass_harvest"} # How many players at each tier
```

#### Economy / XP Flow Metrics
```
mcrpg_xp_modifier_usage{modifier="rested"}             # How often rested XP is consumed
mcrpg_xp_modifier_usage{modifier="boosted"}            # Boosted XP consumption
mcrpg_xp_source_breakdown{source="block_break"}        # XP by source type
mcrpg_xp_source_breakdown{source="entity_damage"}      # XP by source type
```

#### Server Context Labels (Sent With Every Submission)
```
server_id="abc123"                    # Unique server identifier (API key hash)
plugin_version="2.0.0.5"             # McRPG version
minecraft_version="1.21"             # Server MC version
player_count_bucket="10-50"          # Anonymized player count range
xp_multiplier_limit="10.0"          # Server's configured XP cap
```

### The "Level Up Speed Per Hour Per Skill When Farming" Problem

You mentioned this specific metric. Here's how to approach it despite configurable XP rates:

1. **Collect the server's XP configuration alongside the metric** — include the configured XP multiplier, per-material rates, etc. as labels or metadata
2. **Normalize to a "baseline" configuration** — define a reference config, and weight/adjust submitted data to approximate what the rates would be under that baseline
3. **Alternatively, collect the raw math** — track `(XP gained, time spent, actions performed, configured multiplier)` as separate metrics, then compute normalized rates server-side:
   ```
   normalized_xp_per_hour = raw_xp_per_hour / server_xp_multiplier
   ```
4. **Accept imperfection** — even approximate data across hundreds of servers reveals patterns that no single server can show. If Mining levels 3x faster than Herbalism across 500 servers with varied configs, that's a real signal.

---

## 6. Loadout Composition & Ability Co-Occurrence Metrics

### Current Loadout System in McRPG

Based on the codebase (`Loadout.java`, `LoadoutHolder.java`, `LoadoutAbilityDAO.java`):
- Players have up to **3 loadout slots**, each holding up to **15 abilities**
- **Constraint**: only ONE `ActiveAbility` per skill per loadout (e.g., can't have both SerratedStrikes AND another Swords active in the same loadout)
- **~20+ equippable abilities** across 4 skills (Swords, Mining, Woodcutting, Herbalism), with a mix of passive and active types
- Loadouts are stored in `mcrpg_loadout` table (`holder_uuid`, `loadout_id`, `ability_id`)
- Players select their active loadout slot via `mcrpg_player_loadout_selection`

This is a rich source of balance data: which abilities do players actually choose to equip?

### Metric Type 1: Ability Popularity (Pie Charts)

The simplest metric — "what percentage of loadouts contain ability X?"

#### What to Collect (Plugin-Side)
```java
// Snapshot taken hourly: scan all online players' ACTIVE loadouts
class LoadoutCompositionSnapshot {
    // Per ability: how many active loadouts contain it
    Map<String, Integer> abilityEquipCounts;  // e.g., {"bleed": 142, "extra_ore": 98, ...}
    int totalActiveLoadouts;                   // e.g., 200
    int totalOnlinePlayers;                    // e.g., 200 (1 active loadout per player)
}
```

#### Resulting Metrics
```
mcrpg_loadout_ability_equipped{ability="bleed"}          142  # 71% of loadouts
mcrpg_loadout_ability_equipped{ability="extra_ore"}       98  # 49% of loadouts
mcrpg_loadout_ability_equipped{ability="serrated_strikes"} 87  # 43.5% of loadouts
mcrpg_loadout_ability_equipped{ability="ore_scanner"}      23  # 11.5% of loadouts
mcrpg_loadout_total_active                                200
```

In Grafana, this naturally becomes a pie chart via:
```promql
mcrpg_loadout_ability_equipped / mcrpg_loadout_total_active
```

#### Useful Slices
- **By skill**: "What % of loadouts have ANY Swords ability vs ANY Mining ability?" — tells you which skill trees feel rewarding enough to invest in
- **Active vs passive**: "Do players prefer passive abilities (always on) or active abilities (manual trigger)?" — if passives dominate, actives may feel too clunky
- **By tier**: "At what tier do players start equipping an ability?" — if nobody equips Bleed until Tier 3, maybe the early tiers feel underwhelming

### Metric Type 2: Ability Co-Occurrence (Combo Detection)

This is the more interesting and analytically rich metric: "which abilities are equipped **together**?"

#### The Market Basket Analysis Analogy

This is the same problem as retail's "customers who bought X also bought Y" — formally called **association rule mining**. The classic algorithms are:

- **Apriori**: Find all ability pairs (and triples) that appear together in loadouts more often than expected by chance
- **FP-Growth**: More efficient version of Apriori for larger item sets

The key metrics from association rule mining:

| Metric | Formula | What It Tells You |
|---|---|---|
| **Support** | `count(A ∧ B) / total_loadouts` | How common is this combo? |
| **Confidence** | `count(A ∧ B) / count(A)` | If you have A, how likely do you also have B? |
| **Lift** | `confidence(A→B) / support(B)` | Is this combo more common than random chance? Lift > 1 = synergy |

**Example**: If Bleed appears in 70% of loadouts and SerratedStrikes in 40%, random chance says they'd co-occur in 28% of loadouts. If they actually co-occur in 38%, the lift is 1.36 — suggesting players perceive a synergy (which makes sense since SerratedStrikes boosts Bleed activation).

#### What to Collect (Plugin-Side)

Computing full co-occurrence matrices on every server would be expensive. Instead, send **pair counts** for the most common pairs:

```java
class LoadoutCoOccurrenceSnapshot {
    // Top N ability pairs by co-occurrence count
    // N = 50 is plenty (with ~20 abilities, there are ~190 possible pairs)
    List<AbilityPair> pairCounts;
    int totalActiveLoadouts;

    static class AbilityPair {
        String abilityA;     // Lexicographically first (canonical ordering)
        String abilityB;
        int coOccurrences;   // Loadouts containing BOTH
    }
}
```

Actually, with only ~20 equippable abilities, there are at most `20 choose 2 = 190` pairs. That's small enough to send **all** pairs every hour. No need to truncate.

#### Resulting Metrics
```
# Gauge: number of active loadouts containing both abilities
mcrpg_loadout_pair{a="bleed",b="serrated_strikes"}        87
mcrpg_loadout_pair{a="bleed",b="deeper_wound"}            72
mcrpg_loadout_pair{a="bleed",b="vampire"}                 65
mcrpg_loadout_pair{a="extra_ore",b="its_a_triple"}        54
mcrpg_loadout_pair{a="extra_lumber",b="heavy_swing"}      41
mcrpg_loadout_pair{a="bleed",b="extra_ore"}               38  # Cross-skill combo
```

#### Server-Side Analysis

With pair counts and individual counts from many servers, compute lift server-side:

```promql
# Lift for (Bleed, SerratedStrikes) combo:
# lift = P(A∧B) / (P(A) × P(B))

(sum(mcrpg_loadout_pair{a="bleed",b="serrated_strikes"}) / sum(mcrpg_loadout_total_active))
/
(
  (sum(mcrpg_loadout_ability_equipped{ability="bleed"}) / sum(mcrpg_loadout_total_active))
  *
  (sum(mcrpg_loadout_ability_equipped{ability="serrated_strikes"}) / sum(mcrpg_loadout_total_active))
)
```

A **lift heatmap** in Grafana across all ability pairs would immediately reveal:
- **High lift pairs (> 1.5)**: Strong perceived synergies — are these intended or exploits?
- **Low lift pairs (< 0.5)**: Anti-synergies — abilities that players avoid combining. Why?
- **Cross-skill high-lift pairs**: Players combining abilities from different skill trees in unexpected ways

#### Scaling Consideration: Triples and Beyond

With 15-slot loadouts, you might want triples too (`A + B + C`). With 20 abilities, there are `20 choose 3 = 1140` triples — still manageable. But beyond triples the cardinality explodes. Recommendation:
- **Always send**: All pair counts (~190 pairs)
- **Optionally send**: Top 100 triple counts (pre-filtered on the plugin side to only triples that actually appear in loadouts)
- **Never send**: Quadruples or higher — analyze these server-side by correlating pair data

### Metric Type 3: Loadout Diversity Index

A single number that captures how "solved" or "diverse" the loadout meta is:

```java
// Shannon entropy of loadout compositions
// High entropy = diverse meta (many viable builds)
// Low entropy = solved meta (everyone runs the same build)
double loadoutDiversityIndex = -sum(p_i * log2(p_i))
    // where p_i = fraction of loadouts containing ability i
```

Track this over time. If diversity drops after a patch, something became dominant.

---

## 7. PvP Win/Loss Tracking

### The Core Problem: What Is a "Fight"?

In structured games (League of Legends, chess), a match has clear start/end boundaries. In Minecraft open-world PvP, there's no formal fight structure. A "fight" could be:
- A 1v1 duel lasting 30 seconds
- A surprise ambush that's over in 2 hits
- A prolonged 3v3 skirmish
- A player hitting someone once and running away
- Two players trading blows with a 30-second break mid-fight

You need a **heuristic** to define fight boundaries.

### Recommended Heuristic: Damage Window / Combat Session

The approach used by open-world PvP games like Albion Online and EVE Online:

```
FIGHT DEFINITION:
1. A "combat session" begins when Player A damages Player B (or vice versa)
2. The session is ACTIVE as long as damage events continue between the
   same pair of players with gaps of ≤ 15 seconds
3. The session ENDS when:
   a. One player dies → clear winner/loser
   b. 15 seconds pass with no damage between the pair → "disengage"
   c. One player logs out → "flee"
```

#### Codebase Integration Points

McRPG already hooks `EntityDamageByEntityEvent` at MONITOR priority (`OnAttackAbilityListener.java:15`). The PvP tracking system would layer on top:

```java
// New: PvPCombatTracker (runs alongside ability activation)
// Listens to EntityDamageByEntityEvent where both entities are Players

class PvPCombatSession {
    UUID playerA;
    UUID playerB;
    long sessionStartTick;
    long lastDamageTick;

    // Per-player stats within this session
    double damageDealtByA;
    double damageDealtByB;
    int hitsLandedByA;
    int hitsLandedByB;

    // Ability tracking within fight
    Map<String, Integer> abilitiesUsedByA;  // ability_id → activation count
    Map<String, Integer> abilitiesUsedByB;

    // Loadout snapshot (captured at fight start)
    Set<String> loadoutA;
    Set<String> loadoutB;
}
```

### Fight Outcomes

| Outcome | How Detected | Recording |
|---|---|---|
| **Kill** | `PlayerDeathEvent` with player killer | Clear win/loss |
| **Disengage** | 15s timeout with no damage | Partial outcome — compare damage dealt |
| **Flee (logout)** | `PlayerQuitEvent` during active session | Count as loss for the quitter |
| **Interrupted** | Third player joins the fight | Flag as multi-party, analyze separately |

For **disengagements** (no kill), you can still extract value:
- If Player A dealt 80% of total damage → "dominant" outcome for A
- If roughly equal damage → "draw"
- Threshold: `damage_ratio = damageByA / (damageByA + damageByB)` → Win if > 0.65, Draw if 0.35-0.65, Loss if < 0.35

### What to Send as Metrics

**Critical privacy point**: Never send individual fight data or player identifiers. Pre-aggregate on the plugin side:

```java
class PvPMetricsBatch {
    // Hourly aggregate

    int totalFights;                    // Total PvP combat sessions this hour
    int totalKills;                     // Sessions ending in a kill
    int totalDisengages;                // Sessions ending in disengage
    int totalFlees;                     // Sessions ending in logout

    // Ability performance in PvP
    // Per ability: (times_used_in_fights, fights_where_user_won, fights_where_user_lost)
    Map<String, AbilityPvPStats> abilityPvPPerformance;

    // Loadout vs Loadout outcomes (the really interesting data)
    // Hash loadout compositions into canonical "build archetypes"
    // then track win rates between archetypes
    List<ArchetypeMatchup> archetypeMatchups;

    static class AbilityPvPStats {
        String abilityId;
        int timesActivatedInFights;
        int fightsWhereEquippedAndWon;
        int fightsWhereEquippedAndLost;
        double totalDamageContributed;  // Damage dealt by this ability in PvP
    }

    static class ArchetypeMatchup {
        String archetypeHashA;    // Hash of sorted ability set
        String archetypeHashB;
        int winsForA;
        int winsForB;
        int draws;
    }
}
```

### Ability PvP Win Rate Metric

The most actionable balance metric:

```
# For each ability: what is the win rate when this ability is in the loadout?
mcrpg_pvp_ability_winrate{ability="bleed"}          0.54   # 54% — slightly above average
mcrpg_pvp_ability_winrate{ability="serrated_strikes"} 0.61 # 61% — potentially overpowered
mcrpg_pvp_ability_winrate{ability="vampire"}         0.58  # 58% — strong in PvP
mcrpg_pvp_ability_winrate{ability="ore_scanner"}     0.41  # 41% — utility, not combat-focused

mcrpg_pvp_ability_usage{ability="bleed"}             312   # Used in 312 fights this hour
mcrpg_pvp_ability_usage{ability="ore_scanner"}        14   # Rarely seen in PvP
```

**Important**: Win rate alone is misleading. You MUST pair it with **pick rate**:
- 60% win rate at 50% pick rate = probably overpowered
- 60% win rate at 2% pick rate = niche/skill-dependent, probably fine
- 45% win rate at 80% pick rate = underpowered but feels necessary (possible game design issue)

This is exactly how League of Legends and Overwatch analyze hero/champion balance — the "win rate vs pick rate scatter plot" is the standard industry tool.

### Loadout Archetype Tracking

Rather than tracking every unique loadout (too many combinations), group loadouts into **archetypes** based on their active abilities:

```java
// Archetype = the set of ACTIVE abilities in the loadout
// (Passives are excluded since they don't represent a "playstyle choice" as strongly)
//
// Example archetypes:
//   "Swords/SerratedStrikes + Mining/OreScanner"   → "Melee+Utility"
//   "Swords/SerratedStrikes + Woodcutting/HeavySwing" → "Full Melee"
//   "Mining/OreScanner + Mining/RemoteTransfer"     → "Pure Utility"

String archetypeHash = sortedActiveAbilities.stream()
    .map(NamespacedKey::toString)
    .collect(Collectors.joining("+"));
```

Then track **archetype vs archetype matchup win rates** — this reveals:
- Rock-paper-scissors dynamics (healthy: archetypes have strengths and weaknesses)
- Dominant archetypes (unhealthy: one build beats everything)
- Dead archetypes (no one runs them — abilities need buffing or reworking)

### Multi-Party Fights

When a third player enters an existing combat session (damages either participant):
- **Option A**: Promote to a "skirmish" and track separately from 1v1s. Don't count toward 1v1 balance stats.
- **Option B**: Track kill credit only — whoever lands the killing blow "wins," all others who contributed damage are "participants"
- **Recommendation**: Option A. 1v1 data is cleanest for balance. Skirmishes are interesting but introduce too many confounders (gear difference, health states, 2v1 unfairness).

### Skill-Based Matchmaking Consideration

Without skill-based matchmaking, raw win rates are noisy — a good player with a "weak" build beats a bad player with a "strong" build. Mitigations:
- **Volume**: Across hundreds of servers and thousands of fights, player skill averages out
- **Relative ranking**: You care about "is Bleed's win rate higher than DeeperWound's win rate" — both are affected equally by player skill noise
- **Confidence intervals**: Report win rates with error bars. A 55% ± 8% win rate is very different from 55% ± 1%

---

## 8. Security & Trust Model

### Threat Model

| Threat | Impact | Likelihood |
|---|---|---|
| **Data poisoning** — submitting false metrics to skew balance analysis | High | Medium |
| **Volume attacks** — flooding the API with submissions | Medium | Medium |
| **Identity spoofing** — impersonating other servers | Medium | Low |
| **Replay attacks** — re-sending old data | Low | Low |
| **Reverse engineering** — understanding the API to craft convincing fake data | Medium | High (plugin is open-source) |

### Multi-Layer Defense

#### Layer 1: Authentication
- **API key per server** — generated on first plugin enable, stored in plugin config
- **Key registration** — lightweight registration endpoint (no email required, just generates a key)
- **Server fingerprint** — hash of (plugin version, MC version, installed plugins list) sent with each submission. Detect if same key is used from wildly different servers
- **HTTPS only** — transport encryption for all submissions

#### Layer 2: Rate Limiting
- **Per API key**: Max 2 submissions per hour (since data is pre-aggregated hourly)
- **Per IP**: Max 10 keys per IP (prevents mass key generation)
- **Global**: Overall system capacity protection
- **Algorithm**: Token bucket (allows catch-up submissions after downtime)

#### Layer 3: Data Validation
- **Schema validation**: Reject malformed JSON/OTLP immediately
- **Range checking**: XP values, damage numbers, player counts must fall within physically possible ranges for Minecraft
- **Rate checking**: Changes per hour must be plausible (no server has 1 billion XP gained per hour)
- **Internal consistency**: Metrics from the same submission shouldn't contradict each other (e.g., total XP gained should roughly match sum of per-skill XP)

#### Layer 4: Trust Scoring
Inspired by CrowdSec's crowdsourced threat intelligence model:

```
Trust Score (0.0 - 1.0) based on:
├── Participation duration     (longer = higher trust)
├── Submission consistency     (stable patterns = higher trust)
├── Correlation with peers     (similar to other servers = higher trust)
├── Data plausibility          (within expected ranges = higher trust)
└── Volume of data             (more players = more statistically meaningful)
```

- **Score < 0.3**: Data is stored but excluded from aggregate dashboards
- **Score 0.3 - 0.7**: Data included with reduced weight
- **Score > 0.7**: Data included at full weight
- Trust score is **not disclosed** to the server operator (prevents gaming)

#### Layer 5: Statistical Outlier Detection
- **IQR method** on aggregated metrics — flag submissions outside 1.5× IQR
- **Contextual grouping** — compare servers within similar configuration brackets
- **Trend analysis** — sudden large changes in a server's metrics trigger review
- **Consensus check** — if 95% of servers agree on a range and 5% are outside it, weight the outliers less

### Open-Source Considerations

Since McRPG is open-source, sophisticated actors can read the validation logic and craft submissions that pass all checks. Mitigations:
- **Server-side validation logic is NOT in the plugin** — the API gateway's rules are private
- **Pre-aggregation makes spoofing harder** — generating a plausible histogram/distribution is much harder than faking a single number
- **Trust scoring penalizes inconsistency** — even if one submission is convincing, sustained fake data over weeks is very difficult to maintain
- **Statistical consensus** — fake data from a few servers is drowned out by real data from hundreds

---

## 9. Privacy & GDPR Compliance

### Core Principles

1. **Opt-in only** — telemetry must be explicitly enabled in config. Default: disabled.
   ```yaml
   configuration:
     telemetry:
       enabled: false  # Must be explicitly set to true
   ```

2. **No personal data** — never transmit player names, UUIDs, IPs, or any individually identifiable information. Only aggregate statistics.

3. **Pre-aggregation** — all data is aggregated on the plugin side before transmission. The central server never sees individual player actions.

4. **Transparency** — document exactly what data is collected in:
   - Plugin description on SpigotMC/Modrinth/Hangar
   - A `/mcrpg telemetry info` command that shows what will be sent
   - A config comment explaining each collected metric

5. **Data deletion** — provide a way for server operators to request deletion of all data associated with their API key

6. **EU considerations** — if feasible, host the backend in the EU to avoid GDPR cross-border transfer complications

### GDPR Specifics
- **Consent** (Art. 6(1)(a)): Opt-in config satisfies "freely given, specific, informed" consent
- **Data minimization** (Art. 5(1)(c)): Only collect balance-relevant aggregate metrics
- **Right to erasure** (Art. 17): API key deletion endpoint removes all associated data
- **Transparency** (Art. 13/14): In-plugin command + documentation satisfies notice requirements
- IP addresses are used only for rate limiting and never stored or linked to metric data

---

## 10. How Other Games Do This

### WarcraftLogs (World of Warcraft)
- Players upload combat log files via a companion app
- Backend processes event-level data for per-class/encounter rankings
- Trust: Data comes from game client logs (hard to fabricate without playing)
- Scale: Millions of logs from thousands of guilds

### Raider.IO (World of Warcraft)
- Scrapes Blizzard's official API for leaderboard data
- Very high trust (official source)
- Not applicable to McRPG (no official API to scrape)

### SimulationCraft / Raidbots
- Community contributes class action priority lists
- Raidbots runs simulations on Google Cloud
- Balance analysis via statistical comparison of simulated outcomes

### Amazon GameLift (2025)
- Added **OpenTelemetry-based** telemetry to all server SDKs
- Fleet-level, instance-level, and game-session-level metrics
- Validates that OTel is becoming the industry standard for game telemetry

### mcMMO (Direct Competitor)
- **No crowdsourced telemetry** — balance adjustments driven by community forum feedback
- Each server tunes independently with no cross-server data sharing
- **This is the gap McRPG can fill** — no Minecraft RPG plugin does this today

---

## 11. Statistical Approaches for Balance Analysis

### Pre-Aggregation on the Plugin Side

Rather than sending raw events, the plugin should compute and send:

```java
// Per skill, per hour:
class SkillMetricsBatch {
    String skillId;              // "mcrpg:swords"
    int activePlayerCount;       // Players who gained XP this hour
    long totalXpGained;          // Sum of all XP gained
    int totalActions;            // Number of XP-granting actions
    int levelUpCount;            // Number of level-up events
    int[] xpPerPlayerHistogram;  // Bucketed: [0-100, 100-500, 500-1000, 1000+]
    int minPlayerLevel;          // Lowest active player level
    int maxPlayerLevel;          // Highest active player level
    double medianPlayerLevel;    // Median active player level

    // Server config context
    double configuredXpMultiplier;
    String levelUpEquation;
}
```

### Server-Side Aggregation

Once data is in the time-series backend:

1. **Weighted aggregation**: Weight each server's contribution by `trust_score × sqrt(active_player_count)`. More players = more statistical power, but diminishing returns prevent mega-servers from dominating.

2. **Robust statistics**: Use **median** and **IQR** instead of mean/stddev. Crowdsourced data has outliers; robust statistics handle them gracefully.

3. **Trimmed means**: Discard top and bottom 5-10% of submissions before computing means. Removes both poisoned data and legitimate edge cases.

4. **Contextual grouping**: Before aggregating, group servers by:
   - XP multiplier range (1x, 2-5x, 5-10x)
   - Player count bracket (1-10, 10-50, 50-200, 200+)
   - Plugin version
   - Then aggregate within groups and compare across groups

5. **Bayesian updating**: Start with priors from your own test servers. Update beliefs as crowdsourced data arrives. Small amounts of poisoned data shift conclusions minimally.

6. **Normalization**: To compare XP rates across servers with different configs:
   ```
   normalized_rate = raw_xp_per_hour / (server_xp_multiplier × material_multiplier_avg)
   ```
   This isn't perfect (interactions between modifiers are complex) but gives a useful approximation.

### Detecting Balance Issues

With aggregated data, you can set up Grafana alerts for:
- **Skill imbalance**: If normalized XP/hour for Mining is 3x higher than Swords across most servers
- **Ability dominance**: If one ability's activation rate is 10x others at the same tier
- **Level curve problems**: If time-to-level suddenly spikes or drops at certain level thresholds
- **Config sensitivity**: If small config changes cause disproportionate outcome changes

---

## 12. Implementation Roadmap

### Phase 1: Foundation (In-Plugin Instrumentation)
1. **Initialize bStats** for standard plugin adoption metrics (server count, version, player count)
2. **Add metric collection hooks** at key event points:
   - `SkillGainExpEvent` → increment XP counters per skill
   - `SkillGainLevelEvent` → increment level-up counters
   - Ability activation events → track activation counts
3. **Add loadout snapshot collection** — hourly scan of online players' active loadouts for ability popularity and pair co-occurrence counts
4. **Implement local pre-aggregation** — hourly rollups of counters and histograms
5. **Add telemetry config section** — opt-in toggle, API key management, `/mcrpg telemetry` commands

### Phase 2: PvP Combat Tracking (In-Plugin)
1. **Implement PvPCombatTracker** — listen to `EntityDamageByEntityEvent` for player-vs-player damage
2. **Combat session state machine** — track active fights with a 15-second rolling timeout
3. **Fight outcome detection** — kill (via `PlayerDeathEvent`), disengage (timeout), flee (logout during session)
4. **Per-fight ability tracking** — record which abilities activated during each fight, per combatant
5. **Loadout snapshot at fight start** — capture both players' active loadouts for archetype analysis
6. **Pre-aggregate PvP stats hourly** — ability PvP win rates, archetype matchup outcomes, fight counts

### Phase 3: Backend (Central Collection)
1. **Deploy validation API gateway** — lightweight HTTP service (Go, Rust, or Java)
   - API key registration and authentication
   - Rate limiting
   - Schema and range validation
2. **Deploy VictoriaMetrics** (single binary) for time-series storage
3. **Deploy Grafana** for dashboards
4. **Set up recording rules** for pre-computed distributions (ability popularity ratios, co-occurrence lift)
5. **Test with your own servers** as the initial data source

### Phase 4: Trust & Security
1. **Implement trust scoring** — start simple (participation duration + data consistency)
2. **Add statistical outlier detection** — IQR-based flagging
3. **Server fingerprinting** — detect key sharing/misuse
4. **PvP-specific validation** — detect impossible fight counts, validate damage ranges, flag servers with implausible win rate distributions
5. **Monitor and iterate** — the trust model will need tuning based on real data

### Phase 5: Analysis & Dashboards
1. **Build balance dashboards** in Grafana:
   - XP rates per skill (normalized)
   - Level progression curves
   - **Ability popularity pie charts** — equipment rates across all servers
   - **Co-occurrence lift heatmap** — which ability pairs have synergy
   - **PvP ability win rate vs pick rate scatter plot** — the standard balance analysis tool
   - **Archetype matchup matrix** — win rates between loadout archetypes
   - **Loadout diversity index over time** — is the meta getting stale?
   - Cross-server comparison
2. **Set up alerts** for significant balance anomalies:
   - Ability win rate exceeding 58% with pick rate above 20%
   - Loadout diversity index dropping below threshold
   - Single archetype exceeding 30% pick rate
3. **Run association rule mining** (Apriori/FP-Growth) on aggregated loadout data periodically (weekly batch) to discover non-obvious synergies

### Phase 6: Community
1. **Public (read-only) dashboard** — let the community see aggregate balance data
2. **Server operator dashboard** — let individual servers compare their stats to the global average
3. **Balance feedback loop** — publish balance reports based on the data
4. **"Meta report"** — periodic auto-generated reports showing top loadout archetypes, ability tier lists based on PvP performance, and emerging combo trends

---

## Key Decisions to Make

1. **Backend choice**: VictoriaMetrics (simple, efficient) vs Grafana Mimir (scalable, multi-tenant) vs Custom API + TimescaleDB (maximum control)
2. **Instrumentation library**: OpenTelemetry Java SDK (future-proof, backend-agnostic) vs bare HTTP client (simpler, less dependency weight)
3. **Hosting**: Self-hosted VPS vs Grafana Cloud (free tier) vs AWS/GCP
4. **Opt-in vs opt-out**: Recommendation is opt-in for GDPR compliance, but this reduces data volume
5. **Submission frequency**: Hourly (recommended) vs every 30 min vs on-demand
6. **Public dashboards**: Whether to make aggregated data publicly viewable

---

## References

### Prometheus
- [Prometheus Push vs Pull](https://signoz.io/guides/is-prometheus-monitoring-push-or-pull/)
- [When to Use the Pushgateway](https://prometheus.io/docs/practices/pushing/)
- [Prometheus Federation](https://prometheus.io/docs/prometheus/latest/federation/)
- [Prometheus Remote Write Spec](https://prometheus.io/docs/specs/prw/remote_write_spec/)
- [Prometheus Java Client](https://github.com/prometheus/client_java)

### Alternatives
- [OpenTelemetry](https://opentelemetry.io/)
- [OpenTelemetry Java SDK](https://opentelemetry.io/docs/languages/java/sdk/)
- [OTel Java Metrics Performance](https://opentelemetry.io/blog/2024/java-metric-systems-compared/)
- [Grafana Mimir](https://grafana.com/oss/mimir/)
- [VictoriaMetrics](https://victoriametrics.com/)
- [VictoriaMetrics vs Prometheus](https://last9.io/blog/prometheus-vs-victoriametrics/)

### Security & Privacy
- [CrowdSec - Crowdsourced Threat Intelligence Model](https://github.com/crowdsecurity/crowdsec)
- [GDPR Telemetry Compliance](https://www.activemind.legal/guides/telemetry-data/)
- [Gaming & GDPR 2025](https://heydata.eu/en/magazine/gaming-gdpr-risks-are-rising-and-these-2025-cases-prove-it/)
- [Data Privacy for Game Developers](https://lootlocker.com/blog/essential-law-for-game-devs-a-game-dev-s-guide-to-data-privacy)

### Game Telemetry Precedents
- [WarcraftLogs](https://www.warcraftlogs.com/)
- [Raider.IO](https://raider.io/)
- [SimulationCraft](https://github.com/simulationcraft/simc)
- [Raidbots Architecture](https://medium.com/raidbots/raidbots-technical-architecture-303349d82784)
- [Amazon GameLift Telemetry (OTel-based)](https://aws.amazon.com/about-aws/whats-new/2025/10/amazon-gamelift-servers-telemetry-metrics/)
- [bStats](https://bstats.org/)

### Statistical Methods
- [Statistical Outlier Detection](https://www.numberanalytics.com/blog/10-statistical-outlier-detection-methods-accurate-analytics)
- [Unsupervised Outlier Detection](https://journalofbigdata.springeropen.com/articles/10.1186/s40537-021-00469-z)
