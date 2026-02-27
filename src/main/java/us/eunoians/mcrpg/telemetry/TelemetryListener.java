package us.eunoians.mcrpg.telemetry;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.event.ability.AbilityActivateEvent;
import us.eunoians.mcrpg.event.skill.SkillGainExpEvent;
import us.eunoians.mcrpg.event.skill.SkillGainLevelEvent;

/**
 * Bukkit event listener that feeds game events into the {@link MetricsAccumulator}.
 * <p>
 * All handlers run at {@link EventPriority#MONITOR} priority and only record data
 * for non-cancelled events. They never modify event state.
 */
public class TelemetryListener implements Listener {

    private final MetricsAccumulator accumulator;

    public TelemetryListener(@NotNull MetricsAccumulator accumulator) {
        this.accumulator = accumulator;
    }

    /**
     * Records XP gain events into the accumulator.
     * Runs at MONITOR priority to ensure we only capture the final XP value
     * after all other plugins have had a chance to modify it.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSkillGainExp(@NotNull SkillGainExpEvent event) {
        accumulator.recordXpGain(
                event.getSkillKey(),
                event.getExperience(),
                event.getSkillHolder().getUUID()
        );
    }

    /**
     * Records level-up events into the accumulator.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onSkillGainLevel(@NotNull SkillGainLevelEvent event) {
        accumulator.recordLevelUp(
                event.getSkillKey(),
                event.getLevels()
        );
    }

    /**
     * Records ability activation events into the accumulator.
     * <p>
     * Listens to the abstract {@link AbilityActivateEvent} which all concrete
     * activation events (BleedActivateEvent, ExtraOreActivateEvent, etc.) extend.
     * This means we automatically capture activations for any ability — including
     * abilities added by future content expansions — without needing to add
     * per-ability listener methods.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAbilityActivate(@NotNull AbilityActivateEvent event) {
        accumulator.recordAbilityActivation(event.getAbility().getAbilityKey());
    }
}
