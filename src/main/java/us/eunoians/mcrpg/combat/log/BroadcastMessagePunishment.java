package us.eunoians.mcrpg.combat.log;

import com.diamonddagger590.mccore.configuration.common.ReloadableBoolean;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.ManagerKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.combat.CombatSession;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.CombatConfigFile;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.Map;

/**
 * Announces the combat log to every online player and the console. Delegates to the
 * {@code McRPGLocalizationManager}'s {@code broadcastMessage(Route, Map)} so each
 * recipient's message is resolved against their own locale chain.
 */
public class BroadcastMessagePunishment extends CombatLogPunishmentType {

    public static final NamespacedKey KEY =
            new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "broadcast_message");

    private static final String CONFIG_KEY = "broadcast-message";

    private ReloadableBoolean enabled;

    /**
     * Constructs a new {@link BroadcastMessagePunishment}.
     *
     * @param mcRPG The plugin instance for config and registry access.
     */
    public BroadcastMessagePunishment(@NotNull McRPG mcRPG) {
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
    public BroadcastMessagePunishment(@Nullable NamespacedKey expansionKey) {
        super(KEY, CONFIG_KEY, expansionKey);
    }

    @Override
    public boolean isEnabled() {
        return enabled == null || enabled.getContent();
    }

    @Override
    public void apply(@NotNull Player player, @NotNull CombatSession session,
                      @NotNull McRPG mcRPG) {
        var localizationManager = mcRPG.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION);
        Location loc = player.getLocation();
        localizationManager.broadcastMessage(LocalizationKey.COMBAT_LOG_BROADCAST, Map.of(
                "player", player.getName(),
                "world", loc.getWorld().getName(),
                "x", String.valueOf((int) loc.getX()),
                "y", String.valueOf((int) loc.getY()),
                "z", String.valueOf((int) loc.getZ())
        ));
    }
}
