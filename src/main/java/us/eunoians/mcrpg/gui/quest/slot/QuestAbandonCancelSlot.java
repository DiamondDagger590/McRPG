package us.eunoians.mcrpg.gui.quest.slot;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.quest.ActiveQuestGui;
import us.eunoians.mcrpg.gui.quest.QuestAbandonConfirmGui;
import us.eunoians.mcrpg.gui.quest.QuestDetailGui;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Set;

/**
 * Cancel button in the {@link QuestAbandonConfirmGui}.
 * Returns the player to the previous GUI without abandoning the quest.
 */
public class QuestAbandonCancelSlot implements McRPGSlot {

    private final QuestInstance questInstance;
    private final boolean fromDetail;

    /**
     * @param questInstance the quest being considered for abandonment
     * @param fromDetail    if true, returns to QuestDetailGui; otherwise returns to ActiveQuestGui
     */
    public QuestAbandonCancelSlot(@NotNull QuestInstance questInstance, boolean fromDetail) {
        this.questInstance = questInstance;
        this.fromDetail = fromDetail;
    }

    @Override
    public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
        mcRPGPlayer.getAsBukkitPlayer().ifPresent(player -> {
            if (fromDetail) {
                QuestDetailGui detailGui = QuestDetailGui.forActiveQuest(mcRPGPlayer, questInstance);
                McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER)
                        .manager(McRPGManagerKey.GUI).trackPlayerGui(player, detailGui);
                player.openInventory(detailGui.getInventory());
            } else {
                ActiveQuestGui activeGui = new ActiveQuestGui(mcRPGPlayer);
                McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER)
                        .manager(McRPGManagerKey.GUI).trackPlayerGui(player, activeGui);
                player.openInventory(activeGui.getInventory());
            }
        });
        return true;
    }

    @NotNull
    @Override
    public ItemBuilder getItem(@NotNull McRPGPlayer mcRPGPlayer) {
        return ItemBuilder.from(RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION)
                .getLocalizedSection(mcRPGPlayer, LocalizationKey.QUEST_ABANDON_CONFIRM_GUI_CANCEL_BUTTON_DISPLAY_ITEM));
    }

    @NotNull
    @Override
    public Set<Class<?>> getValidGuiTypes() {
        return Set.of(QuestAbandonConfirmGui.class);
    }
}
