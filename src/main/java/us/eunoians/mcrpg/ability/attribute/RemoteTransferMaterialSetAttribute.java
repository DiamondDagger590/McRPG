package us.eunoians.mcrpg.ability.attribute;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.gui.Gui;
import com.diamonddagger590.mccore.gui.slot.Slot;
import com.diamonddagger590.mccore.player.CorePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.impl.Ability;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.ability.AbilityEditGui;
import us.eunoians.mcrpg.gui.ability.RemoteTransferGui;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This attribute is used specifically for {@link us.eunoians.mcrpg.ability.impl.mining.RemoteTransfer} to store a {@link Set}
 * of {@link Material}s that are disabled for a player's allow list.
 */
public class RemoteTransferMaterialSetAttribute extends OptionalSavingAbilityAttribute<Set<Material>> implements GuiModifiableAttribute {

    RemoteTransferMaterialSetAttribute() {
        super("material_set", AbilityAttributeManager.REMOTE_TRANSFER_MATERIAL_SET_ATTRIBUTE);
    }

    public RemoteTransferMaterialSetAttribute(@NotNull Set<Material> set) {
        super("material_set", AbilityAttributeManager.REMOTE_TRANSFER_MATERIAL_SET_ATTRIBUTE, set);
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
    public Slot getSlot(@NotNull McRPGPlayer player, @NotNull Ability ability) {
        return new Slot() {
            @Override
            public boolean onClick(@NotNull CorePlayer corePlayer, @NotNull ClickType clickType) {
                var guiOptional = CorePlugin.getInstance().getGuiTracker().getOpenedGui(corePlayer);
                if (corePlayer instanceof McRPGPlayer mcRPGPlayer && mcRPGPlayer.getAsBukkitPlayer().isPresent()
                        && guiOptional.isPresent() && guiOptional.get() instanceof AbilityEditGui abilityEditGui) {
                    abilityEditGui.setIgnoreClose(true);
                    RemoteTransferGui remoteTransferGui = new RemoteTransferGui(mcRPGPlayer);
                    Player player = mcRPGPlayer.getAsBukkitPlayer().get();
                    player.closeInventory();
                    Bukkit.getScheduler().scheduleSyncDelayedTask(McRPG.getInstance(), new Runnable() {
                        @Override
                        public void run() {
                            McRPG.getInstance().getGuiTracker().trackPlayerGui(mcRPGPlayer, remoteTransferGui);
                            player.openInventory(remoteTransferGui.getInventory());
                        }
                    }, 1L);
                }
                return true;
            }

            @NotNull
            @Override
            public ItemStack getItem() {
                MiniMessage miniMessage = McRPG.getInstance().getMiniMessage();
                ItemStack itemStack = new ItemStack(Material.CHEST);
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.displayName(miniMessage.deserialize("<gold>Block Filter"));
                itemMeta.lore(getGuiLore(player, ability));
                itemStack.setItemMeta(itemMeta);
                return itemStack;
            }

            @Override
            public Set<Class<? extends Gui>> getValidGuiTypes() {
                return Set.of(AbilityEditGui.class);
            }
        };
    }

    @NotNull
    @Override
    public List<Component> getGuiLore(@NotNull McRPGPlayer mcRPGPlayer, @NotNull Ability ability) {
        MiniMessage miniMessage = McRPG.getInstance().getMiniMessage();
        return List.of(miniMessage.deserialize("<gray>Click to filter blocks for <gold>Remote Transfer</gold>."));
    }
}
