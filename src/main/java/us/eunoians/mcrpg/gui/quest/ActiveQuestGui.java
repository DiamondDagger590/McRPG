package us.eunoians.mcrpg.gui.quest;

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
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.common.McRPGPaginatedGui;
import us.eunoians.mcrpg.gui.common.slot.McRPGPreviousGuiSlot;
import us.eunoians.mcrpg.gui.home.HomeGui;
import us.eunoians.mcrpg.gui.quest.slot.ActiveQuestSlot;
import us.eunoians.mcrpg.gui.quest.slot.ViewQuestHistorySlot;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.ArrayList;
import java.util.List;

/**
 * Paginated GUI displaying all active (in-progress) quests for a player.
 */
public class ActiveQuestGui extends McRPGPaginatedGui {

    private static final int NAVIGATION_ROW_START_INDEX = 45;
    private static final int PREVIOUS_GUI_SLOT_INDEX = NAVIGATION_ROW_START_INDEX;
    private static final int PREVIOUS_PAGE_SLOT_INDEX = NAVIGATION_ROW_START_INDEX + 2;
    private static final int NEXT_PAGE_SLOT_INDEX = NAVIGATION_ROW_START_INDEX + 6;
    private static final int VIEW_HISTORY_SLOT_INDEX = NAVIGATION_ROW_START_INDEX + 8;

    private final Player player;

    public ActiveQuestGui(@NotNull McRPGPlayer mcRPGPlayer) {
        super(mcRPGPlayer);
        this.player = mcRPGPlayer.getAsBukkitPlayer()
                .orElseThrow(() -> new CorePlayerOfflineException(mcRPGPlayer));
    }

    @NotNull
    @Override
    protected Inventory getInventoryForPage(int page) {
        return Bukkit.createInventory(player, 54,
                RegistryAccess.registryAccess()
                        .registry(RegistryKey.MANAGER)
                        .manager(McRPGManagerKey.LOCALIZATION)
                        .getLocalizedMessageAsComponent(getCreatingPlayer(), LocalizationKey.ACTIVE_QUEST_GUI_TITLE));
    }

    @Override
    protected void paintInventoryForPage(@NotNull Inventory inventory, int page) {
        paintNavigationBar(page);
        paintQuests(page);
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
        setSlot(VIEW_HISTORY_SLOT_INDEX, new ViewQuestHistorySlot());
    }

    private void paintQuests(int page) {
        List<QuestInstance> activeQuests = getActiveQuestsForPage(page);
        for (int i = 0; i < NAVIGATION_ROW_START_INDEX; i++) {
            if (i < activeQuests.size()) {
                setSlot(i, new ActiveQuestSlot(activeQuests.get(i)));
            } else {
                removeSlot(i);
            }
        }
    }

    @NotNull
    private List<QuestInstance> getActiveQuestsForPage(int page) {
        List<QuestInstance> allActive = getUnexpiredActiveQuests();
        int start = (page - 1) * NAVIGATION_ROW_START_INDEX;
        int end = Math.min(start + NAVIGATION_ROW_START_INDEX, allActive.size());
        if (start >= allActive.size()) {
            return List.of();
        }
        return allActive.subList(start, end);
    }

    @NotNull
    private List<QuestInstance> getUnexpiredActiveQuests() {
        QuestManager questManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.QUEST);
        List<QuestInstance> allActive = questManager.getActiveQuestsForPlayer(
                getCreatingPlayer().getUUID());
        List<QuestInstance> unexpired = new ArrayList<>(allActive.size());
        for (QuestInstance quest : allActive) {
            if (quest.isExpired()) {
                quest.expire();
                continue;
            }
            unexpired.add(quest);
        }
        return List.copyOf(unexpired);
    }

    @Override
    public int getMaximumPage() {
        int total = getUnexpiredActiveQuests().size();
        return Math.max(1, (int) Math.ceil((double) total / NAVIGATION_ROW_START_INDEX));
    }

    @NotNull
    public McRPGPreviousGuiSlot getPreviousGuiSlot() {
        return new McRPGPreviousGuiSlot() {
            @Override
            public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
                mcRPGPlayer.getAsBukkitPlayer().ifPresent(player -> {
                    HomeGui homeGui = new HomeGui(mcRPGPlayer);
                    McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER)
                            .manager(McRPGManagerKey.GUI).trackPlayerGui(player, homeGui);
                    player.openInventory(homeGui.getInventory());
                });
                return true;
            }

            @NotNull
            @Override
            public Route getSpecificDisplayItemRoute() {
                return LocalizationKey.ACTIVE_QUEST_GUI_PREVIOUS_GUI_BUTTON_DISPLAY_ITEM;
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
