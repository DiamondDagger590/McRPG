package us.eunoians.mcrpg.quest.board.template;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class QuestDeserializationExceptionTest {

    @Nested
    @DisplayName("Constructor without cause")
    class ConstructorWithoutCauseTests {

        @DisplayName("getMessage returns provided message")
        @Test
        void getMessage_returnsProvidedMessage() {
            var ex = new QuestDeserializationException("parse error", "mcrpg:daily_1", "objective type mcrpg:block_break");
            assertEquals("parse error", ex.getMessage());
        }

        @DisplayName("getQuestKey returns provided key")
        @Test
        void getQuestKey_returnsProvidedKey() {
            var ex = new QuestDeserializationException("msg", "mcrpg:daily_1", "element");
            assertEquals("mcrpg:daily_1", ex.getQuestKey());
        }

        @DisplayName("getQuestKey returns null when not provided")
        @Test
        void getQuestKey_returnsNull_whenNotProvided() {
            var ex = new QuestDeserializationException("msg", null, "element");
            assertNull(ex.getQuestKey());
        }

        @DisplayName("getFailedElement returns provided element")
        @Test
        void getFailedElement_returnsProvidedElement() {
            var ex = new QuestDeserializationException("msg", "key", "objective type mcrpg:block_break");
            assertEquals("objective type mcrpg:block_break", ex.getFailedElement());
        }

        @DisplayName("getCause returns null")
        @Test
        void getCause_returnsNull() {
            var ex = new QuestDeserializationException("msg", "key", "element");
            assertNull(ex.getCause());
        }
    }

    @Nested
    @DisplayName("Constructor with cause")
    class ConstructorWithCauseTests {

        @DisplayName("getMessage returns provided message")
        @Test
        void getMessage_returnsProvidedMessage() {
            var cause = new IllegalStateException("bad json");
            var ex = new QuestDeserializationException("parse error", cause, "mcrpg:daily_1", "reward type");
            assertEquals("parse error", ex.getMessage());
        }

        @DisplayName("getCause returns provided cause")
        @Test
        void getCause_returnsProvidedCause() {
            var cause = new IllegalStateException("bad json");
            var ex = new QuestDeserializationException("msg", cause, "key", "element");
            assertSame(cause, ex.getCause());
        }

        @DisplayName("getQuestKey returns provided key")
        @Test
        void getQuestKey_returnsProvidedKey() {
            var cause = new RuntimeException();
            var ex = new QuestDeserializationException("msg", cause, "mcrpg:weekly_5", "element");
            assertEquals("mcrpg:weekly_5", ex.getQuestKey());
        }

        @DisplayName("getQuestKey returns null when not provided")
        @Test
        void getQuestKey_returnsNull_whenNotProvided() {
            var cause = new RuntimeException();
            var ex = new QuestDeserializationException("msg", cause, null, "element");
            assertNull(ex.getQuestKey());
        }

        @DisplayName("getFailedElement returns provided element")
        @Test
        void getFailedElement_returnsProvidedElement() {
            var cause = new RuntimeException();
            var ex = new QuestDeserializationException("msg", cause, "key", "scope provider mcrpg:single_player");
            assertEquals("scope provider mcrpg:single_player", ex.getFailedElement());
        }
    }

    @Nested
    @DisplayName("Inheritance")
    class InheritanceTests {

        @DisplayName("extends RuntimeException")
        @Test
        void constructor_producesRuntimeException() {
            var ex = new QuestDeserializationException("msg", null, "element");
            assertInstanceOf(RuntimeException.class, ex);
        }
    }
}
