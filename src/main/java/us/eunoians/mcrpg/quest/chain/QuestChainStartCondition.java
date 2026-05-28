package us.eunoians.mcrpg.quest.chain;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * An extensible condition that gates whether a chain step can be started for a player.
 * No built-in implementations ship in the current version — the interface is provided
 * for third-party extensibility and future built-in conditions.
 * <p>
 * Conditions are evaluated by {@link QuestChainManager} before starting a chain step.
 * All conditions on a step must pass for the step to start.
 * <p>
 * Third-party implementations register via {@link us.eunoians.mcrpg.expansion.content.QuestChainStartConditionContentPack}.
 */
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
