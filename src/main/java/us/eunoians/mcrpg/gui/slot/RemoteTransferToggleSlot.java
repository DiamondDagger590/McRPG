package us.eunoians.mcrpg.gui.slot;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.gui.Gui;
import com.diamonddagger590.mccore.gui.slot.Slot;
import com.diamonddagger590.mccore.player.CorePlayer;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeManager;
import us.eunoians.mcrpg.ability.attribute.RemoteTransferMaterialSetAttribute;
import us.eunoians.mcrpg.ability.impl.mining.RemoteTransfer;
import us.eunoians.mcrpg.ability.impl.mining.remotetransfer.RemoteTransferCategoryType;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.ability.RemoteTransferGui;

import java.util.List;
import java.util.Set;

/**
 * This slot is used to toggle a specific item's allow list state for a given player's {@link RemoteTransfer}.
 */
public class RemoteTransferToggleSlot extends Slot {

    private final McRPGPlayer mcRPGPlayer;
    private final Material material;
    private final RemoteTransferCategoryType remoteTransferCategoryType;

    public RemoteTransferToggleSlot(@NotNull McRPGPlayer mcRPGPlayer, @NotNull Material material, @NotNull RemoteTransferCategoryType remoteTransferCategoryType) {
        this.mcRPGPlayer = mcRPGPlayer;
        this.material = material;
        this.remoteTransferCategoryType = remoteTransferCategoryType;
    }

    @Override
    public boolean onClick(@NotNull CorePlayer corePlayer, @NotNull ClickType clickType) {
        var guiOptional = CorePlugin.getInstance().getGuiTracker().getOpenedGui(corePlayer);
        guiOptional.ifPresent(gui -> {
            toggleMaterial();
            gui.refreshGUI();
        });
        return true;
    }

