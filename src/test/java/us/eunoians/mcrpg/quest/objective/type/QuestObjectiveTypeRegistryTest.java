package us.eunoians.mcrpg.quest.objective.type;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.QuestTestHelper;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class QuestObjectiveTypeRegistryTest extends McRPGBaseTest {

    private QuestObjectiveTypeRegistry registry;

    @BeforeEach
    public void setup() {
        registry = new QuestObjectiveTypeRegistry();
    }

    @DisplayName("Given an objective type, when registering, then it can be retrieved by key")
    @Test
    public void register_allowsRetrievalByKey() {
        MockQuestObjectiveType type = QuestTestHelper.mockObjectiveType("reg_type");
        registry.register(type);
        assertTrue(registry.get(type.getKey()).isPresent());
    }

    @DisplayName("Given a registered type, when registering same key, then it throws IllegalStateException")
    @Test
    public void register_throwsOnDuplicateKey() {
        MockQuestObjectiveType type = QuestTestHelper.mockObjectiveType("dup_type");
        registry.register(type);
        assertThrows(IllegalStateException.class, () -> registry.register(QuestTestHelper.mockObjectiveType("dup_type")));
    }

    @DisplayName("Given a registered type, when checking isRegistered, then it returns true")
    @Test
    public void isRegistered_returnsTrue() {
        MockQuestObjectiveType type = QuestTestHelper.mockObjectiveType("check_type");
        registry.register(type);
        assertTrue(registry.isRegistered(type.getKey()));
    }

    @DisplayName("Given no registration, when checking isRegistered, then it returns false")
    @Test
    public void isRegistered_returnsFalse() {
        assertFalse(registry.isRegistered(new NamespacedKey("mcrpg", "missing")));
    }

    @DisplayName("Given a registered type, when calling getOrThrow, then it returns the type")
    @Test
    public void getOrThrow_returns_whenRegistered() {
        MockQuestObjectiveType type = QuestTestHelper.mockObjectiveType("throw_type");
        registry.register(type);
        registry.getOrThrow(type.getKey());
    }

    @DisplayName("Given no registration, when calling getOrThrow, then it throws")
    @Test
    public void getOrThrow_throws_whenNotRegistered() {
        assertThrows(IllegalArgumentException.class,
                () -> registry.getOrThrow(new NamespacedKey("mcrpg", "missing")));
    }

    @DisplayName("Given registered types, when getting keys, then snapshot is immutable")
    @Test
    public void getRegisteredKeys_returnsImmutableSet() {
        registry.register(QuestTestHelper.mockObjectiveType("keys_type"));
        assertThrows(UnsupportedOperationException.class,
                () -> registry.getRegisteredKeys().add(new NamespacedKey("mcrpg", "hack")));
    }
}
