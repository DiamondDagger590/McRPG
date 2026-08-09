package us.eunoians.mcrpg.quest.objective.type.builtin;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.AbilityType;
import us.eunoians.mcrpg.event.loadout.LoadoutAbilityChangeEvent;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.quest.impl.objective.QuestObjectiveInstance;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveProgressContext;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("LoadoutEquipObjectiveType — extended coverage")
public class LoadoutEquipObjectiveTypeCoverageTest extends McRPGBaseTest {

    private LoadoutEquipObjectiveType baseType;

    @BeforeEach
    public void setup() {
        RegistryAccess registryAccess = RegistryAccess.registryAccess();
        if (registryAccess.registry(McRPGRegistryKey.ABILITY) == null) {
            registryAccess.register(new AbilityRegistry(mcRPG));
        }
        baseType = new LoadoutEquipObjectiveType();
    }

    @Nested
    @DisplayName("Identity")
    class Identity {

        @Test
        @DisplayName("getKey returns loadout_equip key")
        public void getKey_returnsLoadoutEquipKey() {
            assertEquals(LoadoutEquipObjectiveType.KEY, baseType.getKey());
            assertEquals("mcrpg", baseType.getKey().getNamespace());
            assertEquals("loadout_equip", baseType.getKey().getKey());
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
        @DisplayName("returns true for LoadoutEquipQuestContext")
        public void canProcess_returnsTrue_forCorrectContext() {
            LoadoutEquipQuestContext context = mock(LoadoutEquipQuestContext.class);
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
        @DisplayName("returns false for AbilityUnlockQuestContext")
        public void canProcess_returnsFalse_forAbilityUnlockContext() {
            AbilityUnlockQuestContext context = mock(AbilityUnlockQuestContext.class);
            assertFalse(baseType.canProcess(context));
        }
    }

    @Nested
    @DisplayName("processProgress — wrong context")
    class ProcessProgressWrongContext {

        @Test
        @DisplayName("returns 0 for wrong context type")
        public void processProgress_returnsZero_whenWrongContextType() {
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            QuestObjectiveProgressContext wrongContext = mock(QuestObjectiveProgressContext.class);
            assertEquals(0, baseType.processProgress(instance, wrongContext));
        }
    }

    @Nested
    @DisplayName("parseConfig")
    class ParseConfig {

        @Test
        @DisplayName("no filters creates new instance with EMPTY filter")
        public void parseConfig_returnsNewInstance_withNoFilters() {
            Section section = mock(Section.class);
            when(section.contains("ability")).thenReturn(false);
            when(section.contains("ability-type")).thenReturn(false);

            LoadoutEquipObjectiveType configured = baseType.parseConfig(section);
            assertNotSame(baseType, configured);
        }

        @Test
        @DisplayName("ability key filter is parsed from config")
        public void parseConfig_parsesAbilityKeyFilter() {
            Section section = mock(Section.class);
            when(section.contains("ability")).thenReturn(true);
            when(section.getString("ability")).thenReturn("mcrpg:bleed");
            when(section.contains("ability-type")).thenReturn(false);

            LoadoutEquipObjectiveType configured = baseType.parseConfig(section);
            assertNotSame(baseType, configured);
        }

        @Test
        @DisplayName("ability type filter is parsed from config")
        public void parseConfig_parsesAbilityTypeFilter() {
            Section section = mock(Section.class);
            when(section.contains("ability")).thenReturn(false);
            when(section.contains("ability-type")).thenReturn(true);
            when(section.getString("ability-type")).thenReturn("ACTIVE");

            LoadoutEquipObjectiveType configured = baseType.parseConfig(section);
            assertNotSame(baseType, configured);
        }

        @Test
        @DisplayName("invalid ability-type returns NEVER_MATCH filter")
        public void parseConfig_returnsNeverMatch_whenAbilityTypeInvalid() {
            Section section = mock(Section.class);
            when(section.contains("ability-type")).thenReturn(true);
            when(section.getString("ability-type")).thenReturn("INVALID_TYPE");
            when(section.contains("ability")).thenReturn(false);

            LoadoutEquipObjectiveType configured = baseType.parseConfig(section);

            LoadoutAbilityChangeEvent event = createEquipEvent(new NamespacedKey("mcrpg", "bleed"));
            LoadoutEquipQuestContext context = new LoadoutEquipQuestContext(event);

            Ability ability = mock(Ability.class);
            when(ability.getAbilityKey()).thenReturn(new NamespacedKey("mcrpg", "bleed"));
            when(ability.getAbilityType()).thenReturn(AbilityType.PASSIVE);
            RegistryAccess.registryAccess().registry(McRPGRegistryKey.ABILITY)
                    .register(ability);

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(0, configured.processProgress(instance, context));
        }

        @Test
        @DisplayName("invalid ability key returns NEVER_MATCH filter")
        public void parseConfig_returnsNeverMatch_whenAbilityKeyInvalid() {
            Section section = mock(Section.class);
            when(section.contains("ability-type")).thenReturn(false);
            when(section.contains("ability")).thenReturn(true);
            when(section.getString("ability")).thenReturn("not a valid key!!!");

            LoadoutEquipObjectiveType configured = baseType.parseConfig(section);

            LoadoutAbilityChangeEvent event = createEquipEvent(new NamespacedKey("mcrpg", "bleed"));
            LoadoutEquipQuestContext context = new LoadoutEquipQuestContext(event);

            Ability ability = mock(Ability.class);
            when(ability.getAbilityKey()).thenReturn(new NamespacedKey("mcrpg", "bleed"));
            RegistryAccess.registryAccess().registry(McRPGRegistryKey.ABILITY)
                    .register(ability);

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(0, configured.processProgress(instance, context));
        }

        @Test
        @DisplayName("ability key takes priority over ability type when both present")
        public void parseConfig_abilityKeyTakesPriority_whenBothPresent() {
            Section section = mock(Section.class);
            when(section.contains("ability-type")).thenReturn(true);
            when(section.getString("ability-type")).thenReturn("PASSIVE");
            when(section.contains("ability")).thenReturn(true);
            when(section.getString("ability")).thenReturn("mcrpg:bleed");

            LoadoutEquipObjectiveType configured = baseType.parseConfig(section);

            Ability matchingKey = mock(Ability.class);
            when(matchingKey.getAbilityKey()).thenReturn(new NamespacedKey("mcrpg", "bleed"));
            when(matchingKey.getAbilityType()).thenReturn(AbilityType.ACTIVE);
            RegistryAccess.registryAccess().registry(McRPGRegistryKey.ABILITY)
                    .register(matchingKey);

            LoadoutAbilityChangeEvent event = createEquipEvent(new NamespacedKey("mcrpg", "bleed"));
            LoadoutEquipQuestContext context = new LoadoutEquipQuestContext(event);

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(1, configured.processProgress(instance, context));
        }
    }

    @Nested
    @DisplayName("processProgress — specific ability key filter")
    class ProcessProgressAbilityKeyFilter {

        private LoadoutEquipObjectiveType configured;

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
            Ability ability = mock(Ability.class);
            when(ability.getAbilityKey()).thenReturn(new NamespacedKey("mcrpg", "bleed"));
            RegistryAccess.registryAccess().registry(McRPGRegistryKey.ABILITY)
                    .register(ability);

            LoadoutAbilityChangeEvent event = createEquipEvent(new NamespacedKey("mcrpg", "bleed"));
            LoadoutEquipQuestContext context = new LoadoutEquipQuestContext(event);

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(1, configured.processProgress(instance, context));
        }

        @Test
        @DisplayName("returns 0 when ability key does not match")
        public void processProgress_returnsZero_whenKeyDoesNotMatch() {
            Ability ability = mock(Ability.class);
            when(ability.getAbilityKey()).thenReturn(new NamespacedKey("mcrpg", "vampire"));
            RegistryAccess.registryAccess().registry(McRPGRegistryKey.ABILITY)
                    .register(ability);

            LoadoutAbilityChangeEvent event = createEquipEvent(new NamespacedKey("mcrpg", "vampire"));
            LoadoutEquipQuestContext context = new LoadoutEquipQuestContext(event);

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(0, configured.processProgress(instance, context));
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
            LoadoutEquipObjectiveType configured = baseType.parseConfig(section);

            Ability ability = mock(Ability.class);
            when(ability.getAbilityKey()).thenReturn(new NamespacedKey("mcrpg", "bleed"));
            when(ability.getAbilityType()).thenReturn(AbilityType.PASSIVE);
            RegistryAccess.registryAccess().registry(McRPGRegistryKey.ABILITY)
                    .register(ability);

            LoadoutAbilityChangeEvent event = createEquipEvent(new NamespacedKey("mcrpg", "bleed"));
            LoadoutEquipQuestContext context = new LoadoutEquipQuestContext(event);

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(1, configured.processProgress(instance, context));
        }

        @Test
        @DisplayName("returns 0 when PASSIVE filter does not match ACTIVE ability")
        public void processProgress_returnsZero_whenPassiveFilterDoesNotMatchActive() {
            Section section = mock(Section.class);
            when(section.contains("ability")).thenReturn(false);
            when(section.contains("ability-type")).thenReturn(true);
            when(section.getString("ability-type")).thenReturn("PASSIVE");
            LoadoutEquipObjectiveType configured = baseType.parseConfig(section);

            Ability ability = mock(Ability.class);
            when(ability.getAbilityKey()).thenReturn(new NamespacedKey("mcrpg", "rage_spike"));
            when(ability.getAbilityType()).thenReturn(AbilityType.ACTIVE);
            RegistryAccess.registryAccess().registry(McRPGRegistryKey.ABILITY)
                    .register(ability);

            LoadoutAbilityChangeEvent event = createEquipEvent(new NamespacedKey("mcrpg", "rage_spike"));
            LoadoutEquipQuestContext context = new LoadoutEquipQuestContext(event);

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(0, configured.processProgress(instance, context));
        }

        @Test
        @DisplayName("returns 1 when ACTIVE filter matches ACTIVE ability")
        public void processProgress_returnsOne_whenActiveTypeMatches() {
            Section section = mock(Section.class);
            when(section.contains("ability")).thenReturn(false);
            when(section.contains("ability-type")).thenReturn(true);
            when(section.getString("ability-type")).thenReturn("ACTIVE");
            LoadoutEquipObjectiveType configured = baseType.parseConfig(section);

            Ability ability = mock(Ability.class);
            when(ability.getAbilityKey()).thenReturn(new NamespacedKey("mcrpg", "rage_spike"));
            when(ability.getAbilityType()).thenReturn(AbilityType.ACTIVE);
            RegistryAccess.registryAccess().registry(McRPGRegistryKey.ABILITY)
                    .register(ability);

            LoadoutAbilityChangeEvent event = createEquipEvent(new NamespacedKey("mcrpg", "rage_spike"));
            LoadoutEquipQuestContext context = new LoadoutEquipQuestContext(event);

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(1, configured.processProgress(instance, context));
        }

        @Test
        @DisplayName("returns 1 when INNATE filter matches INNATE ability")
        public void processProgress_returnsOne_whenInnateTypeMatches() {
            Section section = mock(Section.class);
            when(section.contains("ability")).thenReturn(false);
            when(section.contains("ability-type")).thenReturn(true);
            when(section.getString("ability-type")).thenReturn("INNATE");
            LoadoutEquipObjectiveType configured = baseType.parseConfig(section);

            Ability ability = mock(Ability.class);
            when(ability.getAbilityKey()).thenReturn(new NamespacedKey("mcrpg", "some_innate"));
            when(ability.getAbilityType()).thenReturn(AbilityType.INNATE);
            RegistryAccess.registryAccess().registry(McRPGRegistryKey.ABILITY)
                    .register(ability);

            LoadoutAbilityChangeEvent event = createEquipEvent(new NamespacedKey("mcrpg", "some_innate"));
            LoadoutEquipQuestContext context = new LoadoutEquipQuestContext(event);

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(1, configured.processProgress(instance, context));
        }
    }

    private LoadoutAbilityChangeEvent createEquipEvent(NamespacedKey abilityKey) {
        return new LoadoutAbilityChangeEvent(
                UUID.randomUUID(),
                LoadoutAbilityChangeEvent.ChangeReason.EQUIP,
                null,
                abilityKey,
                0
        );
    }
}
