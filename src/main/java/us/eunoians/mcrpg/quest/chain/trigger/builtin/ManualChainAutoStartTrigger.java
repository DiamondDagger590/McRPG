package us.eunoians.mcrpg.quest.chain.trigger.builtin;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.quest.chain.trigger.ChainAutoStartTrigger;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.Optional;

/**
 * A {@link ChainAutoStartTrigger} that never fires automatically. Chains using this trigger must
 * be started explicitly by an admin command or via code (e.g., a third-party plugin calling
 * {@code QuestChainManager.startChain(...)}).
 */
public final class ManualChainAutoStartTrigger implements ChainAutoStartTrigger {

    public static final NamespacedKey KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "manual");

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
