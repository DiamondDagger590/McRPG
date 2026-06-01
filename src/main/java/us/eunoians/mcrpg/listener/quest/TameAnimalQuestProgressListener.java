package us.eunoians.mcrpg.listener.quest;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityTameEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.quest.objective.type.builtin.TameAnimalQuestContext;

/**
 * Listens for {@link EntityTameEvent} and drives quest objective progress for any
 * active animal-taming objectives the taming player is contributing to.
 * <p>
 * Only events where the owner is a {@link Player} are processed.
 */
public class TameAnimalQuestProgressListener implements QuestProgressListener {

    private final QuestManager questManager;

    /**
     * Creates a new listener with the provided quest manager.
     *
     * @param questManager the quest manager used to resolve and progress active quests
     */
    public TameAnimalQuestProgressListener(@NotNull QuestManager questManager) {
        this.questManager = questManager;
    }

    /**
     * Handles an entity tame event by progressing any matching quest objectives
     * for the player who tamed the animal.
     *
     * @param event the entity tame event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTameAnimal(EntityTameEvent event) {
        if (!(event.getOwner() instanceof Player player)) {
            return;
        }
        progressQuests(questManager, player.getUniqueId(), new TameAnimalQuestContext(event));
    }
}
