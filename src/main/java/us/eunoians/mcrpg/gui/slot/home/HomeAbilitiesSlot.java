package us.eunoians.mcrpg.gui.slot.home;

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
import us.eunoians.mcrpg.gui.HomeGui;
import us.eunoians.mcrpg.gui.ability.AbilityGui;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;

import java.util.List;
import java.util.Set;

/**
 * This slot is used in the {@link HomeGui} to open a new {@link AbilityGui} when clicked.
 */
public class HomeAbilitiesSlot extends McRPGSlot {

    @Override
    public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
        AbilityGui abilityGui = new AbilityGui(mcRPGPlayer);
        mcRPGPlayer.getAsBukkitPlayer().ifPresent(player -> {
            McRPG.getInstance().getGuiTracker().trackPlayerGui(player, abilityGui);
            player.openInventory(abilityGui.getInventory());
        });
        return true;
    }

    @NotNull
    @Override
    public ItemBuilder getItem(@Nullable McRPGPlayer mcRPGPlayer) {
        MiniMessage miniMessage = McRPG.getInstance().getMiniMessage();
        ItemStack itemStack = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.displayName(miniMessage.deserialize("<red>Abilities"));
        itemMeta.lore(List.of(miniMessage.deserialize("<gray>Click to view abilities.")));
        itemStack.setItemMeta(itemMeta);
        return ItemBuilder.from(itemStack);
    }

    @Override
    public Set<Class<?>> getValidGuiTypes() {
        return Set.of(HomeGui.class);
    }
}
