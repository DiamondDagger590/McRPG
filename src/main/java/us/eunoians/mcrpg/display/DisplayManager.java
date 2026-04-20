package us.eunoians.mcrpg.display;

import com.diamonddagger590.mccore.configuration.ReloadableContent;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.Manager;
import com.diamonddagger590.mccore.registry.manager.ManagerKey;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.hud.HudConfigFile;
import us.eunoians.mcrpg.display.hud.ActionBarHudDisplay;
import us.eunoians.mcrpg.display.hud.ActionBarHudRenderer;
import us.eunoians.mcrpg.display.hud.FontWidthTable;
import us.eunoians.mcrpg.display.hud.MinecraftDefaultFontWidthTable;
import us.eunoians.mcrpg.display.impl.PlayerDisplay;
import us.eunoians.mcrpg.display.impl.TickablePlayerDisplay;
import us.eunoians.mcrpg.entity.McRPGPlayerManager;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Function;

/**
 * Coordinates per-player {@link PlayerDisplay}s on behalf of McRPG.
 * <p>
 * The manager owns <em>shared</em> collaborators (the {@link ActionBarHudRenderer}
 * and its backing {@link FontWidthTable} + reloadable config flag) but does not
 * own per-player state. Each player holds their own display instances in a
 * type-keyed map on {@link McRPGPlayer}; this manager exposes a generic
 * {@code get/has/set/remove/getOrCreateDisplay} API that delegates to that map,
 * plus a generic per-tick driver for anything implementing
 * {@link TickablePlayerDisplay}.
 */
public class DisplayManager extends Manager<McRPG> {

    private final ActionBarHudRenderer hudRenderer;
    private final ReloadableContent<Boolean> persistentPoolEnabled;
    /**
     * Scratch buffer reused across every player in a single {@link #tickDisplays}
     * pass so the hot path doesn't allocate a fresh snapshot list each tick.
     * Strictly main-thread use only (HUD tick runs on the Bukkit scheduler).
     */
    private final ArrayList<PlayerDisplay> tickSnapshotBuffer = new ArrayList<>(4);

