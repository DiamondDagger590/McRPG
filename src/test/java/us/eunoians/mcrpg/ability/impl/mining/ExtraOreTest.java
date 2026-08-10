package us.eunoians.mcrpg.ability.impl.mining;

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
import us.eunoians.mcrpg.configuration.file.skill.MiningConfigFile;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.impl.mining.Mining;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExtraOreTest extends McRPGBaseTest {

    private YamlDocument miningConfig;
    private ExtraOre extraOre;

    @BeforeEach
    void setUp() {
        miningConfig = mock(YamlDocument.class);
        FileManager fileManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.FILE);
        when(fileManager.getFile(FileType.MINING_CONFIG)).thenReturn(miningConfig);

        when(miningConfig.getStringList(MiningConfigFile.EXTRA_ORE_VALID_DROPS))
                .thenReturn(List.of("DIAMOND_ORE", "IRON_ORE"));

        extraOre = new ExtraOre(mcRPG);
    }

    @Nested
    @DisplayName("getActivationChance")
    class GetActivationChance {

        @Test
        @DisplayName("evaluates formula with mining_level variable")
        void getActivationChance_evaluatesFormulaWithMiningLevel() {
            int level = 10;
            SkillHolder skillHolder = mock(SkillHolder.class);
            SkillHolder.SkillHolderData skillHolderData = mock(SkillHolder.SkillHolderData.class);
            when(skillHolder.getSkillHolderData(Mining.MINING_KEY)).thenReturn(Optional.of(skillHolderData));
            when(skillHolderData.getCurrentLevel()).thenReturn(level);
            when(miningConfig.getString(MiningConfigFile.EXTRA_ORE_ACTIVATION_EQUATION)).thenReturn("mining_level*2");

            assertEquals(20.0, extraOre.getActivationChance(skillHolder), 0.001);
        }

        @Test
        @DisplayName("returns literal value when given plain number")
        void getActivationChance_returnsLiteral() {
            SkillHolder skillHolder = mock(SkillHolder.class);
            SkillHolder.SkillHolderData skillHolderData = mock(SkillHolder.SkillHolderData.class);
            when(skillHolder.getSkillHolderData(Mining.MINING_KEY)).thenReturn(Optional.of(skillHolderData));
            when(skillHolderData.getCurrentLevel()).thenReturn(5);
            when(miningConfig.getString(MiningConfigFile.EXTRA_ORE_ACTIVATION_EQUATION)).thenReturn("30");

            assertEquals(30.0, extraOre.getActivationChance(skillHolder), 0.001);
        }

        @Test
        @DisplayName("returns zero when holder has no mining skill data")
        void getActivationChance_returnsZero_whenNoMiningData() {
            SkillHolder skillHolder = mock(SkillHolder.class);
            when(skillHolder.getSkillHolderData(Mining.MINING_KEY)).thenReturn(Optional.empty());

            assertEquals(0.0, extraOre.getActivationChance(skillHolder), 0.001);
        }
    }

    @Nested
    @DisplayName("Metadata")
    class Metadata {

        @Test
        @DisplayName("getAbilityKey returns EXTRA_ORE_KEY")
        void getAbilityKey_returnsExtraOreKey() {
            assertEquals(ExtraOre.EXTRA_ORE_KEY, extraOre.getAbilityKey());
        }

        @Test
        @DisplayName("getSkillKey returns MINING_KEY")
        void getSkillKey_returnsMiningKey() {
            assertEquals(Mining.MINING_KEY, extraOre.getSkillKey());
        }

        @Test
        @DisplayName("getDatabaseName returns extra_ore")
        void getDatabaseName_returnsExtraOre() {
            assertEquals("extra_ore", extraOre.getDatabaseName());
        }

        @Test
        @DisplayName("getAbilityEnabledRoute returns correct route")
        void getAbilityEnabledRoute_returnsCorrectRoute() {
            assertEquals(MiningConfigFile.EXTRA_ORE_ENABLED, extraOre.getAbilityEnabledRoute());
        }

        @Test
        @DisplayName("getYamlDocument returns non-null")
        void getYamlDocument_returnsNonNull() {
            assertNotNull(extraOre.getYamlDocument());
        }
    }

    @Nested
    @DisplayName("getMultiplierMap")
    class GetMultiplierMap {

        @Test
        @DisplayName("returns non-null map")
        void getMultiplierMap_returnsNonNull() {
            assertNotNull(extraOre.getMultiplierMap());
        }

        @Test
        @DisplayName("returns empty map initially")
        void getMultiplierMap_returnsEmptyInitially() {
            assertTrue(extraOre.getMultiplierMap().isEmpty());
        }
    }
}
