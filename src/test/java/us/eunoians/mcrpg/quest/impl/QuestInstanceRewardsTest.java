package us.eunoians.mcrpg.quest.impl;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.quest.QuestTestHelper;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.reward.MockQuestRewardType;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * Tests {@link QuestInstance#grantRewards} for the online player path.
 */
public class QuestInstanceRewardsTest extends McRPGBaseTest {

    @BeforeEach
    public void setup() {
        server.getPluginManager().clearEvents();
        if (!RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).registered(McRPGManagerKey.QUEST)) {
            RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(mock(QuestManager.class));
        }
    }

    @DisplayName("Given an online player in scope, when grantRewards is called, then rewards are granted")
    @Test
    public void grantRewards_grantsToOnlinePlayer() {
        QuestDefinition def = QuestTestHelper.singlePhaseQuest("reward_online_test");
        PlayerMock player = new PlayerMock(server, "RewardPlayer");
        server.addPlayer(player);

        QuestInstance instance = QuestTestHelper.startedQuestWithPlayer(def, player.getUniqueId());

        MockQuestRewardType reward = QuestTestHelper.mockRewardType("test_reward");
        instance.grantRewards(List.of(reward));

        assertEquals(1, reward.getGrantCount());
        assertEquals(player, reward.getGrantedTo().get(0));
    }

    @DisplayName("Given an empty rewards list, when grantRewards is called, then nothing happens")
    @Test
    public void grantRewards_skipsEmptyRewardsList() {
        QuestDefinition def = QuestTestHelper.singlePhaseQuest("reward_empty_test");
        PlayerMock player = new PlayerMock(server, "EmptyReward");
        server.addPlayer(player);

        QuestInstance instance = QuestTestHelper.startedQuestWithPlayer(def, player.getUniqueId());
        instance.grantRewards(List.of());
        // No exception = pass
    }

    @DisplayName("Given a quest with no scope, when grantRewards is called, then nothing happens")
    @Test
    public void grantRewards_handlesNoScope() {
        QuestDefinition def = QuestTestHelper.singlePhaseQuest("reward_noscope_test");
        QuestInstance instance = QuestTestHelper.newQuestInstance(def);
        MockQuestRewardType reward = QuestTestHelper.mockRewardType("noscope_reward");

        instance.grantRewards(List.of(reward));

        assertEquals(0, reward.getGrantCount());
    }

    @DisplayName("Given multiple rewards, when grantRewards is called, then all rewards are granted")
    @Test
    public void grantRewards_grantsMultipleRewards() {
        QuestDefinition def = QuestTestHelper.singlePhaseQuest("reward_multi_test");
        PlayerMock player = new PlayerMock(server, "MultiReward");
        server.addPlayer(player);

        QuestInstance instance = QuestTestHelper.startedQuestWithPlayer(def, player.getUniqueId());

        MockQuestRewardType reward1 = QuestTestHelper.mockRewardType("multi_reward_1");
        MockQuestRewardType reward2 = QuestTestHelper.mockRewardType("multi_reward_2");
        instance.grantRewards(List.of(reward1, reward2));

        assertEquals(1, reward1.getGrantCount());
        assertEquals(1, reward2.getGrantCount());
    }
}
