package us.eunoians.mcrpg.ability.unlock.builtin;

import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.route.Route;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.unlock.UnlockConditionParseException;
import us.eunoians.mcrpg.ability.unlock.UnlockConditionType;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.McRPGPlayerManager;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.Map;
import java.util.Optional;

/**
 * Unlock condition met when a player's {@link com.diamonddagger590.mccore.statistic.PlayerStatisticData}
 * value for {@link #statisticKey} is at or above {@link #threshold}.
 * <p>
 * Owners may supply a custom display string via {@code text} (inline MiniMessage) or
 * {@code locale-key} (translatable through the locale chain); otherwise the bundled default
 * template is used.
 */
public final class StatisticUnlockConditionType implements UnlockConditionType {

    public static final NamespacedKey KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "statistic");

    private final NamespacedKey statisticKey;
    private final long threshold;
    private final Route localeKey;
    private final String inlineText;

    public StatisticUnlockConditionType() {
        this(null, 0L, null, null);
    }

    public StatisticUnlockConditionType(@Nullable NamespacedKey statisticKey, long threshold,
                                        @Nullable Route localeKey, @Nullable String inlineText) {
        this.statisticKey = statisticKey;
        this.threshold = threshold;
        this.localeKey = localeKey;
        this.inlineText = inlineText;
    }

    @NotNull
    @Override
    public NamespacedKey getKey() {
        return KEY;
    }

    @NotNull
    @Override
    public UnlockConditionType parseConfig(@NotNull Section section) {
        NamespacedKey stat = McRPGMethods.parseNamespacedKey(section.getString("statistic"));
        if (stat == null) {
            throw new UnlockConditionParseException("mcrpg:statistic requires a 'statistic' key");
        }
        if (!section.contains("threshold")) {
            throw new UnlockConditionParseException("mcrpg:statistic requires a 'threshold' key");
        }
        boolean hasKey = section.contains("locale-key");
        boolean hasText = section.contains("text");
        if (hasKey && hasText) {
            throw new UnlockConditionParseException(
                    "mcrpg:statistic may set at most one of 'locale-key' or 'text'");
        }
        Route customKey = hasKey ? Route.fromString(section.getString("locale-key")) : null;
        String customText = hasText ? section.getString("text") : null;
        return new StatisticUnlockConditionType(stat, section.getLong("threshold"), customKey, customText);
    }

    @Override
    public boolean isMet(@NotNull AbilityHolder holder) {
        if (statisticKey == null) {
            return false;
        }
        return resolvePlayer(holder)
                .map(player -> player.getStatisticData().getLongValue(statisticKey).orElse(0L) >= threshold)
                .orElse(false);
    }

    @Override
    public double getProgress(@NotNull AbilityHolder holder) {
        if (statisticKey == null || threshold <= 0L) {
            return 0.0;
        }
        return resolvePlayer(holder)
                .map(player -> Math.min(1.0,
                        (double) player.getStatisticData().getLongValue(statisticKey).orElse(0L) / threshold))
                .orElse(0.0);
    }

    @NotNull
    @Override
    public Component getDisplayDescription(@NotNull McRPGPlayer player) {
        if (statisticKey == null) {
            return Component.empty();
        }
        McRPGLocalizationManager localization = McRPG.getInstance().registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
        var formatter = localization.getDisplayDecimalFormatter();
        long current = player.getStatisticData().getLongValue(statisticKey).orElse(0L);
        Map<String, String> placeholders = Map.of(
                "statistic", statisticKey.toString(),
                "required", formatter.formatDisplayDecimal(player, threshold),
                "current", formatter.formatDisplayDecimal(player, current));
        if (localeKey != null) {
            return localization.getLocalizedMessageAsComponent(player, localeKey, placeholders);
        }
        if (inlineText != null) {
            String resolved = localization.getLocalizedMessage(
                    localization.resolvePaletteColors(inlineText), placeholders);
            return McRPG.getInstance().getMiniMessage().deserialize(resolved);
        }
        return localization.getLocalizedMessageAsComponent(player,
                LocalizationKey.UNLOCK_CONDITION_STATISTIC_DESCRIPTION, placeholders);
    }

    /**
     * Statistic key this condition targets, or {@code null} on the unconfigured prototype.
     *
     * @return the statistic key, or {@code null}
     */
    @Nullable
    public NamespacedKey getStatisticKey() {
        return statisticKey;
    }

    /**
     * The threshold value the statistic must reach.
     *
     * @return the threshold
     */
    public long getThreshold() {
        return threshold;
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.of(McRPGExpansion.EXPANSION_KEY);
    }

    @NotNull
    private Optional<McRPGPlayer> resolvePlayer(@NotNull AbilityHolder holder) {
        McRPGPlayerManager playerManager = McRPG.getInstance().registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.PLAYER);
        return playerManager.getPlayer(holder.getUUID());
    }
}
