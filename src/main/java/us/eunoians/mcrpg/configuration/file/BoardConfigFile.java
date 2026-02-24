package us.eunoians.mcrpg.configuration.file;

import dev.dejvokep.boostedyaml.route.Route;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Configuration file for {@code quest-board/board.yml}.
 * <p>
 * The rarities section is ignored during config updates so user-defined rarities
 * are not overwritten by defaults.
 */
public final class BoardConfigFile extends ConfigFile {

    private static final int CURRENT_VERSION = 1;

    public static final Route MINIMUM_TOTAL_OFFERINGS =
            Route.fromString("slot-layout.minimum-total-offerings");
    public static final Route MAX_ACCEPTED_QUESTS =
            Route.fromString("max-accepted-quests");
    public static final Route RARITIES =
            Route.fromString("rarities");
    public static final Route SOURCE_WEIGHT_HAND_CRAFTED =
            Route.fromString("quest-source-weights.hand-crafted");
    public static final Route SOURCE_WEIGHT_TEMPLATE =
            Route.fromString("quest-source-weights.template");
    public static final Route ROTATION_TIME =
            Route.fromString("rotation.time");
    public static final Route ROTATION_TIMEZONE =
            Route.fromString("rotation.timezone");
    public static final Route ROTATION_WEEKLY_RESET_DAY =
            Route.fromString("rotation.weekly-reset-day");
    public static final Route ROTATION_CHECK_INTERVAL =
            Route.fromString("rotation.task-check-interval-seconds");

    @NotNull
    @Override
    public UpdaterSettings getUpdaterSettings() {
        Map<String, Set<Route>> ignoredRoutes = new HashMap<>();
        for (int i = 1; i <= CURRENT_VERSION; i++) {
            Set<Route> ignored = new HashSet<>();
            ignored.add(RARITIES);
            ignoredRoutes.put(String.valueOf(i), ignored);
        }
        return UpdaterSettings.builder()
                .addIgnoredRoutes(ignoredRoutes)
                .build();
    }
}
