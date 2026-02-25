package us.eunoians.mcrpg.gui.board.slot;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.board.QuestBoardGui;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Set;

/**
 * Back button in scoped (group) mode that returns to the shared/personal board view.
 */
public class ScopedBackSlot implements McRPGSlot {

    @Override
    public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
        QuestBoardGui gui = new QuestBoardGui(mcRPGPlayer);
        mcRPGPlayer.getAsBukkitPlayer().ifPresent(player -> {
            RegistryAccess.registryAccess()
                    .registry(RegistryKey.MANAGER)
                    .manager(McRPGManagerKey.GUI)
                    .trackPlayerGui(player, gui);
            player.openInventory(gui.getInventory());
        });
        return true;
    }

    @NotNull
    @Override
    public ItemBuilder getItem(@NotNull McRPGPlayer mcRPGPlayer) {
        return ItemBuilder.from(new ItemStack(Material.ARROW))
                .setDisplayName("Back");
    }

    @NotNull
    @Override
    public Set<Class<?>> getValidGuiTypes() {
        return Set.of(QuestBoardGui.class);
    }
}
