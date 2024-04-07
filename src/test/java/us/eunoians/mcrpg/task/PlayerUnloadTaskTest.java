package us.eunoians.mcrpg.task;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import com.diamonddagger590.mccore.player.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerQuitEvent;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.database.table.SkillDAO;
import us.eunoians.mcrpg.database.table.SkillDataSnapshot;
import us.eunoians.mcrpg.entity.AbilityHolderTracker;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.listener.player.PlayerLeaveListener;
import us.eunoians.mcrpg.util.TestUtils;

import java.sql.Connection;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

public class PlayerUnloadTaskTest {

    private static ServerMock serverMock;
    private static McRPG plugin;

    @BeforeAll
    public static void load() {
        serverMock = MockBukkit.mock();
        plugin = MockBukkit.load(McRPG.class);
    }

    @AfterAll
    public static void unload() {
        MockBukkit.unmock();
    }

    @Test
    void testGetPlayer() {
        PlayerMock playerMock = serverMock.addPlayer();
        McRPGPlayer mcRPGPlayer = new McRPGPlayer(playerMock.getUniqueId());
        McRPGPlayerUnloadTask unloadTask = new McRPGPlayerUnloadTask(plugin, mcRPGPlayer);
        assertEquals(mcRPGPlayer, unloadTask.getCorePlayer());
        playerMock.disconnect();
    }

    @Test
    void testGetPlugin() {
        McRPGPlayerUnloadTask unloadTask = new McRPGPlayerUnloadTask(plugin, mock(McRPGPlayer.class));
        assertEquals(plugin, unloadTask.getPlugin());
    }

    @Test
    void testPlayerUnloadSuccess() {
        // We want to create a spy of McRPG to mock the database connection
        McRPG spy = spy(plugin);
        TestUtils.mockDatabaseConnection(spy);

        // Mock the listener to manually call unload task
        PlayerLeaveListener mockListener = mock(PlayerLeaveListener.class);
        doAnswer(answer -> {
            McRPG mcRPG = McRPG.getInstance();
            Player player = answer.getArgument(0, PlayerQuitEvent.class).getPlayer();
            McRPGPlayer mcRPGPlayer = new McRPGPlayer(player);
            McRPGPlayerUnloadTask unloadTask = new McRPGPlayerUnloadTask(spy, mcRPGPlayer);
            assertTrue(unloadTask.unloadPlayer());
            return null;
        }).when(mockListener).handleQuit(any(PlayerQuitEvent.class));
        Bukkit.getPluginManager().registerEvents(mockListener, spy);

        // Setup the player as being loaded (tested elsewhere)
        PlayerMock playerMock = serverMock.addPlayer();
        PlayerManager playerManager = spy.getPlayerManager();
        AbilityHolderTracker tracker = spy.getEntityManager();
        McRPGPlayer player = new McRPGPlayer(playerMock);
        playerManager.addPlayer(player);
        tracker.trackAbilityHolder(player.asSkillHolder());
        // Assert player data is there
        assertTrue(playerManager.getPlayer(player.getUUID()).isPresent());
        assertTrue(tracker.getAbilityHolder(player.getUUID()).isPresent());
        // Disconnect player
        // Mock the SkillDAO
        try (MockedStatic<SkillDAO> skillDAOMockedStatic = Mockito.mockStatic(SkillDAO.class)) {
            skillDAOMockedStatic.when(() -> {
                        SkillDAO.saveAllSkillHolderInformation(any(Connection.class), any(SkillHolder.class));
                    })
                    .thenAnswer(answer -> {
                        CompletableFuture<SkillDataSnapshot> future = new CompletableFuture<>();
                        future.complete(null);
                        return future;
                    });
            playerMock.disconnect();
        }
        assertFalse(playerManager.getPlayer(player.getUUID()).isPresent());
        assertFalse(tracker.getAbilityHolder(player.getUUID()).isPresent());
        HandlerList.unregisterAll(mockListener);
    }
}
