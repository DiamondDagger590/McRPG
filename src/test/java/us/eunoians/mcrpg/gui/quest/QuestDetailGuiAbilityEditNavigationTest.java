package us.eunoians.mcrpg.gui.quest;

import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.ability.StubTierableAbility;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.entity.player.McRPGPlayerExtension;
import us.eunoians.mcrpg.quest.QuestTestHelper;
import us.eunoians.mcrpg.quest.board.BoardOffering;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.impl.QuestInstance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * Tests navigation routing for {@link QuestDetailGui} when opened from the Ability Edit GUI.
 */
@ExtendWith(McRPGPlayerExtension.class)
class QuestDetailGuiAbilityEditNavigationTest extends McRPGBaseTest {

    private StubTierableAbility ability;
    private QuestDefinition definition;
    private QuestInstance questInstance;

    @BeforeEach
    void setup() {
        ability = new StubTierableAbility(mcRPG, new NamespacedKey("mcrpg", "nav_test_ability"));
        definition = QuestTestHelper.singlePhaseQuest("nav_upgrade_quest");
        questInstance = QuestTestHelper.startedQuestInstance(definition);
    }

    @Test
    @DisplayName("forUpgradeQuest returns route QUEST_DETAIL_GUI_PREVIOUS_FROM_ABILITY_EDIT_BUTTON_DISPLAY_ITEM")
    void getPreviousGuiSlot_fromAbilityEdit_returnsAbilityEditRoute(McRPGPlayer player) {
        addPlayerToServer(player);
        QuestDetailGui gui = QuestDetailGui.forUpgradeQuest(player, questInstance, ability);
        Route route = gui.getPreviousGuiSlot().getSpecificDisplayItemRoute();
        assertEquals(LocalizationKey.QUEST_DETAIL_GUI_PREVIOUS_FROM_ABILITY_EDIT_BUTTON_DISPLAY_ITEM, route);
    }

    @Test
    @DisplayName("forActiveQuest returns route QUEST_DETAIL_GUI_PREVIOUS_FROM_ACTIVE_BUTTON_DISPLAY_ITEM")
    void getPreviousGuiSlot_forActiveQuest_returnsActiveRoute(McRPGPlayer player) {
        addPlayerToServer(player);
        QuestDetailGui gui = QuestDetailGui.forActiveQuest(player, questInstance);
        Route route = gui.getPreviousGuiSlot().getSpecificDisplayItemRoute();
        assertEquals(LocalizationKey.QUEST_DETAIL_GUI_PREVIOUS_FROM_ACTIVE_BUTTON_DISPLAY_ITEM, route);
    }

    @Test
    @DisplayName("forBoardPreview returns route QUEST_DETAIL_GUI_PREVIOUS_FROM_BOARD_BUTTON_DISPLAY_ITEM")
    void getPreviousGuiSlot_forBoardPreview_returnsBoardRoute(McRPGPlayer player) {
        addPlayerToServer(player);
        var offering = mock(BoardOffering.class);
        QuestDetailGui gui = QuestDetailGui.forBoardPreview(player, definition, offering);
        Route route = gui.getPreviousGuiSlot().getSpecificDisplayItemRoute();
        assertEquals(LocalizationKey.QUEST_DETAIL_GUI_PREVIOUS_FROM_BOARD_BUTTON_DISPLAY_ITEM, route);
    }
}
