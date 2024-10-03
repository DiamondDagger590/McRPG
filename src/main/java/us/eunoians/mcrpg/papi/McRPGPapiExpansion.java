package us.eunoians.mcrpg.papi;

import com.google.common.collect.ImmutableList;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.papi.placeholder.McRPGPlaceHolderType;
import us.eunoians.mcrpg.papi.placeholder.McRPGPlaceholder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class McRPGPapiExpansion extends PlaceholderExpansion {

    private final Map<String, McRPGPlaceholder> placeholders = new HashMap<>();

    public McRPGPapiExpansion(@NotNull McRPG mcRPG) {
        Arrays.stream(McRPGPlaceHolderType.values()).forEach(mcRPGPlaceHolderType -> mcRPGPlaceHolderType.registerPlaceholders(mcRPG, this));
    }

    @NotNull
    @Override
    public String getIdentifier() {
        return "mcrpg";
    }

    @NotNull
    @Override
    public String getAuthor() {
        return "DiamondDagger590";
    }

    @NotNull
    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @NotNull List<String> getPlaceholders() {
        return ImmutableList.copyOf(placeholders.keySet());
    }

    @Nullable
    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        params = params.toLowerCase(Locale.ROOT);
        if (placeholders.containsKey(params)) {
            return placeholders.get(params).parsePlaceholder(player);
        }
        return null;
    }

    // TODO support reloading these after new content packs are registered
    public void registerPlaceholder(@NotNull McRPGPlaceholder placeholder) {
        placeholders.put(placeholder.getIdentifier(), placeholder);
    }
}
