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
import us.eunoians.mcrpg.database.table.quest.ChainCompletionRun;
import us.eunoians.mcrpg.database.table.quest.CompletionRecord;
import us.eunoians.mcrpg.database.table.quest.QuestChainCompletionLogDAO;
import us.eunoians.mcrpg.database.table.quest.QuestCompletionLogDAO;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.common.McRPGPaginatedGui;
import us.eunoians.mcrpg.gui.common.slot.McRPGPreviousGuiSlot;
import us.eunoians.mcrpg.gui.quest.slot.CompletedQuestSlot;
import us.eunoians.mcrpg.gui.quest.slot.QuestChainHistorySlot;
import us.eunoians.mcrpg.gui.quest.slot.QuestHistoryEmptySlot;
import us.eunoians.mcrpg.gui.quest.slot.QuestHistorySortSlot;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import com.diamonddagger590.mccore.gui.KeyedGui;
import org.bukkit.NamespacedKey;
import us.eunoians.mcrpg.util.McRPGMethods;

/**
 * Paginated GUI displaying a player's completed quest history with sort toggling.
 */
public class QuestHistoryGui extends McRPGPaginatedGui implements KeyedGui {

    public static final NamespacedKey GUI_KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "quest_history");

    private static final int NAVIGATION_ROW_START_INDEX = 45;
    private static final int PREVIOUS_GUI_SLOT_INDEX = NAVIGATION_ROW_START_INDEX;
    private static final int PREVIOUS_PAGE_SLOT_INDEX = NAVIGATION_ROW_START_INDEX + 2;
    private static final int SORT_SLOT_INDEX = NAVIGATION_ROW_START_INDEX + 4;
    private static final int NEXT_PAGE_SLOT_INDEX = NAVIGATION_ROW_START_INDEX + 6;

    private final Player player;
    private boolean sortAscending = false;
    /**
     * Set to {@code true} when the first async load completes. Until then no empty-state
     * slot is shown because the list being empty only means loading hasn't finished yet.
     */
    private boolean loaded = false;

    /**
     * Monotonically increasing counter used to discard stale async load results.
     * Incremented on every {@link #loadCompletionRecords()} call; captured in the async
     * lambda and compared against the current value before applying results on the main
     * thread. Stale results (from a previous load that completed after a newer one
     * started) are discarded if the captured generation differs from the current value.
     */
    private int loadGeneration = 0;

    /**
     * Merged display list combining {@link QuestChainHistorySlot} entries for chain runs and
     * {@link CompletedQuestSlot} entries for standalone quests, sorted by completion timestamp.
     */
    private List<McRPGSlot> displayItems;

    public QuestHistoryGui(@NotNull McRPGPlayer mcRPGPlayer) {
        super(mcRPGPlayer);
        this.player = mcRPGPlayer.getAsBukkitPlayer()
                .orElseThrow(() -> new CorePlayerOfflineException(mcRPGPlayer));
        this.displayItems = new ArrayList<>();
        loadCompletionRecords();
    }

    /**
     * Submits two DB queries on the database executor thread — one for individual quest completions
     * and one for chain completion runs — then merges the results on the main thread and refreshes.
     * <p>
     * Quests that appear in a chain completion run are excluded from the individual slot list so
     * they are represented by the chain slot instead. The {@code sortAscending} flag is captured
     * at submission time to avoid races with a rapid sort toggle.
     */
    private void loadCompletionRecords() {
        boolean ascending = sortAscending;
        int generation = ++loadGeneration;
        Database database = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.DATABASE).getDatabase();
        database.getDatabaseExecutorService().submit(() -> {
            List<CompletionRecord> questRecords;
            List<ChainCompletionRun> chainRuns;
            Set<String> chainQuestKeys;
            try (Connection connection = database.getConnection()) {
                questRecords = QuestCompletionLogDAO.getCompletionHistory(
                        connection, getCreatingPlayer().getUUID(), ascending);
                chainRuns = QuestChainCompletionLogDAO.getChainCompletionRuns(
                        connection, getCreatingPlayer().getUUID());
                chainQuestKeys = QuestChainCompletionLogDAO.getChainParticipantQuestKeys(
                        connection, getCreatingPlayer().getUUID());
            } catch (SQLException e) {
                McRPG.getInstance().getLogger().log(Level.SEVERE,
                        "Failed to load quest completion history for player " + getCreatingPlayer().getUUID(), e);
                questRecords = new ArrayList<>();
                chainRuns = new ArrayList<>();
                chainQuestKeys = Set.of();
            }

            List<McRPGSlot> merged = buildDisplayItems(questRecords, chainRuns, chainQuestKeys, ascending);
            Bukkit.getScheduler().runTask(McRPG.getInstance(), () -> {
                if (generation != loadGeneration) {
                    return;
                }
                displayItems = merged;
                loaded = true;
                refreshGUI();
            });
        });
    }

    /**
     * Merges quest completion records and chain completion runs into a unified display list.
     * Chain-managed quest entries are excluded from the individual quest list.
     *
     * @param questRecords   all individual quest completions from the log
     * @param chainRuns      all chain completion run summaries
     * @param chainQuestKeys the set of quest definition keys that belong to any chain completion
     * @param ascending      sort direction
     * @return merged sorted list of display slots
     */
    @NotNull
    private List<McRPGSlot> buildDisplayItems(@NotNull List<CompletionRecord> questRecords,
                                               @NotNull List<ChainCompletionRun> chainRuns,
                                               @NotNull Set<String> chainQuestKeys,
                                               boolean ascending) {
        record TimestampedSlot(McRPGSlot slot, long timestamp) {
        }

        List<TimestampedSlot> entries = new ArrayList<>();

        for (CompletionRecord record : questRecords) {
            if (!chainQuestKeys.contains(record.definitionKey())) {
                entries.add(new TimestampedSlot(new CompletedQuestSlot(record), record.completedAt()));
            }
        }
        for (ChainCompletionRun run : chainRuns) {
            entries.add(new TimestampedSlot(new QuestChainHistorySlot(run), run.completedAt()));
        }

        Comparator<TimestampedSlot> comparator = ascending
                ? Comparator.comparingLong(TimestampedSlot::timestamp)
                : Comparator.comparingLong(TimestampedSlot::timestamp).reversed();
        entries.sort(comparator);

        return entries.stream().map(TimestampedSlot::slot).toList();
    }

    public void toggleSort() {
        sortAscending = !sortAscending;
        setPage(1);
        loadCompletionRecords();
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
        List<McRPGSlot> pageSlots = getSlotsForPage(page);
        if (loaded && displayItems.isEmpty()) {
            for (int i = 0; i < NAVIGATION_ROW_START_INDEX; i++) {
                removeSlot(i);
            }
            setSlot(22, new QuestHistoryEmptySlot());
            return;
        }
        for (int i = 0; i < NAVIGATION_ROW_START_INDEX; i++) {
            if (i < pageSlots.size()) {
                setSlot(i, pageSlots.get(i));
            } else {
                removeSlot(i);
            }
        }
    }

    @NotNull
    private List<McRPGSlot> getSlotsForPage(int page) {
        int start = (page - 1) * NAVIGATION_ROW_START_INDEX;
        int end = Math.min(start + NAVIGATION_ROW_START_INDEX, displayItems.size());
        if (start >= displayItems.size()) {
            return List.of();
        }
        return displayItems.subList(start, end);
    }

    @Override
    public int getMaximumPage() {
        return Math.max(1, (int) Math.ceil((double) displayItems.size() / NAVIGATION_ROW_START_INDEX));
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

    @Override
    @NotNull
    public Optional<NamespacedKey> getGuiKey() {
        return Optional.of(GUI_KEY);
    }
}
