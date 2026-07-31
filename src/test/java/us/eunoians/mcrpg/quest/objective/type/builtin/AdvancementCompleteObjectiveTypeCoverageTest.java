package us.eunoians.mcrpg.quest.objective.type.builtin;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.impl.objective.QuestObjectiveInstance;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("AdvancementCompleteObjectiveType Coverage")
class AdvancementCompleteObjectiveTypeCoverageTest extends McRPGBaseTest {

    private AdvancementCompleteObjectiveType type;

    @BeforeEach
    void setUp() {
        type = new AdvancementCompleteObjectiveType();
    }

    @Nested
    @DisplayName("checkInitialProgress")
    class CheckInitialProgress {

        @Test
        @DisplayName("returns 0 with specific filter when advancements are not registered on server")
        void checkInitialProgress_specificFilter_returnsZero_whenAdvancementsNotRegistered() {
            Section section = mock(Section.class);
            when(section.contains("advancements")).thenReturn(true);
            when(section.getStringList("advancements")).thenReturn(List.of(
                    "minecraft:story/iron_tools",
                    "minecraft:nether/find_fortress"
            ));
            AdvancementCompleteObjectiveType configured = type.parseConfig(section);

            PlayerMock player = server.addPlayer();
            long progress = configured.checkInitialProgress(player);

            assertEquals(0, progress);
        }

        @Test
        @DisplayName("returns 0 with empty filter when no displayed advancements exist")
        void checkInitialProgress_emptyFilter_returnsZero_whenNoDisplayedAdvancements() {
            PlayerMock player = server.addPlayer();
            long progress = type.checkInitialProgress(player);

            assertEquals(0, progress);
        }

        @Test
        @DisplayName("skips null NamespacedKey from invalid advancement string")
        void checkInitialProgress_specificFilter_skipsInvalidKey() {
            Section section = mock(Section.class);
            when(section.contains("advancements")).thenReturn(true);
            when(section.getStringList("advancements")).thenReturn(List.of(
                    ":::invalid:::key:::"
            ));
            AdvancementCompleteObjectiveType configured = type.parseConfig(section);

            PlayerMock player = server.addPlayer();
            long progress = configured.checkInitialProgress(player);

            assertEquals(0, progress);
        }

        @Test
        @DisplayName("handles mix of valid and invalid keys in filter")
        void checkInitialProgress_specificFilter_handlesMixedKeys() {
            Section section = mock(Section.class);
            when(section.contains("advancements")).thenReturn(true);
            when(section.getStringList("advancements")).thenReturn(List.of(
                    ":::bad:::",
                    "minecraft:story/iron_tools",
                    "also:::bad"
            ));
            AdvancementCompleteObjectiveType configured = type.parseConfig(section);

            PlayerMock player = server.addPlayer();
            long progress = configured.checkInitialProgress(player);

            assertEquals(0, progress);
        }
    }

    @Nested
    @DisplayName("processProgress multi-advancement filter")
    class ProcessProgressMultiFilter {

        @Test
        @DisplayName("returns 1 for first matching advancement in multi-filter")
        void processProgress_multiFilter_returnsOne_whenFirstMatches() {
            Section section = mock(Section.class);
            when(section.contains("advancements")).thenReturn(true);
            when(section.getStringList("advancements")).thenReturn(List.of(
                    "minecraft:story/iron_tools",
                    "minecraft:nether/find_fortress",
                    "minecraft:end/kill_dragon"
            ));
            AdvancementCompleteObjectiveType configured = type.parseConfig(section);

            Advancement advancement = mock(Advancement.class);
            when(advancement.getKey()).thenReturn(new NamespacedKey("minecraft", "nether/find_fortress"));
            PlayerAdvancementDoneEvent event = mock(PlayerAdvancementDoneEvent.class);
            when(event.getAdvancement()).thenReturn(advancement);

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            long progress = configured.processProgress(instance, new AdvancementCompleteQuestContext(event));

            assertEquals(1, progress);
        }

