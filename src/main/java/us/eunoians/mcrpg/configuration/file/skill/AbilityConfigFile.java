package us.eunoians.mcrpg.configuration.file.skill;

import dev.dejvokep.boostedyaml.route.Route;
import us.eunoians.mcrpg.configuration.file.ConfigFile;

public abstract class AbilityConfigFile extends ConfigFile {

    // Main Headers
    protected static final Route PERMISSIONS_HEADER = Route.fromString("permissions");
    protected static final Route LEVELING_HEADER = Route.fromString("leveling");
    protected static final Route EXPERIENCE_HEADER = Route.fromString("experience");
    protected static final Route ABILITY_CONFIGURATION_HEADER = Route.fromString("ability-configuration");

    public static final Route SKILL_ENABLED = Route.fromString("skill-enabled");

    // Permissions
    public static final Route RESTRICT_SKILL_TO_PERMISSIONS = Route.addTo(PERMISSIONS_HEADER, "restrict-skill-to-permissions");
    public static final Route USE_PERMISSIONS_TO_UNLOCK_ABILITIES = Route.addTo(PERMISSIONS_HEADER, "use-permissions-to-unlock-abilities");
    public static final Route USE_PERMISSIONS_TO_ACTIVATE_ABILITIES = Route.addTo(PERMISSIONS_HEADER, "use-permissions-to-activate-abilities");

    // Leveling
    public static final Route LEVEL_UP_EQUATION = Route.addTo(LEVELING_HEADER, "level-up-equation");
    public static final Route MAXIMUM_SKILL_LEVEL = Route.addTo(LEVELING_HEADER, "maximum-skill-level");
}
