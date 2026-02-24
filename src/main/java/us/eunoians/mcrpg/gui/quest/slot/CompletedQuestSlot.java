package us.eunoians.mcrpg.gui.quest.slot;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import dev.dejvokep.boostedyaml.route.Route;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.database.table.quest.CompletionRecord;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.quest.QuestDetailGui;
import us.eunoians.mcrpg.gui.quest.QuestHistoryGui;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.definition.QuestDefinitionRegistry;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Slot representing a single completed quest in the {@link QuestHistoryGui}.
 */
public class CompletedQuestSlot implements McRPGSlot {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM dd, yyyy");

    private final CompletionRecord record;

    public CompletedQuestSlot(@NotNull CompletionRecord record) {
        this.record = record;
    }

    @Override
    public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
        mcRPGPlayer.getAsBukkitPlayer().ifPresent(player -> {
            QuestDetailGui detailGui = QuestDetailGui.forCompletedQuest(mcRPGPlayer, record);
            McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER)
                    .manager(McRPGManagerKey.GUI).trackPlayerGui(player, detailGui);
            player.openInventory(detailGui.getInventory());
        });
        return true;
    }

    @NotNull
    @Override
    public ItemBuilder getItem(@NotNull McRPGPlayer mcRPGPlayer) {
        Map<String, String> placeholders = new HashMap<>();

        QuestDefinitionRegistry definitionRegistry = RegistryAccess.registryAccess()
                .registry(McRPGRegistryKey.QUEST_DEFINITION);
        NamespacedKey defKey = NamespacedKey.fromString(record.definitionKey());
        Optional<QuestDefinition> defOpt = defKey != null ? definitionRegistry.get(defKey) : Optional.empty();

        Route displayRoute;
        if (defOpt.isPresent()) {
            String questName = defOpt.get().getDisplayName(mcRPGPlayer);
            placeholders.put("quest_name", questName);
            displayRoute = LocalizationKey.QUEST_HISTORY_GUI_COMPLETED_QUEST_SLOT_DISPLAY_ITEM;
        } else {
            placeholders.put("quest_name", record.definitionKey());
            displayRoute = LocalizationKey.QUEST_HISTORY_GUI_UNKNOWN_QUEST_SLOT_DISPLAY_ITEM;
        }

        placeholders.put("completed_date", DATE_FORMAT.format(new Date(record.completedAt())));

        return ItemBuilder.from(RegistryAccess.registryAccess()
                        .registry(RegistryKey.MANAGER)
                        .manager(McRPGManagerKey.LOCALIZATION)
                        .getLocalizedSection(mcRPGPlayer, displayRoute))
                .addPlaceholders(placeholders);
    }

    @NotNull
    @Override
    public Set<Class<?>> getValidGuiTypes() {
        return Set.of(QuestHistoryGui.class);
    }
}
