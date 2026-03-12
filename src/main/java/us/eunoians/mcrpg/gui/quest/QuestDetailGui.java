package us.eunoians.mcrpg.gui.quest;

import com.diamonddagger590.mccore.exception.CorePlayerOfflineException;
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
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.database.table.quest.CompletionRecord;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.common.McRPGPaginatedGui;
import us.eunoians.mcrpg.gui.common.slot.McRPGPreviousGuiSlot;
import us.eunoians.mcrpg.gui.quest.slot.QuestDetailObjectiveSlot;
import us.eunoians.mcrpg.gui.quest.slot.QuestDetailOverviewSlot;
import us.eunoians.mcrpg.gui.quest.slot.QuestDetailPhaseSlot;
import us.eunoians.mcrpg.gui.quest.slot.QuestDetailAbandonSlot;
import us.eunoians.mcrpg.gui.quest.slot.QuestDetailDurationSlot;
import us.eunoians.mcrpg.gui.quest.slot.QuestDetailRewardSlot;
import us.eunoians.mcrpg.gui.board.QuestBoardGui;
import us.eunoians.mcrpg.quest.board.BoardOffering;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.definition.QuestDefinitionRegistry;
import us.eunoians.mcrpg.quest.definition.QuestObjectiveDefinition;
import us.eunoians.mcrpg.quest.definition.QuestPhaseDefinition;
import us.eunoians.mcrpg.quest.definition.QuestStageDefinition;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.quest.impl.objective.QuestObjectiveInstance;
import us.eunoians.mcrpg.quest.impl.stage.QuestStageInstance;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Detail GUI showing the full phase/stage/objective breakdown of a quest.
 * Supports both active quest instances and completed quests from the history log.
 */
public class QuestDetailGui extends McRPGPaginatedGui {

    private static final int NAVIGATION_ROW_START_INDEX = 45;
    private static final int PREVIOUS_GUI_SLOT_INDEX = NAVIGATION_ROW_START_INDEX;
    private static final int PREVIOUS_PAGE_SLOT_INDEX = NAVIGATION_ROW_START_INDEX + 2;
    private static final int NEXT_PAGE_SLOT_INDEX = NAVIGATION_ROW_START_INDEX + 6;
    private static final int ABANDON_SLOT_INDEX = NAVIGATION_ROW_START_INDEX + 8;

    private final NamespacedKey questKey;
    @Nullable
    private final QuestInstance questInstance;
    @Nullable
    private final CompletionRecord completionRecord;
    @Nullable
    private final QuestDefinition previewDefinition;
    @Nullable
    private final BoardOffering previewOffering;
    private final Player player;
    private final boolean fromHistory;
    private final boolean boardPreview;
    private final List<Slot<McRPGPlayer>> contentSlots;

    private QuestDetailGui(@NotNull McRPGPlayer mcRPGPlayer,
                           @NotNull NamespacedKey questKey,
                           @Nullable QuestInstance questInstance,
                           @Nullable CompletionRecord completionRecord,
                           boolean fromHistory,
                           @Nullable QuestDefinition previewDefinition,
                           @Nullable BoardOffering previewOffering) {
        super(mcRPGPlayer);
        this.player = mcRPGPlayer.getAsBukkitPlayer()
                .orElseThrow(() -> new CorePlayerOfflineException(mcRPGPlayer));
        this.questKey = questKey;
        this.questInstance = questInstance;
        this.completionRecord = completionRecord;
        this.fromHistory = fromHistory;
        this.boardPreview = previewDefinition != null;
        this.previewDefinition = previewDefinition;
        this.previewOffering = previewOffering;
        this.contentSlots = buildContentSlots(mcRPGPlayer);
    }

    /**
     * Creates a detail GUI for an active quest instance.
     */
    @NotNull
    public static QuestDetailGui forActiveQuest(@NotNull McRPGPlayer player,
                                                @NotNull QuestInstance questInstance) {
        return new QuestDetailGui(player, questInstance.getQuestKey(), questInstance, null, false, null, null);
    }

    /**
     * Creates a detail GUI for a completed quest from the history log.
     */
    @NotNull
    public static QuestDetailGui forCompletedQuest(@NotNull McRPGPlayer player,
                                                   @NotNull CompletionRecord record) {
        NamespacedKey key = NamespacedKey.fromString(record.definitionKey());
        if (key == null) {
            key = new NamespacedKey("mcrpg", record.definitionKey());
        }
        return new QuestDetailGui(player, key, null, record, true, null, null);
    }

    /**
     * Creates a preview GUI for a board offering before the player accepts it.
     * Shows the quest's phases, objectives, and rewards without needing an active instance.
     */
    @NotNull
    public static QuestDetailGui forBoardPreview(@NotNull McRPGPlayer player,
                                                 @NotNull QuestDefinition definition,
                                                 @NotNull BoardOffering offering) {
        return new QuestDetailGui(player, definition.getQuestKey(), null, null, false, definition, offering);
    }

