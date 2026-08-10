package us.eunoians.mcrpg.ability.impl.herbalism;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.configuration.FileManager;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.skill.HerbalismConfigFile;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.impl.herbalism.Herbalism;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TooManyPlantsTest extends McRPGBaseTest {

    private YamlDocument herbalismConfig;
    private TooManyPlants tooManyPlants;

    @BeforeEach
    void setUp() {
        herbalismConfig = mock(YamlDocument.class);
        FileManager fileManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.FILE);
        when(fileManager.getFile(FileType.HERBALISM_CONFIG)).thenReturn(herbalismConfig);

        when(herbalismConfig.getStringList(HerbalismConfigFile.TOO_MANY_PLANTS_VALID_DROPS))
                .thenReturn(List.of("WHEAT", "CARROTS"));

        tooManyPlants = new TooManyPlants(mcRPG);
    }

    @Nested
    @DisplayName("getActivationChance")
    class GetActivationChance {

        @Test
        @DisplayName("evaluates formula with herbalism_level variable")
        void getActivationChance_evaluatesFormulaWithHerbalismLevel() {
            int level = 10;
            SkillHolder skillHolder = mock(SkillHolder.class);
            SkillHolder.SkillHolderData skillHolderData = mock(SkillHolder.SkillHolderData.class);
            when(skillHolder.getSkillHolderData(Herbalism.HERBALISM_KEY)).thenReturn(Optional.of(skillHolderData));
            when(skillHolderData.getCurrentLevel()).thenReturn(level);
            when(herbalismConfig.getString(HerbalismConfigFile.TOO_MANY_PLANTS_ACTIVATION_EQUATION)).thenReturn("herbalism_level*3");

            assertEquals(30.0, tooManyPlants.getActivationChance(skillHolder), 0.001);
        }

        @Test
        @DisplayName("returns literal value when given plain number")
        void getActivationChance_returnsLiteral() {
            SkillHolder skillHolder = mock(SkillHolder.class);
            SkillHolder.SkillHolderData skillHolderData = mock(SkillHolder.SkillHolderData.class);
            when(skillHolder.getSkillHolderData(Herbalism.HERBALISM_KEY)).thenReturn(Optional.of(skillHolderData));
            when(skillHolderData.getCurrentLevel()).thenReturn(5);
            when(herbalismConfig.getString(HerbalismConfigFile.TOO_MANY_PLANTS_ACTIVATION_EQUATION)).thenReturn("25");

            assertEquals(25.0, tooManyPlants.getActivationChance(skillHolder), 0.001);
        }

        @Test
        @DisplayName("returns zero when holder has no herbalism skill data")
        void getActivationChance_returnsZero_whenNoHerbalismData() {
            SkillHolder skillHolder = mock(SkillHolder.class);
            when(skillHolder.getSkillHolderData(Herbalism.HERBALISM_KEY)).thenReturn(Optional.empty());

            assertEquals(0.0, tooManyPlants.getActivationChance(skillHolder), 0.001);
        }
    }

    @Nested
    @DisplayName("Metadata")
    class Metadata {

        @Test
        @DisplayName("getAbilityKey returns TOO_MANY_PLANTS_KEY")
        void getAbilityKey_returnsTooManyPlantsKey() {
            assertEquals(TooManyPlants.TOO_MANY_PLANTS_KEY, tooManyPlants.getAbilityKey());
        }

        @Test
        @DisplayName("getSkillKey returns HERBALISM_KEY")
        void getSkillKey_returnsHerbalismKey() {
            assertEquals(Herbalism.HERBALISM_KEY, tooManyPlants.getSkillKey());
        }

        @Test
        @DisplayName("getDatabaseName returns too_many_plants")
        void getDatabaseName_returnsTooManyPlants() {
            assertEquals("too_many_plants", tooManyPlants.getDatabaseName());
        }

        @Test
        @DisplayName("getAbilityEnabledRoute returns correct route")
        void getAbilityEnabledRoute_returnsCorrectRoute() {
            assertEquals(HerbalismConfigFile.TOO_MANY_PLANTS_ENABLED, tooManyPlants.getAbilityEnabledRoute());
        }

        @Test
        @DisplayName("getYamlDocument returns non-null")
        void getYamlDocument_returnsNonNull() {
            assertNotNull(tooManyPlants.getYamlDocument());
        }
    }

    @Nested
    @DisplayName("getMultiplierMap")
    class GetMultiplierMap {

        @Test
        @DisplayName("returns non-null map")
        void getMultiplierMap_returnsNonNull() {
            assertNotNull(tooManyPlants.getMultiplierMap());
        }

        @Test
        @DisplayName("returns empty map initially")
        void getMultiplierMap_returnsEmptyInitially() {
            assertTrue(tooManyPlants.getMultiplierMap().isEmpty());
        }
    }
}
