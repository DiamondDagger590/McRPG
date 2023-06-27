package us.eunoians.mcrpg.configuration;

import ch.jalu.configme.SettingsHolder;
import ch.jalu.configme.SettingsManager;
import ch.jalu.configme.SettingsManagerBuilder;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.file.MainConfigFile;

import java.io.File;
import java.io.IOException;

public enum FileType {

    MAIN_CONFIG("config.yml", MainConfigFile.class);

    private static final String FOLDER_PATH = McRPG.getInstance().getDataFolder().getPath();

    private final String filePath;
    private final Class<? extends SettingsHolder> clazz;

    FileType(@NotNull String filePath, @NotNull Class<? extends SettingsHolder> clazz) {
        this.filePath = filePath;
        this.clazz = clazz;
    }

    @NotNull
    public SettingsManager initializeFile() {
        File configFile = new File(FOLDER_PATH + File.separator + filePath);
        if(!configFile.exists()) {
            try {
                configFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return SettingsManagerBuilder
                .withYamlFile(configFile)
                .configurationData(clazz)
                .useDefaultMigrationService()
                .create();
    }
}
