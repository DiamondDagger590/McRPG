package us.eunoians.mcrpg.quest.definition;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.QuestTestHelper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class QuestDefinitionRegistryCoverageTest extends McRPGBaseTest {

    private QuestDefinitionRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new QuestDefinitionRegistry();
    }

    @Nested
    @DisplayName("deregister")
    class Deregister {

        @DisplayName("returns true when key was registered")
        @Test
        void deregister_returnsTrue_whenKeyRegistered() {
            QuestDefinition def = QuestTestHelper.singlePhaseQuest("deregister_test");
            registry.register(def);
            assertTrue(registry.deregister(new NamespacedKey("mcrpg", "deregister_test")));
        }

        @DisplayName("returns false when key was not registered")
        @Test
        void deregister_returnsFalse_whenKeyNotRegistered() {
            assertFalse(registry.deregister(new NamespacedKey("mcrpg", "nonexistent")));
        }

        @DisplayName("definition no longer retrievable after deregister")
        @Test
        void deregister_removesDefinition() {
            QuestDefinition def = QuestTestHelper.singlePhaseQuest("remove_test");
            registry.register(def);
            registry.deregister(new NamespacedKey("mcrpg", "remove_test"));
            assertFalse(registry.isRegistered(new NamespacedKey("mcrpg", "remove_test")));
            assertTrue(registry.get(new NamespacedKey("mcrpg", "remove_test")).isEmpty());
        }

        @DisplayName("deregistered key can be re-registered")
        @Test
        void deregister_allowsReRegistration() {
            QuestDefinition def1 = QuestTestHelper.singlePhaseQuest("rereg_test");
            registry.register(def1);
            registry.deregister(new NamespacedKey("mcrpg", "rereg_test"));

            QuestDefinition def2 = QuestTestHelper.singlePhaseQuest("rereg_test");
            registry.register(def2);
            assertTrue(registry.isRegistered(new NamespacedKey("mcrpg", "rereg_test")));
        }
    }

    @Nested
    @DisplayName("registered (Registry interface)")
    class RegisteredInterface {

        @DisplayName("returns true for registered definition")
        @Test
        void registered_returnsTrue_whenRegistered() {
            QuestDefinition def = QuestTestHelper.singlePhaseQuest("reg_check");
            registry.register(def);
            assertTrue(registry.registered(def));
        }

        @DisplayName("returns false for unregistered definition")
        @Test
        void registered_returnsFalse_whenNotRegistered() {
            QuestDefinition def = QuestTestHelper.singlePhaseQuest("unreg_check");
            assertFalse(registry.registered(def));
        }

        @DisplayName("returns false after deregistration")
        @Test
        void registered_returnsFalse_afterDeregister() {
            QuestDefinition def = QuestTestHelper.singlePhaseQuest("dereg_check");
            registry.register(def);
            registry.deregister(def.getQuestKey());
            assertFalse(registry.registered(def));
        }
    }

    @Nested
    @DisplayName("getAll")
    class GetAll {

        @DisplayName("returns all registered definitions")
        @Test
        void getAll_returnsAllDefinitions() {
            registry.register(QuestTestHelper.singlePhaseQuest("a"));
            registry.register(QuestTestHelper.singlePhaseQuest("b"));
            var all = registry.getAll();
            assertEquals(2, all.size());
        }

        @DisplayName("returns empty collection when empty")
        @Test
        void getAll_returnsEmpty_whenNoRegistrations() {
            assertTrue(registry.getAll().isEmpty());
        }
    }
}
