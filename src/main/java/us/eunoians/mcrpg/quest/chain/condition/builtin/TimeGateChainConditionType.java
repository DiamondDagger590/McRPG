package us.eunoians.mcrpg.quest.chain.condition.builtin;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.chain.QuestChainStartCondition;
import us.eunoians.mcrpg.quest.chain.condition.QuestChainStartConditionType;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Optional;

/**
 * Built-in condition type that gates chain starts on a calendar time threshold.
 * <p>
 * Parses the {@code after} field as an ISO-8601 {@link LocalDateTime} and an optional
 * {@code timezone} field (defaults to {@link ZoneId#systemDefault()}). Produces a
 * {@link TimeGateCondition} that passes only when the current time is at or after the
 * configured boundary.
 *
 * <p><b>YAML example:</b>
 * <pre>
 * type: "mcrpg:time_gate"
 * after: "2026-07-01T00:00:00"
 * timezone: "America/New_York"
 * </pre>
 *
 * @see TimeGateCondition
 */
public class TimeGateChainConditionType implements QuestChainStartConditionType {

    /** Type key: {@code mcrpg:time_gate}. */
    public static final NamespacedKey KEY =
            new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "time_gate");

    @Override
    @NotNull
    public NamespacedKey getKey() {
        return KEY;
    }

    /**
     * {@inheritDoc}
     *
     * @return empty — this is a built-in condition type not provided by an expansion
     */
    @Override
    @NotNull
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.empty();
    }

    /**
     * Parses a {@link TimeGateCondition} from the given YAML section.
     * <p>
     * Required field: {@code after} (ISO-8601 local date-time string).
     * Optional field: {@code timezone} (IANA timezone ID; defaults to the system default).
     *
     * @param config the YAML section containing {@code after} and optionally {@code timezone}
     * @return the parsed {@link TimeGateCondition}
     * @throws IllegalArgumentException if the {@code after} field is missing or not a valid
     *                                  ISO-8601 date-time, or if the {@code timezone} field
     *                                  is not a valid IANA timezone ID
     */
    @Override
    @NotNull
    public QuestChainStartCondition parse(@NotNull Section config) {
        String afterStr = config.getString("after");
        if (afterStr == null) {
            throw new IllegalArgumentException("TimeGateChainConditionType requires an 'after' field");
        }

        LocalDateTime after = parseAfter(afterStr);
        ZoneId timezone = parseTimezone(config);

        return new TimeGateCondition(KEY, after, timezone);
    }

    /**
     * Parses the {@code after} string as an ISO-8601 {@link LocalDateTime}.
     *
     * @param afterStr the raw date-time string from YAML
     * @return the parsed local date-time
     * @throws IllegalArgumentException if the string is not a valid ISO-8601 date-time
     */
    @NotNull
    private LocalDateTime parseAfter(@NotNull String afterStr) {
        try {
            return LocalDateTime.parse(afterStr);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Invalid 'after' value '" + afterStr
                            + "' — expected ISO-8601 format (e.g., 2026-07-01T00:00:00)", e);
        }
    }

    /**
     * Parses the optional {@code timezone} field from the YAML section.
     *
     * @param config the YAML section that may contain a {@code timezone} field
     * @return the parsed {@link ZoneId}, or the system default if the field is absent
     * @throws IllegalArgumentException if the timezone string is present but not a valid
     *                                  IANA timezone ID
     */
    @NotNull
    private ZoneId parseTimezone(@NotNull Section config) {
        String timezoneStr = config.getString("timezone");
        if (timezoneStr == null) {
            return ZoneId.systemDefault();
        }
        try {
            return ZoneId.of(timezoneStr);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Invalid 'timezone' value '" + timezoneStr
                            + "' — expected a valid IANA timezone ID (e.g., America/New_York)", e);
        }
    }
}
