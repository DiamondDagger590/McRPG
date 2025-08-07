package us.eunoians.mcrpg.external.common;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * A safe zone plugin hook is a plugin hook which allows for defining
 * various "safe zones" for rested experience accumulation via
 * {@link us.eunoians.mcrpg.task.player.McRPGPlayerSafeZoneCheckTask}.
 */
public interface SafeZonePluginHook {

    /**
     * Checks to see if a {@link Player} is in what McRPG considers a safe zone.
     *
     * @param player The {@link Player} to check.
     * @return {@code true} if the player is in a safe zone.
     */
    boolean isPlayerInSafeZone(@NotNull Player player);
}
