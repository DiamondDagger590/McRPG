package us.eunoians.mcrpg.external.lands;


import com.diamonddagger590.mccore.registry.plugin.PluginHook;
import me.angeschossen.lands.api.LandsIntegration;
import me.angeschossen.lands.api.land.Area;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;

/**
 * A hook for containing all code related to <a href="https://www.spigotmc.org/resources/lands-%E2%AD%95-land-claim-plugin-%E2%9C%85-grief-prevention-protection-gui-management-nations-wars-1-21-support.53313/">Lands</a>
 * that McRPG needs in order to support it.
 */
public class LandsHook extends PluginHook {

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
}
