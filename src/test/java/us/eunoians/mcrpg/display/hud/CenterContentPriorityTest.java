package us.eunoians.mcrpg.display.hud;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("CenterContentPriority")
class CenterContentPriorityTest {

    @Test
    @DisplayName("AMBIENT_FEEDBACK is higher than COMBAT_EXIT_FEEDBACK")
    void ambientFeedback_isHigherThanCombatExitFeedback() {
        assertTrue(CenterContentPriority.AMBIENT_FEEDBACK > CenterContentPriority.COMBAT_EXIT_FEEDBACK);
    }

    @Test
    @DisplayName("SAFE_ZONE_TRANSITION is higher than AMBIENT_FEEDBACK")
    void safeZoneTransition_isHigherThanAmbientFeedback() {
        assertTrue(CenterContentPriority.SAFE_ZONE_TRANSITION > CenterContentPriority.AMBIENT_FEEDBACK);
    }

    @Test
    @DisplayName("ABILITY_FEEDBACK is higher than SAFE_ZONE_TRANSITION")
    void abilityFeedback_isHigherThanSafeZoneTransition() {
        assertTrue(CenterContentPriority.ABILITY_FEEDBACK > CenterContentPriority.SAFE_ZONE_TRANSITION);
    }

    @Test
    @DisplayName("COMBO_STATE is higher than ABILITY_FEEDBACK")
    void comboState_isHigherThanAbilityFeedback() {
        assertTrue(CenterContentPriority.COMBO_STATE > CenterContentPriority.ABILITY_FEEDBACK);
    }

    @Test
    @DisplayName("all priorities are positive")
    void allPriorities_arePositive() {
        assertTrue(CenterContentPriority.COMBAT_EXIT_FEEDBACK > 0);
        assertTrue(CenterContentPriority.AMBIENT_FEEDBACK > 0);
        assertTrue(CenterContentPriority.SAFE_ZONE_TRANSITION > 0);
        assertTrue(CenterContentPriority.ABILITY_FEEDBACK > 0);
        assertTrue(CenterContentPriority.COMBO_STATE > 0);
    }

    @Test
    @DisplayName("third-party gap between COMBAT_EXIT_FEEDBACK and AMBIENT_FEEDBACK exists")
    void gap_betweenCombatExitAndAmbient() {
        assertTrue(CenterContentPriority.AMBIENT_FEEDBACK - CenterContentPriority.COMBAT_EXIT_FEEDBACK > 1,
                "gap should allow third-party priorities between combat exit and ambient");
    }

    @Test
    @DisplayName("third-party gap between AMBIENT_FEEDBACK and SAFE_ZONE_TRANSITION exists")
    void gap_betweenAmbientAndSafeZone() {
        assertTrue(CenterContentPriority.SAFE_ZONE_TRANSITION - CenterContentPriority.AMBIENT_FEEDBACK > 1,
                "gap should allow third-party priorities between ambient and safe zone");
    }

    @Test
    @DisplayName("third-party gap between SAFE_ZONE_TRANSITION and ABILITY_FEEDBACK exists")
    void gap_betweenSafeZoneAndAbilityFeedback() {
        assertTrue(CenterContentPriority.ABILITY_FEEDBACK - CenterContentPriority.SAFE_ZONE_TRANSITION > 1,
                "gap should allow third-party priorities between safe zone and ability feedback");
    }

    @Test
    @DisplayName("third-party gap between ABILITY_FEEDBACK and COMBO_STATE exists")
    void gap_betweenAbilityFeedbackAndComboState() {
        assertTrue(CenterContentPriority.COMBO_STATE - CenterContentPriority.ABILITY_FEEDBACK > 1,
                "gap should allow third-party priorities between ability feedback and combo state");
    }

    @Test
    @DisplayName("COMBO_STATE is below 100 to leave room for emergency overrides")
    void comboState_isBelowHundred() {
        assertTrue(CenterContentPriority.COMBO_STATE < 100,
                "COMBO_STATE should be below 100 to leave room for third-party emergency overrides");
    }
}
