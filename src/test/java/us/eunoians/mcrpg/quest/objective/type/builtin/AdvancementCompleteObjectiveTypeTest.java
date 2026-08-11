package us.eunoians.mcrpg.quest.objective.type.builtin;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.entity.player.McRPGPlayerExtension;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.quest.impl.objective.QuestObjectiveInstance;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveProgressContext;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("AdvancementCompleteObjectiveType")
@ExtendWith(McRPGPlayerExtension.class)
class AdvancementCompleteObjectiveTypeTest extends McRPGBaseTest {

    private AdvancementCompleteObjectiveType type;
    private McRPGLocalizationManager localizationManager;

    @BeforeEach
    void setUp() {
        type = new AdvancementCompleteObjectiveType();
        localizationManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION);
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

    @Nested
    @DisplayName("describeObjective")
    class DescribeObjective {

        @Test
        @DisplayName("uses ANY locale key when no filter configured")
        void describeObjective_usesAnyKey_whenNoFilter(McRPGPlayer player) {
            when(localizationManager.getLocalizedMessage(eq(player),
                    eq(LocalizationKey.QUEST_OBJECTIVE_ADVANCEMENT_COMPLETE_ANY), anyMap()))
                    .thenReturn("Complete 5 advancements");

            String description = type.describeObjective(player, 5);
            assertEquals("Complete 5 advancements", description);
            verify(localizationManager).getLocalizedMessage(eq(player),
                    eq(LocalizationKey.QUEST_OBJECTIVE_ADVANCEMENT_COMPLETE_ANY),
                    eq(Map.of("count", "5")));
        }

        @Test
        @DisplayName("uses SINGLE locale key when exactly one advancement configured")
        void describeObjective_usesSingleKey_whenOneAdvancement(McRPGPlayer player) {
            Section section = mock(Section.class);
            when(section.contains("advancements")).thenReturn(true);
            when(section.getStringList("advancements")).thenReturn(List.of("minecraft:story/iron_tools"));
            AdvancementCompleteObjectiveType configured = type.parseConfig(section);

            when(localizationManager.getLocalizedMessage(eq(player),
                    eq(LocalizationKey.QUEST_OBJECTIVE_ADVANCEMENT_COMPLETE_SINGLE), anyMap()))
                    .thenReturn("Complete the story/iron tools advancement");

            String description = configured.describeObjective(player, 1);
            assertEquals("Complete the story/iron tools advancement", description);
            verify(localizationManager).getLocalizedMessage(eq(player),
                    eq(LocalizationKey.QUEST_OBJECTIVE_ADVANCEMENT_COMPLETE_SINGLE),
                    eq(Map.of("count", "1", "advancement", "story/iron tools")));
        }

        @Test
        @DisplayName("uses MULTI locale keys when multiple advancements configured")
        void describeObjective_usesMultiKeys_whenMultipleAdvancements(McRPGPlayer player) {
            Section section = mock(Section.class);
            when(section.contains("advancements")).thenReturn(true);
            when(section.getStringList("advancements")).thenReturn(List.of(
                    "minecraft:story/iron_tools",
                    "minecraft:nether/find_fortress"
            ));
            AdvancementCompleteObjectiveType configured = type.parseConfig(section);

            when(localizationManager.getLocalizedMessage(eq(player),
                    eq(LocalizationKey.QUEST_OBJECTIVE_ADVANCEMENT_COMPLETE_MULTI_HEADER), anyMap()))
                    .thenReturn("Complete 2 advancements");
            when(localizationManager.getLocalizedMessage(eq(player),
                    eq(LocalizationKey.QUEST_OBJECTIVE_ADVANCEMENT_COMPLETE_MULTI_ITEM), anyMap()))
                    .thenReturn("  - advancement");

            String description = configured.describeObjective(player, 2);
            assertNotNull(description);
            assertTrue(description.startsWith("Complete 2 advancements"));
            assertTrue(description.contains("\n"));
        }

        @Test
        @DisplayName("extractDisplayName strips namespace and replaces underscores with spaces")
        void describeObjective_formatsAdvancementName(McRPGPlayer player) {
            Section section = mock(Section.class);
            when(section.contains("advancements")).thenReturn(true);
            when(section.getStringList("advancements")).thenReturn(List.of("minecraft:story/iron_tools"));
            AdvancementCompleteObjectiveType configured = type.parseConfig(section);

            when(localizationManager.getLocalizedMessage(eq(player),
                    eq(LocalizationKey.QUEST_OBJECTIVE_ADVANCEMENT_COMPLETE_SINGLE), anyMap()))
                    .thenAnswer(inv -> {
                        @SuppressWarnings("unchecked")
                        Map<String, String> placeholders = inv.getArgument(2, Map.class);
                        return placeholders.get("advancement");
                    });

            String description = configured.describeObjective(player, 1);
            assertEquals("story/iron tools", description);
        }

        @Test
        @DisplayName("extractDisplayName handles key without namespace colon")
        void describeObjective_handlesKeyWithoutColon(McRPGPlayer player) {
            Section section = mock(Section.class);
            when(section.contains("advancements")).thenReturn(true);
            when(section.getStringList("advancements")).thenReturn(List.of("story/iron_tools"));
            AdvancementCompleteObjectiveType configured = type.parseConfig(section);

            when(localizationManager.getLocalizedMessage(eq(player),
                    eq(LocalizationKey.QUEST_OBJECTIVE_ADVANCEMENT_COMPLETE_SINGLE), anyMap()))
                    .thenAnswer(inv -> {
                        @SuppressWarnings("unchecked")
                        Map<String, String> placeholders = inv.getArgument(2, Map.class);
                        return placeholders.get("advancement");
                    });

            String description = configured.describeObjective(player, 1);
            assertEquals("story/iron tools", description);
        }
    }
}
