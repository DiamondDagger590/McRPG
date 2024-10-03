package us.eunoians.mcrpg.task;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerJoinEvent;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.database.table.SkillDAO;
import us.eunoians.mcrpg.database.table.SkillDataSnapshot;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.listener.entity.player.PlayerJoinListener;
import us.eunoians.mcrpg.util.TestUtils;

import java.sql.Connection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

public class PlayerLoadTaskTest {

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
        McRPGPlayerLoadTask mcRPGPlayerLoadTask = new McRPGPlayerLoadTask(plugin, mcRPGPlayer);
        assertEquals(mcRPGPlayer, mcRPGPlayerLoadTask.getCorePlayer());
        playerMock.disconnect();
    }

    @Test
    void testGetPlugin() {
        McRPGPlayerLoadTask mcRPGPlayerLoadTask = new McRPGPlayerLoadTask(plugin, mock(McRPGPlayer.class));
        assertEquals(plugin, mcRPGPlayerLoadTask.getPlugin());
    }

    @Test
    void testLoadPlayerSuccess() {
        // We want to create a spy of McRPG to mock the database connection
        McRPG spy = spy(plugin);
        TestUtils.mockDatabaseConnection(spy);

        // Mock the listener to use our special database stuff
        PlayerJoinListener mockListener = mock(PlayerJoinListener.class);
        doAnswer(answer -> {
            McRPG mcRPG = McRPG.getInstance();
            Player player = answer.getArgument(0, PlayerJoinEvent.class).getPlayer();
            McRPGPlayer mcRPGPlayer = new McRPGPlayer(player);
            McRPGPlayerLoadTask loadTask = new McRPGPlayerLoadTask(spy, mcRPGPlayer);
            /*
            We are manually mocking the results of what should happen by calling #runTask on the loadTask.

            It was throwing a weird fit and runnables were getting funky... maybe because it's an expireable and I use
            a timing system outside of Bukkit's tick system? Which could cause the task to not be called...

            I tried to spy it and set the max runtime for the task to the max long, but then it still was failing on certain assertions
            so I guess this is more stable? *shrug*
             */
            assertTrue(loadTask.loadPlayer().get());
            loadTask.onPlayerLoadSuccessfully();
            return null;
        }).when(mockListener).handleJoin(any(PlayerJoinEvent.class));
        Bukkit.getPluginManager().registerEvents(mockListener, spy);

        // Mock the SkillDAO
        try (MockedStatic<SkillDAO> skillDAOMockedStatic = Mockito.mockStatic(SkillDAO.class)) {
            skillDAOMockedStatic.when(() -> {
                        SkillDAO.getAllPlayerSkillInformation(any(Connection.class), any(UUID.class), any(NamespacedKey.class));
                    })
                    .thenAnswer(answer -> {
                        CompletableFuture<SkillDataSnapshot> future = new CompletableFuture<>();
                        future.complete(new SkillDataSnapshot(answer.getArgument(1, UUID.class), answer.getArgument(2, NamespacedKey.class), 10, 15));
                        return future;
                    });
            // Mock a player joining (things are all handled inside the player join listener)
            PlayerMock playerMock = serverMock.addPlayer();
            // Assert player data loaded
            assertTrue(spy.getPlayerManager().getPlayer(playerMock.getUniqueId()).isPresent());
            assertTrue(spy.getEntityManager().getAbilityHolder(playerMock.getUniqueId()).isPresent());
            // Assert skills were loaded
            SkillHolder skillHolder = (SkillHolder) spy.getEntityManager().getAbilityHolder(playerMock.getUniqueId()).get();
            assertNotEquals(spy.getSkillRegistry().getRegisteredSkills().size(), 0);
            assertEquals(skillHolder.getSkills().size(), spy.getSkillRegistry().getRegisteredSkills().size());
            playerMock.disconnect();
        }
        HandlerList.unregisterAll(mockListener);
    }
}
