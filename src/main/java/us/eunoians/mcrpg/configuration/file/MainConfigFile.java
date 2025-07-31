package us.eunoians.mcrpg.configuration.file;

import dev.dejvokep.boostedyaml.route.Route;

public final class MainConfigFile extends ConfigFile {

    private static final int CURRENT_VERSION = 1;

    // Headers
    private static final Route CONFIGURATION_HEADER = Route.fromString("configuration");
    private static final Route ADMIN_CONFIGURATION_HEADER = Route.addTo(CONFIGURATION_HEADER, "admin");
    private static final Route SAVE_TASK_HEADER = Route.addTo(CONFIGURATION_HEADER, "save-task");
    private static final Route EXPERIENCE_CONFIGURATION_HEADER = Route.addTo(CONFIGURATION_HEADER, "experience");
    private static final Route BOOSTED_EXPERIENCE_CONFIGURATION_HEADER = Route.addTo(EXPERIENCE_CONFIGURATION_HEADER, "boosted-experience");
    private static final Route SKILL_BOOKS_CONFIGURATION_HEADER = Route.addTo(CONFIGURATION_HEADER, "skill-books");
    private static final Route DISPLAY_CONFIGURATION_HEADER = Route.addTo(CONFIGURATION_HEADER, "display");
    private static final Route DISPLAY_EXPERIENCE_CONFIGURATION_HEADER = Route.addTo(DISPLAY_CONFIGURATION_HEADER, "exp-updates");
    private static final Route GAMEPLAY_CONFIGURATION_HEADER = Route.addTo(CONFIGURATION_HEADER, "gameplay");
    private static final Route LOADOUT_CONFIGURATION_HEADER = Route.addTo(GAMEPLAY_CONFIGURATION_HEADER, "loadout");
    private static final Route MCMMO_CONFIGURATION_HEADER = Route.addTo(CONFIGURATION_HEADER, "mcmmo");
    private static final Route DATABASE_HEADER = Route.addTo(CONFIGURATION_HEADER, "database");
    private static final Route LOCALIZATION_HEADER = Route.addTo(CONFIGURATION_HEADER, "localization");

    // Database fields
    public static final Route DATABASE_DRIVER = Route.addTo(DATABASE_HEADER, "driver");

    // General Configuration fields
    public static final Route DISABLED_WORLDS = Route.addTo(CONFIGURATION_HEADER, "disabled-worlds");
    public static final Route ENABLE_ABILITY_SPY = Route.addTo(ADMIN_CONFIGURATION_HEADER, "enable-ability-spy");

    // Localization
    public static final Route SERVER_DEFAULT_LOCALE = Route.addTo(LOCALIZATION_HEADER, "server-default-locale");

    // Save task fields
    public static final Route SAVE_TASK_FREQUENCY = Route.addTo(SAVE_TASK_HEADER, "frequency");

    // Experience fields
    public static final Route MODIFY_MOB_SPAWN_EXPERIENCE_CONFIGURATION = Route.addTo(EXPERIENCE_CONFIGURATION_HEADER, ".modify-mob-spawn-experience");
    public static final Route MAX_DAMAGE_CAP_TO_AWARD_EXPERIENCE = Route.addTo(EXPERIENCE_CONFIGURATION_HEADER, "max-damage-cap-to-award-exp");
    public static final Route EXPERIENCE_MULTIPLIER_LIMIT = Route.addTo(EXPERIENCE_CONFIGURATION_HEADER, "exp-multiplier-limit");
    public static final Route SHIELD_BLOCKING_MODIFIER = Route.addTo(EXPERIENCE_CONFIGURATION_HEADER, "shield-blocking-modifier");
    public static final Route RESET_EXPERIENCE_UPON_REDEEM_LEVELS = Route.addTo(EXPERIENCE_CONFIGURATION_HEADER, "reset-exp-upon-redeemed-levels");
    public static final Route DISABLE_EXPERIENCE_FROM_ENDER_PEARLS = Route.addTo(EXPERIENCE_CONFIGURATION_HEADER, "disable-experience-from-ender-pearls");
    public static final Route BOOSTED_EXPERIENCE_USAGE_RATE = Route.addTo(BOOSTED_EXPERIENCE_CONFIGURATION_HEADER, "usage-rate");

