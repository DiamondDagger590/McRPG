package us.eunoians.mcrpg.listener.quest;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.quest.objective.type.builtin.DistanceTraveledQuestContext;

/**
 * Listens for {@link PlayerMoveEvent} and drives quest objective progress for any
 * active distance-traveled objectives the moving player is contributing to.
 * <p>
 * Rotation-only moves (no positional change) and sub-block movements are ignored
 * to avoid unnecessary progress calls on high-frequency events.
 */
public class DistanceTraveledQuestProgressListener implements QuestProgressListener {

    private final QuestManager questManager;

    /**
     * Creates a new listener with the provided quest manager.
     *
     * @param questManager the quest manager used to resolve and progress active quests
     */
    public DistanceTraveledQuestProgressListener(@NotNull QuestManager questManager) {
        this.questManager = questManager;
    }

    /**
     * Handles a player move event by progressing any matching quest objectives
     * for the player who moved, filtering out rotation-only and sub-block movements.
     *
     * @param event the player move event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDistanceTraveled(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        if (from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY() && from.getBlockZ() == to.getBlockZ()) {
            return;
        }
        double distance = from.distance(to);
        long blockDistance = Math.round(distance);
        if (blockDistance < 1) {
            return;
        }
        progressQuests(questManager, event.getPlayer().getUniqueId(), new DistanceTraveledQuestContext(event, blockDistance));
    }
}
