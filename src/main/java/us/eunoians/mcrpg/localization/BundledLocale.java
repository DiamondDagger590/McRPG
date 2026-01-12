package us.eunoians.mcrpg.localization;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

/**
 * Enumerates the bundled locale files that ship with McRPG.
 * <p>
 * Each bundled locale consists of a folder name and the list of locale files
 * that should be extracted from the JAR resources to the plugin's data folder.
 * <p>
 * Third-party plugins or server owners can add additional locales by placing
 * {@code .yml} files in the {@code plugins/McRPG/localization/<language>/} folder.
 * These files will be automatically discovered and loaded at startup.
 */
public enum BundledLocale {

    /**
     * The English locale files bundled with McRPG.
     */
    ENGLISH("english", "en.yml", "en_commands.yml", "en_gui.yml", "en_abilities.yml", "en_skills.yml"),
    ;

    private final String folderName;
    private final List<String> fileNames;

    /**
     * Creates a new bundled locale definition.
     *
     * @param folderName The name of the folder in the localization directory.
     * @param fileNames  The names of the locale files within that folder.
     */
    BundledLocale(@NotNull String folderName, @NotNull String... fileNames) {
        this.folderName = folderName;
        this.fileNames = Arrays.asList(fileNames);
    }

    /**
     * Gets the folder name where this locale's files are stored.
     *
     * @return The folder name (e.g., "english").
     */
    @NotNull
    public String getFolderName() {
        return folderName;
    }

    /**
     * Gets the list of file names for this locale.
     *
     * @return An unmodifiable list of file names.
     */
    @NotNull
    public List<String> getFileNames() {
        return fileNames;
    }

    /**
     * Finds a bundled locale by its folder name.
     *
     * @param folderName The folder name to search for.
     * @return The matching {@link BundledLocale}, or {@code null} if not found.
     */
    public static BundledLocale fromFolderName(@NotNull String folderName) {
        for (BundledLocale locale : values()) {
            if (locale.folderName.equalsIgnoreCase(folderName)) {
                return locale;
            }
        }
        return null;
    }
}
