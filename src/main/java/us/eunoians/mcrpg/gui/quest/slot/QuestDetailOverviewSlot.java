package us.eunoians.mcrpg.gui.quest.slot;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.database.table.quest.CompletionRecord;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.quest.QuestDetailGui;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.definition.QuestDefinitionRegistry;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Overview header slot at the top of the {@link QuestDetailGui},
 * showing quest name, state, and timing information.
 */
public class QuestDetailOverviewSlot implements McRPGSlot {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM dd, yyyy HH:mm");

    private final NamespacedKey questKey;
    @Nullable
    private final QuestInstance questInstance;
    @Nullable
    private final CompletionRecord completionRecord;
    @Nullable
    private final QuestDefinition resolvedDefinition;

    public QuestDetailOverviewSlot(@NotNull NamespacedKey questKey,
                                   @Nullable QuestInstance questInstance,
                                   @Nullable CompletionRecord completionRecord) {
        this(questKey, questInstance, completionRecord, null);
    }

    public QuestDetailOverviewSlot(@NotNull NamespacedKey questKey,
                                   @Nullable QuestInstance questInstance,
                                   @Nullable CompletionRecord completionRecord,
                                   @Nullable QuestDefinition resolvedDefinition) {
        this.questKey = questKey;
        this.questInstance = questInstance;
        this.completionRecord = completionRecord;
        this.resolvedDefinition = resolvedDefinition;
    }

    @Override
    public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
        return true;
    }

    @NotNull
    @Override
    public ItemBuilder getItem(@NotNull McRPGPlayer mcRPGPlayer) {
        Map<String, String> placeholders = new HashMap<>();

        Optional<QuestDefinition> defOpt;
        if (resolvedDefinition != null) {
            defOpt = Optional.of(resolvedDefinition);
        } else {
            QuestDefinitionRegistry definitionRegistry = RegistryAccess.registryAccess()
                    .registry(McRPGRegistryKey.QUEST_DEFINITION);
            defOpt = definitionRegistry.get(questKey);
        }

        String questName = defOpt.map(def -> def.getDisplayName(mcRPGPlayer))
                .orElse(questKey.toString());
        placeholders.put("quest_name", questName);

        if (questInstance != null) {
            placeholders.put("quest_state", questInstance.getQuestState().name());
            questInstance.getStartTime().ifPresent(t ->
                    placeholders.put("start_time", DATE_FORMAT.format(new Date(t))));
            questInstance.getEndTime().ifPresent(t ->
                    placeholders.put("end_time", DATE_FORMAT.format(new Date(t))));
            questInstance.getExpirationTime().ifPresent(t ->
                    placeholders.put("expiration_time", DATE_FORMAT.format(new Date(t))));
        } else if (completionRecord != null) {
            placeholders.put("quest_state", "COMPLETED");
            placeholders.put("completed_date", DATE_FORMAT.format(new Date(completionRecord.completedAt())));
        } else {
            placeholders.put("quest_state", "PREVIEW");
        }

        int phaseCount = defOpt.map(QuestDefinition::getPhaseCount).orElse(0);
        placeholders.put("phase_total", String.valueOf(phaseCount));

        return ItemBuilder.from(RegistryAccess.registryAccess()
                        .registry(RegistryKey.MANAGER)
                        .manager(McRPGManagerKey.LOCALIZATION)
                        .getLocalizedSection(mcRPGPlayer, LocalizationKey.QUEST_DETAIL_GUI_OVERVIEW_SLOT_DISPLAY_ITEM))
                .addPlaceholders(placeholders);
    }

    @NotNull
    @Override
    public Set<Class<?>> getValidGuiTypes() {
        return Set.of(QuestDetailGui.class);
    }
}
