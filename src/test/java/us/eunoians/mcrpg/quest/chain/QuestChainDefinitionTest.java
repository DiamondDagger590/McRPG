package us.eunoians.mcrpg.quest.chain;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class QuestChainDefinitionTest extends McRPGBaseTest {

    private NamespacedKey chainKey;
    private NamespacedKey sourceKey;
    private NamespacedKey triggerKey;
    private NamespacedKey questKeyA;
    private NamespacedKey questKeyB;
    private NamespacedKey questKeyC;

    @BeforeEach
    public void setUp() {
        chainKey = new NamespacedKey("mcrpg", "test_chain");
        sourceKey = new NamespacedKey("mcrpg", "manual");
        triggerKey = new NamespacedKey("mcrpg", "first_join");
        questKeyA = new NamespacedKey("mcrpg", "quest_a");
        questKeyB = new NamespacedKey("mcrpg", "quest_b");
        questKeyC = new NamespacedKey("mcrpg", "quest_c");
    }

    private QuestChainDefinition buildThreeStepChain() {
        return new QuestChainDefinition.Builder(chainKey, sourceKey, triggerKey,
                List.of(QuestChainStep.simple(questKeyA), QuestChainStep.simple(questKeyB), QuestChainStep.simple(questKeyC)))
                .build();
    }

    @DisplayName("Given valid steps, When build is called, Then chain definition is created with step index")
    @Test
    public void build_createsDefinition_whenStepsAreValid() {
        var def = buildThreeStepChain();
        assertEquals(3, def.getSteps().size());
    }

    @DisplayName("Given configured display name, When getDisplayName is called, Then it returns the configured name")
    @Test
    public void getDisplayName_returnsConfiguredName_whenSet() {
        var def = new QuestChainDefinition.Builder(chainKey, sourceKey, triggerKey, List.of(QuestChainStep.simple(questKeyA)))
                .displayName("My Display Name")
                .build();
        assertEquals("My Display Name", def.getDisplayName());
    }

    @DisplayName("Given no display name, When getDisplayName is called, Then it falls back to chain key value portion")
    @Test
    public void getDisplayName_fallsBackToKeyValue_whenNotSet() {
        var def = new QuestChainDefinition.Builder(chainKey, sourceKey, triggerKey, List.of(QuestChainStep.simple(questKeyA)))
                .build();
        assertEquals("test_chain", def.getDisplayName());
    }

    @DisplayName("Given a quest key in the steps, When getStep is called, Then it returns the correct step")
    @Test
    public void getStep_returnsStep_whenKeyExists() {
        var def = buildThreeStepChain();
        var result = def.getStep(questKeyB);
        assertTrue(result.isPresent());
        assertEquals(questKeyB, result.get().questKey());
    }

    @DisplayName("Given a quest key not in the steps, When getStep is called, Then it returns empty")
    @Test
    public void getStep_returnsEmpty_whenKeyNotFound() {
        var def = buildThreeStepChain();
        assertTrue(def.getStep(new NamespacedKey("mcrpg", "unknown_quest")).isEmpty());
    }

    @DisplayName("Given a quest key in the steps, When getStepIndex is called, Then it returns the correct index")
    @Test
    public void getStepIndex_returnsCorrectIndex_whenKeyExists() {
        var def = buildThreeStepChain();
        assertEquals(0, def.getStepIndex(questKeyA));
        assertEquals(1, def.getStepIndex(questKeyB));
        assertEquals(2, def.getStepIndex(questKeyC));
    }

    @DisplayName("Given a quest key not in the steps, When getStepIndex is called, Then it returns -1")
    @Test
    public void getStepIndex_returnsNegativeOne_whenKeyNotFound() {
        var def = buildThreeStepChain();
        assertEquals(-1, def.getStepIndex(new NamespacedKey("mcrpg", "unknown")));
    }

    @DisplayName("Given a quest key for a non-last step, When getNextStep is called, Then it returns the next step")
    @Test
    public void getNextStep_returnsNextStep_whenNotLastStep() {
        var def = buildThreeStepChain();
        var next = def.getNextStep(questKeyA);
        assertTrue(next.isPresent());
        assertEquals(questKeyB, next.get().questKey());
    }

    @DisplayName("Given a quest key for the last step, When getNextStep is called, Then it returns empty")
    @Test
    public void getNextStep_returnsEmpty_whenLastStep() {
        var def = buildThreeStepChain();
        assertTrue(def.getNextStep(questKeyC).isEmpty());
    }

    @DisplayName("Given an empty steps list, When build is called, Then it throws IllegalArgumentException")
    @Test
    public void build_throwsIllegalArgumentException_whenStepsEmpty() {
        assertThrows(IllegalArgumentException.class, () ->
                new QuestChainDefinition.Builder(chainKey, sourceKey, triggerKey, List.of()).build());
    }

    @DisplayName("Given duplicate quest keys in steps, When build is called, Then it throws IllegalStateException")
    @Test
    public void build_throwsIllegalStateException_whenDuplicateQuestKeys() {
        assertThrows(IllegalStateException.class, () ->
                new QuestChainDefinition.Builder(chainKey, sourceKey, triggerKey,
                        List.of(QuestChainStep.simple(questKeyA), QuestChainStep.simple(questKeyA))).build());
    }
}
