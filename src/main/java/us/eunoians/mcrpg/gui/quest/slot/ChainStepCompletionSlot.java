package us.eunoians.mcrpg.gui.quest.slot;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.database.table.quest.CompletionRecord;
import us.eunoians.mcrpg.database.table.quest.QuestChainCompletionLogDAO;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.quest.QuestChainHistoryDetailGui;
import us.eunoians.mcrpg.gui.quest.QuestDetailGui;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.definition.QuestDefinitionRegistry;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * A slot in {@link QuestChainHistoryDetailGui} representing a single step completion
 * within a chain run. Displays the quest name, step number, and completion date.
 * Clicking opens {@link QuestDetailGui} for the completed quest.
 */
public class ChainStepCompletionSlot implements McRPGSlot {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM dd, yyyy");

    private final QuestChainCompletionLogDAO.ChainStepRecord stepRecord;
    private final int stepNumber;

    public ChainStepCompletionSlot(@NotNull QuestChainCompletionLogDAO.ChainStepRecord stepRecord,
                                   int stepNumber) {
        this.stepRecord = stepRecord;
        this.stepNumber = stepNumber;
    }

    @Override
    public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
        NamespacedKey questKey = NamespacedKey.fromString(stepRecord.questKey());
        if (questKey == null) {
            return false;
        }

        QuestDefinitionRegistry definitionRegistry = RegistryAccess.registryAccess()
                .registry(McRPGRegistryKey.QUEST_DEFINITION);
        Optional<QuestDefinition> definitionOpt = definitionRegistry.get(questKey);
        if (definitionOpt.isEmpty()) {
            return false;
        }

        mcRPGPlayer.getAsBukkitPlayer().ifPresent(player -> {
            var record = new CompletionRecord(stepRecord.questKey(), UUID.randomUUID(), stepRecord.completedAt());
            QuestDetailGui detailGui = QuestDetailGui.forCompletedQuest(mcRPGPlayer, record);
            McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER)
                    .manager(McRPGManagerKey.GUI).trackPlayerGui(player, detailGui);
            player.openInventory(detailGui.getInventory());
        });
        return true;
    }

    @Override
    @NotNull
    public ItemBuilder getItem(@NotNull McRPGPlayer mcRPGPlayer) {
        McRPGLocalizationManager localizationManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);

        QuestDefinitionRegistry definitionRegistry = RegistryAccess.registryAccess()
                .registry(McRPGRegistryKey.QUEST_DEFINITION);
        NamespacedKey questKey = NamespacedKey.fromString(stepRecord.questKey());
        Optional<QuestDefinition> defOpt = questKey != null ? definitionRegistry.get(questKey) : Optional.empty();

        String questName = defOpt.map(def -> def.getDisplayName(mcRPGPlayer)).orElse(stepRecord.questKey());
        String completedDate = DATE_FORMAT.format(new Date(stepRecord.completedAt()));

        ItemBuilder itemBuilder = ItemBuilder.from(localizationManager.getLocalizedSection(
                mcRPGPlayer, LocalizationKey.QUEST_CHAIN_HISTORY_GUI_STEP_SLOT_DISPLAY_ITEM))
                .addPlaceholders(Map.of(
                        "quest_name", questName,
                        "step_number", String.valueOf(stepNumber),
                        "completed_date", completedDate
                ));
        itemBuilder.applyTagReplacements(localizationManager.getPaletteReplacements());
        return itemBuilder;
    }

    @Override
    @NotNull
    public Set<Class<?>> getValidGuiTypes() {
        return Set.of(QuestChainHistoryDetailGui.class);
    }
}