    @NotNull
    private List<Slot<McRPGPlayer>> buildContentSlots(@NotNull McRPGPlayer player) {
        List<Slot<McRPGPlayer>> slots = new ArrayList<>();

        QuestDefinition def;
        if (previewDefinition != null) {
            def = previewDefinition;
        } else {
            QuestDefinitionRegistry definitionRegistry = RegistryAccess.registryAccess()
                    .registry(McRPGRegistryKey.QUEST_DEFINITION);
            def = definitionRegistry.get(questKey).orElse(null);
        }

        slots.add(new QuestDetailOverviewSlot(questKey, questInstance, completionRecord, def));

        if (def != null) {
            for (QuestPhaseDefinition phaseDef : def.getPhases()) {
                slots.add(new QuestDetailPhaseSlot(phaseDef, def.getPhaseCount()));

                for (QuestStageDefinition stageDef : phaseDef.getStages()) {
                    for (QuestObjectiveDefinition objDef : stageDef.getObjectives()) {
                        QuestObjectiveInstance objInstance = findObjectiveInstance(objDef.getObjectiveKey());
                        slots.add(new QuestDetailObjectiveSlot(questKey, objDef, objInstance));
                    }
                }
            }

            slots.add(new QuestDetailRewardSlot(def, player));
            slots.add(new QuestDetailDurationSlot(def));
        }

        return slots;
    }

    @Nullable
    private QuestObjectiveInstance findObjectiveInstance(@NotNull NamespacedKey objectiveKey) {
        if (questInstance == null) {
            return null;
        }
        return questInstance.getQuestStageInstances().stream()
                .flatMap(stage -> stage.getQuestObjectives().stream())
                .filter(obj -> obj.getQuestObjectiveKey().equals(objectiveKey))
                .findFirst()
                .orElse(null);
    }

    @NotNull
    @Override
    protected Inventory getInventoryForPage(int page) {
        return Bukkit.createInventory(player, 54,
                RegistryAccess.registryAccess()
                        .registry(RegistryKey.MANAGER)
                        .manager(McRPGManagerKey.LOCALIZATION)
                        .getLocalizedMessageAsComponent(getCreatingPlayer(), LocalizationKey.QUEST_DETAIL_GUI_TITLE));
    }

    @Override
    protected void paintInventoryForPage(@NotNull Inventory inventory, int page) {
        paintNavigationBar(page);
        paintContent(page);
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

        if (questInstance != null && !boardPreview && !fromHistory
                && questInstance.getQuestSource().isAbandonable()) {
            QuestDefinitionRegistry defRegistry = RegistryAccess.registryAccess()
                    .registry(McRPGRegistryKey.QUEST_DEFINITION);
            String displayName = defRegistry.get(questKey)
                    .map(def -> def.getDisplayName(getCreatingPlayer()))
                    .orElse(questKey.toString());
            setSlot(ABANDON_SLOT_INDEX, new QuestDetailAbandonSlot(questInstance, displayName));
        }
    }

    private void paintContent(int page) {
        int start = (page - 1) * NAVIGATION_ROW_START_INDEX;
        for (int i = 0; i < NAVIGATION_ROW_START_INDEX; i++) {
            int dataIndex = start + i;
            if (dataIndex < contentSlots.size()) {
                setSlot(i, contentSlots.get(dataIndex));
            } else {
                removeSlot(i);
            }
        }
    }

    @Override
    public int getMaximumPage() {
        return Math.max(1, (int) Math.ceil((double) contentSlots.size() / NAVIGATION_ROW_START_INDEX));
    }

    @NotNull
    public McRPGPreviousGuiSlot getPreviousGuiSlot() {
        return new McRPGPreviousGuiSlot() {
            @Override
            public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
                mcRPGPlayer.getAsBukkitPlayer().ifPresent(player -> {
                    if (boardPreview) {
                        QuestBoardGui boardGui = new QuestBoardGui(mcRPGPlayer);
                        McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER)
                                .manager(McRPGManagerKey.GUI).trackPlayerGui(player, boardGui);
                        player.openInventory(boardGui.getInventory());
                    } else if (fromHistory) {
                        QuestHistoryGui historyGui = new QuestHistoryGui(mcRPGPlayer);
                        McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER)
                                .manager(McRPGManagerKey.GUI).trackPlayerGui(player, historyGui);
                        player.openInventory(historyGui.getInventory());
                    } else {
                        ActiveQuestGui activeGui = new ActiveQuestGui(mcRPGPlayer);
                        McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER)
                                .manager(McRPGManagerKey.GUI).trackPlayerGui(player, activeGui);
                        player.openInventory(activeGui.getInventory());
                    }
                });
                return true;
            }

            @NotNull
            @Override
            public Route getSpecificDisplayItemRoute() {
                return LocalizationKey.QUEST_DETAIL_GUI_PREVIOUS_GUI_BUTTON_DISPLAY_ITEM;
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
