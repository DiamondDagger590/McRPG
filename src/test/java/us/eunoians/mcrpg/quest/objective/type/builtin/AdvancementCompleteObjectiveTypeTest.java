package us.eunoians.mcrpg.quest.objective.type.builtin;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.quest.impl.objective.QuestObjectiveInstance;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveProgressContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("AdvancementCompleteObjectiveType")
class AdvancementCompleteObjectiveTypeTest extends McRPGBaseTest {

    private AdvancementCompleteObjectiveType type;

    @BeforeEach
    void setUp() {
        type = new AdvancementCompleteObjectiveType();
    }

    @Nested
    @DisplayName("getKey")
    class GetKey {

        @Test
        @DisplayName("returns advancement_complete key")
        void getKey_returnsAdvancementCompleteKey() {
            assertEquals(AdvancementCompleteObjectiveType.KEY, type.getKey());
        }
    }

    @Nested
    @DisplayName("getExpansionKey")
    class GetExpansionKey {

        @Test
        @DisplayName("returns McRPGExpansion key")
        void getExpansionKey_returnsMcRPGExpansionKey() {
            assertTrue(type.getExpansionKey().isPresent());
            assertEquals(McRPGExpansion.EXPANSION_KEY, type.getExpansionKey().get());
        }
    }

    @Nested
    @DisplayName("canProcess")
    class CanProcess {

        @Test
        @DisplayName("returns true for AdvancementCompleteQuestContext")
        void canProcess_returnsTrue_forAdvancementContext() {
            PlayerAdvancementDoneEvent mockEvent = mock(PlayerAdvancementDoneEvent.class);
            AdvancementCompleteQuestContext context = new AdvancementCompleteQuestContext(mockEvent);
            assertTrue(type.canProcess(context));
        }

        @Test
        @DisplayName("returns false for other context types")
        void canProcess_returnsFalse_forOtherContext() {
            QuestObjectiveProgressContext context = mock(QuestObjectiveProgressContext.class);
            assertFalse(type.canProcess(context));
        }
    }

    @Nested
    @DisplayName("processProgress")
    class ProcessProgress {

        @Test
        @DisplayName("returns 0 for wrong context type")
        void processProgress_returnsZero_whenWrongContextType() {
            QuestObjectiveProgressContext wrongContext = mock(QuestObjectiveProgressContext.class);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            long progress = type.processProgress(instance, wrongContext);

            assertEquals(0, progress);
        }

        @Test
        @DisplayName("returns 1 for any advancement when no filter configured")
        void processProgress_returnsOne_whenNoFilterConfigured() {
            Advancement advancement = mock(Advancement.class);
            NamespacedKey advKey = new NamespacedKey("minecraft", "story/iron_tools");
            when(advancement.getKey()).thenReturn(advKey);

            PlayerAdvancementDoneEvent event = mock(PlayerAdvancementDoneEvent.class);
            when(event.getAdvancement()).thenReturn(advancement);
            AdvancementCompleteQuestContext context = new AdvancementCompleteQuestContext(event);

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            long progress = type.processProgress(instance, context);

            assertEquals(1, progress);
        }

        @Test
        @DisplayName("returns 1 when advancement matches filter")
        void processProgress_returnsOne_whenAdvancementMatchesFilter() {
            Section section = mock(Section.class);
            when(section.contains("advancements")).thenReturn(true);
            when(section.getStringList("advancements")).thenReturn(List.of("minecraft:story/iron_tools"));
            AdvancementCompleteObjectiveType configured = type.parseConfig(section);

            Advancement advancement = mock(Advancement.class);
            NamespacedKey advKey = new NamespacedKey("minecraft", "story/iron_tools");
            when(advancement.getKey()).thenReturn(advKey);

            PlayerAdvancementDoneEvent event = mock(PlayerAdvancementDoneEvent.class);
            when(event.getAdvancement()).thenReturn(advancement);
            AdvancementCompleteQuestContext context = new AdvancementCompleteQuestContext(event);

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            long progress = configured.processProgress(instance, context);

            assertEquals(1, progress);
        }

        @Test
        @DisplayName("returns 0 when advancement does not match filter")
        void processProgress_returnsZero_whenAdvancementDoesNotMatchFilter() {
            Section section = mock(Section.class);
            when(section.contains("advancements")).thenReturn(true);
            when(section.getStringList("advancements")).thenReturn(List.of("minecraft:story/iron_tools"));
            AdvancementCompleteObjectiveType configured = type.parseConfig(section);

            Advancement advancement = mock(Advancement.class);
            NamespacedKey advKey = new NamespacedKey("minecraft", "nether/find_fortress");
            when(advancement.getKey()).thenReturn(advKey);

            PlayerAdvancementDoneEvent event = mock(PlayerAdvancementDoneEvent.class);
            when(event.getAdvancement()).thenReturn(advancement);
            AdvancementCompleteQuestContext context = new AdvancementCompleteQuestContext(event);

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            long progress = configured.processProgress(instance, context);

            assertEquals(0, progress);
        }
    }

    @Nested
    @DisplayName("parseConfig")
    class ParseConfig {

        @Test
        @DisplayName("returns instance with empty set when no advancements key")
        void parseConfig_returnsEmptySet_whenNoAdvancementsKey() {
            Section section = mock(Section.class);
            when(section.contains("advancements")).thenReturn(false);

            AdvancementCompleteObjectiveType configured = type.parseConfig(section);

            Advancement advancement = mock(Advancement.class);
            NamespacedKey advKey = new NamespacedKey("minecraft", "story/iron_tools");
            when(advancement.getKey()).thenReturn(advKey);

            PlayerAdvancementDoneEvent event = mock(PlayerAdvancementDoneEvent.class);
            when(event.getAdvancement()).thenReturn(advancement);
            AdvancementCompleteQuestContext context = new AdvancementCompleteQuestContext(event);

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(1, configured.processProgress(instance, context));
        }

        @Test
        @DisplayName("returns instance with parsed advancement set")
        void parseConfig_returnsParsedSet() {
            Section section = mock(Section.class);
            when(section.contains("advancements")).thenReturn(true);
            when(section.getStringList("advancements")).thenReturn(List.of(
                    "minecraft:story/iron_tools",
                    "minecraft:nether/find_fortress"
            ));

            AdvancementCompleteObjectiveType configured = type.parseConfig(section);

            Advancement matchingAdvancement = mock(Advancement.class);
            when(matchingAdvancement.getKey()).thenReturn(new NamespacedKey("minecraft", "story/iron_tools"));
            PlayerAdvancementDoneEvent matchEvent = mock(PlayerAdvancementDoneEvent.class);
            when(matchEvent.getAdvancement()).thenReturn(matchingAdvancement);

            Advancement nonMatchingAdvancement = mock(Advancement.class);
            when(nonMatchingAdvancement.getKey()).thenReturn(new NamespacedKey("minecraft", "story/smelt_iron"));
            PlayerAdvancementDoneEvent noMatchEvent = mock(PlayerAdvancementDoneEvent.class);
            when(noMatchEvent.getAdvancement()).thenReturn(nonMatchingAdvancement);

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            assertEquals(1, configured.processProgress(instance, new AdvancementCompleteQuestContext(matchEvent)));
            assertEquals(0, configured.processProgress(instance, new AdvancementCompleteQuestContext(noMatchEvent)));
        }
    }
}
