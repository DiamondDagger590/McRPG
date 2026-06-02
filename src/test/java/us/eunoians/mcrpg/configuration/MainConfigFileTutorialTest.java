package us.eunoians.mcrpg.configuration;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.MainConfigFile;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that {@link MainConfigFile#TUTORIAL_ENABLED} resolves correctly
 * against the bundled {@code config.yml} defaults.
 */
public class MainConfigFileTutorialTest extends McRPGBaseTest {

    @Test
    @DisplayName("Given the bundled config.yml default, when TUTORIAL_ENABLED route is read, then it resolves to true")
    public void tutorialEnabled_defaultValueIsTrue() {
        YamlDocument config = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.FILE)
                .getFile(FileType.MAIN_CONFIG);

        assertTrue(config.getBoolean(MainConfigFile.TUTORIAL_ENABLED),
                "tutorial.enabled should default to true in config.yml");
    }
}
