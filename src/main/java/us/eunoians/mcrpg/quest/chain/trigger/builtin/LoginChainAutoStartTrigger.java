package us.eunoians.mcrpg.quest.chain.trigger.builtin;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.quest.chain.trigger.ChainAutoStartTrigger;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.Optional;

/**
 * A {@link ChainAutoStartTrigger} that fires on every player login. Chains using this trigger
 * are evaluated for re-start eligibility each time a player logs in, making it suitable for
 * repeatable chains that should restart after each session.
 * <p>
 * Evaluation is handled by {@code QuestChainLoginListener}, which runs at
 * {@link org.bukkit.event.EventPriority#NORMAL} to ensure re-resolution completes before
 * first-join chain evaluation at {@link org.bukkit.event.EventPriority#MONITOR}.
 * <p>
 * {@code tryStartChain} internally checks whether the player already has an active or
 * terminal state for each chain, so this trigger is idempotent — chains gated by
 * {@link us.eunoians.mcrpg.quest.chain.QuestChainRepeatMode#ONCE} will not restart on subsequent logins.
 */
public final class LoginChainAutoStartTrigger implements ChainAutoStartTrigger {

    public static final NamespacedKey KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "login");

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
