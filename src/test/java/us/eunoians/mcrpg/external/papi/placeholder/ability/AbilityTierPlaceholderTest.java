package us.eunoians.mcrpg.external.papi.placeholder.ability;

import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.ability.AbilityData;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityTierAttribute;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.entity.player.McRPGPlayerExtension;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("AbilityTierPlaceholder")
@ExtendWith(McRPGPlayerExtension.class)
class AbilityTierPlaceholderTest extends McRPGBaseTest {

    private static final NamespacedKey ABILITY_KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "test_ability");

    private OfflinePlayer offlinePlayer(UUID uuid) {
        OfflinePlayer offlinePlayer = mock(OfflinePlayer.class);
        when(offlinePlayer.getUniqueId()).thenReturn(uuid);
        return offlinePlayer;
    }

    @Test
    @DisplayName("returns the tier when the player has the ability with a tier attribute")
    void parsePlaceholder_returnsTier_whenAbilityDataAndTierAttributeExist(McRPGPlayer mcRPGPlayer) {
        SkillHolder skillHolder = mock(SkillHolder.class);
        doReturn(skillHolder).when(mcRPGPlayer).asSkillHolder();
        AbilityData abilityData = mock(AbilityData.class);
        AbilityTierAttribute tierAttribute = new AbilityTierAttribute(3);
        when(abilityData.getAbilityAttribute(AbilityAttributeRegistry.ABILITY_TIER_ATTRIBUTE_KEY))
                .thenReturn(Optional.of(tierAttribute));
        when(skillHolder.getAbilityData(ABILITY_KEY)).thenReturn(Optional.of(abilityData));

        AbilityTierPlaceholder placeholder = new AbilityTierPlaceholder(ABILITY_KEY);
        assertEquals("3", placeholder.parsePlaceholder(offlinePlayer(mcRPGPlayer.getUUID())));
    }

    @Test
    @DisplayName("returns null when the player is not loaded")
    void parsePlaceholder_returnsNull_whenPlayerNotLoaded() {
        AbilityTierPlaceholder placeholder = new AbilityTierPlaceholder(ABILITY_KEY);
        assertNull(placeholder.parsePlaceholder(offlinePlayer(UUID.randomUUID())));
    }

    @Test
    @DisplayName("returns null when the player has no ability data for the key")
    void parsePlaceholder_returnsNull_whenAbilityDataMissing(McRPGPlayer mcRPGPlayer) {
        SkillHolder skillHolder = mock(SkillHolder.class);
        doReturn(skillHolder).when(mcRPGPlayer).asSkillHolder();
        when(skillHolder.getAbilityData(ABILITY_KEY)).thenReturn(Optional.empty());

        AbilityTierPlaceholder placeholder = new AbilityTierPlaceholder(ABILITY_KEY);
        assertNull(placeholder.parsePlaceholder(offlinePlayer(mcRPGPlayer.getUUID())));
    }

    @Test
    @DisplayName("returns null when the ability data has no tier attribute")
    void parsePlaceholder_returnsNull_whenTierAttributeMissing(McRPGPlayer mcRPGPlayer) {
        SkillHolder skillHolder = mock(SkillHolder.class);
        doReturn(skillHolder).when(mcRPGPlayer).asSkillHolder();
        AbilityData abilityData = mock(AbilityData.class);
        when(abilityData.getAbilityAttribute(AbilityAttributeRegistry.ABILITY_TIER_ATTRIBUTE_KEY))
                .thenReturn(Optional.empty());
        when(skillHolder.getAbilityData(ABILITY_KEY)).thenReturn(Optional.of(abilityData));

        AbilityTierPlaceholder placeholder = new AbilityTierPlaceholder(ABILITY_KEY);
        assertNull(placeholder.parsePlaceholder(offlinePlayer(mcRPGPlayer.getUUID())));
    }

    @Test
    @DisplayName("identifier follows the expected naming pattern")
    void getIdentifier_matchesExpectedPattern() {
        AbilityTierPlaceholder placeholder = new AbilityTierPlaceholder(ABILITY_KEY);
        assertEquals("test_ability_tier", placeholder.getIdentifier());
    }
}
