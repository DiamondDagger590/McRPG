package us.eunoians.mcrpg.papi.placeholder;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.papi.McRPGPapiExpansion;

public interface PlaceholderRegisterFunction {

    void registerPlaceholders(@NotNull McRPG mcRPG, @NotNull McRPGPapiExpansion mcRPGPapiExpansion);
}
