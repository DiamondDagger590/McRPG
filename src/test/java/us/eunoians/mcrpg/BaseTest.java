package us.eunoians.mcrpg;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import org.junit.After;
import org.junit.Before;

/**
 * Base test implementation that makes writing unit tests for {@link McRPG} a lot faster!
 *
 * @author OxKitsune
 */
public class BaseTest {

    /**
     * Mocked server instance
     */
    private ServerMock server;

    /**
     * Mocked McRPG instance
     */
    private McRPG plugin;

    @Before
    public void setUp() {
        this.server = MockBukkit.mock();
        this.plugin = MockBukkit.load(McRPG.class);
    }


    @After
    public void tearDown() {
        MockBukkit.unmock();
    }

    /**
     * Get the {@link MockBukkit} server instance.
     *
     * @return the mock bukkit server instance
     */
    public ServerMock getServer() {
        return server;
    }

    /**
     * Get the {@link McRPG} plugin instance.
     *
     * @return the {@link McRPG} plugin instance.
     */
    public McRPG getPlugin() {
        return plugin;
    }
}
