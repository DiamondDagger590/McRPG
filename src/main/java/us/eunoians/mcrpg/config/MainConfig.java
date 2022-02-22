package us.eunoians.mcrpg.config;

import de.articdive.enum_to_yaml.interfaces.ConfigurationEnum;
import org.jetbrains.annotations.NotNull;

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

    //McMMO
    MCMMO_CONVERSION_EQUATION("mcmmo.conversion-equation", "((skill_exp)*0.5)",
        "#Converts a players level to exp and then convert that into 'boosted experience' using the equation below",
        "#To configure how boosted exp works, please look at the 'boosted-exp' section"),
    BOOSTED_EXP_USAGE_RATE("boosted-exp.usage-rate", "((gained_exp)*2.25)",
        "#When a player gains exp, this equation is factored in and if there is remaining boosted exp,",
        "#then it will add this equation value to the gained amount"),

    //General Configuration
    DISABLE_TIPS("configuration.disable-tips", false,
        "#If true, then McRPG gameplay tips will not be sent to any players"),

    //Exp modifications
    MAX_DAMAGE_CAP("configuration.experience.max-damage-cap-to-award-exp", 1000000,
        "#The maximum amount of damage allowed for giving experience"),
    EXP_MULTIPLIER_CAP("configuration.experience.exp-multiplier-limit", 3.0,
        "#The max amount of exp multiplier that a player should be able to get from various exp modifiers"),
    SHIELD_BLOCKING_MODIFIER("configuration.ShieldBlockingModifier", 0.5, "#How much exp gain should be modified if the target is blocking with a shield."),
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
