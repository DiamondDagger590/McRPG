package us.eunoians.mcrpg.listener.quest;

import io.papermc.paper.event.player.PlayerTradeEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.quest.objective.type.builtin.VillagerTradeQuestContext;

/**
 * Listens for {@link PlayerTradeEvent} and drives quest objective progress for any
 * active villager-trade objectives the trading player is contributing to.
 */
public class VillagerTradeQuestProgressListener implements QuestProgressListener {

    private final QuestManager questManager;

    /**
     * Creates a new listener with the provided quest manager.
     *
     * @param questManager the quest manager used to resolve and progress active quests
     */
    public VillagerTradeQuestProgressListener(@NotNull QuestManager questManager) {
        this.questManager = questManager;
    }

    /**
     * Handles a player trade event by progressing any matching quest objectives
     * for the player who traded with a villager.
     *
     * @param event the player trade event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVillagerTrade(PlayerTradeEvent event) {
        progressQuests(questManager, event.getPlayer().getUniqueId(), new VillagerTradeQuestContext(event));
    }
}
