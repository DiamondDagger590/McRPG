package us.eunoians.mcrpg.expansion.content;

import com.diamonddagger590.mccore.statistic.SimpleStatistic;
import com.diamonddagger590.mccore.statistic.Statistic;
import com.diamonddagger590.mccore.statistic.StatisticType;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.util.McRPGMethods;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StatisticContentTest extends McRPGBaseTest {

    @SuppressWarnings("deprecation")
    private static final NamespacedKey TEST_KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "test_stat");

    @SuppressWarnings("deprecation")
    private static final NamespacedKey EXPANSION_KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "test-expansion");

    @DisplayName("Given a statistic and expansion key, when wrapping in StatisticContent, then getStatistic returns the wrapped statistic")
    @Test
    public void getStatistic_returnsWrappedStatistic() {
        Statistic statistic = new SimpleStatistic(TEST_KEY, StatisticType.LONG, 0L, "Test", "A test stat");
        StatisticContent content = new StatisticContent(statistic, EXPANSION_KEY);

        assertEquals(statistic, content.getStatistic());
    }

    @DisplayName("Given a StatisticContent with an expansion key, when getting expansion key, then it is present")
    @Test
    public void getExpansionKey_returnsPresent_whenProvided() {
        Statistic statistic = new SimpleStatistic(TEST_KEY, StatisticType.LONG, 0L, "Test", "A test stat");
        StatisticContent content = new StatisticContent(statistic, EXPANSION_KEY);

        assertTrue(content.getExpansionKey().isPresent());
        assertEquals(EXPANSION_KEY, content.getExpansionKey().get());
    }

    @DisplayName("Given a StatisticContent with null expansion key, when getting expansion key, then it is empty")
    @Test
    public void getExpansionKey_returnsEmpty_whenNull() {
        Statistic statistic = new SimpleStatistic(TEST_KEY, StatisticType.LONG, 0L, "Test", "A test stat");
        StatisticContent content = new StatisticContent(statistic, null);

        assertTrue(content.getExpansionKey().isEmpty());
    }

    @DisplayName("Given a StatisticContentPack, when adding content, then content is retrievable")
    @Test
    public void statisticContentPack_addAndGetContent() {
        Statistic statistic = new SimpleStatistic(TEST_KEY, StatisticType.LONG, 0L, "Test", "A test stat");
        StatisticContent content = new StatisticContent(statistic, EXPANSION_KEY);
        StatisticContentPack pack = new StatisticContentPack(new us.eunoians.mcrpg.expansion.McRPGExpansion(mcRPG));

        pack.addContent(content);

        assertNotNull(pack.getContent());
        assertEquals(1, pack.getContent().size());
        assertEquals(content, pack.getContent().iterator().next());
    }
}
