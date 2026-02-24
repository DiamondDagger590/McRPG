package us.eunoians.mcrpg.util;

import org.jetbrains.annotations.NotNull;

import java.util.OptionalInt;
import java.util.Set;

/**
 * Utility for extracting the highest numeric suffix from a set of permission nodes.
 * <p>
 * Used to determine bonus slot counts from permission-based values like
 * {@code mcrpg.board.extra-slots.1}, {@code mcrpg.board.extra-slots.5}, etc.
 */
public final class PermissionNumberParser {

    private PermissionNumberParser() {
    }

    /**
     * Finds the highest numeric suffix among permissions that start with the given prefix.
     * <p>
     * For example, given permissions {@code ["mcrpg.board.extra.2", "mcrpg.board.extra.5"]}
     * and prefix {@code "mcrpg.board.extra."}, returns {@code OptionalInt.of(5)}.
     *
     * @param permissions the set of permission strings to search
     * @param prefix      the prefix to match (including trailing dot)
     * @return the highest numeric suffix, or empty if none found
     */
    @NotNull
    public static OptionalInt getHighestNumericSuffix(@NotNull Set<String> permissions,
                                                      @NotNull String prefix) {
        int highest = Integer.MIN_VALUE;
        boolean found = false;

        for (String permission : permissions) {
            if (!permission.startsWith(prefix)) {
                continue;
            }
            String suffix = permission.substring(prefix.length());
            try {
                int value = Integer.parseInt(suffix);
                if (value > highest) {
                    highest = value;
                    found = true;
                }
            } catch (NumberFormatException ignored) {
            }
        }

        return found ? OptionalInt.of(highest) : OptionalInt.empty();
    }
}
