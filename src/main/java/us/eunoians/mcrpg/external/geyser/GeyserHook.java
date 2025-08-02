package us.eunoians.mcrpg.external.geyser;

import com.diamonddagger590.mccore.registry.plugin.PluginHook;
import org.geysermc.api.Geyser;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;

import java.util.UUID;

/**
 * A hook for containing all code related to <a href="https://www.spigotmc.org/resources/geyser-minecraft-bedrock-protocol-support.81297/">Geyser</a>
 * that this plugin needs in order to support it.
 */
public class GeyserHook extends PluginHook<McRPG> {

    public GeyserHook(@NotNull McRPG plugin) {
        super(plugin);
    }

    public boolean isBedrockPlayer(@NotNull UUID uuid) {
        return Geyser.api().isBedrockPlayer(uuid);
    }
}
