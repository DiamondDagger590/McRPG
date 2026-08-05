package us.eunoians.mcrpg.entity.player;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.ability.AbilityData;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityTierAttribute;
import us.eunoians.mcrpg.ability.impl.type.SkillAbility;
import us.eunoians.mcrpg.ability.impl.type.TierableAbility;
import us.eunoians.mcrpg.entity.holder.SkillHolder;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link McRPGPlayer#canPlayerStartUpgradeQuest(TierableAbility)}.
 */
@ExtendWith(McRPGPlayerExtension.class)
@DisplayName("McRPGPlayer#canPlayerStartUpgradeQuest")
class McRPGPlayerCanStartUpgradeQuestTest extends McRPGBaseTest {

    private static final NamespacedKey ABILITY_KEY = new NamespacedKey("mcrpg", "test_ability");
    private static final NamespacedKey SKILL_KEY = new NamespacedKey("mcrpg", "test_skill");

    private SkillHolder skillHolderSpy;

    @BeforeEach
    void setupSkillHolderSpy(McRPGPlayer mcRPGPlayer) throws Exception {
        SkillHolder realHolder = mcRPGPlayer.asSkillHolder();
        skillHolderSpy = spy(realHolder);
        Field skillHolderField = McRPGPlayer.class.getDeclaredField("skillHolder");
        skillHolderField.setAccessible(true);
        skillHolderField.set(mcRPGPlayer, skillHolderSpy);
    }

    @Nested
    @DisplayName("Returns false")
    class ReturnsFalse {

        @Test
        @DisplayName("Given no ability data is present for the ability, then returns false")
        void canPlayerStartUpgradeQuest_returnsFalse_whenNoAbilityData(McRPGPlayer mcRPGPlayer) {
            TierableAbility ability = mockTierableAbility(3, 2);
            doReturn(Optional.empty()).when(skillHolderSpy).getAbilityData(ability);

            assertFalse(mcRPGPlayer.canPlayerStartUpgradeQuest(ability));
        }

        @Test
        @DisplayName("Given the player already has an active upgrade quest, then returns false")
        void canPlayerStartUpgradeQuest_returnsFalse_whenActiveUpgradeQuestExists(McRPGPlayer mcRPGPlayer) {
            TierableAbility ability = mockTierableAbility(3, 2);
            AbilityData abilityData = mockAbilityDataWithTier(1);
            doReturn(Optional.of(abilityData)).when(skillHolderSpy).getAbilityData(ability);
            doReturn(true).when(skillHolderSpy).hasActiveUpgradeQuest(ABILITY_KEY);

            assertFalse(mcRPGPlayer.canPlayerStartUpgradeQuest(ability));
        }

        @Test
        @DisplayName("Given the ability data has no tier attribute, then returns false")
        void canPlayerStartUpgradeQuest_returnsFalse_whenNoTierAttribute(McRPGPlayer mcRPGPlayer) {
            TierableAbility ability = mockTierableAbility(3, 2);
            AbilityData abilityData = mock(AbilityData.class);
            when(abilityData.getAbilityAttribute(AbilityAttributeRegistry.ABILITY_TIER_ATTRIBUTE_KEY))
                    .thenReturn(Optional.empty());
            doReturn(Optional.of(abilityData)).when(skillHolderSpy).getAbilityData(ability);
            doReturn(false).when(skillHolderSpy).hasActiveUpgradeQuest(ABILITY_KEY);

            assertFalse(mcRPGPlayer.canPlayerStartUpgradeQuest(ability));
        }

        @Test
        @DisplayName("Given the player is already at max tier, then returns false")
        void canPlayerStartUpgradeQuest_returnsFalse_whenAtMaxTier(McRPGPlayer mcRPGPlayer) {
            TierableAbility ability = mockTierableAbility(3, 2);
            AbilityData abilityData = mockAbilityDataWithTier(3);
            doReturn(Optional.of(abilityData)).when(skillHolderSpy).getAbilityData(ability);
            doReturn(false).when(skillHolderSpy).hasActiveUpgradeQuest(ABILITY_KEY);

            assertFalse(mcRPGPlayer.canPlayerStartUpgradeQuest(ability));
        }

        @Test
        @DisplayName("Given a SkillAbility where the player's level is below unlock level for next tier, then returns false")
        void canPlayerStartUpgradeQuest_returnsFalse_whenSkillLevelTooLow(McRPGPlayer mcRPGPlayer) {
            TierableSkillAbility ability = mockTierableSkillAbility(5, 50);
            AbilityData abilityData = mockAbilityDataWithTier(1);
            doReturn(Optional.of(abilityData)).when(skillHolderSpy).getAbilityData(ability);
            doReturn(false).when(skillHolderSpy).hasActiveUpgradeQuest(ABILITY_KEY);

            SkillHolder.SkillHolderData skillData = mock(SkillHolder.SkillHolderData.class);
            when(skillData.getCurrentLevel()).thenReturn(10);
            doReturn(Optional.of(skillData)).when(skillHolderSpy).getSkillHolderData(SKILL_KEY);

            assertFalse(mcRPGPlayer.canPlayerStartUpgradeQuest(ability));
        }

