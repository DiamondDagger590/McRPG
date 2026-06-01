package us.eunoians.mcrpg.quest.chain;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.quest.definition.OnStartMessage;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class CascadeContextTest {

    private static final NamespacedKey CHAIN_KEY = new NamespacedKey("mcrpg", "test_chain");
    private static final NamespacedKey QUEST_A = new NamespacedKey("mcrpg", "quest_a");
    private static final NamespacedKey QUEST_B = new NamespacedKey("mcrpg", "quest_b");

    @Test
    @DisplayName("Given a new context, when hasAutoCompletedSteps is called, then it returns false")
    public void hasAutoCompletedSteps_returnsFalse_whenNewContext() {
        CascadeContext context = new CascadeContext(CHAIN_KEY);

        assertFalse(context.hasAutoCompletedSteps());
    }

    @Test
    @DisplayName("Given a new context, when getAutoCompletedSteps is called, then it returns an empty list")
    public void getAutoCompletedSteps_returnsEmptyList_whenNewContext() {
        CascadeContext context = new CascadeContext(CHAIN_KEY);

        assertTrue(context.getAutoCompletedSteps().isEmpty());
    }

    @Test
    @DisplayName("Given a new context, when getChainKey is called, then it returns the chain key from construction")
    public void getChainKey_returnsConstructorValue() {
        CascadeContext context = new CascadeContext(CHAIN_KEY);

        assertEquals(CHAIN_KEY, context.getChainKey());
    }

    @Test
    @DisplayName("Given a new context, when getLastStartedQuestKey is called, then it returns empty")
    public void getLastStartedQuestKey_returnsEmpty_whenNewContext() {
        CascadeContext context = new CascadeContext(CHAIN_KEY);

        assertEquals(Optional.empty(), context.getLastStartedQuestKey());
    }

    @Test
    @DisplayName("Given a context, when recordAutoCompletedStep is called, then hasAutoCompletedSteps returns true")
    public void hasAutoCompletedSteps_returnsTrue_afterRecording() {
        CascadeContext context = new CascadeContext(CHAIN_KEY);

        context.recordAutoCompletedStep(QUEST_A, "Quest A");

        assertTrue(context.hasAutoCompletedSteps());
    }

    @Test
    @DisplayName("Given a context, when recordAutoCompletedStep is called twice, then steps are stored in order")
    public void recordAutoCompletedStep_preservesInsertionOrder() {
        CascadeContext context = new CascadeContext(CHAIN_KEY);

        context.recordAutoCompletedStep(QUEST_A, "Quest A");
        context.recordAutoCompletedStep(QUEST_B, "Quest B");

        List<CascadeContext.CascadeCompletedStep> steps = context.getAutoCompletedSteps();
        assertEquals(2, steps.size());
        assertEquals(QUEST_A, steps.get(0).questKey());
        assertEquals("Quest A", steps.get(0).displayName());
        assertEquals(QUEST_B, steps.get(1).questKey());
        assertEquals("Quest B", steps.get(1).displayName());
    }

    @Test
    @DisplayName("Given a context with deferred messages, when getDeferredMessagesFor is called with the same key, then it returns the stored messages")
    public void deferMessages_roundTrip() {
        CascadeContext context = new CascadeContext(CHAIN_KEY);
        List<OnStartMessage> messages = List.of(OnStartMessage.fromInline(List.of("<primary>Hello")));

        context.deferMessages(QUEST_A, messages);

        List<OnStartMessage> result = context.getDeferredMessagesFor(QUEST_A);
        assertEquals(1, result.size());
        assertEquals(messages.get(0).inlineMessages(), result.get(0).inlineMessages());
    }

    @Test
    @DisplayName("Given a context, when getDeferredMessagesFor is called with an unknown key, then it returns an empty list")
    public void getDeferredMessagesFor_returnsEmptyList_whenKeyUnknown() {
        CascadeContext context = new CascadeContext(CHAIN_KEY);

        List<OnStartMessage> result = context.getDeferredMessagesFor(QUEST_A);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Given a context, when deferMessages stores a list, then the returned list is a defensive copy")
    public void deferMessages_storesDefensiveCopy() {
        CascadeContext context = new CascadeContext(CHAIN_KEY);
        List<OnStartMessage> original = new java.util.ArrayList<>();
        original.add(OnStartMessage.fromInline(List.of("<primary>Hello")));

        context.deferMessages(QUEST_A, original);
        original.clear();

        assertEquals(1, context.getDeferredMessagesFor(QUEST_A).size());
    }

    @Test
    @DisplayName("Given a context, when setLastStartedQuestKey is called, then getLastStartedQuestKey returns it")
    public void setLastStartedQuestKey_roundTrip() {
        CascadeContext context = new CascadeContext(CHAIN_KEY);

        context.setLastStartedQuestKey(QUEST_A);

        assertEquals(Optional.of(QUEST_A), context.getLastStartedQuestKey());
    }

    @Test
    @DisplayName("Given a context with a last started key, when setLastStartedQuestKey is called again, then it overwrites")
    public void setLastStartedQuestKey_overwritesPrevious() {
        CascadeContext context = new CascadeContext(CHAIN_KEY);

        context.setLastStartedQuestKey(QUEST_A);
        context.setLastStartedQuestKey(QUEST_B);

        assertEquals(Optional.of(QUEST_B), context.getLastStartedQuestKey());
    }

    @Test
    @DisplayName("Given a context with auto-completed steps, when getAutoCompletedSteps is called, then the returned list is unmodifiable")
    public void getAutoCompletedSteps_returnsUnmodifiableList() {
        CascadeContext context = new CascadeContext(CHAIN_KEY);
        context.recordAutoCompletedStep(QUEST_A, "Quest A");

        List<CascadeContext.CascadeCompletedStep> steps = context.getAutoCompletedSteps();

        assertThrows(UnsupportedOperationException.class, () -> steps.add(
                new CascadeContext.CascadeCompletedStep(QUEST_B, "Quest B")));
    }
}
