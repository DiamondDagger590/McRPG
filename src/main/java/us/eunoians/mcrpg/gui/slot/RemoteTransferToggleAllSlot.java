package us.eunoians.mcrpg.gui.slot;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.ability.RemoteTransferGui;

import java.util.List;

/**
 * This slot is used to toggle the allow state for all materials for a given player's {@link us.eunoians.mcrpg.ability.impl.mining.RemoteTransfer}.
 */
public class RemoteTransferToggleAllSlot extends McRPGSlot {

    private boolean enableAll;

    public RemoteTransferToggleAllSlot(boolean enableAll) {
        this.enableAll = enableAll;
    }

    @Override
    public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
        var guiOptional = CorePlugin.getInstance().getGuiTracker().getOpenedGui(mcRPGPlayer);
        guiOptional.ifPresent(gui -> {
            if (gui instanceof RemoteTransferGui remoteTransferGui) {
                for (RemoteTransferToggleSlot slot : remoteTransferGui.getAllCurrentItemSlots()) {
                    slot.toggleMaterial(enableAll);
                }
                setToggleState(!enableAll);
                gui.refreshGUI();
            }
        });
        return true;
    }

    @NotNull
    @Override
    public ItemBuilder getItem(@Nullable McRPGPlayer mcRPGPlayer) {
        MiniMessage miniMessage = McRPG.getInstance().getMiniMessage();
        ItemStack itemStack = new ItemStack(enableAll ? Material.GREEN_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.displayName(miniMessage.deserialize("<red>Click to toggle all items</red>"));
        itemMeta.lore(List.of(miniMessage.deserialize("<gray>Click to toggle all items in the current category to be " + (enableAll ? "<green>enabled</green>" : "<red>disabled</red>") + ".")));
        itemStack.setItemMeta(itemMeta);
        return ItemBuilder.from(itemStack);
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
