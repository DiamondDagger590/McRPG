package us.eunoians.mcrpg.quest.impl.scope;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;

import java.sql.Connection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class QuestScopeProviderRegistryTest extends McRPGBaseTest {

    private QuestScopeProviderRegistry registry;

    @BeforeEach
    public void setup() {
        registry = new QuestScopeProviderRegistry();
    }

    @DisplayName("Given a provider, when registering, then it can be retrieved by key")
    @Test
    public void register_allowsRetrievalByKey() {
        QuestScopeProvider<?> provider = createTestProvider("test_scope");
        registry.register(provider);
        assertTrue(registry.get(new NamespacedKey("mcrpg", "test_scope")).isPresent());
    }

    @DisplayName("Given a registered provider, when registering duplicate key, then it throws IllegalStateException")
    @Test
    public void register_throwsOnDuplicateKey() {
        QuestScopeProvider<?> provider1 = createTestProvider("dup_scope");
        QuestScopeProvider<?> provider2 = createTestProvider("dup_scope");
        registry.register(provider1);
        assertThrows(IllegalStateException.class, () -> registry.register(provider2));
    }

    @DisplayName("Given a registered provider, when checking isRegistered, then it returns true")
    @Test
    public void isRegistered_returnsTrue_forRegisteredKey() {
        registry.register(createTestProvider("check_scope"));
        assertTrue(registry.isRegistered(new NamespacedKey("mcrpg", "check_scope")));
    }

    @DisplayName("Given no registration, when checking isRegistered, then it returns false")
    @Test
    public void isRegistered_returnsFalse_forUnregisteredKey() {
        assertFalse(registry.isRegistered(new NamespacedKey("mcrpg", "missing")));
    }

    @DisplayName("Given a registered provider, when getting by key, then getOrThrow returns it")
    @Test
    public void getOrThrow_returnsProvider_whenRegistered() {
        registry.register(createTestProvider("throw_scope"));
        registry.getOrThrow(new NamespacedKey("mcrpg", "throw_scope"));
    }

    @DisplayName("Given no registration, when calling getOrThrow, then it throws IllegalArgumentException")
    @Test
    public void getOrThrow_throwsIllegalArgumentException_whenNotRegistered() {
        assertThrows(IllegalArgumentException.class,
                () -> registry.getOrThrow(new NamespacedKey("mcrpg", "missing")));
    }

    private QuestScopeProvider<?> createTestProvider(String key) {
        NamespacedKey nsKey = new NamespacedKey("mcrpg", key);
        return new QuestScopeProvider<QuestScope>() {
            @Override
            public NamespacedKey getKey() {
                return nsKey;
            }

            @Override
            public QuestScope createNewScope(UUID questUUID) {
                return null;
            }

            @Override
            public CompletableFuture<QuestScope> loadScope(UUID questUUID, UUID scopeUUID) {
                return CompletableFuture.completedFuture(null);
            }

            @Override
            public List<UUID> resolveActiveQuestUUIDs(UUID playerUUID, Connection connection) {
                return List.of();
            }
        };
    }
}
