package us.eunoians.mcrpg.external.nocheatplus;

import com.diamonddagger590.mccore.registry.plugin.PluginHook;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;

/**
 * The plugin hook needed for hooking into
 * <a href="https://www.spigotmc.org/resources/nocheatplus.26/">NoCheatPlus</a>.
 */
public class NoCheatPlusHook extends PluginHook<McRPG> {

    public NoCheatPlusHook(@NotNull McRPG plugin) {
        super(plugin);
    }
}
