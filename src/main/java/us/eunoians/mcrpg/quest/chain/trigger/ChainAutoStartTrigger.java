package us.eunoians.mcrpg.quest.chain.trigger;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.expansion.content.McRPGContent;

/**
 * An extensible trigger that defines <em>when</em> the chain system evaluates whether to start
 * a chain for a player. Triggers are independent of conditions (which define <em>if</em> the
 * chain should start for a specific player).
 * <p>
 * Each trigger is identified by a {@link NamespacedKey} and is registered in the
 * {@link ChainAutoStartTriggerRegistry}. Third-party plugins register custom triggers via
 * {@link us.eunoians.mcrpg.expansion.content.ChainAutoStartTriggerContentPack}.
 * <p>
 * Built-in triggers have their evaluation logic in dedicated listeners
 * (e.g., {@code QuestChainFirstJoinListener}), not in the trigger itself. The trigger serves
 * only as a registry marker that chain definitions reference by key.
 */
public interface ChainAutoStartTrigger extends McRPGContent {

    /**
     * Gets the unique key identifying this trigger type.
     *
     * @return the trigger key
     */
    @NotNull
    NamespacedKey getKey();
}
