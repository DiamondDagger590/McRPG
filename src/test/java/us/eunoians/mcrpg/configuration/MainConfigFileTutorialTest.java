package us.eunoians.mcrpg.configuration;

import dev.dejvokep.boostedyaml.YamlDocument;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.configuration.file.MainConfigFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that {@link MainConfigFile#TUTORIAL_ENABLED} resolves correctly
 * against the bundled {@code config.yml} defaults.
 */
public class MainConfigFileTutorialTest extends McRPGBaseTest {

    @Test
    @DisplayName("Given the bundled config.yml default, when TUTORIAL_ENABLED route is read, then it resolves to true")
    public void tutorialEnabled_defaultValueIsTrue() throws IOException {
        URL configUrl = getClass().getClassLoader().getResource("config.yml");
        assertNotNull(configUrl, "Bundled config.yml must be on the classpath");

        try (InputStream stream = configUrl.openStream()) {
            YamlDocument config = YamlDocument.create(stream);
            assertTrue(config.getBoolean(MainConfigFile.TUTORIAL_ENABLED),
                    "tutorial.enabled should default to true in config.yml");
        }
    }
}
