package us.eunoians.mcrpg.configuration.file.combo;

import dev.dejvokep.boostedyaml.route.Route;
import us.eunoians.mcrpg.configuration.file.ConfigFile;

import static com.diamonddagger590.mccore.util.Methods.toRoutePath;

/**
 * Configuration file for the combo activation system (PoC).
 * <p>
 * All keys are nested under the {@code combo} root section.
 */
public final class ComboConfigFile extends ConfigFile {

    private static final int CURRENT_VERSION = 1;

    // Root header
    private static final String COMBO_HEADER = "combo";

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
    public static final Route SHOCKWAVE_HUNGER_COST = Route.fromString(toRoutePath(SHOCKWAVE_HEADER, "hunger-cost"));
    public static final Route SHOCKWAVE_RADIUS = Route.fromString(toRoutePath(SHOCKWAVE_HEADER, "radius"));
    public static final Route SHOCKWAVE_KNOCKBACK_FORCE = Route.fromString(toRoutePath(SHOCKWAVE_HEADER, "knockback-force"));

    // Cleave
    private static final String CLEAVE_HEADER = toRoutePath(ABILITIES_HEADER, "cleave");
    public static final Route CLEAVE_HUNGER_COST = Route.fromString(toRoutePath(CLEAVE_HEADER, "hunger-cost"));
    public static final Route CLEAVE_RADIUS = Route.fromString(toRoutePath(CLEAVE_HEADER, "radius"));
    public static final Route CLEAVE_DAMAGE = Route.fromString(toRoutePath(CLEAVE_HEADER, "damage"));

    // RageSpike
    private static final String RAGE_SPIKE_HEADER = toRoutePath(ABILITIES_HEADER, "rage-spike");
    public static final Route RAGE_SPIKE_HUNGER_COST = Route.fromString(toRoutePath(RAGE_SPIKE_HEADER, "hunger-cost"));

    // OreScanner
    private static final String ORE_SCANNER_HEADER = toRoutePath(ABILITIES_HEADER, "ore-scanner");
    public static final Route ORE_SCANNER_HUNGER_COST = Route.fromString(toRoutePath(ORE_SCANNER_HEADER, "hunger-cost"));

    // MassHarvest
    private static final String MASS_HARVEST_HEADER = toRoutePath(ABILITIES_HEADER, "mass-harvest");
    public static final Route MASS_HARVEST_HUNGER_COST = Route.fromString(toRoutePath(MASS_HARVEST_HEADER, "hunger-cost"));
}
