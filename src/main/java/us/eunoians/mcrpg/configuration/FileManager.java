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
     * If the folder doesn't exist or is empty, copies the default en.yml from resources.
     */
    private void loadLocalizationFiles() {
        File localizationFolder = new File(plugin().getDataFolder(), LOCALIZATION_FOLDER);

        // Create folder if it doesn't exist
        if (!localizationFolder.exists()) {
            localizationFolder.mkdirs();
        }

        // Copy default en.yml if folder is empty
        File defaultLocale = new File(localizationFolder, "en.yml");
        if (!defaultLocale.exists()) {
            try (InputStream defaultStream = plugin().getResource(LOCALIZATION_FOLDER + "/en.yml")) {
                if (defaultStream != null) {
                    Files.copy(defaultStream, defaultLocale.toPath());
                }
            } catch (IOException e) {
                Bukkit.getLogger().warning("Failed to copy default locale file: " + e.getMessage());
            }
        }

        // Load all .yml files in the folder
        File[] files = localizationFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files != null) {
            for (File file : files) {
                try {
                    Bukkit.getLogger().info("Loading locale file: " + file.getName());
                    YamlDocument document = YamlDocument.create(
                            file,
                            plugin().getResource(LOCALIZATION_FOLDER + "/" + file.getName()),
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
                    Bukkit.getLogger().warning("Failed to load locale file " + file.getName() + ": " + e.getMessage());
                }
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
