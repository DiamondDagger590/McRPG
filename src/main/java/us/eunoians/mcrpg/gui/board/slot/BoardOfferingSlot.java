package us.eunoians.mcrpg.gui.board.slot;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.board.QuestBoardGui;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
import us.eunoians.mcrpg.quest.board.BoardOffering;
import us.eunoians.mcrpg.quest.board.QuestBoardManager;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Set;

/**
 * GUI slot displaying a single board offering. Clicking triggers quest acceptance.
 */
public class BoardOfferingSlot implements McRPGSlot {

    private final BoardOffering offering;

    public BoardOfferingSlot(@NotNull BoardOffering offering) {
        this.offering = offering;
    }

    @Override
    public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
        mcRPGPlayer.getAsBukkitPlayer().ifPresent(player -> {
            QuestBoardManager boardManager = RegistryAccess.registryAccess()
                    .registry(RegistryKey.MANAGER)
                    .manager(McRPGManagerKey.QUEST_BOARD);
            boardManager.acceptOffering(player, offering.getOfferingId());
        });
        return true;
    }

    @NotNull
    @Override
    public ItemBuilder getItem(@NotNull McRPGPlayer mcRPGPlayer) {
        // Phase 1: Basic item representation
        return ItemBuilder.from(new org.bukkit.inventory.ItemStack(Material.PAPER))
                .setDisplayName(offering.getQuestDefinitionKey().getKey()
                        .replace('_', ' '))
                .addDisplayLore("Rarity: " + offering.getRarityKey().getKey())
                .addDisplayLore("Category: " + offering.getCategoryKey().getKey())
                .addDisplayLore("Click to accept");
    }

    @NotNull
    @Override
    public Set<Class<?>> getValidGuiTypes() {
        return Set.of(QuestBoardGui.class);
    }
}
