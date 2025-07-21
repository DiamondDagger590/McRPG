package us.eunoians.mcrpg.configuration.file.skill;

import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.route.Route;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.diamonddagger590.mccore.util.Methods.toRoutePath;

/**
 * Contains all the {@link Route}s used for the swords_configuration.yml
 */
public final class SwordsConfigFile extends AbilityConfigFile {

    private static final int CURRENT_VERSION = 1;

    public static final Route SKILL_ENABLED = Route.fromString("skill-enabled");
    // Experience
    public static final String MATERIAL_MODIFIERS_HEADER = toRoutePath(EXPERIENCE_HEADER, "material-modifiers");
    public static final String ENTITY_EXPERIENCE_HEADER = toRoutePath(EXPERIENCE_HEADER, "sources");

    // Bleed
    private static final String BLEED_HEADER = toRoutePath(ABILITY_CONFIGURATION_HEADER, "bleed");
    public static final Route BLEED_ENABLED = Route.fromString(toRoutePath(BLEED_HEADER, "enabled"));
    public static final Route BLEED_ACTIVATION_EQUATION = Route.fromString(toRoutePath(BLEED_HEADER, "activation-equation"));
    public static final Route BLEED_MINIMUM_HEALTH_ALLOWED = Route.fromString(toRoutePath(BLEED_HEADER, "minimum-health-allowed"));
    public static final Route BLEED_BASE_CYCLES = Route.fromString(toRoutePath(BLEED_HEADER, "bleed-base-cycles"));
    public static final Route BLEED_BASE_FREQUENCY = Route.fromString(toRoutePath(BLEED_HEADER, "bleed-base-frequency"));
    public static final Route BLEED_BASE_DAMAGE = Route.fromString(toRoutePath(BLEED_HEADER, "bleed-base-damage"));
    public static final Route BLEED_GRANT_IMMUNITY_AFTER_EXPIRE = Route.fromString(toRoutePath(BLEED_HEADER, "grant-bleed-immunity-after-expire"));
    public static final Route BLEED_IMMUNITY_DURATION = Route.fromString(toRoutePath(BLEED_HEADER, "bleed-immunity-duration"));
    public static final Route BLEED_DAMAGE_PIERCE_ARMOR = Route.fromString(toRoutePath(BLEED_HEADER, "bleed-damage-pierce-armor"));

    // Enhanced Bleed
    private static final String ENHANCED_BLEED_HEADER = toRoutePath(ABILITY_CONFIGURATION_HEADER, "enhanced-bleed");
    public static final Route ENHANCED_BLEED_ENABLED = Route.fromString(toRoutePath(ENHANCED_BLEED_HEADER, "enabled"));
    public static final Route ENHANCED_BLEED_AMOUNT_OF_TIERS = Route.fromString(toRoutePath(ENHANCED_BLEED_HEADER, "amount-of-tiers"));
    public static final Route ENHANCED_BLEED_TIER_CONFIGURATION_HEADER = Route.fromString(toRoutePath(ENHANCED_BLEED_HEADER, "tier-configuration"));

    // Deeper Wound
    private static final String DEEPER_WOUND_HEADER = toRoutePath(ABILITY_CONFIGURATION_HEADER, "deeper-wound");
    public static final Route DEEPER_WOUND_ENABLED = Route.fromString(toRoutePath(DEEPER_WOUND_HEADER, "enabled"));
    public static final Route DEEPER_WOUND_AMOUNT_OF_TIERS = Route.fromString(toRoutePath(DEEPER_WOUND_HEADER, "amount-of-tiers"));
    public static final Route DEEPER_WOUND_TIER_CONFIGURATION_HEADER = Route.fromString(toRoutePath(DEEPER_WOUND_HEADER, "tier-configuration"));

    // Vampire
    private static final String VAMPIRE_HEADER = toRoutePath(ABILITY_CONFIGURATION_HEADER, "vampire");
    public static final Route VAMPIRE_ENABLED = Route.fromString(toRoutePath(VAMPIRE_HEADER, "enabled"));
    public static final Route VAMPIRE_AMOUNT_OF_TIERS = Route.fromString(toRoutePath(VAMPIRE_HEADER, "amount-of-tiers"));
    public static final Route VAMPIRE_TIER_CONFIGURATION_HEADER = Route.fromString(toRoutePath(VAMPIRE_HEADER, "tier-configuration"));

    // Rage Spike
    private static final String SERRATED_STRIKES_HEADER = toRoutePath(ABILITY_CONFIGURATION_HEADER, "serrated-strikes");
    public static final Route SERRATED_STRIKES_ENABLED = Route.fromString(toRoutePath(SERRATED_STRIKES_HEADER, "enabled"));
    public static final Route SERRATED_STRIKES_AMOUNT_OF_TIERS = Route.fromString(toRoutePath(SERRATED_STRIKES_HEADER, "amount-of-tiers"));
    public static final Route SERRATED_STRIKES_CONFIGURATION_HEADER = Route.fromString(toRoutePath(SERRATED_STRIKES_HEADER, "tier-configuration"));

    // Rage Spike
    private static final String RAGE_SPIKE_HEADER = toRoutePath(ABILITY_CONFIGURATION_HEADER, "rage-spike");
    public static final Route RAGE_SPIKE_ENABLED = Route.fromString(toRoutePath(RAGE_SPIKE_HEADER, "enabled"));
    public static final Route RAGE_SPIKE_AMOUNT_OF_TIERS = Route.fromString(toRoutePath(RAGE_SPIKE_HEADER, "amount-of-tiers"));
    public static final Route RAGE_SPIKE_CONFIGURATION_HEADER = Route.fromString(toRoutePath(RAGE_SPIKE_HEADER, "tier-configuration"));


    @NotNull
    @Override
    public UpdaterSettings getUpdaterSettings() {
        return UpdaterSettings.builder().setVersioning(new BasicVersioning("config-version")).addIgnoredRoutes(getIgnoredRoutes()).build();
    }

    @NotNull
    private Map<String, Set<Route>> getIgnoredRoutes() {
        Map<String, Set<Route>> ignoredRoutes = new HashMap<>();
        for (int i = 1; i <= CURRENT_VERSION; i++) {
            Set<Route> ignoredRouteSet = new HashSet<>();
            // Add routes that have custom sections to all versions
            ignoredRouteSet.add(Route.fromString(MATERIAL_MODIFIERS_HEADER));
            ignoredRouteSet.add(ENHANCED_BLEED_TIER_CONFIGURATION_HEADER);
            // Add set to the map
            ignoredRoutes.put(String.valueOf(i), ignoredRouteSet);
        }
        return ignoredRoutes;
    }
}
