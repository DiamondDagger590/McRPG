package us.eunoians.mcrpg.event.quest;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.QuestTestHelper;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.quest.reward.MockQuestRewardType;
import us.eunoians.mcrpg.quest.reward.QuestRewardType;
import us.eunoians.mcrpg.quest.reward.RewardGrantContext;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link QuestRewardGrantEvent}.
 */
class QuestRewardGrantEventTest extends McRPGBaseTest {

    private static final NamespacedKey QUEST_KEY = new NamespacedKey("mcrpg", "reward_quest");

    @Nested
    @DisplayName("Constructor and getters")
    class ConstructorAndGetters {

        @Test
        @DisplayName("Given all parameters, When getters are called, Then they return the values passed to the constructor")
        void getters_returnConstructorValues() {
            QuestDefinition definition = QuestTestHelper.singlePhaseQuest("rge_getters");
            QuestInstance instance = QuestTestHelper.startedQuestInstance(definition);
            UUID playerUUID = UUID.randomUUID();
            List<QuestRewardType> rewards = new ArrayList<>();
            rewards.add(QuestTestHelper.mockRewardType("test_reward"));

            QuestRewardGrantEvent event = new QuestRewardGrantEvent(
                    instance, QUEST_KEY, playerUUID, rewards, RewardGrantContext.INLINE);

            assertSame(instance, event.getQuestInstance());
            assertEquals(QUEST_KEY, event.getQuestKey());
            assertEquals(playerUUID, event.getPlayerUUID());
            assertSame(rewards, event.getRewards());
            assertEquals(RewardGrantContext.INLINE, event.getContext());
        }

        @Test
        @DisplayName("Given null quest instance, When getQuestInstance is called, Then returns null")
        void getQuestInstance_returnsNull_whenConstructedWithNull() {
            UUID playerUUID = UUID.randomUUID();
            List<QuestRewardType> rewards = new ArrayList<>();

            QuestRewardGrantEvent event = new QuestRewardGrantEvent(
                    null, QUEST_KEY, playerUUID, rewards, RewardGrantContext.PENDING);

            assertNull(event.getQuestInstance());
        }

        @Test
        @DisplayName("Given DISTRIBUTION context, When getContext is called, Then returns DISTRIBUTION")
        void getContext_returnsDistribution() {
            UUID playerUUID = UUID.randomUUID();
            List<QuestRewardType> rewards = new ArrayList<>();

            QuestRewardGrantEvent event = new QuestRewardGrantEvent(
                    null, QUEST_KEY, playerUUID, rewards, RewardGrantContext.DISTRIBUTION);

            assertEquals(RewardGrantContext.DISTRIBUTION, event.getContext());
        }
    }

    @Nested
    @DisplayName("Cancellation")
    class Cancellation {

        @Test
        @DisplayName("Given a new event, When isCancelled is called, Then returns false")
        void isCancelled_returnsFalse_byDefault() {
            UUID playerUUID = UUID.randomUUID();
            List<QuestRewardType> rewards = new ArrayList<>();

            QuestRewardGrantEvent event = new QuestRewardGrantEvent(
                    null, QUEST_KEY, playerUUID, rewards, RewardGrantContext.INLINE);

            assertFalse(event.isCancelled());
        }

        @Test
        @DisplayName("Given setCancelled(true), When isCancelled is called, Then returns true")
        void isCancelled_returnsTrue_afterSetCancelledTrue() {
            UUID playerUUID = UUID.randomUUID();
            List<QuestRewardType> rewards = new ArrayList<>();

            QuestRewardGrantEvent event = new QuestRewardGrantEvent(
                    null, QUEST_KEY, playerUUID, rewards, RewardGrantContext.INLINE);
            event.setCancelled(true);

            assertTrue(event.isCancelled());
        }

        @Test
        @DisplayName("Given setCancelled(true) then setCancelled(false), When isCancelled is called, Then returns false")
        void isCancelled_returnsFalse_afterReenabling() {
            UUID playerUUID = UUID.randomUUID();
            List<QuestRewardType> rewards = new ArrayList<>();

            QuestRewardGrantEvent event = new QuestRewardGrantEvent(
                    null, QUEST_KEY, playerUUID, rewards, RewardGrantContext.INLINE);
            event.setCancelled(true);
            event.setCancelled(false);

            assertFalse(event.isCancelled());
        }
    }

    @Nested
    @DisplayName("Mutable reward list")
    class MutableRewardList {

        @Test
        @DisplayName("Given a mutable reward list, When a reward is added, Then getRewards reflects the addition")
        void getRewards_reflectsAddition() {
            UUID playerUUID = UUID.randomUUID();
            List<QuestRewardType> rewards = new ArrayList<>();

            QuestRewardGrantEvent event = new QuestRewardGrantEvent(
                    null, QUEST_KEY, playerUUID, rewards, RewardGrantContext.INLINE);

            MockQuestRewardType reward = QuestTestHelper.mockRewardType("added_reward");
            event.getRewards().add(reward);

            assertEquals(1, event.getRewards().size());
            assertSame(reward, event.getRewards().get(0));
        }

        @Test
        @DisplayName("Given a mutable reward list, When a reward is removed, Then getRewards reflects the removal")
        void getRewards_reflectsRemoval() {
            UUID playerUUID = UUID.randomUUID();
            MockQuestRewardType reward = QuestTestHelper.mockRewardType("removable");
            List<QuestRewardType> rewards = new ArrayList<>();
            rewards.add(reward);

            QuestRewardGrantEvent event = new QuestRewardGrantEvent(
                    null, QUEST_KEY, playerUUID, rewards, RewardGrantContext.INLINE);

            event.getRewards().remove(0);

            assertTrue(event.getRewards().isEmpty());
        }

        @Test
        @DisplayName("Given a mutable reward list, When cleared, Then getRewards returns empty list")
        void getRewards_returnsEmpty_afterClear() {
            UUID playerUUID = UUID.randomUUID();
            List<QuestRewardType> rewards = new ArrayList<>();
            rewards.add(QuestTestHelper.mockRewardType("r1"));
            rewards.add(QuestTestHelper.mockRewardType("r2"));

            QuestRewardGrantEvent event = new QuestRewardGrantEvent(
                    null, QUEST_KEY, playerUUID, rewards, RewardGrantContext.INLINE);

            event.getRewards().clear();

            assertTrue(event.getRewards().isEmpty());
        }
    }

    @Nested
    @DisplayName("Handler list")
    class HandlerListTests {

        @Test
        @DisplayName("getHandlers returns the static handler list")
        void getHandlers_matchesStaticHandlerList() {
            UUID playerUUID = UUID.randomUUID();
            List<QuestRewardType> rewards = new ArrayList<>();

            QuestRewardGrantEvent event = new QuestRewardGrantEvent(
                    null, QUEST_KEY, playerUUID, rewards, RewardGrantContext.INLINE);

            assertSame(QuestRewardGrantEvent.getHandlerList(), event.getHandlers());
        }
    }
}
