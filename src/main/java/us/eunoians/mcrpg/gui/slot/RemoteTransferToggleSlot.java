package us.eunoians.mcrpg.gui.slot;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.util.item.CustomItemWrapper;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeRegistry;
import us.eunoians.mcrpg.ability.attribute.RemoteTransferItemSetAttribute;
import us.eunoians.mcrpg.ability.impl.mining.RemoteTransfer;
import us.eunoians.mcrpg.ability.impl.mining.remotetransfer.RemoteTransferCategory;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.ability.RemoteTransferGui;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Set;

/**
 * This slot is used to toggle a specific item's allow list state for a given player's {@link RemoteTransfer}.
 */
public class RemoteTransferToggleSlot extends McRPGSlot {

    private final McRPGPlayer mcRPGPlayer;
    private final CustomItemWrapper customItemWrapper;
    private final RemoteTransferCategory remoteTransferCategory;

    public RemoteTransferToggleSlot(@NotNull McRPGPlayer mcRPGPlayer, @NotNull CustomItemWrapper customItemWrapper, @NotNull RemoteTransferCategory remoteTransferCategory) {
        this.mcRPGPlayer = mcRPGPlayer;
        this.customItemWrapper = customItemWrapper;
        this.remoteTransferCategory = remoteTransferCategory;
    }

    @Override
    public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
        var guiOptional = mcRPGPlayer.getPlugin().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.GUI).getOpenedGui(mcRPGPlayer);
        guiOptional.ifPresent(gui -> {
            toggleItemStack();
            gui.refreshGUI();
        });
        return true;
    }

    @NotNull
    @Override
    public ItemBuilder getItem(@NotNull McRPGPlayer mcRPGPlayer) {
        boolean materialDisabled = isItemDisallowed();
        ItemBuilder builder = customItemWrapper.itemBuilder();
        Route localizationRoute = materialDisabled ? LocalizationKey.REMOTE_TRANSFER_GUI_CATEGORY_ITEM_OPTION_DISABLED_DISPLAY_ITEM : LocalizationKey.REMOTE_TRANSFER_GUI_CATEGORY_ITEM_OPTION_ENABLED_DISPLAY_ITEM;
        builder.withDisplayLore(RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION).getLocalizedMessages(mcRPGPlayer, Route.addTo(localizationRoute, "lore")));
        builder.addPlaceholder("remote-transfer-category", remoteTransferCategory.getName(mcRPGPlayer));
        return builder;
    }

    @Override
    public Set<Class<?>> getValidGuiTypes() {
        return Set.of(RemoteTransferGui.class);
    }

    /**
     * Gets the {@link RemoteTransferCategory} that the item represented by this slot belongs to.
     *
     * @return The {@link RemoteTransferCategory} that the item represented by this slot belongs to.
     */
    @NotNull
    public RemoteTransferCategory getRemoteTransferCategory() {
        return remoteTransferCategory;
    }

    /**
     * Checks to see if the item represented by this slot is disallowed for usage with {@link RemoteTransfer}.
     *
     * @return {@code true} if the item represented by this slow is disallowed for usage with {@link RemoteTransfer}.
     */
    public boolean isItemDisallowed() {
        RemoteTransfer remoteTransfer = (RemoteTransfer) McRPG.getInstance().registryAccess().registry(McRPGRegistryKey.ABILITY).getRegisteredAbility(RemoteTransfer.REMOTE_TRANSFER_KEY);
        var abilityDataOptional = mcRPGPlayer.asSkillHolder().getAbilityData(remoteTransfer);
        if (abilityDataOptional.isPresent() && abilityDataOptional.get().getAbilityAttribute(AbilityAttributeRegistry.REMOTE_TRANSFER_ITEM_SET_ATTRIBUTE).isPresent() &&
                abilityDataOptional.get().getAbilityAttribute(AbilityAttributeRegistry.REMOTE_TRANSFER_ITEM_SET_ATTRIBUTE).get() instanceof RemoteTransferItemSetAttribute remoteTransferItemSetAttribute) {
            return remoteTransferItemSetAttribute.isCustomItemWrapperStored(customItemWrapper);
        }
        return false;
    }

    /**
     * Toggles the allow list state for the item represented by this slot.
     */
    public void toggleItemStack() {
        RemoteTransfer remoteTransfer = (RemoteTransfer) McRPG.getInstance().registryAccess().registry(McRPGRegistryKey.ABILITY).getRegisteredAbility(RemoteTransfer.REMOTE_TRANSFER_KEY);
        var abilityDataOptional = mcRPGPlayer.asSkillHolder().getAbilityData(remoteTransfer);
        if (abilityDataOptional.isPresent() && abilityDataOptional.get().getAbilityAttribute(AbilityAttributeRegistry.REMOTE_TRANSFER_ITEM_SET_ATTRIBUTE).isPresent() &&
                abilityDataOptional.get().getAbilityAttribute(AbilityAttributeRegistry.REMOTE_TRANSFER_ITEM_SET_ATTRIBUTE).get() instanceof RemoteTransferItemSetAttribute remoteTransferItemSetAttribute) {
            if (remoteTransferItemSetAttribute.isCustomItemWrapperStored(customItemWrapper)) {
                remoteTransferItemSetAttribute.getContent().remove(customItemWrapper);
                abilityDataOptional.get().addAttribute(remoteTransferItemSetAttribute);
            } else {
                remoteTransferItemSetAttribute.getContent().add(customItemWrapper);
                abilityDataOptional.get().addAttribute(remoteTransferItemSetAttribute);
            }
        }
    }

    /**
     * Sets the allow list state for the item represented by this slot.
     *
     * @param enableItem If the item represented by this slot should be allow listed by {@link RemoteTransfer}.
     */
    public void toggleItemStack(boolean enableItem) {
        RemoteTransfer remoteTransfer = (RemoteTransfer) McRPG.getInstance().registryAccess().registry(McRPGRegistryKey.ABILITY).getRegisteredAbility(RemoteTransfer.REMOTE_TRANSFER_KEY);
        var abilityDataOptional = mcRPGPlayer.asSkillHolder().getAbilityData(remoteTransfer);
        if (abilityDataOptional.isPresent() && abilityDataOptional.get().getAbilityAttribute(AbilityAttributeRegistry.REMOTE_TRANSFER_ITEM_SET_ATTRIBUTE).isPresent() &&
                abilityDataOptional.get().getAbilityAttribute(AbilityAttributeRegistry.REMOTE_TRANSFER_ITEM_SET_ATTRIBUTE).get() instanceof RemoteTransferItemSetAttribute remoteTransferItemSetAttribute) {
            if (enableItem) {
                remoteTransferItemSetAttribute.getContent().remove(customItemWrapper);
                abilityDataOptional.get().addAttribute(remoteTransferItemSetAttribute);
            } else {
                remoteTransferItemSetAttribute.getContent().add(customItemWrapper);
                abilityDataOptional.get().addAttribute(remoteTransferItemSetAttribute);
            }
        }
    }
}
