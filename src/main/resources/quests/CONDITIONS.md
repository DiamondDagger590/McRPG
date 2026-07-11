# Template Conditions — Integration Guide

This document explains how template conditions work in McRPG: what built-in conditions are available, how they are configured in YAML, where they are evaluated at runtime, and how to implement a custom condition type.

**Audience:** Plugin developers and AI agents working on quest template or board features.

---

## 1. Overview

A `TemplateCondition` is a predicate evaluated against a `ConditionContext` to control whether a template element is included in a generated quest, whether a prerequisite is met, or whether a reward fallback should be used.

Conditions are used in three main areas:

| Area | YAML key | Purpose |
|------|----------|---------|
| Template element gating | `condition:` on phases, stages, or objectives | Controls whether the element is included in the generated quest definition |
| Template prerequisite | `prerequisite:` on the template root | Controls whether a template is eligible for a player |
| Reward fallback | `fallback.condition:` on reward entries | Controls whether the fallback reward is granted instead of the primary |

All three areas use the same condition types, the same `ConditionParser`, and the same `ConditionContext` — only the context factory method differs (see section 5).

---

## 2. The `TemplateCondition` Interface

```java
public interface TemplateCondition extends McRPGContent {

    @NotNull NamespacedKey getKey();

    boolean evaluate(@NotNull ConditionContext context);

    @NotNull TemplateCondition fromConfig(@NotNull Section section);

    @NotNull default Map<String, Object> serializeConfig() { return Map.of(); }
}
```

| Method | Purpose |
|--------|---------|
| `getKey()` | Stable `NamespacedKey` identifying this condition type (e.g. `mcrpg:chance`). |
| `evaluate(context)` | Returns `true` if the condition passes. Must handle missing context fields gracefully. |
| `fromConfig(Section)` | Factory: reads type-specific YAML and returns a new configured instance. The prototype in the registry stays unconfigured. |
| `serializeConfig()` | Returns a map of config keys for JSON persistence. Must **not** include a `"type"` key — the serializer adds the discriminator. |

**Lifecycle:**

1. A prototype instance is registered in `TemplateConditionRegistry` via `ContentExpansion`.
2. When YAML is loaded, `ConditionParser.parseSingle(section)` dispatches to the right prototype's `fromConfig`.
3. The configured instance is stored on the template element, prerequisite, or reward fallback.
4. At evaluation time, `evaluate(context)` is called with a context appropriate to the evaluation site.

---

## 3. Built-in Condition Types

### `chance`

Evaluates to `true` with the given probability using the context's seeded `Random`. If no random is available (e.g., reward-grant context), returns `true`.

| Shorthand | `fromConfig` key | Type |
|-----------|-----------------|------|
| `chance: 0.5` | `chance` | `double` in `[0.0, 1.0]` |

### `rarity-at-least`

Evaluates to `true` if the rolled rarity is at least as rare as the specified minimum. Lower weight = rarer, so the condition passes when `rolledWeight <= minimumWeight`. Returns `true` if no rarity context is available.

| Shorthand | `fromConfig` key | Type |
|-----------|-----------------|------|
| `rarity-at-least: epic` | `min-rarity` | `NamespacedKey` (bare key defaults to `mcrpg:`) |

Valid rarity values in the 5-tier system: `common`, `uncommon`, `rare`, `epic`, `legendary`.

Note: the shorthand key (`rarity-at-least`) and the `fromConfig` key (`min-rarity`) differ. Both work — shorthand is for YAML convenience, `fromConfig` is for explicit `type:` syntax and JSON round-trips.

### `permission`

Evaluates to `true` if the player has the specified Bukkit permission node. Returns `false` if no player UUID is available or the player is offline.

| Shorthand | `fromConfig` key | Type |
|-----------|-----------------|------|
| `permission: "mcrpg.quest.legendary"` | `permission` | `String` |

### `variable`

Evaluates to `true` if a resolved template variable satisfies a check. Returns `true` if no resolved variables are available (safe default for non-generation contexts). Returns `false` if the variable is missing.

**Shorthand:**

```yaml
variable:
  name: "biome"
  contains-any:
    - "PLAINS"
    - "FOREST"
```

