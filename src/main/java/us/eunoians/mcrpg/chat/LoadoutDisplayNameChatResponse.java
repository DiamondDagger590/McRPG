package us.eunoians.mcrpg.chat;

import com.diamonddagger590.mccore.chat.ChatResponse;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChatEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.MainConfigFile;
import us.eunoians.mcrpg.gui.loadout.display.LoadoutDisplayHomeGui;
import us.eunoians.mcrpg.loadout.Loadout;
import us.eunoians.mcrpg.loadout.LoadoutDisplay;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.UUID;

/**
 * This chat response updates the player's {@link Loadout}'s display name
 * based on their chat response.
 */
public class LoadoutDisplayNameChatResponse extends ChatResponse {

    private final Loadout loadout;

    public LoadoutDisplayNameChatResponse(@NotNull UUID chatterUUID, @NotNull Loadout loadout) {
        super(chatterUUID);
        this.loadout = loadout;
    }

    @Override
    public long getResponseWaitTime() {
        return McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE).getFile(FileType.MAIN_CONFIG).getInt(MainConfigFile.LOADOUT_DISPLAY_NAME_RESPONSE_TIMEOUT, 10);
    }

    @Override
    public void onResponse(@NotNull PlayerChatEvent playerChatEvent) {
        LoadoutDisplay loadoutDisplay = loadout.getDisplay();
        loadoutDisplay.setDisplayName(playerChatEvent.getMessage());
        Player player = playerChatEvent.getPlayer();
        McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.PLAYER).getPlayer(player.getUniqueId()).ifPresent(mcRPGPlayer -> {
            LoadoutDisplayHomeGui loadoutDisplayHomeGui = new LoadoutDisplayHomeGui(mcRPGPlayer, loadout);
            McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.GUI).trackPlayerGui(mcRPGPlayer, loadoutDisplayHomeGui);
            player.openInventory(loadoutDisplayHomeGui.getInventory());
        });
    }

    @Override
    public void onExpire() {

    }
}
