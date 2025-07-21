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
 * The configuration file for the mining.yml configuration file.
 */
public class MiningConfigFile extends AbilityConfigFile {

    private static final int CURRENT_VERSION = 1;

    public static final Route SKILL_ENABLED = Route.fromString("skill-enabled");

    // Experience
    public static final String BLOCK_EXPERIENCE_HEADER = toRoutePath(EXPERIENCE_HEADER, "sources");

    // Extra Ore
    private static final String EXTRA_ORE_HEADER = toRoutePath(ABILITY_CONFIGURATION_HEADER, "extra-ore");
    public static final Route EXTRA_ORE_ENABLED = Route.fromString(toRoutePath(EXTRA_ORE_HEADER, "enabled"));
    public static final Route EXTRA_ORE_ACTIVATION_EQUATION = Route.fromString(toRoutePath(EXTRA_ORE_HEADER, "activation-equation"));
    public static final Route EXTRA_ORE_VALID_DROPS = Route.fromString(toRoutePath(EXTRA_ORE_HEADER, "valid-drops"));

    // It's A Triple
    private static final String ITS_A_TRIPLE_HEADER = toRoutePath(ABILITY_CONFIGURATION_HEADER, "its-a-triple");
    public static final Route ITS_A_TRIPLE_ENABLED = Route.fromString(toRoutePath(ITS_A_TRIPLE_HEADER, "enabled"));
    public static final Route ITS_A_TRIPLE_AMOUNT_OF_TIERS = Route.fromString(toRoutePath(ITS_A_TRIPLE_HEADER, "amount-of-tiers"));
    public static final Route ITS_A_TRIPLE_CONFIGURATION_HEADER = Route.fromString(toRoutePath(ITS_A_TRIPLE_HEADER, "tier-configuration"));

    // Remote Transfer
    private static final String REMOTE_TRANSFER_HEADER = toRoutePath(ABILITY_CONFIGURATION_HEADER, "remote-transfer");
    public static final Route REMOTE_TRANSFER_ENABLED = Route.fromString(toRoutePath(REMOTE_TRANSFER_HEADER, "enabled"));
    public static final Route REMOTE_TRANSFER_AMOUNT_OF_TIERS = Route.fromString(toRoutePath(REMOTE_TRANSFER_HEADER, "amount-of-tiers"));
    public static final Route REMOTE_TRANSFER_CONFIGURATION_HEADER = Route.fromString(toRoutePath(REMOTE_TRANSFER_HEADER, "tier-configuration"));

    // Remote Transfer allow list
    public static final String REMOTE_TRANSFER_CATEGORIES_HEADER = toRoutePath(REMOTE_TRANSFER_HEADER, "categories");
    public static final Route REMOTE_TRANSFER_ALLOW_LIST_NETHER = Route.fromString(toRoutePath(REMOTE_TRANSFER_CATEGORIES_HEADER, "nether"));
    public static final Route REMOTE_TRANSFER_ALLOW_LIST_ORES = Route.fromString(toRoutePath(REMOTE_TRANSFER_CATEGORIES_HEADER, "ores"));
    public static final Route REMOTE_TRANSFER_ALLOW_LIST_CAVES = Route.fromString(toRoutePath(REMOTE_TRANSFER_CATEGORIES_HEADER, "caves"));
    public static final Route REMOTE_TRANSFER_ALLOW_LIST_END = Route.fromString(toRoutePath(REMOTE_TRANSFER_CATEGORIES_HEADER, "end"));
    public static final Route REMOTE_TRANSFER_ALLOW_LIST_OCEAN = Route.fromString(toRoutePath(REMOTE_TRANSFER_CATEGORIES_HEADER, "ocean"));
    public static final Route REMOTE_TRANSFER_ALLOW_LIST_TERRACOTTA= Route.fromString(toRoutePath(REMOTE_TRANSFER_CATEGORIES_HEADER, "terracotta"));
    public static final Route REMOTE_TRANSFER_ALLOW_LIST_OVERWORLD = Route.fromString(toRoutePath(REMOTE_TRANSFER_CATEGORIES_HEADER, "overworld"));
    public static final Route REMOTE_TRANSFER_ALLOW_LIST_CUSTOM = Route.fromString(toRoutePath(REMOTE_TRANSFER_CATEGORIES_HEADER, "custom"));

    // Ore Scanner
    private static final String ORE_SCANNER_HEADER = toRoutePath(ABILITY_CONFIGURATION_HEADER, "ore-scanner");
    public static final Route ORE_SCANNER_ENABLED = Route.fromString(toRoutePath(ORE_SCANNER_HEADER, "enabled"));
    public static final Route ORE_SCANNER_BLOCK_TYPES = Route.fromString(toRoutePath(ORE_SCANNER_HEADER, "block-types"));
    public static final Route ORE_SCANNER_AMOUNT_OF_TIERS = Route.fromString(toRoutePath(ORE_SCANNER_HEADER, "amount-of-tiers"));
    public static final Route ORE_SCANNER_CONFIGURATION_HEADER = Route.fromString(toRoutePath(ORE_SCANNER_HEADER, "tier-configuration"));

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
            ignoredRouteSet.add(EXTRA_ORE_VALID_DROPS);
            // Add set to the map
            ignoredRoutes.put(String.valueOf(i), ignoredRouteSet);
        }
        return ignoredRoutes;
    }
}
