package us.eunoians.mcrpg.quest.board.template;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.quest.QuestTestHelper;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.definition.QuestDefinitionRegistry;

import static org.junit.jupiter.api.Assertions.*;

class EphemeralDefinitionCleanupTest {

    private QuestDefinitionRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new QuestDefinitionRegistry();
    }

    @Test
    @DisplayName("Deregister removes gen_ definition from registry")
    void deregister_removesGeneratedDefinition() {
        NamespacedKey genKey = NamespacedKey.fromString("mcrpg:gen_daily_mining_abcd1234");
        QuestDefinition def = QuestTestHelper.singlePhaseQuest("gen_daily_mining_abcd1234");
        registry.register(def);

        assertTrue(registry.isRegistered(genKey));
        assertTrue(registry.deregister(genKey));
        assertFalse(registry.isRegistered(genKey));
    }

    @Test
    @DisplayName("Deregister on non-existing key returns false (no-op)")
    void deregister_nonExistingKey_returnsFalse() {
        NamespacedKey genKey = NamespacedKey.fromString("mcrpg:gen_nonexistent_12345678");
        assertFalse(registry.deregister(genKey));
    }

    @Test
    @DisplayName("Non-generated quest key (no gen_ prefix) is not ephemeral")
    void nonGeneratedKey_notEphemeral() {
        NamespacedKey normalKey = NamespacedKey.fromString("mcrpg:daily_mining");
        QuestDefinition def = QuestTestHelper.singlePhaseQuest("daily_mining");
        registry.register(def);

        assertFalse(normalKey.getKey().startsWith("gen_"));
        assertTrue(registry.isRegistered(normalKey));
    }

    @Test
    @DisplayName("gen_ prefix detection correctly identifies ephemeral definitions")
    void genPrefixDetection() {
        assertTrue("gen_daily_mining_abcd1234".startsWith("gen_"));
        assertTrue("gen_x_00000000".startsWith("gen_"));
        assertFalse("daily_mining".startsWith("gen_"));
        assertFalse("general_quest".startsWith("gen_"));
    }

    @Test
    @DisplayName("Deregister then re-register same key succeeds (restart recovery flow)")
    void deregister_thenReregister_succeeds() {
        NamespacedKey genKey = NamespacedKey.fromString("mcrpg:gen_tmpl_a_abcd1234");
        QuestDefinition original = QuestTestHelper.singlePhaseQuest("gen_tmpl_a_abcd1234");
        registry.register(original);

        assertTrue(registry.deregister(genKey));
        assertFalse(registry.isRegistered(genKey));

        QuestDefinition recovered = QuestTestHelper.singlePhaseQuest("gen_tmpl_a_abcd1234");
        assertDoesNotThrow(() -> registry.register(recovered));
        assertTrue(registry.isRegistered(genKey));
        assertEquals(recovered, registry.getOrThrow(genKey));
    }

    @Test
    @DisplayName("Deregister does not affect other registered definitions")
    void deregister_doesNotAffectOthers() {
        NamespacedKey genKey = NamespacedKey.fromString("mcrpg:gen_tmpl_a_12345678");
        NamespacedKey normalKey = NamespacedKey.fromString("mcrpg:daily_mining");
        registry.register(QuestTestHelper.singlePhaseQuest("gen_tmpl_a_12345678"));
        registry.register(QuestTestHelper.singlePhaseQuest("daily_mining"));

        assertTrue(registry.deregister(genKey));
        assertFalse(registry.isRegistered(genKey));
        assertTrue(registry.isRegistered(normalKey));
    }
}
