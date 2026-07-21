package us.eunoians.mcrpg.configuration.file;

import dev.dejvokep.boostedyaml.route.Route;

import static com.diamonddagger590.mccore.util.Methods.toRoutePath;

/**
 * Configuration file for {@code combat_configuration.yml}.
 * <p>
 * Contains route constants for combat session settings such as timeout duration,
 * maximum mob participants, and timeout scan interval.
 */
public final class CombatConfigFile extends ConfigFile {

    private static final int CURRENT_VERSION = 2;

    // Session
    private static final String SESSION_HEADER = "session";
    public static final Route SESSION_TIMEOUT_SECONDS =
            Route.fromString(toRoutePath(SESSION_HEADER, "timeout-seconds"));
    public static final Route MAX_MOB_PARTICIPANTS =
            Route.fromString(toRoutePath(SESSION_HEADER, "max-mob-participants"));
    public static final Route TIMEOUT_SCAN_INTERVAL_SECONDS =
            Route.fromString(toRoutePath(SESSION_HEADER, "timeout-scan-interval-seconds"));

    // Per-Session Statistics
    private static final String PER_SESSION_STATISTICS_HEADER = "per-session-statistics";
    public static final Route FEED_TO_CUMULATIVE =
            Route.fromString(toRoutePath(PER_SESSION_STATISTICS_HEADER, "feed-to-cumulative"));
}
