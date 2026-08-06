package us.eunoians.mcrpg.stat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import us.eunoians.mcrpg.McRPGBaseTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("McRPGPlayerStat")
class McRPGPlayerStatTest extends McRPGBaseTest {

    @ParameterizedTest
    @EnumSource(McRPGPlayerStat.class)
    @DisplayName("every enum value has a non-null NamespacedKey")
    void everyValue_hasNonNullKey(McRPGPlayerStat stat) {
        assertNotNull(stat.getKey());
    }

    @ParameterizedTest
    @EnumSource(McRPGPlayerStat.class)
    @DisplayName("every enum value uses the mcrpg namespace")
    void everyValue_usesMcrpgNamespace(McRPGPlayerStat stat) {
        assertEquals("mcrpg", stat.getKey().getNamespace());
    }

    @Test
    @DisplayName("HEALTH key string is 'health'")
    void health_keyStringIsHealth() {
        assertEquals("health", McRPGPlayerStat.HEALTH.getKey().getKey());
    }

    @Test
    @DisplayName("MANA key string is 'mana'")
    void mana_keyStringIsMana() {
        assertEquals("mana", McRPGPlayerStat.MANA.getKey().getKey());
    }

    @Test
    @DisplayName("HEALTH and MANA have distinct keys")
    void healthAndMana_haveDistinctKeys() {
        assertNotEquals(McRPGPlayerStat.HEALTH.getKey(), McRPGPlayerStat.MANA.getKey());
    }

    @Test
    @DisplayName("enum has exactly two values")
    void enum_hasExactlyTwoValues() {
        assertEquals(2, McRPGPlayerStat.values().length);
    }
}
