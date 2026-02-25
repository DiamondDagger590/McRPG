package us.eunoians.mcrpg.gui.board.slot;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.gui.board.QuestBoardGui;
import us.eunoians.mcrpg.quest.board.BoardOffering;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ScopedOfferingSlotTest extends McRPGBaseTest {

    @DisplayName("getValidGuiTypes returns QuestBoardGui")
    @Test
    void getValidGuiTypes_returnsQuestBoardGui() {
        BoardOffering offering = new BoardOffering(
                UUID.randomUUID(), UUID.randomUUID(),
                new NamespacedKey("mcrpg", "land_daily"), 0,
                new NamespacedKey("mcrpg", "test_quest"),
                new NamespacedKey("mcrpg", "common"),
                "kingdom_alpha", Duration.ofHours(24));

        ScopedOfferingSlot slot = new ScopedOfferingSlot(
                offering, "kingdom_alpha",
                new NamespacedKey("mcrpg", "land_scope"),
                "Kingdom Alpha", true);

        assertEquals(Set.of(QuestBoardGui.class), slot.getValidGuiTypes());
    }

    @DisplayName("getValidGuiTypes returns QuestBoardGui for non-manager")
    @Test
    void getValidGuiTypes_nonManager_returnsQuestBoardGui() {
        BoardOffering offering = new BoardOffering(
                UUID.randomUUID(), UUID.randomUUID(),
                new NamespacedKey("mcrpg", "land_daily"), 0,
                new NamespacedKey("mcrpg", "test_quest"),
                new NamespacedKey("mcrpg", "common"),
                "kingdom_beta", Duration.ofHours(24));

        ScopedOfferingSlot slot = new ScopedOfferingSlot(
                offering, "kingdom_beta",
                new NamespacedKey("mcrpg", "land_scope"),
                "Kingdom Beta", false);

        assertEquals(Set.of(QuestBoardGui.class), slot.getValidGuiTypes());
    }
}
