package us.eunoians.mcrpg.gui.quest.slot;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.quest.QuestHistoryGui;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Set;

/**
 * Toggle slot in the {@link QuestHistoryGui} that cycles between date ascending and descending sort.
 */
public class QuestHistorySortSlot implements McRPGSlot {

    private final QuestHistoryGui historyGui;

    public QuestHistorySortSlot(@NotNull QuestHistoryGui historyGui) {
        this.historyGui = historyGui;
    }

    @Override
    public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
        historyGui.toggleSort();
        return true;
    }

    @NotNull
    @Override
    public ItemBuilder getItem(@NotNull McRPGPlayer mcRPGPlayer) {
        var route = historyGui.isSortAscending()
                ? LocalizationKey.QUEST_HISTORY_GUI_SORT_DATE_ASC_DISPLAY_ITEM
                : LocalizationKey.QUEST_HISTORY_GUI_SORT_DATE_DESC_DISPLAY_ITEM;
        return ItemBuilder.from(RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION)
                .getLocalizedSection(mcRPGPlayer, route));
    }

    @NotNull
    @Override
    public Set<Class<?>> getValidGuiTypes() {
        return Set.of(QuestHistoryGui.class);
    }
}
