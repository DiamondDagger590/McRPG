package us.eunoians.mcrpg.quest.board.template;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("TemplateRewardDefinition")
public class TemplateRewardDefinitionTest extends McRPGBaseTest {

    @DisplayName("Record stores all provided values")
    @Test
    void constructor_storesAllFields() {
        NamespacedKey typeKey = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "experience");
        var reward = new TemplateRewardDefinition(typeKey, "xp_bonus", Map.of("amount", "100"));

        assertEquals(typeKey, reward.typeKey());
        assertEquals("xp_bonus", reward.label());
        assertEquals(Map.of("amount", "100"), reward.config());
    }

    @DisplayName("Canonical constructor makes config map immutable")
    @Test
    void constructor_configMap_isImmutable() {
        NamespacedKey typeKey = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "command");
        var reward = new TemplateRewardDefinition(typeKey, "cmd_reward", Map.of("command", "/give"));

        assertThrows(UnsupportedOperationException.class,
                () -> reward.config().put("extra", "value"));
    }

    @DisplayName("Mutations to original map do not affect the record")
    @Test
    void constructor_defensiveCopy_originalUnaffected() {
        NamespacedKey typeKey = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "title");
        Map<String, Object> mutableMap = new HashMap<>();
        mutableMap.put("title", "Champion");

        var reward = new TemplateRewardDefinition(typeKey, "champion_title", mutableMap);
        mutableMap.put("subtitle", "Sneaky");

        assertEquals(1, reward.config().size());
        assertEquals("Champion", reward.config().get("title"));
    }

    @DisplayName("Empty config map is accepted")
    @Test
    void constructor_emptyConfig_accepted() {
        NamespacedKey typeKey = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "sound");
        var reward = new TemplateRewardDefinition(typeKey, "fanfare", Map.of());

        assertEquals(0, reward.config().size());
    }
}