**Check types:**

| Key | Check | Applies to |
|-----|-------|------------|
| `contains-any` | List membership | `POOL` variables (merged value list) or string values |
| `greater-than` | `value > threshold` | `RANGE` variables (numeric) |
| `less-than` | `value < threshold` | `RANGE` variables (numeric) |
| `at-least` | `value >= threshold` | `RANGE` variables (numeric) |
| `at-most` | `value <= threshold` | `RANGE` variables (numeric) |

Exactly one check key must be present per `variable` block.

### `min-completions`

Evaluates to `true` if the player has completed at least N quests, optionally filtered by board category and/or minimum rarity. Returns `false` if no player UUID or completion history is available.

**Shorthand:**

```yaml
min-completions: 5
category: mcrpg:daily_mining    # optional — only count completions in this category
min-rarity: mcrpg:rare          # optional — only count completions at this rarity or rarer
```

### `all` / `any` (compound)

Combines multiple child conditions with AND (`all`) or OR (`any`) logic. Children are a named map — labels are for readability only.

**Shorthand:**

```yaml
all:
  must-be-rare:
    rarity-at-least: rare
  coin-flip:
    chance: 0.5
```

```yaml
any:
  has-permission:
    permission: "mcrpg.vip"
  veteran:
    min-completions: 20
```

Children can be any condition type, including nested `all`/`any` blocks.

---

## 4. YAML Configuration

Conditions are parsed by [`ConditionParser`](board/template/condition/ConditionParser.java). The parser tries keys in this order:

1. `all` → compound AND
2. `any` → compound OR
3. `rarity-at-least` → rarity gate
4. `chance` → probability check
5. `variable` → variable check
6. `permission` → permission check
7. `min-completions` → completion prerequisite
8. `type` → explicit type key (any registered condition, including third-party)

If none match, an `IllegalArgumentException` is thrown.

**Entry points in YAML:**

```yaml
# Template element condition (phase/stage/objective)
condition:
  chance: 0.3

# Template prerequisite
prerequisite:
  min-completions: 10

# Reward fallback condition
fallback:
  condition:
    permission: "mcrpg.title.hero"
  reward:
    type: mcrpg:experience
    skill: MINING
    amount: 1000
```

**Explicit `type:` syntax** — for third-party conditions or when you prefer explicit keys over shorthand:

```yaml
condition:
  type: myplugin:economy_check
  min-balance: 1000
  currency: gold
```

The `type:` value is a `NamespacedKey` (lowercased). The entire section (including `type:`) is passed to `fromConfig`.

---

## 5. Evaluation Contexts

`ConditionContext` is a record with six nullable fields. Different evaluation sites populate different subsets. Conditions handle missing fields gracefully — each condition's Javadoc documents what happens when a field it needs is null.

**Factory methods:**

| Factory | When used | Fields populated |
|---------|-----------|-----------------|
| `forTemplateGeneration(rarity, registry, random, vars)` | Shared board generation (no specific player) | rarity, registry, random, variables |
| `forPersonalGeneration(rarity, registry, random, vars, playerUUID, history)` | Personal offering generation | All fields |
| `forPrerequisiteCheck(playerUUID, history)` | Template eligibility gating | player, history |
| `forRewardGrant(playerUUID, rarity, registry)` | Reward fallback evaluation at grant time | player, rarity, registry |

**Null-handling summary for built-in conditions:**

| Condition | Missing rarity/registry | Missing random | Missing variables | Missing player | Missing history |
|-----------|------------------------|---------------|-------------------|----------------|----------------|
| `chance` | — | returns `true` | — | — | — |
| `rarity-at-least` | returns `true` | — | — | — | — |
| `permission` | — | — | — | returns `false` | — |
| `variable` | — | — | returns `true` | — | — |
| `min-completions` | — | — | — | returns `false` | returns `false` |

The pattern: generation-context conditions (`chance`, `rarity-at-least`, `variable`) default to `true` when their context is missing, so they don't accidentally exclude content in non-generation evaluation sites. Player-context conditions (`permission`, `min-completions`) default to `false`.