        @Test
        @DisplayName("Given a SkillAbility where no skill data exists, then returns false")
        void canPlayerStartUpgradeQuest_returnsFalse_whenNoSkillData(McRPGPlayer mcRPGPlayer) {
            TierableSkillAbility ability = mockTierableSkillAbility(5, 20);
            AbilityData abilityData = mockAbilityDataWithTier(1);
            doReturn(Optional.of(abilityData)).when(skillHolderSpy).getAbilityData(ability);
            doReturn(false).when(skillHolderSpy).hasActiveUpgradeQuest(ABILITY_KEY);
            doReturn(Optional.empty()).when(skillHolderSpy).getSkillHolderData(SKILL_KEY);

            assertFalse(mcRPGPlayer.canPlayerStartUpgradeQuest(ability));
        }
    }

    @Nested
    @DisplayName("Returns true")
    class ReturnsTrue {

        @Test
        @DisplayName("Given a non-SkillAbility tierable ability with room to upgrade, then returns true")
        void canPlayerStartUpgradeQuest_returnsTrue_whenNonSkillAbilityCanUpgrade(McRPGPlayer mcRPGPlayer) {
            TierableAbility ability = mockTierableAbility(5, 2);
            AbilityData abilityData = mockAbilityDataWithTier(2);
            doReturn(Optional.of(abilityData)).when(skillHolderSpy).getAbilityData(ability);
            doReturn(false).when(skillHolderSpy).hasActiveUpgradeQuest(ABILITY_KEY);

            assertTrue(mcRPGPlayer.canPlayerStartUpgradeQuest(ability));
        }

        @Test
        @DisplayName("Given a SkillAbility where skill level meets the unlock threshold, then returns true")
        void canPlayerStartUpgradeQuest_returnsTrue_whenSkillLevelMeetsThreshold(McRPGPlayer mcRPGPlayer) {
            TierableSkillAbility ability = mockTierableSkillAbility(5, 20);
            AbilityData abilityData = mockAbilityDataWithTier(1);
            doReturn(Optional.of(abilityData)).when(skillHolderSpy).getAbilityData(ability);
            doReturn(false).when(skillHolderSpy).hasActiveUpgradeQuest(ABILITY_KEY);

            SkillHolder.SkillHolderData skillData = mock(SkillHolder.SkillHolderData.class);
            when(skillData.getCurrentLevel()).thenReturn(25);
            doReturn(Optional.of(skillData)).when(skillHolderSpy).getSkillHolderData(SKILL_KEY);

            assertTrue(mcRPGPlayer.canPlayerStartUpgradeQuest(ability));
        }

        @Test
        @DisplayName("Given a SkillAbility where skill level equals the unlock threshold exactly, then returns true")
        void canPlayerStartUpgradeQuest_returnsTrue_whenSkillLevelExactlyMeetsThreshold(McRPGPlayer mcRPGPlayer) {
            TierableSkillAbility ability = mockTierableSkillAbility(5, 20);
            AbilityData abilityData = mockAbilityDataWithTier(1);
            doReturn(Optional.of(abilityData)).when(skillHolderSpy).getAbilityData(ability);
            doReturn(false).when(skillHolderSpy).hasActiveUpgradeQuest(ABILITY_KEY);

            SkillHolder.SkillHolderData skillData = mock(SkillHolder.SkillHolderData.class);
            when(skillData.getCurrentLevel()).thenReturn(20);
            doReturn(Optional.of(skillData)).when(skillHolderSpy).getSkillHolderData(SKILL_KEY);

            assertTrue(mcRPGPlayer.canPlayerStartUpgradeQuest(ability));
        }

        @Test
        @DisplayName("Given a player at tier 1 with max tier 2, then returns true when conditions are met")
        void canPlayerStartUpgradeQuest_returnsTrue_whenOneUpgradeRemaining(McRPGPlayer mcRPGPlayer) {
            TierableAbility ability = mockTierableAbility(2, 2);
            AbilityData abilityData = mockAbilityDataWithTier(1);
            doReturn(Optional.of(abilityData)).when(skillHolderSpy).getAbilityData(ability);
            doReturn(false).when(skillHolderSpy).hasActiveUpgradeQuest(ABILITY_KEY);

            assertTrue(mcRPGPlayer.canPlayerStartUpgradeQuest(ability));
        }
    }

    private interface TierableSkillAbility extends TierableAbility, SkillAbility {}

    private TierableAbility mockTierableAbility(int maxTier, int unlockLevel) {
        TierableAbility ability = mock(TierableAbility.class);
        when(ability.getAbilityKey()).thenReturn(ABILITY_KEY);
        when(ability.getMaxTier()).thenReturn(maxTier);
        when(ability.getUnlockLevelForTier(any(int.class))).thenReturn(unlockLevel);
        return ability;
    }

    private TierableSkillAbility mockTierableSkillAbility(int maxTier, int unlockLevel) {
        TierableSkillAbility ability = mock(TierableSkillAbility.class);
        when(ability.getAbilityKey()).thenReturn(ABILITY_KEY);
        when(ability.getMaxTier()).thenReturn(maxTier);
        when(ability.getUnlockLevelForTier(any(int.class))).thenReturn(unlockLevel);
        when(ability.getSkillKey()).thenReturn(SKILL_KEY);
        return ability;
    }

    private AbilityData mockAbilityDataWithTier(int tier) {
        AbilityData abilityData = mock(AbilityData.class);
        AbilityTierAttribute tierAttribute = mock(AbilityTierAttribute.class);
        when(tierAttribute.getContent()).thenReturn(tier);
        when(abilityData.getAbilityAttribute(AbilityAttributeRegistry.ABILITY_TIER_ATTRIBUTE_KEY))
                .thenReturn(Optional.of(tierAttribute));
        return abilityData;
    }
}
