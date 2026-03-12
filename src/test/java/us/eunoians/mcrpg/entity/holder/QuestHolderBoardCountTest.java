package us.eunoians.mcrpg.entity.holder;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class QuestHolderBoardCountTest {

    @DisplayName("Board count defaults to zero")
    @Test
    public void boardCount_defaultsToZero() {
        QuestHolder holder = new QuestHolder(UUID.randomUUID());
        assertEquals(0, holder.getActiveBoardQuestCount());
    }

    @DisplayName("setActiveBoardQuestCount seeds the initial value")
    @Test
    public void setActiveBoardQuestCount_seedsValue() {
        QuestHolder holder = new QuestHolder(UUID.randomUUID());
        holder.setActiveBoardQuestCount(3);
        assertEquals(3, holder.getActiveBoardQuestCount());
    }

    @DisplayName("incrementBoardQuestCount increases count by one")
    @Test
    public void incrementBoardQuestCount_incrementsByOne() {
        QuestHolder holder = new QuestHolder(UUID.randomUUID());
        holder.setActiveBoardQuestCount(2);
        holder.incrementBoardQuestCount();
        assertEquals(3, holder.getActiveBoardQuestCount());
    }

    @DisplayName("decrementBoardQuestCount decreases count by one")
    @Test
    public void decrementBoardQuestCount_decrementsByOne() {
        QuestHolder holder = new QuestHolder(UUID.randomUUID());
        holder.setActiveBoardQuestCount(2);
        holder.decrementBoardQuestCount();
        assertEquals(1, holder.getActiveBoardQuestCount());
    }

    @DisplayName("decrementBoardQuestCount does not go below zero")
    @Test
    public void decrementBoardQuestCount_doesNotGoBelowZero() {
        QuestHolder holder = new QuestHolder(UUID.randomUUID());
        holder.decrementBoardQuestCount();
        assertEquals(0, holder.getActiveBoardQuestCount());
    }

    @DisplayName("Multiple increments and decrements are tracked correctly")
    @Test
    public void incrementAndDecrement_trackedCorrectly() {
        QuestHolder holder = new QuestHolder(UUID.randomUUID());
        holder.incrementBoardQuestCount();
        holder.incrementBoardQuestCount();
        holder.incrementBoardQuestCount();
        assertEquals(3, holder.getActiveBoardQuestCount());

        holder.decrementBoardQuestCount();
        assertEquals(2, holder.getActiveBoardQuestCount());

        holder.decrementBoardQuestCount();
        holder.decrementBoardQuestCount();
        assertEquals(0, holder.getActiveBoardQuestCount());

        holder.decrementBoardQuestCount();
        assertEquals(0, holder.getActiveBoardQuestCount());
    }
}