**Impact on shared board generation:** During shared board generation (using `forTemplateGeneration`), no player UUID or completion history is available. This means player-dependent conditions like `permission` and `min-completions` return `false` — not `true`. Templates gated by these conditions as prerequisites are therefore excluded from shared boards. This is the correct behavior: it prevents prerequisite-gated templates (e.g., legendary templates requiring 15+ quest completions) from appearing on shared boards where no specific player's history can be checked.

---

## 6. Where Conditions Are Evaluated

### Template element gating

[`QuestTemplateEngine.generate()`](board/template/QuestTemplateEngine.java) evaluates `condition:` on each phase, stage, and objective during quest generation. Elements whose conditions return `false` are excluded from the generated `QuestDefinition`.

### Template prerequisites

[`QuestTemplate.getPrerequisite()`](board/template/QuestTemplate.java) stores the optional prerequisite condition. Prerequisites are now actively wired into the board offering selection pipeline via `QuestPool`.

- **Personal offering generation:** `ConditionContext.forPrerequisiteCheck(playerUUID, history)` is used, providing the player's UUID and completion history. Player-dependent conditions like `min-completions` are evaluated accurately.
- **Shared board generation:** `QuestPool` uses `forTemplateGeneration` which has no player UUID. Player-dependent conditions (`permission`, `min-completions`) return `false` in this context, so templates with those prerequisites are excluded from the shared board.

### Reward fallbacks

[`RewardFallback`](board/template/condition/RewardFallback.java) pairs a `TemplateCondition` with a fallback `QuestRewardType`. When the condition evaluates to `true`, the fallback reward is granted instead of the primary. See [`REWARDS.md`](REWARDS.md) section 5.

---

## 7. Registering a Custom Condition Type

Adding a custom condition type requires:

