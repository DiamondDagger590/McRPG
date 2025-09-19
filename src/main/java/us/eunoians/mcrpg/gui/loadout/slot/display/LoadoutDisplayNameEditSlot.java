package us.eunoians.mcrpg.gui.loadout.slot.display;


import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.chat.LoadoutDisplayNameChatResponse;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
import us.eunoians.mcrpg.loadout.Loadout;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

/**
 * Clicking this slot will start a {@link LoadoutDisplayNameChatResponse}, where when responded to,
 * the response will be saved as the new name for the {@link us.eunoians.mcrpg.loadout.LoadoutDisplay}.
 */
public class LoadoutDisplayNameEditSlot implements McRPGSlot {

    private final Loadout loadout;

    public LoadoutDisplayNameEditSlot(@NotNull Loadout loadout) {
        this.loadout = loadout;
    }

    @Override
    public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
        mcRPGPlayer.getAsBukkitPlayer().ifPresent(player -> {
            // Close inventory
            player.closeInventory();
            // Notify player to send a response for the new name of the loadout
            McRPGLocalizationManager localizationManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
            player.sendMessage(localizationManager.getLocalizedMessageAsComponent(player, LocalizationKey.LOADOUT_DISPLAY_HOME_GUI_NAME_EDIT_PROMPT));
            LoadoutDisplayNameChatResponse loadoutDisplayNameChatResponse = new LoadoutDisplayNameChatResponse(player.getUniqueId(), loadout);
            RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.CHAT_RESPONSE).addPendingResponse(player.getUniqueId(), loadoutDisplayNameChatResponse);
        });
        return true;
    }


    @NotNull
    @Override
    public ItemBuilder getItem(@NotNull McRPGPlayer mcRPGPlayer) {
        return ItemBuilder.from(RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION).getLocalizedSection(mcRPGPlayer, LocalizationKey.LOADOUT_DISPLAY_HOME_GUI_NAME_EDIT_DISPLAY_ITEM));
    }
}
