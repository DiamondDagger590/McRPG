package us.eunoians.mcrpg.listener.quest;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityBreedEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.quest.objective.type.builtin.BreedAnimalQuestContext;

/**
 * Listens for {@link EntityBreedEvent} and drives quest objective progress for any
 * active animal-breeding objectives the breeding player is contributing to.
 * <p>
 * Only events where the breeder is a {@link Player} are processed.
 */
public class BreedAnimalQuestProgressListener implements QuestProgressListener {

    private final QuestManager questManager;

    /**
     * Creates a new listener with the provided quest manager.
     *
     * @param questManager the quest manager used to resolve and progress active quests
     */
    public BreedAnimalQuestProgressListener(@NotNull QuestManager questManager) {
        this.questManager = questManager;
    }

    /**
     * Handles an entity breed event by progressing any matching quest objectives
     * for the player who bred the animals.
     *
     * @param event the entity breed event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreedAnimal(EntityBreedEvent event) {
        if (!(event.getBreeder() instanceof Player player)) {
            return;
        }
        progressQuests(questManager, player.getUniqueId(), new BreedAnimalQuestContext(event));
    }
}
