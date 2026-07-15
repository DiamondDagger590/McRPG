package us.eunoians.mcrpg.event.quest;

import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.QuestTestHelper;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.quest.source.builtin.ManualQuestSource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Locks in the quest-event dispatch contract: the abstract {@link QuestEvent} base owns the single
 * shared {@link HandlerList} and every concrete subclass inherits it, mirroring Bukkit's own
 * {@code EntityDamageEvent} / {@code EntityDamageByEntityEvent} idiom. This lets a listener on the
 * base type receive <b>every</b> quest event (including {@link QuestCancelEvent}, which a divergent
 * per-subclass list previously hid) while a subclass listener is still filtered to its own subtype.
 */
class QuestEventHandlerListTest extends McRPGBaseTest {

    private static final List<Class<?>> CONCRETE_QUEST_EVENTS = List.of(
            QuestStartEvent.class,
            QuestCompleteEvent.class,
            QuestExpireEvent.class,
            QuestCancelEvent.class,
            QuestObjectiveProgressEvent.class,
            QuestObjectiveCompleteEvent.class,
            QuestStageCompleteEvent.class,
            QuestPhaseCompleteEvent.class
    );

    /**
     * Reflectively resolves the (possibly inherited) static {@code getHandlerList()} on an event
     * class.
     *
     * @param eventClass the event class
     * @return the handler list the class resolves to
     */
    private HandlerList handlerListOf(@NotNull Class<?> eventClass) throws Exception {
        return (HandlerList) eventClass.getMethod("getHandlerList").invoke(null);
    }

    @Test
    @DisplayName("every concrete quest event resolves to the base's shared handler list")
    void concreteEvents_shareBaseHandlerList() throws Exception {
        HandlerList baseList = handlerListOf(QuestEvent.class);
        for (Class<?> eventClass : CONCRETE_QUEST_EVENTS) {
            assertSame(baseList, handlerListOf(eventClass),
                    eventClass.getSimpleName() + " must inherit the shared QuestEvent handler list, not declare its own");
        }
    }

    @Test
    @DisplayName("abstract QuestEvent base declares the shared static handler list")
    void abstractBase_declaresSharedHandlerList() {
        assertDoesNotThrow(() -> QuestEvent.class.getDeclaredMethod("getHandlerList"),
                "the base must own the shared handler list so subclasses can inherit it");
    }

    @Test
    @DisplayName("a listener on the QuestEvent base receives a QuestCancelEvent")
    void baseListener_receivesCancelEvent() {
        QuestDefinition definition = QuestTestHelper.singlePhaseQuest("hl_base_cancel");
        QuestInstance instance = QuestTestHelper.startedQuestInstance(definition);
        BaseListener listener = new BaseListener();
        server.getPluginManager().registerEvents(listener, McRPG.getInstance());
        try {
            server.getPluginManager().callEvent(new QuestCancelEvent(instance, definition, false));
            assertEquals(1, listener.received.size(), "base listener must receive the cancel event");
            assertInstanceOf(QuestCancelEvent.class, listener.received.get(0));
        } finally {
            HandlerList.unregisterAll(listener);
        }
    }

    @Test
    @DisplayName("a subtype listener is filtered to its own event and ignores siblings")
    void subtypeListener_isFilteredToOwnEvent() {
        QuestDefinition definition = QuestTestHelper.singlePhaseQuest("hl_filter");
        QuestInstance instance = QuestTestHelper.startedQuestInstance(definition);
        CancelListener listener = new CancelListener();
        server.getPluginManager().registerEvents(listener, McRPG.getInstance());
        try {
            server.getPluginManager().callEvent(
                    new QuestStartEvent(instance, definition, new ManualQuestSource()));
            assertTrue(listener.received.isEmpty(), "cancel listener must not fire for a sibling start event");

            server.getPluginManager().callEvent(new QuestCancelEvent(instance, definition, false));
            assertEquals(1, listener.received.size(), "cancel listener must fire for the cancel event");
        } finally {
            HandlerList.unregisterAll(listener);
        }
    }

    /** Captures every quest event seen through the abstract base type. */
    private static final class BaseListener implements Listener {
        private final List<QuestEvent> received = new ArrayList<>();

        @EventHandler
        public void onQuestEvent(@NotNull QuestEvent event) {
            received.add(event);
        }
    }

    /** Captures only quest cancel events. */
    private static final class CancelListener implements Listener {
        private final List<QuestCancelEvent> received = new ArrayList<>();

        @EventHandler
        public void onCancel(@NotNull QuestCancelEvent event) {
            received.add(event);
        }
    }
}
