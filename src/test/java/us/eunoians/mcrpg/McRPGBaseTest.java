package us.eunoians.mcrpg;

import com.diamonddagger590.mccore.bootstrap.StartupProfile;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockito.MockedStatic;
import us.eunoians.mcrpg.bootstrap.BootstrapFactory;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;

import static com.diamonddagger590.mccore.testing.RegistryResetExtension.resetRegistry;
import static com.diamonddagger590.mccore.testing.RegistryResetExtension.setupRegistry;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;

/**
 * A base test that provides some utility methods as well as
 * integration with {@link org.mockbukkit.mockbukkit.MockBukkit}.
 * <p>
 * By extending this class, you should have access to the various mocking functionality
 * provided by MockBukkit.
 */
public class McRPGBaseTest {

    protected ServerMock server;
    protected McRPG mcRPG;

    public McRPGBaseTest() {
        StartupProfile.TEST.setSystemProperty();
        server = MockBukkit.getOrCreateMock();
        if (server.getWorld("world") == null) {
            server.addSimpleWorld("world");
        }
        resetRegistry();
        setupRegistry();
        try (MockedStatic<BootstrapFactory> staticBootstrapFactory = mockStatic(BootstrapFactory.class)) {
            staticBootstrapFactory.when(BootstrapFactory::getBootstrap).thenReturn(new TestBootstrap());
            mcRPG = MockBukkit.load(McRPG.class);
        }
    }

    /**
     * Tears the mock server down after every test.
     * <p>
     * The constructor loads a fresh {@link McRPG} instance into the server for each test, and
     * {@link MockBukkit#getOrCreateMock()} hands back one process-wide server. Without this
     * teardown every loaded plugin, its registered listeners, and its scheduled tasks stay
     * reachable for the lifetime of the JVM — the suite's retained heap then grows with the test
     * count until the worker dies with an {@link OutOfMemoryError}, and work outliving one test
     * (scheduler tasks, handler-list listeners) executes inside a later, unrelated one.
     * <p>
     * {@link MockBukkit#unmock()} disables the loaded plugins, shuts the scheduler down and unloads
     * the plugin manager, so the next constructor call starts from a clean server. It no-ops when
     * nothing is mocked, so classes that manage their own server lifecycle are unaffected.
     */
    @AfterEach
    public void tearDownMockServer() {
        MockBukkit.unmock();
    }

    @NotNull
    public PlayerMock addPlayerToServer(@NotNull McRPGPlayer mcRPGPlayer) {
        PlayerMock playerMock = new PlayerMock(server, "Verum", mcRPGPlayer.getUUID());
        server.addPlayer(playerMock);
        return playerMock;
    }

    @NotNull
    public <T extends Entity> T spawnEntity(Class<T> entityClass) {
        World world = server.getWorld("world");
        return spy(world.spawn(new Location(world, 0, 0, 0), entityClass));
    }
}
