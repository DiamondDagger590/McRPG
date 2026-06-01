package us.eunoians.mcrpg.listener.quest;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.quest.objective.type.builtin.ShearEntityQuestContext;

/**
 * Listens for {@link PlayerShearEntityEvent} and drives quest objective progress for any
 * active entity-shearing objectives the shearing player is contributing to.
 */
public class ShearEntityQuestProgressListener implements QuestProgressListener {

    private final QuestManager questManager;

    /**
     * Creates a new listener with the provided quest manager.
     *
     * @param questManager the quest manager used to resolve and progress active quests
     */
    public ShearEntityQuestProgressListener(@NotNull QuestManager questManager) {
        this.questManager = questManager;
    }

    /**
     * Handles a shear entity event by progressing any matching quest objectives
     * for the player who sheared the entity.
     *
     * @param event the player shear entity event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onShearEntity(PlayerShearEntityEvent event) {
        progressQuests(questManager, event.getPlayer().getUniqueId(), new ShearEntityQuestContext(event));
    }
}
