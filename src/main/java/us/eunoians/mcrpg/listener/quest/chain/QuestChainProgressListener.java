package us.eunoians.mcrpg.listener.quest.chain;

import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.event.quest.QuestCompleteEvent;
import us.eunoians.mcrpg.quest.chain.CascadeOrchestrator;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.quest.impl.scope.QuestScope;

/**
 * Listens for {@link QuestCompleteEvent} and advances any quest chain whose
 * current step is the quest that just completed. Delegates through
 * {@link CascadeOrchestrator} so auto-completing chain steps within a single
 * tick are batched into a cascade rather than producing individual messages.
 * <p>
 * Runs at {@link EventPriority#MONITOR} so other plugins receive the event first.
 * The chain manager uses the O(1) reverse index in {@code QuestChainPlayerData}
 * to determine whether the completed quest belongs to an active chain.
 */
public class QuestChainProgressListener implements Listener {

    private final CascadeOrchestrator cascadeOrchestrator;

    /**
     * Creates a new progress listener.
     *
     * @param cascadeOrchestrator the cascade orchestrator that wraps chain advancement
     */
    public QuestChainProgressListener(@NotNull CascadeOrchestrator cascadeOrchestrator) {
        this.cascadeOrchestrator = cascadeOrchestrator;
    }

    /**
     * When a quest completes, notifies the cascade orchestrator so it can advance any
     * chain that has this quest as its current step. If the next step auto-completes,
     * the orchestrator batches the cascade into a summary for the player.
     *
     * @param event the quest complete event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onQuestComplete(@NotNull QuestCompleteEvent event) {
        QuestInstance instance = event.getQuestInstance();
        NamespacedKey completedQuestKey = event.getQuestDefinition().getQuestKey();
        instance.getQuestScope().map(QuestScope::getCurrentPlayersInScope)
                .ifPresent(players -> players.forEach(playerUUID -> {
                    boolean advanced = cascadeOrchestrator.advanceChain(playerUUID, completedQuestKey);
                    if (!advanced) {
                        McRPG.getInstance().getLogger().fine(
                                "[QuestChainProgressListener] advanceChain returned false for player "
                                        + playerUUID + ", quest " + completedQuestKey
                                        + " — quest is not a current chain step for this player");
                    }
                }));
    }
}
