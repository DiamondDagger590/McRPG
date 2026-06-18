package us.eunoians.mcrpg.quest.board;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BoardRotationTest extends McRPGBaseTest {

    private static final UUID ROTATION_ID = UUID.randomUUID();
    private static final NamespacedKey BOARD_KEY = new NamespacedKey("mcrpg", "default");
    private static final NamespacedKey REFRESH_TYPE_KEY = new NamespacedKey("mcrpg", "daily");

    @Test
    @DisplayName("getRotationId returns constructor value")
    void getRotationId_returnsConstructorValue() {
        BoardRotation rotation = new BoardRotation(ROTATION_ID, BOARD_KEY, REFRESH_TYPE_KEY, 100L, 1000L, 2000L);

        assertEquals(ROTATION_ID, rotation.getRotationId());
    }

    @Test
    @DisplayName("getBoardKey returns constructor value")
    void getBoardKey_returnsConstructorValue() {
        BoardRotation rotation = new BoardRotation(ROTATION_ID, BOARD_KEY, REFRESH_TYPE_KEY, 100L, 1000L, 2000L);

        assertEquals(BOARD_KEY, rotation.getBoardKey());
    }

    @Test
    @DisplayName("getRefreshTypeKey returns constructor value")
    void getRefreshTypeKey_returnsConstructorValue() {
        BoardRotation rotation = new BoardRotation(ROTATION_ID, BOARD_KEY, REFRESH_TYPE_KEY, 100L, 1000L, 2000L);

        assertEquals(REFRESH_TYPE_KEY, rotation.getRefreshTypeKey());
    }

    @Test
    @DisplayName("getRotationEpoch returns constructor value")
    void getRotationEpoch_returnsConstructorValue() {
        BoardRotation rotation = new BoardRotation(ROTATION_ID, BOARD_KEY, REFRESH_TYPE_KEY, 42L, 1000L, 2000L);

        assertEquals(42L, rotation.getRotationEpoch());
    }

    @Test
    @DisplayName("getStartedAt returns constructor value")
    void getStartedAt_returnsConstructorValue() {
        BoardRotation rotation = new BoardRotation(ROTATION_ID, BOARD_KEY, REFRESH_TYPE_KEY, 100L, 1699999999000L, 2000L);

        assertEquals(1699999999000L, rotation.getStartedAt());
    }

    @Test
    @DisplayName("getExpiresAt returns constructor value")
    void getExpiresAt_returnsConstructorValue() {
        BoardRotation rotation = new BoardRotation(ROTATION_ID, BOARD_KEY, REFRESH_TYPE_KEY, 100L, 1000L, 1700086400000L);

        assertEquals(1700086400000L, rotation.getExpiresAt());
    }

    @Test
    @DisplayName("All fields are stored correctly from constructor")
    void constructor_storesAllFields() {
        long epoch = 7L;
        long start = 1700000000000L;
        long expires = 1700086400000L;

        BoardRotation rotation = new BoardRotation(ROTATION_ID, BOARD_KEY, REFRESH_TYPE_KEY, epoch, start, expires);

        assertEquals(ROTATION_ID, rotation.getRotationId());
        assertEquals(BOARD_KEY, rotation.getBoardKey());
        assertEquals(REFRESH_TYPE_KEY, rotation.getRefreshTypeKey());
        assertEquals(epoch, rotation.getRotationEpoch());
        assertEquals(start, rotation.getStartedAt());
        assertEquals(expires, rotation.getExpiresAt());
    }

    @Test
    @DisplayName("Zero epoch values are accepted")
    void constructor_acceptsZeroEpochValues() {
        BoardRotation rotation = new BoardRotation(ROTATION_ID, BOARD_KEY, REFRESH_TYPE_KEY, 0L, 0L, 0L);

        assertEquals(0L, rotation.getRotationEpoch());
        assertEquals(0L, rotation.getStartedAt());
        assertEquals(0L, rotation.getExpiresAt());
    }
}
