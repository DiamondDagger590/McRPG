package us.eunoians.mcrpg.combat;

import com.diamonddagger590.mccore.util.TimeProvider;
import com.diamonddagger590.mccore.util.item.CustomEntityWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.McRPGBaseTest;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@DisplayName("CombatSession")
class CombatSessionTest extends McRPGBaseTest {

    private static final int MAX_MOB_PARTICIPANTS = 3;
    private static final long TIMEOUT_MILLIS = 8000L;

    private TimeProvider timeProvider;
    private UUID ownerUUID;

    @BeforeEach
    void setUp() {
        timeProvider = McRPG.getInstance().getTimeProvider();
        ownerUUID = UUID.randomUUID();
    }

    /**
     * Creates a new {@link CombatSession} with the test defaults.
     *
     * @return A new {@link CombatSession}.
     */
    private CombatSession createSession() {
        return new CombatSession(ownerUUID, MAX_MOB_PARTICIPANTS, TIMEOUT_MILLIS);
    }

    /**
     * Creates a player {@link CombatParticipant} with a random UUID.
     *
     * @return A new player {@link CombatParticipant}.
     */
    private CombatParticipant createPlayerParticipant() {
        return new CombatParticipant(UUID.randomUUID(), ParticipantType.PLAYER,
                new CustomEntityWrapper("PLAYER"), timeProvider.now().toEpochMilli());
    }

    /**
     * Creates a mob {@link CombatParticipant} with a random UUID.
     *
     * @return A new mob {@link CombatParticipant}.
     */
    private CombatParticipant createMobParticipant() {
        return new CombatParticipant(UUID.randomUUID(), ParticipantType.MOB,
                new CustomEntityWrapper("ZOMBIE"), timeProvider.now().toEpochMilli());
    }

    @DisplayName("New session starts with empty participant rosters")
    @Test
    void newSession_startsEmpty() {
        CombatSession session = createSession();

        assertTrue(session.getParticipants().isEmpty());
        assertTrue(session.getPlayerParticipants().isEmpty());
        assertTrue(session.getMobParticipants().isEmpty());
        assertTrue(session.isEmpty());
    }

    @Nested
    @DisplayName("getCombatType")
    class GetCombatType {

        @DisplayName("returns PVE when roster has only mob participants")
        @Test
        void getCombatType_returnsPVE_whenOnlyMobs() {
            CombatSession session = createSession();
            session.addParticipant(createMobParticipant());

            assertEquals(CombatType.PVE, session.getCombatType());
        }

        @DisplayName("returns PVP when roster has at least one player participant")
        @Test
        void getCombatType_returnsPVP_whenHasPlayerParticipant() {
            CombatSession session = createSession();
            session.addParticipant(createPlayerParticipant());

            assertEquals(CombatType.PVP, session.getCombatType());
        }

        @DisplayName("returns PVE when roster is empty")
        @Test
        void getCombatType_returnsPVE_whenEmpty() {
            CombatSession session = createSession();

            assertEquals(CombatType.PVE, session.getCombatType());
        }

        @DisplayName("transitions from PVE to PVP when a player participant is added")
        @Test
        void getCombatType_transitionsPVEToPVP_whenPlayerAdded() {
            CombatSession session = createSession();
            session.addParticipant(createMobParticipant());
            assertEquals(CombatType.PVE, session.getCombatType());

            session.addParticipant(createPlayerParticipant());
            assertEquals(CombatType.PVP, session.getCombatType());
        }

        @DisplayName("transitions from PVP to PVE when the last player participant is removed")
        @Test
        void getCombatType_transitionsPVPToPVE_whenLastPlayerRemoved() {
            CombatSession session = createSession();
            CombatParticipant player = createPlayerParticipant();
            session.addParticipant(player);
            session.addParticipant(createMobParticipant());
            assertEquals(CombatType.PVP, session.getCombatType());

            session.removeParticipant(player.getUUID());
            assertEquals(CombatType.PVE, session.getCombatType());
        }
    }

    @Nested
    @DisplayName("addParticipant")
    class AddParticipant {

        @DisplayName("PLAYER type adds to player map and returns empty")
        @Test
        void addParticipant_playerType_addsToPlayerMap() {
            CombatSession session = createSession();
            CombatParticipant player = createPlayerParticipant();

            Optional<CombatParticipant> evicted = session.addParticipant(player);

            assertTrue(evicted.isEmpty());
            assertEquals(1, session.getPlayerParticipants().size());
            assertTrue(session.hasParticipant(player.getUUID()));
        }

