package us.eunoians.mcrpg.gui.ability.slot.remotetransfer;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.ability.RemoteTransferGui;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

/**
 * This slot is used to toggle the allow state for all materials for a given player's {@link us.eunoians.mcrpg.ability.impl.mining.RemoteTransfer}.
 */
public class RemoteTransferToggleAllSlot implements McRPGSlot {

    private boolean enableAll;

    public RemoteTransferToggleAllSlot(boolean enableAll) {
        this.enableAll = enableAll;
    }

    @Override
    public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
        var guiOptional = mcRPGPlayer.getPlugin().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.GUI).getOpenedGui(mcRPGPlayer);
        guiOptional.ifPresent(gui -> {
            if (gui instanceof RemoteTransferGui remoteTransferGui) {
                for (RemoteTransferToggleSlot slot : remoteTransferGui.getAllCurrentItemSlots()) {
                    slot.toggleItemStack(enableAll);
                }
                setToggleState(!enableAll);
                gui.refreshGUI();
            }
        });
        return true;
    }

    @NotNull
    @Override
    public ItemBuilder getItem(@NotNull McRPGPlayer mcRPGPlayer) {
        return ItemBuilder.from(RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION)
                .getLocalizedSection(mcRPGPlayer, enableAll ? LocalizationKey.REMOTE_TRANSFER_GUI_TOGGLE_ENTIRE_CATEGORY_SLOT_TOGGLE_ENABLE :
                        LocalizationKey.REMOTE_TRANSFER_GUI_TOGGLE_ENTIRE_CATEGORY_SLOT_TOGGLE_DISABLE));
    }

    /**
     * Checks to see if, when clicked, this button will enable or disable all materials for the current category in the
     * player's {@link RemoteTransferGui}.
     *
     * @return {@code true} if this button will enable all materials when clicked, or {@code false} if it will disable all
     * materials.
     */
    public boolean isEnableAll() {
        return enableAll;
    }

    /**
     * Sets the toggle state for this slot.
     *
     * @param enableAll {@code true} if this button will enable all materials when clicked, or {@code false} if it will disable all
     *                  materials.
     */
    public void setToggleState(boolean enableAll) {
        this.enableAll = enableAll;
    }
}
