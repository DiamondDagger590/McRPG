package us.eunoians.mcrpg.combat.log;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.combat.CombatType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Immutable audit trail entry for a combat log incident. The {@code id} field is
 * populated by the database on insert (auto-increment); callers creating entries
 * for insertion pass {@code 0} as the id.
 *
 * @param id                 The auto-increment primary key (0 for new entries).
 * @param playerUUID         The UUID of the player who combat logged.
 * @param timestamp          When the logout occurred.
 * @param world              The world name at time of logout.
 * @param x                  X coordinate at time of logout.
 * @param y                  Y coordinate at time of logout.
 * @param z                  Z coordinate at time of logout.
 * @param combatType         The derived session type at time of logout.
 * @param participantUUIDs   UUIDs of participants in the session at time of logout.
 * @param punishmentsApplied Punishment types that were applied.
 */
public record CombatLogEntry(
        long id,
        @NotNull UUID playerUUID,
        @NotNull Instant timestamp,
        @NotNull String world,
        double x,
        double y,
        double z,
        @NotNull CombatType combatType,
        @NotNull List<UUID> participantUUIDs,
        @NotNull List<CombatLogPunishmentType> punishmentsApplied
) {

    /**
     * Compact constructor — defensively copies the mutable lists.
     */
    public CombatLogEntry {
        participantUUIDs = List.copyOf(participantUUIDs);
        punishmentsApplied = List.copyOf(punishmentsApplied);
    }
}
