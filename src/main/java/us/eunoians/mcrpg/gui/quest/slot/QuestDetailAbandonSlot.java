package us.eunoians.mcrpg.gui.quest.slot;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.quest.QuestAbandonConfirmGui;
import us.eunoians.mcrpg.gui.quest.QuestDetailGui;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Set;

/**
 * Abandon button in the {@link QuestDetailGui} navigation bar.
 * Opens the {@link QuestAbandonConfirmGui}.
 */
public class QuestDetailAbandonSlot implements McRPGSlot {

    private final QuestInstance questInstance;
    private final String questDisplayName;

    public QuestDetailAbandonSlot(@NotNull QuestInstance questInstance, @NotNull String questDisplayName) {
        this.questInstance = questInstance;
        this.questDisplayName = questDisplayName;
    }

    @Override
    public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
        mcRPGPlayer.getAsBukkitPlayer().ifPresent(player -> {
            QuestAbandonConfirmGui confirmGui = new QuestAbandonConfirmGui(
                    mcRPGPlayer, questInstance, questDisplayName, true);
            McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER)
                    .manager(McRPGManagerKey.GUI).trackPlayerGui(player, confirmGui);
            player.openInventory(confirmGui.getInventory());
        });
        return true;
    }

    @NotNull
    @Override
    public ItemBuilder getItem(@NotNull McRPGPlayer mcRPGPlayer) {
        return ItemBuilder.from(RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION)
                .getLocalizedSection(mcRPGPlayer, LocalizationKey.QUEST_DETAIL_GUI_ABANDON_BUTTON_DISPLAY_ITEM));
    }

    @NotNull
    @Override
    public Set<Class<?>> getValidGuiTypes() {
        return Set.of(QuestDetailGui.class);
    }
}
