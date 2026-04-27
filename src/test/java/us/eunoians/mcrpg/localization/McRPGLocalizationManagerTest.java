package us.eunoians.mcrpg.localization;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for {@link McRPGLocalizationManager}.
 * <p>
 * The manager is normally registered as a full mock in the test registry (see {@code TestBootstrap}).
 * These tests use {@code CALLS_REAL_METHODS} to exercise concrete methods on the class without
 * starting the full plugin environment.
 */
class McRPGLocalizationManagerTest {

    private McRPGLocalizationManager manager;

    @BeforeEach
    void setup() {
        manager = mock(McRPGLocalizationManager.class, CALLS_REAL_METHODS);
    }

    @DisplayName("Given a template with a single placeholder, when getLocalizedMessage is called, then the placeholder is replaced")
    @Test
    void getLocalizedMessage_singlePlaceholder_isReplaced() {
        String result = manager.getLocalizedMessage("You gained <amount> XP", Map.of("amount", "500"));

        assertEquals("You gained 500 XP", result);
    }

    @DisplayName("Given a template with multiple placeholders, when getLocalizedMessage is called, then all placeholders are replaced")
    @Test
    void getLocalizedMessage_multiplePlaceholders_allReplaced() {
        String result = manager.getLocalizedMessage("<skill> +<amount> XP", Map.of("skill", "Mining", "amount", "250"));

        assertEquals("Mining +250 XP", result);
    }

    @DisplayName("Given a template with a placeholder not in the map, when getLocalizedMessage is called, then the token is left unchanged")
    @Test
    void getLocalizedMessage_unknownPlaceholder_leftUnchanged() {
        String result = manager.getLocalizedMessage("You gained <amount> <skill> XP", Map.of("amount", "100"));

        assertEquals("You gained 100 <skill> XP", result);
    }

    @DisplayName("Given an empty placeholder map, when getLocalizedMessage is called, then the template is returned unchanged")
    @Test
    void getLocalizedMessage_emptyMap_returnsTemplateUnchanged() {
        String template = "Complete the quest to earn a reward";

        String result = manager.getLocalizedMessage(template, Map.of());

        assertEquals(template, result);
    }

    @DisplayName("Given a template containing MiniMessage color tags, when getLocalizedMessage is called, then the tags are preserved")
    @Test
    void getLocalizedMessage_miniMessageTagsPreserved() {
        String result = manager.getLocalizedMessage("<gold><amount> XP", Map.of("amount", "500"));

        assertEquals("<gold>500 XP", result);
    }
}
