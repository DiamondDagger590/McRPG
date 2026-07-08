package us.eunoians.mcrpg.database.table.quest;

import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.UUID;

/**
 * A lightweight DTO representing a single entry in the quest completion log.
 *
 * @param definitionKey the quest definition key (e.g. {@code "mcrpg:daily_mining"})
 * @param questUUID     the specific quest instance UUID that was completed
 * @param completedAt   the completion timestamp
 */
public record CompletionRecord(@NotNull String definitionKey,
                                @NotNull UUID questUUID,
                                @NotNull Instant completedAt) {
}
