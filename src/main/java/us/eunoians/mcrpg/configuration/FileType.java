package us.eunoians.mcrpg.configuration;

import de.articdive.enum_to_yaml.EnumConfigurationBuilder;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.files.MainConfigurationFile;
import us.eunoians.mcrpg.util.IOUtil;

import java.io.File;

/**
 * An enum that contains all files that McRPG uses. Some of these files will be auto updating by utilizing EnumToYAML
 * and others will just be normal configuration files.
 */
public enum FileType {

    //The main config.yml used by McRPG
    MAIN_CONFIG(McRPG.getInstance().getDataFolder() + File.separator + "config.yml", (filePath) -> {

        File file = new File(filePath);
        new EnumConfigurationBuilder(file, MainConfigurationFile.class)
            .setWidth(100000)
            .build();

        return file;
    }),

    EXP_PERMISSIONS(McRPG.getInstance().getDataFolder() + File.separator + "exp_perms.yml", (filePath) -> {

        McRPG mcRPG = McRPG.getInstance();
        File file = new File(mcRPG.getDataFolder(), filePath);
        if (!file.exists()) {
            try {
                IOUtil.saveResource(mcRPG, filePath, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return file;
    }),
    ;

    private final String path;
    private final FileBuildFunction fileBuildFunction;

    FileType(@NotNull String path, @NotNull FileBuildFunction fileBuildFunction) {
        this.path = path;
        this.fileBuildFunction = fileBuildFunction;
    }

    /**
     * Gets the path that the file will be located at
     *
     * @return The path that the file will be located at
     */
    @NotNull
    public String getPath() {
        return path;
    }

    /**
     * Get the function used to build the configuration file.
     * <p>
     * This is package private, as the only class that should be utilizing the build function would be {@link FileManager}.
     *
     * @return A {@link FileBuildFunction} that can be used to build the configuration file.
     */
    @NotNull
    FileBuildFunction getFileBuildFunction() {
        return fileBuildFunction;
    }
}
