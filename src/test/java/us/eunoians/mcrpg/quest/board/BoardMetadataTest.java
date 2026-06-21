package us.eunoians.mcrpg.quest.board;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BoardMetadataTest extends McRPGBaseTest {

    @DisplayName("getMetadataKey returns BoardMetadata.METADATA_KEY")
    @Test
    void getMetadataKey_returnsMetadataKey() {
        BoardMetadata metadata = new BoardMetadata(true, Set.of(), Set.of(), null, null);
        assertSame(BoardMetadata.METADATA_KEY, metadata.getMetadataKey());
    }

    @DisplayName("serialize / deserialize round-trip preserves all fields")
    @Test
    void serializeDeserialize_roundTrip_preservesAllFields() {
        NamespacedKey common = new NamespacedKey("mcrpg", "common");
        NamespacedKey rare = new NamespacedKey("mcrpg", "rare");
        Set<NamespacedKey> rarities = Set.of(common, rare);
        Duration cooldown = Duration.ofMinutes(30);
        String scope = "PLAYER";

        BoardMetadata original = new BoardMetadata(false, rarities, Set.of(), cooldown, scope);
        Map<String, Object> serialized = original.serialize();
        BoardMetadata deserialized = BoardMetadata.deserialize(serialized);

        assertEquals(original.boardEligible(), deserialized.boardEligible());
        assertEquals(false, deserialized.boardEligible());
        assertEquals(original.supportedRarities(), deserialized.supportedRarities());
        assertTrue(deserialized.supportedRarities().contains(common));
        assertTrue(deserialized.supportedRarities().contains(rare));
        assertEquals(original.acceptanceCooldown(), deserialized.acceptanceCooldown());
        assertEquals(Duration.ofMinutes(30), deserialized.acceptanceCooldown());
        assertEquals(original.cooldownScope(), deserialized.cooldownScope());
        assertEquals("PLAYER", deserialized.cooldownScope());
    }

    @DisplayName("serialize / deserialize round-trip with null cooldown and scope")
    @Test
    void serializeDeserialize_roundTrip_nullCooldownAndScope() {
        BoardMetadata original = new BoardMetadata(true, Set.of(new NamespacedKey("mcrpg", "common")), Set.of(), null, null);
        Map<String, Object> serialized = original.serialize();
        BoardMetadata deserialized = BoardMetadata.deserialize(serialized);

        assertEquals(original.boardEligible(), deserialized.boardEligible());
        assertEquals(original.supportedRarities(), deserialized.supportedRarities());
        assertEquals(original.acceptanceCooldown(), deserialized.acceptanceCooldown());
        assertEquals(original.cooldownScope(), deserialized.cooldownScope());
        assertEquals(null, deserialized.acceptanceCooldown());
        assertEquals(null, deserialized.cooldownScope());
    }

    @DisplayName("boardEligible, supportedRarities, acceptanceCooldown, cooldownScope accessors work")
    @Test
    void accessors_returnCorrectValues() {
        NamespacedKey common = new NamespacedKey("mcrpg", "common");
        Set<NamespacedKey> rarities = Set.of(common);
        Duration cooldown = Duration.ofHours(1);
        String scope = "GLOBAL";

        BoardMetadata metadata = new BoardMetadata(true, rarities, Set.of(), cooldown, scope);

        assertTrue(metadata.boardEligible());
        assertEquals(rarities, metadata.supportedRarities());
        assertEquals(cooldown, metadata.acceptanceCooldown());
        assertEquals(scope, metadata.cooldownScope());
    }

    @DisplayName("deserialize with missing fields uses defaults")
    @Test
    void deserialize_missingFields_usesDefaults() {
        Map<String, Object> empty = Map.of();
        BoardMetadata deserialized = BoardMetadata.deserialize(empty);

        assertTrue(deserialized.boardEligible());
        assertTrue(deserialized.supportedRarities().isEmpty());
        assertEquals(null, deserialized.acceptanceCooldown());
        assertEquals(null, deserialized.cooldownScope());
    }

    @DisplayName("deserialize with only board-eligible false preserves value")
    @Test
    void deserialize_boardEligibleFalse_preservesValue() {
        Map<String, Object> data = Map.of("board-eligible", false);
        BoardMetadata deserialized = BoardMetadata.deserialize(data);

        assertEquals(false, deserialized.boardEligible());
    }

    @DisplayName("serialize / deserialize round-trip with supported refresh types")
    @Test
    void serializeDeserialize_roundTrip_withRefreshTypes() {
        Set<String> refreshTypes = Set.of("DAILY", "WEEKLY");
        BoardMetadata original = new BoardMetadata(
                true, Set.of(new NamespacedKey("mcrpg", "common")), refreshTypes, null, null);
        Map<String, Object> serialized = original.serialize();
        BoardMetadata deserialized = BoardMetadata.deserialize(serialized);

        assertEquals(2, deserialized.supportedRefreshTypes().size());
        assertTrue(deserialized.supportedRefreshTypes().contains("DAILY"));
        assertTrue(deserialized.supportedRefreshTypes().contains("WEEKLY"));
    }

    @DisplayName("serialize omits refresh types when empty")
    @Test
    void serialize_omitsRefreshTypes_whenEmpty() {
        BoardMetadata metadata = new BoardMetadata(true, Set.of(), Set.of(), null, null);
        Map<String, Object> serialized = metadata.serialize();

        assertFalse(serialized.containsKey("supported-refresh-types"));
    }

    @DisplayName("serialize omits cooldown when null")
    @Test
    void serialize_omitsCooldown_whenNull() {
        BoardMetadata metadata = new BoardMetadata(true, Set.of(), Set.of(), null, null);
        Map<String, Object> serialized = metadata.serialize();

        assertFalse(serialized.containsKey("acceptance-cooldown-ms"));
        assertFalse(serialized.containsKey("cooldown-scope"));
    }

    @DisplayName("deserialize ignores non-Number cooldown value")
    @Test
    void deserialize_ignoresNonNumberCooldown() {
        Map<String, Object> data = new HashMap<>();
        data.put("board-eligible", true);
        data.put("acceptance-cooldown-ms", "not-a-number");
        BoardMetadata deserialized = BoardMetadata.deserialize(data);

        assertNull(deserialized.acceptanceCooldown());
    }

    @DisplayName("deserialize ignores non-String cooldown scope")
    @Test
    void deserialize_ignoresNonStringScope() {
        Map<String, Object> data = new HashMap<>();
        data.put("board-eligible", true);
        data.put("cooldown-scope", 42);
        BoardMetadata deserialized = BoardMetadata.deserialize(data);

        assertNull(deserialized.cooldownScope());
    }

    @DisplayName("deserialize ignores non-List rarities value")
    @Test
    void deserialize_ignoresNonListRarities() {
        Map<String, Object> data = new HashMap<>();
        data.put("board-eligible", true);
        data.put("supported-rarities", "not-a-list");
        BoardMetadata deserialized = BoardMetadata.deserialize(data);

        assertTrue(deserialized.supportedRarities().isEmpty());
    }

    @DisplayName("deserialize normalizes refresh types to uppercase")
    @Test
    void deserialize_normalizesRefreshTypesToUppercase() {
        Map<String, Object> data = new HashMap<>();
        data.put("supported-refresh-types", List.of("daily", "Weekly"));
        BoardMetadata deserialized = BoardMetadata.deserialize(data);

        assertTrue(deserialized.supportedRefreshTypes().contains("DAILY"));
        assertTrue(deserialized.supportedRefreshTypes().contains("WEEKLY"));
    }

    @DisplayName("serialize includes cooldown-ms as long millis")
    @Test
    void serialize_includesCooldownAsMillis() {
        Duration cooldown = Duration.ofMinutes(5);
        BoardMetadata metadata = new BoardMetadata(true, Set.of(), Set.of(), cooldown, "GLOBAL");
        Map<String, Object> serialized = metadata.serialize();

        assertEquals(300000L, serialized.get("acceptance-cooldown-ms"));
    }

    @DisplayName("deserialize with integer cooldown millis")
    @Test
    void deserialize_integerCooldownMillis() {
        Map<String, Object> data = new HashMap<>();
        data.put("acceptance-cooldown-ms", 60000);
        BoardMetadata deserialized = BoardMetadata.deserialize(data);

        assertEquals(Duration.ofMinutes(1), deserialized.acceptanceCooldown());
    }
}
