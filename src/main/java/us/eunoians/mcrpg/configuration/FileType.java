package us.eunoians.mcrpg.configuration;

import dev.dejvokep.boostedyaml.YamlDocument;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.file.ConfigFile;
import us.eunoians.mcrpg.configuration.file.MainConfigFile;
import us.eunoians.mcrpg.configuration.file.SwordsConfigFile;

import java.io.File;
import java.io.IOException;

public enum FileType {

    MAIN_CONFIG("config.yml", new MainConfigFile()),
    SWORDS_CONFIG("skill_configuration" + File.separator + "swords_configuration.yml", new SwordsConfigFile()),;

    private final String filePath;
    private final ConfigFile configFile;

    FileType(@NotNull String filePath, @NotNull ConfigFile configFile) {
        this.filePath = filePath;
        this.configFile = configFile;
    }

    @NotNull
    public YamlDocument initializeFile() {
        try {
            return YamlDocument.create(new File(McRPG.getInstance().getDataFolder(), filePath), McRPG.getInstance().getResource(filePath), configFile.getGeneralSettings(),
                    configFile.getLoaderSettings(), configFile.getDumperSettings(), configFile.getUpdaterSettings());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
