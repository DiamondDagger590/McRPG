package us.eunoians.mcrpg.listener.quest;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.quest.objective.type.builtin.DamageTakenQuestContext;

/**
 * Listens for {@link EntityDamageEvent} and drives quest objective progress for any
 * active damage-taken objectives the damaged player is contributing to.
 * <p>
 * Only events where the damaged entity is a {@link Player} are processed.
 */
public class DamageTakenQuestProgressListener implements QuestProgressListener {

    private final QuestManager questManager;

    /**
     * Creates a new listener with the provided quest manager.
     *
     * @param questManager the quest manager used to resolve and progress active quests
     */
    public DamageTakenQuestProgressListener(@NotNull QuestManager questManager) {
        this.questManager = questManager;
    }

    /**
     * Handles an entity damage event by progressing any matching quest objectives
     * for the player who took the damage.
     *
     * @param event the entity damage event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamageTaken(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        progressQuests(questManager, player.getUniqueId(), new DamageTakenQuestContext(event));
    }
}
