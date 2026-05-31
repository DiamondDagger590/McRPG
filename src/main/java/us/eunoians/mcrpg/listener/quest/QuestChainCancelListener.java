package us.eunoians.mcrpg.listener.quest;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.event.quest.QuestCancelEvent;
import us.eunoians.mcrpg.quest.chain.QuestChainManager;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.quest.impl.scope.QuestScope;

/**
 * Listens for {@link QuestCancelEvent} and propagates cancellation or expiration
 * into any owning chain's state.
 * <p>
 * When a chain-managed quest is <em>cancelled</em> (player abandon), the chain
 * transitions to {@link us.eunoians.mcrpg.quest.chain.QuestChainState#ABANDONED}.
 * <p>
 * When a chain-managed quest <em>expires</em>, the step's {@code on-quest-expire}
 * setting controls the transition (only {@code fail-chain} is functional initially,
 * causing {@link us.eunoians.mcrpg.quest.chain.QuestChainState#FAILED}).
 */
public class QuestChainCancelListener implements Listener {

    private final QuestChainManager chainManager;

    /**
     * Creates a new cancel listener.
     *
     * @param chainManager the chain manager to delegate state transitions to
     */
    public QuestChainCancelListener(@NotNull QuestChainManager chainManager) {
        this.chainManager = chainManager;
    }

    /**
     * When a quest is cancelled (not due to expiration), checks if it belongs to an
     * ACTIVE chain and transitions that chain to ABANDONED if so.
     *
     * @param event the quest cancel event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onQuestCancel(@NotNull QuestCancelEvent event) {
        if (event.isExpiration()) {
            return;
        }
        QuestInstance instance = event.getQuestInstance();
        instance.getQuestScope().map(QuestScope::getCurrentPlayersInScope)
                .ifPresent(players -> players.forEach(playerUUID ->
                        chainManager.handleQuestCancelled(playerUUID, event.getQuestDefinitionKey())));
    }

    /**
     * When a chain-managed quest expires, applies the step's {@code on-quest-expire}
     * policy to determine the chain's terminal state.
     *
     * @param event the quest cancel event (with {@link QuestCancelEvent#isExpiration()} {@code true})
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onQuestExpire(@NotNull QuestCancelEvent event) {
        if (!event.isExpiration()) {
            return;
        }
        QuestInstance instance = event.getQuestInstance();
        instance.getQuestScope().map(QuestScope::getCurrentPlayersInScope)
                .ifPresent(players -> players.forEach(playerUUID ->
                        chainManager.handleQuestExpired(playerUUID, event.getQuestDefinitionKey())));
    }
}
