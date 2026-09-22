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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class QuestRewardGrantedEventTest {

    @Test
    @DisplayName("Getters return values passed to the constructor")
    void getters_returnConstructorValues() {
        QuestInstance questInstance = mock(QuestInstance.class);
        NamespacedKey questKey = new NamespacedKey("mcrpg", "test_quest");
        UUID playerUUID = UUID.randomUUID();
        QuestRewardType mockReward = mock(QuestRewardType.class);
        List<QuestRewardType> rewards = List.of(mockReward);
        RewardGrantContext context = RewardGrantContext.INLINE;

        QuestRewardGrantedEvent event = new QuestRewardGrantedEvent(
                questInstance, questKey, playerUUID, rewards, context);

        assertSame(questInstance, event.getQuestInstance());
        assertEquals(questKey, event.getQuestKey());
        assertEquals(playerUUID, event.getPlayerUUID());
        assertEquals(1, event.getGrantedRewards().size());
        assertSame(mockReward, event.getGrantedRewards().getFirst());
        assertEquals(context, event.getContext());
    }

    @Test
    @DisplayName("Null quest instance is returned when not tied to a live instance")
    void nullQuestInstance_returnsNull() {
        NamespacedKey questKey = new NamespacedKey("mcrpg", "test_quest");
        UUID playerUUID = UUID.randomUUID();
        List<QuestRewardType> rewards = new ArrayList<>();

        QuestRewardGrantedEvent event = new QuestRewardGrantedEvent(
                null, questKey, playerUUID, rewards, RewardGrantContext.PENDING);

        assertNull(event.getQuestInstance());
    }

    @Test
    @DisplayName("Granted rewards list is immutable (List.copyOf)")
    void grantedRewards_isImmutable() {
        NamespacedKey questKey = new NamespacedKey("mcrpg", "test_quest");
        UUID playerUUID = UUID.randomUUID();
        QuestRewardType mockReward = mock(QuestRewardType.class);
        List<QuestRewardType> rewards = new ArrayList<>(List.of(mockReward));

        QuestRewardGrantedEvent event = new QuestRewardGrantedEvent(
                null, questKey, playerUUID, rewards, RewardGrantContext.INLINE);

        assertThrows(UnsupportedOperationException.class,
                () -> event.getGrantedRewards().add(mock(QuestRewardType.class)));
    }

    @Test
    @DisplayName("Modifying the source list after construction does not affect the event")
    void grantedRewards_defensiveCopy() {
        NamespacedKey questKey = new NamespacedKey("mcrpg", "test_quest");
        UUID playerUUID = UUID.randomUUID();
        QuestRewardType mockReward = mock(QuestRewardType.class);
        List<QuestRewardType> rewards = new ArrayList<>(List.of(mockReward));

        QuestRewardGrantedEvent event = new QuestRewardGrantedEvent(
                null, questKey, playerUUID, rewards, RewardGrantContext.DISTRIBUTION);

        rewards.clear();

        assertEquals(1, event.getGrantedRewards().size());
        assertSame(mockReward, event.getGrantedRewards().getFirst());
    }

    @ParameterizedTest
    @EnumSource(RewardGrantContext.class)
    @DisplayName("Each RewardGrantContext is correctly stored and returned")
    void context_allValues_returnedCorrectly(RewardGrantContext context) {
        NamespacedKey questKey = new NamespacedKey("mcrpg", "test_quest");
        UUID playerUUID = UUID.randomUUID();
        List<QuestRewardType> rewards = new ArrayList<>();

        QuestRewardGrantedEvent event = new QuestRewardGrantedEvent(
                null, questKey, playerUUID, rewards, context);

        assertEquals(context, event.getContext());
    }

    @Test
    @DisplayName("getHandlers and getHandlerList return the same HandlerList")
    void handlers_returnStaticHandlerList() {
        NamespacedKey questKey = new NamespacedKey("mcrpg", "test_quest");
        UUID playerUUID = UUID.randomUUID();
        List<QuestRewardType> rewards = new ArrayList<>();

        QuestRewardGrantedEvent event = new QuestRewardGrantedEvent(
                null, questKey, playerUUID, rewards, RewardGrantContext.INLINE);

        assertSame(QuestRewardGrantedEvent.getHandlerList(), event.getHandlers());
    }
}
