package us.eunoians.mcrpg.quest.board;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("BoardOffering template constructors")
class BoardOfferingTemplateTest extends McRPGBaseTest {

    private static final NamespacedKey CATEGORY = new NamespacedKey("mcrpg", "shared_daily");
    private static final NamespacedKey QUEST_DEF = new NamespacedKey("mcrpg", "test_quest");
    private static final NamespacedKey RARITY = new NamespacedKey("mcrpg", "common");
    private static final NamespacedKey TEMPLATE = new NamespacedKey("mcrpg", "gather_template");
    private static final Duration COMPLETION_TIME = Duration.ofHours(24);
    private static final String GENERATED_JSON = "{\"key\":\"mcrpg:generated_quest\"}";

    private BoardOffering newTemplateOffering() {
        return new BoardOffering(
                UUID.randomUUID(),
                UUID.randomUUID(),
                CATEGORY,
                0,
                QUEST_DEF,
                RARITY,
                null,
                COMPLETION_TIME,
                TEMPLATE,
                GENERATED_JSON
        );
    }

    private BoardOffering newNonTemplateOffering() {
        return new BoardOffering(
                UUID.randomUUID(),
                UUID.randomUUID(),
                CATEGORY,
                0,
                QUEST_DEF,
                RARITY,
                null,
                COMPLETION_TIME
        );
    }

    @Nested
    @DisplayName("Template VISIBLE constructor")
    class TemplateVisibleConstructor {

        @Test
        @DisplayName("starts in VISIBLE state")
        void startsInVisibleState() {
            BoardOffering offering = newTemplateOffering();
            assertEquals(BoardOffering.State.VISIBLE, offering.getState());
        }

        @Test
        @DisplayName("getTemplateKey returns the template key")
        void getTemplateKey_returnsTemplateKey() {
            BoardOffering offering = newTemplateOffering();
            assertTrue(offering.getTemplateKey().isPresent());
            assertEquals(TEMPLATE, offering.getTemplateKey().get());
        }

        @Test
        @DisplayName("getGeneratedDefinition returns the JSON")
        void getGeneratedDefinition_returnsJson() {
            BoardOffering offering = newTemplateOffering();
            assertTrue(offering.getGeneratedDefinition().isPresent());
            assertEquals(GENERATED_JSON, offering.getGeneratedDefinition().get());
        }

        @Test
        @DisplayName("isTemplateGenerated returns true")
        void isTemplateGenerated_returnsTrue() {
            BoardOffering offering = newTemplateOffering();
            assertTrue(offering.isTemplateGenerated());
        }

        @Test
        @DisplayName("acceptedAt and questInstanceUUID are empty before acceptance")
        void beforeAcceptance_optionalsEmpty() {
            BoardOffering offering = newTemplateOffering();
            assertTrue(offering.getAcceptedAt().isEmpty());
            assertTrue(offering.getQuestInstanceUUID().isEmpty());
        }
    }

    @Nested
    @DisplayName("Non-template constructor")
    class NonTemplateConstructor {

        @Test
        @DisplayName("getTemplateKey returns empty")
        void getTemplateKey_returnsEmpty() {
            BoardOffering offering = newNonTemplateOffering();
            assertTrue(offering.getTemplateKey().isEmpty());
        }

        @Test
        @DisplayName("getGeneratedDefinition returns empty")
        void getGeneratedDefinition_returnsEmpty() {
            BoardOffering offering = newNonTemplateOffering();
            assertTrue(offering.getGeneratedDefinition().isEmpty());
        }

        @Test
        @DisplayName("isTemplateGenerated returns false")
        void isTemplateGenerated_returnsFalse() {
            BoardOffering offering = newNonTemplateOffering();
            assertFalse(offering.isTemplateGenerated());
        }
    }

    @Nested
    @DisplayName("Database reconstruction with template metadata")
    class DatabaseReconstructionWithTemplate {

