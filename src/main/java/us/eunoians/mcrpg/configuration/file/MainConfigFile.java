package us.eunoians.mcrpg.configuration.file;

import dev.dejvokep.boostedyaml.route.Route;

public class MainConfigFile extends ConfigFile {

    private static final int CURRENT_VERSION = 1;

    // Headers
    private static final Route CONFIGURATION_HEADER = Route.fromString("configuration");
    private static final Route ADMIN_CONFIGURATION_HEADER = Route.addTo(CONFIGURATION_HEADER, "admin");
    private static final Route EXPERIENCE_CONFIGURATION_HEADER = Route.addTo(CONFIGURATION_HEADER, "experience");
    private static final Route BOOSTED_EXPERIENCE_CONFIGURATION_HEADER = Route.addTo(EXPERIENCE_CONFIGURATION_HEADER, "boosted-experience");
    private static final Route MODIFY_MOB_SPAWN_EXPERIENCE_CONFIGURATION_HEADER = Route.addTo(EXPERIENCE_CONFIGURATION_HEADER, ".modify-mob-spawn-experience");
    private static final Route SKILL_BOOKS_CONFIGURATION_HEADER = Route.addTo(CONFIGURATION_HEADER, "skill-books");
    private static final Route DISPLAY_CONFIGURATION_HEADER = Route.addTo(CONFIGURATION_HEADER, "display");
    private static final Route DISPLAY_EXPERIENCE_CONFIGURATION_HEADER = Route.addTo(DISPLAY_CONFIGURATION_HEADER, "exp-updates");
    private static final Route GAMEPLAY_CONFIGURATION_HEADER = Route.addTo(CONFIGURATION_HEADER, "gameplay");
    private static final Route MOB_HEALTH_BAR_CONFIGURATION_HEADER = Route.addTo(GAMEPLAY_CONFIGURATION_HEADER, "mob-health-bar");
    private static final Route MCMMO_CONFIGURATION_HEADER = Route.addTo(CONFIGURATION_HEADER, "mcmmo");

    private static final Route DATABASE_HEADER = Route.addTo(CONFIGURATION_HEADER, "database");
    public static final Route DATABASE_DRIVER = Route.addTo(DATABASE_HEADER, "driver");
    public static final Route DISABLED_WORLDS = Route.addTo(CONFIGURATION_HEADER, "disabled-worlds");
    public static final Route SAVE_INTERVAL = Route.addTo(CONFIGURATION_HEADER, "save-interval");
    public static final Route LANGUAGE_FILE = Route.addTo(CONFIGURATION_HEADER, "language-file");
    public static final Route DISABLE_TIPS = Route.addTo(CONFIGURATION_HEADER, "disable-tips");
    public static final Route ENABLE_ABILITY_SPY = Route.addTo(ADMIN_CONFIGURATION_HEADER, "enable-ability-spy");
    public static final Route MAX_DAMAGE_CAP_TO_AWARD_EXPERIENCE = Route.addTo(EXPERIENCE_CONFIGURATION_HEADER, "max-damage-cap-to-award-exp");
    public static final Route EXPERIENCE_MULTIPLIER_LIMIT = Route.addTo(EXPERIENCE_CONFIGURATION_HEADER, "exp-multiplier-limit");
    public static final Route SHIELD_BLOCKING_MODIFIER = Route.addTo(EXPERIENCE_CONFIGURATION_HEADER, "shield-blocking-modifier");
    public static final Route DISABLE_EXPERIENCE_FROM_ENDER_PEARLS = Route.addTo(EXPERIENCE_CONFIGURATION_HEADER, "disable-experience-from-ender-pearls");
    public static final Route BOOSTED_EXPERIENCE_USAGE_RATE = Route.addTo(BOOSTED_EXPERIENCE_CONFIGURATION_HEADER, "usage-rate");
    public static final Route MODIFY_MOB_SPAWN_EXPERIENCE_FROM_SPAWNER = Route.addTo(MODIFY_MOB_SPAWN_EXPERIENCE_CONFIGURATION_HEADER, "spawner");
    public static final Route MODIFY_MOB_SPAWN_EXPERIENCE_FROM_SPAWN_EGG = Route.addTo(MODIFY_MOB_SPAWN_EXPERIENCE_CONFIGURATION_HEADER, "spawn-egg");
    public static final Route RESET_EXPERIENCE_UPON_REDEEM_LEVELS = Route.addTo(EXPERIENCE_CONFIGURATION_HEADER, "reset-exp-upon-redeemed-levels");
    public static final Route DISABLE_SKILL_BOOKS_IN_END = Route.addTo(SKILL_BOOKS_CONFIGURATION_HEADER, "disable-books-in-end");
    public static final Route EXPERIENCE_UPDATES_ENABLED = Route.addTo(DISPLAY_EXPERIENCE_CONFIGURATION_HEADER, "enabled");
    public static final Route EXPERIENCE_UPDATE_DISPLAY_TYPE = Route.addTo(DISPLAY_EXPERIENCE_CONFIGURATION_HEADER, "display-type");
    public static final Route EXPERIENCE_UPDATE_DISPLAY_DURATION = Route.addTo(DISPLAY_EXPERIENCE_CONFIGURATION_HEADER, "display-duration");
    public static final Route REPLACE_ABILITY_COOLDOWN_TIME = Route.addTo(GAMEPLAY_CONFIGURATION_HEADER, "replace-ability-cooldown-time");
    public static final Route REQUIRE_EMPTY_OFF_HAND_TO_READY = Route.addTo(GAMEPLAY_CONFIGURATION_HEADER, "require-empty-off-hand-to-ready");
    public static final Route ENABLE_LEVEL_UP_PERMISSIONS = Route.addTo(GAMEPLAY_CONFIGURATION_HEADER, "enable-level-up-permissions");
    public static final Route MOB_HEALTH_BAR_ENABLED = Route.addTo(GAMEPLAY_CONFIGURATION_HEADER, "mob-health-bar");
    public static final Route MOB_HEALTH_BAR_DISPLAY_DURATION = Route.addTo(GAMEPLAY_CONFIGURATION_HEADER, "health-bar-display-duration");
    public static final Route MCMMO_CONVERSION_EQUATION = Route.addTo(MCMMO_CONFIGURATION_HEADER, "conversion-equation");

}
