package us.eunoians.mcrpg.external.lands;


import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.plugin.PluginHook;
import dev.dejvokep.boostedyaml.YamlDocument;
import me.angeschossen.lands.api.LandsIntegration;
import me.angeschossen.lands.api.land.Area;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.MainConfigFile;
import us.eunoians.mcrpg.external.common.SafeZonePluginHook;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

/**
 * A hook for containing all code related to <a href="https://www.spigotmc.org/resources/lands-%E2%AD%95-land-claim-plugin-%E2%9C%85-grief-prevention-protection-gui-management-nations-wars-1-21-support.53313/">Lands</a>
 * that McRPG needs in order to support it.
 */
public class LandsHook extends PluginHook<McRPG> implements SafeZonePluginHook {

    private final LandsIntegration landsIntegration;

    public LandsHook(@NotNull McRPG plugin) {
        super(plugin);
        this.landsIntegration = LandsIntegration.of(plugin);
    }

    /**
     * Checks to see if the provided {@link Player} is standing in is an {@link Area}
     * that they own.
     *
     * @param player The {@link Player} to check.
     * @return {@code true} if the provided player is standing in an {@link Area} that
     * they own.
     */
    public boolean isPlayerStandingInOwnedLand(@NotNull Player player) {
        Area area = landsIntegration.getArea(player.getLocation());
        if (area != null) {
            return area.getOwnerUID().equals(player.getUniqueId());
        }
        return false;
    }

    /**
     * Checks to see if the provided {@link Player} is standing in an {@link Area} that
     * they are trusted in.
     *
     * @param player The {@link Player} to check.
     * @return {@code true} if the provided player is standing in an {@link Area} that they are
     * trusted in.
     */
    public boolean isPlayerStandingInTrustedLand(@NotNull Player player) {
        Area area = landsIntegration.getArea(player.getLocation());
        if (area != null) {
            return area.isTrusted(player.getUniqueId());
        }
        return false;
    }

    /**
     * Checks to see if the provided {@link Player} is standing in an {@link Area} that
     * they are a tenant of.
     *
     * @param player The {@link Player} to check.
     * @return {@code true} if the provided player is standing in an {@link Area} that they
     * are a tenant of.
     */
    public boolean isPlayerStandingInTenantLand(@NotNull Player player) {
        Area area = landsIntegration.getArea(player.getLocation());
        if (area != null && area.getTenant() != null) {
            return area.getTenant() == player.getUniqueId();
        }
        return false;
    }

    /**
     * Checks to see if the {@link Player} is currently standing in an {@link Area}.
     *
     * @param player The {@link Player} to check.
     * @return {@code true} if the provided {@link Player} is standing in an {@link Area}.
     */
    public boolean isPlayerStandingInLand(@NotNull Player player) {
        return landsIntegration.getArea(player.getLocation()) != null;
    }

    @Override
    public boolean isPlayerInSafeZone(@NotNull Player player) {
        YamlDocument config = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE).getFile(FileType.MINING_CONFIG);
        boolean ownedLandsAreSafeZone = config.getBoolean(MainConfigFile.SAFE_ZONE_HOOKS_LANDS_OWNED_LANDS, false);
        boolean trustedLandsAreSafeZone = config.getBoolean(MainConfigFile.SAFE_ZONE_HOOKS_LANDS_TRUSTED_LANDS, false);
        boolean tenantLandsAreSafeZone = config.getBoolean(MainConfigFile.SAFE_ZONE_HOOKS_LANDS_TENANT_LANDS, false);
        boolean anyLandsAreSafeZone = config.getBoolean(MainConfigFile.SAFE_ZONE_HOOKS_LANDS_ANY_LANDS, false);
        boolean wildZonesAreSafeZone = config.getBoolean(MainConfigFile.SAFE_ZONE_HOOKS_LANDS_WILD_ZONE, false);
        if (ownedLandsAreSafeZone && isPlayerStandingInOwnedLand(player)) {
            return true;
        }
        else if (trustedLandsAreSafeZone && isPlayerStandingInTrustedLand(player)) {
            return true;
        }
        else if (tenantLandsAreSafeZone && isPlayerStandingInTenantLand(player)) {
            return true;
        }
        else if (anyLandsAreSafeZone && isPlayerStandingInLand(player)) {
            return true;
        }
        else return wildZonesAreSafeZone && !isPlayerStandingInLand(player);
    }
}
