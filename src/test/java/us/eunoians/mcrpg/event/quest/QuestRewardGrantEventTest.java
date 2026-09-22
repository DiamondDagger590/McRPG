package us.eunoians.mcrpg.event.quest;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
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
import static org.mockito.Mockito.mock;

class QuestRewardGrantEventTest {

    @Test
    @DisplayName("Getters return values passed to the constructor")
    void getters_returnConstructorValues() {
        QuestInstance questInstance = mock(QuestInstance.class);
        NamespacedKey questKey = new NamespacedKey("mcrpg", "test_quest");
        UUID playerUUID = UUID.randomUUID();
        List<QuestRewardType> rewards = new ArrayList<>();
        rewards.add(mock(QuestRewardType.class));
        RewardGrantContext context = RewardGrantContext.INLINE;

        QuestRewardGrantEvent event = new QuestRewardGrantEvent(
                questInstance, questKey, playerUUID, rewards, context);

        assertSame(questInstance, event.getQuestInstance());
        assertEquals(questKey, event.getQuestKey());
        assertEquals(playerUUID, event.getPlayerUUID());
        assertSame(rewards, event.getRewards());
        assertEquals(context, event.getContext());
    }

    @Test
    @DisplayName("Null quest instance is returned when not tied to a live instance")
    void nullQuestInstance_returnsNull() {
        NamespacedKey questKey = new NamespacedKey("mcrpg", "test_quest");
        UUID playerUUID = UUID.randomUUID();
        List<QuestRewardType> rewards = new ArrayList<>();

        QuestRewardGrantEvent event = new QuestRewardGrantEvent(
                null, questKey, playerUUID, rewards, RewardGrantContext.PENDING);

        assertNull(event.getQuestInstance());
    }

    @Test
    @DisplayName("Event defaults to not cancelled")
    void event_defaultsNotCancelled() {
        NamespacedKey questKey = new NamespacedKey("mcrpg", "test_quest");
        UUID playerUUID = UUID.randomUUID();
        List<QuestRewardType> rewards = new ArrayList<>();

        QuestRewardGrantEvent event = new QuestRewardGrantEvent(
                null, questKey, playerUUID, rewards, RewardGrantContext.INLINE);

        assertFalse(event.isCancelled());
    }

    @Test
    @DisplayName("setCancelled toggles cancellation state")
    void setCancelled_togglesState() {
        NamespacedKey questKey = new NamespacedKey("mcrpg", "test_quest");
        UUID playerUUID = UUID.randomUUID();
        List<QuestRewardType> rewards = new ArrayList<>();

        QuestRewardGrantEvent event = new QuestRewardGrantEvent(
                null, questKey, playerUUID, rewards, RewardGrantContext.INLINE);

        event.setCancelled(true);
        assertTrue(event.isCancelled());

        event.setCancelled(false);
        assertFalse(event.isCancelled());
    }

    @Test
    @DisplayName("Reward list is mutable — listeners can add entries")
    void rewardList_isMutable() {
        NamespacedKey questKey = new NamespacedKey("mcrpg", "test_quest");
        UUID playerUUID = UUID.randomUUID();
        List<QuestRewardType> rewards = new ArrayList<>();

        QuestRewardGrantEvent event = new QuestRewardGrantEvent(
                null, questKey, playerUUID, rewards, RewardGrantContext.INLINE);

        QuestRewardType mockReward = mock(QuestRewardType.class);
        event.getRewards().add(mockReward);

        assertEquals(1, event.getRewards().size());
        assertSame(mockReward, event.getRewards().getFirst());
    }

    @Test
    @DisplayName("Reward list is mutable — listeners can remove entries")
    void rewardList_canRemoveEntries() {
        NamespacedKey questKey = new NamespacedKey("mcrpg", "test_quest");
        UUID playerUUID = UUID.randomUUID();
        QuestRewardType mockReward = mock(QuestRewardType.class);
        List<QuestRewardType> rewards = new ArrayList<>(List.of(mockReward));

        QuestRewardGrantEvent event = new QuestRewardGrantEvent(
                null, questKey, playerUUID, rewards, RewardGrantContext.DISTRIBUTION);

        event.getRewards().clear();

        assertTrue(event.getRewards().isEmpty());
    }

    @ParameterizedTest
    @EnumSource(RewardGrantContext.class)
    @DisplayName("Each RewardGrantContext is correctly stored and returned")
    void context_allValues_returnedCorrectly(RewardGrantContext context) {
        NamespacedKey questKey = new NamespacedKey("mcrpg", "test_quest");
        UUID playerUUID = UUID.randomUUID();
        List<QuestRewardType> rewards = new ArrayList<>();

        QuestRewardGrantEvent event = new QuestRewardGrantEvent(
                null, questKey, playerUUID, rewards, context);

        assertEquals(context, event.getContext());
    }

    @Test
    @DisplayName("getHandlers and getHandlerList return the same HandlerList")
    void handlers_returnStaticHandlerList() {
        NamespacedKey questKey = new NamespacedKey("mcrpg", "test_quest");
        UUID playerUUID = UUID.randomUUID();
        List<QuestRewardType> rewards = new ArrayList<>();

        QuestRewardGrantEvent event = new QuestRewardGrantEvent(
                null, questKey, playerUUID, rewards, RewardGrantContext.INLINE);

        assertSame(QuestRewardGrantEvent.getHandlerList(), event.getHandlers());
    }
}
