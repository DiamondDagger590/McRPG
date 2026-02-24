package us.eunoians.mcrpg.quest.reward;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.QuestTestHelper;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class QuestRewardTypeRegistryTest extends McRPGBaseTest {

    private QuestRewardTypeRegistry registry;

    @BeforeEach
    public void setup() {
        registry = new QuestRewardTypeRegistry();
    }

    @DisplayName("Given a reward type, when registering, then it can be retrieved by key")
    @Test
    public void register_allowsRetrievalByKey() {
        MockQuestRewardType type = QuestTestHelper.mockRewardType("reg_reward");
        registry.register(type);
        assertTrue(registry.get(type.getKey()).isPresent());
    }

    @DisplayName("Given a registered type, when registering same key, then it throws IllegalStateException")
    @Test
    public void register_throwsOnDuplicateKey() {
        MockQuestRewardType type = QuestTestHelper.mockRewardType("dup_reward");
        registry.register(type);
        assertThrows(IllegalStateException.class, () -> registry.register(QuestTestHelper.mockRewardType("dup_reward")));
    }

    @DisplayName("Given a registered type, when checking isRegistered, then it returns true")
    @Test
    public void isRegistered_returnsTrue() {
        MockQuestRewardType type = QuestTestHelper.mockRewardType("check_reward");
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
        MockQuestRewardType type = QuestTestHelper.mockRewardType("throw_reward");
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
        registry.register(QuestTestHelper.mockRewardType("keys_reward"));
        assertThrows(UnsupportedOperationException.class,
                () -> registry.getRegisteredKeys().add(new NamespacedKey("mcrpg", "hack")));
    }
}
