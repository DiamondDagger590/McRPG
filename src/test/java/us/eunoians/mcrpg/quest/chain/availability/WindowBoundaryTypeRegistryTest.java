package us.eunoians.mcrpg.quest.chain.availability;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPGBaseTest;

import java.io.File;
import java.util.Optional;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("WindowBoundaryTypeRegistry")
class WindowBoundaryTypeRegistryTest extends McRPGBaseTest {

    private static final NamespacedKey TEST_KEY = new NamespacedKey("mcrpg", "test_boundary");
    private static final NamespacedKey OTHER_KEY = new NamespacedKey("mcrpg", "other_boundary");

    private WindowBoundaryTypeRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new WindowBoundaryTypeRegistry();
    }

    /**
     * Creates a stub {@link WindowBoundaryType} with the given key.
     *
     * @param key The key identifying this boundary type.
     * @return A stub boundary type.
     */
    private WindowBoundaryType stubType(@NotNull NamespacedKey key) {
        return new WindowBoundaryType() {
            @Override
            @NotNull
            public NamespacedKey getKey() {
                return key;
            }

            @Override
            @NotNull
            public Optional<NamespacedKey> getExpansionKey() {
                return Optional.empty();
            }

            @Override
            @NotNull
            public Optional<WindowBoundary> parse(@NotNull Section section, @NotNull File file,
                                                   @NotNull Logger logger) {
                return Optional.empty();
            }
        };
    }

    @Nested
    @DisplayName("register")
    class Register {

        @Test
        @DisplayName("registered type is retrievable by key")
        void register_makesTypeRetrievableByKey() {
            WindowBoundaryType type = stubType(TEST_KEY);
            registry.register(type);

            Optional<WindowBoundaryType> result = registry.get(TEST_KEY);

            assertTrue(result.isPresent());
            assertEquals(type, result.get());
        }

        @Test
        @DisplayName("registering a second type with the same key replaces the first")
        void register_replacesExistingType_whenSameKey() {
            WindowBoundaryType first = stubType(TEST_KEY);
            WindowBoundaryType second = stubType(TEST_KEY);
            registry.register(first);
            registry.register(second);

            Optional<WindowBoundaryType> result = registry.get(TEST_KEY);

            assertTrue(result.isPresent());
            assertEquals(second, result.get());
        }
    }

    @Nested
    @DisplayName("registered")
    class Registered {

        @Test
        @DisplayName("returns true for a registered type")
        void registered_returnsTrue_forRegisteredType() {
            WindowBoundaryType type = stubType(TEST_KEY);
            registry.register(type);

            assertTrue(registry.registered(type));
        }

        @Test
        @DisplayName("returns false for an unregistered type")
        void registered_returnsFalse_forUnregisteredType() {
            WindowBoundaryType type = stubType(TEST_KEY);

            assertFalse(registry.registered(type));
        }

        @Test
        @DisplayName("returns true for a different instance with the same key")
        void registered_returnsTrue_forDifferentInstanceWithSameKey() {
            WindowBoundaryType original = stubType(TEST_KEY);
            WindowBoundaryType sameKey = stubType(TEST_KEY);
            registry.register(original);

            assertTrue(registry.registered(sameKey));
        }
    }

    @Nested
    @DisplayName("get")
    class Get {

        @Test
        @DisplayName("returns empty for an unknown key")
        void get_returnsEmpty_forUnknownKey() {
            assertTrue(registry.get(TEST_KEY).isEmpty());
        }

        @Test
        @DisplayName("returns the correct type when multiple are registered")
        void get_returnsCorrectType_whenMultipleRegistered() {
            WindowBoundaryType testType = stubType(TEST_KEY);
            WindowBoundaryType otherType = stubType(OTHER_KEY);
            registry.register(testType);
            registry.register(otherType);

            assertEquals(testType, registry.get(TEST_KEY).orElseThrow());
            assertEquals(otherType, registry.get(OTHER_KEY).orElseThrow());
        }
    }
}
