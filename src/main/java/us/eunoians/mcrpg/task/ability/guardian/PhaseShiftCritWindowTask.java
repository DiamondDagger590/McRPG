package us.eunoians.mcrpg.task.ability.guardian;

import com.diamonddagger590.mccore.task.core.ExpireableCoreTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.impl.guardian.PhaseShift;

import java.util.UUID;

/**
 * A task that manages the critical strike window granted by Phase Shift.
 * When the window expires without the player landing a crit, the PDC tag is removed.
 */
public final class PhaseShiftCritWindowTask extends ExpireableCoreTask {

    private final UUID playerUUID;

    /**
     * Creates a new Phase Shift crit window task.
     *
     * @param plugin          The McRPG plugin instance.
     * @param playerUUID      The UUID of the player who has the crit window.
     * @param critWindowTicks The duration of the crit window in ticks.
     */
    public PhaseShiftCritWindowTask(@NotNull McRPG plugin, @NotNull UUID playerUUID, int critWindowTicks) {
        super(plugin, 0.0, 999.0, (long) Math.ceil(critWindowTicks / 20.0));
        this.playerUUID = playerUUID;
    }

    @Override
    protected void onTaskExpire() {
        Player player = Bukkit.getPlayer(playerUUID);
        if (player == null || !player.isOnline()) {
            return;
        }
        player.getPersistentDataContainer().remove(PhaseShift.CRIT_WINDOW_TAG);
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

    }

    @Override
    protected void onIntervalPause() {

    }

    @Override
    protected void onIntervalResume() {

    }
}
