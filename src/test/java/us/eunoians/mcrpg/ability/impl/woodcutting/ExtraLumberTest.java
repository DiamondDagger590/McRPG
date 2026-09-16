package us.eunoians.mcrpg.ability.impl.woodcutting;

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
import us.eunoians.mcrpg.configuration.file.skill.WoodcuttingConfigFile;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.impl.woodcutting.WoodCutting;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link ExtraLumber} configuration value resolution and metadata accessors.
 * <p>
 * ExtraLumber uses the holder's woodcutting level via the
 * {@code woodcutting_level} Parser variable rather than a tier-based route lookup.
 */
class ExtraLumberTest extends McRPGBaseTest {

    private YamlDocument woodcuttingConfig;
    private ExtraLumber extraLumber;

    @BeforeEach
    void setUp() {
        woodcuttingConfig = mock(YamlDocument.class);
        FileManager fileManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.FILE);
        when(fileManager.getFile(FileType.WOODCUTTING_CONFIG)).thenReturn(woodcuttingConfig);

        extraLumber = new ExtraLumber(mcRPG);
    }

    @Nested
    @DisplayName("getActivationChance")
    class GetActivationChance {

        @Test
        @DisplayName("evaluates formula with woodcutting_level variable")
        void getActivationChance_evaluatesFormulaWithWoodcuttingLevel() {
            int level = 10;
            SkillHolder skillHolder = mock(SkillHolder.class);
            SkillHolder.SkillHolderData skillHolderData = mock(SkillHolder.SkillHolderData.class);
            when(skillHolder.getSkillHolderData(WoodCutting.WOODCUTTING_KEY)).thenReturn(Optional.of(skillHolderData));
            when(skillHolderData.getCurrentLevel()).thenReturn(level);
            when(woodcuttingConfig.getString(WoodcuttingConfigFile.EXTRA_LUMBER_ACTIVATION_EQUATION)).thenReturn("woodcutting_level*2");

            assertEquals(20.0, extraLumber.getActivationChance(skillHolder), 0.001);
        }

        @Test
        @DisplayName("returns literal value when given plain number")
        void getActivationChance_returnsLiteral() {
            SkillHolder skillHolder = mock(SkillHolder.class);
            SkillHolder.SkillHolderData skillHolderData = mock(SkillHolder.SkillHolderData.class);
            when(skillHolder.getSkillHolderData(WoodCutting.WOODCUTTING_KEY)).thenReturn(Optional.of(skillHolderData));
            when(skillHolderData.getCurrentLevel()).thenReturn(5);
            when(woodcuttingConfig.getString(WoodcuttingConfigFile.EXTRA_LUMBER_ACTIVATION_EQUATION)).thenReturn("30");

            assertEquals(30.0, extraLumber.getActivationChance(skillHolder), 0.001);
        }

        @Test
        @DisplayName("returns zero when holder has no woodcutting skill data")
        void getActivationChance_returnsZero_whenNoWoodcuttingData() {
            SkillHolder skillHolder = mock(SkillHolder.class);
            when(skillHolder.getSkillHolderData(WoodCutting.WOODCUTTING_KEY)).thenReturn(Optional.empty());

            assertEquals(0.0, extraLumber.getActivationChance(skillHolder), 0.001);
        }

        @Test
        @DisplayName("evaluates complex formula with woodcutting_level variable")
        void getActivationChance_evaluatesComplexFormula() {
            int level = 20;
            SkillHolder skillHolder = mock(SkillHolder.class);
            SkillHolder.SkillHolderData skillHolderData = mock(SkillHolder.SkillHolderData.class);
            when(skillHolder.getSkillHolderData(WoodCutting.WOODCUTTING_KEY)).thenReturn(Optional.of(skillHolderData));
            when(skillHolderData.getCurrentLevel()).thenReturn(level);
            when(woodcuttingConfig.getString(WoodcuttingConfigFile.EXTRA_LUMBER_ACTIVATION_EQUATION)).thenReturn("woodcutting_level+5");

            assertEquals(25.0, extraLumber.getActivationChance(skillHolder), 0.001);
        }
    }

    @Nested
    @DisplayName("Metadata")
    class Metadata {

        @Test
        @DisplayName("getAbilityKey returns EXTRA_LUMBER_KEY")
        void getAbilityKey_returnsExtraLumberKey() {
            assertEquals(ExtraLumber.EXTRA_LUMBER_KEY, extraLumber.getAbilityKey());
        }

        @Test
        @DisplayName("getSkillKey returns WOODCUTTING_KEY")
        void getSkillKey_returnsWoodcuttingKey() {
            assertEquals(WoodCutting.WOODCUTTING_KEY, extraLumber.getSkillKey());
        }

        @Test
        @DisplayName("getDatabaseName returns extra_lumber")
        void getDatabaseName_returnsExtraLumber() {
            assertEquals("extra_lumber", extraLumber.getDatabaseName());
        }

        @Test
        @DisplayName("getAbilityEnabledRoute returns correct route")
        void getAbilityEnabledRoute_returnsCorrectRoute() {
            assertEquals(WoodcuttingConfigFile.EXTRA_LUMBER_ENABLED, extraLumber.getAbilityEnabledRoute());
        }

        @Test
        @DisplayName("getYamlDocument returns non-null")
        void getYamlDocument_returnsNonNull() {
            assertNotNull(extraLumber.getYamlDocument());
        }
    }
}
