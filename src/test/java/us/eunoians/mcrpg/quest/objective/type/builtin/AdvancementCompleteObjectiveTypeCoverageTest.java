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
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("AdvancementCompleteObjectiveType — extended coverage")
public class AdvancementCompleteObjectiveTypeCoverageTest extends McRPGBaseTest {

    private AdvancementCompleteObjectiveType baseType;

    @BeforeEach
    public void setup() {
        baseType = new AdvancementCompleteObjectiveType();
    }

    @Nested
    @DisplayName("Identity")
    class Identity {

        @Test
        @DisplayName("getKey returns advancement_complete key")
        public void getKey_returnsAdvancementCompleteKey() {
            assertEquals(AdvancementCompleteObjectiveType.KEY, baseType.getKey());
            assertEquals("mcrpg", baseType.getKey().getNamespace());
            assertEquals("advancement_complete", baseType.getKey().getKey());
        }

        @Test
        @DisplayName("getExpansionKey returns McRPGExpansion key")
        public void getExpansionKey_returnsMcRPGExpansionKey() {
            assertTrue(baseType.getExpansionKey().isPresent());
            assertEquals(McRPGExpansion.EXPANSION_KEY, baseType.getExpansionKey().get());
        }
    }

    @Nested
    @DisplayName("canProcess")
    class CanProcess {

        @Test
        @DisplayName("returns true for AdvancementCompleteQuestContext")
        public void canProcess_returnsTrue_forCorrectContext() {
            AdvancementCompleteQuestContext context = mock(AdvancementCompleteQuestContext.class);
            assertTrue(baseType.canProcess(context));
        }

        @Test
        @DisplayName("returns false for generic mock context")
        public void canProcess_returnsFalse_forGenericContext() {
            QuestObjectiveProgressContext context = mock(QuestObjectiveProgressContext.class);
            assertFalse(baseType.canProcess(context));
        }

        @Test
        @DisplayName("returns false for BlockBreakQuestContext")
        public void canProcess_returnsFalse_forBlockBreakContext() {
            BlockBreakQuestContext context = mock(BlockBreakQuestContext.class);
            assertFalse(baseType.canProcess(context));
        }

        @Test
        @DisplayName("returns false for LoadoutEquipQuestContext")
        public void canProcess_returnsFalse_forLoadoutEquipContext() {
            LoadoutEquipQuestContext context = mock(LoadoutEquipQuestContext.class);
            assertFalse(baseType.canProcess(context));
        }
    }

    @Nested
    @DisplayName("processProgress — empty filter (base type)")
    class ProcessProgressEmptyFilter {

        @Test
        @DisplayName("returns 1 for any advancement when no filter is set")
        public void processProgress_returnsOne_whenNoFilter() {
            AdvancementCompleteQuestContext context = createContext("minecraft:story/iron_tools");
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(1, baseType.processProgress(instance, context));
        }

        @Test
        @DisplayName("returns 0 for wrong context type")
        public void processProgress_returnsZero_whenWrongContextType() {
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            QuestObjectiveProgressContext wrongContext = mock(QuestObjectiveProgressContext.class);
            assertEquals(0, baseType.processProgress(instance, wrongContext));
        }
    }

    @Nested
    @DisplayName("processProgress — specific advancement filter")
    class ProcessProgressSpecificFilter {

        private AdvancementCompleteObjectiveType configured;

        @BeforeEach
        public void setupConfigured() {
            Section section = mock(Section.class);
            when(section.contains("advancements")).thenReturn(true);
            when(section.getStringList("advancements")).thenReturn(
                    List.of("minecraft:story/iron_tools", "minecraft:story/upgrade_tools"));
            configured = baseType.parseConfig(section);
        }

        @Test
        @DisplayName("returns 1 when advancement key matches")
        public void processProgress_returnsOne_whenKeyMatches() {
            AdvancementCompleteQuestContext context = createContext("minecraft:story/iron_tools");
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(1, configured.processProgress(instance, context));
        }

        @Test
        @DisplayName("returns 1 for second matching advancement")
        public void processProgress_returnsOne_forSecondMatchingAdvancement() {
            AdvancementCompleteQuestContext context = createContext("minecraft:story/upgrade_tools");
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(1, configured.processProgress(instance, context));
        }

        @Test
        @DisplayName("returns 0 when advancement key does not match")
        public void processProgress_returnsZero_whenKeyDoesNotMatch() {
            AdvancementCompleteQuestContext context = createContext("minecraft:adventure/kill_a_mob");
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(0, configured.processProgress(instance, context));
        }
    }

    @Nested
    @DisplayName("parseConfig")
    class ParseConfig {

        @Test
        @DisplayName("no advancements key creates instance with empty filter")
        public void parseConfig_returnsNewInstance_withEmptyFilter() {
            Section section = mock(Section.class);
            when(section.contains("advancements")).thenReturn(false);

            AdvancementCompleteObjectiveType configured = baseType.parseConfig(section);
            assertNotSame(baseType, configured);

            AdvancementCompleteQuestContext context = createContext("minecraft:any/advancement");
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(1, configured.processProgress(instance, context));
        }

        @Test
        @DisplayName("advancements list is parsed from config")
        public void parseConfig_parsesAdvancementsList() {
            Section section = mock(Section.class);
            when(section.contains("advancements")).thenReturn(true);
            when(section.getStringList("advancements")).thenReturn(
                    List.of("minecraft:story/mine_stone"));

            AdvancementCompleteObjectiveType configured = baseType.parseConfig(section);
            assertNotSame(baseType, configured);
        }

        @Test
        @DisplayName("empty advancements list acts as no filter")
        public void parseConfig_emptyList_actsAsNoFilter() {
            Section section = mock(Section.class);
            when(section.contains("advancements")).thenReturn(true);
            when(section.getStringList("advancements")).thenReturn(List.of());

            AdvancementCompleteObjectiveType configured = baseType.parseConfig(section);

            AdvancementCompleteQuestContext context = createContext("minecraft:any/advancement");
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(1, configured.processProgress(instance, context));
        }

        @Test
        @DisplayName("single advancement in list filters correctly")
        public void parseConfig_singleAdvancement_filtersCorrectly() {
            Section section = mock(Section.class);
            when(section.contains("advancements")).thenReturn(true);
            when(section.getStringList("advancements")).thenReturn(
                    List.of("minecraft:story/mine_stone"));

            AdvancementCompleteObjectiveType configured = baseType.parseConfig(section);

            AdvancementCompleteQuestContext matching = createContext("minecraft:story/mine_stone");
            AdvancementCompleteQuestContext nonMatching = createContext("minecraft:adventure/kill_a_mob");
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            assertEquals(1, configured.processProgress(instance, matching));
            assertEquals(0, configured.processProgress(instance, nonMatching));
        }
    }

    private AdvancementCompleteQuestContext createContext(String advancementKey) {
        NamespacedKey key = NamespacedKey.fromString(advancementKey);
        Advancement advancement = mock(Advancement.class);
        when(advancement.getKey()).thenReturn(key);

        PlayerAdvancementDoneEvent event = mock(PlayerAdvancementDoneEvent.class);
        when(event.getAdvancement()).thenReturn(advancement);

        return new AdvancementCompleteQuestContext(event);
    }
}
