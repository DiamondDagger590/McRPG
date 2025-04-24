package us.eunoians.mcrpg.external.papi;

import com.diamonddagger590.mccore.external.papi.PapiHook;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;

public final class McRPGPapiHook extends PapiHook<McRPG> {

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
