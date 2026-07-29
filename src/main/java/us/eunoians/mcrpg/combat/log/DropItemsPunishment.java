package us.eunoians.mcrpg.combat.log;

import com.diamonddagger590.mccore.configuration.common.ReloadableBoolean;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.ManagerKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.combat.CombatSession;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.CombatConfigFile;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.util.McRPGMethods;

/**
 * Drops the player's inventory at their logout location. Mutually excluded
 * by {@link KillOnLogoutPunishment} since death already drops items.
 */
public class DropItemsPunishment extends CombatLogPunishmentType {

    public static final NamespacedKey KEY =
            new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "drop_items");

    private static final String CONFIG_KEY = "drop-items";

    private ReloadableBoolean enabled;

    /**
     * Constructs a new {@link DropItemsPunishment}.
     *
     * @param mcRPG The plugin instance for config and registry access.
     */
    public DropItemsPunishment(@NotNull McRPG mcRPG) {
        super(KEY, CONFIG_KEY, null);
        YamlDocument config = mcRPG.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.FILE)
                .getFile(FileType.COMBAT_CONFIG);
        this.enabled = new ReloadableBoolean(config,
                Route.fromString(CombatConfigFile.PUNISHMENT_HEADER + "." + CONFIG_KEY));
        mcRPG.registryAccess().registry(RegistryKey.MANAGER)
                .manager(ManagerKey.RELOADABLE_CONTENT)
                .trackReloadableContent(this.enabled);
    }

    /**
     * Test-only constructor that skips config initialization. {@link #isEnabled()}
     * returns {@code true} by default when constructed this way.
     *
     * @param expansionKey The owning expansion key, or {@code null}.
     */
    public DropItemsPunishment(@Nullable NamespacedKey expansionKey) {
        super(KEY, CONFIG_KEY, expansionKey);
    }

    @Override
    public boolean isEnabled() {
        return enabled == null || enabled.getContent();
    }

    @Override
    public void apply(@NotNull Player player, @NotNull CombatSession session,
                      @NotNull McRPG mcRPG) {
        Location location = player.getLocation();
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && !item.getType().isAir()) {
                location.getWorld().dropItemNaturally(location, item);
            }
        }
        player.getInventory().clear();
    }
}
