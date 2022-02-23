package us.eunoians.mcrpg.config;

import de.articdive.enum_to_yaml.interfaces.ConfigurationEnum;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

/**
 * The enum that is used to generate the main config.yml for McRPG
 *
 * @author DiamondDagger590
 */
public enum MainConfig implements ConfigurationEnum {

    //Header
    CONFIGURATION_HEADER("configuration", "", "##################################",
        "### Main Config For McRPG ###",
        "###################################"),

    //Databases
    DATABASE_DRIVER("database.driver", "SQLite",
        "#Sets the type of the database driver. The existing options are: 'SQLite' and 'H2'",
        "#If you wish to use H2, you must download the H2 driver from the following website: https://h2database.com/h2-2019-02-22.zip",
        "#Download and unzip this. You then will need to create a new folder called 'libs' under McRPG's plugin folder and upload the jar there",
        "#Once done, rename the jar to 'h2.jar' and McRPG will use that as the database driver."),

    //Miscellaneous Configuration
    DISABLED_WORLDS("configuration.disabled-worlds", Collections.singletonList("test"),
        "#What worlds should McRPG be disabled in"),
    SAVE_INTERVAL("configuration.save-interval", 1,
        "#This is how often the plugin saves player data (async) in minutes"),
    LANGUAGE_FILE("configuration.language-file", "en",
        "#What lang file you want to use. Do not include the .yml"),
    
    //Admin
    ABILITY_SPY_ENABLED("configuration.admin.enable-ability-spy", false,
        "#If enabled, admins will be alerted when abilities are unlocked and upgraded"),

    //McMMO
    MCMMO_CONVERSION_EQUATION("mcmmo.conversion-equation", "((skill_exp)*0.5)",
        "#Converts a players level to exp and then convert that into 'boosted experience' using the equation below",
        "#To configure how boosted exp works, please look at the 'boosted-exp' section"),

    //Exp modifications
    MAX_DAMAGE_CAP("configuration.experience.max-damage-cap-to-award-exp", 1000000,
        "#The maximum amount of damage allowed for giving experience"),
    EXP_MULTIPLIER_CAP("configuration.experience.exp-multiplier-limit", 3.0,
        "#The max amount of exp multiplier that a player should be able to get from various exp modifiers"),
    SHIELD_BLOCKING_MODIFIER("configuration.experience.shield-blocking-modifier", 0.5,
        "#How much the experience gain should be modified if the target is blocking with a shield.",
        "#This is mostly used to lower the ability of players to use shields to farm exp, while still awarding experience"),
    DISABLE_ENDER_PEARL_EXP("configuration.experience.disable-experience-from-ender-pearls", true,
        "#If experience gain should be disabled for damage from ender pearls"),
    BOOSTED_EXP_USAGE_RATE("configuration.experience.boosted-exp.usage-rate", "((gained_exp)*2.25)",
        "#When a player gains exp, this equation is factored in and if there is remaining boosted exp,",
        "#then it will add this equation value to the gained amount"),
    MODIFY_MOB_EXP_SECTION_HEADER("configuration.experience.modify-mob-spawn-experience", "",
        "#Modify the exp worth of mobs from spawners and eggs"),
    MODIFY_SPAWNER_MOB_EXP("configuration.experience.modify-mob-spawn-experience.spawner", 0.5,
        "#The multiplier to apply whenever a mob from spawners gives experience"),
    MODIFY_EGG_MOB_EXP("configuration.experience.modify-mob-spawn-experience.spawn-eggs", 0.5,
        "#The multiplier to apply whenever a mob from spawn eggs gives experience"),

    //Skill Books
    DISABLE_BOOKS_IN_END("configuration.skill-books.disable-books-in-end", true,
        "#If enabled, skill books will be unable to be dropped by blocks and mobs in the end"),

    //Exp display
    EXP_UPDATES_ENABLED("configuration.display.exp-updates.enabled", false,
        "#If players should be sent a display every time they gain experience"),
    EXP_UPDATES_DISPLAY_TYPE("configuration.display.exp-updates.display-type", "BOSSBAR",
        "#What type should the display be. Accepted values are: BOSSBAR, SCOREBOARD, or ACTIONBAR"),
    EXP_UPDATES_DISPLAY_DURATION("configuration.display.exp-updates.display-duration", 3,
        "#Duration of the reminder in seconds. Only used for scoreboard and bossbar"),

    //Gameplay
    DISABLE_TIPS("configuration.disable-tips", false,
        "#If true, then McRPG gameplay tips will not be sent to any players"),
    REPLACE_ABILITY_COOLDOWN("configuration.gameplay.replace-ability-cooldown", 1440,
        "#How long the cooldown for replacing an ability should be in minutes"),
    REQUIRE_EMPTY_OFF_HAND("configuration.gameplay-require-empty-off-hand-to-ready", false,
        "#If enabled, players will be required to have an empty off hand in order to ready their abilities"),
    USE_LEVEL_PERMS("configuration.gameplay.enable-levelup-permissions", false,
        "#If enabled, then a player will not be able to gain experience past a certain level.",
        "#An example is if we set a player to have the permission 'mcrpg.swords.500'. This player would gain no exp past level 500 in swords",
        "#Use mcrpg.%skill%.%level% as the perm"),
    MOB_HEALTH_BAR_ENABLED("configuration.gameplay.mob-health-bar.enabled", true,
        "#If mobs should display their current health above their head when attacked",
        "#The type of display is configured per player in their player settings"),
    MOB_HEALTH_BAR_DISPLAY_DURATION("configuration.gameplay.mob-health-bar.health-bar-display-duration", 5,
        "#How long should the health bars be displayed for in seconds"),
    ;

    private final String path;
    private final Object defaultValue;
    private final String[] comments;

    MainConfig(@NotNull String path, @NotNull Object defaultValue, @NotNull String... comments) {
        this.path = path;
        this.defaultValue = defaultValue;
        this.comments = comments;
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public String getPath() {
        return path;
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public String[] getComments() {
        return comments;
    }

}
