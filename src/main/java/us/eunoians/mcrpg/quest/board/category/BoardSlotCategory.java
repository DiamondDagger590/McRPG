package us.eunoians.mcrpg.quest.board.category;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * Immutable data class representing a slot category on the quest board.
 * <p>
 * Parsed from category YAML files in {@code quest-board/categories/}. Uses
 * {@link NamespacedKey} for all identifiers. The {@code refreshTypeKey} references
 * a registered {@link us.eunoians.mcrpg.quest.board.refresh.RefreshType}.
 */
public final class BoardSlotCategory {

    /**
     * How offerings in this category are generated and distributed to players.
     */
    public enum Visibility {
        /** All players see the same offerings. */
        SHARED,
        /** Each player gets unique offerings (Phase 2). */
        PERSONAL,
        /** Offerings are scoped to a group (e.g., land members) (Phase 3). */
        SCOPED
    }

    private final NamespacedKey key;
    private final Visibility visibility;
    private final NamespacedKey refreshTypeKey;
    private final Duration refreshInterval;
    private final Duration completionTime;
    private final NamespacedKey scopeProviderKey;
    private final int min;
    private final int max;
    private final double chancePerSlot;
    private final int priority;
    private final Duration appearanceCooldown;
    private final String requiredPermission;
    private final Integer maxActivePerEntity;

    public BoardSlotCategory(@NotNull NamespacedKey key,
                             @NotNull Visibility visibility,
                             @NotNull NamespacedKey refreshTypeKey,
                             @NotNull Duration refreshInterval,
                             @NotNull Duration completionTime,
                             @NotNull NamespacedKey scopeProviderKey,
                             int min,
                             int max,
                             double chancePerSlot,
                             int priority,
                             @Nullable Duration appearanceCooldown,
                             @Nullable String requiredPermission,
                             @Nullable Integer maxActivePerEntity) {
        this.key = key;
        this.visibility = visibility;
        this.refreshTypeKey = refreshTypeKey;
        this.refreshInterval = refreshInterval;
        this.completionTime = completionTime;
        this.scopeProviderKey = scopeProviderKey;
        this.min = min;
        this.max = max;
        this.chancePerSlot = chancePerSlot;
        this.priority = priority;
        this.appearanceCooldown = appearanceCooldown;
        this.requiredPermission = requiredPermission;
        this.maxActivePerEntity = maxActivePerEntity;
    }

    /**
     * Returns the unique identifier for this category.
     *
     * @return the namespaced key
     */
    @NotNull
    public NamespacedKey getKey() {
        return key;
    }

    /**
     * Returns how offerings in this category are generated and distributed to players.
     *
     * @return the visibility mode
     */
    @NotNull
    public Visibility getVisibility() {
        return visibility;
    }

    /**
     * Returns the key of the {@link us.eunoians.mcrpg.quest.board.refresh.RefreshType}
     * that governs when this category's offerings rotate.
     *
     * @return the refresh type key
     */
    @NotNull
    public NamespacedKey getRefreshTypeKey() {
        return refreshTypeKey;
    }

    /**
     * Returns the minimum interval between refreshes for this category.
     *
     * @return the refresh interval
     */
    @NotNull
    public Duration getRefreshInterval() {
        return refreshInterval;
    }

    /**
     * Returns the time limit players have to complete offerings from this category
     * after acceptance.
     *
     * @return the completion time window
     */
    @NotNull
    public Duration getCompletionTime() {
        return completionTime;
    }

    /**
     * Returns the key of the scope provider that determines offering visibility
     * for {@link Visibility#SCOPED} categories.
     *
     * @return the scope provider key
     */
    @NotNull
    public NamespacedKey getScopeProviderKey() {
        return scopeProviderKey;
    }

    /**
     * Returns the guaranteed minimum number of slots this category will occupy
     * on the board each rotation.
     *
     * @return the minimum slot count
     */
    public int getMin() {
        return min;
    }

    /**
     * Returns the maximum number of slots this category may occupy on the board.
     *
     * @return the maximum slot count
     */
    public int getMax() {
        return max;
    }

    /**
     * Returns the probability (0.0 to 1.0) that each additional slot beyond
     * {@link #getMin()} will be filled by this category.
     *
     * @return the per-slot chance
     */
    public double getChancePerSlot() {
        return chancePerSlot;
    }

    /**
     * Returns the priority order for slot allocation. Lower values are filled first
     * when multiple categories compete for remaining board space.
     *
     * @return the priority value
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Returns the cooldown duration between consecutive appearances of offerings
     * from this category, if configured.
     *
     * @return the appearance cooldown, or empty if none
     */
    @NotNull
    public Optional<Duration> getAppearanceCooldown() {
        return Optional.ofNullable(appearanceCooldown);
    }

    /**
     * Returns the permission node a player must have to see offerings from
     * this category, if one is configured.
     *
     * @return the required permission, or empty if unrestricted
     */
    @NotNull
    public Optional<String> getRequiredPermission() {
        return Optional.ofNullable(requiredPermission);
    }

    /**
     * Returns the maximum number of active quests from this category per scope entity,
     * if configured.
     *
     * @return the max active per entity, or empty if unlimited
     */
    @NotNull
    public OptionalInt getMaxActivePerEntity() {
        return maxActivePerEntity != null ? OptionalInt.of(maxActivePerEntity) : OptionalInt.empty();
    }
}
