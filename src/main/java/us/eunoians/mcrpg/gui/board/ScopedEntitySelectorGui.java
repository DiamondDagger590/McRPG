package us.eunoians.mcrpg.gui.board;

import com.diamonddagger590.mccore.gui.slot.Slot;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.board.slot.BoardBackSlot;
import us.eunoians.mcrpg.gui.board.slot.ScopedEntitySelectSlot;
import us.eunoians.mcrpg.gui.common.McRPGPaginatedGui;
import us.eunoians.mcrpg.quest.board.scope.ScopedBoardAdapter;
import us.eunoians.mcrpg.quest.board.scope.ScopedBoardAdapterRegistry;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Paginated GUI that lists all scope entities the player can manage across
 * all registered {@link ScopedBoardAdapter}s. Selecting an entity opens the
 * {@link QuestBoardGui} in {@link BoardGuiMode#SCOPED} mode filtered to
 * that entity.
 * <p>
 * If the player only has one manageable entity, callers should skip this GUI
 * and open the board directly (single-entity fast path in {@link us.eunoians.mcrpg.gui.board.slot.ScopedTabSlot}).
 */
public class ScopedEntitySelectorGui extends McRPGPaginatedGui {

    private static final int ROWS = 6;
    private static final int SLOTS_PER_PAGE = 45;

    private final List<ScopedEntityEntry> manageableEntities;

    public ScopedEntitySelectorGui(@NotNull McRPGPlayer player) {
        super(player);
        this.manageableEntities = collectManageableEntities(player.getUUID());
    }

    @NotNull
    @Override
    protected Inventory getInventoryForPage(int page) {
        return Bukkit.createInventory(null, ROWS * 9,
                RegistryAccess.registryAccess()
                        .registry(RegistryKey.MANAGER)
                        .manager(McRPGManagerKey.LOCALIZATION)
                        .getLocalizedMessageAsComponent(getCreatingPlayer(), LocalizationKey.QUEST_BOARD_GROUP_SELECTOR_TITLE));
    }

    @Override
    protected void paintInventoryForPage(@NotNull Inventory inventory, int page) {
        Slot<McRPGPlayer> filler = getFillerItemSlot();
        for (int i = 0; i < inventory.getSize(); i++) {
            setSlot(i, filler);
        }

        int startIndex = page * SLOTS_PER_PAGE;
        int endIndex = Math.min(startIndex + SLOTS_PER_PAGE, manageableEntities.size());

        for (int i = startIndex; i < endIndex; i++) {
            ScopedEntityEntry entry = manageableEntities.get(i);
            setSlot(i - startIndex, new ScopedEntitySelectSlot(
                    entry.scopeProviderKey(), entry.entityId(), entry.displayName()));
        }

        int navRowStart = 45;
        setSlot(navRowStart, new BoardBackSlot());
        if (page > 0) {
            setSlot(navRowStart + 3, getPreviousPageSlot());
        }
        if (endIndex < manageableEntities.size()) {
            setSlot(navRowStart + 5, getNextPageSlot());
        }
    }

    @Override
    public int getMaximumPage() {
        return Math.max(1, (int) Math.ceil((double) manageableEntities.size() / SLOTS_PER_PAGE));
    }

    @Override
    public void registerListeners() {
        Bukkit.getPluginManager().registerEvents(this, McRPG.getInstance());
    }

    @Override
    public void unregisterListeners() {
        org.bukkit.event.inventory.InventoryClickEvent.getHandlerList().unregister(this);
    }

    /**
     * Queries all registered {@link ScopedBoardAdapter}s and collects every scope entity
     * the player has management permissions for. Each entry pairs the adapter's scope
     * provider key with the entity identifier and its resolved display name.
     *
     * @param playerUUID the player whose manageable entities are being collected
     * @return an immutable list of manageable entity entries across all scope types
     */
    @NotNull
    private static List<ScopedEntityEntry> collectManageableEntities(@NotNull java.util.UUID playerUUID) {
        ScopedBoardAdapterRegistry registry = McRPG.getInstance().registryAccess()
                .registry(McRPGRegistryKey.SCOPED_BOARD_ADAPTER);

        List<ScopedEntityEntry> entries = new ArrayList<>();
        for (ScopedBoardAdapter adapter : registry.getAll()) {
            NamespacedKey scopeKey = adapter.getScopeProviderKey();
            Set<String> manageable = adapter.getManageableEntities(playerUUID);

            for (String entityId : manageable) {
                String displayName = adapter.getEntityDisplayName(entityId).orElse(entityId);
                entries.add(new ScopedEntityEntry(scopeKey, entityId, displayName));
            }
        }
        return List.copyOf(entries);
    }

    /**
     * Represents a single scope entity that the player can manage, combining the
     * adapter's scope provider key, the entity identifier, and its display name.
     *
     * @param scopeProviderKey the {@link NamespacedKey} of the {@link ScopedBoardAdapter}'s scope provider
     * @param entityId         the scope entity identifier (e.g., land name, faction ID)
     * @param displayName      the human-readable display name for the entity
     */
    record ScopedEntityEntry(
            @NotNull NamespacedKey scopeProviderKey,
            @NotNull String entityId,
            @NotNull String displayName
    ) {}
}