1. **Implement `TemplateCondition`** with a unique `NamespacedKey`, `evaluate`, `fromConfig`, and `serializeConfig`.
2. **Create a `ContentExpansion` subclass** (if your plugin doesn't already have one).
3. **Return a `TemplateConditionContentPack`** from `getExpansionContent()`.
4. **Register the expansion** with `ContentExpansionManager` during your plugin's `onEnable`.

Once registered, the condition is usable via the explicit `type:` syntax in YAML.

**Step 1: Implement the condition**

```java
public class EconomyCheckCondition implements TemplateCondition {

    public static final NamespacedKey KEY =
            new NamespacedKey("myplugin", "economy_check");

    private final double minBalance;
    private final String currency;

    public EconomyCheckCondition() {
        this.minBalance = 0;
        this.currency = "";
    }

    private EconomyCheckCondition(double minBalance, @NotNull String currency) {
        this.minBalance = minBalance;
        this.currency = currency;
    }

    @NotNull @Override
    public NamespacedKey getKey() { return KEY; }

    @Override
    public boolean evaluate(@NotNull ConditionContext context) {
        if (context.playerUUID() == null) {
            return false;
        }
        // Check the player's balance via your economy API
        return MyEconomyAPI.getBalance(context.playerUUID(), currency) >= minBalance;
    }

    @NotNull @Override
    public TemplateCondition fromConfig(@NotNull Section section) {
        return new EconomyCheckCondition(
                section.getDouble("min-balance", 0),
                section.getString("currency", "gold"));
    }

    @NotNull @Override
    public Map<String, Object> serializeConfig() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("min-balance", minBalance);
        map.put("currency", currency);
        return map;
    }

    @NotNull @Override
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.of(MyPluginExpansion.EXPANSION_KEY);
    }
}
```

**Step 2: Register via `ContentExpansion`**

```java
public class MyPluginExpansion extends ContentExpansion {

    public static final NamespacedKey EXPANSION_KEY =
            new NamespacedKey("myplugin", "myplugin-expansion");

    public MyPluginExpansion() { super(EXPANSION_KEY); }

    @NotNull @Override
    public Set<McRPGContentPack<? extends McRPGContent>> getExpansionContent() {
        TemplateConditionContentPack pack = new TemplateConditionContentPack(this);
        pack.addContent(new EconomyCheckCondition());
        return Set.of(pack);
    }

    // ... getExpansionName, etc.
}
```

**Step 3: Use in YAML**

```yaml
condition:
  type: myplugin:economy_check
  min-balance: 1000
  currency: gold
```

Custom conditions work everywhere built-in conditions work: template element gating, prerequisites, and reward fallbacks.

---

## 8. Serialization

Conditions attached to reward fallbacks in template-generated quest definitions are serialized to JSON by [`GeneratedQuestDefinitionSerializer`](board/template/GeneratedQuestDefinitionSerializer.java) and deserialized on load. The serializer uses `serializeConfig()` to produce the map and `fromConfig(Section)` to reconstruct via a BoostedYAML bridge.

**Rules for `serializeConfig()`:**

- Return a map whose keys match what `fromConfig(Section)` reads
- Do **not** include a `"type"` key — the serializer adds the type discriminator automatically
- The default implementation returns `Map.of()` (suitable for stateless conditions)
- `CompoundCondition` adds `"type"` per child in its serialized output so explicit-type parsing works on the deserialize path

---

## 9. Common Pitfalls

**Not handling null context fields.** Every `evaluate` implementation must handle null fields in `ConditionContext`. Follow the convention: generation-context conditions return `true` when their field is missing; player-context conditions return `false`.

**Putting `"type"` in `serializeConfig()`.** The serializer injects the type discriminator. Including it in your map causes duplicate or conflicting keys.

**Shorthand vs `fromConfig` key mismatch.** Some built-in conditions use different keys in shorthand (`rarity-at-least`) vs `fromConfig` (`min-rarity`). When implementing a custom condition, choose a single key used by both the YAML shorthand (if you add one) and `fromConfig`.

**Assuming player is always available.** Shared board generation creates conditions with `forTemplateGeneration` which has no player UUID. Permission and completion conditions correctly return `false` in this case, but custom conditions must follow the same pattern.

**Non-deterministic chance evaluation.** `ChanceCondition` uses `context.random()`, which is a seeded `Random` from the template engine. This ensures the same generation seed produces the same element selection. Custom probability-based conditions should also use `context.random()` rather than `ThreadLocalRandom` or `Math.random()`.

---

## Key Files Reference

| File | Purpose |
|------|---------|
| [`TemplateCondition`](board/template/condition/TemplateCondition.java) | Extensibility contract |
| [`ConditionContext`](board/template/condition/ConditionContext.java) | Evaluation context record + factory methods |
| [`ConditionParser`](board/template/condition/ConditionParser.java) | YAML → `TemplateCondition` dispatcher |
| [`TemplateConditionRegistry`](board/template/condition/TemplateConditionRegistry.java) | Type registry |
| [`ChanceCondition`](board/template/condition/ChanceCondition.java) | Probability check |
| [`RarityCondition`](board/template/condition/RarityCondition.java) | Rarity gate |
| [`PermissionCondition`](board/template/condition/PermissionCondition.java) | Bukkit permission check |
| [`VariableCondition`](board/template/condition/VariableCondition.java) | Template variable check |
| [`CompletionPrerequisiteCondition`](board/template/condition/CompletionPrerequisiteCondition.java) | Quest completion history check |
| [`CompoundCondition`](board/template/condition/CompoundCondition.java) | AND/OR combinator |
| [`VariableCheck`](board/template/condition/VariableCheck.java) | Sealed check types (`ContainsAny`, `NumericComparison`) |
| [`ComparisonOperator`](board/template/condition/ComparisonOperator.java) | `>`, `<`, `>=`, `<=` for numeric checks |
| [`QuestCompletionHistory`](board/template/condition/QuestCompletionHistory.java) | Completion data query interface |
| [`RewardFallback`](board/template/condition/RewardFallback.java) | Condition + fallback reward pair |
| [`QuestTemplateEngine`](board/template/QuestTemplateEngine.java) | Phase/stage/objective evaluation during generation |
| [`TemplateConditionContentPack`](../expansion/content/TemplateConditionContentPack.java) | Expansion pack for condition types |
| [`GeneratedQuestDefinitionSerializer`](board/template/GeneratedQuestDefinitionSerializer.java) | JSON serialization for conditions in generated definitions |
