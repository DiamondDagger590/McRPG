package us.eunoians.mcrpg.quest.chain.availability.builtin;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.chain.availability.WindowBoundary;
import us.eunoians.mcrpg.quest.chain.availability.WindowBoundaryType;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Built-in boundary type for one-time fixed dates. Parses a {@code date} field
 * as an ISO-8601 local date-time (e.g., {@code 2026-06-01T00:00:00}).
 */
public class FixedWindowBoundaryType implements WindowBoundaryType {

    /** Type key: {@code mcrpg:fixed}. */
    public static final NamespacedKey KEY =
            new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "fixed");

    @Override
    @NotNull
    public NamespacedKey getKey() {
        return KEY;
    }

    /**
     * {@inheritDoc}
     *
     * @return empty — this is a built-in boundary type not provided by an expansion
     */
    @Override
    @NotNull
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.empty();
    }

    @Override
    @NotNull
    public Optional<WindowBoundary> parse(@NotNull Section section, @NotNull File file,
                                          @NotNull Logger logger) {
        String dateStr = section.getString("date");
        if (dateStr == null) {
            logger.warning("[AvailabilityConfig] Fixed boundary in " + file.getName()
                    + " is missing 'date' field — skipping window");
            return Optional.empty();
        }
        try {
            LocalDateTime dateTime = LocalDateTime.parse(dateStr);
            return Optional.of(new WindowBoundary.Fixed(dateTime));
        } catch (DateTimeParseException e) {
            logger.warning("[AvailabilityConfig] Invalid date '" + dateStr + "' in "
                    + file.getName() + " — expected ISO-8601 format (e.g., 2026-06-01T00:00:00)");
            return Optional.empty();
        }
    }
}
