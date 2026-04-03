package us.eunoians.mcrpg.expansion.content;

import com.diamonddagger590.mccore.statistic.Statistic;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Wraps a McCore {@link Statistic} definition as {@link McRPGContent} so that it can be
 * included in a {@link StatisticContentPack} and registered through the
 * {@link us.eunoians.mcrpg.expansion.ContentExpansion} system.
 * <p>
 * Composition is used rather than inheritance because {@link com.diamonddagger590.mccore.statistic.SimpleStatistic}
 * is a record and cannot be extended.
 */
public final class StatisticContent implements McRPGContent {

    private final Statistic statistic;
    private final @Nullable NamespacedKey expansionKey;

    /**
     * Creates a new {@link StatisticContent} wrapping the given statistic.
     *
     * @param statistic    The {@link Statistic} definition to wrap.
     * @param expansionKey The {@link NamespacedKey} of the owning
     *                     {@link us.eunoians.mcrpg.expansion.ContentExpansion}, or {@code null}
     *                     if this statistic is not tied to a specific expansion.
     */
    public StatisticContent(@NotNull Statistic statistic, @Nullable NamespacedKey expansionKey) {
        this.statistic = statistic;
        this.expansionKey = expansionKey;
    }

    /**
     * Gets the wrapped {@link Statistic} definition.
     *
     * @return The wrapped {@link Statistic}.
     */
    @NotNull
    public Statistic getStatistic() {
        return statistic;
    }

    /** {@inheritDoc} */
    @Override
    @NotNull
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.ofNullable(expansionKey);
    }
}
