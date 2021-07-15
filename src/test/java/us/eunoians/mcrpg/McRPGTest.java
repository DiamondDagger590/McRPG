package us.eunoians.mcrpg;

import org.junit.Assert;

public class McRPGTest extends BaseTest {

    //@Test
    public void enabledTest () {
        Assert.assertTrue(getServer().getPluginManager().isPluginEnabled(getPlugin()));
    }
}
