package us.eunoians.mcrpg.quest.chain.trigger.builtin;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.quest.chain.trigger.ChainAutoStartTrigger;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.Optional;

/**
 * A {@link ChainAutoStartTrigger} that fires the first time a player ever joins the server
 * (i.e., when {@code PlayerJoinEvent} fires with {@code Player#hasPlayedBefore()} returning
 * {@code false}).
 * <p>
 * Evaluation is handled by {@code QuestChainFirstJoinListener}. All chains whose
 * {@code auto-start.trigger} resolves to this key are evaluated for the joining player.
 */
public final class FirstJoinChainAutoStartTrigger implements ChainAutoStartTrigger {

    public static final NamespacedKey KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "first_join");

    @NotNull
    @Override
    public NamespacedKey getKey() {
        return KEY;
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.of(McRPGExpansion.EXPANSION_KEY);
    }
}