        @DisplayName("MOB type adds to mob queue and returns empty when queue is not full")
        @Test
        void addParticipant_mobType_addsToMobQueue_whenNotFull() {
            CombatSession session = createSession();
            CombatParticipant mob = createMobParticipant();

            Optional<CombatParticipant> evicted = session.addParticipant(mob);

            assertTrue(evicted.isEmpty());
            assertEquals(1, session.getMobParticipants().size());
            assertTrue(session.hasParticipant(mob.getUUID()));
        }

        @DisplayName("MOB type evicts oldest mob when queue is at capacity")
        @Test
        void addParticipant_mobType_evictsOldest_whenQueueFull() {
            CombatSession session = createSession();

            CombatParticipant mob1 = createMobParticipant();
            CombatParticipant mob2 = createMobParticipant();
            CombatParticipant mob3 = createMobParticipant();
            session.addParticipant(mob1);
            session.addParticipant(mob2);
            session.addParticipant(mob3);

            CombatParticipant mob4 = createMobParticipant();
            Optional<CombatParticipant> evicted = session.addParticipant(mob4);

            assertTrue(evicted.isPresent());
            assertEquals(MAX_MOB_PARTICIPANTS, session.getMobParticipants().size());
            assertTrue(session.hasParticipant(mob4.getUUID()));
        }

        @DisplayName("Evicted mob is the first mob added (FIFO order)")
        @Test
        void addParticipant_evictedMobIsFirstAdded() {
            CombatSession session = createSession();

            CombatParticipant mob1 = createMobParticipant();
            CombatParticipant mob2 = createMobParticipant();
            CombatParticipant mob3 = createMobParticipant();
            session.addParticipant(mob1);
            session.addParticipant(mob2);
            session.addParticipant(mob3);

            CombatParticipant mob4 = createMobParticipant();
            Optional<CombatParticipant> evicted = session.addParticipant(mob4);

            assertTrue(evicted.isPresent());
            assertEquals(mob1.getUUID(), evicted.get().getUUID());
            assertFalse(session.hasParticipant(mob1.getUUID()));
        }

        @DisplayName("MOB add does not throw when max mob participants is zero")
        @Test
        void addParticipant_mobType_doesNotThrow_whenMaxIsZero() {
            CombatSession session = new CombatSession(ownerUUID, 0, TIMEOUT_MILLIS);
            CombatParticipant mob = createMobParticipant();

            // The eviction guard must not call removeFirst() on an empty queue (NoSuchElementException).
            assertDoesNotThrow(() -> session.addParticipant(mob));
            assertTrue(session.hasParticipant(mob.getUUID()));
        }

        @DisplayName("Re-adding the same mob does not create a duplicate or evict")
        @Test
        void addParticipant_mobType_deduplicates_whenAddedTwice() {
            CombatSession session = createSession();
            CombatParticipant mob = createMobParticipant();

            session.addParticipant(mob);
            Optional<CombatParticipant> evicted = session.addParticipant(mob);

            assertTrue(evicted.isEmpty());
            assertEquals(1, session.getMobParticipants().size());
            assertTrue(session.hasParticipant(mob.getUUID()));
            // A single removal fully removes the mob (no lingering duplicate entry).
            session.removeParticipant(mob.getUUID());
            assertFalse(session.hasParticipant(mob.getUUID()));
        }
    }

    @Nested
    @DisplayName("removeParticipant")
    class RemoveParticipant {

        @DisplayName("removes a player participant by UUID")
        @Test
        void removeParticipant_removesPlayerByUUID() {
            CombatSession session = createSession();
            CombatParticipant player = createPlayerParticipant();
            session.addParticipant(player);

            Optional<CombatParticipant> removed = session.removeParticipant(player.getUUID());

            assertTrue(removed.isPresent());
            assertEquals(player.getUUID(), removed.get().getUUID());
            assertFalse(session.hasParticipant(player.getUUID()));
        }

        @DisplayName("removes a mob participant by UUID")
        @Test
        void removeParticipant_removesMobByUUID() {
            CombatSession session = createSession();
            CombatParticipant mob = createMobParticipant();
            session.addParticipant(mob);

            Optional<CombatParticipant> removed = session.removeParticipant(mob.getUUID());

            assertTrue(removed.isPresent());
            assertEquals(mob.getUUID(), removed.get().getUUID());
            assertFalse(session.hasParticipant(mob.getUUID()));
        }

