package us.eunoians.mcrpg.quest.source;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.quest.source.builtin.AbilityUpgradeQuestSource;
import us.eunoians.mcrpg.quest.source.builtin.BoardLandQuestSource;
import us.eunoians.mcrpg.quest.source.builtin.BoardPersonalQuestSource;
import us.eunoians.mcrpg.quest.source.builtin.ManualQuestSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class QuestSourceTest extends McRPGBaseTest {

    @DisplayName("BoardPersonalQuestSource getKey returns mcrpg:board_personal")
    @Test
    void boardPersonal_getKey() {
        var source = new BoardPersonalQuestSource();
        assertEquals(new NamespacedKey("mcrpg", "board_personal"), source.getKey());
    }

    @DisplayName("BoardPersonalQuestSource isAbandonable returns true")
    @Test
    void boardPersonal_isAbandonable() {
        var source = new BoardPersonalQuestSource();
        assertTrue(source.isAbandonable());
    }

    @DisplayName("BoardPersonalQuestSource getExpansionKey returns McRPG expansion key")
    @Test
    void boardPersonal_getExpansionKey() {
        var source = new BoardPersonalQuestSource();
        assertTrue(source.getExpansionKey().isPresent());
        assertEquals(McRPGExpansion.EXPANSION_KEY, source.getExpansionKey().get());
    }

    @DisplayName("BoardLandQuestSource getKey returns mcrpg:board_land")
    @Test
    void boardLand_getKey() {
        var source = new BoardLandQuestSource();
        assertEquals(new NamespacedKey("mcrpg", "board_land"), source.getKey());
    }

    @DisplayName("BoardLandQuestSource isAbandonable returns true")
    @Test
    void boardLand_isAbandonable() {
        var source = new BoardLandQuestSource();
        assertTrue(source.isAbandonable());
    }

    @DisplayName("BoardLandQuestSource getExpansionKey returns McRPG expansion key")
    @Test
    void boardLand_getExpansionKey() {
        var source = new BoardLandQuestSource();
        assertTrue(source.getExpansionKey().isPresent());
        assertEquals(McRPGExpansion.EXPANSION_KEY, source.getExpansionKey().get());
    }

    @DisplayName("AbilityUpgradeQuestSource getKey returns mcrpg:ability_upgrade")
    @Test
    void abilityUpgrade_getKey() {
        var source = new AbilityUpgradeQuestSource();
        assertEquals(new NamespacedKey("mcrpg", "ability_upgrade"), source.getKey());
    }

    @DisplayName("AbilityUpgradeQuestSource isAbandonable returns false")
    @Test
    void abilityUpgrade_isAbandonable() {
        var source = new AbilityUpgradeQuestSource();
        assertFalse(source.isAbandonable());
    }

    @DisplayName("AbilityUpgradeQuestSource getExpansionKey returns McRPG expansion key")
    @Test
    void abilityUpgrade_getExpansionKey() {
        var source = new AbilityUpgradeQuestSource();
        assertTrue(source.getExpansionKey().isPresent());
        assertEquals(McRPGExpansion.EXPANSION_KEY, source.getExpansionKey().get());
    }

    @DisplayName("ManualQuestSource getKey returns mcrpg:manual")
    @Test
    void manual_getKey() {
        var source = new ManualQuestSource();
        assertEquals(new NamespacedKey("mcrpg", "manual"), source.getKey());
    }

    @DisplayName("ManualQuestSource isAbandonable returns false")
    @Test
    void manual_isAbandonable() {
        var source = new ManualQuestSource();
        assertFalse(source.isAbandonable());
    }

    @DisplayName("ManualQuestSource getExpansionKey returns McRPG expansion key")
    @Test
    void manual_getExpansionKey() {
        var source = new ManualQuestSource();
        assertTrue(source.getExpansionKey().isPresent());
        assertEquals(McRPGExpansion.EXPANSION_KEY, source.getExpansionKey().get());
    }
}
