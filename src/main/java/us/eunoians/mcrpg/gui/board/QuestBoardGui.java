package us.eunoians.mcrpg.gui.board;

import com.diamonddagger590.mccore.gui.slot.Slot;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.board.slot.BoardBackSlot;
import us.eunoians.mcrpg.gui.board.slot.BoardOfferingSlot;
import us.eunoians.mcrpg.gui.common.McRPGPaginatedGui;
import us.eunoians.mcrpg.quest.board.BoardOffering;
import us.eunoians.mcrpg.quest.board.QuestBoardManager;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.util.McRPGMethods;

import org.bukkit.NamespacedKey;

import java.util.List;

/**
 * Main quest board GUI. Phase 1 shows shared offerings only.
 */
public class QuestBoardGui extends McRPGPaginatedGui {

    private static final NamespacedKey DEFAULT_BOARD_KEY =
            new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "default_board");
    private static final int ROWS = 6;
    private static final int SLOTS_PER_PAGE = 45;

    private final List<BoardOffering> offerings;

    public QuestBoardGui(@NotNull McRPGPlayer player) {
        super(player);
        QuestBoardManager boardManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.QUEST_BOARD);
        this.offerings = boardManager.getSharedOfferingsForBoard(DEFAULT_BOARD_KEY).stream()
                .filter(o -> o.getState() == BoardOffering.State.VISIBLE)
                .toList();
    }

    @NotNull
    @Override
    protected Inventory getInventoryForPage(int page) {
        return Bukkit.createInventory(null, ROWS * 9,
                RegistryAccess.registryAccess()
                        .registry(RegistryKey.MANAGER)
                        .manager(McRPGManagerKey.LOCALIZATION)
                        .getLocalizedMessageAsComponent(getCreatingPlayer(), LocalizationKey.QUEST_BOARD_GUI_TITLE));
    }

    @Override
    protected void paintInventoryForPage(@NotNull Inventory inventory, int page) {
        Slot<McRPGPlayer> filler = getFillerItemSlot();
        for (int i = 0; i < inventory.getSize(); i++) {
            setSlot(i, filler);
        }

        int startIndex = page * SLOTS_PER_PAGE;
        int endIndex = Math.min(startIndex + SLOTS_PER_PAGE, offerings.size());
        for (int i = startIndex; i < endIndex; i++) {
            setSlot(i - startIndex, new BoardOfferingSlot(offerings.get(i)));
        }

        // Navigation bar (row 6)
        int navRowStart = 45;
        setSlot(navRowStart, new BoardBackSlot());
        if (page > 0) {
            setSlot(navRowStart + 3, getPreviousPageSlot());
        }
        if (endIndex < offerings.size()) {
            setSlot(navRowStart + 5, getNextPageSlot());
        }
    }

    @Override
    public int getMaximumPage() {
        return Math.max(1, (int) Math.ceil((double) offerings.size() / SLOTS_PER_PAGE));
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
