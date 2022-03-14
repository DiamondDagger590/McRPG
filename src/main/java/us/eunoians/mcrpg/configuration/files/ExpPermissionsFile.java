package us.eunoians.mcrpg.configuration.files;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.configuration.FileType;

/**
 * This file is used to create custom permission nodes that modify the rate of experience players gain for different skills
 * if they have a matching permission node.
 */
public enum ExpPermissionsFile implements McRPGConfigurationFile {

    PERMISSION_CONFIGURATION_SECTION("", ""),
    PERMISSION_NODE("%s.permission-node", "mcrpg.expbuff.miner"),
    APPLICABLE_SKILL_MODIFIER("%s.affected-skills.%s", 0.5),
    PRIORITY("%s.priority", 0);

    private final String path;
    private final Object defaultValue;

    ExpPermissionsFile(@NotNull String path, @NotNull Object defaultValue) {
        this.path = path;
        this.defaultValue = defaultValue;
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
     * Gets the {@link FileType} that represents this file
     *
     * @return The {@link FileType} that represents this file
     */
    @NotNull
    @Override
    public FileType getFileType() {
        return FileType.EXP_PERMISSIONS;
    }
}
