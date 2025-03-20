package us.eunoians.mcrpg.external.papi.placeholder;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An abstraction for a PAPI placeholder.
 */
public abstract class McRPGPlaceholder {

    private final String identifier;

    public McRPGPlaceholder(@NotNull String identifier) {
        this.identifier = identifier;
    }

    /**
     * The identifier (minus the mcrpg_ prefix) of this placeholder.
     *
     * @return The identifier of this placeholder.
     */
    @NotNull
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Parses this placeholder for the given {@link OfflinePlayer}.
     *
     * @param offlinePlayer The {@link OfflinePlayer} to parse for.
     * @return The message to replace the placeholder with, or {@code null} if no replacement
     * should happen.
     */
    @Nullable
    public abstract String parsePlaceholder(@NotNull OfflinePlayer offlinePlayer);
}
