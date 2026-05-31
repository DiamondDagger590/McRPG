package us.eunoians.mcrpg.task.ability.guardian;

import com.diamonddagger590.mccore.task.core.ExpireableCoreTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;

import java.util.UUID;

/**
 * A task that manages the critical strike window granted by Phase Shift.
 * When the window expires without the player landing a crit, the crit tag is consumed/removed.
 */
public final class PhaseShiftCritWindowTask extends ExpireableCoreTask {

    private final UUID playerUUID;
    private final McRPGPlayer mcRPGPlayer;

    /**
     * Creates a new Phase Shift crit window task.
     *
     * @param plugin       The McRPG plugin instance
     * @param mcRPGPlayer  The player who has the crit window
     * @param critWindowTicks The duration of the crit window in ticks
     */
    public PhaseShiftCritWindowTask(@NotNull McRPG plugin, @NotNull McRPGPlayer mcRPGPlayer, int critWindowTicks) {
        super(plugin, 0.0, 999.0, (long) Math.ceil(critWindowTicks / 20.0));
        this.mcRPGPlayer = mcRPGPlayer;
        this.playerUUID = mcRPGPlayer.getUUID();
    }

    @Override
    protected void onTaskExpire() {
        Player player = Bukkit.getPlayer(playerUUID);
        if (player == null || !player.isOnline()) {
            return;
        }
        mcRPGPlayer.consumeCritWindow();
    }

    @Override
    protected void onCancel() {

    }

    @Override
    protected void onDelayComplete() {

    }

    @Override
    protected void onIntervalStart() {

    }

    @Override
    protected void onIntervalComplete() {
        // No periodic tick needed - this task only uses expiration
    }

    @Override
    protected void onIntervalPause() {

    }

    @Override
    protected void onIntervalResume() {

    }
}
