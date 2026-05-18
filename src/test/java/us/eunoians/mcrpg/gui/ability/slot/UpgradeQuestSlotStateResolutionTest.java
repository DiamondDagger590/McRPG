package us.eunoians.mcrpg.gui.ability.slot;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.bukkit.event.inventory.ClickType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.ability.AbilityData;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.StubTierableAbility;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityTierAttribute;
import us.eunoians.mcrpg.ability.attribute.AbilityUpgradeQuestAttribute;
import us.eunoians.mcrpg.ability.impl.MockAbility;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.entity.player.McRPGPlayerExtension;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.quest.QuestTestHelper;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests state resolution for {@link AbilityUpgradeQuestAttribute#getSlot(McRPGPlayer, us.eunoians.mcrpg.ability.Ability)}.
 */
@ExtendWith(McRPGPlayerExtension.class)
class UpgradeQuestSlotStateResolutionTest extends McRPGBaseTest {

    private StubTierableAbility ability;
    private QuestManager questManager;

    @BeforeEach
    void setup() {
        ability = new StubTierableAbility(mcRPG, new NamespacedKey("mcrpg", "test_tierable_ability"));
        questManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.QUEST);
        // AbilityHolder.addAbilityData() / getAbilityData() validates ability existence via the registry.
        // Register a permissive mock so the validation passes for stub abilities.
        // getRegisteredAbility() must also be configured: when ability data is absent, AbilityHolder
        // auto-creates default data by calling getRegisteredAbility(key).getApplicableAttributes().
        AbilityRegistry mockAbilityRegistry = mock(AbilityRegistry.class);
        when(mockAbilityRegistry.registered(any(NamespacedKey.class))).thenReturn(true);
        when(mockAbilityRegistry.getRegisteredAbility(any(NamespacedKey.class))).thenReturn(ability);
        RegistryAccess.registryAccess().register(mockAbilityRegistry);
    }

    @Test
    @DisplayName("Non-TierableAbility throws IllegalArgumentException")
    void getSlot_nonTierableAbility_throwsIllegalArgumentException(McRPGPlayer player) {
        MockAbility nonTierable = new MockAbility(mcRPG);
        AbilityUpgradeQuestAttribute attribute = new AbilityUpgradeQuestAttribute(AbilityUpgradeQuestAttribute.defaultUUID());
        assertThrows(IllegalArgumentException.class, () -> attribute.getSlot(player, nonTierable));
    }

    @Test
    @DisplayName("Current tier >= max tier resolves MAX_TIER_REACHED")
    void getSlot_currentTierAtMax_resolvesMaxTierReached(McRPGPlayer player) {
        ability.withMaxTier(3);
        AbilityData abilityData = new AbilityData(ability.getAbilityKey(),
                new AbilityTierAttribute(3));
        player.asSkillHolder().addAbilityData(abilityData);

        AbilityUpgradeQuestAttribute attribute = new AbilityUpgradeQuestAttribute(AbilityUpgradeQuestAttribute.defaultUUID());
        McRPGSlot slot = attribute.getSlot(player, ability);

        assertInstanceOf(UpgradeQuestSlot.class, slot);
        assertEquals(UpgradeQuestSlot.SlotState.MAX_TIER_REACHED,
                ((UpgradeQuestSlot) slot).getSlotState());
    }

    @Test
    @DisplayName("Current tier exceeds max tier resolves MAX_TIER_REACHED")
    void getSlot_currentTierAboveMax_resolvesMaxTierReached(McRPGPlayer player) {
        ability.withMaxTier(3);
        AbilityData abilityData = new AbilityData(ability.getAbilityKey(),
                new AbilityTierAttribute(5));
        player.asSkillHolder().addAbilityData(abilityData);

        AbilityUpgradeQuestAttribute attribute = new AbilityUpgradeQuestAttribute(AbilityUpgradeQuestAttribute.defaultUUID());
        McRPGSlot slot = attribute.getSlot(player, ability);

        assertInstanceOf(UpgradeQuestSlot.class, slot);
        assertEquals(UpgradeQuestSlot.SlotState.MAX_TIER_REACHED,
                ((UpgradeQuestSlot) slot).getSlotState());
    }

    @Test
    @DisplayName("Active quest UUID stored and quest found in QuestManager resolves ACTIVE_QUEST")
    void getSlot_activeQuestFound_resolvesActiveQuest(McRPGPlayer player) {
        ability.withMaxTier(5);
        QuestDefinition def = QuestTestHelper.singlePhaseQuest("upgrade_active_quest");
        QuestInstance questInstance = QuestTestHelper.startedQuestWithPlayer(def, player.getUUID());

        AbilityUpgradeQuestAttribute attribute = new AbilityUpgradeQuestAttribute(questInstance.getQuestUUID());
        AbilityData abilityData = new AbilityData(ability.getAbilityKey(),
                new AbilityTierAttribute(1), attribute);
        player.asSkillHolder().addAbilityData(abilityData);

        when(questManager.getActiveQuestsForPlayer(player.getUUID())).thenReturn(List.of(questInstance));

        McRPGSlot slot = attribute.getSlot(player, ability);

        assertInstanceOf(UpgradeQuestSlot.class, slot);
        assertEquals(UpgradeQuestSlot.SlotState.ACTIVE_QUEST,
                ((UpgradeQuestSlot) slot).getSlotState());
    }

    @Test
    @DisplayName("Orphaned quest UUID (no matching quest) resolves LOCKED_BEHIND_LEVEL")
    void getSlot_orphanedQuestUUID_resolvesLockedBehindLevel(McRPGPlayer player) {
        ability.withMaxTier(5);
        AbilityUpgradeQuestAttribute attribute = new AbilityUpgradeQuestAttribute(UUID.randomUUID());
        AbilityData abilityData = new AbilityData(ability.getAbilityKey(),
                new AbilityTierAttribute(1), attribute);
        player.asSkillHolder().addAbilityData(abilityData);

        when(questManager.getActiveQuestsForPlayer(player.getUUID())).thenReturn(List.of());

        McRPGSlot slot = attribute.getSlot(player, ability);

        assertInstanceOf(UpgradeQuestSlot.class, slot);
        assertEquals(UpgradeQuestSlot.SlotState.LOCKED_BEHIND_LEVEL,
                ((UpgradeQuestSlot) slot).getSlotState());
    }

    @Test
    @DisplayName("Orphaned UUID self-heals: attribute reset to default after getSlot()")
    void getSlot_orphanedQuestUUID_clearsAttribute(McRPGPlayer player) {
        ability.withMaxTier(5);
        AbilityUpgradeQuestAttribute attribute = new AbilityUpgradeQuestAttribute(UUID.randomUUID());
        AbilityData abilityData = new AbilityData(ability.getAbilityKey(),
                new AbilityTierAttribute(1), attribute);
        player.asSkillHolder().addAbilityData(abilityData);

        when(questManager.getActiveQuestsForPlayer(player.getUUID())).thenReturn(List.of());

        attribute.getSlot(player, ability);

        var updatedAttribute = abilityData.getAbilityAttribute(AbilityAttributeRegistry.ABILITY_QUEST_ATTRIBUTE);
        assertFalse(updatedAttribute.map(a -> ((AbilityUpgradeQuestAttribute) a).shouldContentBeSaved()).orElse(true),
                "Orphaned attribute should be cleared (shouldContentBeSaved must be false)");
    }

    @Test
    @DisplayName("No active quest UUID (default sentinel) resolves LOCKED_BEHIND_LEVEL")
    void getSlot_noActiveQuestUUID_resolvesLockedBehindLevel(McRPGPlayer player) {
        ability.withMaxTier(5);
        AbilityUpgradeQuestAttribute attribute = new AbilityUpgradeQuestAttribute(AbilityUpgradeQuestAttribute.defaultUUID());
        AbilityData abilityData = new AbilityData(ability.getAbilityKey(),
                new AbilityTierAttribute(1), attribute);
        player.asSkillHolder().addAbilityData(abilityData);

        McRPGSlot slot = attribute.getSlot(player, ability);

        assertInstanceOf(UpgradeQuestSlot.class, slot);
        assertEquals(UpgradeQuestSlot.SlotState.LOCKED_BEHIND_LEVEL,
                ((UpgradeQuestSlot) slot).getSlotState());
    }

    @Test
    @DisplayName("Missing ability data resolves LOCKED_BEHIND_LEVEL")
    void getSlot_missingAbilityData_resolvesLockedBehindLevel(McRPGPlayer player) {
        ability.withMaxTier(5);
        AbilityUpgradeQuestAttribute attribute = new AbilityUpgradeQuestAttribute(AbilityUpgradeQuestAttribute.defaultUUID());

        McRPGSlot slot = attribute.getSlot(player, ability);

        assertInstanceOf(UpgradeQuestSlot.class, slot);
        assertEquals(UpgradeQuestSlot.SlotState.LOCKED_BEHIND_LEVEL,
                ((UpgradeQuestSlot) slot).getSlotState());
    }
}
