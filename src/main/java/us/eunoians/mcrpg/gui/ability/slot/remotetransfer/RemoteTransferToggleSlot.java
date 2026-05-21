package us.eunoians.mcrpg.gui.ability.slot.remotetransfer;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.util.item.CustomItemWrapper;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.Material;
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
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * This slot is used to toggle a specific item's allow list state for a given player's {@link RemoteTransfer}.
 */
public class RemoteTransferToggleSlot implements McRPGSlot {

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
        McRPGLocalizationManager localizationManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION);
        boolean materialDisabled = isItemDisallowed();
        Route localizationRoute = materialDisabled
                ? LocalizationKey.REMOTE_TRANSFER_GUI_CATEGORY_ITEM_OPTION_DISABLED_DISPLAY_ITEM
                : LocalizationKey.REMOTE_TRANSFER_GUI_CATEGORY_ITEM_OPTION_ENABLED_DISPLAY_ITEM;
        ItemBuilder itemBuilder = ItemBuilder.from(Objects.requireNonNull(customItemWrapper.material().get().asItemType()));
        customItemWrapper.customItem().ifPresent(itemBuilder::withCustomItem);

        Map<String, String> placeholders = Map.of(
                "item-type", resolveItemTypeDisplayName(customItemWrapper),
                "remote-transfer-category", remoteTransferCategory.getName(mcRPGPlayer));
        String displayName = localizationManager.resolvePaletteColors(localizationManager.getLocalizedMessage(
                localizationManager.getLocalizedMessage(mcRPGPlayer, Route.addTo(localizationRoute, "name")),
                placeholders));
        itemBuilder.setDisplayName(displayName);
        var resolvedLore = localizationManager.getLocalizedMessages(mcRPGPlayer, Route.addTo(localizationRoute, "lore")).stream()
                .map(line -> localizationManager.resolvePaletteColors(
                        localizationManager.getLocalizedMessage(line, placeholders)))
                .toList();
        itemBuilder.withDisplayLore(resolvedLore);
        itemBuilder.setEnchantGlint(!materialDisabled);
        return itemBuilder;
    }

    /**
     * Resolves the human-readable label for the {@code <item-type>} placeholder in toggle-slot locale templates.
     *
     * @param wrapper The configured block or custom item for this slot.
     * @return A display name for the item (custom item id or a title-cased material name).
     */
    @NotNull
    private static String resolveItemTypeDisplayName(@NotNull CustomItemWrapper wrapper) {
        return wrapper.customItem()
                .orElseGet(() -> wrapper.material()
                        .map(RemoteTransferToggleSlot::formatMaterialName)
                        .orElse("Unknown"));
    }

    /**
     * Converts a {@link Material} enum constant into a title-cased label (e.g. {@code DIAMOND_ORE} → {@code Diamond Ore}).
     *
     * @param material The material to format.
     * @return A title-cased display name derived from the enum constant.
     */
    @NotNull
    private static String formatMaterialName(@NotNull Material material) {
        String[] parts = material.name().split("_");
        StringBuilder label = new StringBuilder();
        for (String part : parts) {
            if (!label.isEmpty()) {
                label.append(' ');
            }
            label.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                label.append(part.substring(1).toLowerCase());
            }
        }
        return label.toString();
    }

    @NotNull
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
