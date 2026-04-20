package us.eunoians.mcrpg.display.hud;

import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.display.DisplayManager;
import us.eunoians.mcrpg.display.impl.TickablePlayerDisplay;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

/**
 * Repeating task that drives every online player's
 * {@link TickablePlayerDisplay}s — including the action bar HUD — by delegating
 * to {@link DisplayManager#tickDisplays(long, double)}.
 * <p>
 * The task itself is deliberately thin: all per-frame logic (regen, slot
 * resolution, rendering, sending) lives on {@link ActionBarHudDisplay} and its
 * collaborators so this class is only responsible for the scheduler cadence.
 * <p>
 * TODO(#215): Migrate to McCore's {@code CoreTask} / {@code DelayableCoreTask}
 * so scheduling, cancellation, and lifecycle follow the same pattern as the
 * rest of the plugin rather than a raw {@link BukkitRunnable}.
 */
public class ActionBarHudTask extends BukkitRunnable {

    private final McRPG plugin;
    private final int intervalTicks;

    /**
     * @param plugin        The McRPG plugin instance.
     * @param intervalTicks How often (in ticks) this task runs.
     */
    public ActionBarHudTask(@NotNull McRPG plugin, int intervalTicks) {
        this.plugin = plugin;
        this.intervalTicks = intervalTicks;
    }

    /**
     * Starts this task as a repeating timer.
     */
    public void start() {
        this.runTaskTimer(plugin, 0L, intervalTicks);
    }

    @Override
    public void run() {
        double secondsElapsed = intervalTicks / 20.0;
        long currentTick = Bukkit.getCurrentTick();
        DisplayManager displayManager = plugin.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.DISPLAY);
        displayManager.tickDisplays(currentTick, secondsElapsed);
    }
}
