package us.eunoians.mcrpg.ability.unlock;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.ability.unlock.builtin.DisplayHintUnlockConditionType;
import us.eunoians.mcrpg.ability.unlock.builtin.SkillLevelUnlockConditionType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UnlockConditionTypeRegistryTest extends McRPGBaseTest {

    private UnlockConditionTypeRegistry registry;

    @BeforeEach
    public void setupRegistry() {
        registry = new UnlockConditionTypeRegistry();
    }

    @DisplayName("Given an unlock condition type, when registering, then it can be retrieved by key")
    @Test
    public void register_allowsRetrievalByKey() {
        SkillLevelUnlockConditionType type = new SkillLevelUnlockConditionType();
        registry.register(type);
        assertTrue(registry.get(type.getKey()).isPresent());
        assertEquals(type, registry.get(type.getKey()).get());
    }

    @DisplayName("Given a registered type, when registering same key again, then it throws")
    @Test
    public void register_throwsOnDuplicateKey() {
        registry.register(new SkillLevelUnlockConditionType());
        assertThrows(IllegalStateException.class,
                () -> registry.register(new SkillLevelUnlockConditionType()));
    }

    @DisplayName("Given a registered type, when checking isRegistered, then it returns true")
    @Test
    public void isRegistered_returnsTrue() {
        registry.register(new DisplayHintUnlockConditionType());
        assertTrue(registry.isRegistered(DisplayHintUnlockConditionType.KEY));
    }

    @DisplayName("Given no registration, when checking isRegistered, then it returns false")
    @Test
    public void isRegistered_returnsFalse() {
        assertFalse(registry.isRegistered(new NamespacedKey("mcrpg", "missing")));
    }

    @DisplayName("Given no registration, when calling getOrThrow, then it throws")
    @Test
    public void getOrThrow_throws_whenNotRegistered() {
        assertThrows(IllegalArgumentException.class,
                () -> registry.getOrThrow(new NamespacedKey("mcrpg", "missing")));
    }

    @DisplayName("Given a registered type, when calling getOrThrow, then it returns the type")
    @Test
    public void getOrThrow_returns_whenRegistered() {
        SkillLevelUnlockConditionType type = new SkillLevelUnlockConditionType();
        registry.register(type);
        assertEquals(type, registry.getOrThrow(type.getKey()));
    }

    @DisplayName("Given registered types, when getting keys, then snapshot is immutable")
    @Test
    public void getRegisteredKeys_returnsImmutableSet() {
        registry.register(new SkillLevelUnlockConditionType());
        assertThrows(UnsupportedOperationException.class,
                () -> registry.getRegisteredKeys().add(new NamespacedKey("mcrpg", "hack")));
    }

    @DisplayName("Given a registered type, when checking registered(type), then it returns true")
    @Test
    public void registered_returnsTrueForRegisteredInstance() {
        SkillLevelUnlockConditionType type = new SkillLevelUnlockConditionType();
        registry.register(type);
        assertTrue(registry.registered(type));
    }
}
