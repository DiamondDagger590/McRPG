package us.eunoians.mcrpg.combat.log;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.combat.CombatType;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("CombatLogEntry")
class CombatLogEntryTest {

    @Test
    @DisplayName("constructor stores all fields correctly")
    void constructor_storesAllFields() {
        UUID playerUUID = UUID.randomUUID();
        Instant timestamp = Instant.now();
        UUID participantUUID = UUID.randomUUID();
        List<UUID> participants = List.of(participantUUID);
        List<CombatLogPunishmentType> punishments = List.of(CombatLogPunishmentType.KILL_ON_LOGOUT);

        CombatLogEntry entry = new CombatLogEntry(1L, playerUUID, timestamp, "world", 1.0, 2.0, 3.0,
                CombatType.PVP, participants, punishments);

        assertEquals(1L, entry.id());
        assertEquals(playerUUID, entry.playerUUID());
        assertEquals(timestamp, entry.timestamp());
        assertEquals("world", entry.world());
        assertEquals(1.0, entry.x());
        assertEquals(2.0, entry.y());
        assertEquals(3.0, entry.z());
        assertEquals(CombatType.PVP, entry.combatType());
        assertEquals(participants, entry.participantUUIDs());
        assertEquals(punishments, entry.punishmentsApplied());
    }

    @Test
    @DisplayName("participantUUIDs returns an immutable list")
    void participantUUIDs_isImmutable() {
        List<UUID> mutableList = new ArrayList<>();
        mutableList.add(UUID.randomUUID());
        CombatLogEntry entry = new CombatLogEntry(0, UUID.randomUUID(), Instant.now(), "world",
                0, 0, 0, CombatType.PVE, mutableList, List.of());

        assertThrows(UnsupportedOperationException.class, () -> entry.participantUUIDs().add(UUID.randomUUID()));
    }

    @Test
    @DisplayName("punishmentsApplied returns an immutable list")
    void punishmentsApplied_isImmutable() {
        List<CombatLogPunishmentType> mutableList = new ArrayList<>();
        mutableList.add(CombatLogPunishmentType.DROP_ITEMS);
        CombatLogEntry entry = new CombatLogEntry(0, UUID.randomUUID(), Instant.now(), "world",
                0, 0, 0, CombatType.PVE, List.of(), mutableList);

        assertThrows(UnsupportedOperationException.class,
                () -> entry.punishmentsApplied().add(CombatLogPunishmentType.KILL_ON_LOGOUT));
    }

    @Test
    @DisplayName("record equality is based on all fields")
    void equality_basedOnAllFields() {
        UUID playerUUID = UUID.randomUUID();
        Instant timestamp = Instant.now();
        List<UUID> participants = List.of();
        List<CombatLogPunishmentType> punishments = List.of();

        CombatLogEntry first = new CombatLogEntry(1L, playerUUID, timestamp, "world", 1.0, 2.0, 3.0,
                CombatType.PVP, participants, punishments);
        CombatLogEntry second = new CombatLogEntry(1L, playerUUID, timestamp, "world", 1.0, 2.0, 3.0,
                CombatType.PVP, participants, punishments);

        assertEquals(first, second);
    }
}
