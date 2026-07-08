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
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.definition.QuestDefinitionRegistry;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Overview header slot at the top of the {@link QuestDetailGui},
 * showing quest name, state, and timing information.
 */
public class QuestDetailOverviewSlot implements McRPGSlot {

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

    /**
     * Builds the overview display item for this quest.
     *
     * <p>Placeholders resolved:
     * <ul>
     *   <li>{@code quest_name} — quest display name, or the raw key if the definition is not registered</li>
     *   <li>{@code quest_state} — localized state label (e.g. "Completed", "In Progress", "Preview")</li>
     *   <li>{@code start_time} — locale-aware formatted start timestamp (active quest only)</li>
     *   <li>{@code end_time} — locale-aware formatted end timestamp (active quest only)</li>
     *   <li>{@code expiration_time} — locale-aware formatted expiration timestamp (active quest only)</li>
     *   <li>{@code completed_date} — locale-aware formatted completion date (history view only)</li>
     *   <li>{@code phase_total} — total phase count in the quest definition</li>
     * </ul>
     *
     * @param mcRPGPlayer the player viewing the slot; used for locale resolution
     * @return the item builder populated with all placeholders and palette replacements applied
     */
    @NotNull
    @Override
    public ItemBuilder getItem(@NotNull McRPGPlayer mcRPGPlayer) {
        Map<String, String> placeholders = new HashMap<>();

        McRPGLocalizationManager localizationManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);

        Locale locale = localizationManager.getLocaleChain(mcRPGPlayer).getNodeValue();
        DateTimeFormatter dateFormat = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
                .withLocale(locale).withZone(ZoneOffset.UTC);

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
                    placeholders.put("start_time", dateFormat.format(t)));
            questInstance.getEndTime().ifPresent(t ->
                    placeholders.put("end_time", dateFormat.format(t)));
            questInstance.getExpirationTime().ifPresent(t ->
                    placeholders.put("expiration_time", dateFormat.format(t)));
        } else if (completionRecord != null) {
            placeholders.put("quest_state", localizationManager
                    .getLocalizedMessage(mcRPGPlayer, LocalizationKey.QUEST_DETAIL_GUI_STATE_COMPLETED));
            placeholders.put("completed_date", dateFormat.format(completionRecord.completedAt()));
        } else {
            String previewLabel = localizationManager
                    .getLocalizedMessage(mcRPGPlayer, LocalizationKey.QUEST_DETAIL_GUI_STATE_PREVIEW);
            placeholders.put("quest_state", previewLabel);
        }

        int phaseCount = defOpt.map(QuestDefinition::getPhaseCount).orElse(0);
        placeholders.put("phase_total", String.valueOf(phaseCount));

        ItemBuilder itemBuilder = ItemBuilder.from(localizationManager.getLocalizedSection(mcRPGPlayer, LocalizationKey.QUEST_DETAIL_GUI_OVERVIEW_SLOT_DISPLAY_ITEM))
                .addPlaceholders(placeholders);
        itemBuilder.applyTagReplacements(localizationManager.getPaletteReplacements());
        return itemBuilder;
    }

    @NotNull
    @Override
    public Set<Class<?>> getValidGuiTypes() {
        return Set.of(QuestDetailGui.class);
    }
}
