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
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.util.McRPGMethods;

/**
 * Listens on {@link PlayerLoadEvent} at {@link EventPriority#NORMAL} and performs two
 * actions in order:
 * <ol>
 *   <li>Re-resolves all ACTIVE chain states against current definitions (handles definition
 *       changes that occurred while the player was offline, such as removed steps or renamed
 *       quest keys).</li>
 *   <li>Evaluates {@code mcrpg:login}-triggered chains for repeatable re-start eligibility.</li>
 * </ol>
 * <p>
 * Must run at {@link EventPriority#NORMAL} so re-resolution completes before
 * {@link QuestChainFirstJoinListener} (at {@link EventPriority#MONITOR}) evaluates first-join
 * chains. Bukkit fires handlers in priority order LOWEST → MONITOR, so NORMAL always
 * precedes MONITOR for the same event.
 */
public class QuestChainLoginListener implements Listener {

    private final QuestChainManager chainManager;

    /**
     * Creates a new login listener.
     *
     * @param chainManager the chain manager used for re-resolution and chain starts
     */
    public QuestChainLoginListener(@NotNull QuestChainManager chainManager) {
        this.chainManager = chainManager;
    }

    /**
     * On player load at NORMAL priority, re-resolves ACTIVE chain states and evaluates
     * login-triggered chains.
     *
     * @param event the player load event
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerLoad(@NotNull PlayerLoadEvent event) {
        CorePlayer corePlayer = event.getCorePlayer();
        if (!(corePlayer instanceof McRPGPlayer mcRPGPlayer)) {
            return;
        }
        Player player = mcRPGPlayer.getAsBukkitPlayer().orElse(null);
        if (player == null) {
            return;
        }

        chainManager.reResolveOnLogin(player.getUniqueId(), () -> {
            NamespacedKey loginKey = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "login");
            QuestChainRegistry chainRegistry = McRPG.getInstance().registryAccess()
                    .registry(McRPGRegistryKey.QUEST_CHAIN);
            for (QuestChainDefinition chain : chainRegistry.getChainsForTrigger(loginKey)) {
                chainManager.tryStartChain(player, chain.getChainKey());
            }
        });
    }
}
