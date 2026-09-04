package us.eunoians.mcrpg.quest.objective.type.builtin;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.quest.impl.objective.QuestObjectiveInstance;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveProgressContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("EnterBedObjectiveType")
public class EnterBedObjectiveTypeTest extends McRPGBaseTest {

    private EnterBedObjectiveType type;
    private final QuestObjectiveInstance mockInstance = mock(QuestObjectiveInstance.class);

    @BeforeEach
    public void setup() {
        type = new EnterBedObjectiveType();
    }

    @Nested
    @DisplayName("canProcess")
    class CanProcess {

        @Test
        @DisplayName("returns true for EnterBedQuestContext")
        public void canProcess_returnsTrue_forEnterBedContext() {
            PlayerBedEnterEvent mockEvent = mock(PlayerBedEnterEvent.class);
            EnterBedQuestContext context = new EnterBedQuestContext(mockEvent);
            assertTrue(type.canProcess(context));
        }

        @Test
        @DisplayName("returns false for generic context")
        public void canProcess_returnsFalse_forOtherContext() {
            QuestObjectiveProgressContext context = mock(QuestObjectiveProgressContext.class);
            assertFalse(type.canProcess(context));
        }
    }

    @Nested
    @DisplayName("identity")
    class Identity {

        @Test
        @DisplayName("key returns enter_bed")
        public void getKey_returnsEnterBedKey() {
            assertEquals(EnterBedObjectiveType.KEY, type.getKey());
        }

        @Test
        @DisplayName("expansion key is McRPGExpansion")
        public void getExpansionKey_returnsMcRPGExpansionKey() {
            assertTrue(type.getExpansionKey().isPresent());
            assertEquals(McRPGExpansion.EXPANSION_KEY, type.getExpansionKey().get());
        }
    }

    @Nested
    @DisplayName("parseConfig")
    class ParseConfig {

        @Test
        @DisplayName("returns a new instance")
        public void parseConfig_returnsNewInstance() {
            Section section = mock(Section.class);
            EnterBedObjectiveType parsed = type.parseConfig(section);
            assertNotSame(type, parsed);
        }

        @Test
        @DisplayName("parsed instance preserves key")
        public void parseConfig_preservesKey() {
            Section section = mock(Section.class);
            EnterBedObjectiveType parsed = type.parseConfig(section);
            assertEquals(EnterBedObjectiveType.KEY, parsed.getKey());
        }
    }

    @Nested
    @DisplayName("processProgress")
    class ProcessProgress {

        @Test
        @DisplayName("wrong context type returns 0")
        public void processProgress_returnsZero_forWrongContextType() {
            QuestObjectiveProgressContext wrongContext = mock(QuestObjectiveProgressContext.class);
            assertEquals(0, type.processProgress(mockInstance, wrongContext));
        }

        @Test
        @DisplayName("BedEnterResult.OK returns 1")
        public void processProgress_returnsOne_whenResultIsOk() {
            PlayerBedEnterEvent event = mock(PlayerBedEnterEvent.class);
            when(event.getBedEnterResult()).thenReturn(PlayerBedEnterEvent.BedEnterResult.OK);
            EnterBedQuestContext context = new EnterBedQuestContext(event);
            assertEquals(1, type.processProgress(mockInstance, context));
        }

        @Test
        @DisplayName("BedEnterResult.NOT_POSSIBLE_HERE returns 0")
        public void processProgress_returnsZero_whenResultIsNotPossibleHere() {
            PlayerBedEnterEvent event = mock(PlayerBedEnterEvent.class);
            when(event.getBedEnterResult()).thenReturn(PlayerBedEnterEvent.BedEnterResult.NOT_POSSIBLE_HERE);
            EnterBedQuestContext context = new EnterBedQuestContext(event);
            assertEquals(0, type.processProgress(mockInstance, context));
        }

        @Test
        @DisplayName("BedEnterResult.NOT_POSSIBLE_NOW returns 0")
        public void processProgress_returnsZero_whenResultIsNotPossibleNow() {
            PlayerBedEnterEvent event = mock(PlayerBedEnterEvent.class);
            when(event.getBedEnterResult()).thenReturn(PlayerBedEnterEvent.BedEnterResult.NOT_POSSIBLE_NOW);
            EnterBedQuestContext context = new EnterBedQuestContext(event);
            assertEquals(0, type.processProgress(mockInstance, context));
        }

        @Test
        @DisplayName("BedEnterResult.TOO_FAR_AWAY returns 0")
        public void processProgress_returnsZero_whenResultIsTooFarAway() {
            PlayerBedEnterEvent event = mock(PlayerBedEnterEvent.class);
            when(event.getBedEnterResult()).thenReturn(PlayerBedEnterEvent.BedEnterResult.TOO_FAR_AWAY);
            EnterBedQuestContext context = new EnterBedQuestContext(event);
            assertEquals(0, type.processProgress(mockInstance, context));
        }

        @Test
        @DisplayName("BedEnterResult.NOT_SAFE returns 0")
        public void processProgress_returnsZero_whenResultIsNotSafe() {
            PlayerBedEnterEvent event = mock(PlayerBedEnterEvent.class);
            when(event.getBedEnterResult()).thenReturn(PlayerBedEnterEvent.BedEnterResult.NOT_SAFE);
            EnterBedQuestContext context = new EnterBedQuestContext(event);
            assertEquals(0, type.processProgress(mockInstance, context));
        }
    }
}
