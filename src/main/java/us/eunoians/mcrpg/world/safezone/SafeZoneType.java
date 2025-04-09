package us.eunoians.mcrpg.world.safezone;

import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.MainConfigFile;
import us.eunoians.mcrpg.external.lands.LandsHook;
import us.eunoians.mcrpg.external.worldguard.WorldGuardHook;

/**
 * A Safe Zone Type represents a 3rd party plugin that McRPG natively supports
 * in defining a safe zone.
 */
public enum SafeZoneType {

    LANDS(mcRPGPlayer -> {
        McRPG mcRPG = mcRPGPlayer.getPlugin();
        YamlDocument configFile = mcRPG.getFileManager().getFile(FileType.MAIN_CONFIG);
        var landsHookOptional = mcRPG.getLandsHook();
        var playerOptional = mcRPGPlayer.getAsBukkitPlayer();
        if (landsHookOptional.isPresent() && playerOptional.isPresent()) {
            LandsHook landsHook = landsHookOptional.get();
            Player player = playerOptional.get();
            return (landsHook.isPlayerStandingInOwnedLand(player) && configFile.getBoolean(MainConfigFile.SAFE_ZONE_HOOKS_LANDS_OWNED_LANDS, false))
                    || (landsHook.isPlayerStandingInTrustedLand(player) && configFile.getBoolean(MainConfigFile.SAFE_ZONE_HOOKS_LANDS_TRUSTED_LANDS, false))
                    || (landsHook.isPlayerStandingInTenantLand(player) && configFile.getBoolean(MainConfigFile.SAFE_ZONE_HOOKS_LANDS_TENANT_LANDS, false))
                    || ((landsHook.isPlayerStandingInLand(player) ^ configFile.getBoolean(MainConfigFile.SAFE_ZONE_HOOKS_LANDS_ANY_LANDS, false) || configFile.getBoolean(MainConfigFile.SAFE_ZONE_HOOKS_LANDS_WILD_ZONE, false)));
        }
        return false;
    }),
    WORLD_GUARD(mcRPGPlayer -> {
        McRPG mcRPG = mcRPGPlayer.getPlugin();
        YamlDocument configFile = mcRPG.getFileManager().getFile(FileType.MAIN_CONFIG);
        var worldGuardHookOptional = mcRPG.getWorldGuardHook();
        var playerOptional = mcRPGPlayer.getAsBukkitPlayer();
        if (worldGuardHookOptional.isPresent() && playerOptional.isPresent()) {
            WorldGuardHook worldGuardHook = worldGuardHookOptional.get();
            Player player = playerOptional.get();
            return configFile.getBoolean(MainConfigFile.SAFE_ZONE_HOOKS_WORLD_GUARD_ENABLED) && worldGuardHook.isPlayerInSafeZone(player);
        }
        return false;
    }),
    ;

    private final SafeZoneFunction safeZoneFunction;

    SafeZoneType(@NotNull SafeZoneFunction safeZoneFunction) {
        this.safeZoneFunction = safeZoneFunction;
    }

    /**
     * Gets the {@link SafeZoneFunction} for calculating safe zone eligibility.
     *
     * @return The {@link SafeZoneFunction} for calculating safe zone eligibility.
     */
    @NotNull
    public SafeZoneFunction getSafeZoneFunction() {
        return safeZoneFunction;
    }
}
