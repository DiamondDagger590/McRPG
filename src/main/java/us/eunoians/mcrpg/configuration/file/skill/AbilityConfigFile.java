package us.eunoians.mcrpg.configuration.file.skill;

import us.eunoians.mcrpg.configuration.file.ConfigFile;

import static com.diamonddagger590.mccore.util.Methods.toRoutePath;

public abstract class AbilityConfigFile extends ConfigFile {

    // Main Headers
    protected static final String PERMISSIONS_HEADER = "permissions";
    protected static final String LEVELING_HEADER = "leveling";
    protected static final String EXPERIENCE_HEADER = "experience";
    protected static final String ABILITY_CONFIGURATION_HEADER = "ability-configuration";

    public static final String SKILL_ENABLED = "skill-enabled";

    // Permissions
    public static final String RESTRICT_SKILL_TO_PERMISSIONS = toRoutePath(PERMISSIONS_HEADER, "restrict-skill-to-permissions");
    public static final String USE_PERMISSIONS_TO_UNLOCK_ABILITIES = toRoutePath(PERMISSIONS_HEADER, "use-permissions-to-unlock-abilities");
    public static final String USE_PERMISSIONS_TO_ACTIVATE_ABILITIES = toRoutePath(PERMISSIONS_HEADER, "use-permissions-to-activate-abilities");

    // Leveling
    public static final String LEVEL_UP_EQUATION = toRoutePath(LEVELING_HEADER, "level-up-equation");
    public static final String MAXIMUM_SKILL_LEVEL = toRoutePath(LEVELING_HEADER, "maximum-skill-level");
}