        @DisplayName("returns empty when UUID is not in the roster")
        @Test
        void removeParticipant_returnsEmpty_whenUUIDNotFound() {
            CombatSession session = createSession();

            Optional<CombatParticipant> removed = session.removeParticipant(UUID.randomUUID());

            assertTrue(removed.isEmpty());
        }
    }

    @Nested
    @DisplayName("hasParticipant")
    class HasParticipant {

        @DisplayName("returns true for a player in the roster")
        @Test
        void hasParticipant_returnsTrue_forPlayer() {
            CombatSession session = createSession();
            CombatParticipant player = createPlayerParticipant();
            session.addParticipant(player);

            assertTrue(session.hasParticipant(player.getUUID()));
        }

        @DisplayName("returns true for a mob in the roster")
        @Test
        void hasParticipant_returnsTrue_forMob() {
            CombatSession session = createSession();
            CombatParticipant mob = createMobParticipant();
            session.addParticipant(mob);

            assertTrue(session.hasParticipant(mob.getUUID()));
        }

        @DisplayName("returns false for an unknown UUID")
        @Test
        void hasParticipant_returnsFalse_forUnknownUUID() {
            CombatSession session = createSession();

            assertFalse(session.hasParticipant(UUID.randomUUID()));
        }
    }

    @Nested
    @DisplayName("getParticipant")
    class GetParticipant {

        @DisplayName("returns the participant for a known UUID")
        @Test
        void getParticipant_returnsParticipant_forKnownUUID() {
            CombatSession session = createSession();
            CombatParticipant mob = createMobParticipant();
            session.addParticipant(mob);

            Optional<CombatParticipant> found = session.getParticipant(mob.getUUID());

            assertTrue(found.isPresent());
            assertEquals(mob.getUUID(), found.get().getUUID());
        }

        @DisplayName("returns empty for an unknown UUID")
        @Test
        void getParticipant_returnsEmpty_forUnknownUUID() {
            CombatSession session = createSession();

            Optional<CombatParticipant> found = session.getParticipant(UUID.randomUUID());

            assertTrue(found.isEmpty());
        }
    }

    @Nested
    @DisplayName("getParticipants / getPlayerParticipants / getMobParticipants")
    class ParticipantCollections {

        @DisplayName("getParticipants returns all player and mob participants")
        @Test
        void getParticipants_returnsAll() {
            CombatSession session = createSession();
            CombatParticipant player = createPlayerParticipant();
            CombatParticipant mob = createMobParticipant();
            session.addParticipant(player);
            session.addParticipant(mob);

            Collection<CombatParticipant> all = session.getParticipants();

            assertEquals(2, all.size());
        }

        @DisplayName("getPlayerParticipants returns only player participants")
        @Test
        void getPlayerParticipants_returnsOnlyPlayers() {
            CombatSession session = createSession();
            CombatParticipant player = createPlayerParticipant();
            CombatParticipant mob = createMobParticipant();
            session.addParticipant(player);
            session.addParticipant(mob);

            Collection<CombatParticipant> players = session.getPlayerParticipants();

            assertEquals(1, players.size());
            assertTrue(players.stream().allMatch(p -> p.getParticipantType() == ParticipantType.PLAYER));
        }

        @DisplayName("getMobParticipants returns only mob participants")
        @Test
        void getMobParticipants_returnsOnlyMobs() {
            CombatSession session = createSession();
            CombatParticipant player = createPlayerParticipant();
            CombatParticipant mob = createMobParticipant();
            session.addParticipant(player);
            session.addParticipant(mob);

            Collection<CombatParticipant> mobs = session.getMobParticipants();

            assertEquals(1, mobs.size());
            assertTrue(mobs.stream().allMatch(p -> p.getParticipantType() == ParticipantType.MOB));
        }
    }

    @Nested
    @DisplayName("recordActivity")
    class RecordActivity {

        @DisplayName("updates lastActivityMillis")
        @Test
        void recordActivity_updatesLastActivityMillis() {
            CombatSession session = createSession();
            long initialActivity = session.getLastActivityMillis();

            Instant futureInstant = Instant.ofEpochMilli(initialActivity + 5000L);
            when(timeProvider.now()).thenReturn(futureInstant);

            session.recordActivity();

            assertEquals(futureInstant.toEpochMilli(), session.getLastActivityMillis());
        }
    }

    @Nested
    @DisplayName("recordParticipantInteraction")
    class RecordParticipantInteraction {

