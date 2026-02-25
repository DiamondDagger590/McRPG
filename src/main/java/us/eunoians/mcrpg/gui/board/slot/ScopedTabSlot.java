package us.eunoians.mcrpg.gui.board.slot;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.board.BoardGuiMode;
import us.eunoians.mcrpg.gui.board.QuestBoardGui;
import us.eunoians.mcrpg.gui.board.ScopedEntitySelectorGui;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
import us.eunoians.mcrpg.quest.board.scope.ScopedBoardAdapter;
import us.eunoians.mcrpg.quest.board.scope.ScopedBoardAdapterRegistry;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Navigation bar slot that switches the quest board to scoped (group) mode.
 * <p>
 * If the player has management permissions in exactly one scope entity (across
 * all adapters), clicking directly opens the board in scoped mode (fast path).
 * If multiple entities are manageable, opens the {@link ScopedEntitySelectorGui}.
 */
public class ScopedTabSlot implements McRPGSlot {

    @Override
    public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
        mcRPGPlayer.getAsBukkitPlayer().ifPresent(player -> {
            List<ManageableEntity> entities = collectManageableEntities(mcRPGPlayer.getUUID());

            if (entities.size() == 1) {
                QuestBoardGui gui = new QuestBoardGui(mcRPGPlayer, BoardGuiMode.SCOPED);
                RegistryAccess.registryAccess()
                        .registry(RegistryKey.MANAGER)
                        .manager(McRPGManagerKey.GUI)
                        .trackPlayerGui(player, gui);
                player.openInventory(gui.getInventory());
            } else {
                ScopedEntitySelectorGui gui = new ScopedEntitySelectorGui(mcRPGPlayer);
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
        return ItemBuilder.from(new ItemStack(Material.COMPASS))
                .setDisplayName("Group Quests");
    }

    @NotNull
    @Override
    public Set<Class<?>> getValidGuiTypes() {
        return Set.of(QuestBoardGui.class);
    }

    @NotNull
    private static List<ManageableEntity> collectManageableEntities(@NotNull java.util.UUID playerUUID) {
        ScopedBoardAdapterRegistry registry = McRPG.getInstance().registryAccess()
                .registry(McRPGRegistryKey.SCOPED_BOARD_ADAPTER);

        List<ManageableEntity> result = new ArrayList<>();
        for (ScopedBoardAdapter adapter : registry.getAll()) {
            for (String entityId : adapter.getManageableEntities(playerUUID)) {
                result.add(new ManageableEntity(adapter.getScopeProviderKey(), entityId));
            }
        }
        return result;
    }

    private record ManageableEntity(@NotNull NamespacedKey scopeKey, @NotNull String entityId) {}
}
