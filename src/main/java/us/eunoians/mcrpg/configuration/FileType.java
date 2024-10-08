package us.eunoians.mcrpg.configuration;

import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.file.ConfigFile;
import us.eunoians.mcrpg.configuration.file.MainConfigFile;
import us.eunoians.mcrpg.configuration.file.skill.MiningConfigFile;
import us.eunoians.mcrpg.configuration.file.skill.SwordsConfigFile;
import us.eunoians.mcrpg.configuration.file.skill.WoodcuttingConfigFile;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public enum FileType {

    MAIN_CONFIG("config.yml", new MainConfigFile()),
    SWORDS_CONFIG("skill_configuration" + "/" + "swords_configuration.yml", new SwordsConfigFile()),
    MINING_CONFIG("skill_configuration" + "/" + "mining_configuration.yml", new MiningConfigFile()),
    WOODCUTTING_CONFIG("skill_configuration" + "/" + "woodcutting_configuration.yml", new WoodcuttingConfigFile()),
    ;

    private final String filePath;
    private final ConfigFile configFile;

    FileType(@NotNull String filePath, @NotNull ConfigFile configFile) {
        this.filePath = filePath;
        this.configFile = configFile;
    }

    @NotNull
    public YamlDocument initializeFile() {
        try {
            Bukkit.getLogger().info("Loading " + filePath);
            return YamlDocument.create(new File(McRPG.getInstance().getDataFolder(), filePath), Objects.requireNonNull(McRPG.getInstance().getResource(filePath)), configFile.getGeneralSettings(),
                    configFile.getLoaderSettings(), configFile.getDumperSettings(), configFile.getUpdaterSettings());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
