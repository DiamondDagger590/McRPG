package us.eunoians.mcrpg.display.hud;

import com.diamonddagger590.mccore.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.stat.CombatStatInstance;
import us.eunoians.mcrpg.stat.McRPGCombatStat;
import us.eunoians.mcrpg.stat.PlayerCombatData;

/**
 * Repeating task that renders the action bar HUD for all online McRPG players
 * and ticks mana regeneration.
 * <p>
 * Runs every N ticks (configurable, default 2). For each player:
 * <ol>
 *   <li>Ticks mana regen based on elapsed time</li>
 *   <li>Computes HP display from vanilla health scaled to custom max</li>
 *   <li>Reads mana from {@link PlayerCombatData}</li>
 *   <li>Reads center content from {@link McRPGPlayer#getActionBarCenterContent(long)}</li>
 *   <li>Renders and sends the composed action bar via {@link ActionBarHudRenderer}</li>
 * </ol>
 * <p>
 * TODO(#215): Migrate to McCore's {@code CoreTask} / {@code DelayableCoreTask}
 * so scheduling, cancellation, and lifecycle follow the same pattern as the rest
 * of the plugin rather than a raw {@link BukkitRunnable}.
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
        var playerManager = plugin.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.PLAYER);

        for (Player player : Bukkit.getOnlinePlayers()) {
            var mcRPGPlayerOpt = playerManager.getPlayer(player.getUniqueId());
            if (mcRPGPlayerOpt.isEmpty()) {
                continue;
            }
            McRPGPlayer mcRPGPlayer = mcRPGPlayerOpt.get();
            PlayerCombatData combatData = mcRPGPlayer.getPlayerCombatData();

            combatData.tickRegen(secondsElapsed);

            int healthCurrent = computeScaledHealth(player, combatData);
            int healthMax = getStatMax(combatData, McRPGCombatStat.HEALTH_KEY);
            int manaCurrent = getStatCurrent(combatData, McRPGCombatStat.MANA_KEY);
            int manaMax = getStatMax(combatData, McRPGCombatStat.MANA_KEY);

            Component centerContent = mcRPGPlayer.getActionBarCenterContent(currentTick)
                    .orElse(null);

            Component hud = ActionBarHudRenderer.buildHud(
                    healthCurrent, healthMax,
                    McRPGCombatStat.HEALTH.getDisplaySymbol(),
                    manaCurrent, manaMax,
                    McRPGCombatStat.MANA.getDisplaySymbol(),
                    centerContent
            );

            player.sendActionBar(hud);
        }
    }

    private int computeScaledHealth(@NotNull Player player, @NotNull PlayerCombatData combatData) {
        double vanillaHealth = player.getHealth();
        double vanillaMax = player.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue();
        double healthPercent = (vanillaMax > 0) ? (vanillaHealth / vanillaMax) : 0;
        int customMax = getStatMax(combatData, McRPGCombatStat.HEALTH_KEY);
        return (int) Math.round(healthPercent * customMax);
    }

    private int getStatMax(@NotNull PlayerCombatData combatData, @NotNull org.bukkit.NamespacedKey key) {
        return combatData.getInstance(key)
                .map(CombatStatInstance::getEffectiveMax)
                .map(d -> (int) Math.round(d))
                .orElse(0);
    }

    private int getStatCurrent(@NotNull PlayerCombatData combatData, @NotNull org.bukkit.NamespacedKey key) {
        return combatData.getInstance(key)
                .map(CombatStatInstance::getCurrent)
                .map(d -> (int) Math.round(d))
                .orElse(0);
    }
}
