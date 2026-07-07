package us.eunoians.mcrpg.quest.board.template.condition;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.expansion.McRPGExpansion;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ChanceCondition")
class ChanceConditionTest {

    @Nested
    @DisplayName("constructor validation")
    class ConstructorValidation {

        @Test
        @DisplayName("rejects chance below 0.0")
        void constructor_throws_whenChanceBelowZero() {
            assertThrows(IllegalArgumentException.class, () -> new ChanceCondition(-0.1));
        }

        @Test
        @DisplayName("rejects chance above 1.0")
        void constructor_throws_whenChanceAboveOne() {
            assertThrows(IllegalArgumentException.class, () -> new ChanceCondition(1.1));
        }

        @Test
        @DisplayName("accepts boundary value 0.0")
        void constructor_accepts_zeroChance() {
            assertDoesNotThrow(() -> new ChanceCondition(0.0));
        }

        @Test
        @DisplayName("accepts boundary value 1.0")
        void constructor_accepts_fullChance() {
            assertDoesNotThrow(() -> new ChanceCondition(1.0));
        }

        @Test
        @DisplayName("no-arg prototype constructor creates valid instance")
        void noArgConstructor_createsPrototype() {
            ChanceCondition prototype = new ChanceCondition();
            assertEquals(0.5, prototype.getChance());
        }
    }

    @Nested
    @DisplayName("getters")
    class Getters {

        @Test
        @DisplayName("getKey returns mcrpg:chance")
        void getKey_returnsMcrpgChance() {
            assertEquals(NamespacedKey.fromString("mcrpg:chance"), new ChanceCondition(0.5).getKey());
        }

        @Test
        @DisplayName("getExpansionKey returns McRPGExpansion key")
        void getExpansionKey_returnsMcRPGExpansionKey() {
            Optional<NamespacedKey> key = new ChanceCondition(0.5).getExpansionKey();
            assertTrue(key.isPresent());
            assertEquals(McRPGExpansion.EXPANSION_KEY, key.get());
        }

        @Test
        @DisplayName("getChance returns configured probability")
        void getChance_returnsConfiguredValue() {
            assertEquals(0.75, new ChanceCondition(0.75).getChance());
        }
    }

    @Nested
    @DisplayName("serializeConfig")
    class SerializeConfig {

        @Test
        @DisplayName("serializes chance value")
        void serializeConfig_containsChanceKey() {
            Map<String, Object> config = new ChanceCondition(0.3).serializeConfig();
            assertEquals(0.3, config.get("chance"));
        }

        @Test
        @DisplayName("serialized map has exactly one entry")
        void serializeConfig_hasOneEntry() {
            Map<String, Object> config = new ChanceCondition(0.5).serializeConfig();
            assertEquals(1, config.size());
        }
    }
}
