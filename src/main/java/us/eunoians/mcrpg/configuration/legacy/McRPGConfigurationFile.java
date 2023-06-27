package us.eunoians.mcrpg.configuration.legacy;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.configuration.FileType;

/**
 * An interface that can be extended by McRPG files in order to allow more structured file usage
 * going forward.
 */
public sealed interface McRPGConfigurationFile permits ExpPermissionsFile, MainConfigurationFile, SwordsConfigurationFile {

    /**
     * Gets the configuration path for the node.
     *
     * @return The configuration path for the node.
     */
    @NotNull
    public String getPath();

    /**
     * Get the default value found at the node. This will be an empty string for section keys that are meant to return configuration sections
     *
     * @return The default value found at the node. This will be an empty string for section keys that are meant to return configuration sections
     */
    @NotNull
    public Object getDefaultValue();

    /**
     * Returns if the {@link #getPath()} contains placeholders that can be replaced in order to allow for more dynamic configuration
     *
     * @return {@code true} if the {@link #getPath()} contains placeholders that can be replaced in order to allow for more dynamic configuration
     */
    public boolean acceptsPlaceholders();

    /**
     * Gets the {@link FileType} that represents this file
     *
     * @return The {@link FileType} that represents this file
     */
    @NotNull
    public FileType getFileType();
}
