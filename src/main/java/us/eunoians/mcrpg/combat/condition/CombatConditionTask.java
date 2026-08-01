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
import java.util.logging.Level;

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
     * The minimum allowed check interval, in seconds. A third-party condition returning a value below
     * this would make the task run every tick and scan all online players, so it is floored here.
     */
    private static final double MINIMUM_CHECK_INTERVAL_SECONDS = 0.25;

    /**
     * Constructs a new {@link CombatConditionTask}.
     * <p>
     * The task frequency is derived from the condition's {@link CombatCondition#getCheckIntervalSeconds()},
     * floored at {@value #MINIMUM_CHECK_INTERVAL_SECONDS} seconds.
     *
     * @param plugin               The {@link McRPG} plugin instance.
     * @param combatTrackerManager The {@link CombatTrackerManager} to report combat activity to.
     * @param condition            The {@link CombatCondition} to evaluate.
     */
    public CombatConditionTask(@NotNull McRPG plugin,
                               @NotNull CombatTrackerManager combatTrackerManager,
                               @NotNull CombatCondition condition) {
        super(plugin, 0, Math.max(MINIMUM_CHECK_INTERVAL_SECONDS, condition.getCheckIntervalSeconds()));
        this.combatTrackerManager = combatTrackerManager;
        this.condition = condition;
        if (condition.getCheckIntervalSeconds() < MINIMUM_CHECK_INTERVAL_SECONDS) {
            plugin.getLogger().log(Level.WARNING, "Combat condition {0} check interval {1}s is below the minimum; using {2}s.",
                    new Object[]{condition.getKey().toString(), condition.getCheckIntervalSeconds(), MINIMUM_CHECK_INTERVAL_SECONDS});
        }
    }

    @Override
    protected void onIntervalComplete() {
        for (Player player : evaluateEntities()) {
            // Evaluate each player defensively: a third-party condition that throws for one player
            // must not abort the whole pass (which, because the interval only advances on a normal
            // return, would otherwise re-run and re-throw every tick).
            try {
                evaluatePlayer(player);
            } catch (Exception e) {
                getPlugin().getLogger().log(Level.WARNING, "Combat condition " + condition.getKey()
                        + " threw while evaluating player " + player.getUniqueId() + "; skipping", e);
            }
        }
    }

    /**
     * Evaluates the condition for a single player and reports activity to the manager. Reports
     * condition-only activity when the condition implies no specific participants, otherwise reports
     * a combat interaction against each implied participant.
     *
     * @param player The player to evaluate.
     */
    private void evaluatePlayer(@NotNull Player player) {
        if (!condition.isInCombat(player)) {
            return;
        }
        Set<UUID> impliedParticipants = condition.getImpliedParticipants(player);
        if (impliedParticipants.isEmpty()) {
            combatTrackerManager.reportConditionActivity(player.getUniqueId(), condition.getKey());
        } else {
            for (UUID participantUUID : impliedParticipants) {
                combatTrackerManager.reportCombatActivity(player.getUniqueId(), participantUUID);
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
