package us.eunoians.mcrpg.quest.objective.type.builtin;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.ability.AbilityType;
import us.eunoians.mcrpg.ability.impl.type.UnlockableAbility;
import us.eunoians.mcrpg.event.ability.AbilityUnlockEvent;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.quest.impl.objective.QuestObjectiveInstance;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveProgressContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("AbilityUnlockObjectiveType — extended coverage")
public class AbilityUnlockObjectiveTypeCoverageTest extends McRPGBaseTest {

    private AbilityUnlockObjectiveType baseType;

    @BeforeEach
    public void setup() {
        baseType = new AbilityUnlockObjectiveType();
    }

    @Nested
    @DisplayName("Identity")
    class Identity {

        @Test
        @DisplayName("getKey returns ability_unlock key")
        public void getKey_returnsAbilityUnlockKey() {
            assertEquals(AbilityUnlockObjectiveType.KEY, baseType.getKey());
            assertEquals("mcrpg", baseType.getKey().getNamespace());
            assertEquals("ability_unlock", baseType.getKey().getKey());
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
        @DisplayName("returns true for AbilityUnlockQuestContext")
        public void canProcess_returnsTrue_forCorrectContext() {
            AbilityUnlockEvent event = mock(AbilityUnlockEvent.class);
            AbilityUnlockQuestContext context = new AbilityUnlockQuestContext(event);
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
        @DisplayName("returns 1 for any ability unlock when no filter is set")
        public void processProgress_returnsOne_whenNoFilter() {
            UnlockableAbility ability = mock(UnlockableAbility.class);
            when(ability.getAbilityKey()).thenReturn(new NamespacedKey("mcrpg", "bleed"));
            when(ability.getAbilityType()).thenReturn(AbilityType.PASSIVE);
            AbilityUnlockEvent event = mock(AbilityUnlockEvent.class);
            when(event.getAbility()).thenReturn(ability);

            AbilityUnlockQuestContext context = new AbilityUnlockQuestContext(event);
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
    @DisplayName("processProgress — specific ability key filter")
    class ProcessProgressAbilityKeyFilter {

        private AbilityUnlockObjectiveType configured;

        @BeforeEach
        public void setupConfigured() {
            Section section = mock(Section.class);
            when(section.contains("ability")).thenReturn(true);
            when(section.getString("ability")).thenReturn("mcrpg:bleed");
            when(section.contains("ability-type")).thenReturn(false);
            configured = baseType.parseConfig(section);
        }

        @Test
        @DisplayName("returns 1 when ability key matches")
        public void processProgress_returnsOne_whenKeyMatches() {
            UnlockableAbility ability = mock(UnlockableAbility.class);
            when(ability.getAbilityKey()).thenReturn(new NamespacedKey("mcrpg", "bleed"));
            AbilityUnlockEvent event = mock(AbilityUnlockEvent.class);
            when(event.getAbility()).thenReturn(ability);

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(1, configured.processProgress(instance, new AbilityUnlockQuestContext(event)));
        }

        @Test
        @DisplayName("returns 0 when ability key does not match")
        public void processProgress_returnsZero_whenKeyDoesNotMatch() {
            UnlockableAbility ability = mock(UnlockableAbility.class);
            when(ability.getAbilityKey()).thenReturn(new NamespacedKey("mcrpg", "vampire"));
            AbilityUnlockEvent event = mock(AbilityUnlockEvent.class);
            when(event.getAbility()).thenReturn(ability);

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(0, configured.processProgress(instance, new AbilityUnlockQuestContext(event)));
        }
    }

    @Nested
    @DisplayName("processProgress — ability type filter")
    class ProcessProgressAbilityTypeFilter {

        @Test
        @DisplayName("returns 1 when PASSIVE filter matches PASSIVE ability")
        public void processProgress_returnsOne_whenPassiveTypeMatches() {
            Section section = mock(Section.class);
            when(section.contains("ability")).thenReturn(false);
            when(section.contains("ability-type")).thenReturn(true);
            when(section.getString("ability-type")).thenReturn("PASSIVE");
            AbilityUnlockObjectiveType configured = baseType.parseConfig(section);

            UnlockableAbility ability = mock(UnlockableAbility.class);
            when(ability.getAbilityKey()).thenReturn(new NamespacedKey("mcrpg", "bleed"));
            when(ability.getAbilityType()).thenReturn(AbilityType.PASSIVE);
            AbilityUnlockEvent event = mock(AbilityUnlockEvent.class);
            when(event.getAbility()).thenReturn(ability);

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(1, configured.processProgress(instance, new AbilityUnlockQuestContext(event)));
        }

        @Test
        @DisplayName("returns 0 when PASSIVE filter does not match ACTIVE ability")
        public void processProgress_returnsZero_whenPassiveFilterDoesNotMatchActive() {
            Section section = mock(Section.class);
            when(section.contains("ability")).thenReturn(false);
            when(section.contains("ability-type")).thenReturn(true);
            when(section.getString("ability-type")).thenReturn("PASSIVE");
            AbilityUnlockObjectiveType configured = baseType.parseConfig(section);

            UnlockableAbility ability = mock(UnlockableAbility.class);
            when(ability.getAbilityKey()).thenReturn(new NamespacedKey("mcrpg", "rage_spike"));
            when(ability.getAbilityType()).thenReturn(AbilityType.ACTIVE);
            AbilityUnlockEvent event = mock(AbilityUnlockEvent.class);
            when(event.getAbility()).thenReturn(ability);

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(0, configured.processProgress(instance, new AbilityUnlockQuestContext(event)));
        }

        @Test
        @DisplayName("returns 1 when ACTIVE filter matches ACTIVE ability")
        public void processProgress_returnsOne_whenActiveTypeMatches() {
            Section section = mock(Section.class);
            when(section.contains("ability")).thenReturn(false);
            when(section.contains("ability-type")).thenReturn(true);
            when(section.getString("ability-type")).thenReturn("ACTIVE");
            AbilityUnlockObjectiveType configured = baseType.parseConfig(section);

            UnlockableAbility ability = mock(UnlockableAbility.class);
            when(ability.getAbilityKey()).thenReturn(new NamespacedKey("mcrpg", "rage_spike"));
            when(ability.getAbilityType()).thenReturn(AbilityType.ACTIVE);
            AbilityUnlockEvent event = mock(AbilityUnlockEvent.class);
            when(event.getAbility()).thenReturn(ability);

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(1, configured.processProgress(instance, new AbilityUnlockQuestContext(event)));
        }

        @Test
        @DisplayName("returns 1 when INNATE filter matches INNATE ability")
        public void processProgress_returnsOne_whenInnateTypeMatches() {
            Section section = mock(Section.class);
            when(section.contains("ability")).thenReturn(false);
            when(section.contains("ability-type")).thenReturn(true);
            when(section.getString("ability-type")).thenReturn("INNATE");
            AbilityUnlockObjectiveType configured = baseType.parseConfig(section);

            UnlockableAbility ability = mock(UnlockableAbility.class);
            when(ability.getAbilityKey()).thenReturn(new NamespacedKey("mcrpg", "some_innate"));
            when(ability.getAbilityType()).thenReturn(AbilityType.INNATE);
            AbilityUnlockEvent event = mock(AbilityUnlockEvent.class);
            when(event.getAbility()).thenReturn(ability);

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(1, configured.processProgress(instance, new AbilityUnlockQuestContext(event)));
        }
    }

    @Nested
    @DisplayName("parseConfig")
    class ParseConfig {

        @Test
        @DisplayName("no filters creates instance with EMPTY filter")
        public void parseConfig_returnsNewInstance_withNoFilters() {
            Section section = mock(Section.class);
            when(section.contains("ability")).thenReturn(false);
            when(section.contains("ability-type")).thenReturn(false);

            AbilityUnlockObjectiveType configured = baseType.parseConfig(section);
            assertNotSame(baseType, configured);

            UnlockableAbility ability = mock(UnlockableAbility.class);
            when(ability.getAbilityKey()).thenReturn(new NamespacedKey("mcrpg", "anything"));
            when(ability.getAbilityType()).thenReturn(AbilityType.PASSIVE);
            AbilityUnlockEvent event = mock(AbilityUnlockEvent.class);
            when(event.getAbility()).thenReturn(ability);

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(1, configured.processProgress(instance, new AbilityUnlockQuestContext(event)));
        }

        @Test
        @DisplayName("ability key filter is parsed from config")
        public void parseConfig_parsesAbilityKeyFilter() {
            Section section = mock(Section.class);
            when(section.contains("ability")).thenReturn(true);
            when(section.getString("ability")).thenReturn("mcrpg:deeper_wound");
            when(section.contains("ability-type")).thenReturn(false);

            AbilityUnlockObjectiveType configured = baseType.parseConfig(section);
            assertNotSame(baseType, configured);
        }

        @Test
        @DisplayName("ability type filter is parsed from config")
        public void parseConfig_parsesAbilityTypeFilter() {
            Section section = mock(Section.class);
            when(section.contains("ability")).thenReturn(false);
            when(section.contains("ability-type")).thenReturn(true);
            when(section.getString("ability-type")).thenReturn("ACTIVE");

            AbilityUnlockObjectiveType configured = baseType.parseConfig(section);
            assertNotSame(baseType, configured);
        }

        @Test
        @DisplayName("invalid ability-type returns NEVER_MATCH filter")
        public void parseConfig_returnsNeverMatch_whenAbilityTypeInvalid() {
            Section section = mock(Section.class);
            when(section.contains("ability-type")).thenReturn(true);
            when(section.getString("ability-type")).thenReturn("INVALID_TYPE");
            when(section.contains("ability")).thenReturn(false);

            AbilityUnlockObjectiveType configured = baseType.parseConfig(section);

            UnlockableAbility ability = mock(UnlockableAbility.class);
            when(ability.getAbilityKey()).thenReturn(new NamespacedKey("mcrpg", "bleed"));
            when(ability.getAbilityType()).thenReturn(AbilityType.PASSIVE);
            AbilityUnlockEvent event = mock(AbilityUnlockEvent.class);
            when(event.getAbility()).thenReturn(ability);

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(0, configured.processProgress(instance, new AbilityUnlockQuestContext(event)));
        }

        @Test
        @DisplayName("invalid ability key returns NEVER_MATCH filter")
        public void parseConfig_returnsNeverMatch_whenAbilityKeyInvalid() {
            Section section = mock(Section.class);
            when(section.contains("ability-type")).thenReturn(false);
            when(section.contains("ability")).thenReturn(true);
            when(section.getString("ability")).thenReturn("not a valid key!!!");

            AbilityUnlockObjectiveType configured = baseType.parseConfig(section);

            UnlockableAbility ability = mock(UnlockableAbility.class);
            when(ability.getAbilityKey()).thenReturn(new NamespacedKey("mcrpg", "bleed"));
            AbilityUnlockEvent event = mock(AbilityUnlockEvent.class);
            when(event.getAbility()).thenReturn(ability);

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(0, configured.processProgress(instance, new AbilityUnlockQuestContext(event)));
        }

        @Test
        @DisplayName("ability key takes priority over ability type when both present")
        public void parseConfig_abilityKeyTakesPriority_whenBothPresent() {
            Section section = mock(Section.class);
            when(section.contains("ability-type")).thenReturn(true);
            when(section.getString("ability-type")).thenReturn("PASSIVE");
            when(section.contains("ability")).thenReturn(true);
            when(section.getString("ability")).thenReturn("mcrpg:bleed");

            AbilityUnlockObjectiveType configured = baseType.parseConfig(section);

            UnlockableAbility matchingKey = mock(UnlockableAbility.class);
            when(matchingKey.getAbilityKey()).thenReturn(new NamespacedKey("mcrpg", "bleed"));
            when(matchingKey.getAbilityType()).thenReturn(AbilityType.ACTIVE);
            AbilityUnlockEvent event = mock(AbilityUnlockEvent.class);
            when(event.getAbility()).thenReturn(matchingKey);

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(1, configured.processProgress(instance, new AbilityUnlockQuestContext(event)));
        }
    }
}
