package us.eunoians.mcrpg.gui.board.slot;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.board.QuestBoardGui;
import us.eunoians.mcrpg.gui.board.ScopedEntitySelectorGui;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
import us.eunoians.mcrpg.quest.board.scope.ScopedBoardAdapter;
import us.eunoians.mcrpg.quest.board.scope.ScopedBoardAdapterRegistry;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Set;

/**
 * Back button in scoped (group) mode. If the player manages multiple entities,
 * navigates back to the {@link ScopedEntitySelectorGui}. Otherwise returns
 * to the shared/personal board view.
 */
public class ScopedBackSlot implements McRPGSlot {

    @Override
    public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
        mcRPGPlayer.getAsBukkitPlayer().ifPresent(player -> {
            int manageableCount = countManageableEntities(mcRPGPlayer.getUUID());

            if (manageableCount > 1) {
                ScopedEntitySelectorGui gui = new ScopedEntitySelectorGui(mcRPGPlayer);
                RegistryAccess.registryAccess()
                        .registry(RegistryKey.MANAGER)
                        .manager(McRPGManagerKey.GUI)
                        .trackPlayerGui(player, gui);
                player.openInventory(gui.getInventory());
            } else {
                QuestBoardGui gui = new QuestBoardGui(mcRPGPlayer);
                RegistryAccess.registryAccess()
                        .registry(RegistryKey.MANAGER)
                        .manager(McRPGManagerKey.GUI)
                        .trackPlayerGui(player, gui);
                player.openInventory(gui.getInventory());
            }
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

    private static int countManageableEntities(@NotNull java.util.UUID playerUUID) {
        ScopedBoardAdapterRegistry registry = McRPG.getInstance().registryAccess()
                .registry(McRPGRegistryKey.SCOPED_BOARD_ADAPTER);

        int count = 0;
        for (ScopedBoardAdapter adapter : registry.getAll()) {
            count += adapter.getManageableEntities(playerUUID).size();
        }
        return count;
    }
}
