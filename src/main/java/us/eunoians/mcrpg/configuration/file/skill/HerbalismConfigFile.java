package us.eunoians.mcrpg.configuration.file.skill;

import dev.dejvokep.boostedyaml.route.Route;

import static com.diamonddagger590.mccore.util.Methods.toRoutePath;

/**
 * This class contains all the {@link Route}s for the {@link us.eunoians.mcrpg.skill.impl.herbalism.Herbalism} configuration file.
 */
public class HerbalismConfigFile extends SkillConfigFile {

    public static final String MATERIAL_MODIFIERS_HEADER = toRoutePath(EXPERIENCE_HEADER, "material-modifiers");
    public static final String BLOCK_EXPERIENCE_HEADER = toRoutePath(EXPERIENCE_HEADER, "sources");
    public static final Route ALLOWED_ITEMS_FOR_EXPERIENCE_GAIN = Route.fromString(toRoutePath(EXPERIENCE_HEADER, "allowed-items-for-experience-gain"));

    // Instant Irrigation
    private static final String INSTANT_IRRIGATION_HEADER = toRoutePath(ABILITY_CONFIGURATION_HEADER, "instant-irrigation");
    public static final Route INSTANT_IRRIGATION_ENABLED = Route.fromString(toRoutePath(INSTANT_IRRIGATION_HEADER, "enabled"));
    public static final Route INSTANT_IRRIGATION_COOLDOWN = Route.fromString(toRoutePath(INSTANT_IRRIGATION_HEADER, "cooldown"));

    // Verdant Surge
    private static final String VERDANT_SURGE_HEADER = toRoutePath(ABILITY_CONFIGURATION_HEADER, "verdant-surge");
    public static final Route VERDANT_SURGE_ENABLED = Route.fromString(toRoutePath(VERDANT_SURGE_HEADER, "enabled"));
    public static final Route VERDANT_SURGE_AMOUNT_OF_TIERS = Route.fromString(toRoutePath(VERDANT_SURGE_HEADER, "amount-of-tiers"));
    public static final Route VERDANT_SURGE_TIER_CONFIGURATION_HEADER = Route.fromString(toRoutePath(VERDANT_SURGE_HEADER, "tier-configuration"));

    // Mass Harvest
    private static final String MASS_HARVEST_HEADER = toRoutePath(ABILITY_CONFIGURATION_HEADER, "mass-harvest");
    public static final Route MASS_HARVEST_ENABLED = Route.fromString(toRoutePath(MASS_HARVEST_HEADER, "enabled"));
    public static final Route MASS_HARVEST_AMOUNT_OF_TIERS = Route.fromString(toRoutePath(MASS_HARVEST_HEADER, "amount-of-tiers"));
    public static final Route MASS_HARVEST_TIER_CONFIGURATION_HEADER = Route.fromString(toRoutePath(MASS_HARVEST_HEADER, "tier-configuration"));
    public static final Route MASS_HARVEST_VALID_BLOCKS = Route.fromString(toRoutePath(MASS_HARVEST_HEADER, "allowed-blocks"));

}
