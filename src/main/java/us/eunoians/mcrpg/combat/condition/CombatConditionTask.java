package us.eunoians.mcrpg.combat.condition;

import com.diamonddagger590.mccore.task.core.CancelableCoreTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.combat.CombatTrackerManager;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

/**
 * Repeating task that evaluates a single {@link CombatCondition} at its declared cadence. The default
 * implementation iterates all online players and calls {@link CombatCondition#isInCombat(org.bukkit.entity.LivingEntity)}.
 * For entities that match, it reports combat activity to the manager — creating sessions for entities not yet
 * in combat and refreshing existing sessions.
 * <p>
 * Subclasses may override {@link #evaluateEntities()} to scope the entity set (e.g., only players in an arena region).
 */
public class CombatConditionTask extends CancelableCoreTask {

    private final CombatTrackerManager combatTrackerManager;
    private final CombatCondition condition;

    /**
     * Constructs a new {@link CombatConditionTask}.
     * <p>
     * The task frequency is derived from the condition's {@link CombatCondition#getCheckIntervalSeconds()}.
     *
     * @param plugin               The {@link McRPG} plugin instance.
     * @param combatTrackerManager The {@link CombatTrackerManager} to report combat activity to.
     * @param condition            The {@link CombatCondition} to evaluate.
     */
    public CombatConditionTask(@NotNull McRPG plugin,
                               @NotNull CombatTrackerManager combatTrackerManager,
                               @NotNull CombatCondition condition) {
        super(plugin, 0, condition.getCheckIntervalSeconds());
        this.combatTrackerManager = combatTrackerManager;
        this.condition = condition;
    }

    @Override
    protected void onIntervalComplete() {
        for (Player player : evaluateEntities()) {
            if (condition.isInCombat(player)) {
                Set<UUID> impliedParticipants = condition.getImpliedParticipants(player);
                if (impliedParticipants.isEmpty()) {
                    combatTrackerManager.reportConditionActivity(player.getUniqueId(), condition.getKey());
                } else {
                    for (UUID participantUUID : impliedParticipants) {
                        combatTrackerManager.reportCombatActivity(player.getUniqueId(), participantUUID);
                    }
                }
            }
        }
    }

    @Override
    protected void onCancel() { }

    @Override
    protected void onDelayComplete() { }

    @Override
    protected void onIntervalStart() { }

    @Override
    protected void onIntervalPause() { }

    @Override
    protected void onIntervalResume() { }

    /**
     * Gets the collection of players to evaluate this tick. The default implementation
     * returns all online players. Subclasses may override to scope the evaluation
     * (e.g., only players in an arena region).
     *
     * @return A {@link Collection} of players to evaluate.
     */
    @NotNull
    protected Collection<? extends Player> evaluateEntities() {
        return Bukkit.getOnlinePlayers();
    }

    /**
     * Gets the condition this task evaluates.
     *
     * @return The {@link CombatCondition}.
     */
    @NotNull
    public CombatCondition getCondition() {
        return condition;
    }
}
