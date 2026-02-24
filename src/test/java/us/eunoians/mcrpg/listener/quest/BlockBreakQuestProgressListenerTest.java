package us.eunoians.mcrpg.listener.quest;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.World;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockBreakEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BlockBreakQuestProgressListenerTest extends McRPGBaseTest {

    private QuestManager mockQuestManager;
    private World world;

    @BeforeEach
    public void setup() {
        HandlerList.unregisterAll(mcRPG);
        server.getPluginManager().clearEvents();
        mockQuestManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.QUEST);
        when(mockQuestManager.getActiveQuestsForPlayer(any(UUID.class))).thenReturn(List.of());
        server.getPluginManager().registerEvents(new BlockBreakQuestProgressListener(), mcRPG);
        world = server.addSimpleWorld("block_break_test");
    }

    @DisplayName("Given a block break event, when fired, then progressQuests queries active quests for the player")
    @Test
    public void onBlockBreak_callsProgressQuests() {
        PlayerMock player = new PlayerMock(server, "Miner");
        server.addPlayer(player);

        BlockBreakEvent event = new BlockBreakEvent(world.getBlockAt(0, 64, 0), player);
        server.getPluginManager().callEvent(event);

        verify(mockQuestManager).getActiveQuestsForPlayer(player.getUniqueId());
    }

    @DisplayName("Given a cancelled block break event, when fired, then progressQuests is not invoked")
    @Test
    public void onBlockBreak_cancelledEvent_doesNotProgress() {
        PlayerMock player = new PlayerMock(server, "CancelMiner");
        server.addPlayer(player);

        BlockBreakEvent event = new BlockBreakEvent(world.getBlockAt(0, 64, 0), player);
        event.setCancelled(true);
        server.getPluginManager().callEvent(event);

        verify(mockQuestManager, never()).getActiveQuestsForPlayer(any(UUID.class));
    }
}
