package us.eunoians.mcrpg.external.sickle;

import com.diamonddagger590.mccore.registry.plugin.PluginHook;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;

/**
 * The plugin hook responsible for hooking into
 * <a href="https://www.spigotmc.org/resources/sickle-harvest-crops-with-a-right-click.29443/">Sickle</a>.
 */
public class SickleHook extends PluginHook<McRPG> {

    public SickleHook(@NotNull McRPG plugin) {
        super(plugin);
    }
}
