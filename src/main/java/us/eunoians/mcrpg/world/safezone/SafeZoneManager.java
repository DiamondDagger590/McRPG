package us.eunoians.mcrpg.world.safezone;

import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.Manager;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.HashSet;
import java.util.Set;

/**
 * This manager is responsible for aggregating all the definitions of a "safe zone"
 * into one central place to make it easier to utilize while also being plugin agnostic.
 * <p>
 * Any third party plugins natively supported by McRPG will have their support defined in {@link SafeZoneType}s.
 */
public class SafeZoneManager extends Manager<McRPG> {

    private final Set<SafeZoneFunction> safeZoneFunctions;

    public SafeZoneManager(@NotNull McRPG mcRPG) {
        super(mcRPG);
        this.safeZoneFunctions = new HashSet<>();
        registerNativeFunctions();
    }

    /**
     * Registers the native {@link SafeZoneFunction}s that McRPG supports
     */
    private void registerNativeFunctions() {
        for (SafeZoneType safeZoneType : SafeZoneType.values()) {
            registerSafeZoneFunction(safeZoneType.getSafeZoneFunction());
        }
    }

    /**
     * Registers the provided {@link SafeZoneFunction} to be considered when checking if a player is
     * in a safe zone or not.
     *
     * @param safeZoneFunction The {@link SafeZoneFunction} to register.
     */
    public void registerSafeZoneFunction(@NotNull SafeZoneFunction safeZoneFunction) {
        this.safeZoneFunctions.add(safeZoneFunction);
    }

    /**
     * Checks to see if the provided {@link Player} is currently in a safe zone.
     *
     * @param player The {@link Player} to check.
     * @return {@code true} if the player has data loaded and is standing in a safe zone.
     */
    public boolean isPlayerInSafeZone(Player player) {
        var playerOptional = plugin().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.PLAYER).getPlayer(player.getUniqueId());
        return playerOptional.filter(this::isPlayerInSafeZone).isPresent();
    }

    /**
     * Checks to see if the provided {@link McRPGPlayer} is currently in a safe zone.
     *
     * @param mcRPGPlayer The {@link McRPGPlayer} to check.
     * @return {@code true} if the player is standing in a safe zone.
     */
    public boolean isPlayerInSafeZone(@NotNull McRPGPlayer mcRPGPlayer) {
        for (SafeZoneFunction safeZoneFunction : safeZoneFunctions) {
            if (safeZoneFunction.isPlayerInSafeZone(mcRPGPlayer)) {
                return true;
            }
        }
        return false;
    }
}
