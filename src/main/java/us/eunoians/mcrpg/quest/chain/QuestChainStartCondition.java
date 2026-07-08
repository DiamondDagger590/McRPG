package us.eunoians.mcrpg.quest.chain;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

/**
 * An extensible condition that gates whether a chain step can be started for a player.
 * <p>
 * Built-in implementations live in
 * {@link us.eunoians.mcrpg.quest.chain.condition.builtin}. The condition evaluation
 * hook in {@link QuestChainManager} is not yet wired — defining and registering a condition
 * will not currently affect chain start behavior. When the wiring lands, all conditions
 * registered on a step must pass before {@link QuestChainManager} will call
 * {@link ChainQuestStarter} to start the step's quest.
 * <p>
 * Third-party implementations register a
 * {@link us.eunoians.mcrpg.quest.chain.condition.QuestChainStartConditionType} in the
 * {@link us.eunoians.mcrpg.quest.chain.condition.QuestChainStartConditionTypeRegistry}
 * via {@link us.eunoians.mcrpg.expansion.content.QuestChainStartConditionContentPack}.
 */
@ApiStatus.Experimental
public interface QuestChainStartCondition {

    /**
     * Gets the unique key identifying this condition type.
     *
     * @return the condition type key
     */
    @NotNull
    NamespacedKey getKey();

    /**
     * Evaluates whether the given player satisfies this condition at the given point in time.
     *
     * @param player the player to evaluate
     * @param now    the current instant, provided by the caller for testability
     * @return {@code true} if the condition is satisfied
     */
    boolean evaluate(@NotNull Player player, @NotNull Instant now);
}
