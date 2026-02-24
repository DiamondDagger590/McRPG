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
import us.eunoians.mcrpg.gui.quest.QuestDetailGui;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
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
            QuestDetailGui detailGui = QuestDetailGui.forActiveQuest(mcRPGPlayer, questInstance);
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
        Optional<QuestDefinition> defOpt = definitionRegistry.get(questInstance.getQuestKey());

        String questName = defOpt.map(def -> def.getDisplayName(mcRPGPlayer))
                .orElse(questInstance.getQuestKey().toString());
        placeholders.put("quest_name", questName);

        int totalPhases = defOpt.map(QuestDefinition::getPhaseCount).orElse(1);
        int currentPhase = questInstance.getActiveQuestStages().stream()
                .mapToInt(s -> s.getPhaseIndex())
                .min().orElse(0) + 1;
        placeholders.put("phase_current", String.valueOf(currentPhase));
        placeholders.put("phase_total", String.valueOf(totalPhases));

        StringBuilder objectiveProgress = new StringBuilder();
        for (var stage : questInstance.getActiveQuestStages()) {
            for (QuestObjectiveInstance obj : stage.getQuestObjectives()) {
                if (objectiveProgress.length() > 0) {
                    objectiveProgress.append("\n");
                }
                String objDesc = defOpt.flatMap(def -> def.findObjectiveDefinition(obj.getQuestObjectiveKey()))
                        .map(objDef -> objDef.getDescription(mcRPGPlayer, questInstance.getQuestKey()))
                        .orElse(obj.getQuestObjectiveKey().getKey());
                objectiveProgress.append(objDesc)
                        .append(": ").append(obj.getCurrentProgression())
                        .append("/").append(obj.getRequiredProgression());
            }
        }
        placeholders.put("objectives", objectiveProgress.toString());

        String timeRemaining = questInstance.getExpirationTime()
                .map(expiry -> {
                    long remaining = expiry - McRPG.getInstance().getTimeProvider().now().toEpochMilli();
                    if (remaining <= 0) {
                        return "Expired";
                    }
                    Duration d = Duration.ofMillis(remaining);
                    long hours = d.toHours();
                    long minutes = d.toMinutesPart();
                    return hours + "h " + minutes + "m";
                })
                .orElse("None");
        placeholders.put("time_remaining", timeRemaining);

        return ItemBuilder.from(RegistryAccess.registryAccess()
                        .registry(RegistryKey.MANAGER)
                        .manager(McRPGManagerKey.LOCALIZATION)
                        .getLocalizedSection(mcRPGPlayer, LocalizationKey.ACTIVE_QUEST_GUI_QUEST_SLOT_DISPLAY_ITEM))
                .addPlaceholders(placeholders);
    }

    @NotNull
    @Override
    public Set<Class<?>> getValidGuiTypes() {
        return Set.of(ActiveQuestGui.class);
    }
}
