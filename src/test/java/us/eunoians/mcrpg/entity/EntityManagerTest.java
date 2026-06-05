package us.eunoians.mcrpg.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.holder.QuestHolder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EntityManagerTest extends McRPGBaseTest {

    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        entityManager = new EntityManager(mcRPG);
    }

    @Nested
    @DisplayName("AbilityHolder tracking")
    class AbilityHolderTracking {

        @Test
        @DisplayName("getAbilityHolder returns empty for untracked UUID")
        void getAbilityHolder_returnsEmpty_whenNotTracked() {
            Optional<AbilityHolder> result = entityManager.getAbilityHolder(UUID.randomUUID());
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("trackAbilityHolder makes holder retrievable")
        void trackAbilityHolder_makesHolderRetrievable() {
            UUID uuid = UUID.randomUUID();
            AbilityHolder holder = new AbilityHolder(mcRPG, uuid);
            entityManager.trackAbilityHolder(holder);

            Optional<AbilityHolder> result = entityManager.getAbilityHolder(uuid);
            assertTrue(result.isPresent());
            assertSame(holder, result.get());
        }

        @Test
        @DisplayName("isAbilityHolderTracked returns true for tracked holder")
        void isAbilityHolderTracked_returnsTrue_whenTracked() {
            UUID uuid = UUID.randomUUID();
            AbilityHolder holder = new AbilityHolder(mcRPG, uuid);
            entityManager.trackAbilityHolder(holder);

            assertTrue(entityManager.isAbilityHolderTracked(holder));
        }

        @Test
        @DisplayName("isAbilityHolderTracked returns false for untracked holder")
        void isAbilityHolderTracked_returnsFalse_whenNotTracked() {
            AbilityHolder holder = new AbilityHolder(mcRPG, UUID.randomUUID());
            assertFalse(entityManager.isAbilityHolderTracked(holder));
        }

        @Test
        @DisplayName("isAbilityHolderTracked by UUID returns true for tracked UUID")
        void isAbilityHolderTracked_byUuid_returnsTrue_whenTracked() {
            UUID uuid = UUID.randomUUID();
            AbilityHolder holder = new AbilityHolder(mcRPG, uuid);
            entityManager.trackAbilityHolder(holder);

            assertTrue(entityManager.isAbilityHolderTracked(uuid));
        }

        @Test
        @DisplayName("isAbilityHolderTracked by UUID returns false for untracked UUID")
        void isAbilityHolderTracked_byUuid_returnsFalse_whenNotTracked() {
            assertFalse(entityManager.isAbilityHolderTracked(UUID.randomUUID()));
        }

        @Test
        @DisplayName("removeAbilityHolder returns the removed holder")
        void removeAbilityHolder_returnsRemovedHolder() {
            UUID uuid = UUID.randomUUID();
            AbilityHolder holder = new AbilityHolder(mcRPG, uuid);
            entityManager.trackAbilityHolder(holder);

            Optional<AbilityHolder> removed = entityManager.removeAbilityHolder(uuid);
            assertTrue(removed.isPresent());
            assertSame(holder, removed.get());
        }

        @Test
        @DisplayName("removeAbilityHolder returns empty for untracked UUID")
        void removeAbilityHolder_returnsEmpty_whenNotTracked() {
            Optional<AbilityHolder> removed = entityManager.removeAbilityHolder(UUID.randomUUID());
            assertTrue(removed.isEmpty());
        }

        @Test
        @DisplayName("getAbilityHolder returns empty after removal")
        void getAbilityHolder_returnsEmpty_afterRemoval() {
            UUID uuid = UUID.randomUUID();
            AbilityHolder holder = new AbilityHolder(mcRPG, uuid);
            entityManager.trackAbilityHolder(holder);
            entityManager.removeAbilityHolder(uuid);

            assertTrue(entityManager.getAbilityHolder(uuid).isEmpty());
            assertFalse(entityManager.isAbilityHolderTracked(uuid));
        }

        @Test
        @DisplayName("trackAbilityHolder overwrites previous holder for same UUID")
        void trackAbilityHolder_overwritesPreviousHolder() {
            UUID uuid = UUID.randomUUID();
            AbilityHolder first = new AbilityHolder(mcRPG, uuid);
            AbilityHolder second = new AbilityHolder(mcRPG, uuid);
            entityManager.trackAbilityHolder(first);
            entityManager.trackAbilityHolder(second);

            Optional<AbilityHolder> result = entityManager.getAbilityHolder(uuid);
            assertTrue(result.isPresent());
            assertSame(second, result.get());
        }

        @Test
        @DisplayName("Multiple holders are tracked independently")
        void trackAbilityHolder_multipleHolders_trackedIndependently() {
            UUID uuid1 = UUID.randomUUID();
            UUID uuid2 = UUID.randomUUID();
            AbilityHolder holder1 = new AbilityHolder(mcRPG, uuid1);
            AbilityHolder holder2 = new AbilityHolder(mcRPG, uuid2);

            entityManager.trackAbilityHolder(holder1);
            entityManager.trackAbilityHolder(holder2);

            assertSame(holder1, entityManager.getAbilityHolder(uuid1).orElseThrow());
            assertSame(holder2, entityManager.getAbilityHolder(uuid2).orElseThrow());
        }
    }

    @Nested
    @DisplayName("QuestHolder tracking")
    class QuestHolderTracking {

        @Test
        @DisplayName("getQuestHolder returns empty for untracked UUID")
        void getQuestHolder_returnsEmpty_whenNotTracked() {
            assertTrue(entityManager.getQuestHolder(UUID.randomUUID()).isEmpty());
        }

        @Test
        @DisplayName("trackQuestHolder makes holder retrievable")
        void trackQuestHolder_makesHolderRetrievable() {
            UUID uuid = UUID.randomUUID();
            QuestHolder holder = new QuestHolder(uuid);
            entityManager.trackQuestHolder(holder);

            Optional<QuestHolder> result = entityManager.getQuestHolder(uuid);
            assertTrue(result.isPresent());
            assertSame(holder, result.get());
        }

        @Test
        @DisplayName("isQuestHolderTracked returns true for tracked holder")
        void isQuestHolderTracked_returnsTrue_whenTracked() {
            UUID uuid = UUID.randomUUID();
            QuestHolder holder = new QuestHolder(uuid);
            entityManager.trackQuestHolder(holder);

            assertTrue(entityManager.isQuestHolderTracked(holder));
        }

        @Test
        @DisplayName("isQuestHolderTracked returns false for untracked holder")
        void isQuestHolderTracked_returnsFalse_whenNotTracked() {
            QuestHolder holder = new QuestHolder(UUID.randomUUID());
            assertFalse(entityManager.isQuestHolderTracked(holder));
        }

        @Test
        @DisplayName("isQuestHolderTracked by UUID returns true for tracked UUID")
        void isQuestHolderTracked_byUuid_returnsTrue_whenTracked() {
            UUID uuid = UUID.randomUUID();
            QuestHolder holder = new QuestHolder(uuid);
            entityManager.trackQuestHolder(holder);

            assertTrue(entityManager.isQuestHolderTracked(uuid));
        }

        @Test
        @DisplayName("isQuestHolderTracked by UUID returns false for untracked UUID")
        void isQuestHolderTracked_byUuid_returnsFalse_whenNotTracked() {
            assertFalse(entityManager.isQuestHolderTracked(UUID.randomUUID()));
        }

        @Test
        @DisplayName("removeQuestHolder returns the removed holder")
        void removeQuestHolder_returnsRemovedHolder() {
            UUID uuid = UUID.randomUUID();
            QuestHolder holder = new QuestHolder(uuid);
            entityManager.trackQuestHolder(holder);

            Optional<QuestHolder> removed = entityManager.removeQuestHolder(uuid);
            assertTrue(removed.isPresent());
            assertSame(holder, removed.get());
        }

        @Test
        @DisplayName("removeQuestHolder returns empty for untracked UUID")
        void removeQuestHolder_returnsEmpty_whenNotTracked() {
            assertTrue(entityManager.removeQuestHolder(UUID.randomUUID()).isEmpty());
        }

        @Test
        @DisplayName("getQuestHolder returns empty after removal")
        void getQuestHolder_returnsEmpty_afterRemoval() {
            UUID uuid = UUID.randomUUID();
            QuestHolder holder = new QuestHolder(uuid);
            entityManager.trackQuestHolder(holder);
            entityManager.removeQuestHolder(uuid);

            assertTrue(entityManager.getQuestHolder(uuid).isEmpty());
            assertFalse(entityManager.isQuestHolderTracked(uuid));
        }
    }

    @Nested
    @DisplayName("Cross-holder independence")
    class CrossHolderIndependence {

        @Test
        @DisplayName("Tracking an ability holder does not affect quest holder")
        void trackAbilityHolder_doesNotAffectQuestHolder() {
            UUID uuid = UUID.randomUUID();
            AbilityHolder abilityHolder = new AbilityHolder(mcRPG, uuid);
            entityManager.trackAbilityHolder(abilityHolder);

            assertFalse(entityManager.isQuestHolderTracked(uuid));
        }

        @Test
        @DisplayName("Tracking a quest holder does not affect ability holder")
        void trackQuestHolder_doesNotAffectAbilityHolder() {
            UUID uuid = UUID.randomUUID();
            QuestHolder questHolder = new QuestHolder(uuid);
            entityManager.trackQuestHolder(questHolder);

            assertFalse(entityManager.isAbilityHolderTracked(uuid));
        }

        @Test
        @DisplayName("Removing ability holder does not remove quest holder")
        void removeAbilityHolder_doesNotRemoveQuestHolder() {
            UUID uuid = UUID.randomUUID();
            AbilityHolder abilityHolder = new AbilityHolder(mcRPG, uuid);
            QuestHolder questHolder = new QuestHolder(uuid);
            entityManager.trackAbilityHolder(abilityHolder);
            entityManager.trackQuestHolder(questHolder);

            entityManager.removeAbilityHolder(uuid);
            assertTrue(entityManager.isQuestHolderTracked(uuid));
        }
    }
}
