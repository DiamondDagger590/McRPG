package us.eunoians.mcrpg.configuration.file;

import ch.jalu.configme.Comment;
import ch.jalu.configme.SettingsHolder;
import ch.jalu.configme.configurationdata.CommentsConfiguration;
import ch.jalu.configme.properties.Property;
import ch.jalu.configme.properties.PropertyInitializer;
import us.eunoians.mcrpg.display.DisplayType;

import java.util.Arrays;
import java.util.List;

/**
 * The main configuration file for McRPG
 */
public class MainConfigFile implements SettingsHolder {

    private static final String CONFIGURATION_HEADER = "configuration";
    private static final String ADMIN_CONFIGURATION_HEADER = CONFIGURATION_HEADER + ".admin";
    private static final String EXPERIENCE_CONFIGURATION_HEADER = CONFIGURATION_HEADER + ".experience";
    private static final String BOOSTED_EXPERIENCE_CONFIGURATION_HEADER = EXPERIENCE_CONFIGURATION_HEADER + ".boosted-experience";
    private static final String MODIFY_MOB_SPAWN_EXPERIENCE_CONFIGURATION_HEADER = EXPERIENCE_CONFIGURATION_HEADER + ".modify-mob-spawn-experience";
    private static final String SKILL_BOOKS_CONFIGURATION_HEADER = CONFIGURATION_HEADER + ".skill-books";
    private static final String DISPLAY_CONFIGURATION_HEADER = CONFIGURATION_HEADER + ".display";
    private static final String DISPLAY_EXPERIENCE_CONFIGURATION_HEADER = DISPLAY_CONFIGURATION_HEADER + ".exp-updates";
    private static final String GAMEPLAY_CONFIGURATION_HEADER = CONFIGURATION_HEADER + ".gameplay";
    private static final String MOB_HEALTH_BAR_CONFIGURATION_HEADER = GAMEPLAY_CONFIGURATION_HEADER + ".mob-health-bar";
    private static final String MCMMO_CONFIGURATION_HEADER = CONFIGURATION_HEADER + ".mcmmo";

    @Comment({
            "Sets the type of the database driver. The existing options are: 'SQLite' and 'H2' as of version 2.0.0.0. Please check the latest version for any new options.",
            "",
            "If you wish to use H2, you must download the H2 driver from the following website: https://h2database.com/h2-2019-02-22.zip",
            "Download and unzip this. You then will need to create a new folder called 'libs' under McRPG's plugin folder and upload the jar there",
            "Once done, rename the jar to 'h2.jar' and McRPG will use that as the database driver."
    })
    public static final Property<String> DATABASE_DRIVER = PropertyInitializer.newProperty(CONFIGURATION_HEADER + ".database.driver", "SQLite");

    @Comment("What worlds should McRPG be disabled in")
    public static final Property<List<String>> DISABLED_WORLD = PropertyInitializer.newListProperty(CONFIGURATION_HEADER + ".disabled-worlds", Arrays.asList("test"));

    @Comment("This is how often the plugin saves player data (async) in minutes")
    public static final Property<Integer> SAVE_INTERVAL = PropertyInitializer.newProperty(CONFIGURATION_HEADER + ".save-interval", 1);

    @Comment("What language file you want to use. Do not include the .yml")
    public static final Property<String> LANGUAGE_FILE = PropertyInitializer.newProperty(CONFIGURATION_HEADER + ".language-file", "en");

    @Comment("If set to true, then players will not receive McRPG gameplay tips")
    public static final Property<Boolean> DISABLE_TIPS = PropertyInitializer.newProperty(CONFIGURATION_HEADER + ".disable-tips", false);

    @Comment("If enabled, admins with the permission node 'mcrpg.ability.spy' will be alerted when abilities are unlocked and upgraded")
    public static final Property<Boolean> ENABLE_ABILITY_SPY = PropertyInitializer.newProperty(ADMIN_CONFIGURATION_HEADER + ".enable-ability-spy", false);

    @Comment("The maximum amount of damage allowed for giving experience. Any damage above this threshold will be ignored when calculating exp to award.")
    public static final Property<Integer> MAX_DAMAGE_CAP_TO_AWARD_EXPERIENCE = PropertyInitializer.newProperty(EXPERIENCE_CONFIGURATION_HEADER + ".max-damage-cap-to-award-exp", 1000000);

