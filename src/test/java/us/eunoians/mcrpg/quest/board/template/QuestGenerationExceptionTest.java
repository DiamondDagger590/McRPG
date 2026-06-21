package us.eunoians.mcrpg.quest.board.template;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class QuestGenerationExceptionTest {

    private static final NamespacedKey TEMPLATE_KEY = new NamespacedKey("mcrpg", "test_template");
    private static final NamespacedKey RARITY_KEY = new NamespacedKey("mcrpg", "common");
    private static final NamespacedKey ELEMENT_KEY = new NamespacedKey("mcrpg", "block_break");

    @Nested
    @DisplayName("Constructor without cause")
    class ConstructorWithoutCauseTests {

        @DisplayName("getMessage returns provided message")
        @Test
        void getMessage_returnsProvidedMessage() {
            var ex = new QuestGenerationException("generation failed", TEMPLATE_KEY, RARITY_KEY, ELEMENT_KEY);
            assertEquals("generation failed", ex.getMessage());
        }

        @DisplayName("getTemplateKey returns provided key")
        @Test
        void getTemplateKey_returnsProvidedKey() {
            var ex = new QuestGenerationException("msg", TEMPLATE_KEY, RARITY_KEY, ELEMENT_KEY);
            assertEquals(TEMPLATE_KEY, ex.getTemplateKey());
        }

        @DisplayName("getRarityKey returns provided key")
        @Test
        void getRarityKey_returnsProvidedKey() {
            var ex = new QuestGenerationException("msg", TEMPLATE_KEY, RARITY_KEY, ELEMENT_KEY);
            assertEquals(RARITY_KEY, ex.getRarityKey());
        }

        @DisplayName("getFailedElementKey returns provided key")
        @Test
        void getFailedElementKey_returnsProvidedKey() {
            var ex = new QuestGenerationException("msg", TEMPLATE_KEY, RARITY_KEY, ELEMENT_KEY);
            assertEquals(ELEMENT_KEY, ex.getFailedElementKey());
        }

        @DisplayName("getFailedElementKey returns null when not provided")
        @Test
        void getFailedElementKey_returnsNull_whenNotProvided() {
            var ex = new QuestGenerationException("msg", TEMPLATE_KEY, RARITY_KEY, null);
            assertNull(ex.getFailedElementKey());
        }

        @DisplayName("getCause returns null")
        @Test
        void getCause_returnsNull() {
            var ex = new QuestGenerationException("msg", TEMPLATE_KEY, RARITY_KEY, ELEMENT_KEY);
            assertNull(ex.getCause());
        }
    }

    @Nested
    @DisplayName("Constructor with cause")
    class ConstructorWithCauseTests {

        @DisplayName("getMessage returns provided message")
        @Test
        void getMessage_returnsProvidedMessage() {
            var cause = new IllegalArgumentException("root cause");
            var ex = new QuestGenerationException("generation failed", cause, TEMPLATE_KEY, RARITY_KEY, ELEMENT_KEY);
            assertEquals("generation failed", ex.getMessage());
        }

        @DisplayName("getCause returns provided cause")
        @Test
        void getCause_returnsProvidedCause() {
            var cause = new IllegalArgumentException("root cause");
            var ex = new QuestGenerationException("msg", cause, TEMPLATE_KEY, RARITY_KEY, ELEMENT_KEY);
            assertSame(cause, ex.getCause());
        }

        @DisplayName("getTemplateKey returns provided key")
        @Test
        void getTemplateKey_returnsProvidedKey() {
            var cause = new RuntimeException();
            var ex = new QuestGenerationException("msg", cause, TEMPLATE_KEY, RARITY_KEY, ELEMENT_KEY);
            assertEquals(TEMPLATE_KEY, ex.getTemplateKey());
        }

        @DisplayName("getRarityKey returns provided key")
        @Test
        void getRarityKey_returnsProvidedKey() {
            var cause = new RuntimeException();
            var ex = new QuestGenerationException("msg", cause, TEMPLATE_KEY, RARITY_KEY, ELEMENT_KEY);
            assertEquals(RARITY_KEY, ex.getRarityKey());
        }

        @DisplayName("getFailedElementKey returns null when not provided")
        @Test
        void getFailedElementKey_returnsNull_whenNotProvided() {
            var cause = new RuntimeException();
            var ex = new QuestGenerationException("msg", cause, TEMPLATE_KEY, RARITY_KEY, null);
            assertNull(ex.getFailedElementKey());
        }

        @DisplayName("getFailedElementKey returns provided key")
        @Test
        void getFailedElementKey_returnsProvidedKey() {
            var cause = new RuntimeException();
            var ex = new QuestGenerationException("msg", cause, TEMPLATE_KEY, RARITY_KEY, ELEMENT_KEY);
            assertEquals(ELEMENT_KEY, ex.getFailedElementKey());
        }
    }

    @Nested
    @DisplayName("Inheritance")
    class InheritanceTests {

        @DisplayName("extends RuntimeException")
        @Test
        void constructor_producesRuntimeException() {
            var ex = new QuestGenerationException("msg", TEMPLATE_KEY, RARITY_KEY, null);
            assertInstanceOf(RuntimeException.class, ex);
        }
    }
}
