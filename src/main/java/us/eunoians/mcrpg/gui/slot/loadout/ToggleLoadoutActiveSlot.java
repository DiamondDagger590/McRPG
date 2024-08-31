package us.eunoians.mcrpg.gui.slot.loadout;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.exception.CorePlayerOfflineException;
import com.diamonddagger590.mccore.gui.Gui;
import com.diamonddagger590.mccore.gui.slot.Slot;
import com.diamonddagger590.mccore.player.CorePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.loadout.LoadoutGui;
import us.eunoians.mcrpg.loadout.Loadout;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * This slot is used to represent the current active state of a player's
 * {@link Loadout}. If the loadout is not currently the player's active one,
 * clicking this slot will set it as such.
 */
public class ToggleLoadoutActiveSlot extends Slot {

    private final McRPGPlayer mcRPGPlayer;
    private final Player player;
    private final Loadout loadout;

    public ToggleLoadoutActiveSlot(@NotNull McRPGPlayer mcRPGPlayer, @NotNull Loadout loadout) {
        this.mcRPGPlayer = mcRPGPlayer;
        Optional<Player> playerOptional = mcRPGPlayer.getAsBukkitPlayer();
        if (playerOptional.isEmpty()) {
            throw new CorePlayerOfflineException(mcRPGPlayer);
        }
        this.player = playerOptional.get();
        this.loadout = loadout;
    }

    @NotNull
    @Override
    public ItemStack getItem() {
        MiniMessage miniMessage = McRPG.getInstance().getMiniMessage();
        ItemStack itemStack = new ItemStack(isLoadoutActive() ? Material.GREEN_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.displayName(miniMessage.deserialize("<red>Loadout Active State"));
        List<Component> lore = new ArrayList<>();
        lore.add(miniMessage.deserialize("<gray>This loadout is currently " + (isLoadoutActive() ? "<green>active</green>" : "<red>inactive</red>") + "."));
        if (!isLoadoutActive()) {
            lore.add(miniMessage.deserialize("<gray>Click to set this loadout as your active loadout."));
        }
        itemMeta.lore(lore);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    @Override
    public Set<Class<? extends Gui>> getValidGuiTypes() {
        return Set.of(LoadoutGui.class);
    }

    @Override
    public boolean onClick(@NotNull CorePlayer corePlayer, @NotNull ClickType clickType) {
        var guiOptional = CorePlugin.getInstance().getGuiTracker().getOpenedGui(corePlayer);
        guiOptional.ifPresent(gui -> {
            if (!isLoadoutActive()) {
                player.performCommand("loadout set " + loadout.getLoadoutSlot());
            }
            gui.refreshGUI();
        });
        return true;
    }

    /**
     * Checks to see if the {@link Loadout} is the active one for the player.
     * @return {@code true} if the {@link Loadout} is the active one for the player.
     */
    public boolean isLoadoutActive() {
        return mcRPGPlayer.asSkillHolder().getCurrentLoadoutSlot() == loadout.getLoadoutSlot();
    }
}
