package us.eunoians.mcrpg.bootstrap;

import com.diamonddagger590.mccore.bootstrap.BootstrapContext;
import com.diamonddagger590.mccore.bootstrap.registrar.Registrar;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.expansion.ContentExpansionManager;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.expansion.handler.ContentHandlerType;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Arrays;

/**
 * This registrar is in charge of registering commands for McRPG.
 */
final class McRPGExpansionRegistrar implements Registrar<McRPG> {

    @Override
    public void register(@NotNull BootstrapContext<McRPG> context) {
        McRPG plugin = context.plugin();
        ContentExpansionManager contentExpansionManager = plugin.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.CONTENT_EXPANSION);
        Arrays.stream(ContentHandlerType.values()).forEach(contentHandlerType -> contentExpansionManager.registerContentHandler(contentHandlerType.getContentHandler()));
        contentExpansionManager.registerContentExpansion(new McRPGExpansion(plugin));
    }
}
