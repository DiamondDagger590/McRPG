package us.eunoians.mcrpg.configuration;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * This class manages all McRPG files, including the ability to load files, reload files, and get files from memory.
 */
public class FileManager {

    private final McRPG mcRPG;
    private final Map<FileType, File> files;
    private final Map<FileType, FileConfiguration> fileConfigurations;

    public FileManager(McRPG mcRPG) {
        this.mcRPG = mcRPG;
        this.files = new HashMap<>();
        this.fileConfigurations = new HashMap<>();
    }

    /**
     * Initialize any files and load their corresponding {@link FileConfiguration}s to memory.
     * <p>
     * This will also auto update any files that utilize EnumToYAML.
     */
    public void initializeAndLoadFiles() {

        //Preemptively check and create if absent the McRPG data folder
        if (!mcRPG.getDataFolder().exists()) {
            mcRPG.getDataFolder().mkdirs();
        }

        files.clear();
        fileConfigurations.clear();
        Arrays.stream(FileType.values()).forEach(this::loadFile);
    }

    /**
     * Reload all files that McRPG uses
     */
    public void reloadFiles() {
        for (FileType fileType : FileType.values()) {
            reloadFile(fileType);
        }
    }

    /**
     * Reloads a specific file, using the provided {@link FileType}.
     *
     * @param fileType The {@link FileType} to reload the file for
     */
    public void reloadFile(@NotNull FileType fileType) {

        fileConfigurations.remove(fileType);

        if (files.containsKey(fileType)) { //If the file exists, we don't need to recreate it
            FileConfiguration fileConfiguration = YamlConfiguration.loadConfiguration(files.get(fileType));
            fileConfigurations.put(fileType, fileConfiguration);
        }
        else { //Otherwise create the file through standard loading
            loadFile(fileType);
        }
    }

    /**
     * Load into memory the {@link File} and {@link FileConfiguration} associated with the provided {@link FileType}
     *
     * @param fileType The {@link FileType} to load into memory
     */
    private void loadFile(@NotNull FileType fileType) {
        File file = fileType.getFileBuildFunction().buildFile(fileType.getPath());
        FileConfiguration fileConfiguration = YamlConfiguration.loadConfiguration(file);

        files.put(fileType, file);
        fileConfigurations.put(fileType, fileConfiguration);
    }

    /**
     * Gets the {@link File} associated with the provided {@link FileType}.
     *
     * @param fileType The {@link FileType} to get the associated {@link File} for
     * @return The {@link File} associated with the provided {@link FileType}.
     */
    @NotNull
    public File getFile(@NotNull FileType fileType) {
        return files.getOrDefault(fileType, new File(fileType.getPath()));
    }

    /**
     * Gets the {@link FileConfiguration} associated with the provided {@link FileType}.
     *
     * @param fileType The {@link FileType} to get the associated {@link FileConfiguration} for
     * @return The {@link FileConfiguration} associated with the provided {@link FileType}.
     */
    @NotNull
    public FileConfiguration getFileConfiguration(@NotNull FileType fileType) {
        return fileConfigurations.getOrDefault(fileType, YamlConfiguration.loadConfiguration(getFile(fileType)));
    }
}
