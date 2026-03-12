package us.eunoians.mcrpg.listener.quest;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.event.HandlerList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.entity.player.McRPGPlayerExtension;
import us.eunoians.mcrpg.event.quest.QuestCancelEvent;
import us.eunoians.mcrpg.event.quest.QuestCompleteEvent;
import us.eunoians.mcrpg.quest.QuestTestHelper;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.quest.impl.scope.impl.SinglePlayerQuestScope;
import us.eunoians.mcrpg.quest.source.builtin.BoardPersonalQuestSource;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(McRPGPlayerExtension.class)
public class QuestListenerBoardCountTest extends McRPGBaseTest {

    @BeforeEach
    public void setup() {
        HandlerList.unregisterAll(mcRPG);
        server.getPluginManager().clearEvents();
        server.getPluginManager().registerEvents(new QuestCancelListener(), mcRPG);
        server.getPluginManager().registerEvents(new QuestCompleteListener(), mcRPG);
    }

    @DisplayName("QuestCancelListener decrements board count for board-sourced quest")
    @Test
    public void cancelListener_decrementsBoardCount_forBoardQuest(McRPGPlayer mcRPGPlayer) {
        mcRPGPlayer.asQuestHolder().setActiveBoardQuestCount(2);

        QuestDefinition def = QuestTestHelper.singlePhaseQuest("cancel_board_count_test");
        QuestInstance quest = createBoardQuestWithPlayer(def, mcRPGPlayer);

        server.getPluginManager().callEvent(new QuestCancelEvent(quest));

        assertEquals(1, mcRPGPlayer.asQuestHolder().getActiveBoardQuestCount());
    }

    @DisplayName("QuestCancelListener does not decrement board count for non-board quest")
    @Test
    public void cancelListener_skipsDecrement_forNonBoardQuest(McRPGPlayer mcRPGPlayer) {
        mcRPGPlayer.asQuestHolder().setActiveBoardQuestCount(2);

        QuestDefinition def = QuestTestHelper.singlePhaseQuest("cancel_manual_count_test");
        QuestInstance quest = QuestTestHelper.startedQuestWithPlayer(def, mcRPGPlayer.getUUID());

        server.getPluginManager().callEvent(new QuestCancelEvent(quest));

        assertEquals(2, mcRPGPlayer.asQuestHolder().getActiveBoardQuestCount());
    }

    @DisplayName("QuestCompleteListener decrements board count for board-sourced quest")
    @Test
    public void completeListener_decrementsBoardCount_forBoardQuest(McRPGPlayer mcRPGPlayer) {
        mcRPGPlayer.asQuestHolder().setActiveBoardQuestCount(3);

        QuestDefinition def = QuestTestHelper.singlePhaseQuest("complete_board_count_test");
        QuestInstance quest = createBoardQuestWithPlayer(def, mcRPGPlayer);

        server.getPluginManager().callEvent(new QuestCompleteEvent(quest, def));

        assertEquals(2, mcRPGPlayer.asQuestHolder().getActiveBoardQuestCount());
    }

    @DisplayName("QuestCompleteListener does not decrement board count for non-board quest")
    @Test
    public void completeListener_skipsDecrement_forNonBoardQuest(McRPGPlayer mcRPGPlayer) {
        mcRPGPlayer.asQuestHolder().setActiveBoardQuestCount(3);

        QuestDefinition def = QuestTestHelper.singlePhaseQuest("complete_manual_count_test");
        QuestInstance quest = QuestTestHelper.startedQuestWithPlayer(def, mcRPGPlayer.getUUID());

        server.getPluginManager().callEvent(new QuestCompleteEvent(quest, def));

        assertEquals(3, mcRPGPlayer.asQuestHolder().getActiveBoardQuestCount());
    }

    @DisplayName("Decrement at zero stays at zero")
    @Test
    public void cancelListener_decrement_atZero_staysZero(McRPGPlayer mcRPGPlayer) {
        mcRPGPlayer.asQuestHolder().setActiveBoardQuestCount(0);

        QuestDefinition def = QuestTestHelper.singlePhaseQuest("cancel_zero_bound_test");
        QuestInstance quest = createBoardQuestWithPlayer(def, mcRPGPlayer);

        server.getPluginManager().callEvent(new QuestCancelEvent(quest));

        assertEquals(0, mcRPGPlayer.asQuestHolder().getActiveBoardQuestCount());
    }

    private QuestInstance createBoardQuestWithPlayer(QuestDefinition def, McRPGPlayer mcRPGPlayer) {
        QuestInstance instance = new QuestInstance(def, null, Map.of(), new BoardPersonalQuestSource(), null);
        SinglePlayerQuestScope scope = new SinglePlayerQuestScope(instance.getQuestUUID());
        scope.setPlayerInScope(mcRPGPlayer.getUUID());
        instance.setQuestScope(scope);
        instance.start(def);
        return instance;
    }
}
