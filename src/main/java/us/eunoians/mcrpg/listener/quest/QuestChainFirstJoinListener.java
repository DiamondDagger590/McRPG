package us.eunoians.mcrpg.listener.quest;

import com.diamonddagger590.mccore.event.player.PlayerLoadEvent;
import com.diamonddagger590.mccore.player.CorePlayer;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.quest.chain.QuestChainDefinition;
import us.eunoians.mcrpg.quest.chain.QuestChainManager;
import us.eunoians.mcrpg.quest.chain.QuestChainRegistry;
import us.eunoians.mcrpg.quest.chain.trigger.builtin.FirstJoinChainAutoStartTrigger;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;

/**
 * Listens on {@link PlayerLoadEvent} at {@link EventPriority#MONITOR} and evaluates all
 * chains whose {@code auto-start.trigger} is {@code mcrpg:first_join} for the loading player.
 * <p>
 * {@code tryStartChain} internally checks whether the player already has state for each chain,
 * making this listener idempotent — it runs on every login but only starts chains the player
 * has never encountered before.
 * <p>
 * Runs at {@code MONITOR} priority so {@link QuestChainLoginListener} (at {@code NORMAL})
 * can complete re-resolution before first-join chains are evaluated.
 */
public class QuestChainFirstJoinListener implements Listener {

    private final QuestChainManager chainManager;

    /**
     * Creates a new first-join listener.
     *
     * @param chainManager the chain manager used to attempt chain starts
     */
    public QuestChainFirstJoinListener(@NotNull QuestChainManager chainManager) {
        this.chainManager = chainManager;
    }

    /**
     * On player load at MONITOR priority, evaluates all first-join chains for the player.
     * This runs after {@link QuestChainLoginListener} has completed re-resolution, ensuring
     * {@code tryStartChain} sees up-to-date chain state.
     *
     * @param event the player load event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerLoad(@NotNull PlayerLoadEvent event) {
        CorePlayer corePlayer = event.getCorePlayer();
        if (!(corePlayer instanceof McRPGPlayer mcRPGPlayer)) {
            return;
        }
        Player player = mcRPGPlayer.getAsBukkitPlayer().orElse(null);
        if (player == null) {
            return;
        }

        NamespacedKey firstJoinKey = FirstJoinChainAutoStartTrigger.KEY;
        QuestChainRegistry chainRegistry = McRPG.getInstance().registryAccess()
                .registry(McRPGRegistryKey.QUEST_CHAIN);
        for (QuestChainDefinition chain : chainRegistry.getChainsForTrigger(firstJoinKey)) {
            chainManager.tryStartChain(player, chain.getChainKey());
        }
    }
}
