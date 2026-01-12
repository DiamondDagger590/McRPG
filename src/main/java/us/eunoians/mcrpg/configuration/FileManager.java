package us.eunoians.mcrpg.configuration;

import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.Manager;
import com.diamonddagger590.mccore.registry.manager.ManagerKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import dev.dejvokep.boostedyaml.spigot.SpigotSerializer;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages all of McRPGs configuration files and is the point of access
 * to get any {@link YamlDocument}s that hold configuration values.
 */
public final class FileManager extends Manager<McRPG> {

    private static final String LOCALIZATION_FOLDER = "localization";

    private final Map<FileType, YamlDocument> loadedFiles;
    private final List<YamlDocument> localizationFiles;

    public FileManager(@NotNull McRPG mcRPG) {
        super(mcRPG);
        this.loadedFiles = new HashMap<>();
        this.localizationFiles = new ArrayList<>();

        if (!mcRPG.getDataFolder().exists()) {
            mcRPG.getDataFolder().mkdirs();
        }

        loadFiles();
        loadLocalizationFiles();
    }

    /**
     * Loads all the configuration files for McRPG.
     */
    private void loadFiles() {
        for (FileType fileType : FileType.values()) {
            loadedFiles.put(fileType, fileType.initializeFile());
        }
    }

    /**
     * Reloads all the configuration files for McRPG.
     */
    public void reloadFiles() {
        for (YamlDocument yamlDocument : loadedFiles.values()) {
            try {
                yamlDocument.reload();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        plugin().registryAccess().registry(RegistryKey.MANAGER).manager(ManagerKey.RELOADABLE_CONTENT).reloadAllContent();
    }

    /**
     * Gets the {@link YamlDocument} that contains all the configuration information
     * for the provided {@link FileType}.
     *
     * @param fileType The {@link FileType} to get the configuration for.
     * @return The {@link YamlDocument} that contains all the configuration information for the provided
     * {@link FileType}.
     */
    @NotNull
    public YamlDocument getFile(@NotNull FileType fileType) {
        return loadedFiles.get(fileType);
    }

    /**
     * Loads all locale files from the localization folder.
     * <p>
     * Locale files are organized in subfolders by language name (e.g., {@code localization/english/}).
     * Each subfolder can contain multiple {@code .yml} files that share the same {@code locale} key.
     * <p>
     * If the folder doesn't exist or has no subfolders, copies the default English locale from resources.
     */
    private void loadLocalizationFiles() {
        File localizationFolder = new File(plugin().getDataFolder(), LOCALIZATION_FOLDER);

        // Create folder if it doesn't exist
        if (!localizationFolder.exists()) {
            localizationFolder.mkdirs();
        }

        // Copy default English locale folder if it doesn't exist
        File englishFolder = new File(localizationFolder, "english");
        if (!englishFolder.exists()) {
            englishFolder.mkdirs();
            copyDefaultLocaleFolder("english");
        }

        // Scan all subfolders for locale files
        File[] languageFolders = localizationFolder.listFiles(File::isDirectory);
        if (languageFolders != null) {
            for (File languageFolder : languageFolders) {
                loadLocaleFilesFromFolder(languageFolder);
            }
        }
    }

    /**
     * Copies all default locale files from resources for a given language folder.
     *
     * @param languageFolderName The name of the language folder (e.g., "english").
     */
    private void copyDefaultLocaleFolder(@NotNull String languageFolderName) {
        // We need to know what files exist in the resources folder
        // Since we can't list resources directly, we try to copy known default files
        String resourcePath = LOCALIZATION_FOLDER + "/" + languageFolderName + "/";
        File targetFolder = new File(plugin().getDataFolder(), resourcePath);

        // Try to copy each file that might exist in resources
        // The resource stream will be null if the file doesn't exist
        String[] possibleFiles = getResourceLocaleFiles(languageFolderName);
        for (String fileName : possibleFiles) {
            try (InputStream resourceStream = plugin().getResource(resourcePath + fileName)) {
                if (resourceStream != null) {
                    File targetFile = new File(targetFolder, fileName);
                    Files.copy(resourceStream, targetFile.toPath());
                    Bukkit.getLogger().info("Copied default locale file: " + languageFolderName + "/" + fileName);
                }
            } catch (IOException e) {
                Bukkit.getLogger().warning("Failed to copy default locale file " + languageFolderName + "/" + fileName + ": " + e.getMessage());
            }
        }
    }

    /**
     * Gets the list of locale files that exist in resources for a given language.
     * <p>
     * Since Java cannot list resources in a folder directly, this method returns
     * a list of known locale file patterns to check.
     *
     * @param languageFolderName The language folder name.
     * @return An array of potential file names to check.
     */
    @NotNull
    private String[] getResourceLocaleFiles(@NotNull String languageFolderName) {
        // For the default English locale, we know the exact files
        if ("english".equals(languageFolderName)) {
            return new String[]{"en.yml", "en_commands.yml", "en_gui.yml", "en_abilities.yml", "en_skills.yml"};
        }
        // For other languages, return empty - they'll be loaded from disk if present
        return new String[]{};
    }

    /**
     * Loads all {@code .yml} files from a language folder.
     *
     * @param languageFolder The folder containing locale files for a language.
     */
    private void loadLocaleFilesFromFolder(@NotNull File languageFolder) {
        File[] files = languageFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) {
            return;
        }

        String languageName = languageFolder.getName();
        for (File file : files) {
            try {
                Bukkit.getLogger().info("Loading locale file: " + languageName + "/" + file.getName());
                String resourcePath = LOCALIZATION_FOLDER + "/" + languageName + "/" + file.getName();
                YamlDocument document = YamlDocument.create(
                        file,
                        plugin().getResource(resourcePath),
                        GeneralSettings.builder()
                                .setKeyFormat(GeneralSettings.KeyFormat.STRING)
                                .setSerializer(SpigotSerializer.getInstance())
                                .build(),
                        LoaderSettings.builder().setAutoUpdate(true).build(),
                        UpdaterSettings.builder()
                                .setVersioning(new BasicVersioning("config-version"))
                                .build()
                );
                localizationFiles.add(document);
            } catch (IOException e) {
                Bukkit.getLogger().warning("Failed to load locale file " + languageName + "/" + file.getName() + ": " + e.getMessage());
            }
        }
    }

    /**
     * Gets all loaded localization files.
     *
     * @return A {@link List} of {@link YamlDocument}s representing all loaded locale files.
     */
    @NotNull
    public List<YamlDocument> getLocalizationFiles() {
        return localizationFiles;
    }
}
