package us.eunoians.mcrpg.world.safezone;

import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.MainConfigFile;
import us.eunoians.mcrpg.external.lands.LandsHook;
import us.eunoians.mcrpg.external.worldguard.WorldGuardHook;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.registry.plugin.McRPGPluginHookKey;

/**
 * A Safe Zone Type represents a 3rd party plugin that McRPG natively supports
 * in defining a safe zone.
 */
public enum SafeZoneType {

    LANDS(mcRPGPlayer -> {
        McRPG mcRPG = mcRPGPlayer.getPlugin();
        YamlDocument configFile = mcRPG.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE).getFile(FileType.MAIN_CONFIG);
        var landsHookOptional = mcRPG.registryAccess().registry(RegistryKey.PLUGIN_HOOK).pluginHook(McRPGPluginHookKey.LANDS);
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
        YamlDocument configFile = mcRPG.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE).getFile(FileType.MAIN_CONFIG);
        var worldGuardHookOptional = mcRPG.registryAccess().registry(RegistryKey.PLUGIN_HOOK).pluginHook(McRPGPluginHookKey.WORLDGUARD);
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
