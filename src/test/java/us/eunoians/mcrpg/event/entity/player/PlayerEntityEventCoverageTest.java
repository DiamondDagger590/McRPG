package us.eunoians.mcrpg.event.entity.player;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.entity.player.McRPGPlayerExtension;
import us.eunoians.mcrpg.event.entity.player.PlayerSafeZoneStateChangeEvent.SafeZoneStateChangeType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(McRPGPlayerExtension.class)
public class PlayerEntityEventCoverageTest extends McRPGBaseTest {

    @Nested
    @DisplayName("PlayerAwardedRestedExperienceEvent")
    class PlayerAwardedRestedExperienceEventTests {

        @Test
        @DisplayName("getRestedExperience returns constructor value")
        void getRestedExperience_returnsConstructorValue(McRPGPlayer player) {
            var event = new PlayerAwardedRestedExperienceEvent(player, 50.0f, 100.0);
            assertEquals(50.0f, event.getRestedExperience());
        }

        @Test
        @DisplayName("getMaxAccumulation returns constructor value")
        void getMaxAccumulation_returnsConstructorValue(McRPGPlayer player) {
            var event = new PlayerAwardedRestedExperienceEvent(player, 50.0f, 200.0);
            assertEquals(200.0, event.getMaxAccumulation());
        }

        @Test
        @DisplayName("getMcRPGPlayer returns constructor value")
        void getMcRPGPlayer_returnsConstructorValue(McRPGPlayer player) {
            var event = new PlayerAwardedRestedExperienceEvent(player, 50.0f, 100.0);
            assertSame(player, event.getMcRPGPlayer());
        }

        @Test
        @DisplayName("negative restedExperience clamped to zero")
        void negativeRestedExperience_clampedToZero(McRPGPlayer player) {
            var event = new PlayerAwardedRestedExperienceEvent(player, -10.0f, 100.0);
            assertEquals(0.0f, event.getRestedExperience());
        }

        @Test
        @DisplayName("negative maxAccumulation clamped to zero")
        void negativeMaxAccumulation_clampedToZero(McRPGPlayer player) {
            var event = new PlayerAwardedRestedExperienceEvent(player, 50.0f, -5.0);
            assertEquals(0.0, event.getMaxAccumulation());
        }

        @Test
        @DisplayName("setRestedExperience updates value")
        void setRestedExperience_updatesValue(McRPGPlayer player) {
            var event = new PlayerAwardedRestedExperienceEvent(player, 50.0f, 100.0);
            event.setRestedExperience(75.0);
            assertEquals(75.0f, event.getRestedExperience());
        }

        @Test
        @DisplayName("setRestedExperience clamps to zero when negative")
        void setRestedExperience_clampsToZero_whenNegative(McRPGPlayer player) {
            var event = new PlayerAwardedRestedExperienceEvent(player, 50.0f, 100.0);
            event.setRestedExperience(-20.0);
            assertEquals(0.0f, event.getRestedExperience());
        }

        @Test
        @DisplayName("setRestedExperience clamps to maxAccumulation when exceeding")
        void setRestedExperience_clampsToMax_whenExceeding(McRPGPlayer player) {
            var event = new PlayerAwardedRestedExperienceEvent(player, 50.0f, 100.0);
            event.setRestedExperience(500.0);
            assertEquals(100.0f, event.getRestedExperience());
        }

        @Test
        @DisplayName("isCancelled defaults to false")
        void isCancelled_defaultsFalse(McRPGPlayer player) {
            var event = new PlayerAwardedRestedExperienceEvent(player, 50.0f, 100.0);
            assertFalse(event.isCancelled());
        }

        @Test
        @DisplayName("setCancelled changes state")
        void setCancelled_changesState(McRPGPlayer player) {
            var event = new PlayerAwardedRestedExperienceEvent(player, 50.0f, 100.0);
            event.setCancelled(true);
            assertTrue(event.isCancelled());
        }

        @Test
        @DisplayName("setCancelled can revert to false")
        void setCancelled_canRevert(McRPGPlayer player) {
            var event = new PlayerAwardedRestedExperienceEvent(player, 50.0f, 100.0);
            event.setCancelled(true);
            event.setCancelled(false);
            assertFalse(event.isCancelled());
        }

