package us.eunoians.mcrpg.combat.condition;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.combat.CombatTrackerManager;
import us.eunoians.mcrpg.expansion.content.McRPGContent;

import java.util.Set;
import java.util.UUID;

/**
 * Interface for state-based combat conditions. Conditions are polled periodically at their declared cadence
 * by a managed {@link CombatConditionTask}. The built-in {@link org.bukkit.event.entity.EntityDamageByEntityEvent}
 * handling is <em>not</em> a condition — it is the core mechanism. Conditions extend the system for
 * proximity-based, region-based, or other continuous state checks.
 * <p>
 * Event-based combat triggers (e.g., healing-as-combat) do not need to implement this interface — they listen
 * to Bukkit events directly and call {@code CombatTrackerManager.reportCombatActivity()}.
 */
public interface CombatCondition extends McRPGContent {

    /**
     * Gets the unique key identifying this condition.
     *
     * @return The {@link NamespacedKey} for this condition.
     */
    @NotNull
    NamespacedKey getKey();

    /**
     * Gets the interval in seconds between periodic evaluations of this condition.
     * Passed directly to {@link com.diamonddagger590.mccore.task.core.CancelableCoreTask}'s frequency parameter, which uses
     * real-time seconds rather than server ticks for lag resistance.
     *
     * @return The check interval in seconds.
     */
    double getCheckIntervalSeconds();

    /**
     * Evaluates whether the given entity should be considered in combat due to this condition.
     * Called both by the condition's periodic task (for session creation) and by the timeout
     * scan (as a hold-open gate before ending sessions).
     *
     * @param entity The entity to evaluate.
     * @return {@code true} if this condition puts the entity in combat.
     */
    boolean isInCombat(@NotNull LivingEntity entity);

    /**
     * Gets the participants implied by this condition for the given entity, if any.
     * Returns an empty set if the condition is proximity-based rather than entity-vs-entity.
     *
     * @param entity The entity to evaluate.
     * @return A {@link Set} of participant UUIDs implied by this condition.
     */
    @NotNull
    Set<UUID> getImpliedParticipants(@NotNull LivingEntity entity);

    /**
     * Creates the periodic task responsible for evaluating this condition. The default
     * implementation returns a {@link CombatConditionTask} that iterates all online players.
     * Third-party conditions may override this to provide a custom task subclass with
     * scoped entity evaluation or specialized logic.
     *
     * @param mcRPG                The {@link McRPG} plugin instance the task is scheduled under.
     * @param combatTrackerManager The {@link CombatTrackerManager} the task reports activity to.
     * @return A new {@link CombatConditionTask} for this condition.
     */
    @NotNull
    default CombatConditionTask createTask(@NotNull McRPG mcRPG, @NotNull CombatTrackerManager combatTrackerManager) {
        return new CombatConditionTask(mcRPG, combatTrackerManager, this);
    }
}
