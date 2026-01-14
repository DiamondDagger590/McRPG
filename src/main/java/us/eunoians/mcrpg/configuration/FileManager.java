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
import us.eunoians.mcrpg.localization.BundledLocale;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
     * Reloads all the configuration files for McRPG, including localization files.
     */
    public void reloadFiles() {
        for (YamlDocument yamlDocument : loadedFiles.values()) {
            try {
                yamlDocument.reload();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        // Reload localization files
        for (YamlDocument yamlDocument : localizationFiles) {
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
     * Bundled locale files are loaded using BoostedYaml's auto-update feature, which will create
     * the file from resources if it doesn't exist and update it with new keys from the resource.
     */
    private void loadLocalizationFiles() {
        File localizationFolder = new File(plugin().getDataFolder(), LOCALIZATION_FOLDER);

        // Create folder if it doesn't exist
        if (!localizationFolder.exists()) {
            localizationFolder.mkdirs();
        }

        // Create bundled locale folders if they don't exist
        for (BundledLocale bundledLocale : BundledLocale.values()) {
            File localeFolder = new File(localizationFolder, bundledLocale.getFolderName());
            if (!localeFolder.exists()) {
                localeFolder.mkdirs();
            }
        }

        // Track which files we've already loaded (to avoid loading bundled files twice)
        Set<String> loadedFilePaths = new HashSet<>();

        // First, load all bundled locale files (these have resource streams for auto-updating)
        for (BundledLocale bundledLocale : BundledLocale.values()) {
            File localeFolder = new File(localizationFolder, bundledLocale.getFolderName());
            for (String fileName : bundledLocale.getFileNames()) {
                File file = new File(localeFolder, fileName);
                String resourcePath = LOCALIZATION_FOLDER + "/" + bundledLocale.getFolderName() + "/" + fileName;
                loadLocaleFile(file, resourcePath);
                loadedFilePaths.add(file.getAbsolutePath());
            }
        }

        // Then, scan all subfolders for additional locale files added by server owners
        File[] languageFolders = localizationFolder.listFiles(File::isDirectory);
        if (languageFolders != null) {
            for (File languageFolder : languageFolders) {
                File[] files = languageFolder.listFiles((dir, name) -> name.endsWith(".yml"));
                if (files == null) {
                    continue;
                }

                for (File file : files) {
                    // Skip files we've already loaded as bundled files
                    if (loadedFilePaths.contains(file.getAbsolutePath())) {
                        continue;
                    }
                    // Load without resource stream (no auto-updating for user-added files)
                    loadLocaleFile(file, null);
                }
            }
        }
    }

    /**
     * Loads a single locale file using BoostedYaml.
     * <p>
     * If a resource path is provided, the file will be created from the resource if it doesn't exist
     * and will be auto-updated with new keys from the resource on subsequent loads.
     *
     * @param file         The file to load.
     * @param resourcePath The resource path for auto-updating, or {@code null} for user-added files.
     */
    private void loadLocaleFile(@NotNull File file, String resourcePath) {
        try {
            String displayPath = file.getParentFile().getName() + "/" + file.getName();
            Bukkit.getLogger().info("Loading locale file: " + displayPath);
            YamlDocument document = YamlDocument.create(
                    file,
                    resourcePath != null ? plugin().getResource(resourcePath) : null,
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
            String displayPath = file.getParentFile().getName() + "/" + file.getName();
            Bukkit.getLogger().warning("Failed to load locale file " + displayPath + ": " + e.getMessage());
        }
    }

    /**
     * Gets all loaded localization files.
     *
     * @return An unmodifiable {@link List} of {@link YamlDocument}s representing all loaded locale files.
     */
    @NotNull
    public List<YamlDocument> getLocalizationFiles() {
        return List.copyOf(localizationFiles);
    }
}
