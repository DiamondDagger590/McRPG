package us.eunoians.mcrpg.statistic;

import com.diamonddagger590.mccore.statistic.Statistic;
import com.diamonddagger590.mccore.statistic.StatisticType;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.util.McRPGMethods;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class McRPGStatisticTest extends McRPGBaseTest {

    @DisplayName("Given a skill key, when getting the experience key, then the key follows the expected format")
    @Test
    public void getSkillExperienceKey_returnsCorrectFormat() {
        NamespacedKey skillKey = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "mining");
        NamespacedKey result = McRPGStatistic.getSkillExperienceKey(skillKey);
        assertEquals("mcrpg", result.getNamespace());
        assertEquals("mining_experience", result.getKey());
    }

    @DisplayName("Given a skill key, when getting the max level key, then the key follows the expected format")
    @Test
    public void getSkillMaxLevelKey_returnsCorrectFormat() {
        NamespacedKey skillKey = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "swords");
        NamespacedKey result = McRPGStatistic.getSkillMaxLevelKey(skillKey);
        assertEquals("mcrpg", result.getNamespace());
        assertEquals("swords_max_level", result.getKey());
    }

    @DisplayName("Given an ability key, when getting the activation key, then the key follows the expected format")
    @Test
    public void getAbilityActivationKey_returnsCorrectFormat() {
        NamespacedKey abilityKey = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "bleed");
        NamespacedKey result = McRPGStatistic.getAbilityActivationKey(abilityKey);
        assertEquals("mcrpg", result.getNamespace());
        assertEquals("bleed_activations", result.getKey());
    }

    @DisplayName("Given an ability key and display name, when creating an activation statistic, then the statistic has correct properties")
    @Test
    public void createAbilityActivationStatistic_hasCorrectProperties() {
        NamespacedKey abilityKey = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "bleed");
        Statistic stat = McRPGStatistic.createAbilityActivationStatistic(abilityKey, "Bleed");

        assertEquals("bleed_activations", stat.getStatisticKey().getKey());
        assertEquals(StatisticType.LONG, stat.getStatisticType());
        assertEquals(0L, stat.getDefaultValue());
        assertEquals("Bleed Activations", stat.getDisplayName());
        assertEquals("Times Bleed has been activated", stat.getDescription());
    }

    @DisplayName("Given a skill key and display name, when creating a skill experience statistic, then the statistic has correct properties")
    @Test
    public void createSkillExperienceStatistic_hasCorrectProperties() {
        NamespacedKey skillKey = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "mining");
        Statistic stat = McRPGStatistic.createSkillExperienceStatistic(skillKey, "Mining");

        assertEquals("mining_experience", stat.getStatisticKey().getKey());
        assertEquals(StatisticType.LONG, stat.getStatisticType());
        assertEquals(0L, stat.getDefaultValue());
        assertEquals("Mining Experience", stat.getDisplayName());
        assertEquals("Total Mining XP earned", stat.getDescription());
    }

    @DisplayName("Given a skill key and display name, when creating a skill max level statistic, then the statistic has correct properties")
    @Test
    public void createSkillMaxLevelStatistic_hasCorrectProperties() {
        NamespacedKey skillKey = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "mining");
        Statistic stat = McRPGStatistic.createSkillMaxLevelStatistic(skillKey, "Mining");

        assertEquals("mining_max_level", stat.getStatisticKey().getKey());
        assertEquals(StatisticType.INT, stat.getStatisticType());
        assertEquals(0, stat.getDefaultValue());
        assertEquals("Mining Max Level", stat.getDisplayName());
        assertEquals("Highest Mining level reached", stat.getDescription());
    }

    @DisplayName("Given static statistics, when checking ALL_STATIC_STATISTICS, then it contains global stats but not per-skill stats")
    @Test
    public void allStaticStatistics_containsGlobalStats() {
        assertTrue(McRPGStatistic.ALL_STATIC_STATISTICS.contains(McRPGStatistic.BLOCKS_MINED));
        assertTrue(McRPGStatistic.ALL_STATIC_STATISTICS.contains(McRPGStatistic.MOBS_KILLED));
        assertTrue(McRPGStatistic.ALL_STATIC_STATISTICS.contains(McRPGStatistic.DAMAGE_DEALT));
        assertTrue(McRPGStatistic.ALL_STATIC_STATISTICS.contains(McRPGStatistic.DAMAGE_TAKEN));
        assertTrue(McRPGStatistic.ALL_STATIC_STATISTICS.contains(McRPGStatistic.TOTAL_SKILL_EXPERIENCE));
        assertTrue(McRPGStatistic.ALL_STATIC_STATISTICS.contains(McRPGStatistic.TOTAL_SKILL_LEVELS_GAINED));
        assertTrue(McRPGStatistic.ALL_STATIC_STATISTICS.contains(McRPGStatistic.ABILITIES_ACTIVATED));
    }

    @DisplayName("Given ALL_STATIC_STATISTICS, when checking contents, then it is not empty and is unmodifiable")
    @Test
    public void allStaticStatistics_isNotEmptyAndUnmodifiable() {
        assertFalse(McRPGStatistic.ALL_STATIC_STATISTICS.isEmpty());
        assertNotNull(McRPGStatistic.ALL_STATIC_STATISTICS);
        assertThrows(UnsupportedOperationException.class, () -> McRPGStatistic.ALL_STATIC_STATISTICS.add(McRPGStatistic.BLOCKS_MINED));
    }

    @DisplayName("Given DAMAGE_DEALT, when checking type, then it is DOUBLE")
    @Test
    public void damageDealt_isDoubleType() {
        assertEquals(StatisticType.DOUBLE, McRPGStatistic.DAMAGE_DEALT.getStatisticType());
        assertEquals(0.0, McRPGStatistic.DAMAGE_DEALT.getDefaultValue());
    }

    @DisplayName("Given BLOCKS_MINED, when checking type, then it is LONG")
    @Test
    public void blocksMined_isLongType() {
        assertEquals(StatisticType.LONG, McRPGStatistic.BLOCKS_MINED.getStatisticType());
        assertEquals(0L, McRPGStatistic.BLOCKS_MINED.getDefaultValue());
    }
}
