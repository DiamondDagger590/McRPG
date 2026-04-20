package us.eunoians.mcrpg.configuration.file.hud;

import dev.dejvokep.boostedyaml.route.Route;
import us.eunoians.mcrpg.configuration.file.ConfigFile;

import static com.diamonddagger590.mccore.util.Methods.toRoutePath;

/**
 * Configuration file for HUD-related display settings.
 * <p>
 * Currently owns the action bar HUD keys; any future HUD surface (boss bar,
 * custom fonts, etc.) should live here too rather than bleeding into unrelated
 * feature configs.
 */
public final class HudConfigFile extends ConfigFile {

    // --- Action bar HUD ---
    private static final String ACTION_BAR_HEADER = toRoutePath("action-bar");

    /**
     * How often (in server ticks) the action bar HUD refreshes.
     */
    public static final Route ACTION_BAR_UPDATE_INTERVAL_TICKS = Route.fromString(toRoutePath(ACTION_BAR_HEADER, "update-interval-ticks"));

    /**
     * Whether HP and mana are rendered continuously on the action bar. When
     * disabled, only transient center content (combo dots, XP gains, ability
     * feedback, safe-zone transitions) is shown.
     */
    public static final Route ACTION_BAR_PERSISTENT_POOL_DISPLAY = Route.fromString(toRoutePath(ACTION_BAR_HEADER, "persistent-pool-display"));
}
