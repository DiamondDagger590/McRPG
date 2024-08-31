package us.eunoians.mcrpg.gui.slot.loadout;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.exception.CorePlayerOfflineException;
import com.diamonddagger590.mccore.gui.Gui;
import com.diamonddagger590.mccore.gui.slot.Slot;
import com.diamonddagger590.mccore.player.CorePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.geysermc.api.Geyser;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.loadout.LoadoutGui;
import us.eunoians.mcrpg.gui.loadout.LoadoutSelectionGui;
import us.eunoians.mcrpg.loadout.Loadout;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * This slot is used to select a specific {@link Loadout} to edit.
 */
public class LoadoutSelectionSlot extends Slot {

    private final McRPGPlayer mcRPGPlayer;
    private final Player player;
    private final Loadout loadout;

    public LoadoutSelectionSlot(@NotNull McRPGPlayer mcRPGPlayer, @NotNull Loadout loadout) {
        this.mcRPGPlayer = mcRPGPlayer;
        Optional<Player> playerOptional = mcRPGPlayer.getAsBukkitPlayer();
        if (playerOptional.isEmpty()) {
            throw new CorePlayerOfflineException(mcRPGPlayer);
        }
        this.player = playerOptional.get();
        this.loadout = loadout;
    }

    @Override
    public boolean onClick(@NotNull CorePlayer corePlayer, @NotNull ClickType clickType) {
        var guiOptional = CorePlugin.getInstance().getGuiTracker().getOpenedGui(corePlayer);
        guiOptional.ifPresent(gui -> {
            if (isPlayerOnGeyser() || clickType != ClickType.RIGHT) {
                LoadoutGui loadoutGui = new LoadoutGui(mcRPGPlayer, loadout);
                McRPG.getInstance().getGuiTracker().trackPlayerGui(mcRPGPlayer, loadoutGui);
                player.openInventory(loadoutGui.getInventory());
            }
            else {
                player.performCommand("loadout set " + loadout.getLoadoutSlot());
                gui.refreshGUI();
            }
        });
        return true;
    }

    @NotNull
    @Override
    public ItemStack getItem() {
        MiniMessage miniMessage = McRPG.getInstance().getMiniMessage();
        ItemStack itemStack = new ItemStack(Material.CHERRY_SIGN);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.displayName(miniMessage.deserialize("<gray>Loadout <gold>" + loadout.getLoadoutSlot()));
        List<Component> lore = new ArrayList<>();
        if (isPlayerOnGeyser() || isLoadoutActive()) {
            lore.add(miniMessage.deserialize("<gray>Click to edit this loadout."));
        }
        else {
            lore.add(miniMessage.deserialize("<gray>Left click to edit this loadout."));
            lore.add(miniMessage.deserialize("<gray>Right click to set this loadout as active."));
        }
        if (isLoadoutActive()) {
            lore.add(miniMessage.deserialize("<gray>This is your currently selected loadout."));
            itemMeta.addEnchant(Enchantment.POWER, 1, true);
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        itemMeta.lore(lore);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    @Override
    public Set<Class<? extends Gui>> getValidGuiTypes() {
        return Set.of(LoadoutSelectionGui.class);
    }

    private boolean isLoadoutActive() {
        return loadout.getLoadoutSlot() == mcRPGPlayer.asSkillHolder().getCurrentLoadoutSlot();
    }

    private boolean isPlayerOnGeyser() {
        return (McRPG.getInstance().isGeyserEnabled() && Geyser.api().isBedrockPlayer(mcRPGPlayer.getUUID()));
    }
}
