package us.eunoians.mcrpg.configuration.legacy;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.configuration.FileType;

/**
 * This file is used to create custom permission nodes that modify the rate of experience players gain for different skills
 * if they have a matching permission node.
 */
public enum ExpPermissionsFile implements McRPGConfigurationFile {

    PERMISSION_CONFIGURATION_SECTION("", "", false),
    PERMISSION_NODE("%s.permission-node", "mcrpg.expbuff.miner", true),
    APPLICABLE_SKILL_MODIFIER("%s.affected-skills.%s", 0.5, true),
    PRIORITY("%s.priority", 0, true);

    private final String path;
    private final Object defaultValue;
    private final boolean acceptsPlaceholders;

    ExpPermissionsFile(@NotNull String path, @NotNull Object defaultValue, boolean acceptsPlaceholders) {
        this.path = path;
        this.defaultValue = defaultValue;
        this.acceptsPlaceholders = acceptsPlaceholders;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public String getPath() {
        return path;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
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

    @NotNull
    @Override
    public FileType getFileType() {
        return null;
    }
}
