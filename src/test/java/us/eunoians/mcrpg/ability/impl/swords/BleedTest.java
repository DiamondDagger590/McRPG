package us.eunoians.mcrpg.ability.impl.swords;

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
import us.eunoians.mcrpg.configuration.file.skill.SwordsConfigFile;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.impl.swords.Swords;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link Bleed} configuration value resolution and metadata accessors.
 * <p>
 * Unlike tierable abilities, Bleed uses the holder's swords level via the
 * {@code swords_level} Parser variable rather than a tier-based route lookup.
 */
class BleedTest extends McRPGBaseTest {

    private YamlDocument swordsConfig;
    private Bleed bleed;

    @BeforeEach
    void setUp() {
        swordsConfig = mock(YamlDocument.class);
        FileManager fileManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.FILE);
        when(fileManager.getFile(FileType.SWORDS_CONFIG)).thenReturn(swordsConfig);

        bleed = new Bleed(mcRPG);
    }

    @Nested
    @DisplayName("getActivationChance")
    class GetActivationChance {

        @Test
        @DisplayName("evaluates formula with swords_level variable")
        void getActivationChance_evaluatesFormulaWithSwordsLevel() {
            int level = 10;
            SkillHolder skillHolder = mock(SkillHolder.class);
            SkillHolder.SkillHolderData skillHolderData = mock(SkillHolder.SkillHolderData.class);
            when(skillHolder.getSkillHolderData(Swords.SWORDS_KEY)).thenReturn(Optional.of(skillHolderData));
            when(skillHolderData.getCurrentLevel()).thenReturn(level);
            when(swordsConfig.getString(SwordsConfigFile.BLEED_ACTIVATION_EQUATION)).thenReturn("swords_level*2");

            assertEquals(20.0, bleed.getActivationChance(skillHolder), 0.001);
        }

        @Test
        @DisplayName("returns literal value when given plain number")
        void getActivationChance_returnsLiteral() {
            SkillHolder skillHolder = mock(SkillHolder.class);
            SkillHolder.SkillHolderData skillHolderData = mock(SkillHolder.SkillHolderData.class);
            when(skillHolder.getSkillHolderData(Swords.SWORDS_KEY)).thenReturn(Optional.of(skillHolderData));
            when(skillHolderData.getCurrentLevel()).thenReturn(5);
            when(swordsConfig.getString(SwordsConfigFile.BLEED_ACTIVATION_EQUATION)).thenReturn("30");

            assertEquals(30.0, bleed.getActivationChance(skillHolder), 0.001);
        }

        @Test
        @DisplayName("returns zero when holder is not a SkillHolder")
        void getActivationChance_returnsZero_whenNotSkillHolder() {
            AbilityHolder abilityHolder = mock(AbilityHolder.class);

            assertEquals(0.0, bleed.getActivationChance(abilityHolder), 0.001);
        }

        @Test
        @DisplayName("returns zero when skill data is absent")
        void getActivationChance_returnsZero_whenNoSwordsData() {
            SkillHolder skillHolder = mock(SkillHolder.class);
            when(skillHolder.getSkillHolderData(Swords.SWORDS_KEY)).thenReturn(Optional.empty());

            assertEquals(0.0, bleed.getActivationChance(skillHolder), 0.001);
        }
    }

    @Nested
    @DisplayName("Metadata")
    class Metadata {

        @Test
        @DisplayName("getAbilityKey returns BLEED_KEY")
        void getAbilityKey_returnsBleedKey() {
            assertEquals(Bleed.BLEED_KEY, bleed.getAbilityKey());
        }

        @Test
        @DisplayName("getSkillKey returns SWORDS_KEY")
        void getSkillKey_returnsSwordsKey() {
            assertEquals(Swords.SWORDS_KEY, bleed.getSkillKey());
        }

        @Test
        @DisplayName("getDatabaseName returns bleed")
        void getDatabaseName_returnsBleed() {
            assertEquals("bleed", bleed.getDatabaseName());
        }

        @Test
        @DisplayName("getAbilityEnabledRoute returns correct route")
        void getAbilityEnabledRoute_returnsCorrectRoute() {
            assertEquals(SwordsConfigFile.BLEED_ENABLED, bleed.getAbilityEnabledRoute());
        }

        @Test
        @DisplayName("getYamlDocument returns non-null")
        void getYamlDocument_returnsNonNull() {
            assertNotNull(bleed.getYamlDocument());
        }
    }
}
