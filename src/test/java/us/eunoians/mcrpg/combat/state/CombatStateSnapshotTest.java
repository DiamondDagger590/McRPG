package us.eunoians.mcrpg.combat.state;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("CombatStateSnapshot")
class CombatStateSnapshotTest {

    private static final NamespacedKey KEY = new NamespacedKey("mcrpg", "frenzy_stacks");
    private static final CombatStateType<Integer> TYPE = CombatStateType.of(KEY, Integer.class, 0, null);

    @Test
    @DisplayName("getState returns the resolved value for a known key")
    void getState_returnsResolvedValue_forKnownKey() {
        CombatStateSnapshot snapshot = new CombatStateSnapshot(Map.of(KEY, 3), Map.of(KEY, 7));

        assertEquals(7, snapshot.getState(TYPE));
    }

    @Test
    @DisplayName("getRawState returns the raw value for a known key")
    void getRawState_returnsRawValue_forKnownKey() {
        CombatStateSnapshot snapshot = new CombatStateSnapshot(Map.of(KEY, 3), Map.of(KEY, 7));

        assertEquals(3, snapshot.getRawState(TYPE));
    }

    @Test
    @DisplayName("getState returns the type's default value for an absent key")
    void getState_returnsDefault_forAbsentKey() {
        CombatStateSnapshot snapshot = new CombatStateSnapshot(Map.of(), Map.of());

        assertEquals(TYPE.getDefaultValue(), snapshot.getState(TYPE));
    }

    @Test
    @DisplayName("getRawState returns the type's default value for an absent key")
    void getRawState_returnsDefault_forAbsentKey() {
        CombatStateSnapshot snapshot = new CombatStateSnapshot(Map.of(), Map.of());

        assertEquals(TYPE.getDefaultValue(), snapshot.getRawState(TYPE));
    }

    @Test
    @DisplayName("hasState returns true for a stored key and false for an absent key")
    void hasState_reflectsPresence() {
        CombatStateSnapshot snapshot = new CombatStateSnapshot(Map.of(KEY, 3), Map.of(KEY, 3));

        assertTrue(snapshot.hasState(KEY));
        assertFalse(snapshot.hasState(new NamespacedKey("mcrpg", "other")));
    }

    @Test
    @DisplayName("getStateKeys returns all keys in the snapshot")
    void getStateKeys_returnsAllKeys() {
        NamespacedKey otherKey = new NamespacedKey("mcrpg", "other");
        CombatStateSnapshot snapshot = new CombatStateSnapshot(
                Map.of(KEY, 3, otherKey, 1), Map.of(KEY, 3, otherKey, 1));

        assertEquals(Set.of(KEY, otherKey), snapshot.getStateKeys());
    }
}
