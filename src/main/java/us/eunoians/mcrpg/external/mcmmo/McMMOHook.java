package us.eunoians.mcrpg.external.mcmmo;

import com.diamonddagger590.mccore.registry.plugin.PluginHook;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;

/**
 * A hook for containing all code related to
 * <a href="https://www.spigotmc.org/resources/official-mcmmo-original-author-returns.64348/">McMMO</a>
 * that this plugin needs to support it.
 */
public class McMMOHook extends PluginHook<McRPG> {

    public McMMOHook(@NotNull McRPG plugin) {
        super(plugin);
    }
}
