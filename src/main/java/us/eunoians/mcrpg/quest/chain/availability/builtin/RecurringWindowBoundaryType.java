package us.eunoians.mcrpg.quest.chain.availability.builtin;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.chain.availability.WindowBoundary;
import us.eunoians.mcrpg.quest.chain.availability.WindowBoundaryType;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.io.File;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Built-in boundary type for yearly recurring dates. Parses a {@code month-day}
 * field (ISO-8601 format with {@code --} prefix, e.g., {@code --12-01}) and a
 * {@code time} field (e.g., {@code 00:00:00}).
 */
public class RecurringWindowBoundaryType implements WindowBoundaryType {

    /** Type key: {@code mcrpg:recurring}. */
    public static final NamespacedKey KEY =
            new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "recurring");

    @Override
    @NotNull
    public NamespacedKey getKey() {
        return KEY;
    }

    @Override
    @NotNull
    public Optional<WindowBoundary> parse(@NotNull Section section, @NotNull File file,
                                          @NotNull Logger logger) {
        String monthDayStr = section.getString("month-day");
        String timeStr = section.getString("time");
        if (monthDayStr == null || timeStr == null) {
            logger.warning("[AvailabilityConfig] Recurring boundary in " + file.getName()
                    + " is missing 'month-day' or 'time' field — skipping window");
            return Optional.empty();
        }
        try {
            MonthDay monthDay = MonthDay.parse(monthDayStr);
            LocalTime time = LocalTime.parse(timeStr);
            return Optional.of(new WindowBoundary.Recurring(monthDay, time));
        } catch (DateTimeParseException e) {
            logger.warning("[AvailabilityConfig] Invalid recurring boundary in "
                    + file.getName() + ": " + e.getMessage());
            return Optional.empty();
        }
    }
}
