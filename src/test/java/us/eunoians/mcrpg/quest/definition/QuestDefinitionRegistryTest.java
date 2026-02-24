package us.eunoians.mcrpg.quest.definition;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.QuestTestHelper;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class QuestDefinitionRegistryTest extends McRPGBaseTest {

    private QuestDefinitionRegistry registry;

    @BeforeEach
    public void setup() {
        registry = new QuestDefinitionRegistry();
    }

    @DisplayName("Given a definition, when registering, then it can be retrieved by key")
    @Test
    public void register_allowsRetrievalByKey() {
        QuestDefinition def = QuestTestHelper.singlePhaseQuest("reg_test");
        registry.register(def);
        assertTrue(registry.get(new NamespacedKey("mcrpg", "reg_test")).isPresent());
    }

    @DisplayName("Given a registered definition, when registering same key, then it throws IllegalStateException")
    @Test
    public void register_throwsOnDuplicateKey() {
        QuestDefinition def1 = QuestTestHelper.singlePhaseQuest("dup_test");
        QuestDefinition def2 = QuestTestHelper.singlePhaseQuest("dup_test");
        registry.register(def1);
        assertThrows(IllegalStateException.class, () -> registry.register(def2));
    }

    @DisplayName("Given a registered definition, when getting by key, then getOrThrow returns it")
    @Test
    public void getOrThrow_returns_whenRegistered() {
        QuestDefinition def = QuestTestHelper.singlePhaseQuest("throw_test");
        registry.register(def);
        assertEquals(def, registry.getOrThrow(new NamespacedKey("mcrpg", "throw_test")));
    }

    @DisplayName("Given no registration, when calling getOrThrow, then it throws IllegalArgumentException")
    @Test
    public void getOrThrow_throws_whenNotRegistered() {
        assertThrows(IllegalArgumentException.class,
                () -> registry.getOrThrow(new NamespacedKey("mcrpg", "missing")));
    }

    @DisplayName("Given a registered definition, when checking isRegistered, then it returns true")
    @Test
    public void isRegistered_returnsTrue() {
        registry.register(QuestTestHelper.singlePhaseQuest("check_test"));
        assertTrue(registry.isRegistered(new NamespacedKey("mcrpg", "check_test")));
    }

    @DisplayName("Given no registration, when checking isRegistered, then it returns false")
    @Test
    public void isRegistered_returnsFalse() {
        assertFalse(registry.isRegistered(new NamespacedKey("mcrpg", "missing")));
    }

    @DisplayName("Given registered definitions, when calling getAll, then it returns immutable snapshot")
    @Test
    public void getRegisteredDefinitions_returnsImmutableSnapshot() {
        QuestDefinition def = QuestTestHelper.singlePhaseQuest("all_test");
        registry.register(def);
        Map<NamespacedKey, QuestDefinition> all = registry.getRegisteredDefinitions();
        assertEquals(1, all.size());
        assertThrows(UnsupportedOperationException.class, () -> all.put(new NamespacedKey("mcrpg", "hack"), def));
    }

    @DisplayName("Given registered definitions, when calling replaceConfigDefinitions, then old entries are replaced")
    @Test
    public void replaceConfigDefinitions_replacesAll() {
        registry.register(QuestTestHelper.singlePhaseQuest("old_quest"));
        QuestDefinition newDef = QuestTestHelper.singlePhaseQuest("new_quest");
        registry.replaceConfigDefinitions(Map.of(newDef.getQuestKey(), newDef));
        assertFalse(registry.isRegistered(new NamespacedKey("mcrpg", "old_quest")));
        assertTrue(registry.isRegistered(new NamespacedKey("mcrpg", "new_quest")));
    }

    @DisplayName("Given registered definitions, when getting keys, then it returns immutable set")
    @Test
    public void getRegisteredKeys_returnsImmutableSet() {
        registry.register(QuestTestHelper.singlePhaseQuest("keys_test"));
        assertThrows(UnsupportedOperationException.class,
                () -> registry.getRegisteredKeys().add(new NamespacedKey("mcrpg", "hack")));
    }
}