        @Test
        @DisplayName("returns 0 when advancement not in multi-filter")
        void processProgress_multiFilter_returnsZero_whenNotInFilter() {
            Section section = mock(Section.class);
            when(section.contains("advancements")).thenReturn(true);
            when(section.getStringList("advancements")).thenReturn(List.of(
                    "minecraft:story/iron_tools",
                    "minecraft:nether/find_fortress"
            ));
            AdvancementCompleteObjectiveType configured = type.parseConfig(section);

            Advancement advancement = mock(Advancement.class);
            when(advancement.getKey()).thenReturn(new NamespacedKey("minecraft", "end/kill_dragon"));
            PlayerAdvancementDoneEvent event = mock(PlayerAdvancementDoneEvent.class);
            when(event.getAdvancement()).thenReturn(advancement);

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            long progress = configured.processProgress(instance, new AdvancementCompleteQuestContext(event));

            assertEquals(0, progress);
        }
    }

    @Nested
    @DisplayName("parseConfig edge cases")
    class ParseConfigEdgeCases {

        @Test
        @DisplayName("handles empty advancement list")
        void parseConfig_emptyList_treatsAsNoFilter() {
            Section section = mock(Section.class);
            when(section.contains("advancements")).thenReturn(true);
            when(section.getStringList("advancements")).thenReturn(List.of());
            AdvancementCompleteObjectiveType configured = type.parseConfig(section);

            Advancement advancement = mock(Advancement.class);
            when(advancement.getKey()).thenReturn(new NamespacedKey("minecraft", "any_advancement"));
            PlayerAdvancementDoneEvent event = mock(PlayerAdvancementDoneEvent.class);
            when(event.getAdvancement()).thenReturn(advancement);

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(1, configured.processProgress(instance, new AdvancementCompleteQuestContext(event)));
        }

        @Test
        @DisplayName("single advancement in list creates a singleton filter")
        void parseConfig_singleAdvancement_createsSingletonFilter() {
            Section section = mock(Section.class);
            when(section.contains("advancements")).thenReturn(true);
            when(section.getStringList("advancements")).thenReturn(List.of("minecraft:story/iron_tools"));
            AdvancementCompleteObjectiveType configured = type.parseConfig(section);

            Advancement matchingAdv = mock(Advancement.class);
            when(matchingAdv.getKey()).thenReturn(new NamespacedKey("minecraft", "story/iron_tools"));
            PlayerAdvancementDoneEvent matchEvent = mock(PlayerAdvancementDoneEvent.class);
            when(matchEvent.getAdvancement()).thenReturn(matchingAdv);

            Advancement nonMatchingAdv = mock(Advancement.class);
            when(nonMatchingAdv.getKey()).thenReturn(new NamespacedKey("minecraft", "story/smelt_iron"));
            PlayerAdvancementDoneEvent noMatchEvent = mock(PlayerAdvancementDoneEvent.class);
            when(noMatchEvent.getAdvancement()).thenReturn(nonMatchingAdv);

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(1, configured.processProgress(instance, new AdvancementCompleteQuestContext(matchEvent)));
            assertEquals(0, configured.processProgress(instance, new AdvancementCompleteQuestContext(noMatchEvent)));
        }
    }

    @Nested
    @DisplayName("AdvancementCompleteQuestContext")
    class ContextTests {

        @Test
        @DisplayName("getAdvancementEvent returns the original event")
        void getAdvancementEvent_returnsOriginalEvent() {
            PlayerAdvancementDoneEvent event = mock(PlayerAdvancementDoneEvent.class);
            AdvancementCompleteQuestContext context = new AdvancementCompleteQuestContext(event);
            assertEquals(event, context.getAdvancementEvent());
        }

        @Test
        @DisplayName("context is not null after construction")
        void context_isNotNull() {
            PlayerAdvancementDoneEvent event = mock(PlayerAdvancementDoneEvent.class);
            AdvancementCompleteQuestContext context = new AdvancementCompleteQuestContext(event);
            assertNotNull(context);
        }
    }
}
