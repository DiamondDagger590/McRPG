package us.eunoians.mcrpg.quest.reward.builtin;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests that {@link AbilityUpgradeRewardType#describeForDisplay(McRPGPlayer)} and
 * {@link AbilityUpgradeNextTierRewardType#describeForDisplay(McRPGPlayer)} resolve the
 * {@code <ability>} placeholder through the ability registry rather than title-casing the key.
 */
@SuppressWarnings("unchecked")
class AbilityUpgradeRewardTypeDisplayTest extends McRPGBaseTest {

    private McRPGLocalizationManager localization;
    private AbilityRegistry abilityRegistry;
    private McRPGPlayer player;

    @BeforeEach
    void setup() {
        localization = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);

        abilityRegistry = mock(AbilityRegistry.class);
        RegistryAccess.registryAccess().register(abilityRegistry);

        player = mock(McRPGPlayer.class);
    }

    @Test
    @DisplayName("Given a registered ability, when describeForDisplay(player) is called, then the colored name is used")
    void describeForDisplay_registeredAbility_usesColoredName() {
        NamespacedKey abilityKey = new NamespacedKey("mcrpg", "enhanced_bleed");
        Ability mockAbility = mock(Ability.class);
        when(mockAbility.getColoredName(player)).thenReturn("<color:#7FB87F>Enhanced Bleed</color:#7FB87F>");

        when(abilityRegistry.registered(abilityKey)).thenReturn(true);
        when(abilityRegistry.getRegisteredAbility(abilityKey)).thenReturn(mockAbility);

        when(localization.getLocalizedMessage(eq(player), eq(LocalizationKey.QUEST_REWARD_ABILITY_UPGRADE_FORMAT), any(Map.class)))
                .thenAnswer(inv -> {
                    Map<String, String> vars = inv.getArgument(2);
                    return "Upgrade: " + vars.get("ability") + " (Tier " + vars.get("tier") + ")";
                });
        when(localization.getLocalizedMessage(eq(player), eq(LocalizationKey.QUEST_REWARD_DEFAULT_COLOR)))
                .thenReturn("");

        AbilityUpgradeRewardType reward = new AbilityUpgradeRewardType()
                .fromSerializedConfig(Map.of("ability", abilityKey.toString(), "tier", 3));

        String result = reward.describeForDisplay(player);

        assertTrue(result.contains("<color:#7FB87F>Enhanced Bleed</color:#7FB87F>"),
                "Expected colored ability name in display, got: " + result);
        assertTrue(result.contains("Tier 3"), "Expected tier in display, got: " + result);
    }

    @Test
    @DisplayName("Given an unregistered ability key, when describeForDisplay(player) is called, then the title-cased key is used as fallback")
    void describeForDisplay_unregisteredAbility_usesTitleCasedFallback() {
        NamespacedKey abilityKey = new NamespacedKey("mcrpg", "enhanced_bleed");
        when(abilityRegistry.registered(abilityKey)).thenReturn(false);

        when(localization.getLocalizedMessage(eq(player), eq(LocalizationKey.QUEST_REWARD_ABILITY_UPGRADE_FORMAT), any(Map.class)))
                .thenAnswer(inv -> {
                    Map<String, String> vars = inv.getArgument(2);
                    return "Upgrade: " + vars.get("ability") + " (Tier " + vars.get("tier") + ")";
                });
        when(localization.getLocalizedMessage(eq(player), eq(LocalizationKey.QUEST_REWARD_DEFAULT_COLOR)))
                .thenReturn("");

        AbilityUpgradeRewardType reward = new AbilityUpgradeRewardType()
                .fromSerializedConfig(Map.of("ability", abilityKey.toString(), "tier", 2));

        String result = reward.describeForDisplay(player);

        assertTrue(result.contains("Enhanced Bleed"), "Expected title-cased fallback in display, got: " + result);
    }

    @Test
    @DisplayName("Given a null ability key, when describeForDisplay() is called, then 'Unknown' is used")
    void describeForDisplay_nullAbilityKey_usesUnknown() {
        AbilityUpgradeRewardType reward = new AbilityUpgradeRewardType();

        String result = reward.describeForDisplay();

        assertEquals("Upgrade: Unknown (Tier 0)", result);
    }

    @Test
    @DisplayName("Given a registered ability for next-tier reward, when describeForDisplay(player) is called, then the colored name is used")
    void describeForDisplayNextTier_registeredAbility_usesColoredName() {
        NamespacedKey abilityKey = new NamespacedKey("mcrpg", "bleed");
        Ability mockAbility = mock(Ability.class);
        when(mockAbility.getColoredName(player)).thenReturn("<color:#FF7B5E>Bleed</color:#FF7B5E>");

        when(abilityRegistry.registered(abilityKey)).thenReturn(true);
        when(abilityRegistry.getRegisteredAbility(abilityKey)).thenReturn(mockAbility);

        when(localization.getLocalizedMessage(eq(player), eq(LocalizationKey.QUEST_REWARD_ABILITY_UPGRADE_NEXT_TIER_FORMAT), any(Map.class)))
                .thenAnswer(inv -> {
                    Map<String, String> vars = inv.getArgument(2);
                    return "Upgrade: " + vars.get("ability") + " (Next Tier)";
                });
        when(localization.getLocalizedMessage(eq(player), eq(LocalizationKey.QUEST_REWARD_DEFAULT_COLOR)))
                .thenReturn("");

        AbilityUpgradeNextTierRewardType reward = new AbilityUpgradeNextTierRewardType()
                .fromSerializedConfig(Map.of("ability", abilityKey.toString()));

        String result = reward.describeForDisplay(player);

        assertTrue(result.contains("<color:#FF7B5E>Bleed</color:#FF7B5E>"),
                "Expected colored ability name in next-tier display, got: " + result);
    }

    @Test
    @DisplayName("Given an unregistered ability for next-tier reward, when describeForDisplay(player) is called, then the title-cased key is used")
    void describeForDisplayNextTier_unregisteredAbility_usesTitleCasedFallback() {
        NamespacedKey abilityKey = new NamespacedKey("mcrpg", "deeper_wound");
        when(abilityRegistry.registered(abilityKey)).thenReturn(false);

        when(localization.getLocalizedMessage(eq(player), eq(LocalizationKey.QUEST_REWARD_ABILITY_UPGRADE_NEXT_TIER_FORMAT), any(Map.class)))
                .thenAnswer(inv -> {
                    Map<String, String> vars = inv.getArgument(2);
                    return "Upgrade: " + vars.get("ability") + " (Next Tier)";
                });
        when(localization.getLocalizedMessage(eq(player), eq(LocalizationKey.QUEST_REWARD_DEFAULT_COLOR)))
                .thenReturn("");

        AbilityUpgradeNextTierRewardType reward = new AbilityUpgradeNextTierRewardType()
                .fromSerializedConfig(Map.of("ability", abilityKey.toString()));

        String result = reward.describeForDisplay(player);

        assertTrue(result.contains("Deeper Wound"),
                "Expected title-cased fallback in next-tier display, got: " + result);
    }
}
