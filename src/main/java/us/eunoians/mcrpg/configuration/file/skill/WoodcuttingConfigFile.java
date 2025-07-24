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
 * The configuration file for {@link us.eunoians.mcrpg.skill.impl.woodcutting.Woodcutting}.
 */
public class WoodcuttingConfigFile extends SkillConfigFile {

    private static final int CURRENT_VERSION = 1;

    public static final Route SKILL_ENABLED = Route.fromString("skill-enabled");

    public static final String BLOCK_EXPERIENCE_HEADER = toRoutePath(EXPERIENCE_HEADER, "sources");

    // Extra Lumber
    private static final String EXTRA_LUMBER_HEADER = toRoutePath(ABILITY_CONFIGURATION_HEADER, "extra-lumber");
    public static final Route EXTRA_LUMBER_ENABLED = Route.fromString(toRoutePath(EXTRA_LUMBER_HEADER, "enabled"));
    public static final Route EXTRA_LUMBER_ACTIVATION_EQUATION = Route.fromString(toRoutePath(EXTRA_LUMBER_HEADER, "activation-equation"));
    public static final Route EXTRA_LUMBER_VALID_DROPS = Route.fromString(toRoutePath(EXTRA_LUMBER_HEADER, "valid-drops"));

    // Heavy Swing
    private static final String HEAVY_SWING_HEADER = toRoutePath(ABILITY_CONFIGURATION_HEADER, "heavy-swing");
    public static final Route HEAVY_SWING_ENABLED = Route.fromString(toRoutePath(HEAVY_SWING_HEADER, "enabled"));
    public static final Route HEAVY_SWING_AMOUNT_OF_TIERS = Route.fromString(toRoutePath(HEAVY_SWING_HEADER, "amount-of-tiers"));
    public static final Route HEAVY_SWING_VALID_BLOCKS = Route.fromString(toRoutePath(HEAVY_SWING_HEADER, "valid-blocks"));
    public static final Route HEAVY_SWING_CONFIGURATION_HEADER = Route.fromString(toRoutePath(HEAVY_SWING_HEADER, "tier-configuration"));

    // Dryads Gift
    private static final String DRYADS_GIFT_HEADER = toRoutePath(ABILITY_CONFIGURATION_HEADER, "dryads-gift");
    public static final Route DRYADS_GIFT_ENABLED = Route.fromString(toRoutePath(DRYADS_GIFT_HEADER, "enabled"));
    public static final Route DRYADS_GIFT_AMOUNT_OF_TIERS = Route.fromString(toRoutePath(DRYADS_GIFT_HEADER, "amount-of-tiers"));
    public static final Route DRYADS_GIFT_VALID_BLOCKS = Route.fromString(toRoutePath(DRYADS_GIFT_HEADER, "valid-blocks"));
    public static final Route DRYADS_GIFT_CONFIGURATION_HEADER = Route.fromString(toRoutePath(DRYADS_GIFT_HEADER, "tier-configuration"));

    // Nymphs Vitality
    private static final String NYMPHS_VITALITY_HEADER = toRoutePath(ABILITY_CONFIGURATION_HEADER, "nymphs-vitality");
    public static final Route NYMPHS_VITALITY_ENABLED = Route.fromString(toRoutePath(NYMPHS_VITALITY_HEADER, "enabled"));
    public static final Route NYMPHS_VITALITY_AMOUNT_OF_TIERS = Route.fromString(toRoutePath(NYMPHS_VITALITY_HEADER, "amount-of-tiers"));
    public static final Route NYMPHS_VITALITY_VALID_BIOMES = Route.fromString(toRoutePath(NYMPHS_VITALITY_HEADER, "valid-biomes"));
    public static final Route NYMPHS_VITALITY_CONFIGURATION_HEADER = Route.fromString(toRoutePath(NYMPHS_VITALITY_HEADER, "tier-configuration"));

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
            ignoredRouteSet.add(EXTRA_LUMBER_VALID_DROPS);
            // Add set to the map
            ignoredRoutes.put(String.valueOf(i), ignoredRouteSet);
        }
        return ignoredRoutes;
    }
}
