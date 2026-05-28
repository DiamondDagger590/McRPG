package us.eunoians.mcrpg.quest.chain;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A single step in a quest chain, referencing a quest definition by key.
 * Steps may have optional start conditions and expiration behaviors.
 * <p>
 * Conditions ship as an interface with no built-in implementations — they are
 * provided for third-party extensibility and future built-in conditions.
 * Of the {@code onQuestExpire} behaviors, only {@code "fail-chain"} is functional
 * in the current implementation.
 *
 * @param questKey      the quest definition key this step starts
 * @param conditions    optional conditions that must pass before this step starts
 * @param onQuestExpire what happens when this step's quest expires
 *                      (only "fail-chain" functional currently)
 * @param maxRetries    max retry count for "retry" expire behavior (-1 for unlimited)
 */
public record QuestChainStep(
        @NotNull NamespacedKey questKey,
        @NotNull List<QuestChainStartCondition> conditions,
        @NotNull String onQuestExpire,
        int maxRetries
) {

    /**
     * Creates a step with no conditions and default expiration behavior.
     *
     * @param questKey the quest definition key
     * @return a new step with {@code "fail-chain"} expiration behavior and no conditions
     */
    @NotNull
    public static QuestChainStep simple(@NotNull NamespacedKey questKey) {
        return new QuestChainStep(questKey, List.of(), "fail-chain", -1);
    }
}
