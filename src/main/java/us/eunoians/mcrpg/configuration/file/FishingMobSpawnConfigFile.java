package us.eunoians.mcrpg.configuration.file;

import dev.dejvokep.boostedyaml.route.Route;

import static com.diamonddagger590.mccore.util.Methods.toRoutePath;

/**
 * Contains all the {@link Route}s used for the fishing_mob_spawn_configuration.yml.
 */
public final class FishingMobSpawnConfigFile extends ConfigFile {

    // Top-level headers
    private static final String SPAWN_HEADER = "spawn";
    private static final String MOB_POOL_HEADER = "mob-pool";

    // Spawn settings
    public static final Route SPAWN_ENABLED = Route.fromString(toRoutePath(SPAWN_HEADER, "enabled"));
    public static final Route BASE_CHANCE = Route.fromString(toRoutePath(SPAWN_HEADER, "base-chance"));
    public static final Route MAX_CHANCE = Route.fromString(toRoutePath(SPAWN_HEADER, "max-chance"));
    public static final Route CHANCE_INCREMENT_PER_CATCH = Route.fromString(toRoutePath(SPAWN_HEADER, "chance-increment-per-catch"));
    public static final Route CHANCE_DECREMENT_PER_CATCH = Route.fromString(toRoutePath(SPAWN_HEADER, "chance-decrement-per-catch"));
    public static final Route SAME_AREA_RANGE = Route.fromString(toRoutePath(SPAWN_HEADER, "same-area-range"));
    public static final Route POST_KILL_CHANCE = Route.fromString(toRoutePath(SPAWN_HEADER, "post-kill-chance"));
    public static final Route RESET_ON_WORLD_CHANGE = Route.fromString(toRoutePath(SPAWN_HEADER, "reset-on-world-change"));
    public static final Route SPAWN_OFFSET_FROM_HOOK = Route.fromString(toRoutePath(SPAWN_HEADER, "spawn-offset-from-hook"));
    public static final Route SPAWN_Y_OFFSET = Route.fromString(toRoutePath(SPAWN_HEADER, "spawn-y-offset"));
    public static final Route MAX_ACTIVE_MOBS_PER_PLAYER = Route.fromString(toRoutePath(SPAWN_HEADER, "max-active-mobs-per-player"));

    // Mob pool (map-based — accessed dynamically by key)
    public static final Route MOB_POOL = Route.fromString(MOB_POOL_HEADER);
}
