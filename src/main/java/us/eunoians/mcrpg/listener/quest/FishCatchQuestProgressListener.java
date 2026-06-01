package us.eunoians.mcrpg.listener.quest;

import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerFishEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.quest.objective.type.builtin.FishCatchQuestContext;

/**
 * Listens for {@link PlayerFishEvent} and drives quest objective progress for any
 * active fish-catch objectives the fishing player is contributing to.
 * <p>
 * Only events with state {@link PlayerFishEvent.State#CAUGHT_FISH} where the caught
 * entity is an {@link Item} are processed.
 */
public class FishCatchQuestProgressListener implements QuestProgressListener {

    private final QuestManager questManager;

    /**
     * Creates a new listener with the provided quest manager.
     *
     * @param questManager the quest manager used to resolve and progress active quests
     */
    public FishCatchQuestProgressListener(@NotNull QuestManager questManager) {
        this.questManager = questManager;
    }

    /**
     * Handles a fish event by progressing any matching quest objectives
     * for the player who caught a fish, provided the catch was successful.
     *
     * @param event the player fish event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFishCatch(PlayerFishEvent event) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) {
            return;
        }
        if (!(event.getCaught() instanceof Item caughtItem)) {
            return;
        }
        progressQuests(questManager, event.getPlayer().getUniqueId(), new FishCatchQuestContext(event, caughtItem.getItemStack()));
    }
}
