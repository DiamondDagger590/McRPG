package us.eunoians.mcrpg.external.papi;

import com.diamonddagger590.mccore.external.papi.CorePapiHook;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;

/**
 * A hook for containing all code related to <a href="https://www.spigotmc.org/resources/placeholderapi.6245/">PlaceholderAPI</a>
 * that this plugin needs to support it.
 */
public final class McRPGPapiHook extends CorePapiHook {

    private final McRPGPapiExpansion mcRPGPapiExpansion;

    public McRPGPapiHook(@NotNull McRPG plugin) {
        super(plugin);
        this.mcRPGPapiExpansion = new McRPGPapiExpansion(plugin);
    }

    @NotNull
    public McRPGPapiExpansion getExpansion() {
        return mcRPGPapiExpansion;
    }
}
