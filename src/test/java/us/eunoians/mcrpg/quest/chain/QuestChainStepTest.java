package us.eunoians.mcrpg.quest.chain;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class QuestChainStepTest extends McRPGBaseTest {

    @Test
    @DisplayName("Given a quest key, When simple() factory is called, Then step has correct questKey and defaults")
    public void simple_createsStepWithDefaults_whenGivenQuestKey() {
        var questKey = new NamespacedKey("mcrpg", "my_quest");
        var step = QuestChainStep.simple(questKey);

        assertEquals(questKey, step.questKey());
        assertTrue(step.conditions().isEmpty());
        assertEquals("fail-chain", step.onQuestExpire());
        assertEquals(-1, step.maxRetries());
    }

    @Test
    @DisplayName("Given two steps with same questKey, When compared, Then they are equal")
    public void questChainStep_isEqualToOtherWithSameData() {
        var questKey = new NamespacedKey("mcrpg", "quest_a");
        var step1 = new QuestChainStep(questKey, List.of(), "fail-chain", -1, null);
        var step2 = new QuestChainStep(questKey, List.of(), "fail-chain", -1, null);

        assertEquals(step1, step2);
    }
}
