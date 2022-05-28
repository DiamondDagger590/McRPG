package us.eunoians.mcrpg.configuration;

import com.diamonddagger590.mccore.file.IOUtil;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;

import java.io.File;

/**
 * This functional interface is used to build the files in an abstract way, allowing for generic handling
 * of files using {@link de.articdive.enum_to_yaml.interfaces.ConfigurationEnum} or standard .yml files.
 */
@FunctionalInterface
public interface FileBuildFunction {

    /**
     * The default YAML configuration build function that is used by most non-auto updating configs
     */
    FileBuildFunction DEFAULT_YAML_BUILD_FUNCTION = (filePath) -> {
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
    };

    /**
     * Builds (if non-existent) or loads the {@link File} that is found at the provided file path.
     *
     * @param filePath The file path to build the {@link File} at
     * @return A {@link File} that is found at the provided file path
     */
    @NotNull
    public File buildFile(@NotNull String filePath);
}