    @NotNull
    @Override
    public ItemStack getItem() {
        RemoteTransfer remoteTransfer = (RemoteTransfer) McRPG.getInstance().getAbilityRegistry().getRegisteredAbility(RemoteTransfer.REMOTE_TRANSFER_KEY);
        MiniMessage miniMessage = McRPG.getInstance().getMiniMessage();
        ItemStack itemStack = new ItemStack(material);
        var abilityDataOptional = mcRPGPlayer.asSkillHolder().getAbilityData(remoteTransfer);

        if (abilityDataOptional.isPresent() && abilityDataOptional.get().getAbilityAttribute(AbilityAttributeManager.REMOTE_TRANSFER_MATERIAL_SET_ATTRIBUTE).isPresent() &&
                abilityDataOptional.get().getAbilityAttribute(AbilityAttributeManager.REMOTE_TRANSFER_MATERIAL_SET_ATTRIBUTE).get() instanceof RemoteTransferMaterialSetAttribute remoteTransferMaterialSetAttribute) {
            ItemMeta itemMeta = itemStack.getItemMeta();
            boolean materialDisabled = isMaterialDisallowed();
            itemMeta.lore(List.of(miniMessage.deserialize("<gray>Click to toggle the filter state for this item."),
                    miniMessage.deserialize("<gray>Item is currently " + (materialDisabled ? "<red>disabled</red>" : "<green>enabled</green>") + "."),
                    miniMessage.deserialize("<gray>Category: <gold>" + remoteTransferCategoryType.getName())));
            if (!materialDisabled) {
                itemMeta.addEnchant(Enchantment.POWER, 1, true);
                itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            itemStack.setItemMeta(itemMeta);
        }
        return itemStack;
    }

    @Override
    public Set<Class<? extends Gui>> getValidGuiTypes() {
        return Set.of(RemoteTransferGui.class);
    }

    /**
     * Gets the {@link RemoteTransferCategoryType} that the item represented by this slot belongs to.
     *
     * @return The {@link RemoteTransferCategoryType} that the item represented by this slot belongs to.
     */
    @NotNull
    public RemoteTransferCategoryType getRemoteTransferCategory() {
        return remoteTransferCategoryType;
    }

    /**
     * Checks to see if the item represented by this slot is disallowed for usage with {@link RemoteTransfer}.
     *
     * @return {@code true} if the item represented by this slow is disallowed for usage with {@link RemoteTransfer}.
     */
    public boolean isMaterialDisallowed() {
        RemoteTransfer remoteTransfer = (RemoteTransfer) McRPG.getInstance().getAbilityRegistry().getRegisteredAbility(RemoteTransfer.REMOTE_TRANSFER_KEY);
        var abilityDataOptional = mcRPGPlayer.asSkillHolder().getAbilityData(remoteTransfer);
        if (abilityDataOptional.isPresent() && abilityDataOptional.get().getAbilityAttribute(AbilityAttributeManager.REMOTE_TRANSFER_MATERIAL_SET_ATTRIBUTE).isPresent() &&
                abilityDataOptional.get().getAbilityAttribute(AbilityAttributeManager.REMOTE_TRANSFER_MATERIAL_SET_ATTRIBUTE).get() instanceof RemoteTransferMaterialSetAttribute remoteTransferMaterialSetAttribute) {
            return remoteTransferMaterialSetAttribute.isMaterialStored(material);
        }
        return false;
    }

    /**
     * Toggles the allow list state for the item represented by this slot.
     */
    public void toggleMaterial() {
        RemoteTransfer remoteTransfer = (RemoteTransfer) McRPG.getInstance().getAbilityRegistry().getRegisteredAbility(RemoteTransfer.REMOTE_TRANSFER_KEY);
        var abilityDataOptional = mcRPGPlayer.asSkillHolder().getAbilityData(remoteTransfer);
        if (abilityDataOptional.isPresent() && abilityDataOptional.get().getAbilityAttribute(AbilityAttributeManager.REMOTE_TRANSFER_MATERIAL_SET_ATTRIBUTE).isPresent() &&
                abilityDataOptional.get().getAbilityAttribute(AbilityAttributeManager.REMOTE_TRANSFER_MATERIAL_SET_ATTRIBUTE).get() instanceof RemoteTransferMaterialSetAttribute remoteTransferMaterialSetAttribute) {
            if (remoteTransferMaterialSetAttribute.isMaterialStored(material)) {
                remoteTransferMaterialSetAttribute.getContent().remove(material);
                abilityDataOptional.get().addAttribute(remoteTransferMaterialSetAttribute);
            } else {
                remoteTransferMaterialSetAttribute.getContent().add(material);
                abilityDataOptional.get().addAttribute(remoteTransferMaterialSetAttribute);
            }
        }
    }

    /**
     * Sets the allow list state for the item represented by this slot.
     *
     * @param enableMaterial If the item represented by this slot should be allow listed by {@link RemoteTransfer}.
     */
    public void toggleMaterial(boolean enableMaterial) {
        RemoteTransfer remoteTransfer = (RemoteTransfer) McRPG.getInstance().getAbilityRegistry().getRegisteredAbility(RemoteTransfer.REMOTE_TRANSFER_KEY);
        var abilityDataOptional = mcRPGPlayer.asSkillHolder().getAbilityData(remoteTransfer);
        if (abilityDataOptional.isPresent() && abilityDataOptional.get().getAbilityAttribute(AbilityAttributeManager.REMOTE_TRANSFER_MATERIAL_SET_ATTRIBUTE).isPresent() &&
                abilityDataOptional.get().getAbilityAttribute(AbilityAttributeManager.REMOTE_TRANSFER_MATERIAL_SET_ATTRIBUTE).get() instanceof RemoteTransferMaterialSetAttribute remoteTransferMaterialSetAttribute) {
            if (enableMaterial) {
                remoteTransferMaterialSetAttribute.getContent().remove(material);
                abilityDataOptional.get().addAttribute(remoteTransferMaterialSetAttribute);
            } else {
                remoteTransferMaterialSetAttribute.getContent().add(material);
                abilityDataOptional.get().addAttribute(remoteTransferMaterialSetAttribute);
            }
        }
    }
}
