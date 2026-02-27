package us.eunoians.mcrpg.gui.board;

import com.diamonddagger590.mccore.gui.slot.Slot;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.board.slot.BoardBackSlot;
import us.eunoians.mcrpg.gui.board.slot.BoardOfferingSlot;
import us.eunoians.mcrpg.gui.board.slot.ScopedBackSlot;
import us.eunoians.mcrpg.gui.board.slot.ScopedNoOfferingsSlot;
import us.eunoians.mcrpg.gui.board.slot.ScopedOfferingSlot;
import us.eunoians.mcrpg.gui.board.slot.ScopedTabSlot;
import us.eunoians.mcrpg.gui.common.McRPGPaginatedGui;
import us.eunoians.mcrpg.quest.board.BoardOffering;
import us.eunoians.mcrpg.quest.board.QuestBoardManager;
import us.eunoians.mcrpg.quest.board.scope.ScopedBoardAdapter;
import us.eunoians.mcrpg.quest.board.scope.ScopedBoardAdapterRegistry;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Main quest board GUI. Supports two modes:
 * <ul>
 *   <li>{@link BoardGuiMode#SHARED_AND_PERSONAL} – displays shared offerings (default)</li>
 *   <li>{@link BoardGuiMode#SCOPED} – displays group (scoped) offerings across all member entities</li>
 * </ul>
 */
public class QuestBoardGui extends McRPGPaginatedGui {

    private static final NamespacedKey DEFAULT_BOARD_KEY =
            new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "default_board");
    private static final int ROWS = 6;
    private static final int SLOTS_PER_PAGE = 45;

    private final BoardGuiMode mode;
    private final List<BoardOffering> offerings;
    private final List<ScopedOfferingEntry> scopedOfferings;
    private final boolean hasScopedEntities;

    /**
     * Opens the board in {@link BoardGuiMode#SHARED_AND_PERSONAL} mode.
     */
    public QuestBoardGui(@NotNull McRPGPlayer player) {
        super(player);
        this.mode = BoardGuiMode.SHARED_AND_PERSONAL;

        QuestBoardManager boardManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.QUEST_BOARD);
        this.offerings = boardManager.getSharedOfferingsForBoard(DEFAULT_BOARD_KEY);
        this.scopedOfferings = List.of();
        this.hasScopedEntities = checkHasScopedEntities(player.getUUID());
    }

    /**
     * Opens the board in the given mode. When {@code mode} is {@link BoardGuiMode#SCOPED},
     * loads all scoped offerings the player can see across all member entities.
     */
    public QuestBoardGui(@NotNull McRPGPlayer player, @NotNull BoardGuiMode mode) {
        super(player);
        this.mode = mode;

        QuestBoardManager boardManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.QUEST_BOARD);

        if (mode == BoardGuiMode.SCOPED) {
            this.offerings = List.of();
            this.scopedOfferings = loadScopedOfferings(player.getUUID(), boardManager);
            this.hasScopedEntities = !scopedOfferings.isEmpty();
        } else {
            this.offerings = boardManager.getSharedOfferingsForBoard(DEFAULT_BOARD_KEY);
            this.scopedOfferings = List.of();
            this.hasScopedEntities = checkHasScopedEntities(player.getUUID());
        }
    }

    /**
     * Returns only the VISIBLE offerings from the cached list. Since offering objects
     * are mutable shared references, this re-filters live state on each repaint.
     */
    @NotNull
    private List<BoardOffering> getVisibleOfferings() {
        return offerings.stream()
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
        if (mode == BoardGuiMode.SCOPED) {
            paintScopedOfferings(inventory, page);
        } else {
            paintSharedAndPersonalOfferings(inventory, page);
        }
    }

    private void paintSharedAndPersonalOfferings(@NotNull Inventory inventory, int page) {
        Slot<McRPGPlayer> filler = getFillerItemSlot();
        for (int i = 0; i < inventory.getSize(); i++) {
            setSlot(i, filler);
        }

        List<BoardOffering> visible = getVisibleOfferings();
        int zeroPage = page - 1;
        int startIndex = zeroPage * SLOTS_PER_PAGE;
        int endIndex = Math.min(startIndex + SLOTS_PER_PAGE, visible.size());
        for (int i = startIndex; i < endIndex; i++) {
            setSlot(i - startIndex, new BoardOfferingSlot(visible.get(i)));
        }

        int navRowStart = 45;
        setSlot(navRowStart, new BoardBackSlot());
        if (zeroPage > 0) {
            setSlot(navRowStart + 3, getPreviousPageSlot());
        }
        if (endIndex < visible.size()) {
            setSlot(navRowStart + 5, getNextPageSlot());
        }

        if (hasScopedEntities) {
            setSlot(navRowStart + 8, new ScopedTabSlot());
        }
    }

    private void paintScopedOfferings(@NotNull Inventory inventory, int page) {
        Slot<McRPGPlayer> filler = getFillerItemSlot();
        for (int i = 0; i < inventory.getSize(); i++) {
            setSlot(i, filler);
        }

        int zeroPage = page - 1;
        if (scopedOfferings.isEmpty()) {
            setSlot(22, new ScopedNoOfferingsSlot());
        } else {
            int startIndex = zeroPage * SLOTS_PER_PAGE;
            int endIndex = Math.min(startIndex + SLOTS_PER_PAGE, scopedOfferings.size());
            for (int i = startIndex; i < endIndex; i++) {
                ScopedOfferingEntry entry = scopedOfferings.get(i);
                setSlot(i - startIndex, new ScopedOfferingSlot(
                        entry.offering(), entry.entityId(), entry.scopeProviderKey(),
                        entry.entityDisplayName(), entry.canManage()));
            }
        }

        int navRowStart = 45;
        setSlot(navRowStart, new ScopedBackSlot());
        if (zeroPage > 0) {
            setSlot(navRowStart + 3, getPreviousPageSlot());
        }
        if (!scopedOfferings.isEmpty() && (zeroPage + 1) * SLOTS_PER_PAGE < scopedOfferings.size()) {
            setSlot(navRowStart + 5, getNextPageSlot());
        }
    }

    @Override
    public int getMaximumPage() {
        int itemCount = (mode == BoardGuiMode.SCOPED) ? scopedOfferings.size() : getVisibleOfferings().size();
        return Math.max(1, (int) Math.ceil((double) itemCount / SLOTS_PER_PAGE));
    }

    @Override
    public void registerListeners() {
        Bukkit.getPluginManager().registerEvents(this, McRPG.getInstance());
    }

    @Override
    public void unregisterListeners() {
        InventoryClickEvent.getHandlerList().unregister(this);
    }

    /**
     * Checks whether the player is a member of at least one scope entity across all
     * registered adapters. Used to determine whether the scoped tab should be rendered
     * in the navigation row.
     *
     * @param playerUUID the player to check
     * @return {@code true} if the player belongs to at least one scope entity
     */
    private boolean checkHasScopedEntities(@NotNull UUID playerUUID) {
        ScopedBoardAdapterRegistry adapterRegistry = McRPG.getInstance().registryAccess()
                .registry(McRPGRegistryKey.SCOPED_BOARD_ADAPTER);
        for (ScopedBoardAdapter adapter : adapterRegistry.getAll()) {
            if (!adapter.getMemberEntities(playerUUID).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Loads all visible scoped offerings the player can see by iterating every registered
     * {@link ScopedBoardAdapter}, resolving the player's member entities, and flattening
     * each entity's offerings into a list of {@link ScopedOfferingEntry} records. Each
     * entry carries the management permission flag so the GUI can gate accept/abandon actions.
     *
     * @param playerUUID   the player whose scoped offerings are being loaded
     * @param boardManager the board manager to query for scoped offerings
     * @return an immutable list of scoped offering entries across all member entities
     */
    @NotNull
    private List<ScopedOfferingEntry> loadScopedOfferings(@NotNull UUID playerUUID,
                                                          @NotNull QuestBoardManager boardManager) {
        Map<String, List<BoardOffering>> offeringsMap = boardManager.getScopedOfferingsForPlayer(playerUUID);
        ScopedBoardAdapterRegistry adapterRegistry = McRPG.getInstance().registryAccess()
                .registry(McRPGRegistryKey.SCOPED_BOARD_ADAPTER);

        List<ScopedOfferingEntry> entries = new ArrayList<>();
        for (ScopedBoardAdapter adapter : adapterRegistry.getAll()) {
            NamespacedKey scopeKey = adapter.getScopeProviderKey();
            Set<String> memberEntities = adapter.getMemberEntities(playerUUID);

            for (String entityId : memberEntities) {
                List<BoardOffering> entityOfferings = offeringsMap.getOrDefault(entityId, List.of());
                boolean canManage = adapter.canManageQuests(playerUUID, entityId);
                String displayName = adapter.getEntityDisplayName(entityId).orElse(entityId);

                for (BoardOffering offering : entityOfferings) {
                    entries.add(new ScopedOfferingEntry(offering, entityId, scopeKey, displayName, canManage));
                }
            }
        }
        return List.copyOf(entries);
    }

    /**
     * Flattened representation of a single scoped board offering paired with its
     * entity context and the player's management permission for that entity.
     *
     * @param offering          the board offering
     * @param entityId          the scope entity identifier (e.g., land name)
     * @param scopeProviderKey  the adapter's scope provider key
     * @param entityDisplayName the human-readable entity name
     * @param canManage         whether the player can accept/abandon quests for this entity
     */
    private record ScopedOfferingEntry(
            @NotNull BoardOffering offering,
            @NotNull String entityId,
            @NotNull NamespacedKey scopeProviderKey,
            @NotNull String entityDisplayName,
            boolean canManage
    ) {}
}
