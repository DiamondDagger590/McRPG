package us.eunoians.mcrpg.gui.board;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.gui.slot.Slot;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.board.slot.BoardBackSlot;
import us.eunoians.mcrpg.gui.board.slot.BoardOfferingSlot;
import us.eunoians.mcrpg.gui.board.slot.ScopedBackSlot;
import us.eunoians.mcrpg.gui.board.slot.ScopedOfferingSlot;
import us.eunoians.mcrpg.gui.board.slot.ScopedTabSlot;
import us.eunoians.mcrpg.gui.common.McRPGPaginatedGui;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
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
        this.offerings = boardManager.getSharedOfferingsForBoard(DEFAULT_BOARD_KEY).stream()
                .filter(o -> o.getState() == BoardOffering.State.VISIBLE)
                .toList();
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
            this.offerings = boardManager.getSharedOfferingsForBoard(DEFAULT_BOARD_KEY).stream()
                    .filter(o -> o.getState() == BoardOffering.State.VISIBLE)
                    .toList();
            this.scopedOfferings = List.of();
            this.hasScopedEntities = checkHasScopedEntities(player.getUUID());
        }
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

        int startIndex = page * SLOTS_PER_PAGE;
        int endIndex = Math.min(startIndex + SLOTS_PER_PAGE, offerings.size());
        for (int i = startIndex; i < endIndex; i++) {
            setSlot(i - startIndex, new BoardOfferingSlot(offerings.get(i)));
        }

        int navRowStart = 45;
        setSlot(navRowStart, new BoardBackSlot());
        if (page > 0) {
            setSlot(navRowStart + 3, getPreviousPageSlot());
        }
        if (endIndex < offerings.size()) {
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

        if (scopedOfferings.isEmpty()) {
            setSlot(22, noGroupQuestsSlot());
        } else {
            int startIndex = page * SLOTS_PER_PAGE;
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
        if (page > 0) {
            setSlot(navRowStart + 3, getPreviousPageSlot());
        }
        if (!scopedOfferings.isEmpty() && (page + 1) * SLOTS_PER_PAGE < scopedOfferings.size()) {
            setSlot(navRowStart + 5, getNextPageSlot());
        }
    }

    @Override
    public int getMaximumPage() {
        int itemCount = (mode == BoardGuiMode.SCOPED) ? scopedOfferings.size() : offerings.size();
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

    @NotNull
    private static McRPGSlot noGroupQuestsSlot() {
        return new McRPGSlot() {
            @NotNull
            @Override
            public ItemBuilder getItem(@NotNull McRPGPlayer mcRPGPlayer) {
                return ItemBuilder.from(new ItemStack(Material.BARRIER))
                        .setDisplayName("No group quests available");
            }

            @Override
            public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
                return true;
            }

            @NotNull
            @Override
            public Set<Class<?>> getValidGuiTypes() {
                return Set.of(QuestBoardGui.class);
            }
        };
    }

    private record ScopedOfferingEntry(
            @NotNull BoardOffering offering,
            @NotNull String entityId,
            @NotNull NamespacedKey scopeProviderKey,
            @NotNull String entityDisplayName,
            boolean canManage
    ) {}
}
