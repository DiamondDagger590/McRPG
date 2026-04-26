package us.eunoians.mcrpg.gui.quest.slot;

import org.bukkit.event.inventory.ClickType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.entity.player.McRPGPlayerExtension;
import us.eunoians.mcrpg.gui.quest.QuestDetailGui;
import us.eunoians.mcrpg.quest.QuestTestHelper;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.definition.QuestObjectiveDefinition;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(McRPGPlayerExtension.class)
class QuestDetailGuiSlotsTest extends McRPGBaseTest {

    @Test
    @DisplayName("getValidGuiTypes returns QuestDetailGui for an objective slot")
    void getValidGuiTypes_returnsQuestDetailGui_whenObjectiveSlot(McRPGPlayer player) {
        QuestDefinition def = QuestTestHelper.singlePhaseQuest("detail_obj_quest");
        QuestObjectiveDefinition obj = def.getPhases().getFirst().getStages().getFirst().getObjectives().getFirst();
        var slot = new QuestDetailObjectiveSlot(def.getQuestKey(), obj, null);
        assertEquals(Set.of(QuestDetailGui.class), slot.getValidGuiTypes());
    }

    @Test
    @DisplayName("onClick returns true for an objective slot")
    void onClick_returnsTrue_whenObjectiveSlot(McRPGPlayer player) {
        QuestDefinition def = QuestTestHelper.singlePhaseQuest("detail_obj_click");
        QuestObjectiveDefinition obj = def.getPhases().getFirst().getStages().getFirst().getObjectives().getFirst();
        assertTrue(new QuestDetailObjectiveSlot(def.getQuestKey(), obj, null)
                .onClick(player, ClickType.LEFT));
    }

    @Test
    @DisplayName("getValidGuiTypes returns QuestDetailGui for an overview slot")
    void getValidGuiTypes_returnsQuestDetailGui_whenOverviewSlot(McRPGPlayer player) {
        QuestDefinition def = QuestTestHelper.singlePhaseQuest("detail_over_quest");
        var slot = new QuestDetailOverviewSlot(def.getQuestKey(), null, null, def);
        assertEquals(Set.of(QuestDetailGui.class), slot.getValidGuiTypes());
    }

    @Test
    @DisplayName("getValidGuiTypes returns QuestDetailGui for a reward slot")
    void getValidGuiTypes_returnsQuestDetailGui_whenRewardSlot(McRPGPlayer player) {
        QuestDefinition def = QuestTestHelper.singlePhaseQuest("detail_reward_quest");
        var slot = new QuestDetailRewardSlot(def);
        assertEquals(Set.of(QuestDetailGui.class), slot.getValidGuiTypes());
    }

}
