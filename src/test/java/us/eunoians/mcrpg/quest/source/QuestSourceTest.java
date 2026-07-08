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
import us.eunoians.mcrpg.quest.source.builtin.TutorialQuestSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class QuestSourceTest extends McRPGBaseTest {

    @Test
    @DisplayName("BoardPersonalQuestSource getKey returns mcrpg:board_personal")
    void boardPersonal_getKey() {
        var source = new BoardPersonalQuestSource();
        assertEquals(new NamespacedKey("mcrpg", "board_personal"), source.getKey());
    }

    @Test
    @DisplayName("BoardPersonalQuestSource isAbandonable returns true")
    void boardPersonal_isAbandonable() {
        var source = new BoardPersonalQuestSource();
        assertTrue(source.isAbandonable());
    }

    @Test
    @DisplayName("BoardPersonalQuestSource getExpansionKey returns McRPG expansion key")
    void boardPersonal_getExpansionKey() {
        var source = new BoardPersonalQuestSource();
        assertTrue(source.getExpansionKey().isPresent());
        assertEquals(McRPGExpansion.EXPANSION_KEY, source.getExpansionKey().get());
    }

    @Test
    @DisplayName("BoardLandQuestSource getKey returns mcrpg:board_land")
    void boardLand_getKey() {
        var source = new BoardLandQuestSource();
        assertEquals(new NamespacedKey("mcrpg", "board_land"), source.getKey());
    }

    @Test
    @DisplayName("BoardLandQuestSource isAbandonable returns true")
    void boardLand_isAbandonable() {
        var source = new BoardLandQuestSource();
        assertTrue(source.isAbandonable());
    }

    @Test
    @DisplayName("BoardLandQuestSource getExpansionKey returns McRPG expansion key")
    void boardLand_getExpansionKey() {
        var source = new BoardLandQuestSource();
        assertTrue(source.getExpansionKey().isPresent());
        assertEquals(McRPGExpansion.EXPANSION_KEY, source.getExpansionKey().get());
    }

    @Test
    @DisplayName("AbilityUpgradeQuestSource getKey returns mcrpg:ability_upgrade")
    void abilityUpgrade_getKey() {
        var source = new AbilityUpgradeQuestSource();
        assertEquals(new NamespacedKey("mcrpg", "ability_upgrade"), source.getKey());
    }

    @Test
    @DisplayName("AbilityUpgradeQuestSource isAbandonable returns false")
    void abilityUpgrade_isAbandonable() {
        var source = new AbilityUpgradeQuestSource();
        assertFalse(source.isAbandonable());
    }

    @Test
    @DisplayName("AbilityUpgradeQuestSource getExpansionKey returns McRPG expansion key")
    void abilityUpgrade_getExpansionKey() {
        var source = new AbilityUpgradeQuestSource();
        assertTrue(source.getExpansionKey().isPresent());
        assertEquals(McRPGExpansion.EXPANSION_KEY, source.getExpansionKey().get());
    }

    @Test
    @DisplayName("ManualQuestSource getKey returns mcrpg:manual")
    void manual_getKey() {
        var source = new ManualQuestSource();
        assertEquals(new NamespacedKey("mcrpg", "manual"), source.getKey());
    }

    @Test
    @DisplayName("ManualQuestSource isAbandonable returns false")
    void manual_isAbandonable() {
        var source = new ManualQuestSource();
        assertFalse(source.isAbandonable());
    }

    @Test
    @DisplayName("ManualQuestSource getExpansionKey returns McRPG expansion key")
    void manual_getExpansionKey() {
        var source = new ManualQuestSource();
        assertTrue(source.getExpansionKey().isPresent());
        assertEquals(McRPGExpansion.EXPANSION_KEY, source.getExpansionKey().get());
    }

    @Test
    @DisplayName("TutorialQuestSource getKey returns mcrpg:tutorial")
    void tutorial_getKey() {
        var source = new TutorialQuestSource();
        assertEquals(new NamespacedKey("mcrpg", "tutorial"), source.getKey());
    }

    @Test
    @DisplayName("TutorialQuestSource isAbandonable returns false")
    void tutorial_isAbandonable() {
        var source = new TutorialQuestSource();
        assertFalse(source.isAbandonable());
    }

    @Test
    @DisplayName("TutorialQuestSource getExpansionKey returns McRPG expansion key")
    void tutorial_getExpansionKey() {
        var source = new TutorialQuestSource();
        assertTrue(source.getExpansionKey().isPresent());
        assertEquals(McRPGExpansion.EXPANSION_KEY, source.getExpansionKey().get());
    }
}
