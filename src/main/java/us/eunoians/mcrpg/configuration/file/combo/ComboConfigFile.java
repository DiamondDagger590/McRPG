package us.eunoians.mcrpg.configuration.file.combo;

import dev.dejvokep.boostedyaml.route.Route;
import us.eunoians.mcrpg.configuration.file.ConfigFile;

import static com.diamonddagger590.mccore.util.Methods.toRoutePath;

/**
 * Configuration file for the combo activation system and combat stat settings.
 * <p>
 * Combo keys are nested under {@code combo}. Stat base values under
 * {@code stats}. HUD / display keys live in
 * {@link us.eunoians.mcrpg.configuration.file.hud.HudConfigFile}.
 */
public final class ComboConfigFile extends ConfigFile {

    private static final int CURRENT_VERSION = 3;

    // --- Stats ---
    private static final String STATS_HEADER = "stats";

    private static final String HEALTH_HEADER = toRoutePath(STATS_HEADER, "health");
    public static final Route HEALTH_BASE_MAX = Route.fromString(toRoutePath(HEALTH_HEADER, "base-max"));

    private static final String MANA_HEADER = toRoutePath(STATS_HEADER, "mana");
    public static final Route MANA_BASE_MAX = Route.fromString(toRoutePath(MANA_HEADER, "base-max"));
    public static final Route MANA_REGEN_PER_SECOND = Route.fromString(toRoutePath(MANA_HEADER, "regen-per-second"));

    // --- Combo ---
    private static final String COMBO_HEADER = "combo";

    // Allowed held items
    public static final Route COMBO_ALLOWED_ITEMS = Route.fromString(toRoutePath(COMBO_HEADER, "allowed-items"));

    // Timing
    private static final String TIMING_HEADER = toRoutePath(COMBO_HEADER, "timing");
    public static final Route TIMING_WINDOW_TICKS = Route.fromString(toRoutePath(TIMING_HEADER, "window-ticks"));

    // Failure feedback
    private static final String FAILURE_HEADER = toRoutePath(COMBO_HEADER, "failure-feedback");
    public static final Route FAILURE_SOUND = Route.fromString(toRoutePath(FAILURE_HEADER, "sound"));
    public static final Route FAILURE_SOUND_VOLUME = Route.fromString(toRoutePath(FAILURE_HEADER, "volume"));
    public static final Route FAILURE_SOUND_PITCH = Route.fromString(toRoutePath(FAILURE_HEADER, "pitch"));

    // Per-ability settings
    private static final String ABILITIES_HEADER = toRoutePath(COMBO_HEADER, "abilities");

    // Shockwave
    private static final String SHOCKWAVE_HEADER = toRoutePath(ABILITIES_HEADER, "shockwave");
    public static final Route SHOCKWAVE_MANA_COST = Route.fromString(toRoutePath(SHOCKWAVE_HEADER, "mana-cost"));
    public static final Route SHOCKWAVE_RADIUS = Route.fromString(toRoutePath(SHOCKWAVE_HEADER, "radius"));
    public static final Route SHOCKWAVE_KNOCKBACK_FORCE = Route.fromString(toRoutePath(SHOCKWAVE_HEADER, "knockback-force"));

    // Cleave
    private static final String CLEAVE_HEADER = toRoutePath(ABILITIES_HEADER, "cleave");
    public static final Route CLEAVE_MANA_COST = Route.fromString(toRoutePath(CLEAVE_HEADER, "mana-cost"));
    public static final Route CLEAVE_RADIUS = Route.fromString(toRoutePath(CLEAVE_HEADER, "radius"));
    public static final Route CLEAVE_DAMAGE = Route.fromString(toRoutePath(CLEAVE_HEADER, "damage"));

    // RageSpike
    private static final String RAGE_SPIKE_HEADER = toRoutePath(ABILITIES_HEADER, "rage-spike");
    public static final Route RAGE_SPIKE_MANA_COST = Route.fromString(toRoutePath(RAGE_SPIKE_HEADER, "mana-cost"));

    // OreScanner
    private static final String ORE_SCANNER_HEADER = toRoutePath(ABILITIES_HEADER, "ore-scanner");
    public static final Route ORE_SCANNER_MANA_COST = Route.fromString(toRoutePath(ORE_SCANNER_HEADER, "mana-cost"));

    // MassHarvest
    private static final String MASS_HARVEST_HEADER = toRoutePath(ABILITIES_HEADER, "mass-harvest");
    public static final Route MASS_HARVEST_MANA_COST = Route.fromString(toRoutePath(MASS_HARVEST_HEADER, "mana-cost"));
}
