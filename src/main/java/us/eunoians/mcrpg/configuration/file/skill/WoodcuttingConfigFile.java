package us.eunoians.mcrpg.configuration.file.skill;

import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.route.Route;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The configuration file for {@link us.eunoians.mcrpg.skill.impl.woodcutting.Woodcutting}.
 */
public class WoodcuttingConfigFile extends AbilityConfigFile {

    private static final int CURRENT_VERSION = 1;

    public static final Route BLOCK_EXPERIENCE_HEADER = Route.addTo(EXPERIENCE_HEADER, "sources");

    // Extra Lumber
    private static final Route EXTRA_LUMBER_HEADER = Route.addTo(ABILITY_CONFIGURATION_HEADER, "extra-ore");
    public static final Route EXTRA_LUMBER_ENABLED = Route.addTo(EXTRA_LUMBER_HEADER, "enabled");
    public static final Route EXTRA_LUMBER_ACTIVATION_EQUATION = Route.addTo(EXTRA_LUMBER_HEADER, "activation-equation");
    public static final Route EXTRA_LUMBER_VALID_DROPS = Route.addTo(EXTRA_LUMBER_HEADER, "valid-drops");

    // Heavy Swing
    private static final Route HEAVY_SWING_HEADER = Route.addTo(ABILITY_CONFIGURATION_HEADER, "heavy-swing");
    public static final Route HEAVY_SWING_ENABLED = Route.addTo(HEAVY_SWING_HEADER, "enabled");
    public static final Route HEAVY_SWING_AMOUNT_OF_TIERS = Route.addTo(HEAVY_SWING_HEADER, "amount-of-tiers");
    public static final Route HEAVY_SWING_VALID_BLOCKS = Route.addTo(HEAVY_SWING_HEADER, "valid-blocks");
    public static final Route HEAVY_SWING_CONFIGURATION_HEADER = Route.addTo(HEAVY_SWING_HEADER, "tier-configuration");

    // Dryads Gift
    private static final Route DRYADS_GIFT_HEADER = Route.addTo(ABILITY_CONFIGURATION_HEADER, "dryads-gift");
    public static final Route DRYADS_GIFT_ENABLED = Route.addTo(DRYADS_GIFT_HEADER, "enabled");
    public static final Route DRYADS_GIFT_AMOUNT_OF_TIERS = Route.addTo(DRYADS_GIFT_HEADER, "amount-of-tiers");
    public static final Route DRYADS_GIFT_VALID_BLOCKS = Route.addTo(DRYADS_GIFT_HEADER, "valid-blocks");
    public static final Route DRYADS_GIFT_CONFIGURATION_HEADER = Route.addTo(DRYADS_GIFT_HEADER, "tier-configuration");

    // Nymphs Vitality
    private static final Route NYMPHS_VITALITY_HEADER = Route.addTo(ABILITY_CONFIGURATION_HEADER, "nymphs-vitality");
    public static final Route NYMPHS_VITALITY_ENABLED = Route.addTo(NYMPHS_VITALITY_HEADER, "enabled");
    public static final Route NYMPHS_VITALITY_AMOUNT_OF_TIERS = Route.addTo(NYMPHS_VITALITY_HEADER, "amount-of-tiers");
    public static final Route NYMPHS_VITALITY_VALID_BIOMES = Route.addTo(NYMPHS_VITALITY_HEADER, "valid-biomes");
    public static final Route NYMPHS_VITALITY_CONFIGURATION_HEADER = Route.addTo(NYMPHS_VITALITY_HEADER, "tier-configuration");

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
            ignoredRouteSet.add(EXTRA_LUMBER_VALID_DROPS);
            // Add set to the map
            ignoredRoutes.put(String.valueOf(i), ignoredRouteSet);
        }
        return ignoredRoutes;
    }
}
