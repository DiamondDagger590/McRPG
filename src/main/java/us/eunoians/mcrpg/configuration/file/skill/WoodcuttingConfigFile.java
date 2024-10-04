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
