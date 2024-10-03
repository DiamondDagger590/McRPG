package us.eunoians.mcrpg.papi.placeholder;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class McRPGPlaceholder {

    private final String identifier;

    public McRPGPlaceholder(@NotNull String identifier) {
        this.identifier = identifier;
    }

    @NotNull
    public String getIdentifier() {
        return identifier;
    }

    @Nullable
    public abstract String parsePlaceholder(@NotNull OfflinePlayer offlinePlayer);
}