    @Comment("The maximum amount of exp multiplier that a player should be able to get from various exp modifiers when combined.")
    public static final Property<Double> EXPERIENCE_MULTIPLIER_LIMIT = PropertyInitializer.newProperty(EXPERIENCE_CONFIGURATION_HEADER + ".exp-multiplier-limit", 3.0);

    @Comment("How much the experience gain should be modified if the target is blocking with a shield.")
    public static final Property<Double> SHIELD_BLOCKING_MODIFIER = PropertyInitializer.newProperty(EXPERIENCE_CONFIGURATION_HEADER + ".shield-blocking-modifier", 0.5);

    @Comment("Allows disabling of experience gain when taking damage from ender pearls.")
    public static final Property<Boolean> DISABLE_EXPERIENCE_FROM_ENDER_PEARLS = PropertyInitializer.newProperty(EXPERIENCE_CONFIGURATION_HEADER + ".disable-experience-from-ender-pearls", true);

    @Comment({
            "When a player gains exp, this equation is factored in and if there is remaining boosted exp,",
            "then it will add this equation value to the gained amount"
    })
    public static final Property<String> BOOSTED_EXPERIENCE_USAGE_RATE = PropertyInitializer.newProperty(BOOSTED_EXPERIENCE_CONFIGURATION_HEADER + ".usage-rate", "((gained_exp)*2.25)");

    @Comment("The multiplier to apply whenever a mob from spawners gives experience")
    public static final Property<Double> MODIFY_MOB_SPAWN_EXPERIENCE_FROM_SPAWNER = PropertyInitializer.newProperty(MODIFY_MOB_SPAWN_EXPERIENCE_CONFIGURATION_HEADER + ".spawner", 0.5);

    @Comment("The multiplier to apply whenever a mob from spawn eggs gives experience")
    public static final Property<Double> MODIFY_MOB_SPAWN_EXPERIENCE_FROM_SPAWN_EGG = PropertyInitializer.newProperty(MODIFY_MOB_SPAWN_EXPERIENCE_CONFIGURATION_HEADER + ".spawn-eggs", 0.5);

    @Comment({
            "If when players redeem levels, should this reset the amount of exp needed to level up.",
            "Ex) Player has gained 5,000 experience in Swords and uses a redeemable level. If this is enabled,",
            "then the experience gained will be reset to 0 whenever the level is added. Otherwise, the 5,000 is kept."
    })
    public static final Property<Boolean> RESET_EXPERIENCE_UPON_REDEEMED_LEVELS = PropertyInitializer.newProperty(EXPERIENCE_CONFIGURATION_HEADER + ".reset-exp-upon-redeemed-levels", true);

    @Comment("If set to true, then skill books will be unable to drop in the end")
    public static final Property<Boolean> DISABLE_SKILL_BOOKS_IN_END = PropertyInitializer.newProperty(SKILL_BOOKS_CONFIGURATION_HEADER + ".disable-books-in-end", true);

    @Comment("If set to true, players will get a visual update whenever experience is gained (unless their setting disables this)")
    public static final Property<Boolean> EXP_UPDATES_ENABLED = PropertyInitializer.newProperty(DISPLAY_EXPERIENCE_CONFIGURATION_HEADER + ".enabled", true);

    @Comment("What type should the display be. Accepted values are: BOSSBAR, SCOREBOARD, or ACTIONBAR")
    public static final Property<DisplayType> EXP_UPDATE_DISPLAY_TYPE = PropertyInitializer.newProperty(DisplayType.class, DISPLAY_EXPERIENCE_CONFIGURATION_HEADER + ".display-type", DisplayType.BOSSBAR);

    @Comment("Duration of the reminder in seconds. Only used for scoreboard and bossbar")
    public static final Property<Integer> EXP_UPDATE_DISPLAY_DURATION = PropertyInitializer.newProperty(DISPLAY_EXPERIENCE_CONFIGURATION_HEADER + ".display-duration", 3);

    @Comment("How long the cooldown for replacing an ability should be in minutes")
    public static final Property<Integer> REPLACE_ABILITY_COOLDOWN_TIME = PropertyInitializer.newProperty(GAMEPLAY_CONFIGURATION_HEADER + ".replace-ability-cooldown", 1440);

