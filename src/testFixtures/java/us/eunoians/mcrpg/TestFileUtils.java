package us.eunoians.mcrpg;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Shared filesystem helpers for test classes that need to read from or write to temporary
 * directories. Prevents duplicating boilerplate across configuration and reload test classes.
 */
public final class TestFileUtils {

    private TestFileUtils() {}

    /**
     * Writes {@code content} to a file named {@code filename} inside {@code dir} and
     * registers it for deletion on JVM exit.
     *
     * @param dir      the directory to write into
     * @param filename the file name; must not contain path separators
     * @param content  the text to write
     * @return the path to the written file
     * @throws IOException if the write fails
     */
    public static Path writeFile(@NotNull Path dir, @NotNull String filename, @NotNull String content)
            throws IOException {
        Path path = dir.resolve(filename);
        Files.writeString(path, content);
        path.toFile().deleteOnExit();
        return path;
    }

    /**
     * Recursively deletes {@code file} and all its children. Ignores deletion failures —
     * files are also registered for JVM-exit deletion via {@link #writeFile}.
     *
     * @param file the file or directory to delete
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void deleteRecursively(@NotNull File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursively(child);
                }
            }
        }
        file.delete();
    }
}
