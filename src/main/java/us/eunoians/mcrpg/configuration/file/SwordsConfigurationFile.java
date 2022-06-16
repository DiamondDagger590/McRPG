package us.eunoians.mcrpg.configuration.file;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.configuration.FileType;

public enum SwordsConfigurationFile implements McRPGConfigurationFile {

    SKILL_ENABLED("skill-enabled", true, false),

    /**
     * Permissions
     */
    RESTRICT_SKILL_TO_PERMISSION("permissions.restrict-skill-to-permission", false, false),
    USE_PERMISSIONS_TO_UNLOCK_ABILITIES("permissions.use-permission-to-unlock-abilities", false, false),
    USE_PERMISSIONS_TO_ACTIVATE_ABILITIES("permissions.use-permissions-to-activate-abilities", false, false),

    /**
     * Leveling
     */
    LEVEL_UP_EQUATION("leveling.level-up-equation", "2000+(20*(skill_level))", false),
    MAXIMUM_SKILL_LEVEL("maximum-skill-level", 1000, false)

    /**
     * Experience
     */
    ;

    private final String path;
    private final Object defaultValue;
    private final boolean acceptsPlaceholders;

    SwordsConfigurationFile(@NotNull String path, @NotNull Object defaultValue, boolean acceptsPlaceholders){
        this.path = path;
        this.defaultValue = defaultValue;
        this.acceptsPlaceholders = acceptsPlaceholders;
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public String getPath() {
        return path;
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean acceptsPlaceholders() {
        return acceptsPlaceholders;
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public FileType getFileType() {
        return FileType.SWORDS_CONFIG;
    }
}
