package us.eunoians.mcrpg.event.quest;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.QuestTestHelper;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.quest.reward.QuestRewardType;
import us.eunoians.mcrpg.quest.reward.RewardGrantContext;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link QuestRewardGrantedEvent}.
 */
class QuestRewardGrantedEventTest extends McRPGBaseTest {

    private static final NamespacedKey QUEST_KEY = new NamespacedKey("mcrpg", "granted_quest");

    @Nested
    @DisplayName("Constructor and getters")
    class ConstructorAndGetters {

        @Test
        @DisplayName("Given all parameters, When getters are called, Then they return the values passed to the constructor")
        void getters_returnConstructorValues() {
            QuestDefinition definition = QuestTestHelper.singlePhaseQuest("rged_getters");
            QuestInstance instance = QuestTestHelper.startedQuestInstance(definition);
            UUID playerUUID = UUID.randomUUID();
            List<QuestRewardType> rewards = new ArrayList<>();
            rewards.add(QuestTestHelper.mockRewardType("granted_reward"));

            QuestRewardGrantedEvent event = new QuestRewardGrantedEvent(
                    instance, QUEST_KEY, playerUUID, rewards, RewardGrantContext.INLINE);

            assertSame(instance, event.getQuestInstance());
            assertEquals(QUEST_KEY, event.getQuestKey());
            assertEquals(playerUUID, event.getPlayerUUID());
            assertEquals(RewardGrantContext.INLINE, event.getContext());
        }

        @Test
        @DisplayName("Given null quest instance, When getQuestInstance is called, Then returns null")
        void getQuestInstance_returnsNull_whenConstructedWithNull() {
            UUID playerUUID = UUID.randomUUID();
            List<QuestRewardType> rewards = new ArrayList<>();

            QuestRewardGrantedEvent event = new QuestRewardGrantedEvent(
                    null, QUEST_KEY, playerUUID, rewards, RewardGrantContext.PENDING);

            assertNull(event.getQuestInstance());
        }

        @Test
        @DisplayName("Given PENDING context, When getContext is called, Then returns PENDING")
        void getContext_returnsPending() {
            UUID playerUUID = UUID.randomUUID();
            List<QuestRewardType> rewards = new ArrayList<>();

            QuestRewardGrantedEvent event = new QuestRewardGrantedEvent(
                    null, QUEST_KEY, playerUUID, rewards, RewardGrantContext.PENDING);

            assertEquals(RewardGrantContext.PENDING, event.getContext());
        }
    }

    @Nested
    @DisplayName("Immutability")
    class Immutability {

        @Test
        @DisplayName("Given a reward list, When getGrantedRewards is called, Then the list is immutable")
        void getGrantedRewards_returnsImmutableList() {
            UUID playerUUID = UUID.randomUUID();
            List<QuestRewardType> rewards = new ArrayList<>();
            rewards.add(QuestTestHelper.mockRewardType("immutable_reward"));

            QuestRewardGrantedEvent event = new QuestRewardGrantedEvent(
                    null, QUEST_KEY, playerUUID, rewards, RewardGrantContext.INLINE);

            assertThrows(UnsupportedOperationException.class,
                    () -> event.getGrantedRewards().add(QuestTestHelper.mockRewardType("extra")));
        }

        @Test
        @DisplayName("Given a reward list, When the original list is modified after construction, Then getGrantedRewards is unaffected")
        void getGrantedRewards_isDefensivelyCopied() {
            UUID playerUUID = UUID.randomUUID();
            List<QuestRewardType> rewards = new ArrayList<>();
            rewards.add(QuestTestHelper.mockRewardType("copied_reward"));

            QuestRewardGrantedEvent event = new QuestRewardGrantedEvent(
                    null, QUEST_KEY, playerUUID, rewards, RewardGrantContext.INLINE);

            rewards.clear();

            assertEquals(1, event.getGrantedRewards().size());
        }

        @Test
        @DisplayName("Given a reward list, When getGrantedRewards is called, Then it contains the original rewards")
        void getGrantedRewards_containsOriginalRewards() {
            UUID playerUUID = UUID.randomUUID();
            QuestRewardType reward1 = QuestTestHelper.mockRewardType("r1");
            QuestRewardType reward2 = QuestTestHelper.mockRewardType("r2");
            List<QuestRewardType> rewards = new ArrayList<>();
            rewards.add(reward1);
            rewards.add(reward2);

            QuestRewardGrantedEvent event = new QuestRewardGrantedEvent(
                    null, QUEST_KEY, playerUUID, rewards, RewardGrantContext.DISTRIBUTION);

            assertEquals(2, event.getGrantedRewards().size());
            assertSame(reward1, event.getGrantedRewards().get(0));
            assertSame(reward2, event.getGrantedRewards().get(1));
        }

        @Test
        @DisplayName("Given an empty reward list, When getGrantedRewards is called, Then returns empty immutable list")
        void getGrantedRewards_returnsEmptyImmutableList_whenNoRewards() {
            UUID playerUUID = UUID.randomUUID();
            List<QuestRewardType> rewards = new ArrayList<>();

            QuestRewardGrantedEvent event = new QuestRewardGrantedEvent(
                    null, QUEST_KEY, playerUUID, rewards, RewardGrantContext.INLINE);

            assertTrue(event.getGrantedRewards().isEmpty());
            assertThrows(UnsupportedOperationException.class,
                    () -> event.getGrantedRewards().add(QuestTestHelper.mockRewardType("sneaky")));
        }

        @Test
        @DisplayName("Given a reward list, When remove is called on getGrantedRewards, Then UnsupportedOperationException is thrown")
        void getGrantedRewards_throwsOnRemove() {
            UUID playerUUID = UUID.randomUUID();
            List<QuestRewardType> rewards = new ArrayList<>();
            rewards.add(QuestTestHelper.mockRewardType("no_remove"));

            QuestRewardGrantedEvent event = new QuestRewardGrantedEvent(
                    null, QUEST_KEY, playerUUID, rewards, RewardGrantContext.INLINE);

            assertThrows(UnsupportedOperationException.class,
                    () -> event.getGrantedRewards().remove(0));
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

            QuestRewardGrantedEvent event = new QuestRewardGrantedEvent(
                    null, QUEST_KEY, playerUUID, rewards, RewardGrantContext.INLINE);

            assertSame(QuestRewardGrantedEvent.getHandlerList(), event.getHandlers());
        }
    }
}
