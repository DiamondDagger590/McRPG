package us.eunoians.mcrpg.quest.board.category;

import com.diamonddagger590.mccore.registry.Registry;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Registry for {@link BoardSlotCategory} instances.
 * <p>
 * Loaded from {@code quest-board/categories/} directory. Supports dual-source registration
 * (config-loaded + expansion-registered) with {@link #replaceConfigCategories} for reload.
 */
public class BoardSlotCategoryRegistry implements Registry<BoardSlotCategory> {

    private final Map<NamespacedKey, BoardSlotCategory> categories = new LinkedHashMap<>();
    private final Set<NamespacedKey> configLoadedKeys = new HashSet<>();

    /**
     * Registers a category. If a category with the same key already exists, it is replaced.
     *
     * @param category the category to register
     */
    public void register(@NotNull BoardSlotCategory category) {
        categories.put(category.getKey(), category);
    }

    /**
     * Gets a registered category by its key.
     *
     * @param key the namespaced key
     * @return the category, or empty if not registered
     */
    @NotNull
    public Optional<BoardSlotCategory> get(@NotNull NamespacedKey key) {
        return Optional.ofNullable(categories.get(key));
    }

    /**
     * Gets all registered categories.
     *
     * @return an unmodifiable collection of all categories
     */
    @NotNull
    public Collection<BoardSlotCategory> getAll() {
        return Set.copyOf(categories.values());
    }

    /**
     * Gets all categories sorted by priority (descending — higher priority first).
     *
     * @return a sorted list of categories
     */
    @NotNull
    public List<BoardSlotCategory> getAllByPriority() {
        return categories.values().stream()
                .sorted(Comparator.comparingInt(BoardSlotCategory::getPriority).reversed())
                .toList();
    }

    /**
     * Gets all categories with the specified visibility.
     *
     * @param visibility the visibility filter
     * @return a filtered list of categories
     */
    @NotNull
    public List<BoardSlotCategory> getByVisibility(@NotNull BoardSlotCategory.Visibility visibility) {
        return categories.values().stream()
                .filter(c -> c.getVisibility() == visibility)
                .toList();
    }

    /**
     * Replaces config-loaded categories with a fresh set. Expansion-registered categories
     * are untouched.
     *
     * @param freshConfig the new config-loaded categories
     */
    public void replaceConfigCategories(@NotNull Map<NamespacedKey, BoardSlotCategory> freshConfig) {
        configLoadedKeys.forEach(categories::remove);
        configLoadedKeys.clear();
        freshConfig.forEach((key, category) -> {
            categories.put(key, category);
            configLoadedKeys.add(key);
        });
    }

    /**
     * Clears all registered categories (config-loaded and expansion-registered).
     */
    public void clear() {
        categories.clear();
        configLoadedKeys.clear();
    }

    @Override
    public boolean registered(@NotNull BoardSlotCategory category) {
        return categories.containsKey(category.getKey());
    }
}
