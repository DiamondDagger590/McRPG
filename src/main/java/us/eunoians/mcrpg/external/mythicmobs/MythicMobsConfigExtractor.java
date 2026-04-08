package us.eunoians.mcrpg.external.mythicmobs;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;

/**
 * Extracts bundled MythicMobs pack files from the McRPG JAR to the
 * MythicMobs Packs directory on first startup.
 * <p>
 * Files are only extracted if they do not already exist in the target
 * directory, preserving any server-owner customizations. The pack is
 * deployed to {@code plugins/MythicMobs/Packs/McRPG/}.
 */
public class MythicMobsConfigExtractor {

    /**
     * Bundled pack files (paths relative to the pack root).
     * Add new entries here when additional mobs or configs are shipped.
     */
    private static final List<String> BUNDLED_PACK_FILES = List.of(
            "Mobs/RiptideGuardian.yml",
            "Skills/RiptideGuardianSkills.yml",
            "DropTables/RiptideGuardianDrops.yml"
    );

    private static final String JAR_RESOURCE_PREFIX = "mythicmobs/Packs/McRPG/";

    private MythicMobsConfigExtractor() {
    }

    /**
     * Extracts all bundled pack files to the MythicMobs
     * {@code Packs/McRPG/} directory. Skips any file that already
     * exists on disk.
     *
     * @param plugin the McRPG plugin instance
     */
    public static void extractBundledConfigs(@NotNull McRPG plugin) {
        Path packRoot = plugin.getDataFolder().toPath()
                .getParent()  // plugins/
                .resolve("MythicMobs")
                .resolve("Packs")
                .resolve("McRPG");

        for (String relativePath : BUNDLED_PACK_FILES) {
            Path targetFile = packRoot.resolve(relativePath);

            if (Files.exists(targetFile)) {
                plugin.getLogger().info("MythicMobs pack file '"
                        + relativePath + "' already exists, skipping extraction.");
                continue;
            }

            // Ensure parent directories exist (e.g., Mobs/, Skills/, DropTables/)
            try {
                Files.createDirectories(targetFile.getParent());
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING,
                        "Could not create directory for MythicMobs pack file '"
                                + relativePath + "'", e);
                continue;
            }

            String resourcePath = JAR_RESOURCE_PREFIX + relativePath;
            try (InputStream resourceStream = plugin.getResource(resourcePath)) {
                if (resourceStream == null) {
                    plugin.getLogger().warning("Bundled MythicMobs pack file '"
                            + resourcePath + "' not found in JAR.");
                    continue;
                }
                Files.copy(resourceStream, targetFile);
                plugin.getLogger().info("Extracted MythicMobs pack file '"
                        + relativePath + "' to " + targetFile);
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING,
                        "Failed to extract MythicMobs pack file '"
                                + relativePath + "'", e);
            }
        }
    }
}
