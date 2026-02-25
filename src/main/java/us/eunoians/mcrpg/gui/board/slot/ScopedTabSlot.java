package us.eunoians.mcrpg.gui.board.slot;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.board.BoardGuiMode;
import us.eunoians.mcrpg.gui.board.QuestBoardGui;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Set;

/**
 * Navigation bar slot that switches the quest board to scoped (group) mode.
 * Displayed as "Group Quests" to players.
 */
public class ScopedTabSlot implements McRPGSlot {

    @Override
    public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
        mcRPGPlayer.getAsBukkitPlayer().ifPresent(player -> {
            QuestBoardGui gui = new QuestBoardGui(mcRPGPlayer, BoardGuiMode.SCOPED);
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
        return ItemBuilder.from(new ItemStack(Material.COMPASS))
                .setDisplayName("Group Quests");
    }

    @NotNull
    @Override
    public Set<Class<?>> getValidGuiTypes() {
        return Set.of(QuestBoardGui.class);
    }
}
