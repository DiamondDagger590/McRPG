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
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.quest.impl.objective.QuestObjectiveInstance;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveProgressContext;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("LoadoutEquipObjectiveType")
class LoadoutEquipObjectiveTypeTest extends McRPGBaseTest {

    private LoadoutEquipObjectiveType type;

    @BeforeEach
    void setUp() {
        type = new LoadoutEquipObjectiveType();
        RegistryAccess.registryAccess().register(new AbilityRegistry(mcRPG));
    }

    @Nested
    @DisplayName("Identity")
    class Identity {

        @Test
        @DisplayName("getKey returns loadout_equip key")
        void getKey_returnsExpectedKey() {
            assertEquals(LoadoutEquipObjectiveType.KEY, type.getKey());
        }

        @Test
        @DisplayName("getExpansionKey returns McRPG expansion key")
        void getExpansionKey_returnsMcRPGExpansionKey() {
            assertTrue(type.getExpansionKey().isPresent());
            assertEquals(McRPGExpansion.EXPANSION_KEY, type.getExpansionKey().get());
        }
    }

    @Nested
    @DisplayName("canProcess")
    class CanProcess {

        @Test
        @DisplayName("returns true for LoadoutEquipQuestContext")
        void canProcess_returnsTrue_forCorrectContextType() {
            assertTrue(type.canProcess(mock(LoadoutEquipQuestContext.class)));
        }

        @Test
        @DisplayName("returns false for non-matching context")
        void canProcess_returnsFalse_forOtherContext() {
            QuestObjectiveProgressContext context = mock(QuestObjectiveProgressContext.class);
            assertFalse(type.canProcess(context));
        }
    }

    @Nested
    @DisplayName("parseConfig")
    class ParseConfig {

        @Test
        @DisplayName("no filters creates EMPTY filter instance")
        void parseConfig_noFilters_createsEmptyFilter() {
            Section section = mock(Section.class);
            when(section.contains("ability")).thenReturn(false);
            when(section.contains("ability-type")).thenReturn(false);
            LoadoutEquipObjectiveType configured = type.parseConfig(section);
            assertNotNull(configured);
        }

        @Test
        @DisplayName("parses ability-type PASSIVE filter")
        void parseConfig_abilityTypePassive_createsTypeFilter() {
            Section section = mock(Section.class);
            when(section.contains("ability-type")).thenReturn(true);
            when(section.getString("ability-type")).thenReturn("PASSIVE");
            when(section.contains("ability")).thenReturn(false);

            LoadoutEquipObjectiveType configured = type.parseConfig(section);
            assertNotNull(configured);

            Ability passiveAbility = mock(Ability.class);
            when(passiveAbility.getAbilityType()).thenReturn(AbilityType.PASSIVE);
            NamespacedKey passiveKey = new NamespacedKey("mcrpg", "test_passive");
            when(passiveAbility.getAbilityKey()).thenReturn(passiveKey);

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            LoadoutEquipQuestContext context = mockContextWithAbility(passiveKey);

            mockAbilityRegistry(passiveKey, passiveAbility);

            assertEquals(1L, configured.processProgress(instance, context));
        }

        @Test
        @DisplayName("parses ability-type ACTIVE filter")
        void parseConfig_abilityTypeActive_createsTypeFilter() {
            Section section = mock(Section.class);
            when(section.contains("ability-type")).thenReturn(true);
            when(section.getString("ability-type")).thenReturn("ACTIVE");
            when(section.contains("ability")).thenReturn(false);

            LoadoutEquipObjectiveType configured = type.parseConfig(section);
            assertNotNull(configured);

            Ability activeAbility = mock(Ability.class);
            when(activeAbility.getAbilityType()).thenReturn(AbilityType.ACTIVE);
            NamespacedKey activeKey = new NamespacedKey("mcrpg", "test_active");
            when(activeAbility.getAbilityKey()).thenReturn(activeKey);

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            LoadoutEquipQuestContext context = mockContextWithAbility(activeKey);

            mockAbilityRegistry(activeKey, activeAbility);

            assertEquals(1L, configured.processProgress(instance, context));
        }

        @Test
        @DisplayName("parses specific ability key filter")
        void parseConfig_specificAbility_createsKeyFilter() {
            Section section = mock(Section.class);
            when(section.contains("ability-type")).thenReturn(false);
            when(section.contains("ability")).thenReturn(true);
            when(section.getString("ability")).thenReturn("mcrpg:bleed");

            LoadoutEquipObjectiveType configured = type.parseConfig(section);
            assertNotNull(configured);

            NamespacedKey bleedKey = new NamespacedKey("mcrpg", "bleed");
            Ability bleedAbility = mock(Ability.class);
            when(bleedAbility.getAbilityKey()).thenReturn(bleedKey);
            when(bleedAbility.getAbilityType()).thenReturn(AbilityType.PASSIVE);

            mockAbilityRegistry(bleedKey, bleedAbility);

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            LoadoutEquipQuestContext context = mockContextWithAbility(bleedKey);
            assertEquals(1L, configured.processProgress(instance, context));
        }

