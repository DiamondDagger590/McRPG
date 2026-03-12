package us.eunoians.mcrpg.quest.board;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.entity.player.McRPGPlayerExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(McRPGPlayerExtension.class)
public class QuestBoardManagerBoardCountTest extends McRPGBaseTest {

    private QuestBoardManager boardManager;

    @BeforeEach
    public void setup() {
        boardManager = new QuestBoardManager(McRPG.getInstance());
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(boardManager);
    }

    @DisplayName("getActiveBoardQuestCount returns holder value for registered player")
    @Test
    public void getActiveBoardQuestCount_returnsHolderValue(McRPGPlayer mcRPGPlayer) {
        mcRPGPlayer.asQuestHolder().setActiveBoardQuestCount(5);
        assertEquals(5, boardManager.getActiveBoardQuestCount(mcRPGPlayer.getUUID()));
    }

    @DisplayName("getActiveBoardQuestCount returns 0 for unregistered (offline) player")
    @Test
    public void getActiveBoardQuestCount_returnsZero_forOfflinePlayer(McRPGPlayer mcRPGPlayer) {
        UUID offlineUUID = UUID.randomUUID();
        assertEquals(0, boardManager.getActiveBoardQuestCount(offlineUUID));
    }

    @DisplayName("getActiveBoardQuestCount reflects increment from holder")
    @Test
    public void getActiveBoardQuestCount_reflectsIncrement(McRPGPlayer mcRPGPlayer) {
        mcRPGPlayer.asQuestHolder().setActiveBoardQuestCount(1);
        mcRPGPlayer.asQuestHolder().incrementBoardQuestCount();
        assertEquals(2, boardManager.getActiveBoardQuestCount(mcRPGPlayer.getUUID()));
    }

    @DisplayName("getActiveBoardQuestCount reflects decrement from holder")
    @Test
    public void getActiveBoardQuestCount_reflectsDecrement(McRPGPlayer mcRPGPlayer) {
        mcRPGPlayer.asQuestHolder().setActiveBoardQuestCount(3);
        mcRPGPlayer.asQuestHolder().decrementBoardQuestCount();
        assertEquals(2, boardManager.getActiveBoardQuestCount(mcRPGPlayer.getUUID()));
    }
}
