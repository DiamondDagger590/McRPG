package us.eunoians.mcrpg.gui.quest;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.exception.CorePlayerOfflineException;
import com.diamonddagger590.mccore.exception.gui.InventoryAlreadyExistsForGuiException;
import com.diamonddagger590.mccore.gui.BaseGui;
import com.diamonddagger590.mccore.gui.slot.Slot;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.common.FillerItemGui;
import us.eunoians.mcrpg.gui.quest.slot.QuestAbandonCancelSlot;
import us.eunoians.mcrpg.gui.quest.slot.QuestAbandonConfirmSlot;
import us.eunoians.mcrpg.gui.quest.slot.QuestAbandonInfoSlot;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

/**
 * Confirmation GUI shown before abandoning a quest.
 * Displays a confirm button, quest info, and cancel button in a 3-row layout.
 */
public class QuestAbandonConfirmGui extends BaseGui<McRPGPlayer> implements FillerItemGui {

    private static final int CONFIRM_SLOT_INDEX = 11;
    private static final int QUEST_INFO_SLOT_INDEX = 13;
    private static final int CANCEL_SLOT_INDEX = 15;

    private final Player player;
    private final QuestInstance questInstance;
    private final String questDisplayName;
    private final boolean fromDetail;

    /**
     * @param mcRPGPlayer      the player
     * @param questInstance     the quest being considered for abandonment
     * @param questDisplayName  the display name of the quest
     * @param fromDetail        if true, cancel returns to QuestDetailGui; otherwise to ActiveQuestGui
     */
    public QuestAbandonConfirmGui(@NotNull McRPGPlayer mcRPGPlayer,
                                  @NotNull QuestInstance questInstance,
                                  @NotNull String questDisplayName,
                                  boolean fromDetail) {
        super(mcRPGPlayer);
        this.player = mcRPGPlayer.getAsBukkitPlayer()
                .orElseThrow(() -> new CorePlayerOfflineException(mcRPGPlayer));
        this.questInstance = questInstance;
        this.questDisplayName = questDisplayName;
        this.fromDetail = fromDetail;
    }

    @Override
    protected void buildInventory() {
        if (this.inventory != null) {
            throw new InventoryAlreadyExistsForGuiException(this);
        }
        this.inventory = Bukkit.createInventory(player, 27,
                RegistryAccess.registryAccess()
                        .registry(RegistryKey.MANAGER)
                        .manager(McRPGManagerKey.LOCALIZATION)
                        .getLocalizedMessageAsComponent(getCreatingPlayer(), LocalizationKey.QUEST_ABANDON_CONFIRM_GUI_TITLE));
        paintInventory();
    }

    @Override
    public void paintInventory() {
        Slot<McRPGPlayer> fillerSlot = getFillerItemSlot();
        for (int i = 0; i < inventory.getSize(); i++) {
            setSlot(i, fillerSlot);
        }
        setSlot(CONFIRM_SLOT_INDEX, new QuestAbandonConfirmSlot(questInstance, questDisplayName));
        setSlot(QUEST_INFO_SLOT_INDEX, new QuestAbandonInfoSlot(questDisplayName));
        setSlot(CANCEL_SLOT_INDEX, new QuestAbandonCancelSlot(questInstance, fromDetail));
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
