package us.eunoians.mcrpg.quest.objective.type.builtin;

import io.papermc.paper.event.player.PlayerTradeEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveProgressContext;

/**
 * Progress context wrapping a {@link PlayerTradeEvent} for villager trade objectives.
 */
public class VillagerTradeQuestContext extends QuestObjectiveProgressContext {

    private final PlayerTradeEvent tradeEvent;

    public VillagerTradeQuestContext(@NotNull PlayerTradeEvent tradeEvent) {
        this.tradeEvent = tradeEvent;
    }

    /**
     * Gets the underlying trade event.
     *
     * @return the player trade event
     */
    @NotNull
    public PlayerTradeEvent getTradeEvent() {
        return tradeEvent;
    }
}
