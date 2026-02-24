package us.eunoians.mcrpg.gui.quest;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.exception.CorePlayerOfflineException;
import com.diamonddagger590.mccore.gui.slot.Slot;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.database.table.quest.CompletionRecord;
import us.eunoians.mcrpg.database.table.quest.QuestCompletionLogDAO;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.common.McRPGPaginatedGui;
import us.eunoians.mcrpg.gui.common.slot.McRPGPreviousGuiSlot;
import us.eunoians.mcrpg.gui.quest.slot.CompletedQuestSlot;
import us.eunoians.mcrpg.gui.quest.slot.QuestHistorySortSlot;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Paginated GUI displaying a player's completed quest history with sort toggling.
 */
public class QuestHistoryGui extends McRPGPaginatedGui {

    private static final int NAVIGATION_ROW_START_INDEX = 45;
    private static final int PREVIOUS_GUI_SLOT_INDEX = NAVIGATION_ROW_START_INDEX;
    private static final int PREVIOUS_PAGE_SLOT_INDEX = NAVIGATION_ROW_START_INDEX + 2;
    private static final int SORT_SLOT_INDEX = NAVIGATION_ROW_START_INDEX + 4;
    private static final int NEXT_PAGE_SLOT_INDEX = NAVIGATION_ROW_START_INDEX + 6;

    private final Player player;
    private boolean sortAscending = false;
    private List<CompletionRecord> completionRecords;

    public QuestHistoryGui(@NotNull McRPGPlayer mcRPGPlayer) {
        super(mcRPGPlayer);
        this.player = mcRPGPlayer.getAsBukkitPlayer()
                .orElseThrow(() -> new CorePlayerOfflineException(mcRPGPlayer));
        loadCompletionRecords();
    }

    private void loadCompletionRecords() {
        Database database = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.DATABASE).getDatabase();
        try (Connection connection = database.getConnection()) {
            completionRecords = QuestCompletionLogDAO.getCompletionHistory(
                    connection, getCreatingPlayer().getUUID(), sortAscending);
        } catch (SQLException e) {
            e.printStackTrace();
            completionRecords = new ArrayList<>();
        }
    }

    public void toggleSort() {
        sortAscending = !sortAscending;
        loadCompletionRecords();
        setPage(1);
    }

    public boolean isSortAscending() {
        return sortAscending;
    }

    @NotNull
    @Override
    protected Inventory getInventoryForPage(int page) {
        return Bukkit.createInventory(player, 54,
                RegistryAccess.registryAccess()
                        .registry(RegistryKey.MANAGER)
                        .manager(McRPGManagerKey.LOCALIZATION)
                        .getLocalizedMessageAsComponent(getCreatingPlayer(), LocalizationKey.QUEST_HISTORY_GUI_TITLE));
    }

    @Override
    protected void paintInventoryForPage(@NotNull Inventory inventory, int page) {
        paintNavigationBar(page);
        paintCompletedQuests(page);
    }

    private void paintNavigationBar(int page) {
        Slot<McRPGPlayer> fillerSlot = getFillerItemSlot();
        for (int i = 0; i < 9; i++) {
            setSlot(NAVIGATION_ROW_START_INDEX + i, fillerSlot);
        }
        if (page > 1) {
            setSlot(PREVIOUS_PAGE_SLOT_INDEX, getPreviousPageSlot());
        }
        if (page < getMaximumPage()) {
            setSlot(NEXT_PAGE_SLOT_INDEX, getNextPageSlot());
        }
        setSlot(PREVIOUS_GUI_SLOT_INDEX, getPreviousGuiSlot());
        setSlot(SORT_SLOT_INDEX, new QuestHistorySortSlot(this));
    }

    private void paintCompletedQuests(int page) {
        List<CompletionRecord> pageRecords = getRecordsForPage(page);
        for (int i = 0; i < NAVIGATION_ROW_START_INDEX; i++) {
            if (i < pageRecords.size()) {
                setSlot(i, new CompletedQuestSlot(pageRecords.get(i)));
            } else {
                removeSlot(i);
            }
        }
    }

    @NotNull
    private List<CompletionRecord> getRecordsForPage(int page) {
        int start = (page - 1) * NAVIGATION_ROW_START_INDEX;
        int end = Math.min(start + NAVIGATION_ROW_START_INDEX, completionRecords.size());
        if (start >= completionRecords.size()) {
            return List.of();
        }
        return completionRecords.subList(start, end);
    }

    @Override
    public int getMaximumPage() {
        return Math.max(1, (int) Math.ceil((double) completionRecords.size() / NAVIGATION_ROW_START_INDEX));
    }

    @NotNull
    public McRPGPreviousGuiSlot getPreviousGuiSlot() {
        return new McRPGPreviousGuiSlot() {
            @Override
            public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
                mcRPGPlayer.getAsBukkitPlayer().ifPresent(player -> {
                    ActiveQuestGui activeQuestGui = new ActiveQuestGui(mcRPGPlayer);
                    McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER)
                            .manager(McRPGManagerKey.GUI).trackPlayerGui(player, activeQuestGui);
                    player.openInventory(activeQuestGui.getInventory());
                });
                return true;
            }

            @NotNull
            @Override
            public Route getSpecificDisplayItemRoute() {
                return LocalizationKey.QUEST_HISTORY_GUI_PREVIOUS_GUI_BUTTON_DISPLAY_ITEM;
            }
        };
    }

    @Override
    public void registerListeners() {
        Bukkit.getPluginManager().registerEvents(this, McRPG.getInstance());
    }

    @Override
    public void unregisterListeners() {
        InventoryClickEvent.getHandlerList().unregister(this);
    }
}
