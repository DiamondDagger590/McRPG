package us.eunoians.mcrpg.builder.item.ability;

import com.diamonddagger590.mccore.pair.Pair;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.AbilityData;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityTierAttribute;
import us.eunoians.mcrpg.ability.attribute.AbilityUnlockedAttribute;
import us.eunoians.mcrpg.ability.impl.type.TierableAbility;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.entity.player.McRPGPlayerExtension;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(McRPGPlayerExtension.class)
@DisplayName("AbilityLoreAppender")
class AbilityLoreAppenderTest extends McRPGBaseTest {

    @Nested
    @DisplayName("getAppendLore")
    class GetAppendLore {

        @Test
        @DisplayName("Given no ability data on holder, returns empty lore with ability name placeholder")
        void getAppendLore_returnsEmptyLore_whenNoAbilityData(@NotNull McRPGPlayer mcRPGPlayer) {
            NamespacedKey abilityKey = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "test-ability");
            Ability mockAbility = mock(Ability.class);
            when(mockAbility.getAbilityKey()).thenReturn(abilityKey);
            when(mockAbility.getName(mcRPGPlayer)).thenReturn("Test Ability");
            when(mockAbility.getExpansionKey()).thenReturn(Optional.empty());

            SkillHolder mockSkillHolder = mock(SkillHolder.class);
            when(mockSkillHolder.getAbilityData(mockAbility)).thenReturn(Optional.empty());
            doReturn(mockSkillHolder).when(mcRPGPlayer).asSkillHolder();

            Pair<List<String>, Map<String, String>> result = AbilityLoreAppender.getAppendLore(mcRPGPlayer, mockAbility);

            assertNotNull(result);
            assertTrue(result.getLeft().isEmpty());
            assertEquals("Test Ability", result.getRight().get("ability"));
        }

        @Test
        @DisplayName("Given ability data present but ability is not tierable and has no expansion, returns empty lore")
        void getAppendLore_returnsEmptyLore_whenNotTierableNoExpansion(@NotNull McRPGPlayer mcRPGPlayer) {
            NamespacedKey abilityKey = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "simple-ability");
            Ability mockAbility = mock(Ability.class);
            when(mockAbility.getAbilityKey()).thenReturn(abilityKey);
            when(mockAbility.getName(mcRPGPlayer)).thenReturn("Simple Ability");
            when(mockAbility.getExpansionKey()).thenReturn(Optional.empty());

            AbilityData mockData = mock(AbilityData.class);
            SkillHolder mockSkillHolder = mock(SkillHolder.class);
            when(mockSkillHolder.getAbilityData(mockAbility)).thenReturn(Optional.of(mockData));
            doReturn(mockSkillHolder).when(mcRPGPlayer).asSkillHolder();

            Pair<List<String>, Map<String, String>> result = AbilityLoreAppender.getAppendLore(mcRPGPlayer, mockAbility);

            assertNotNull(result);
            assertTrue(result.getLeft().isEmpty());
            assertEquals("Simple Ability", result.getRight().get("ability"));
        }

        @Test
        @DisplayName("Given a locked tierable ability, returns locked lore with unlock level placeholder")
        void getAppendLore_returnsLockedLore_whenAbilityLocked(@NotNull McRPGPlayer mcRPGPlayer) {
            addPlayerToServer(mcRPGPlayer);

            NamespacedKey abilityKey = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "locked-ability");
            TierableAbility mockAbility = mock(TierableAbility.class);
            when(mockAbility.getAbilityKey()).thenReturn(abilityKey);
            when(mockAbility.getName(mcRPGPlayer)).thenReturn("Locked Ability");
            when(mockAbility.getExpansionKey()).thenReturn(Optional.empty());
            when(mockAbility.getUnlockLevel()).thenReturn(10);

            AbilityUnlockedAttribute unlockedAttribute = new AbilityUnlockedAttribute(false);

            AbilityData mockData = mock(AbilityData.class);
            when(mockData.getAbilityAttribute(AbilityAttributeRegistry.ABILITY_UNLOCKED_ATTRIBUTE))
                    .thenReturn(Optional.of(unlockedAttribute));

            SkillHolder mockSkillHolder = mock(SkillHolder.class);
            when(mockSkillHolder.getAbilityData(mockAbility)).thenReturn(Optional.of(mockData));
            doReturn(mockSkillHolder).when(mcRPGPlayer).asSkillHolder();

            Pair<List<String>, Map<String, String>> result = AbilityLoreAppender.getAppendLore(mcRPGPlayer, mockAbility);

            assertNotNull(result);
            assertFalse(result.getLeft().isEmpty());
            assertEquals("10", result.getRight().get("ability-unlock-level"));
            assertEquals("Locked Ability", result.getRight().get("ability"));
        }

        @Test
        @DisplayName("Given an unlocked tierable ability at max tier with no quest, returns empty lore")
        void getAppendLore_returnsEmptyLore_whenUnlockedAtMaxTier(@NotNull McRPGPlayer mcRPGPlayer) {
            addPlayerToServer(mcRPGPlayer);

            NamespacedKey abilityKey = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "maxed-ability");
            TierableAbility mockAbility = mock(TierableAbility.class);
            when(mockAbility.getAbilityKey()).thenReturn(abilityKey);
            when(mockAbility.getName(mcRPGPlayer)).thenReturn("Maxed Ability");
            when(mockAbility.getExpansionKey()).thenReturn(Optional.empty());
            when(mockAbility.getMaxTier()).thenReturn(3);

            AbilityUnlockedAttribute unlockedAttribute = new AbilityUnlockedAttribute(true);
            AbilityTierAttribute tierAttribute = new AbilityTierAttribute(3);

            AbilityData mockData = mock(AbilityData.class);
            when(mockData.getAbilityAttribute(AbilityAttributeRegistry.ABILITY_UNLOCKED_ATTRIBUTE))
                    .thenReturn(Optional.of(unlockedAttribute));
            when(mockData.getAbilityAttribute(AbilityAttributeRegistry.ABILITY_QUEST_ATTRIBUTE))
                    .thenReturn(Optional.empty());
            when(mockData.getAbilityAttribute(AbilityAttributeRegistry.ABILITY_TIER_ATTRIBUTE_KEY))
                    .thenReturn(Optional.of(tierAttribute));

            SkillHolder mockSkillHolder = mock(SkillHolder.class);
            when(mockSkillHolder.getAbilityData(mockAbility)).thenReturn(Optional.of(mockData));
            doReturn(mockSkillHolder).when(mcRPGPlayer).asSkillHolder();

            Pair<List<String>, Map<String, String>> result = AbilityLoreAppender.getAppendLore(mcRPGPlayer, mockAbility);

            assertNotNull(result);
            assertTrue(result.getLeft().isEmpty());
            assertEquals("Maxed Ability", result.getRight().get("ability"));
        }
    }
}
