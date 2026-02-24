package us.eunoians.mcrpg.listener.quest;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.AbilityData;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityUpgradeQuestAttribute;
import us.eunoians.mcrpg.ability.impl.type.TierableAbility;
import us.eunoians.mcrpg.entity.McRPGPlayerManager;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.event.quest.QuestCancelEvent;
import us.eunoians.mcrpg.event.quest.QuestCompleteEvent;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.quest.QuestTestHelper;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AbilityUpgradeQuestListenerTest extends McRPGBaseTest {

    private UUID playerUUID;
    private AbilityData mockAbilityData;
    private McRPGPlayerManager mockPlayerManager;
    private AbilityRegistry mockAbilityRegistry;

    @BeforeEach
    public void setup() {
        server.getPluginManager().clearEvents();
        playerUUID = UUID.randomUUID();


        // Register the listener under test
        server.getPluginManager().registerEvents(new AbilityUpgradeQuestListener(), mcRPG);
    }

    private void setupAbilityMockChain(UUID questUUID) {
        mockPlayerManager = mock(McRPGPlayerManager.class);
        McRPGPlayer mockPlayer = mock(McRPGPlayer.class);
        SkillHolder mockSkillHolder = mock(SkillHolder.class);
        mockAbilityData = mock(AbilityData.class);

        when(mockPlayerManager.getPlayer(playerUUID)).thenReturn(Optional.of(mockPlayer));
        when(mockPlayer.asSkillHolder()).thenReturn(mockSkillHolder);

        NamespacedKey abilityKey = new NamespacedKey("mcrpg", "test_tierable_ability");
        TierableAbility mockAbility = mock(TierableAbility.class);
        when(mockAbility.getAbilityKey()).thenReturn(abilityKey);

        mockAbilityRegistry = mock(AbilityRegistry.class);
        when(mockAbilityRegistry.getAllAbilities()).thenReturn(Set.of(abilityKey));
        when(mockAbilityRegistry.getRegisteredAbility(abilityKey)).thenReturn(mockAbility);

        when(mockSkillHolder.getAbilityData(any(Ability.class))).thenReturn(Optional.of(mockAbilityData));

        AbilityUpgradeQuestAttribute questAttribute = new AbilityUpgradeQuestAttribute(questUUID);
        when(mockAbilityData.getAbilityAttribute(AbilityAttributeRegistry.ABILITY_QUEST_ATTRIBUTE))
                .thenReturn(Optional.of(questAttribute));

        // Use try/catch in case these are already registered from a prior test instance
        try {
            RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(mockPlayerManager);
        } catch (Exception ignored) {}
        try {
            RegistryAccess.registryAccess().register(mockAbilityRegistry);
        } catch (Exception ignored) {}
    }

    @DisplayName("Given a cancel event for an upgrade quest, when fired, then the matching attribute is cleared")
    @Test
    public void onQuestCancel_clearsMatchingAttribute() {
        QuestDefinition def = QuestTestHelper.singlePhaseQuest("upgrade_cancel_test");
        QuestInstance quest = QuestTestHelper.startedQuestWithPlayer(def, playerUUID);
        setupAbilityMockChain(quest.getQuestUUID());

        server.getPluginManager().callEvent(new QuestCancelEvent(quest));

        verify(mockAbilityData, atLeastOnce()).addAttribute(any(AbilityUpgradeQuestAttribute.class));
    }

    @DisplayName("Given a complete event for an upgrade quest, when fired, then the matching attribute is cleared")
    @Test
    public void onQuestComplete_clearsMatchingAttribute() {
        QuestDefinition def = QuestTestHelper.singlePhaseQuest("upgrade_complete_test");
        QuestInstance quest = QuestTestHelper.startedQuestWithPlayer(def, playerUUID);
        setupAbilityMockChain(quest.getQuestUUID());

        server.getPluginManager().callEvent(new QuestCompleteEvent(quest, def));

        verify(mockAbilityData, atLeastOnce()).addAttribute(any(AbilityUpgradeQuestAttribute.class));
    }

    @DisplayName("Given a cancel event, when attribute references a different quest, then attribute is NOT cleared")
    @Test
    public void onQuestCancel_doesNotClearNonMatchingAttribute() {
        QuestDefinition def = QuestTestHelper.singlePhaseQuest("upgrade_nomatch_test");
        QuestInstance quest = QuestTestHelper.startedQuestWithPlayer(def, playerUUID);
        UUID differentQuestUUID = UUID.randomUUID();
        setupAbilityMockChain(differentQuestUUID);

        server.getPluginManager().callEvent(new QuestCancelEvent(quest));

        verify(mockAbilityData, never()).addAttribute(any(AbilityUpgradeQuestAttribute.class));
    }

    @DisplayName("Given a cancel event for a quest with no scope, when fired, then it does not crash")
    @Test
    public void onQuestCancel_handlesNoScope_gracefully() {
        QuestDefinition def = QuestTestHelper.singlePhaseQuest("upgrade_noscope_test");
        QuestInstance quest = QuestTestHelper.newQuestInstance(def);

        server.getPluginManager().callEvent(new QuestCancelEvent(quest));
    }
}
