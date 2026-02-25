package us.eunoians.mcrpg.gui.board.slot;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.board.BoardGuiMode;
import us.eunoians.mcrpg.gui.board.QuestBoardGui;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
import us.eunoians.mcrpg.quest.board.BoardOffering;
import us.eunoians.mcrpg.quest.board.QuestBoardManager;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * GUI slot for a scoped board offering. Displays the offering with its entity context
 * (shown as "Group: {name}" to players). Permission-gated: managers can accept,
 * non-managers see an informational message.
 */
public class ScopedOfferingSlot implements McRPGSlot {

    private final BoardOffering offering;
    private final String entityId;
    private final NamespacedKey scopeProviderKey;
    private final String entityDisplayName;
    private final boolean canManage;
    private volatile boolean accepting;

    public ScopedOfferingSlot(@NotNull BoardOffering offering,
                              @NotNull String entityId,
                              @NotNull NamespacedKey scopeProviderKey,
                              @NotNull String entityDisplayName,
                              boolean canManage) {
        this.offering = offering;
        this.entityId = entityId;
        this.scopeProviderKey = scopeProviderKey;
        this.entityDisplayName = entityDisplayName;
        this.canManage = canManage;
    }

    @Override
    public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
        if (!canManage) {
            mcRPGPlayer.getAsBukkitPlayer().ifPresent(player ->
                    player.sendMessage("Ask your group leader to accept this quest"));
            return true;
        }

        if (accepting) {
            return true;
        }
        accepting = true;

        mcRPGPlayer.getAsBukkitPlayer().ifPresent(player -> {
            QuestBoardManager boardManager = RegistryAccess.registryAccess()
                    .registry(RegistryKey.MANAGER)
                    .manager(McRPGManagerKey.QUEST_BOARD);

            CompletableFuture<Boolean> future = boardManager.acceptScopedOffering(
                    player, offering.getOfferingId(), entityId, scopeProviderKey);

            future.thenAccept(success -> Bukkit.getScheduler().runTask(McRPG.getInstance(), () -> {
                if (success) {
                    mcRPGPlayer.getAsBukkitPlayer().ifPresent(p -> {
                        QuestBoardGui gui = new QuestBoardGui(mcRPGPlayer,
                                BoardGuiMode.SCOPED);
                        RegistryAccess.registryAccess()
                                .registry(RegistryKey.MANAGER)
                                .manager(McRPGManagerKey.GUI)
                                .trackPlayerGui(p, gui);
                        p.openInventory(gui.getInventory());
                    });
                } else {
                    accepting = false;
                    mcRPGPlayer.getAsBukkitPlayer().ifPresent(p ->
                            p.sendMessage("Could not accept this group quest. The slot limit may have been reached."));
                }
            }));
        });
        return true;
    }

    @NotNull
    @Override
    public ItemBuilder getItem(@NotNull McRPGPlayer mcRPGPlayer) {
        ItemBuilder builder = ItemBuilder.from(new ItemStack(Material.PAPER))
                .setDisplayName(offering.getQuestDefinitionKey().getKey().replace('_', ' '))
                .addDisplayLore("Group: " + entityDisplayName)
                .addDisplayLore("Rarity: " + offering.getRarityKey().getKey())
                .addDisplayLore("Category: " + offering.getCategoryKey().getKey());

        if (canManage) {
            builder.addDisplayLore("Click to accept");
        } else {
            builder.addDisplayLore("Ask your group leader to accept");
        }

        return builder;
    }

    @NotNull
    @Override
    public Set<Class<?>> getValidGuiTypes() {
        return Set.of(QuestBoardGui.class);
    }
}
