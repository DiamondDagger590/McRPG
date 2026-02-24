package us.eunoians.mcrpg.quest.board;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;

import java.time.Duration;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BoardMetadataTest extends McRPGBaseTest {

    @DisplayName("getMetadataKey returns BoardMetadata.METADATA_KEY")
    @Test
    void getMetadataKey_returnsMetadataKey() {
        BoardMetadata metadata = new BoardMetadata(true, Set.of(), null, null);
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

        BoardMetadata original = new BoardMetadata(false, rarities, cooldown, scope);
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
        BoardMetadata original = new BoardMetadata(true, Set.of(new NamespacedKey("mcrpg", "common")), null, null);
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

        BoardMetadata metadata = new BoardMetadata(true, rarities, cooldown, scope);

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
}