        @Test
        @DisplayName("preserves template key and generated definition from persisted state")
        void preservesTemplateMetadata() {
            UUID offeringId = UUID.randomUUID();
            UUID rotationId = UUID.randomUUID();
            long acceptedAt = System.currentTimeMillis();
            UUID questInstanceUUID = UUID.randomUUID();

            BoardOffering offering = new BoardOffering(
                    offeringId,
                    rotationId,
                    CATEGORY,
                    1,
                    QUEST_DEF,
                    RARITY,
                    "land-123",
                    COMPLETION_TIME,
                    BoardOffering.State.ACCEPTED,
                    acceptedAt,
                    questInstanceUUID,
                    TEMPLATE,
                    GENERATED_JSON
            );

            assertEquals(BoardOffering.State.ACCEPTED, offering.getState());
            assertEquals(offeringId, offering.getOfferingId());
            assertEquals(rotationId, offering.getRotationId());
            assertEquals(1, offering.getSlotIndex());
            assertTrue(offering.getScopeTargetId().isPresent());
            assertEquals("land-123", offering.getScopeTargetId().get());
            assertTrue(offering.getAcceptedAt().isPresent());
            assertEquals(acceptedAt, offering.getAcceptedAt().get());
            assertTrue(offering.getQuestInstanceUUID().isPresent());
            assertEquals(questInstanceUUID, offering.getQuestInstanceUUID().get());
            assertTrue(offering.isTemplateGenerated());
            assertEquals(TEMPLATE, offering.getTemplateKey().get());
            assertEquals(GENERATED_JSON, offering.getGeneratedDefinition().get());
        }

        @Test
        @DisplayName("null template key means non-template in DB reconstruction")
        void nullTemplateKey_isNonTemplate() {
            BoardOffering offering = new BoardOffering(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    CATEGORY,
                    0,
                    QUEST_DEF,
                    RARITY,
                    null,
                    COMPLETION_TIME,
                    BoardOffering.State.VISIBLE,
                    null,
                    null,
                    null,
                    null
            );

            assertFalse(offering.isTemplateGenerated());
            assertTrue(offering.getTemplateKey().isEmpty());
            assertTrue(offering.getGeneratedDefinition().isEmpty());
        }
    }

    @Nested
    @DisplayName("Template offering state transitions")
    class TemplateOfferingStateTransitions {

        @Test
        @DisplayName("template offering can be accepted")
        void templateOffering_canBeAccepted() {
            BoardOffering offering = newTemplateOffering();
            UUID questUUID = UUID.randomUUID();
            long now = System.currentTimeMillis();

            offering.accept(now, questUUID);

            assertEquals(BoardOffering.State.ACCEPTED, offering.getState());
            assertTrue(offering.isTemplateGenerated());
            assertEquals(TEMPLATE, offering.getTemplateKey().get());
        }

        @Test
        @DisplayName("template offering can be expired")
        void templateOffering_canBeExpired() {
            BoardOffering offering = newTemplateOffering();
            offering.transitionTo(BoardOffering.State.EXPIRED);

            assertEquals(BoardOffering.State.EXPIRED, offering.getState());
            assertTrue(offering.isTemplateGenerated());
        }

        @Test
        @DisplayName("accepted template offering can be completed")
        void acceptedTemplateOffering_canBeCompleted() {
            BoardOffering offering = newTemplateOffering();
            offering.accept(System.currentTimeMillis(), UUID.randomUUID());
            offering.transitionTo(BoardOffering.State.COMPLETED);

            assertEquals(BoardOffering.State.COMPLETED, offering.getState());
        }

        @Test
        @DisplayName("accepted template offering can be abandoned")
        void acceptedTemplateOffering_canBeAbandoned() {
            BoardOffering offering = newTemplateOffering();
            offering.accept(System.currentTimeMillis(), UUID.randomUUID());
            offering.transitionTo(BoardOffering.State.ABANDONED);

            assertEquals(BoardOffering.State.ABANDONED, offering.getState());
        }
    }
}