        @Test
        @DisplayName("getHandlers returns non-null")
        void getHandlers_returnsNonNull(McRPGPlayer player) {
            var event = new PlayerAwardedRestedExperienceEvent(player, 50.0f, 100.0);
            assertNotNull(event.getHandlers());
        }

        @Test
        @DisplayName("getHandlerList returns non-null")
        void getHandlerList_returnsNonNull() {
            assertNotNull(McRPGPlayerEvent.getHandlerList());
        }

        @Test
        @DisplayName("constructor does not clamp restedExperience to maxAccumulation")
        void constructor_doesNotClampToMax(McRPGPlayer player) {
            var event = new PlayerAwardedRestedExperienceEvent(player, 200.0f, 100.0);
            assertEquals(200.0f, event.getRestedExperience());
        }

        @Test
        @DisplayName("zero restedExperience is preserved")
        void zeroRestedExperience_isPreserved(McRPGPlayer player) {
            var event = new PlayerAwardedRestedExperienceEvent(player, 0.0f, 100.0);
            assertEquals(0.0f, event.getRestedExperience());
        }

        @Test
        @DisplayName("setRestedExperience at exactly max is preserved")
        void setRestedExperience_atMax_isPreserved(McRPGPlayer player) {
            var event = new PlayerAwardedRestedExperienceEvent(player, 50.0f, 100.0);
            event.setRestedExperience(100.0);
            assertEquals(100.0f, event.getRestedExperience());
        }
    }

    @Nested
    @DisplayName("PlayerSafeZoneStateChangeEvent")
    class PlayerSafeZoneStateChangeEventTests {

        @Test
        @DisplayName("getSafeZoneStateChangeType returns ENTERED")
        void getSafeZoneStateChangeType_returnsEntered(McRPGPlayer player) {
            var event = new PlayerSafeZoneStateChangeEvent(player, SafeZoneStateChangeType.ENTERED);
            assertEquals(SafeZoneStateChangeType.ENTERED, event.getSafeZoneStateChangeType());
        }

        @Test
        @DisplayName("getSafeZoneStateChangeType returns LEFT")
        void getSafeZoneStateChangeType_returnsLeft(McRPGPlayer player) {
            var event = new PlayerSafeZoneStateChangeEvent(player, SafeZoneStateChangeType.LEFT);
            assertEquals(SafeZoneStateChangeType.LEFT, event.getSafeZoneStateChangeType());
        }

        @Test
        @DisplayName("getMcRPGPlayer returns constructor value")
        void getMcRPGPlayer_returnsConstructorValue(McRPGPlayer player) {
            var event = new PlayerSafeZoneStateChangeEvent(player, SafeZoneStateChangeType.ENTERED);
            assertSame(player, event.getMcRPGPlayer());
        }

        @Test
        @DisplayName("getHandlers returns non-null")
        void getHandlers_returnsNonNull(McRPGPlayer player) {
            var event = new PlayerSafeZoneStateChangeEvent(player, SafeZoneStateChangeType.ENTERED);
            assertNotNull(event.getHandlers());
        }

        @ParameterizedTest
        @EnumSource(SafeZoneStateChangeType.class)
        @DisplayName("all SafeZoneStateChangeType values are usable")
        void allSafeZoneStateChangeTypes_areUsable(SafeZoneStateChangeType type, McRPGPlayer player) {
            var event = new PlayerSafeZoneStateChangeEvent(player, type);
            assertEquals(type, event.getSafeZoneStateChangeType());
        }
    }

    @Nested
    @DisplayName("SafeZoneStateChangeType")
    class SafeZoneStateChangeTypeTests {

        @Test
        @DisplayName("enum has exactly two values")
        void enumHasTwoValues() {
            assertEquals(2, SafeZoneStateChangeType.values().length);
        }

        @ParameterizedTest
        @EnumSource(SafeZoneStateChangeType.class)
        @DisplayName("valueOf round-trips")
        void valueOf_roundTrips(SafeZoneStateChangeType type) {
            assertEquals(type, SafeZoneStateChangeType.valueOf(type.name()));
        }
    }
}
