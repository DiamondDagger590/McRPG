package us.eunoians.mcrpg.bootstrap;

import com.diamonddagger590.mccore.bootstrap.CoreBootstrap;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;

/**
 * This bootstrap factory provides the main {@link CoreBootstrap}
 * used by McRPG for plugin initialization. By providing it via a factory,
 * unit tests can overwrite this method to return a custom bootstrap for McRPG
 * to use for initialization.
 */
public final class BootstrapFactory {

    /**
     * Gets the {@link CoreBootstrap} being used to initialize McRPG.
     *
     * @return The {@link CoreBootstrap} being used to initialize McRPG.
     */
    @NotNull
    public static CoreBootstrap<McRPG> getBootstrap() {
        return new McRPGBootstrap(McRPG.getInstance());
    }
}
