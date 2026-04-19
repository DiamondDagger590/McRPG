package us.eunoians.mcrpg.stat;

import org.jetbrains.annotations.NotNull;

/**
 * An immutable modifier that contributes to a {@link CombatStatInstance}'s effective value.
 * <p>
 * Modifiers come from sources like passive abilities, equipped items, or temporary buffs.
 * Each modifier is identified by a unique {@code sourceKey} so it can be added and removed
 * cleanly when the source is slotted/unslotted or a buff expires.
 * <p>
 * Effective max is computed as: {@code (base + sumFlatBonuses) * (1 + sumPercentBonuses)}.
 *
 * @param sourceKey    Unique identifier for the source of this modifier (e.g., ability key string,
 *                     item ID). Used as the map key in {@link CombatStatInstance}.
 * @param flatBonus    Flat additive bonus applied before percentage scaling.
 * @param percentBonus Percentage bonus (0.1 = +10%). Applied multiplicatively after flat bonuses.
 */
public record CombatStatModifier(@NotNull String sourceKey, double flatBonus, double percentBonus) {
}
