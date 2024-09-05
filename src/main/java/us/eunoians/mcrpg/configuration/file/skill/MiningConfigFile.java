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
 * The configuration file for the mining.yml configuration file.
 */
public class MiningConfigFile extends AbilityConfigFile {

    private static final int CURRENT_VERSION = 1;

    // Experience
    public static final Route BLOCK_EXPERIENCE_HEADER = Route.addTo(EXPERIENCE_HEADER, "sources");

    // Extra Ore
    private static final Route EXTRA_ORE_HEADER = Route.addTo(ABILITY_CONFIGURATION_HEADER, "extra-ore");
    public static final Route EXTRA_ORE_ENABLED = Route.addTo(EXTRA_ORE_HEADER, "enabled");
    public static final Route EXTRA_ORE_ACTIVATION_EQUATION = Route.addTo(EXTRA_ORE_HEADER, "activation-equation");
    public static final Route EXTRA_ORE_VALID_DROPS = Route.addTo(EXTRA_ORE_HEADER, "valid-drops");

    // It's A Triple
    private static final Route ITS_A_TRIPLE_HEADER = Route.addTo(ABILITY_CONFIGURATION_HEADER, "its-a-triple");
    public static final Route ITS_A_TRIPLE_ENABLED = Route.addTo(ITS_A_TRIPLE_HEADER, "enabled");
    public static final Route ITS_A_TRIPLE_AMOUNT_OF_TIERS = Route.addTo(ITS_A_TRIPLE_HEADER, "amount-of-tiers");
    public static final Route ITS_A_TRIPLE_CONFIGURATION_HEADER = Route.addTo(ITS_A_TRIPLE_HEADER, "tier-configuration");

    // Remote Transfer
    private static final Route REMOTE_TRANSFER_HEADER = Route.addTo(ABILITY_CONFIGURATION_HEADER, "remote-transfer");
    public static final Route REMOTE_TRANSFER_ENABLED = Route.addTo(REMOTE_TRANSFER_HEADER, "enabled");
    public static final Route REMOTE_TRANSFER_AMOUNT_OF_TIERS = Route.addTo(REMOTE_TRANSFER_HEADER, "amount-of-tiers");
    public static final Route REMOTE_TRANSFER_CONFIGURATION_HEADER = Route.addTo(REMOTE_TRANSFER_HEADER, "tier-configuration");

    // Remote Transfer allow list
    private static final Route REMOTE_TRANSFER_ALLOW_LIST_HEADER = Route.addTo(REMOTE_TRANSFER_HEADER, "allowed-blocks");
    public static final Route REMOTE_TRANSFER_ALLOW_LIST_NETHER = Route.addTo(REMOTE_TRANSFER_ALLOW_LIST_HEADER, "nether");
    public static final Route REMOTE_TRANSFER_ALLOW_LIST_ORES = Route.addTo(REMOTE_TRANSFER_ALLOW_LIST_HEADER, "ores");
    public static final Route REMOTE_TRANSFER_ALLOW_LIST_CAVES = Route.addTo(REMOTE_TRANSFER_ALLOW_LIST_HEADER, "caves");
    public static final Route REMOTE_TRANSFER_ALLOW_LIST_END = Route.addTo(REMOTE_TRANSFER_ALLOW_LIST_HEADER, "end");
    public static final Route REMOTE_TRANSFER_ALLOW_LIST_OCEAN = Route.addTo(REMOTE_TRANSFER_ALLOW_LIST_HEADER, "ocean");
    public static final Route REMOTE_TRANSFER_ALLOW_LIST_TERRACOTTA= Route.addTo(REMOTE_TRANSFER_ALLOW_LIST_HEADER, "terracotta");
    public static final Route REMOTE_TRANSFER_ALLOW_LIST_OVERWORLD = Route.addTo(REMOTE_TRANSFER_ALLOW_LIST_HEADER, "overworld");
    public static final Route REMOTE_TRANSFER_ALLOW_LIST_CUSTOM = Route.addTo(REMOTE_TRANSFER_ALLOW_LIST_HEADER, "custom");

    // Ore Scanner
    private static final Route ORE_SCANNER_HEADER = Route.addTo(ABILITY_CONFIGURATION_HEADER, "ore-scanner");
    public static final Route ORE_SCANNER_ENABLED = Route.addTo(ORE_SCANNER_HEADER, "enabled");
    public static final Route ORE_SCANNER_BLOCK_TYPES = Route.addTo(ORE_SCANNER_HEADER, "block-types");
    public static final Route ORE_SCANNER_AMOUNT_OF_TIERS = Route.addTo(ORE_SCANNER_HEADER, "amount-of-tiers");
    public static final Route ORE_SCANNER_CONFIGURATION_HEADER = Route.addTo(ORE_SCANNER_HEADER, "tier-configuration");

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
