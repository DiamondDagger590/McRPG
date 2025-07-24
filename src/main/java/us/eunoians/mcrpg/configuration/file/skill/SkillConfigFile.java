package us.eunoians.mcrpg.configuration.file.skill;

import dev.dejvokep.boostedyaml.route.Route;
import us.eunoians.mcrpg.configuration.file.ConfigFile;

import static com.diamonddagger590.mccore.util.Methods.toRoutePath;

public abstract class SkillConfigFile extends ConfigFile {

    // Main Headers
    protected static final String PERMISSIONS_HEADER = "permissions";
    protected static final String LEVELING_HEADER = "leveling";
    protected static final String EXPERIENCE_HEADER = "experience";
    protected static final String ABILITY_CONFIGURATION_HEADER = "ability-configuration";

    public static final Route SKILL_ENABLED = Route.fromString("skill-enabled");

    // Permissions
    public static final Route RESTRICT_SKILL_TO_PERMISSIONS = Route.fromString(toRoutePath(PERMISSIONS_HEADER, "restrict-skill-to-permissions"));
    public static final Route USE_PERMISSIONS_TO_UNLOCK_ABILITIES = Route.fromString(toRoutePath(PERMISSIONS_HEADER, "use-permissions-to-unlock-abilities"));
    public static final Route USE_PERMISSIONS_TO_ACTIVATE_ABILITIES = Route.fromString(toRoutePath(PERMISSIONS_HEADER, "use-permissions-to-activate-abilities"));

    // Leveling
    public static final Route LEVEL_UP_EQUATION = Route.fromString(toRoutePath(LEVELING_HEADER, "level-up-equation"));
    public static final Route MAXIMUM_SKILL_LEVEL = Route.fromString(toRoutePath(LEVELING_HEADER, "maximum-skill-level"));
}
