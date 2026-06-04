package us.eunoians.mcrpg.quest.chain;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A single step in a quest chain, referencing a quest definition by key.
 * Steps may have optional start conditions, expiration behaviors, and preview metadata.
 *
 * @param questKey      the quest definition key this step starts
 * @param conditions    optional conditions that must pass before this step starts
 * @param onQuestExpire what happens when this step's quest expires
 * @param maxRetries    max retry count for "retry" expire behavior (-1 for unlimited)
 * @param previewItem   pre-built display item for the locked GUI state, built at config load
 *                      time. Null if no preview section is configured — the GUI falls back to
 *                      the quest definition's locale display or a generic item.
 */
public record QuestChainStep(
        @NotNull NamespacedKey questKey,
        @NotNull List<QuestChainStartCondition> conditions,
        @NotNull String onQuestExpire,
        int maxRetries,
        @Nullable ItemStack previewItem
) {

    /**
     * Creates a step with no conditions, no preview, and default expiration behavior.
     *
     * @param questKey the quest definition key
     * @return a new step with {@code "fail-chain"} expiration behavior and no conditions
     */
    @NotNull
    public static QuestChainStep simple(@NotNull NamespacedKey questKey) {
        return new QuestChainStep(questKey, List.of(), "fail-chain", -1, null);
    }
}
