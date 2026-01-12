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
     * If the folder doesn't exist or has no subfolders, copies the bundled locales from resources.
     */
    private void loadLocalizationFiles() {
        File localizationFolder = new File(plugin().getDataFolder(), LOCALIZATION_FOLDER);

        // Create folder if it doesn't exist
        if (!localizationFolder.exists()) {
            localizationFolder.mkdirs();
        }

        // Copy all bundled locale folders if they don't exist
        for (BundledLocale bundledLocale : BundledLocale.values()) {
            File localeFolder = new File(localizationFolder, bundledLocale.getFolderName());
            if (!localeFolder.exists()) {
                localeFolder.mkdirs();
                copyBundledLocaleFolder(bundledLocale);
            }
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
     * Copies all locale files from resources for a bundled locale.
     *
     * @param bundledLocale The {@link BundledLocale} to copy files for.
     */
    private void copyBundledLocaleFolder(@NotNull BundledLocale bundledLocale) {
        String resourcePath = LOCALIZATION_FOLDER + "/" + bundledLocale.getFolderName() + "/";
        File targetFolder = new File(plugin().getDataFolder(), resourcePath);

        for (String fileName : bundledLocale.getFileNames()) {
            try (InputStream resourceStream = plugin().getResource(resourcePath + fileName)) {
                if (resourceStream != null) {
                    File targetFile = new File(targetFolder, fileName);
                    Files.copy(resourceStream, targetFile.toPath());
                    Bukkit.getLogger().info("Copied bundled locale file: " + bundledLocale.getFolderName() + "/" + fileName);
                }
            } catch (IOException e) {
                Bukkit.getLogger().warning("Failed to copy bundled locale file " + bundledLocale.getFolderName() + "/" + fileName + ": " + e.getMessage());
            }
        }
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
