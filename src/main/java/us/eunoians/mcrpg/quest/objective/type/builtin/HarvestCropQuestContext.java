package us.eunoians.mcrpg.quest.objective.type.builtin;

import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveProgressContext;

/**
 * Progress context wrapping a {@link PlayerHarvestBlockEvent} for harvest crop objectives.
 */
public class HarvestCropQuestContext extends QuestObjectiveProgressContext {

    private final PlayerHarvestBlockEvent harvestEvent;

    public HarvestCropQuestContext(@NotNull PlayerHarvestBlockEvent harvestEvent) {
        this.harvestEvent = harvestEvent;
    }

    /**
     * Gets the underlying harvest block event.
     *
     * @return the player harvest block event
     */
    @NotNull
    public PlayerHarvestBlockEvent getHarvestEvent() {
        return harvestEvent;
    }
}
