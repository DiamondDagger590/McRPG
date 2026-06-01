package us.eunoians.mcrpg.quest.objective.type.builtin;

import org.bukkit.event.player.PlayerBedEnterEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveProgressContext;

/**
 * Progress context wrapping a {@link PlayerBedEnterEvent} for enter bed objectives.
 */
public class EnterBedQuestContext extends QuestObjectiveProgressContext {

    private final PlayerBedEnterEvent bedEnterEvent;

    public EnterBedQuestContext(@NotNull PlayerBedEnterEvent bedEnterEvent) {
        this.bedEnterEvent = bedEnterEvent;
    }

    /**
     * Gets the underlying bed enter event.
     *
     * @return the player bed enter event
     */
    @NotNull
    public PlayerBedEnterEvent getBedEnterEvent() {
        return bedEnterEvent;
    }
}
