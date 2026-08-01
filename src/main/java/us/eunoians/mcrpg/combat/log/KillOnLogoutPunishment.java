package us.eunoians.mcrpg.combat.log;

import com.diamonddagger590.mccore.configuration.common.ReloadableBoolean;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.ManagerKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.combat.CombatSession;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.CombatConfigFile;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.Set;

/**
 * Kills the player on logout by setting health to zero, triggering normal death
 * mechanics (item drops, XP loss, death message). Excludes {@link DropItemsPunishment}
 * because death already handles item drops.
 */
public class KillOnLogoutPunishment extends CombatLogPunishmentType {

    public static final NamespacedKey KEY =
            new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "kill_on_logout");

    private static final String CONFIG_KEY = "kill-on-logout";

    private ReloadableBoolean enabled;

    /**
     * Constructs a new {@link KillOnLogoutPunishment}.
     *
     * @param mcRPG The plugin instance for config and registry access.
     */
    public KillOnLogoutPunishment(@NotNull McRPG mcRPG) {
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
    public KillOnLogoutPunishment(@Nullable NamespacedKey expansionKey) {
        super(KEY, CONFIG_KEY, expansionKey);
    }

    @Override
    public boolean isEnabled() {
        return enabled == null || enabled.getContent();
    }

    @Override
    @NotNull
    public Set<NamespacedKey> getExcludes() {
        return Set.of(DropItemsPunishment.KEY);
    }

    @Override
    public void apply(@NotNull Player player, @NotNull CombatSession session,
                      @NotNull McRPG mcRPG) {
        player.setHealth(0);
    }
}
