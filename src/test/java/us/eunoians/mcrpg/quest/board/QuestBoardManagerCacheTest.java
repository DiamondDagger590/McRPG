package us.eunoians.mcrpg.quest.board;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class QuestBoardManagerCacheTest extends McRPGBaseTest {

    private QuestBoardManager boardManager;

    @BeforeEach
    public void setup() {
        boardManager = new QuestBoardManager(McRPG.getInstance());
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(boardManager);
    }

    @DisplayName("Cache miss for unknown board key returns empty list instead of NPE")
    @Test
    public void getSharedOfferings_cacheMiss_returnsEmptyList() {
        NamespacedKey unknownKey = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "nonexistent_board");
        List<BoardOffering> offerings = boardManager.getSharedOfferingsForBoard(unknownKey);
        assertNotNull(offerings);
        assertTrue(offerings.isEmpty());
    }

    @DisplayName("Cache miss for default board before initialization returns empty list")
    @Test
    public void getSharedOfferings_defaultBoard_beforeWarm_returnsEmptyOrCached() {
        NamespacedKey defaultKey = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "default_board");
        List<BoardOffering> offerings = boardManager.getSharedOfferingsForBoard(defaultKey);
        assertNotNull(offerings);
        assertTrue(offerings.isEmpty());
    }
}
