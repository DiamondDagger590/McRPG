package us.eunoians.mcrpg.configuration;

import com.diamonddagger590.mccore.util.Methods;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.quest.board.category.BoardSlotCategory;
import us.eunoians.mcrpg.quest.board.category.BoardSlotCategory.Visibility;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Loads {@link BoardSlotCategory} definitions from YAML files in the
 * {@code quest-board/categories/} directory.
 * <p>
 * Each top-level key in a category file represents one category. Plain-string keys
 * are auto-namespaced under {@code mcrpg:} (with dashes replaced by underscores and
 * lowercased).
 */
public final class BoardCategoryConfigLoader {

    private static final Logger LOGGER = McRPG.getInstance().getLogger();

    /**
     * Loads all category definitions from YAML files in the given directory.
     *
     * @param categoriesDirectory the directory containing category YAML files
     * @return a map of category key to category, in load order
     */
    @NotNull
    public Map<NamespacedKey, BoardSlotCategory> loadCategoriesFromDirectory(@NotNull File categoriesDirectory) {
        Map<NamespacedKey, BoardSlotCategory> result = new LinkedHashMap<>();

        if (!categoriesDirectory.isDirectory()) {
            LOGGER.warning("[QuestBoard] Categories directory does not exist: " + categoriesDirectory.getPath());
            return result;
        }

        File[] files = categoriesDirectory.listFiles((dir, name) ->
                name.endsWith(".yml") || name.endsWith(".yaml"));

        if (files == null || files.length == 0) {
            return result;
        }

        for (File file : files) {
            try {
                YamlDocument doc = YamlDocument.create(file,
                        GeneralSettings.builder().setKeyFormat(GeneralSettings.KeyFormat.STRING).build(),
                        LoaderSettings.DEFAULT);

                for (String rawKey : doc.getRoutesAsStrings(false)) {
                    Section section = doc.getSection(rawKey);
                    if (section == null) {
                        continue;
                    }

                    NamespacedKey categoryKey = autoNamespace(rawKey);
                    try {
                        BoardSlotCategory category = parseCategory(categoryKey, section);
                        result.put(categoryKey, category);
                    } catch (Exception e) {
                        LOGGER.warning("[QuestBoard] Failed to parse category '" + rawKey + "' in " + file.getName() + ": " + e.getMessage());
                    }
                }
            } catch (IOException e) {
                LOGGER.warning("[QuestBoard] Failed to load category file " + file.getName() + ": " + e.getMessage());
            }
        }

        return result;
    }

    @NotNull
    private BoardSlotCategory parseCategory(@NotNull NamespacedKey key, @NotNull Section section) {
        Visibility visibility = Visibility.valueOf(section.getString("visibility", "SHARED").toUpperCase());
        NamespacedKey refreshTypeKey = autoNamespace(section.getString("refresh-type", "daily"));

        Duration refreshInterval = Methods.getTimeInSeconds(section.getString("refresh-interval", "24h"));
        Duration completionTime = Methods.getTimeInSeconds(section.getString("completion-time", "24h"));

        NamespacedKey scopeProviderKey = autoNamespace(section.getString("scope-provider", "single_player"));

        int min = section.getInt("min", 0);
        int max = section.getInt("max", 3);
        double chancePerSlot = section.getDouble("chance-per-slot", 1.0);
        int priority = section.getInt("priority", 0);

        Duration appearanceCooldown = null;
        if (section.contains("appearance-cooldown")) {
            appearanceCooldown = Methods.getTimeInSeconds(section.getString("appearance-cooldown"));
        }

        String requiredPermission = section.contains("required-permission")
                ? section.getString("required-permission")
                : null;

        return new BoardSlotCategory(key, visibility, refreshTypeKey, refreshInterval, completionTime,
                scopeProviderKey, min, max, chancePerSlot, priority, appearanceCooldown, requiredPermission);
    }

    /**
     * Auto-namespaces a plain string key under {@code mcrpg:}, lowercasing and
     * replacing dashes with underscores. If the key already contains a colon,
     * it is parsed as-is.
     */
    @NotNull
    private static NamespacedKey autoNamespace(@NotNull String rawKey) {
        if (rawKey.contains(":")) {
            return NamespacedKey.fromString(rawKey.toLowerCase());
        }
        return new NamespacedKey(McRPGMethods.getMcRPGNamespace(), rawKey.toLowerCase().replace('-', '_'));
    }
}
