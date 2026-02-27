package us.eunoians.mcrpg.quest.board.distribution;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Immutable data record for one distribution tier's preview state.
 *
 * @param tierKey             the tier identifier
 * @param qualifies           whether the player currently qualifies for this tier
 * @param currentContribution the player's current contribution amount
 * @param currentPercent      the player's contribution as a percentage of total
 * @param projectedRewards    lore components describing projected rewards
 */
public record DistributionPreviewEntry(
        @NotNull String tierKey,
        boolean qualifies,
        long currentContribution,
        double currentPercent,
        @NotNull List<Component> projectedRewards
) {
    public DistributionPreviewEntry {
        projectedRewards = List.copyOf(projectedRewards);
    }
}
