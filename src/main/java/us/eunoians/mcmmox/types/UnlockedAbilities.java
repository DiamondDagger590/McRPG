package us.eunoians.mcmmox.types;

import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;
import us.eunoians.mcmmox.Mcmmox;
import us.eunoians.mcmmox.configuration.enums.Config;

import java.io.File;
import java.util.Arrays;

/**
An enum of every ability that can be unlocked
 */
public enum UnlockedAbilities implements GenericAbility {
    BLEED_PLUS("Bleed+", "Swords", AbilityType.PASSIVE, Config.SWORDS_CONFIG),
    DEEPER_WOUND("DeeperWound", "Swords", AbilityType.PASSIVE, Config.SWORDS_CONFIG),
    VAMPIRE("Vampire", "Swords", AbilityType.PASSIVE, Config.SWORDS_CONFIG),
    RAGE_SPIKE("RageSpike", "Swords", AbilityType.ACTIVE, Config.SWORDS_CONFIG),
    SERRATED_STRIKES("SerratedStrikes", "Swords", AbilityType.ACTIVE, Config.SWORDS_CONFIG),
    TAINTED_BLADE("TaintedBlade", "Swords", AbilityType.ACTIVE, Config.SWORDS_CONFIG);

    @Getter private String name;
    @Getter private String skill;
    @Getter private AbilityType abilityType;
    private Config configType;

    /**
     *
     * @param name
     * @param skill
     * @param type
     * @param config
     */
    UnlockedAbilities(String name, String skill, AbilityType type, Config config) {
        this.name = name;
        this.skill = skill;
        this.abilityType = type;
        this.configType = config;
    }

    /**
     * Check if the ability is passive or not
     * @return true if the ability if passive and false if it is active
     */
    public boolean isPassiveAbility() { return abilityType.equals(AbilityType.PASSIVE); }

    /**
     * Check if the ability is enabled or disabled
     * @return ture if the ability is enabled or false if the ability is disabled
     */
    @Override
    public boolean isEnabled() {
        return YamlConfiguration.loadConfiguration(new File(Mcmmox.getInstance().getDataFolder(),
                File.separator + "Skills" + File.separator + this.skill)).getBoolean("EnabledAbilities." + name);
    }

    /**
     *
     * @param ability The name of the ability you want the instance of
     * @return The instance of the unlocked ability if a correct name is provided or null if the ability provided does not exist
     */
    public static UnlockedAbilities fromString(String ability){
        return Arrays.stream(UnlockedAbilities.values()).filter(a -> a.getName().equalsIgnoreCase(ability)).findAny().orElse(null);
    }

    /**
     * Get the level an ability is unlocked
     * @return The integer representation of when the ability is unlocked
     */
    public int getUnlockLevel() {
        return YamlConfiguration.loadConfiguration(new File(Mcmmox.getInstance().getDataFolder(),
                File.separator + "Skills" + File.separator + skill)).getInt("UnlockLevelForAbility." + name);
    }

    /**
     * Check if the ability should use the permission ability.use.* or ability.use.{ability_name}
     * @return true if the permission system is meant to be used for this ability or false if not
     */
    public boolean usePerm() {
        return YamlConfiguration.loadConfiguration(new File(Mcmmox.getInstance().getDataFolder(),
                File.separator + "Skills" + File.separator + skill)).getBoolean("UsePermsForAbility." + name);
    }

    /**
     * Get the max tier an ability can reach. This should not exceed 5 but the limit is not hardcoded
     * @return The max tier an ability can reach
     */
    public int getMaxTier() {
        return YamlConfiguration.loadConfiguration(new File(Mcmmox.getInstance().getDataFolder(),
                File.separator + "Skills" + File.separator + skill)).getInt(name + "Config.TierAmount");
    }

    /**
     * Check what level a skill should be to unlock the provided tier
     * @param tier The tier you want to check
     * @return The level that the tier is unlocked or -1 if that tier doesnt exist
     */
    public int tierUnlockLevel(int tier) {
        return -1;
    }

}
