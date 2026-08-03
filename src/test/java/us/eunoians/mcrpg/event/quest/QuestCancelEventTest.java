package us.eunoians.mcrpg.event.quest;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.QuestTestHelper;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.impl.QuestInstance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link QuestCancelEvent}.
 */
class QuestCancelEventTest extends McRPGBaseTest {

    @Nested
    @DisplayName("Three-argument constructor")
    class ThreeArgConstructor {

        @Test
        @DisplayName("Given expiration=true, When isExpiration is called, Then returns true")
        void isExpiration_returnsTrue_whenConstructedWithExpirationTrue() {
            QuestDefinition definition = QuestTestHelper.singlePhaseQuest("cancel_expire");
            QuestInstance instance = QuestTestHelper.startedQuestInstance(definition);
            QuestCancelEvent event = new QuestCancelEvent(instance, definition, true);
            assertTrue(event.isExpiration());
        }

        @Test
        @DisplayName("Given expiration=false, When isExpiration is called, Then returns false")
        void isExpiration_returnsFalse_whenConstructedWithExpirationFalse() {
            QuestDefinition definition = QuestTestHelper.singlePhaseQuest("cancel_manual");
            QuestInstance instance = QuestTestHelper.startedQuestInstance(definition);
            QuestCancelEvent event = new QuestCancelEvent(instance, definition, false);
            assertFalse(event.isExpiration());
        }

        @Test
        @DisplayName("Given a definition, When getQuestDefinition is called, Then returns the definition")
        void getQuestDefinition_returnsDefinition() {
            QuestDefinition definition = QuestTestHelper.singlePhaseQuest("cancel_def");
            QuestInstance instance = QuestTestHelper.startedQuestInstance(definition);
            QuestCancelEvent event = new QuestCancelEvent(instance, definition, false);
            assertSame(definition, event.getQuestDefinition());
        }

        @Test
        @DisplayName("Given a definition, When getQuestDefinitionKey is called, Then returns the definition's key")
        void getQuestDefinitionKey_returnsDefinitionKey() {
            QuestDefinition definition = QuestTestHelper.singlePhaseQuest("cancel_key");
            QuestInstance instance = QuestTestHelper.startedQuestInstance(definition);
            QuestCancelEvent event = new QuestCancelEvent(instance, definition, false);
            assertEquals(definition.getQuestKey(), event.getQuestDefinitionKey());
        }

        @Test
        @DisplayName("Given a quest instance, When getQuestInstance is called, Then returns the instance")
        void getQuestInstance_returnsInstance() {
            QuestDefinition definition = QuestTestHelper.singlePhaseQuest("cancel_inst");
            QuestInstance instance = QuestTestHelper.startedQuestInstance(definition);
            QuestCancelEvent event = new QuestCancelEvent(instance, definition, false);
            assertSame(instance, event.getQuestInstance());
        }
    }

    @Nested
    @DisplayName("Deprecated two-argument constructor")
    class TwoArgConstructor {

        @SuppressWarnings("deprecation")
        @Test
        @DisplayName("Given no expiration flag, When isExpiration is called, Then defaults to false")
        void isExpiration_defaultsToFalse() {
            QuestDefinition definition = QuestTestHelper.singlePhaseQuest("cancel_dep2");
            QuestInstance instance = QuestTestHelper.startedQuestInstance(definition);
            QuestCancelEvent event = new QuestCancelEvent(instance, definition);
            assertFalse(event.isExpiration());
        }

        @SuppressWarnings("deprecation")
        @Test
        @DisplayName("Given a definition, When getQuestDefinition is called, Then returns the definition")
        void getQuestDefinition_returnsDefinition() {
            QuestDefinition definition = QuestTestHelper.singlePhaseQuest("cancel_dep2_def");
            QuestInstance instance = QuestTestHelper.startedQuestInstance(definition);
            QuestCancelEvent event = new QuestCancelEvent(instance, definition);
            assertSame(definition, event.getQuestDefinition());
        }
    }

    @Nested
    @DisplayName("Deprecated single-argument constructor")
    class SingleArgConstructor {

        @SuppressWarnings("deprecation")
        @Test
        @DisplayName("Given no definition, When getQuestDefinition is called, Then returns null")
        void getQuestDefinition_returnsNull() {
            QuestDefinition definition = QuestTestHelper.singlePhaseQuest("cancel_dep1");
            QuestInstance instance = QuestTestHelper.startedQuestInstance(definition);
            QuestCancelEvent event = new QuestCancelEvent(instance);
            assertNull(event.getQuestDefinition());
        }

        @SuppressWarnings("deprecation")
        @Test
        @DisplayName("Given no definition, When isExpiration is called, Then defaults to false")
        void isExpiration_defaultsToFalse() {
            QuestDefinition definition = QuestTestHelper.singlePhaseQuest("cancel_dep1_exp");
            QuestInstance instance = QuestTestHelper.startedQuestInstance(definition);
            QuestCancelEvent event = new QuestCancelEvent(instance);
            assertFalse(event.isExpiration());
        }

        @SuppressWarnings("deprecation")
        @Test
        @DisplayName("Given no definition, When getQuestDefinitionKey is called, Then falls back to instance key")
        void getQuestDefinitionKey_fallsBackToInstanceKey() {
            QuestDefinition definition = QuestTestHelper.singlePhaseQuest("cancel_fallback");
            QuestInstance instance = QuestTestHelper.startedQuestInstance(definition);
            NamespacedKey instanceKey = instance.getQuestKey();
            QuestCancelEvent event = new QuestCancelEvent(instance);
            assertEquals(instanceKey, event.getQuestDefinitionKey());
        }
    }

    @Nested
    @DisplayName("Handler list")
    class HandlerListTests {

        @Test
        @DisplayName("getHandlers returns the shared QuestEvent handler list")
        void getHandlers_returnsSharedHandlerList() {
            QuestDefinition definition = QuestTestHelper.singlePhaseQuest("cancel_hl");
            QuestInstance instance = QuestTestHelper.startedQuestInstance(definition);
            QuestCancelEvent event = new QuestCancelEvent(instance, definition, false);
            assertSame(QuestEvent.getHandlerList(), event.getHandlers());
        }
    }
}
