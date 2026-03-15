package us.eunoians.mcrpg.skill.impl;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.statistic.Statistic;
import com.diamonddagger590.mccore.statistic.StatisticType;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.configuration.FileManager;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.skill.MiningConfigFile;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.SkillRegistry;
import us.eunoians.mcrpg.skill.impl.mining.Mining;
import us.eunoians.mcrpg.statistic.McRPGStatistic;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class McRPGSkillDefaultStatisticsTest extends McRPGBaseTest {

    private Mining mining;

    @BeforeEach
    public void setup() {
        SkillRegistry skillRegistry = new SkillRegistry();
        RegistryAccess.registryAccess().register(skillRegistry);

        YamlDocument miningConfig = mock(YamlDocument.class);
        when(miningConfig.getStringList(MiningConfigFile.ALLOWED_ITEMS_FOR_EXPERIENCE_GAIN)).thenReturn(List.of("DIAMOND_PICKAXE"));
        FileManager fileManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE);
        when(fileManager.getFile(FileType.MINING_CONFIG)).thenReturn(miningConfig);

        mining = new Mining(mcRPG);
        skillRegistry.register(mining);
    }

    @DisplayName("Given a McRPGSkill (Mining), when getting default statistics, then it returns exactly two statistics")
    @Test
    public void getDefaultStatistics_returnsTwoStatistics() {
        Set<Statistic> stats = mining.getDefaultStatistics();
        assertEquals(2, stats.size());
    }

    @DisplayName("Given a McRPGSkill (Mining), when getting default statistics, then one is an experience statistic")
    @Test
    public void getDefaultStatistics_containsExperienceStatistic() {
        Set<Statistic> stats = mining.getDefaultStatistics();

        boolean hasExperience = stats.stream().anyMatch(s ->
                s.getStatisticKey().equals(McRPGStatistic.getSkillExperienceKey(mining.getSkillKey()))
                        && s.getStatisticType() == StatisticType.LONG
        );
        assertTrue(hasExperience, "Expected an experience statistic for the mining skill");
    }

    @DisplayName("Given a McRPGSkill (Mining), when getting default statistics, then one is a max level statistic")
    @Test
    public void getDefaultStatistics_containsMaxLevelStatistic() {
        Set<Statistic> stats = mining.getDefaultStatistics();

        boolean hasMaxLevel = stats.stream().anyMatch(s ->
                s.getStatisticKey().equals(McRPGStatistic.getSkillMaxLevelKey(mining.getSkillKey()))
                        && s.getStatisticType() == StatisticType.INT
        );
        assertTrue(hasMaxLevel, "Expected a max level statistic for the mining skill");
    }

    @DisplayName("Given a plain Skill (MockSkill), when getting default statistics, then it returns empty")
    @Test
    public void getDefaultStatistics_returnsEmpty_forBaseSkillInterface() {
        MockSkill mockSkill = mock(MockSkill.class);
        when(mockSkill.getDefaultStatistics()).thenCallRealMethod();

        Set<Statistic> stats = mockSkill.getDefaultStatistics();
        assertTrue(stats.isEmpty());
    }
}
