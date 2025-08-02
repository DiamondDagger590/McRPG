package us.eunoians.mcrpg.external.worldguard;

import com.diamonddagger590.mccore.registry.plugin.PluginHook;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.exception.external.worldguard.WorldGuardFlagRegisterException;

/**
 * A hook for containing all code related to <a href="https://modrinth.com/plugin/worldguard/versions">WorldGuard</a> that McRPG
 * needs in order to support it.
 */
public class WorldGuardHook extends PluginHook<McRPG> {

    private static final String SAFE_ZONE_FLAG_KEY = "mcrpg-safe-zone";
    private StateFlag safeZoneFlag;

    public WorldGuardHook(McRPG plugin) {
        super(plugin);
        loadCustomFlags();
    }

    /**
     * Loads all the custom world guard flags that McRPG supports
     *
     * @throws WorldGuardFlagRegisterException If there is some conflict with other plugins regarding flag naming.
     */
    private void loadCustomFlags() {
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            safeZoneFlag = new StateFlag(SAFE_ZONE_FLAG_KEY, true);
            registry.register(safeZoneFlag);
        } catch (FlagConflictException exception) {
            Flag<?> existing = registry.get(SAFE_ZONE_FLAG_KEY);
            if (existing instanceof StateFlag stateFlag) {
                safeZoneFlag = stateFlag;
            } else {
                throw new WorldGuardFlagRegisterException(SAFE_ZONE_FLAG_KEY);
            }
        }
    }

    /**
     * Checks to see if a {@link Player} is in what McRPG considers a safe zone.
     *
     * @param player The {@link Player} to check.
     * @return {@code true} if the player is in a safe zone.
     */
    public boolean isPlayerInSafeZone(@NotNull Player player) {
        Location location = BukkitAdapter.adapt(player.getLocation());
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regionManager = container.get(BukkitAdapter.adapt(player.getWorld()));
        if (regionManager != null && safeZoneFlag != null) {
            ApplicableRegionSet applicableRegionSet = regionManager.getApplicableRegions(location.toVector().toBlockPoint());
            return applicableRegionSet.testState(null, safeZoneFlag);
        }
        return false;
    }
}
