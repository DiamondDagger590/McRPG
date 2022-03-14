package us.eunoians.mcrpg.configuration;

import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * This functional interface is used to build the files in an abstract way, allowing for generic handling
 * of files using {@link de.articdive.enum_to_yaml.interfaces.ConfigurationEnum} or standard .yml files.
 */
@FunctionalInterface
public interface FileBuildFunction {

    /**
     * Builds (if non-existent) or loads the {@link File} that is found at the provided file path.
     *
     * @param filePath The file path to build the {@link File} at
     * @return A {@link File} that is found at the provided file path
     */
    @NotNull
    public File buildFile(@NotNull String filePath);
}
