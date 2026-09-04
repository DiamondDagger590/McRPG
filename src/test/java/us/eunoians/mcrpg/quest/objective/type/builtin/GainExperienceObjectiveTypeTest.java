package us.eunoians.mcrpg.quest.objective.type.builtin;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.event.player.PlayerExpChangeEvent;
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

@DisplayName("GainExperienceObjectiveType")
public class GainExperienceObjectiveTypeTest extends McRPGBaseTest {

    private GainExperienceObjectiveType type;
    private final QuestObjectiveInstance mockInstance = mock(QuestObjectiveInstance.class);

    @BeforeEach
    public void setup() {
        type = new GainExperienceObjectiveType();
    }

    @Nested
    @DisplayName("canProcess")
    class CanProcess {

        @Test
        @DisplayName("returns true for GainExperienceQuestContext")
        public void canProcess_returnsTrue_forGainExperienceContext() {
            PlayerExpChangeEvent mockEvent = mock(PlayerExpChangeEvent.class);
            GainExperienceQuestContext context = new GainExperienceQuestContext(mockEvent);
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
        @DisplayName("key returns gain_experience")
        public void getKey_returnsGainExperienceKey() {
            assertEquals(GainExperienceObjectiveType.KEY, type.getKey());
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
            GainExperienceObjectiveType parsed = type.parseConfig(section);
            assertNotSame(type, parsed);
        }

        @Test
        @DisplayName("parsed instance preserves key")
        public void parseConfig_preservesKey() {
            Section section = mock(Section.class);
            GainExperienceObjectiveType parsed = type.parseConfig(section);
            assertEquals(GainExperienceObjectiveType.KEY, parsed.getKey());
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
        @DisplayName("returns the experience amount from the event")
        public void processProgress_returnsEventAmount() {
            PlayerExpChangeEvent event = mock(PlayerExpChangeEvent.class);
            when(event.getAmount()).thenReturn(15);
            GainExperienceQuestContext context = new GainExperienceQuestContext(event);
            assertEquals(15, type.processProgress(mockInstance, context));
        }

        @Test
        @DisplayName("returns 0 for zero experience gain")
        public void processProgress_returnsZero_forZeroExperience() {
            PlayerExpChangeEvent event = mock(PlayerExpChangeEvent.class);
            when(event.getAmount()).thenReturn(0);
            GainExperienceQuestContext context = new GainExperienceQuestContext(event);
            assertEquals(0, type.processProgress(mockInstance, context));
        }

        @Test
        @DisplayName("returns 1 for single experience point")
        public void processProgress_returnsOne_forSingleExperience() {
            PlayerExpChangeEvent event = mock(PlayerExpChangeEvent.class);
            when(event.getAmount()).thenReturn(1);
            GainExperienceQuestContext context = new GainExperienceQuestContext(event);
            assertEquals(1, type.processProgress(mockInstance, context));
        }

        @Test
        @DisplayName("returns large amount for large experience gain")
        public void processProgress_returnsLargeAmount() {
            PlayerExpChangeEvent event = mock(PlayerExpChangeEvent.class);
            when(event.getAmount()).thenReturn(1000);
            GainExperienceQuestContext context = new GainExperienceQuestContext(event);
            assertEquals(1000, type.processProgress(mockInstance, context));
        }
    }
}
