package us.eunoians.mcrpg.quest.board.configuration;

import com.diamonddagger590.mccore.configuration.ReloadableContent;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.configuration.BoardCategoryConfigLoader;
import us.eunoians.mcrpg.configuration.file.BoardConfigFile;
import us.eunoians.mcrpg.quest.board.category.BoardSlotCategory;
import us.eunoians.mcrpg.quest.board.category.BoardSlotCategoryRegistry;

import java.io.File;
import java.util.Map;

/**
 * Custom {@link ReloadableContent} subclass that re-scans the {@code categories/} directory
 * and pushes the result into the {@link BoardSlotCategoryRegistry} on reload.
 * <p>
 * Uses {@link BoardConfigFile#MINIMUM_TOTAL_OFFERINGS} as the trigger route so the
 * reload fires when the board config is reloaded.
 */
public class ReloadableCategoryConfig extends ReloadableContent<Map<NamespacedKey, BoardSlotCategory>> {

    public ReloadableCategoryConfig(@NotNull YamlDocument boardConfig,
                                    @NotNull BoardSlotCategoryRegistry registry,
                                    @NotNull File categoriesDirectory) {
        super(boardConfig, BoardConfigFile.MINIMUM_TOTAL_OFFERINGS, (doc, route) -> {
            BoardCategoryConfigLoader loader = new BoardCategoryConfigLoader();
            Map<NamespacedKey, BoardSlotCategory> map = loader.loadCategoriesFromDirectory(categoriesDirectory);
            registry.replaceConfigCategories(map);
            return map;
        });
    }
}
