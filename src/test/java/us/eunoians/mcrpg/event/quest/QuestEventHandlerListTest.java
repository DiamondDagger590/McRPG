package us.eunoians.mcrpg.event.quest;

import org.bukkit.event.HandlerList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Locks in the fix from backlog #291 part B: every concrete quest event owns its own
 * {@link HandlerList} rather than sharing the abstract base's list. When events shared a list, a
 * listener on the abstract {@link QuestEvent} base silently never received {@link QuestCancelEvent}s.
 */
class QuestEventHandlerListTest {

    private static final List<Class<?>> CONCRETE_EVENTS = List.of(
            QuestStartEvent.class,
            QuestCompleteEvent.class,
            QuestExpireEvent.class,
            QuestCancelEvent.class,
            QuestObjectiveProgressEvent.class,
            QuestObjectiveCompleteEvent.class,
            QuestStageCompleteEvent.class,
            QuestPhaseCompleteEvent.class,
            QuestRewardGrantEvent.class,
            QuestRewardGrantedEvent.class
    );

    /**
     * Reflectively invokes {@code getHandlerList()} on a concrete event class.
     *
     * @param eventClass the event class
     * @return its static handler list
     */
    private HandlerList handlerListOf(Class<?> eventClass) throws Exception {
        return (HandlerList) eventClass.getMethod("getHandlerList").invoke(null);
    }

    @Test
    @DisplayName("every concrete quest event exposes a distinct handler list")
    void concreteEvents_haveDistinctHandlerLists() throws Exception {
        Map<HandlerList, Class<?>> seen = new IdentityHashMap<>();
        for (Class<?> eventClass : CONCRETE_EVENTS) {
            HandlerList handlerList = handlerListOf(eventClass);
            assertNotNull(handlerList, eventClass.getSimpleName() + " must expose a handler list");
            Class<?> collision = seen.put(handlerList, eventClass);
            assertEquals(null, collision, eventClass.getSimpleName() + " shares a handler list with "
                    + (collision != null ? collision.getSimpleName() : "none"));
        }
        assertEquals(CONCRETE_EVENTS.size(), seen.size(), "all handler lists must be distinct instances");
    }

    @Test
    @DisplayName("abstract QuestEvent base declares no static handler list")
    void abstractBase_declaresNoStaticHandlerList() {
        assertThrows(NoSuchMethodException.class, () -> QuestEvent.class.getDeclaredMethod("getHandlerList"),
                "listening on the abstract base is unsupported; it must not declare a shared handler list");
    }
}