    // Rested Experience
    private static final Route RESTED_EXPERIENCE_HEADER = Route.addTo(EXPERIENCE_CONFIGURATION_HEADER, "rested-experience");
    private static final Route SAFE_ZONE_HEADER = Route.addTo(RESTED_EXPERIENCE_HEADER, "safe-zone");
    private static final Route ONLINE_RESTED_EXPERIENCE_TASK_HEADER = Route.addTo(RESTED_EXPERIENCE_HEADER, "");
    private static final Route SAFE_ZONE_HOOKS_HEADER = Route.addTo(SAFE_ZONE_HEADER, "safe-zone-hooks");
    private static final Route SAFE_ZONE_HOOKS_WORLD_GUARD_HEADER = Route.addTo(SAFE_ZONE_HOOKS_HEADER, "world-guard");
    private static final Route SAFE_ZONE_HOOKS_LANDS_HEADER = Route.addTo(SAFE_ZONE_HOOKS_HEADER, "lands");
    public static final Route RESTED_EXPERIENCE_ACCUMULATION_RATE = Route.addTo(RESTED_EXPERIENCE_HEADER, "accumulation-rate");
    public static final Route RESTED_EXPERIENCE_USAGE_RATE = Route.addTo(RESTED_EXPERIENCE_HEADER, "usage-rate");
    public static final Route RESTED_EXPERIENCE_MAXIMUM_ACCUMULATION = Route.addTo(RESTED_EXPERIENCE_HEADER, "maximum-accumulation");
    public static final Route RESTED_EXPERIENCE_ALLOW_ONLINE_ACCUMULATION = Route.addTo(RESTED_EXPERIENCE_HEADER, "allow-online-accumulation");
    public static final Route ONLINE_RESTED_EXPERIENCE_TASK_FREQUENCY = Route.addTo(ONLINE_RESTED_EXPERIENCE_TASK_HEADER, "frequency");
    public static final Route SAFE_ZONE_ALLOW_ACCUMULATION = Route.addTo(SAFE_ZONE_HEADER, "allow-safe-zone-accumulation");
    public static final Route SAFE_ZONE_ACCUMULATION_RATE = Route.addTo(SAFE_ZONE_HEADER, "safe-zone-accumulation-rate");
    public static final Route SAFE_ZONE_HOOKS_WORLD_GUARD_ENABLED = Route.addTo(SAFE_ZONE_HOOKS_WORLD_GUARD_HEADER, "enabled");
    public static final Route SAFE_ZONE_HOOKS_LANDS_OWNED_LANDS = Route.addTo(SAFE_ZONE_HOOKS_LANDS_HEADER, "owned-lands");
    public static final Route SAFE_ZONE_HOOKS_LANDS_TRUSTED_LANDS = Route.addTo(SAFE_ZONE_HOOKS_LANDS_HEADER, "trusted_lands");
    public static final Route SAFE_ZONE_HOOKS_LANDS_TENANT_LANDS = Route.addTo(SAFE_ZONE_HOOKS_LANDS_HEADER, "tenant-lands");
    public static final Route SAFE_ZONE_HOOKS_LANDS_ANY_LANDS = Route.addTo(SAFE_ZONE_HOOKS_LANDS_HEADER, "any-lands");
    public static final Route SAFE_ZONE_HOOKS_LANDS_WILD_ZONE = Route.addTo(SAFE_ZONE_HOOKS_LANDS_HEADER, "wild-zone");

    // Display
    private static final Route DISPLAY_HEADER = Route.addTo(CONFIGURATION_HEADER, "display");
    private static final Route DISPLAY_EXPERIENCE_UPDATES_HEADER = Route.addTo(DISPLAY_HEADER, "experience-updates");
    private static final Route EXPERIENCE_BOSS_BAR_DISPLAY_HEADER = Route.addTo(DISPLAY_HEADER, "boss-bar");
    public static final Route DISPLAY_EXPERIENCE_UPDATES_ENABLED = Route.addTo(DISPLAY_EXPERIENCE_UPDATES_HEADER, "enabled");
    public static final Route EXPERIENCE_BOSS_BAR_DISPLAY_DURATION = Route.addTo(EXPERIENCE_BOSS_BAR_DISPLAY_HEADER, "display-duration");
    public static final Route EXPERIENCE_BOSS_BAR_DISPLAY_COLOR = Route.addTo(EXPERIENCE_BOSS_BAR_DISPLAY_HEADER, "color");
    public static final Route EXPERIENCE_BOSS_BAR_STYLE = Route.addTo(EXPERIENCE_BOSS_BAR_DISPLAY_HEADER, "style");

    // Gameplay
    private static final Route LOADOUT_HEADER = Route.addTo(GAMEPLAY_CONFIGURATION_HEADER, "loadout");
    public static final Route LOADOUT_DISPLAY_NAME_RESPONSE_TIMEOUT = Route.addTo(LOADOUT_HEADER, "display-name-response-timeout");

    public static final Route DISABLE_SKILL_BOOKS_IN_END = Route.addTo(SKILL_BOOKS_CONFIGURATION_HEADER, "disable-books-in-end");
    public static final Route EXPERIENCE_UPDATES_ENABLED = Route.addTo(DISPLAY_EXPERIENCE_CONFIGURATION_HEADER, "enabled");
    public static final Route EXPERIENCE_UPDATE_DISPLAY_TYPE = Route.addTo(DISPLAY_EXPERIENCE_CONFIGURATION_HEADER, "display-type");
    public static final Route EXPERIENCE_UPDATE_DISPLAY_DURATION = Route.addTo(DISPLAY_EXPERIENCE_CONFIGURATION_HEADER, "display-duration");
    public static final Route MAX_LOADOUT_AMOUNT = Route.addTo(LOADOUT_CONFIGURATION_HEADER, "max-loadout-amount");
    public static final Route MAX_LOADOUT_SIZE = Route.addTo(LOADOUT_CONFIGURATION_HEADER, "max-loadout-size");
    public static final Route REQUIRE_EMPTY_OFF_HAND_TO_READY = Route.addTo(GAMEPLAY_CONFIGURATION_HEADER, "require-empty-off-hand-to-ready");
    public static final Route ENABLE_LEVEL_UP_PERMISSIONS = Route.addTo(GAMEPLAY_CONFIGURATION_HEADER, "enable-level-up-permissions");
    public static final Route MOB_HEALTH_BAR_ENABLED = Route.addTo(GAMEPLAY_CONFIGURATION_HEADER, "mob-health-bar");
    public static final Route MOB_HEALTH_BAR_DISPLAY_DURATION = Route.addTo(GAMEPLAY_CONFIGURATION_HEADER, "health-bar-display-duration");
    public static final Route MCMMO_CONVERSION_EQUATION = Route.addTo(MCMMO_CONFIGURATION_HEADER, "conversion-equation");

}
