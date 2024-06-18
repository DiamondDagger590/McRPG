package us.eunoians.mcrpg.configuration.file;

import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.route.Route;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SwordsConfigFile extends ConfigFile {

    private static final int CURRENT_VERSION = 1;

    // Main Headers
    private static final Route PERMISSIONS_HEADER = Route.fromString("permissions");
    private static final Route LEVELING_HEADER = Route.fromString("leveling");
    private static final Route EXPERIENCE_HEADER = Route.fromString("experience");
    private static final Route ABILITY_CONFIGURATION_HEADER = Route.fromString("ability-configuration");

    public static final Route SKILL_ENABLED = Route.fromString("skill-enabled");

    // Permissions
    public static final Route RESTRICT_SKILL_TO_PERMISSIONS = Route.addTo(PERMISSIONS_HEADER, "restrict-skill-to-permissions");
    public static final Route USE_PERMISSIONS_TO_UNLOCK_ABILITIES = Route.addTo(PERMISSIONS_HEADER, "use-permissions-to-unlock-abilities");
    public static final Route USE_PERMISSIONS_TO_ACTIVATE_ABILITIES = Route.addTo(PERMISSIONS_HEADER, "use-permissions-to-activate-abilities");

    // Leveling
    public static final Route LEVEL_UP_EQUATION = Route.addTo(LEVELING_HEADER, "level-up-equation");
    public static final Route MAXIMUM_SKILL_LEVEL = Route.addTo(LEVELING_HEADER, "maximum-skill-level");

    // Experience
    public static final Route MATERIAL_MODIFIERS = Route.addTo(EXPERIENCE_HEADER, "material-modifiers");

    // Bleed
    private static final Route BLEED_HEADER = Route.addTo(ABILITY_CONFIGURATION_HEADER, "bleed");
    public static final Route BLEED_ACTIVATION_EQUATION = Route.addTo(BLEED_HEADER, "activation-equation");
    public static final Route BLEED_MINIMUM_HEALTH_ALLOWED = Route.addTo(BLEED_HEADER, "minimum-health-allowed");
    public static final Route BLEED_BASE_DURATION = Route.addTo(BLEED_HEADER, "bleed-base-duration");
    public static final Route BLEED_BASE_FREQUENCY = Route.addTo(BLEED_HEADER, "bleed-base-frequency");
    public static final Route BLEED_BASE_DAMAGE = Route.addTo(BLEED_HEADER, "bleed-base-damage");
    public static final Route BLEED_GRANT_IMMUNITY_AFTER_EXPIRE = Route.addTo(BLEED_HEADER, "grant-bleed-immunity-after-expire");
    public static final Route BLEED_IMMUNITY_DURATION = Route.addTo(BLEED_HEADER, "bleed-immunity-duration");
    public static final Route BLEED_DAMAGE_PIERCE_ARMOR = Route.addTo(BLEED_HEADER, "bleed-damage-pierce-armor");

    // Poisoned Bleed
    private static final Route POISONED_BLEED_HEADER = Route.addTo(ABILITY_CONFIGURATION_HEADER, "poisoned-bleed");
    public static final Route POISONED_BLEED_AMOUNT_OF_TIERS = Route.addTo(POISONED_BLEED_HEADER, "amount-of-tiers");
    public static final Route POISONED_BLEED_TIER_UNLOCK_LEVELS = Route.addTo(POISONED_BLEED_HEADER, "tier-unlock-levels");
    public static final Route POISONED_BLEED_TIER_CONFIGURATION = Route.addTo(POISONED_BLEED_HEADER, "tier-configuration");


    @Override
    public UpdaterSettings getUpdaterSettings() {
        return UpdaterSettings.builder().setVersioning(new BasicVersioning("config-version")).addIgnoredRoutes(getIgnoredRoutes()).build();

    }

    private Map<String, Set<Route>> getIgnoredRoutes() {
        Map<String, Set<Route>> ignoredRoutes = new HashMap<>();
        for (int i = 1; i <= CURRENT_VERSION; i++) {
            Set<Route> ignoredRouteSet = new HashSet<>();
            // Add routes that have custom sections to all versions
            ignoredRouteSet.add(MATERIAL_MODIFIERS);
            ignoredRouteSet.add(POISONED_BLEED_TIER_UNLOCK_LEVELS);
            ignoredRouteSet.add(POISONED_BLEED_TIER_CONFIGURATION);
            // Add set to the map
            ignoredRoutes.put(String.valueOf(i), ignoredRouteSet);
        }
        return ignoredRoutes;
    }
}
