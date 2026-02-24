package us.eunoians.mcrpg.quest.board.template;

import org.jetbrains.annotations.Nullable;

/**
 * Per-template override for a rarity's difficulty and/or reward multiplier.
 * {@code null} values mean "use the global rarity registry value".
 *
 * @param difficultyMultiplier override difficulty multiplier, or null to use global
 * @param rewardMultiplier     override reward multiplier, or null to use global
 */
public record RarityOverride(
        @Nullable Double difficultyMultiplier,
        @Nullable Double rewardMultiplier
) {}
