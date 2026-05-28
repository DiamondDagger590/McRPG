package us.eunoians.mcrpg.quest.reward.builtin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.expansion.McRPGExpansion;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class MessageRewardTypeTest extends McRPGBaseTest {

    private MessageRewardType type;

    @BeforeEach
    public void setup() {
        type = new MessageRewardType();
    }

    @DisplayName("getKey returns message key")
    @Test
    public void getKey_returnsExpectedKey() {
        assertEquals(MessageRewardType.KEY, type.getKey());
    }

    @DisplayName("getExpansionKey returns McRPGExpansion key")
    @Test
    public void getExpansionKey_returnsMcRPGExpansionKey() {
        assertTrue(type.getExpansionKey().isPresent());
        assertEquals(McRPGExpansion.EXPANSION_KEY, type.getExpansionKey().get());
    }

    @DisplayName("getNumericAmount returns empty for message rewards")
    @Test
    public void getNumericAmount_returnsEmpty() {
        assertTrue(type.getNumericAmount().isEmpty());
    }

    @DisplayName("withAmountMultiplier returns same instance")
    @Test
    public void withAmountMultiplier_returnsSelf() {
        assertSame(type, type.withAmountMultiplier(2.0));
    }

    @DisplayName("fromSerializedConfig with locale key preserves key")
    @Test
    public void fromSerializedConfig_withLocaleKey_preservesKey() {
        MessageRewardType configured = type.fromSerializedConfig(Map.of("key", "tutorial.welcome"));
        Map<String, Object> serialized = configured.serializeConfig();
        assertEquals("tutorial.welcome", serialized.get("key"));
    }

    @DisplayName("fromSerializedConfig with inline messages preserves messages")
    @Test
    public void fromSerializedConfig_withInlineMessages_preservesMessages() {
        List<String> messages = List.of("<primary>Hello!", "<body>Welcome.");
        MessageRewardType configured = type.fromSerializedConfig(Map.of("messages", messages));
        Map<String, Object> serialized = configured.serializeConfig();
        @SuppressWarnings("unchecked")
        List<String> roundTripped = (List<String>) serialized.get("messages");
        assertEquals(messages, roundTripped);
    }

    @DisplayName("serializeConfig omits key when not set")
    @Test
    public void serializeConfig_omitsKeyWhenNotSet() {
        MessageRewardType configured = type.fromSerializedConfig(Map.of("messages", List.of("<primary>Hi")));
        assertNull(configured.serializeConfig().get("key"));
    }

    @DisplayName("describeForDisplay returns non-null string")
    @Test
    public void describeForDisplay_returnsNonNull() {
        assertNotNull(type.describeForDisplay());
    }
}
