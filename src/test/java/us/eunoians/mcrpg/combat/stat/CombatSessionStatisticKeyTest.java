package us.eunoians.mcrpg.combat.stat;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("CombatSessionStatisticKey")
class CombatSessionStatisticKeyTest {

    private static final List<NamespacedKey> ALL_KEYS = List.of(
            CombatSessionStatisticKey.DAMAGE_DEALT,
            CombatSessionStatisticKey.DAMAGE_TAKEN,
            CombatSessionStatisticKey.HEALING_DEALT,
            CombatSessionStatisticKey.HEALING_RECEIVED,
            CombatSessionStatisticKey.HITS_LANDED,
            CombatSessionStatisticKey.HITS_RECEIVED,
            CombatSessionStatisticKey.KILLS,
            CombatSessionStatisticKey.SESSION_DURATION);

    @Test
    @DisplayName("all built-in keys use the mcrpg namespace")
    void allKeys_useMcrpgNamespace() {
        for (NamespacedKey key : ALL_KEYS) {
            assertEquals("mcrpg", key.getNamespace());
        }
    }

    @Test
    @DisplayName("all built-in keys have distinct string values")
    void allKeys_areDistinct() {
        assertEquals(ALL_KEYS.size(), Set.copyOf(ALL_KEYS).size());
    }
}
