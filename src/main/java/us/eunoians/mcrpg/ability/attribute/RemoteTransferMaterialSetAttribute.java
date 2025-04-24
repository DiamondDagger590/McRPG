package us.eunoians.mcrpg.ability.attribute;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.registry.RegistryKey;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.impl.Ability;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.ability.AbilityEditGui;
import us.eunoians.mcrpg.gui.ability.RemoteTransferGui;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.HashSet;
import java.util.Set;

/**
 * This attribute is used specifically for {@link us.eunoians.mcrpg.ability.impl.mining.RemoteTransfer} to store a {@link Set}
 * of {@link Material}s that are disabled for a player's allow list.
 */
public class RemoteTransferMaterialSetAttribute extends OptionalSavingAbilityAttribute<Set<Material>> implements GuiModifiableAttribute {

    RemoteTransferMaterialSetAttribute() {
        super("material_set", AbilityAttributeRegistry.REMOTE_TRANSFER_MATERIAL_SET_ATTRIBUTE);
    }

    public RemoteTransferMaterialSetAttribute(@NotNull Set<Material> set) {
        super("material_set", AbilityAttributeRegistry.REMOTE_TRANSFER_MATERIAL_SET_ATTRIBUTE, set);
    }

    @Override
    public boolean shouldContentBeSaved() {
        return !getContent().isEmpty();
    }

    @NotNull
    @Override
    public AbilityAttribute<Set<Material>> create(@NotNull Set<Material> content) {
        return new RemoteTransferMaterialSetAttribute(content);
    }

    @NotNull
    @Override
    public Set<Material> convertContent(@NotNull String stringContent) {
        Set<Material> set = new HashSet<>();
        for (String type : stringContent.split(",")) {
            Material material = Material.getMaterial(type);
            if (material != null) {
                set.add(material);
            }
        }
        return set;
    }

    @NotNull
    @Override
    public Set<Material> getDefaultContent() {
        return new HashSet<>();
    }

    @NotNull
    @Override
    public String serializeContent() {
        return getContent().stream().map(Material::name).reduce((s, s2) -> s + "," + s2).orElse("");
    }

    public boolean isMaterialStored(@NotNull Material material) {
        return getContent().contains(material);
    }

    @NotNull
    @Override
    public McRPGSlot getSlot(@NotNull McRPGPlayer player, @NotNull Ability ability) {
        return new McRPGSlot() {
            @Override
            public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
                var guiOptional = mcRPGPlayer.getPlugin().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.GUI).getOpenedGui(mcRPGPlayer);
                if (mcRPGPlayer.getAsBukkitPlayer().isPresent()
                        && guiOptional.isPresent() && guiOptional.get() instanceof AbilityEditGui abilityEditGui) {
                    abilityEditGui.setIgnoreClose(true);
                    RemoteTransferGui remoteTransferGui = new RemoteTransferGui(mcRPGPlayer);
                    Player player = mcRPGPlayer.getAsBukkitPlayer().get();
                    player.closeInventory();
                    Bukkit.getScheduler().scheduleSyncDelayedTask(McRPG.getInstance(), new Runnable() {
                        @Override
                        public void run() {
                            mcRPGPlayer.getPlugin().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.GUI).trackPlayerGui(mcRPGPlayer, remoteTransferGui);
                            player.openInventory(remoteTransferGui.getInventory());
                        }
                    }, 1L);
                }
                return true;
            }

            @NotNull
            @Override
            public ItemBuilder getItem(@Nullable McRPGPlayer mcRPGPlayer) {
                MiniMessage miniMessage = McRPG.getInstance().getMiniMessage();
                ItemStack itemStack = new ItemStack(Material.CHEST);
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.displayName(miniMessage.deserialize("<gold>Block Filter"));
//                itemMeta.lore(getGuiLore(player, ability));
                itemStack.setItemMeta(itemMeta);
                return ItemBuilder.from(itemStack);
            }

            @Override
            public Set<Class<?>> getValidGuiTypes() {
                return Set.of(AbilityEditGui.class);
            }
        };
    }

//    @NotNull
//    @Override
//    public List<Component> getGuiLore(@NotNull McRPGPlayer mcRPGPlayer, @NotNull Ability ability) {
//        MiniMessage miniMessage = McRPG.getInstance().getMiniMessage();
//        return List.of(miniMessage.deserialize("<gray>Click to filter blocks for <gold>Remote Transfer</gold>."));
//    }
}