    public DisplayManager(@NotNull McRPG plugin) {
        super(plugin);
        var fileManager = plugin.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE);
        this.persistentPoolEnabled = new ReloadableContent<>(
                fileManager.getFile(FileType.HUD_CONFIG),
                HudConfigFile.ACTION_BAR_PERSISTENT_POOL_DISPLAY,
                (doc, route) -> doc.getBoolean(route, true)
        );
        plugin.registryAccess().registry(RegistryKey.MANAGER)
                .manager(ManagerKey.RELOADABLE_CONTENT)
                .trackReloadableContent(persistentPoolEnabled);
        FontWidthTable widths = new MinecraftDefaultFontWidthTable();
        this.hudRenderer = new ActionBarHudRenderer(widths, persistentPoolEnabled);
    }

    /**
     * @return The shared {@link ActionBarHudRenderer} used by every HUD display.
     */
    @NotNull
    public ActionBarHudRenderer getHudRenderer() {
        return hudRenderer;
    }

    /**
     * @return The reloadable flag controlling whether HP and mana are rendered
     * continuously on the action bar.
     */
    @NotNull
    public ReloadableContent<Boolean> getPersistentPoolEnabled() {
        return persistentPoolEnabled;
    }

    /**
     * Fetches the {@link PlayerDisplay} of the requested type for {@code player}.
     *
     * @param player The player whose displays to look up.
     * @param type   The base display class to look up under.
     * @param <T>    The display type.
     * @return An {@link Optional} containing the registered display, or empty
     * if none.
     */
    @NotNull
    public <T extends PlayerDisplay> Optional<T> getDisplay(@NotNull McRPGPlayer player, @NotNull Class<T> type) {
        return player.getDisplay(type);
    }

    /**
     * @param player The player to inspect.
     * @param type   The base display class.
     * @return {@code true} if the player has a display registered under
     * {@code type}.
     */
    public boolean hasDisplay(@NotNull McRPGPlayer player, @NotNull Class<? extends PlayerDisplay> type) {
        return player.hasDisplay(type);
    }

    /**
     * Registers a {@link PlayerDisplay}, cleaning up any existing display of the
     * same type.
     *
     * @param player  The player to register the display on.
     * @param type    The base display class to register under.
     * @param display The display instance to register.
     * @param <T>     The display type.
     */
    public <T extends PlayerDisplay> void setDisplay(@NotNull McRPGPlayer player,
                                                     @NotNull Class<T> type,
                                                     @NotNull T display) {
        player.setDisplay(type, display);
    }

    /**
     * Removes a registered {@link PlayerDisplay}, invoking
     * {@link PlayerDisplay#cleanDisplay()} if one existed.
     *
     * @param player The player whose display should be removed.
     * @param type   The base display class to remove.
     */
    public void removeDisplay(@NotNull McRPGPlayer player, @NotNull Class<? extends PlayerDisplay> type) {
        player.removeDisplay(type);
    }

    /**
     * Cleans up and removes every {@link PlayerDisplay} for {@code player}.
     *
     * @param player The player to clear displays on.
     */
    public void clearAllDisplays(@NotNull McRPGPlayer player) {
        player.clearAllDisplays();
    }

    /**
     * Returns the existing display of {@code type} for {@code player}, or
     * creates and registers one using {@code factory} if none exists. Used by
     * call sites that lazily materialise a display on first contact (combo,
     * cooldown, XP, safe-zone, etc.) without duplicating an
     * {@code orElseGet + setDisplay} pair at every site.
     *
     * @param player  The player to fetch or create a display for.
     * @param type    The base display class.
     * @param factory Factory invoked exactly once if no display is registered.
     * @param <T>     The display type.
     * @return The registered display.
     */
    @NotNull
    public <T extends PlayerDisplay> T getOrCreateDisplay(@NotNull McRPGPlayer player,
                                                          @NotNull Class<T> type,
                                                          @NotNull Function<McRPGPlayer, T> factory) {
        Optional<T> existing = player.getDisplay(type);
        if (existing.isPresent()) {
            return existing.get();
        }
        T created = factory.apply(player);
        player.setDisplay(type, created);
        return created;
    }

    /**
     * Returns the existing {@link ActionBarHudDisplay} for {@code player}, or
     * materialises one wired to the shared {@link ActionBarHudRenderer} if
     * none exists. This is the canonical entry point for any feature that
     * needs to push content into the HUD — the factory, class key, and renderer
     * wiring live in one place.
     *
     * @param player The player whose HUD display should be fetched or created.
     * @return The registered {@link ActionBarHudDisplay}.
     */
    @NotNull
    public ActionBarHudDisplay getOrCreateActionBarHud(@NotNull McRPGPlayer player) {
        return getOrCreateDisplay(player, ActionBarHudDisplay.class,
                p -> new ActionBarHudDisplay(p, hudRenderer));
    }

    /**
     * Drives the per-tick lifecycle for every online player's
     * {@link TickablePlayerDisplay}s.
     *
     * @param currentTick    The current server tick.
     * @param secondsElapsed Real seconds elapsed since the previous tick.
     */
    public void tickDisplays(long currentTick, double secondsElapsed) {
        McRPGPlayerManager playerManager = plugin().registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.PLAYER);
        boolean poolEnabled = hudRenderer.isPersistentPoolDisplayEnabled();
        for (Player player : Bukkit.getOnlinePlayers()) {
            Optional<McRPGPlayer> mcRPGPlayerOpt = playerManager.getPlayer(player.getUniqueId());
            if (mcRPGPlayerOpt.isEmpty()) {
                continue;
            }
            McRPGPlayer mcRPGPlayer = mcRPGPlayerOpt.get();
            // When the persistent HP/mana display is enabled every online
            // player needs a HUD display so HP/mana renders continuously; in
            // disabled mode we leave creation to the call sites that surface
            // center content.
            if (poolEnabled) {
                getOrCreateActionBarHud(mcRPGPlayer);
            }
            tickSnapshotBuffer.clear();
            mcRPGPlayer.snapshotDisplaysInto(tickSnapshotBuffer);
            for (int i = 0, size = tickSnapshotBuffer.size(); i < size; i++) {
                if (tickSnapshotBuffer.get(i) instanceof TickablePlayerDisplay tickable) {
                    tickable.tick(currentTick, secondsElapsed);
                }
            }
        }
        // Release strong references so displays removed mid-tick are GC-eligible.
        tickSnapshotBuffer.clear();
    }
}
