package us.eunoians.mcrpg.listener.quest;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.bukkit.event.HandlerList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.event.quest.QuestCompleteEvent;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.quest.QuestTestHelper;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.definition.QuestDefinitionRegistry;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.quest.impl.QuestState;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.quest.board.QuestBoardTerminator;
import us.eunoians.mcrpg.quest.board.distribution.DistributionCompletionService;
import us.eunoians.mcrpg.quest.board.distribution.QuestContributionAggregator;
import us.eunoians.mcrpg.quest.board.distribution.QuestRewardDistributionResolver;
import us.eunoians.mcrpg.quest.board.distribution.RewardDistributionGranter;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

public class QuestCompleteListenerTest extends McRPGBaseTest {

    private QuestManager mockQuestManager;

    @AfterEach
    public void tearDown() {
        HandlerList.unregisterAll(mcRPG);
    }

    @BeforeEach
    public void setup() {
        HandlerList.unregisterAll(mcRPG);
        server.getPluginManager().clearEvents();
        var rarityReg = RegistryAccess.registryAccess().registry(McRPGRegistryKey.QUEST_RARITY);
        var distTypeReg = RegistryAccess.registryAccess().registry(McRPGRegistryKey.REWARD_DISTRIBUTION_TYPE);
        var aggregator = new QuestContributionAggregator();
        var resolver = new QuestRewardDistributionResolver(java.util.logging.Logger.getLogger("test"));
        var distService = new DistributionCompletionService(rarityReg, distTypeReg, new RewardDistributionGranter(mcRPG), aggregator, resolver);
        server.getPluginManager().registerEvents(new QuestCompleteListener(new QuestBoardTerminator(mcRPG), distService, aggregator), mcRPG);

        mockQuestManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.QUEST);
    }

    @DisplayName("Given a completed quest, when QuestCompleteEvent fires, then retireQuest is called")
    @Test
    public void onQuestComplete_retiresQuest() {
        QuestDefinition def = QuestTestHelper.singlePhaseQuest("complete_listener_test");
        QuestInstance quest = QuestTestHelper.startedQuestInstance(def);

        server.getPluginManager().callEvent(new QuestCompleteEvent(quest, def));
        verify(mockQuestManager).retireQuest(any(QuestInstance.class));
    }

    @DisplayName("Given a quest with a gen_ prefix key registered in the definition registry, when QuestCompleteEvent fires, then the ephemeral definition is deregistered")
    @Test
    public void onQuestComplete_deregistersEphemeralDefinition_whenKeyHasGenPrefix() {
        QuestDefinitionRegistry definitionRegistry = RegistryAccess.registryAccess()
                .registry(McRPGRegistryKey.QUEST_DEFINITION);
        QuestDefinition ephemeralDef = QuestTestHelper.singlePhaseQuest("gen_ephemeral_complete");
        definitionRegistry.register(ephemeralDef);

        QuestInstance quest = QuestTestHelper.startedQuestInstance(ephemeralDef);
        server.getPluginManager().callEvent(new QuestCompleteEvent(quest, ephemeralDef));

        assertFalse(
                definitionRegistry.get(new NamespacedKey("mcrpg", "gen_ephemeral_complete")).isPresent(),
                "Ephemeral definition must be removed from the registry on quest completion");
    }
}
