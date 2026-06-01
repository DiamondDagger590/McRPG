package us.eunoians.mcrpg.listener.quest;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.quest.objective.type.builtin.DealDamageQuestContext;

/**
 * Listens for {@link EntityDamageByEntityEvent} and drives quest objective progress for any
 * active deal-damage objectives the attacking player is contributing to.
 * <p>
 * Resolves the attacking player from both direct hits and projectile sources.
 */
public class DealDamageQuestProgressListener implements QuestProgressListener {

    private final QuestManager questManager;

    /**
     * Creates a new listener with the provided quest manager.
     *
     * @param questManager the quest manager used to resolve and progress active quests
     */
    public DealDamageQuestProgressListener(@NotNull QuestManager questManager) {
        this.questManager = questManager;
    }

    /**
     * Handles an entity damage by entity event by progressing any matching quest objectives
     * for the player who dealt the damage, resolving through projectile shooters if needed.
     *
     * @param event the entity damage by entity event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDealDamage(EntityDamageByEntityEvent event) {
        Player attacker;
        Entity damager = event.getDamager();
        if (damager instanceof Player p) {
            attacker = p;
        } else if (damager instanceof Projectile proj && proj.getShooter() instanceof Player p) {
            attacker = p;
        } else {
            return;
        }
        progressQuests(questManager, attacker.getUniqueId(), new DealDamageQuestContext(event));
    }
}
