package us.eunoians.mcrpg;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class McRPGTest {

    /**
     * Mocked server instance
     */
    private ServerMock server;

    /**
     * Mocked McRPG instance
     */
    private McRPG plugin;

    @Before
    public void setUp () {
        this.server = MockBukkit.mock();
        this.plugin = MockBukkit.load(McRPG.class);
    }

    @Test
    public void enabledTest () {
        Assert.assertTrue(server.getPluginManager().isPluginEnabled(plugin));
    }

    @After
    public void tearDown  () {
        MockBukkit.unload();
    }

}
