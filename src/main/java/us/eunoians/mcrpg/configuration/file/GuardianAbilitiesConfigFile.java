package us.eunoians.mcrpg.configuration.file;

import dev.dejvokep.boostedyaml.route.Route;

import static com.diamonddagger590.mccore.util.Methods.toRoutePath;

/**
 * Contains all the {@link Route}s used for the guardian_abilities_configuration.yml.
 */
public final class GuardianAbilitiesConfigFile extends ConfigFile {

    private static final String ABILITY_CONFIGURATION_HEADER = "ability-configuration";

    // Phase Shift
    private static final String PHASE_SHIFT_HEADER = toRoutePath(ABILITY_CONFIGURATION_HEADER, "phase-shift");
    public static final Route PHASE_SHIFT_ENABLED = Route.fromString(toRoutePath(PHASE_SHIFT_HEADER, "enabled"));
    public static final Route PHASE_SHIFT_MANA_COST = Route.fromString(toRoutePath(PHASE_SHIFT_HEADER, "mana-cost"));
    public static final Route PHASE_SHIFT_COOLDOWN = Route.fromString(toRoutePath(PHASE_SHIFT_HEADER, "cooldown"));
    public static final Route PHASE_SHIFT_MAX_RANGE = Route.fromString(toRoutePath(PHASE_SHIFT_HEADER, "max-range"));
    public static final Route PHASE_SHIFT_LAST_HIT_WINDOW_SECONDS = Route.fromString(toRoutePath(PHASE_SHIFT_HEADER, "last-hit-window-seconds"));
    public static final Route PHASE_SHIFT_CRIT_WINDOW_TICKS = Route.fromString(toRoutePath(PHASE_SHIFT_HEADER, "crit-window-ticks"));
    public static final Route PHASE_SHIFT_CRIT_DAMAGE_MULTIPLIER = Route.fromString(toRoutePath(PHASE_SHIFT_HEADER, "crit-damage-multiplier"));
    public static final Route PHASE_SHIFT_TELEPORT_OFFSET = Route.fromString(toRoutePath(PHASE_SHIFT_HEADER, "teleport-offset-behind-target"));
    public static final Route PHASE_SHIFT_DISPLAY_ITEM = Route.fromString(toRoutePath(PHASE_SHIFT_HEADER, "display-item"));
    public static final Route PHASE_SHIFT_UNLOCK_CONDITIONS = Route.fromString(toRoutePath(PHASE_SHIFT_HEADER, "unlock-conditions"));

    // Whirlpool
    private static final String WHIRLPOOL_HEADER = toRoutePath(ABILITY_CONFIGURATION_HEADER, "whirlpool");
    public static final Route WHIRLPOOL_ENABLED = Route.fromString(toRoutePath(WHIRLPOOL_HEADER, "enabled"));
    public static final Route WHIRLPOOL_MANA_COST = Route.fromString(toRoutePath(WHIRLPOOL_HEADER, "mana-cost"));
    public static final Route WHIRLPOOL_COOLDOWN = Route.fromString(toRoutePath(WHIRLPOOL_HEADER, "cooldown"));
    public static final Route WHIRLPOOL_RADIUS = Route.fromString(toRoutePath(WHIRLPOOL_HEADER, "radius"));
    public static final Route WHIRLPOOL_DURATION_TICKS = Route.fromString(toRoutePath(WHIRLPOOL_HEADER, "duration-ticks"));
    public static final Route WHIRLPOOL_PULL_VELOCITY = Route.fromString(toRoutePath(WHIRLPOOL_HEADER, "pull-velocity"));
    public static final Route WHIRLPOOL_SLOWNESS_AMPLIFIER = Route.fromString(toRoutePath(WHIRLPOOL_HEADER, "slowness-amplifier"));
    public static final Route WHIRLPOOL_SLOWNESS_DURATION_TICKS = Route.fromString(toRoutePath(WHIRLPOOL_HEADER, "slowness-duration-ticks"));
    public static final Route WHIRLPOOL_TICK_INTERVAL = Route.fromString(toRoutePath(WHIRLPOOL_HEADER, "tick-interval"));
    public static final Route WHIRLPOOL_DISPLAY_ITEM = Route.fromString(toRoutePath(WHIRLPOOL_HEADER, "display-item"));
    public static final Route WHIRLPOOL_UNLOCK_CONDITIONS = Route.fromString(toRoutePath(WHIRLPOOL_HEADER, "unlock-conditions"));

