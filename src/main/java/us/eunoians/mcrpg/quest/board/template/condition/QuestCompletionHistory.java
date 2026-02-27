package us.eunoians.mcrpg.quest.board.template.condition;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Read-only interface that abstracts over quest completion log data, providing
 * query methods for condition evaluation without coupling conditions to the
 * database layer.
 */
public interface QuestCompletionHistory {

    /**
     * Counts quests this player has completed, optionally filtered by
     * board category key and minimum rarity.
     *
     * @param playerUUID  the player to query
     * @param categoryKey if non-null, only count quests from this board category
     * @param minRarity   if non-null, only count quests at or above this rarity
     * @return the number of matching completed quests
     */
    int countCompletedQuests(@NotNull UUID playerUUID,
                            @Nullable NamespacedKey categoryKey,
                            @Nullable NamespacedKey minRarity);
}
