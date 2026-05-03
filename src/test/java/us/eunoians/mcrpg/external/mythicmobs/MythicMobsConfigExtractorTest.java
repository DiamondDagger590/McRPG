package us.eunoians.mcrpg.external.mythicmobs;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.McRPGBaseTest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

/**
 * Tests for {@link MythicMobsConfigExtractor}.
 */
public class MythicMobsConfigExtractorTest extends McRPGBaseTest {

    private McRPG spyPlugin;

    @TempDir
    Path tempDir;

    @BeforeEach
    public void setup() {
        spyPlugin = spy(mcRPG);
        // Point the data folder to tempDir/McRPG so that parent resolves to tempDir (acting as "plugins/")
        Path pluginDataFolder = tempDir.resolve("McRPG");
        try {
            Files.createDirectories(pluginDataFolder);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        doReturn(pluginDataFolder.toFile()).when(spyPlugin).getDataFolder();
    }

    @Test
    public void extractBundledConfigs_createsPackDirectoriesAndFiles() {
        // The real plugin has the resources on the classpath, so getResource() should work
        new MythicMobsConfigExtractor(spyPlugin).extractBundledConfigs();

        Path packRoot = tempDir.resolve("MythicMobs").resolve("Packs").resolve("McRPG");
        assertTrue(Files.exists(packRoot.resolve("Mobs/RiptideGuardian.yml")),
                "RiptideGuardian.yml should be extracted");
        assertTrue(Files.exists(packRoot.resolve("Skills/RiptideGuardianSkills.yml")),
                "RiptideGuardianSkills.yml should be extracted");
        assertTrue(Files.exists(packRoot.resolve("DropTables/RiptideGuardianDrops.yml")),
                "RiptideGuardianDrops.yml should be extracted");
    }

    @Test
    public void extractBundledConfigs_skipsExistingFiles() throws IOException {
        Path packRoot = tempDir.resolve("MythicMobs").resolve("Packs").resolve("McRPG");
        Path existingFile = packRoot.resolve("Mobs/RiptideGuardian.yml");
        Files.createDirectories(existingFile.getParent());
        String customContent = "# Custom server owner content";
        Files.writeString(existingFile, customContent);

        new MythicMobsConfigExtractor(spyPlugin).extractBundledConfigs();

        // Existing file should NOT be overwritten
        String afterExtraction = Files.readString(existingFile);
        assertTrue(afterExtraction.contains("Custom server owner content"),
                "Existing file should not be overwritten");
    }

    @Test
    public void extractBundledConfigs_handlesNullResource() {
        // Override getResource to return null for all paths
        doReturn(null).when(spyPlugin).getResource(anyString());

        // Should not throw — just log warnings and continue
        new MythicMobsConfigExtractor(spyPlugin).extractBundledConfigs();

        Path packRoot = tempDir.resolve("MythicMobs").resolve("Packs").resolve("McRPG");
        assertFalse(Files.exists(packRoot.resolve("Mobs/RiptideGuardian.yml")),
                "No files should be extracted when resources are null");
    }

    @Test
    public void extractBundledConfigs_extractedFilesHaveContent() throws IOException {
        new MythicMobsConfigExtractor(spyPlugin).extractBundledConfigs();

        Path packRoot = tempDir.resolve("MythicMobs").resolve("Packs").resolve("McRPG");
        Path mobFile = packRoot.resolve("Mobs/RiptideGuardian.yml");

        assertTrue(Files.exists(mobFile), "Mob file should exist");
        String content = Files.readString(mobFile, StandardCharsets.UTF_8);
        assertTrue(content.contains("RiptideGuardian"), "Extracted file should contain mob definition");
    }
}
