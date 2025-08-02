package us.eunoians.mcrpg.world.safezone;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;

/**
 * A function that checks if a {@link McRPGPlayer} is currently standing in a "safe zone".
 * <p>
 * The context of a safe zone may differ from plugin to plugin, and McRPG may lack native support
 * for specific plugins. This serves to allow external plugins to register their own safe zone checks for
 * McRPG.
 */
public interface SafeZoneFunction {

    /**
     * Checks to see if the provided {@link McRPGPlayer} is currently standing in a "safe zone".
     *
     * @param player The {@link McRPGPlayer} to check.
     * @return {@code true} if the player is currently standing in a "safe zone".
     */
    boolean isPlayerInSafeZone(@NotNull McRPGPlayer player);
}
