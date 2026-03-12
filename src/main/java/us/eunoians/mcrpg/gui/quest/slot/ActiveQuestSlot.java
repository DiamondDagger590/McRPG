package us.eunoians.mcrpg.gui.quest.slot;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.quest.ActiveQuestGui;
import us.eunoians.mcrpg.gui.quest.QuestAbandonConfirmGui;
import us.eunoians.mcrpg.gui.quest.QuestDetailGui;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.definition.QuestDefinitionRegistry;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.quest.impl.objective.QuestObjectiveInstance;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Slot representing a single active quest in the {@link ActiveQuestGui}.
 */
public class ActiveQuestSlot implements McRPGSlot {

    private final QuestInstance questInstance;

    public ActiveQuestSlot(@NotNull QuestInstance questInstance) {
        this.questInstance = questInstance;
    }

    @Override
    public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
        mcRPGPlayer.getAsBukkitPlayer().ifPresent(player -> {
            if (clickType == ClickType.RIGHT && questInstance.getQuestSource().isAbandonable()) {
                String questName = resolveDisplayName(mcRPGPlayer);
                QuestAbandonConfirmGui confirmGui = new QuestAbandonConfirmGui(
                        mcRPGPlayer, questInstance, questName, false);
                McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER)
                        .manager(McRPGManagerKey.GUI).trackPlayerGui(player, confirmGui);
                player.openInventory(confirmGui.getInventory());
            } else {
                QuestDetailGui detailGui = QuestDetailGui.forActiveQuest(mcRPGPlayer, questInstance);
                McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER)
                        .manager(McRPGManagerKey.GUI).trackPlayerGui(player, detailGui);
                player.openInventory(detailGui.getInventory());
            }
        });
        return true;
    }

    @NotNull
    private String resolveDisplayName(@NotNull McRPGPlayer mcRPGPlayer) {
        QuestDefinitionRegistry definitionRegistry = RegistryAccess.registryAccess()
                .registry(McRPGRegistryKey.QUEST_DEFINITION);
        return definitionRegistry.get(questInstance.getQuestKey())
                .map(def -> def.getDisplayName(mcRPGPlayer))
                .orElse(questInstance.getQuestKey().toString());
    }

    @NotNull
    @Override
    public ItemBuilder getItem(@NotNull McRPGPlayer mcRPGPlayer) {
        Map<String, String> placeholders = new HashMap<>();
        McRPGLocalizationManager localizationManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION);

        QuestDefinitionRegistry definitionRegistry = RegistryAccess.registryAccess()
                .registry(McRPGRegistryKey.QUEST_DEFINITION);
        Optional<QuestDefinition> defOpt = definitionRegistry.get(questInstance.getQuestKey());

        String questName = defOpt.map(def -> def.getDisplayName(mcRPGPlayer))
                .orElse(questInstance.getQuestKey().toString());
        placeholders.put("quest_name", questName);

        int totalPhases = defOpt.map(QuestDefinition::getPhaseCount).orElse(1);
        int currentPhase = questInstance.getActiveQuestStages().stream()
                .mapToInt(s -> s.getPhaseIndex())
                .min().orElse(0) + 1;

        ItemBuilder builder = ItemBuilder.from(RegistryAccess.registryAccess()
                        .registry(RegistryKey.MANAGER)
                        .manager(McRPGManagerKey.LOCALIZATION)
                        .getLocalizedSection(mcRPGPlayer, LocalizationKey.ACTIVE_QUEST_GUI_QUEST_SLOT_DISPLAY_ITEM))
                .addPlaceholders(placeholders);

        builder.addDisplayLore(formatLocalizedLine(
                localizationManager.getLocalizedMessage(mcRPGPlayer, LocalizationKey.ACTIVE_QUEST_GUI_PHASE_LINE),
                Map.of(
                        "current_phase", String.valueOf(currentPhase),
                        "total_phases", String.valueOf(totalPhases)
                )));
        builder.addDisplayLore("");

        for (var stage : questInstance.getActiveQuestStages()) {
            for (QuestObjectiveInstance obj : stage.getQuestObjectives()) {
                String objDesc = defOpt.flatMap(def -> def.findObjectiveDefinition(obj.getQuestObjectiveKey()))
                        .map(objDef -> objDef.getDescription(mcRPGPlayer, questInstance.getQuestKey()))
                        .orElse(obj.getQuestObjectiveKey().getKey());

                String[] descLines = objDesc.split("\n");
                builder.addDisplayLore(formatLocalizedLine(
                        localizationManager.getLocalizedMessage(mcRPGPlayer, LocalizationKey.ACTIVE_QUEST_GUI_OBJECTIVE_PROGRESS_LINE),
                        Map.of(
                                "objective_description", descLines[0],
                                "current_progress", String.valueOf(obj.getCurrentProgression()),
                                "required_progress", String.valueOf(obj.getRequiredProgression())
                        )));
                for (int i = 1; i < descLines.length; i++) {
                    builder.addDisplayLore(formatLocalizedLine(
                            localizationManager.getLocalizedMessage(mcRPGPlayer, LocalizationKey.ACTIVE_QUEST_GUI_OBJECTIVE_DETAIL_LINE),
                            Map.of("objective_description", descLines[i])));
                }
            }
        }

        String timeRemaining = questInstance.getExpirationTime()
                .map(expiry -> {
                    long remaining = expiry - McRPG.getInstance().getTimeProvider().now().toEpochMilli();
                    if (remaining <= 0) {
                        return localizationManager.getLocalizedMessage(
                                mcRPGPlayer,
                                LocalizationKey.ACTIVE_QUEST_GUI_EXPIRES_EXPIRED);
                    }
                    Duration d = Duration.ofMillis(remaining);
                    long hours = d.toHours();
                    long minutes = d.toMinutesPart();
                    return formatLocalizedLine(
                            localizationManager.getLocalizedMessage(mcRPGPlayer, LocalizationKey.ACTIVE_QUEST_GUI_EXPIRES_TIME_FORMAT),
                            Map.of(
                                    "hours", String.valueOf(hours),
                                    "minutes", String.valueOf(minutes)
                            ));
                })
                .orElse(localizationManager.getLocalizedMessage(
                        mcRPGPlayer,
                        LocalizationKey.ACTIVE_QUEST_GUI_EXPIRES_NONE));

        builder.addDisplayLore("");
        builder.addDisplayLore(formatLocalizedLine(
                localizationManager.getLocalizedMessage(mcRPGPlayer, LocalizationKey.ACTIVE_QUEST_GUI_EXPIRES_LINE),
                Map.of("time_remaining", timeRemaining)));
        builder.addDisplayLore("");
        builder.addDisplayLore(localizationManager.getLocalizedMessage(
                mcRPGPlayer,
                LocalizationKey.ACTIVE_QUEST_GUI_CLICK_TO_VIEW_DETAILS));
        if (questInstance.getQuestSource().isAbandonable()) {
            builder.addDisplayLore(localizationManager.getLocalizedMessage(
                    mcRPGPlayer,
                    LocalizationKey.ACTIVE_QUEST_GUI_RIGHT_CLICK_TO_ABANDON));
        }

        return builder;
    }

    @NotNull
    private static String formatLocalizedLine(@NotNull String template, @NotNull Map<String, String> placeholders) {
        String resolved = template;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            resolved = resolved.replace("<" + entry.getKey() + ">", entry.getValue());
        }
        return resolved;
    }

    @NotNull
    @Override
    public Set<Class<?>> getValidGuiTypes() {
        return Set.of(ActiveQuestGui.class);
    }
}
