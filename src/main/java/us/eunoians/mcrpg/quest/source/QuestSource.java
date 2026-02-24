package us.eunoians.mcrpg.quest.source;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.expansion.content.McRPGContent;

import java.util.Optional;

/**
 * Represents how a quest was obtained (board, ability upgrade, manual, etc.).
 * <p>
 * Registered via {@link QuestSourceRegistry} and content packs, following the same pattern
 * as {@link us.eunoians.mcrpg.quest.objective.type.QuestObjectiveType} and
 * {@link us.eunoians.mcrpg.quest.reward.QuestRewardType}. Each source type can carry
 * behavior beyond the base contract (e.g., a future {@code NpcQuestSource} might reference
 * an NPC ID).
 * <p>
 * Deserialization from DB: the {@code quest_source} column stores the key as a string,
 * resolved via {@link QuestSourceRegistry#get(NamespacedKey)}.
 */
public abstract class QuestSource implements McRPGContent {

    private final NamespacedKey key;

    protected QuestSource(@NotNull NamespacedKey key) {
        this.key = key;
    }

    /**
     * Gets the unique key identifying this quest source type.
     *
     * @return the source key
     */
    @NotNull
    public final NamespacedKey getKey() {
        return key;
    }

    /**
     * Whether quests from this source can be abandoned by the player.
     *
     * @return {@code true} if abandonable
     */
    public abstract boolean isAbandonable();
}
