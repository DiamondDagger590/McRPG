package us.eunoians.mcrpg.skill.experience.context;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import us.eunoians.mcrpg.McRPGBaseTest;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class McRPGGainReasonTest extends McRPGBaseTest {

    @Nested
    @DisplayName("McRPGGainReason")
    class GainReasonTests {

        @ParameterizedTest
        @DisplayName("all values have non-null keys")
        @EnumSource(McRPGGainReason.class)
        void allValues_haveNonNullKeys(McRPGGainReason reason) {
            assertNotNull(reason.getKey());
        }

        @ParameterizedTest
        @DisplayName("all values have non-empty display names")
        @EnumSource(McRPGGainReason.class)
        void allValues_haveNonEmptyDisplayNames(McRPGGainReason reason) {
            assertNotNull(reason.getDisplayName());
            assertFalse(reason.getDisplayName().isEmpty());
        }

        @ParameterizedTest
        @DisplayName("all keys use mcrpg namespace")
        @EnumSource(McRPGGainReason.class)
        void allKeys_useMcRPGNamespace(McRPGGainReason reason) {
            assertEquals("mcrpg", reason.getKey().getNamespace());
        }

        @ParameterizedTest
        @DisplayName("all keys use lowercase enum name")
        @EnumSource(McRPGGainReason.class)
        void allKeys_useLowercaseEnumName(McRPGGainReason reason) {
            assertEquals(reason.name().toLowerCase(), reason.getKey().getKey());
        }

        @Test
        @DisplayName("all keys are unique")
        void allKeys_areUnique() {
            Set<NamespacedKey> keys = new HashSet<>();
            for (McRPGGainReason reason : McRPGGainReason.values()) {
                assertTrue(keys.add(reason.getKey()), "Duplicate key: " + reason.getKey());
            }
        }

        @Test
        @DisplayName("BLOCK_BREAK has expected display name")
        void blockBreak_hasExpectedDisplayName() {
            assertEquals("Block Break", McRPGGainReason.BLOCK_BREAK.getDisplayName());
        }

        @Test
        @DisplayName("ENTITY_DAMAGE has expected display name")
        void entityDamage_hasExpectedDisplayName() {
            assertEquals("Entity Damage", McRPGGainReason.ENTITY_DAMAGE.getDisplayName());
        }

        @Test
        @DisplayName("REDEEM has expected display name")
        void redeem_hasExpectedDisplayName() {
            assertEquals("Redeem", McRPGGainReason.REDEEM.getDisplayName());
        }

        @Test
        @DisplayName("COMMAND has expected display name")
        void command_hasExpectedDisplayName() {
            assertEquals("Command", McRPGGainReason.COMMAND.getDisplayName());
        }

        @Test
        @DisplayName("OTHER has expected display name")
        void other_hasExpectedDisplayName() {
            assertEquals("Other", McRPGGainReason.OTHER.getDisplayName());
        }

        @ParameterizedTest
        @DisplayName("implements GainReason interface")
        @EnumSource(McRPGGainReason.class)
        void implementsGainReasonInterface(McRPGGainReason reason) {
            assertTrue(reason instanceof GainReason);
        }

        @Test
        @DisplayName("BLOCK_BREAK key value is block_break")
        void blockBreak_keyIsBlockBreak() {
            assertEquals("block_break", McRPGGainReason.BLOCK_BREAK.getKey().getKey());
        }

        @Test
        @DisplayName("ENTITY_DAMAGE key value is entity_damage")
        void entityDamage_keyIsEntityDamage() {
            assertEquals("entity_damage", McRPGGainReason.ENTITY_DAMAGE.getKey().getKey());
        }

        @Test
        @DisplayName("expected number of enum values")
        void expectedEnumCount() {
            assertEquals(5, McRPGGainReason.values().length);
        }
    }
}