    // Waterlogged Strike
    private static final String WATERLOGGED_STRIKE_HEADER = toRoutePath(ABILITY_CONFIGURATION_HEADER, "waterlogged-strike");
    public static final Route WATERLOGGED_STRIKE_ENABLED = Route.fromString(toRoutePath(WATERLOGGED_STRIKE_HEADER, "enabled"));
    public static final Route WATERLOGGED_STRIKE_MANA_COST = Route.fromString(toRoutePath(WATERLOGGED_STRIKE_HEADER, "mana-cost"));
    public static final Route WATERLOGGED_STRIKE_COOLDOWN = Route.fromString(toRoutePath(WATERLOGGED_STRIKE_HEADER, "cooldown"));
    public static final Route WATERLOGGED_STRIKE_DAMAGE = Route.fromString(toRoutePath(WATERLOGGED_STRIKE_HEADER, "damage"));
    public static final Route WATERLOGGED_STRIKE_MAX_RANGE = Route.fromString(toRoutePath(WATERLOGGED_STRIKE_HEADER, "max-range"));
    public static final Route WATERLOGGED_STRIKE_PROJECTILE_SPEED = Route.fromString(toRoutePath(WATERLOGGED_STRIKE_HEADER, "projectile-speed"));
    public static final Route WATERLOGGED_STRIKE_SLOWNESS_AMPLIFIER = Route.fromString(toRoutePath(WATERLOGGED_STRIKE_HEADER, "slowness-amplifier"));
    public static final Route WATERLOGGED_STRIKE_SLOWNESS_DURATION_TICKS = Route.fromString(toRoutePath(WATERLOGGED_STRIKE_HEADER, "slowness-duration-ticks"));
    public static final Route WATERLOGGED_STRIKE_DISPLAY_ITEM = Route.fromString(toRoutePath(WATERLOGGED_STRIKE_HEADER, "display-item"));
    public static final Route WATERLOGGED_STRIKE_UNLOCK_CONDITIONS = Route.fromString(toRoutePath(WATERLOGGED_STRIKE_HEADER, "unlock-conditions"));

    // Tsunami Wall
    private static final String TSUNAMI_WALL_HEADER = toRoutePath(ABILITY_CONFIGURATION_HEADER, "tsunami-wall");
    public static final Route TSUNAMI_WALL_ENABLED = Route.fromString(toRoutePath(TSUNAMI_WALL_HEADER, "enabled"));
    public static final Route TSUNAMI_WALL_MANA_COST = Route.fromString(toRoutePath(TSUNAMI_WALL_HEADER, "mana-cost"));
    public static final Route TSUNAMI_WALL_COOLDOWN = Route.fromString(toRoutePath(TSUNAMI_WALL_HEADER, "cooldown"));
    public static final Route TSUNAMI_WALL_WIDTH = Route.fromString(toRoutePath(TSUNAMI_WALL_HEADER, "width"));
    public static final Route TSUNAMI_WALL_HEIGHT = Route.fromString(toRoutePath(TSUNAMI_WALL_HEADER, "height"));
    public static final Route TSUNAMI_WALL_DURATION_TICKS = Route.fromString(toRoutePath(TSUNAMI_WALL_HEADER, "duration-ticks"));
    public static final Route TSUNAMI_WALL_KNOCKBACK_STRENGTH = Route.fromString(toRoutePath(TSUNAMI_WALL_HEADER, "knockback-strength"));
    public static final Route TSUNAMI_WALL_SLOWNESS_AMPLIFIER = Route.fromString(toRoutePath(TSUNAMI_WALL_HEADER, "slowness-amplifier"));
    public static final Route TSUNAMI_WALL_SLOWNESS_DURATION_TICKS = Route.fromString(toRoutePath(TSUNAMI_WALL_HEADER, "slowness-duration-ticks"));
    public static final Route TSUNAMI_WALL_SPAWN_DISTANCE = Route.fromString(toRoutePath(TSUNAMI_WALL_HEADER, "spawn-distance"));
    public static final Route TSUNAMI_WALL_TRAVEL_SPEED = Route.fromString(toRoutePath(TSUNAMI_WALL_HEADER, "travel-speed"));
    public static final Route TSUNAMI_WALL_DISPLAY_ITEM = Route.fromString(toRoutePath(TSUNAMI_WALL_HEADER, "display-item"));
    public static final Route TSUNAMI_WALL_UNLOCK_CONDITIONS = Route.fromString(toRoutePath(TSUNAMI_WALL_HEADER, "unlock-conditions"));
}
