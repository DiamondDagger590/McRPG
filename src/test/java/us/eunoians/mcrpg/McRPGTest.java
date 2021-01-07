package us.eunoians.mcrpg;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class McRPGTest extends BaseTest {

    @Test
    public void enabledTest () {
        Assert.assertTrue(getServer().getPluginManager().isPluginEnabled(getPlugin()));
    }
}
