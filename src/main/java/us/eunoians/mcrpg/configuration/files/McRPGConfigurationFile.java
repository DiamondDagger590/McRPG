package us.eunoians.mcrpg.configuration.files;

import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.FileType;

import java.util.Optional;

/**
 * An interface that can be extended by McRPG files in order to allow more structured file usage
 * going forward.
 */
public sealed interface McRPGConfigurationFile permits ExpPermissionsFile, MainConfigurationFile {

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
     * Gets the {@link FileType} that represents this file
     *
     * @return The {@link FileType} that represents this file
     */
    @NotNull
    public FileType getFileType();

    /**
     * Gets an {@link Optional} that is either empty or contains the {@link FileConfiguration} associated with this file
     *
     * @return An {@link Optional} that is either empty or contains the {@link FileConfiguration} associated with this file
     */
    @NotNull
    public default Optional<FileConfiguration> getFileConfiguration() {
        return McRPG.getInstance().getFileManager().getFileConfiguration(getFileType());
    }
}
