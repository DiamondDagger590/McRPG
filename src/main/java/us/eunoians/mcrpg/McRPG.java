package us.eunoians.mcrpg;

import com.diamonddagger590.mccore.CorePlugin;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.bootstrap.BootstrapFactory;
import us.eunoians.mcrpg.database.McRPGDatabase;

/**
 * The main class for McRPG where developers should be able to access various components of the API's provided by McRPG
 */
public class McRPG extends CorePlugin {

    private static final int id = 6386;

    private McRPGDatabase database;

    @Override
    public void onEnable() {
        super.onEnable();
        BootstrapFactory.getBootstrap().start(resolveProfile());
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    /**
     * Gets the running instance of {@link McRPG}.
     *
     * @return The running instance of {@link McRPG}.
     */
    @NotNull
    public static McRPG getInstance() {
        return (McRPG) CorePlugin.getInstance();
    }
}
