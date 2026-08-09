package us.eunoians.mcrpg.quest.board.template.condition;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.expansion.McRPGExpansion;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("PermissionCondition")
class PermissionConditionTest extends McRPGBaseTest {

    @Nested
    @DisplayName("constructor")
    class ConstructorTests {

        @Test
        @DisplayName("no-arg prototype uses KEY as placeholder permission")
        void noArgConstructor_usesKeyAsPlaceholder() {
            PermissionCondition prototype = new PermissionCondition();
            assertEquals(PermissionCondition.KEY.getKey(), prototype.getPermission());
        }

        @Test
        @DisplayName("blank permission string throws IllegalArgumentException")
        void constructor_throws_whenPermissionBlank() {
            assertThrows(IllegalArgumentException.class, () -> new PermissionCondition(""));
        }

        @Test
        @DisplayName("whitespace-only permission string throws IllegalArgumentException")
        void constructor_throws_whenPermissionWhitespace() {
            assertThrows(IllegalArgumentException.class, () -> new PermissionCondition("   "));
        }
    }

    @Nested
    @DisplayName("getters")
    class GetterTests {

        @Test
        @DisplayName("getKey returns mcrpg:permission_check")
        void getKey_returnsMcrpgPermissionCheck() {
            assertEquals(NamespacedKey.fromString("mcrpg:permission_check"), new PermissionCondition("mcrpg.test").getKey());
        }

        @Test
        @DisplayName("getExpansionKey returns McRPGExpansion key")
        void getExpansionKey_returnsMcRPGExpansionKey() {
            Optional<NamespacedKey> key = new PermissionCondition("mcrpg.test").getExpansionKey();
            assertTrue(key.isPresent());
            assertEquals(McRPGExpansion.EXPANSION_KEY, key.get());
        }

        @Test
        @DisplayName("getPermission returns configured permission string")
        void getPermission_returnsConfiguredPermission() {
            assertEquals("mcrpg.title.hero", new PermissionCondition("mcrpg.title.hero").getPermission());
        }
    }

    @Nested
    @DisplayName("serializeConfig")
    class SerializeConfigTests {

        @Test
        @DisplayName("serializes permission string")
        void serializeConfig_containsPermissionKey() {
            Map<String, Object> config = new PermissionCondition("mcrpg.quest.legendary").serializeConfig();
            assertEquals("mcrpg.quest.legendary", config.get("permission"));
        }

        @Test
        @DisplayName("serialized map has exactly one entry")
        void serializeConfig_hasOneEntry() {
            Map<String, Object> config = new PermissionCondition("mcrpg.test").serializeConfig();
            assertEquals(1, config.size());
        }
    }

    @Nested
    @DisplayName("evaluate")
    class EvaluateTests {

        @Test
        @DisplayName("online player with permission returns true")
        void evaluate_returnsTrue_whenPlayerHasPermission() {
            PlayerMock player = server.addPlayer();
            player.addAttachment(mcRPG, "mcrpg.title.hero", true);

            PermissionCondition condition = new PermissionCondition("mcrpg.title.hero");
            ConditionContext ctx = new ConditionContext(null, null, null, null, player.getUniqueId(), null);
            assertTrue(condition.evaluate(ctx));
        }

        @Test
        @DisplayName("online player without permission returns false")
        void evaluate_returnsFalse_whenPlayerLacksPermission() {
            PlayerMock player = server.addPlayer();

            PermissionCondition condition = new PermissionCondition("mcrpg.title.hero");
            ConditionContext ctx = new ConditionContext(null, null, null, null, player.getUniqueId(), null);
            assertFalse(condition.evaluate(ctx));
        }

        @Test
        @DisplayName("null player UUID in context returns false")
        void evaluate_returnsFalse_whenPlayerUUIDNull() {
            PermissionCondition condition = new PermissionCondition("mcrpg.title.hero");
            ConditionContext ctx = new ConditionContext(null, null, null, null, null, null);
            assertFalse(condition.evaluate(ctx));
        }

        @Test
        @DisplayName("offline player returns false")
        void evaluate_returnsFalse_whenPlayerOffline() {
            UUID offlineUUID = UUID.randomUUID();
            PermissionCondition condition = new PermissionCondition("mcrpg.title.hero");
            ConditionContext ctx = new ConditionContext(null, null, null, null, offlineUUID, null);
            assertFalse(condition.evaluate(ctx));
        }
    }
}
