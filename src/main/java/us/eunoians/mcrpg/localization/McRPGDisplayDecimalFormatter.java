package us.eunoians.mcrpg.localization;

import com.diamonddagger590.mccore.registry.RegistryKey;
import net.kyori.adventure.audience.Audience;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Locale-aware decimal formatting for player-facing UI (localized strings, GUIs, lore, PAPI, etc.).
 * <p>
 * Obtain the shared instance from {@link McRPGLocalizationManager#getDisplayDecimalFormatter()}; that manager
 * constructs this formatter and passes itself so that {@link McRPGPlayer} and {@link Audience} overloads can
 * resolve the format locale by taking the head of {@link McRPGLocalizationManager#getLocaleChain(McRPGPlayer)}.
 * <p>
 * Holds a cache of {@link NumberFormat} instances keyed by {@link Locale}. Because {@code NumberFormat} is mutable and
 * not thread-safe, each call sets minimum and maximum fraction digits on the shared instance and invokes
 * {@link NumberFormat#format(double)} while synchronized on that instance; there is no separate cache entry per digit
 * tuple.
 */
public final class McRPGDisplayDecimalFormatter {

    /**
     * Default minimum fraction digits for {@link #formatDisplayDecimal(Locale, double)} and overloads that omit
     * explicit digit bounds (paired with {@link #DEFAULT_DISPLAY_MAX_FRACTION_DIGITS}).
     */
    private static final int DEFAULT_DISPLAY_MIN_FRACTION_DIGITS = 1;

    /**
     * Default maximum fraction digits for {@link #formatDisplayDecimal(Locale, double)} and overloads that omit
     * explicit digit bounds (paired with {@link #DEFAULT_DISPLAY_MIN_FRACTION_DIGITS}).
     */
    private static final int DEFAULT_DISPLAY_MAX_FRACTION_DIGITS = 2;

    /**
     * Owning manager; {@code null} only for tests that exercise {@link Locale}-only overloads.
     */
    @Nullable
    private final McRPGLocalizationManager localizationManager;

    /**
     * Cached {@link NumberFormat} instances keyed by {@link Locale}. Fraction digit bounds are applied on each
     * format call inside {@code synchronized} on that instance (along with {@code format}) because {@link NumberFormat}
     * is not thread-safe and digit settings are mutable.
     */
    private final ConcurrentHashMap<Locale, NumberFormat> displayNumberFormatsByLocale = new ConcurrentHashMap<>();

    /**
     * Constructs a formatter not bound to a {@link McRPGLocalizationManager}. Only {@link Locale}-based
     * {@code formatDisplayDecimal} overloads are supported; {@link McRPGPlayer} / {@link Audience} overloads throw.
     * Intended for unit tests.
     */
    public McRPGDisplayDecimalFormatter() {
        this.localizationManager = null;
    }

    /**
     * Constructs the formatter owned by {@code localizationManager}. {@link McRPGLocalizationManager} must pass
     * {@code this} after {@code super(mcRPG)} completes.
     *
     * @param localizationManager the localization manager that resolves display locales for players and audiences
     */
    public McRPGDisplayDecimalFormatter(@NotNull McRPGLocalizationManager localizationManager) {
        this.localizationManager = localizationManager;
    }

    /**
     * Formats a value for player-facing UI: locale-specific grouping and decimal separators using the given
     * fraction-digit bounds. Reuses one {@link NumberFormat} per {@code locale}; applies digit settings and formats
     * under {@code synchronized} on that instance.
     *
     * @param locale              the locale whose conventions apply
     * @param value               the value to format
     * @param minFractionDigits   {@link NumberFormat#setMinimumFractionDigits(int)}
     * @param maxFractionDigits   {@link NumberFormat#setMaximumFractionDigits(int)}
     * @return the formatted string
     * @throws IllegalArgumentException if {@code minFractionDigits} or {@code maxFractionDigits} is negative, or if
     *                                  {@code minFractionDigits} is greater than {@code maxFractionDigits}
     */
    @NotNull
    public String formatDisplayDecimal(
            @NotNull Locale locale,
            double value,
            int minFractionDigits,
            int maxFractionDigits) {
        if (minFractionDigits < 0 || maxFractionDigits < 0) {
            throw new IllegalArgumentException("minFractionDigits and maxFractionDigits must be non-negative");
        }
        if (minFractionDigits > maxFractionDigits) {
            throw new IllegalArgumentException("minFractionDigits must not exceed maxFractionDigits");
        }
        NumberFormat numberFormat = displayNumberFormatsByLocale.computeIfAbsent(locale, NumberFormat::getInstance);
        synchronized (numberFormat) {
            numberFormat.setMinimumFractionDigits(minFractionDigits);
            numberFormat.setMaximumFractionDigits(maxFractionDigits);
            return numberFormat.format(value);
        }
    }

    /**
     * Formats a {@code double} for player-facing UI using {@link #DEFAULT_DISPLAY_MIN_FRACTION_DIGITS} and
     * {@link #DEFAULT_DISPLAY_MAX_FRACTION_DIGITS} as the fraction digit bounds. Delegates to
     * {@link #formatDisplayDecimal(Locale, double, int, int)} with those defaults so grouping and decimal separators
     * follow the given {@code locale}.
     *
     * @param locale the locale whose grouping and decimal conventions apply
     * @param value  the numeric value to format
     * @return a string suitable for insertion into localized messages, GUI placeholders, or similar surfaces
     */
    @NotNull
    public String formatDisplayDecimal(@NotNull Locale locale, double value) {
        return formatDisplayDecimal(locale, value, DEFAULT_DISPLAY_MIN_FRACTION_DIGITS, DEFAULT_DISPLAY_MAX_FRACTION_DIGITS);
    }

    /**
     * Formats a {@code float} for player-facing UI using the given {@code locale}, with caller-supplied fraction digit
     * bounds. The value is widened to {@code double} before formatting; behavior otherwise matches
     * {@link #formatDisplayDecimal(Locale, double, int, int)}.
     *
     * @param locale              the locale whose grouping and decimal conventions apply
     * @param value               the numeric value to format
     * @param minFractionDigits   {@link NumberFormat#setMinimumFractionDigits(int)}
     * @param maxFractionDigits   {@link NumberFormat#setMaximumFractionDigits(int)}
     * @return a string suitable for insertion into localized messages, GUI placeholders, or similar surfaces
     * @throws IllegalArgumentException if {@code minFractionDigits} or {@code maxFractionDigits} is negative, or if
     *                                  {@code minFractionDigits} is greater than {@code maxFractionDigits}
     */
    @NotNull
    public String formatDisplayDecimal(
            @NotNull Locale locale,
            float value,
            int minFractionDigits,
            int maxFractionDigits) {
        return formatDisplayDecimal(locale, (double) value, minFractionDigits, maxFractionDigits);
    }

    /**
     * Formats a {@code float} for player-facing UI using the given {@code locale}, with
     * {@link #DEFAULT_DISPLAY_MIN_FRACTION_DIGITS} and {@link #DEFAULT_DISPLAY_MAX_FRACTION_DIGITS} as the fraction digit
     * bounds. The value is widened to {@code double} before formatting.
     *
     * @param locale the locale whose grouping and decimal conventions apply
     * @param value  the numeric value to format
     * @return a string suitable for insertion into localized messages, GUI placeholders, or similar surfaces
     */
    @NotNull
    public String formatDisplayDecimal(@NotNull Locale locale, float value) {
        return formatDisplayDecimal(locale, (double) value);
    }

    /**
     * Formats a {@code double} for player-facing UI using only the <strong>first</strong> {@link Locale} from
     * {@link McRPGLocalizationManager#getLocaleChain(McRPGPlayer)} as the {@link NumberFormat} locale (separators,
     * grouping), with caller-supplied fraction digit bounds. Translation lookup for messages still walks the full
     * chain elsewhere; this method only uses the chain head for numeric typography.
     *
     * @param player              the player whose locale chain head supplies the format locale
     * @param value               the numeric value to format
     * @param minFractionDigits   {@link NumberFormat#setMinimumFractionDigits(int)}
     * @param maxFractionDigits   {@link NumberFormat#setMaximumFractionDigits(int)}
     * @return a string suitable for insertion into localized messages, GUI placeholders, or similar surfaces
     * @throws IllegalArgumentException if {@code minFractionDigits} or {@code maxFractionDigits} is negative, or if
     *                                  {@code minFractionDigits} is greater than {@code maxFractionDigits}
     * @throws IllegalStateException    if this formatter was constructed without a {@link McRPGLocalizationManager}
     */
    @NotNull
    public String formatDisplayDecimal(
            @NotNull McRPGPlayer player,
            double value,
            int minFractionDigits,
            int maxFractionDigits) {
        Locale locale = requireLocalizationManager().getLocaleChain(player).getNodeValue();
        return formatDisplayDecimal(locale, value, minFractionDigits, maxFractionDigits);
    }

    /**
     * Formats a {@code double} for player-facing UI using only the <strong>first</strong> {@link Locale} from
     * {@link McRPGLocalizationManager#getLocaleChain(McRPGPlayer)} as the {@link NumberFormat} locale (separators,
     * grouping), with default fraction digit bounds. Translation lookup for messages still walks the full chain
     * elsewhere; this overload applies the same chain-head rule as {@link #formatDisplayDecimal(McRPGPlayer, double, int, int)}
     * with default digit bounds.
     *
     * @param player the player whose locale chain head supplies the format locale
     * @param value  the numeric value to format
     * @return a string suitable for insertion into localized messages, GUI placeholders, or similar surfaces
     * @throws IllegalStateException if this formatter was constructed without a {@link McRPGLocalizationManager}
     */
    @NotNull
    public String formatDisplayDecimal(@NotNull McRPGPlayer player, double value) {
        Locale locale = requireLocalizationManager().getLocaleChain(player).getNodeValue();
        return formatDisplayDecimal(locale, value);
    }

    /**
     * Formats a {@code float} for player-facing UI using only the <strong>first</strong> {@link Locale} from
     * {@link McRPGLocalizationManager#getLocaleChain(McRPGPlayer)} as the {@link NumberFormat} locale, with
     * caller-supplied fraction digit bounds. The value is widened to {@code double} before formatting; behavior
     * otherwise matches {@link #formatDisplayDecimal(McRPGPlayer, double, int, int)}.
     *
     * @param player              the player whose locale chain head supplies the format locale
     * @param value               the numeric value to format
     * @param minFractionDigits   {@link NumberFormat#setMinimumFractionDigits(int)}
     * @param maxFractionDigits   {@link NumberFormat#setMaximumFractionDigits(int)}
     * @return a string suitable for insertion into localized messages, GUI placeholders, or similar surfaces
     * @throws IllegalArgumentException if {@code minFractionDigits} or {@code maxFractionDigits} is negative, or if
     *                                  {@code minFractionDigits} is greater than {@code maxFractionDigits}
     * @throws IllegalStateException    if this formatter was constructed without a {@link McRPGLocalizationManager}
     */
    @NotNull
    public String formatDisplayDecimal(
            @NotNull McRPGPlayer player,
            float value,
            int minFractionDigits,
            int maxFractionDigits) {
        return formatDisplayDecimal(player, (double) value, minFractionDigits, maxFractionDigits);
    }

    /**
     * Formats a {@code float} for player-facing UI using only the <strong>first</strong> {@link Locale} from
     * {@link McRPGLocalizationManager#getLocaleChain(McRPGPlayer)} as the {@link NumberFormat} locale, with default
     * fraction digit bounds. The value is widened to {@code double} before formatting.
     *
     * @param player the player whose locale chain head supplies the format locale
     * @param value  the numeric value to format
     * @return a string suitable for insertion into localized messages, GUI placeholders, or similar surfaces
     * @throws IllegalStateException if this formatter was constructed without a {@link McRPGLocalizationManager}
     */
    @NotNull
    public String formatDisplayDecimal(@NotNull McRPGPlayer player, float value) {
        return formatDisplayDecimal(player, (double) value);
    }

    /**
     * Formats a {@code double} for the message recipient identified by {@code messageAudience}, with caller-supplied
     * fraction digit bounds. When {@code messageAudience} is a {@link Player} whose {@link McRPGPlayer} is loaded in
     * the player manager, the format locale is the head of that player's locale chain; when the player is not loaded,
     * or when {@code messageAudience} is not a {@link Player}, the format locale is the first node of the server
     * default locale chain held in the owning manager.
     *
     * @param messageAudience     the audience that will read the message or UI containing this number
     * @param value               the numeric value to format
     * @param minFractionDigits   {@link NumberFormat#setMinimumFractionDigits(int)}
     * @param maxFractionDigits   {@link NumberFormat#setMaximumFractionDigits(int)}
     * @return a string suitable for insertion into localized messages, GUI placeholders, or similar surfaces
     * @throws IllegalArgumentException if {@code minFractionDigits} or {@code maxFractionDigits} is negative, or if
     *                                  {@code minFractionDigits} is greater than {@code maxFractionDigits}
     * @throws IllegalStateException    if this formatter was constructed without a {@link McRPGLocalizationManager}
     */
    @NotNull
    public String formatDisplayDecimal(
            @NotNull Audience messageAudience,
            double value,
            int minFractionDigits,
            int maxFractionDigits) {
        Locale locale = resolveAudienceLocale(messageAudience);
        return formatDisplayDecimal(locale, value, minFractionDigits, maxFractionDigits);
    }

    /**
     * Formats a {@code double} for the message recipient identified by {@code messageAudience}, using default fraction
     * digit bounds. When {@code messageAudience} is a {@link Player} with a loaded {@link McRPGPlayer}, the format
     * locale is the head of that player's locale chain; otherwise the first locale from the server default locale
     * chain is used.
     *
     * @param messageAudience the audience that will read the message or UI containing this number
     * @param value           the numeric value to format
     * @return a string suitable for insertion into localized messages, GUI placeholders, or similar surfaces
     * @throws IllegalStateException if this formatter was constructed without a {@link McRPGLocalizationManager}
     */
    @NotNull
    public String formatDisplayDecimal(@NotNull Audience messageAudience, double value) {
        Locale locale = resolveAudienceLocale(messageAudience);
        return formatDisplayDecimal(locale, value);
    }

    /**
     * Formats a {@code float} for the message recipient identified by {@code messageAudience}, with caller-supplied
     * fraction digit bounds. The value is widened to {@code double} before formatting. Resolution of format locale
     * matches {@link #formatDisplayDecimal(Audience, double, int, int)}.
     *
     * @param messageAudience     the audience that will read the message or UI containing this number
     * @param value               the numeric value to format
     * @param minFractionDigits   {@link NumberFormat#setMinimumFractionDigits(int)}
     * @param maxFractionDigits   {@link NumberFormat#setMaximumFractionDigits(int)}
     * @return a string suitable for insertion into localized messages, GUI placeholders, or similar surfaces
     * @throws IllegalArgumentException if {@code minFractionDigits} or {@code maxFractionDigits} is negative, or if
     *                                  {@code minFractionDigits} is greater than {@code maxFractionDigits}
     * @throws IllegalStateException    if this formatter was constructed without a {@link McRPGLocalizationManager}
     */
    @NotNull
    public String formatDisplayDecimal(
            @NotNull Audience messageAudience,
            float value,
            int minFractionDigits,
            int maxFractionDigits) {
        return formatDisplayDecimal(messageAudience, (double) value, minFractionDigits, maxFractionDigits);
    }

    /**
     * Formats a {@code float} for the message recipient identified by {@code messageAudience}, using default fraction
     * digit bounds. The value is widened to {@code double} before formatting.
     *
     * @param messageAudience the audience that will read the message or UI containing this number
     * @param value           the numeric value to format
     * @return a string suitable for insertion into localized messages, GUI placeholders, or similar surfaces
     * @throws IllegalStateException if this formatter was constructed without a {@link McRPGLocalizationManager}
     */
    @NotNull
    public String formatDisplayDecimal(@NotNull Audience messageAudience, float value) {
        return formatDisplayDecimal(messageAudience, (double) value);
    }

    /**
     * Resolves the format {@link Locale} for the given {@code messageAudience}. When the audience is a {@link Player}
     * whose {@link McRPGPlayer} is loaded, returns the head of that player's locale chain. Otherwise returns the
     * server default locale.
     *
     * @param messageAudience the audience that will read the formatted number
     * @return the locale to use for {@link NumberFormat}
     */
    @NotNull
    private Locale resolveAudienceLocale(@NotNull Audience messageAudience) {
        McRPGLocalizationManager manager = requireLocalizationManager();
        if (messageAudience instanceof Player player) {
            return manager.plugin().registryAccess().registry(RegistryKey.MANAGER)
                    .manager(McRPGManagerKey.PLAYER)
                    .getPlayer(player.getUniqueId())
                    .map(mcRPGPlayer -> manager.getLocaleChain(mcRPGPlayer).getNodeValue())
                    .orElseGet(manager::getServerDefaultLocale);
        }
        return manager.getServerDefaultLocale();
    }

    /**
     * Returns the owning localization manager when this formatter was constructed for production use.
     *
     * @return the manager that created this formatter
     * @throws IllegalStateException if this formatter was constructed without a {@link McRPGLocalizationManager}
     */
    @NotNull
    private McRPGLocalizationManager requireLocalizationManager() {
        return Objects.requireNonNull(
                localizationManager,
                "McRPGPlayer and Audience formatDisplayDecimal overloads require McRPGDisplayDecimalFormatter(McRPGLocalizationManager)");
    }
}