        @DisplayName("updates both session lastActivity and participant lastInteraction")
        @Test
        void recordParticipantInteraction_updatesBothTimestamps() {
            CombatSession session = createSession();
            CombatParticipant mob = createMobParticipant();
            session.addParticipant(mob);

            long initialActivity = session.getLastActivityMillis();
            Instant futureInstant = Instant.ofEpochMilli(initialActivity + 3000L);
            when(timeProvider.now()).thenReturn(futureInstant);

            session.recordParticipantInteraction(mob.getUUID());

            assertEquals(futureInstant.toEpochMilli(), session.getLastActivityMillis());
            assertEquals(futureInstant.toEpochMilli(),
                    session.getParticipant(mob.getUUID()).orElseThrow().getLastInteractionMillis());
        }
    }

    @Nested
    @DisplayName("isTimedOut")
    class IsTimedOut {

        @DisplayName("returns false when within timeout window")
        @Test
        void isTimedOut_returnsFalse_whenWithinWindow() {
            CombatSession session = createSession();

            assertFalse(session.isTimedOut());
        }

        @DisplayName("returns true when past timeout window")
        @Test
        void isTimedOut_returnsTrue_whenPastWindow() {
            CombatSession session = createSession();
            long startMillis = session.getStartTimeMillis();

            Instant futureInstant = Instant.ofEpochMilli(startMillis + TIMEOUT_MILLIS + 1);
            when(timeProvider.now()).thenReturn(futureInstant);

            assertTrue(session.isTimedOut());
        }
    }

    @Nested
    @DisplayName("getTimedOutParticipants")
    class GetTimedOutParticipants {

        @DisplayName("returns only participants past the timeout threshold")
        @Test
        void getTimedOutParticipants_returnsOnlyTimedOut() {
            CombatSession session = createSession();
            long startMillis = session.getStartTimeMillis();

            CombatParticipant oldMob = new CombatParticipant(UUID.randomUUID(), ParticipantType.MOB,
                    new CustomEntityWrapper("ZOMBIE"), startMillis);
            session.addParticipant(oldMob);

            Instant futureInstant = Instant.ofEpochMilli(startMillis + TIMEOUT_MILLIS + 1);
            when(timeProvider.now()).thenReturn(futureInstant);

            CombatParticipant freshMob = new CombatParticipant(UUID.randomUUID(), ParticipantType.MOB,
                    new CustomEntityWrapper("SKELETON"), futureInstant.toEpochMilli());
            session.addParticipant(freshMob);

            List<CombatParticipant> timedOut = session.getTimedOutParticipants();

            assertEquals(1, timedOut.size());
            assertEquals(oldMob.getUUID(), timedOut.get(0).getUUID());
        }

        @DisplayName("returns empty list when all participants are fresh")
        @Test
        void getTimedOutParticipants_returnsEmpty_whenAllFresh() {
            CombatSession session = createSession();
            session.addParticipant(createMobParticipant());
            session.addParticipant(createPlayerParticipant());

            List<CombatParticipant> timedOut = session.getTimedOutParticipants();

            assertTrue(timedOut.isEmpty());
        }
    }

    @Nested
    @DisplayName("isEmpty")
    class IsEmpty {

        @DisplayName("returns true when no participants exist")
        @Test
        void isEmpty_returnsTrue_whenNoParticipants() {
            CombatSession session = createSession();

            assertTrue(session.isEmpty());
        }

        @DisplayName("returns false when player participants exist")
        @Test
        void isEmpty_returnsFalse_whenPlayerParticipantsExist() {
            CombatSession session = createSession();
            session.addParticipant(createPlayerParticipant());

            assertFalse(session.isEmpty());
        }

        @DisplayName("returns false when mob participants exist")
        @Test
        void isEmpty_returnsFalse_whenMobParticipantsExist() {
            CombatSession session = createSession();
            session.addParticipant(createMobParticipant());

            assertFalse(session.isEmpty());
        }
    }

    @DisplayName("getDurationMillis returns elapsed time since construction")
    @Test
    void getDurationMillis_returnsElapsedTime() {
        CombatSession session = createSession();
        long startMillis = session.getStartTimeMillis();
        long elapsedMillis = 5000L;

        Instant futureInstant = Instant.ofEpochMilli(startMillis + elapsedMillis);
        when(timeProvider.now()).thenReturn(futureInstant);

        assertEquals(elapsedMillis, session.getDurationMillis());
    }
}
