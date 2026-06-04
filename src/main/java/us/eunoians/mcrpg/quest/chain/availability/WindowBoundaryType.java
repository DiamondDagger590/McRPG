package us.eunoians.mcrpg.quest.chain.availability;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Extensible interface defining how a specific boundary type is deserialized
 * from a YAML config section. Registered in {@link WindowBoundaryTypeRegistry}.
 * Third-party plugins may implement custom boundary types for advanced
 * scheduling needs (e.g., cron-based or external-calendar-driven boundaries).
 */
public interface WindowBoundaryType {

    /**
     * Returns the unique key identifying this boundary type in config files.
     *
     * @return the boundary type key (e.g., {@code mcrpg:fixed})
     */
    @NotNull
    NamespacedKey getKey();

    /**
     * Parses a window boundary from a YAML config section.
     *
     * @param section the YAML section containing boundary configuration
     * @param file    source file for error reporting
     * @param logger  logger for warning messages
     * @return the parsed boundary, or empty if the section is invalid
     */
    @NotNull
    Optional<WindowBoundary> parse(@NotNull Section section, @NotNull File file,
                                   @NotNull Logger logger);
}