        @Test
        @DisplayName("specific ability key takes priority over ability-type")
        void parseConfig_bothFilters_specificKeyTakesPriority() {
            Section section = mock(Section.class);
            when(section.contains("ability-type")).thenReturn(true);
            when(section.getString("ability-type")).thenReturn("ACTIVE");
            when(section.contains("ability")).thenReturn(true);
            when(section.getString("ability")).thenReturn("mcrpg:bleed");

            LoadoutEquipObjectiveType configured = type.parseConfig(section);

            NamespacedKey bleedKey = new NamespacedKey("mcrpg", "bleed");
            Ability bleedAbility = mock(Ability.class);
            when(bleedAbility.getAbilityKey()).thenReturn(bleedKey);
            when(bleedAbility.getAbilityType()).thenReturn(AbilityType.PASSIVE);

            mockAbilityRegistry(bleedKey, bleedAbility);

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            LoadoutEquipQuestContext context = mockContextWithAbility(bleedKey);
            assertEquals(1L, configured.processProgress(instance, context));
        }
    }

    @Nested
    @DisplayName("processProgress")
    class ProcessProgress {

        @Test
        @DisplayName("returns 0 for wrong context type")
        void processProgress_returnsZero_whenWrongContextType() {
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            QuestObjectiveProgressContext wrongContext = mock(QuestObjectiveProgressContext.class);
            assertEquals(0L, type.processProgress(instance, wrongContext));
        }

        @Test
        @DisplayName("unconfigured type returns 1 for any ability")
        void processProgress_returnsOne_whenUnconfigured() {
            NamespacedKey abilityKey = new NamespacedKey("mcrpg", "any_ability");
            Ability ability = mock(Ability.class);
            when(ability.getAbilityKey()).thenReturn(abilityKey);
            when(ability.getAbilityType()).thenReturn(AbilityType.INNATE);

            mockAbilityRegistry(abilityKey, ability);

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            LoadoutEquipQuestContext context = mockContextWithAbility(abilityKey);
            assertEquals(1L, type.processProgress(instance, context));
        }

        @Test
        @DisplayName("returns 0 when ability type does not match filter")
        void processProgress_returnsZero_whenAbilityTypeDoesNotMatch() {
            Section section = mock(Section.class);
            when(section.contains("ability-type")).thenReturn(true);
            when(section.getString("ability-type")).thenReturn("ACTIVE");
            when(section.contains("ability")).thenReturn(false);
            LoadoutEquipObjectiveType configured = type.parseConfig(section);

            NamespacedKey passiveKey = new NamespacedKey("mcrpg", "test_passive");
            Ability passiveAbility = mock(Ability.class);
            when(passiveAbility.getAbilityKey()).thenReturn(passiveKey);
            when(passiveAbility.getAbilityType()).thenReturn(AbilityType.PASSIVE);

            mockAbilityRegistry(passiveKey, passiveAbility);

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            LoadoutEquipQuestContext context = mockContextWithAbility(passiveKey);
            assertEquals(0L, configured.processProgress(instance, context));
        }

        @Test
        @DisplayName("returns 0 when specific ability key does not match filter")
        void processProgress_returnsZero_whenAbilityKeyDoesNotMatch() {
            Section section = mock(Section.class);
            when(section.contains("ability-type")).thenReturn(false);
            when(section.contains("ability")).thenReturn(true);
            when(section.getString("ability")).thenReturn("mcrpg:bleed");
            LoadoutEquipObjectiveType configured = type.parseConfig(section);

            NamespacedKey otherKey = new NamespacedKey("mcrpg", "vampire");
            Ability otherAbility = mock(Ability.class);
            when(otherAbility.getAbilityKey()).thenReturn(otherKey);
            when(otherAbility.getAbilityType()).thenReturn(AbilityType.PASSIVE);

            mockAbilityRegistry(otherKey, otherAbility);

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            LoadoutEquipQuestContext context = mockContextWithAbility(otherKey);
            assertEquals(0L, configured.processProgress(instance, context));
        }

        @Test
        @DisplayName("returns 1 when ability type matches INNATE filter")
        void processProgress_returnsOne_whenInnateTypeMatches() {
            Section section = mock(Section.class);
            when(section.contains("ability-type")).thenReturn(true);
            when(section.getString("ability-type")).thenReturn("INNATE");
            when(section.contains("ability")).thenReturn(false);
            LoadoutEquipObjectiveType configured = type.parseConfig(section);

            NamespacedKey innateKey = new NamespacedKey("mcrpg", "test_innate");
            Ability innateAbility = mock(Ability.class);
            when(innateAbility.getAbilityKey()).thenReturn(innateKey);
            when(innateAbility.getAbilityType()).thenReturn(AbilityType.INNATE);

            mockAbilityRegistry(innateKey, innateAbility);

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            LoadoutEquipQuestContext context = mockContextWithAbility(innateKey);
            assertEquals(1L, configured.processProgress(instance, context));
        }
    }

    private LoadoutEquipQuestContext mockContextWithAbility(NamespacedKey abilityKey) {
        LoadoutEquipQuestContext context = mock(LoadoutEquipQuestContext.class);
        when(context.getAbilityKey()).thenReturn(abilityKey);
        return context;
    }

    private void mockAbilityRegistry(NamespacedKey key, Ability ability) {
        AbilityRegistry abilityRegistry = RegistryAccess.registryAccess()
                .registry(McRPGRegistryKey.ABILITY);
        if (!abilityRegistry.registered(key)) {
            abilityRegistry.register(ability);
        }
    }
}
