package us.eunoians.mcrpg;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.bootstrap.CoreBootstrap;
import com.diamonddagger590.mccore.util.TimeProvider;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.bootstrap.BootstrapFactory;
import us.eunoians.mcrpg.database.McRPGDatabase;

/**
 * The main class for McRPG where developers should be able to access various components of the API's provided by McRPG
 */
public class McRPG extends CorePlugin {

    private static final int id = 6386;

    private McRPGDatabase database;
    private TimeProvider timeProvider;

    @Override
    public void onEnable() {
        super.onEnable();
        CoreBootstrap<?> bootstrap = BootstrapFactory.getBootstrap();
        bootstrap.start(resolveProfile());
        timeProvider = bootstrap.getTimeProvider();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @NotNull
    @Override
    public TimeProvider getTimeProvider() {
        return timeProvider;
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
