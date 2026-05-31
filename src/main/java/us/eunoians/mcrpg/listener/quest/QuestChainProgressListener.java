package us.eunoians.mcrpg.listener.quest;

import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.event.quest.QuestCompleteEvent;
import us.eunoians.mcrpg.quest.chain.QuestChainManager;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.quest.impl.scope.QuestScope;

/**
 * Listens for {@link QuestCompleteEvent} and advances any quest chain whose
 * current step is the quest that just completed.
 * <p>
 * Runs at {@link EventPriority#MONITOR} so other plugins receive the event first.
 * The chain manager uses the O(1) reverse index in {@code QuestChainPlayerData}
 * to determine whether the completed quest belongs to an active chain.
 */
public class QuestChainProgressListener implements Listener {

    private final QuestChainManager chainManager;

    /**
     * Creates a new progress listener.
     *
     * @param chainManager the chain manager to delegate advancement to
     */
    public QuestChainProgressListener(@NotNull QuestChainManager chainManager) {
        this.chainManager = chainManager;
    }

    /**
     * When a quest completes, notifies the chain manager so it can advance any
     * chain that has this quest as its current step.
     *
     * @param event the quest complete event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onQuestComplete(@NotNull QuestCompleteEvent event) {
        QuestInstance instance = event.getQuestInstance();
        NamespacedKey completedQuestKey = event.getQuestDefinition().getQuestKey();
        instance.getQuestScope().map(QuestScope::getCurrentPlayersInScope)
                .ifPresent(players -> players.forEach(playerUUID -> {
                    boolean advanced = chainManager.advanceChain(playerUUID, completedQuestKey);
                    if (!advanced) {
                        McRPG.getInstance().getLogger().fine(
                                "[QuestChainProgressListener] advanceChain returned false for player "
                                        + playerUUID + ", quest " + completedQuestKey
                                        + " — quest is not a current chain step for this player");
                    }
                }));
    }
}