    @Comment("If enabled, players will be required to have an empty off hand in order to ready their abilities")
    public static final Property<Boolean> REQUIRE_EMPTY_OFF_HAND_TO_READY = PropertyInitializer.newProperty(GAMEPLAY_CONFIGURATION_HEADER + ".require-empty-off-hand-to-ready", false);

    @Comment({
            "If enabled, then a player will not be able to gain experience past a certain level.",
            "An example is if we set a player to have the permission 'mcrpg.swords.500'. This player would gain no exp past level 500 in swords",
            "Use mcrpg.%skill%.%level% as the perm"
    })
    public static final Property<Boolean> ENABLE_LEVEL_UP_PERMISSIONS = PropertyInitializer.newProperty(GAMEPLAY_CONFIGURATION_HEADER + ".enable-level-up-permissions", false);

    @Comment({
            "If set to true, mobs will have a healthbar displaying their current health over their heads",
            "The type of display is configured per player in their player settings"
    })
    public static final Property<Boolean> MOB_HEALTH_BAR_ENABLED = PropertyInitializer.newProperty(GAMEPLAY_CONFIGURATION_HEADER + ".mob-health-bar", true);

    @Comment("How long should the health bars be displayed for in seconds")
    public static final Property<Integer> HEALTH_BAR_DISPLAY_DURATION = PropertyInitializer.newProperty(GAMEPLAY_CONFIGURATION_HEADER + ".health-bar-display-duration", 5);

    @Comment({
            "Converts a players level to exp and then convert that into 'boosted experience' using the equation below",
            "To configure how boosted exp works, please look at the 'boosted-exp' section"
    })
    public static final Property<String> MCMMO_CONVERSION_EQUATION = PropertyInitializer.newProperty(MCMMO_CONFIGURATION_HEADER + ".conversion_equation", "((skill_exp)*0.5)");

    @Override
    public void registerComments(CommentsConfiguration commentsConfiguration) {
        String[] configurationHeader = {
                "##################################",
                "### Main Config For McRPG ###",
                "##################################",
                ""
        };
        commentsConfiguration.setComment(CONFIGURATION_HEADER, configurationHeader);

        String[] adminHeader = {"Configure all admin features"};
        commentsConfiguration.setComment(ADMIN_CONFIGURATION_HEADER, adminHeader);

        String[] experienceHeader = {"Modify various features for global skill experience"};
        commentsConfiguration.setComment(EXPERIENCE_CONFIGURATION_HEADER, experienceHeader);

        String[] boostedExperienceHeader = {"Configure boosted experience from McMMO conversion"};
        commentsConfiguration.setComment(BOOSTED_EXPERIENCE_CONFIGURATION_HEADER, boostedExperienceHeader);

        String[] modifyMobSpawnExperienceHeader = {"Modify the exp worth of mobs from spawners and eggs"};
        commentsConfiguration.setComment(MODIFY_MOB_SPAWN_EXPERIENCE_CONFIGURATION_HEADER, modifyMobSpawnExperienceHeader);

        String[] skillBooksHeader = {"Configure skill books"};
        commentsConfiguration.setComment(SKILL_BOOKS_CONFIGURATION_HEADER, skillBooksHeader);

        String[] displayHeader = {"Configure various displays (scoreboard, actionbar, and bossbar) for McRPG"};
        commentsConfiguration.setComment(DISPLAY_CONFIGURATION_HEADER, displayHeader);

        String[] displayExpUpdatesHeader = {"Configure displays for whenever experience is gained"};
        commentsConfiguration.setComment(DISPLAY_EXPERIENCE_CONFIGURATION_HEADER, displayExpUpdatesHeader);

        String[] gameplayHeader = {"Configure various gameplay mechanics"};
        commentsConfiguration.setComment(GAMEPLAY_CONFIGURATION_HEADER, gameplayHeader);

        String[] mobHealthBarHeader = {"Configure the health bars that appear above mobs when they are attacked"};
        commentsConfiguration.setComment(MOB_HEALTH_BAR_CONFIGURATION_HEADER, mobHealthBarHeader);

        String[] mcmmoHeader = {"Configure McMMO integration"};
        commentsConfiguration.setComment(MCMMO_CONFIGURATION_HEADER, mcmmoHeader);
    }
}
