package us.eunoians.mcrpg.quest.chain;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * An extensible condition that gates whether a chain step can be started for a player.
 * <p>
 * No built-in implementations ship in the current version. The interface is provided for
 * third-party extensibility and future built-in conditions. The condition evaluation
 * hook in {@link QuestChainManager} is not yet wired — defining and registering a condition
 * will not currently affect chain start behavior. When the wiring lands, all conditions
 * registered on a step must pass before {@link QuestChainManager} will call
 * {@link ChainQuestStarter} to start the step's quest.
 * <p>
 * Third-party implementations register via
 * {@link us.eunoians.mcrpg.expansion.content.QuestChainStartConditionContentPack}.
 *
 * <p><b>TODO:</b> Wire condition evaluation into {@link QuestChainManager#tryStartChain} and
 * {@link ChainQuestStarter} once the condition registry and step config parsing are implemented.
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
     * Evaluates whether the given player satisfies this condition.
     *
     * @param player the player to evaluate
     * @return {@code true} if the condition is satisfied
     */
    boolean evaluate(@NotNull Player player);
}
