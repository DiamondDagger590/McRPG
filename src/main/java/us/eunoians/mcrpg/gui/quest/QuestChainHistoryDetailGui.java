package us.eunoians.mcrpg.gui.quest;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.exception.CorePlayerOfflineException;
import com.diamonddagger590.mccore.gui.KeyedGui;
import com.diamonddagger590.mccore.gui.slot.Slot;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.database.table.quest.QuestChainCompletionLogDAO;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.common.McRPGPaginatedGui;
import us.eunoians.mcrpg.gui.common.slot.McRPGPreviousGuiSlot;
import us.eunoians.mcrpg.gui.quest.slot.ChainStepCompletionSlot;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.quest.chain.QuestChainDefinition;
import us.eunoians.mcrpg.quest.chain.QuestChainRegistry;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.util.McRPGMethods;


import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;

/**
 * Paginated sub-GUI showing individual quest step completions within a specific chain run.
 * Opened when a player clicks a chain entry in {@link QuestHistoryGui}. Has a back
 * button returning to the history GUI.
 */
public class QuestChainHistoryDetailGui extends McRPGPaginatedGui implements KeyedGui {

    public static final NamespacedKey GUI_KEY =
            new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "chain_history_detail");

    private static final int NAVIGATION_ROW_START_INDEX = 45;
    private static final int PREVIOUS_GUI_SLOT_INDEX = NAVIGATION_ROW_START_INDEX;
    private static final int PREVIOUS_PAGE_SLOT_INDEX = NAVIGATION_ROW_START_INDEX + 2;
    private static final int NEXT_PAGE_SLOT_INDEX = NAVIGATION_ROW_START_INDEX + 6;

    private final Player player;
    private final NamespacedKey chainKey;
    private final int completionNumber;
    private List<QuestChainCompletionLogDAO.ChainStepRecord> stepRecords;

    public QuestChainHistoryDetailGui(@NotNull McRPGPlayer mcRPGPlayer,
                                      @NotNull NamespacedKey chainKey,
                                      int completionNumber) {
        super(mcRPGPlayer);
        this.player = mcRPGPlayer.getAsBukkitPlayer()
                .orElseThrow(() -> new CorePlayerOfflineException(mcRPGPlayer));
        this.chainKey = chainKey;
        this.completionNumber = completionNumber;
        this.stepRecords = new ArrayList<>();
        loadStepRecords();
    }

    /**
     * Loads chain step completion records from the database asynchronously and refreshes the GUI.
     */
    private void loadStepRecords() {
        Database database = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.DATABASE).getDatabase();
        database.getDatabaseExecutorService().submit(() -> {
            List<QuestChainCompletionLogDAO.ChainStepRecord> steps;
            try (Connection connection = database.getConnection()) {
                steps = QuestChainCompletionLogDAO.getStepsForRun(
                        connection, getCreatingPlayer().getUUID(), chainKey.toString(), completionNumber);
            } catch (SQLException e) {
                McRPG.getInstance().getLogger().log(Level.SEVERE,
                        "Failed to load chain step records for player " + getCreatingPlayer().getUUID(), e);
                steps = new ArrayList<>();
            }
            List<QuestChainCompletionLogDAO.ChainStepRecord> finalSteps = steps;
            Bukkit.getScheduler().runTask(McRPG.getInstance(), () -> {
                stepRecords = finalSteps;
                refreshGUI();
            });
        });
    }

    @Override
    @NotNull
    protected Inventory getInventoryForPage(int page) {
        QuestChainRegistry chainRegistry = RegistryAccess.registryAccess()
                .registry(McRPGRegistryKey.QUEST_CHAIN);
        Optional<QuestChainDefinition> definitionOpt = chainRegistry.get(chainKey);
        String chainName = definitionOpt.map(QuestChainDefinition::getDisplayName)
                .orElse(chainKey.getKey());

        McRPGLocalizationManager localizationManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
        return Bukkit.createInventory(player, 54,
                localizationManager.getLocalizedMessageAsComponent(
                        getCreatingPlayer(), LocalizationKey.QUEST_CHAIN_HISTORY_GUI_TITLE,
                        Map.of("chain", chainName)));
    }

    @Override
    protected void paintInventoryForPage(@NotNull Inventory inventory, int page) {
        paintNavigationBar(page);
        paintStepSlots(page);
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
    }

    private void paintStepSlots(int page) {
        List<QuestChainCompletionLogDAO.ChainStepRecord> pageRecords = getRecordsForPage(page);
        for (int i = 0; i < NAVIGATION_ROW_START_INDEX; i++) {
            if (i < pageRecords.size()) {
                // stepNumber is global 1-based index, not page-relative
                int globalIndex = (page - 1) * NAVIGATION_ROW_START_INDEX + i;
                setSlot(i, new ChainStepCompletionSlot(pageRecords.get(i), globalIndex + 1));
            } else {
                removeSlot(i);
            }
        }
    }

    @NotNull
    private List<QuestChainCompletionLogDAO.ChainStepRecord> getRecordsForPage(int page) {
        int start = (page - 1) * NAVIGATION_ROW_START_INDEX;
        int end = Math.min(start + NAVIGATION_ROW_START_INDEX, stepRecords.size());
        if (start >= stepRecords.size()) {
            return List.of();
        }
        return stepRecords.subList(start, end);
    }

    @Override
    public int getMaximumPage() {
        return Math.max(1, (int) Math.ceil((double) stepRecords.size() / NAVIGATION_ROW_START_INDEX));
    }

    /**
     * Creates the back button slot that navigates to {@link QuestHistoryGui}.
     *
     * @return the back button slot
     */
    @NotNull
    public McRPGPreviousGuiSlot getPreviousGuiSlot() {
        return new McRPGPreviousGuiSlot() {
            @Override
            public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
                mcRPGPlayer.getAsBukkitPlayer().ifPresent(p -> {
                    QuestHistoryGui historyGui = new QuestHistoryGui(mcRPGPlayer);
                    McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER)
                            .manager(McRPGManagerKey.GUI).trackPlayerGui(p, historyGui);
                    p.openInventory(historyGui.getInventory());
                });
                return true;
            }

            @Override
            @NotNull
            public Route getSpecificDisplayItemRoute() {
                return LocalizationKey.QUEST_CHAIN_HISTORY_GUI_PREVIOUS_GUI_BUTTON_DISPLAY_ITEM;
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
