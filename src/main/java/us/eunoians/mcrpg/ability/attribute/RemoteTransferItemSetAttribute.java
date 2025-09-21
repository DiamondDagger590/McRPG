package us.eunoians.mcrpg.ability.attribute;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.util.item.CustomItemWrapper;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.ability.AbilityAttributeEditGui;
import us.eunoians.mcrpg.gui.ability.RemoteTransferGui;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.HashSet;
import java.util.Set;

/**
 * This attribute is used specifically for {@link us.eunoians.mcrpg.ability.impl.mining.RemoteTransfer} to store a {@link Set}
 * of {@link String}s representing either a {@link Material} or custom block id that are disabled for a player's allow list.
 */
public class RemoteTransferItemSetAttribute extends OptionalSavingAbilityAttribute<Set<CustomItemWrapper>> implements GuiModifiableAttribute {

    RemoteTransferItemSetAttribute() {
        super("material_set", AbilityAttributeRegistry.REMOTE_TRANSFER_ITEM_SET_ATTRIBUTE);
    }

    public RemoteTransferItemSetAttribute(@NotNull Set<CustomItemWrapper> set) {
        super("material_set", AbilityAttributeRegistry.REMOTE_TRANSFER_ITEM_SET_ATTRIBUTE, set);
    }

    @Override
    public boolean shouldContentBeSaved() {
        return !getContent().isEmpty();
    }

    @NotNull
    @Override
    public AbilityAttribute<Set<CustomItemWrapper>> create(@NotNull Set<CustomItemWrapper> content) {
        return new RemoteTransferItemSetAttribute(content);
    }

    @NotNull
    @Override
    public Set<CustomItemWrapper> convertContent(@NotNull String stringContent) {
        Set<CustomItemWrapper> set = new HashSet<>();
        for (String type : stringContent.split(",")) {
            set.add(new CustomItemWrapper(type));
        }
        return set;
    }

    @NotNull
    @Override
    public Set<CustomItemWrapper> getDefaultContent() {
        return new HashSet<>();
    }

    @NotNull
    @Override
    public String serializeContent() {
        return getContent().stream()
                .map(customItemWrapper -> customItemWrapper.customItem().orElse(customItemWrapper.material().get().toString()))
                .reduce((s, s2) -> s + "," + s2)
                .orElse("");
    }

    public boolean isCustomItemWrapperStored(@NotNull CustomItemWrapper customItemWrapper) {
        return getContent().contains(customItemWrapper);
    }

    @NotNull
    @Override
    public McRPGSlot getSlot(@NotNull McRPGPlayer player, @NotNull Ability ability) {
        return new McRPGSlot() {
            @Override
            public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
                var guiOptional = mcRPGPlayer.getPlugin().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.GUI).getOpenedGui(mcRPGPlayer);
                if (mcRPGPlayer.getAsBukkitPlayer().isPresent()
                        && guiOptional.isPresent() && guiOptional.get() instanceof AbilityAttributeEditGui abilityAttributeEditGui) {
                    abilityAttributeEditGui.setIgnoreClose(true);
                    RemoteTransferGui remoteTransferGui = new RemoteTransferGui(mcRPGPlayer);
                    Player player = mcRPGPlayer.getAsBukkitPlayer().get();
                    player.closeInventory();
                    Bukkit.getScheduler().scheduleSyncDelayedTask(McRPG.getInstance(), () -> {
                        mcRPGPlayer.getPlugin().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.GUI).trackPlayerGui(mcRPGPlayer, remoteTransferGui);
                        player.openInventory(remoteTransferGui.getInventory());
                    }, 1L);
                }
                return true;
            }

            @NotNull
            @Override
            public ItemBuilder getItem(@NotNull McRPGPlayer mcRPGPlayer) {
                return ItemBuilder.from(RegistryAccess.registryAccess()
                        .registry(RegistryKey.MANAGER)
                        .manager(McRPGManagerKey.LOCALIZATION)
                        .getLocalizedSection(mcRPGPlayer, LocalizationKey.REMOTE_TRANSFER_BLOCK_TOGGLE_ATTRIBUTE));
            }

            @NotNull
            @Override
            public Set<Class<?>> getValidGuiTypes() {
                return Set.of(AbilityAttributeEditGui.class);
            }
        };
    }
}
