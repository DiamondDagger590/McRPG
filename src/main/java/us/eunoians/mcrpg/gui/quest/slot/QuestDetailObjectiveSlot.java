package us.eunoians.mcrpg.gui.quest.slot;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.quest.QuestDetailGui;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.quest.definition.QuestObjectiveDefinition;
import us.eunoians.mcrpg.quest.impl.objective.QuestObjectiveInstance;
import us.eunoians.mcrpg.quest.impl.objective.QuestObjectiveState;
import us.eunoians.mcrpg.quest.reward.QuestRewardType;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Objective slot in the {@link QuestDetailGui}, showing objective description and progress.
 */
public class QuestDetailObjectiveSlot implements McRPGSlot {

    private final NamespacedKey questKey;
    private final QuestObjectiveDefinition objectiveDef;
    @Nullable
    private final QuestObjectiveInstance objectiveInstance;

    public QuestDetailObjectiveSlot(@NotNull NamespacedKey questKey,
                                    @NotNull QuestObjectiveDefinition objectiveDef,
                                    @Nullable QuestObjectiveInstance objectiveInstance) {
        this.questKey = questKey;
        this.objectiveDef = objectiveDef;
        this.objectiveInstance = objectiveInstance;
    }

    @Override
    public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
        return true;
    }

    @NotNull
    @Override
    public ItemBuilder getItem(@NotNull McRPGPlayer mcRPGPlayer) {
        Map<String, String> placeholders = new HashMap<>();

        String description = objectiveDef.getDescription(mcRPGPlayer, questKey);
        String[] descLines = description.split("\n");
        placeholders.put("objective_description", descLines[0]);

        String progress;
        String required;
        String state;

        McRPGLocalizationManager localization = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION);

        if (objectiveInstance != null) {
            progress = String.valueOf(objectiveInstance.getCurrentProgression());
            required = String.valueOf(objectiveInstance.getRequiredProgression());
            state = resolveObjectiveStateLabel(localization, mcRPGPlayer, objectiveInstance.getQuestObjectiveState());
        } else {
            progress = "0";
            try {
                required = String.valueOf(objectiveDef.getRequiredProgress());
            } catch (IllegalStateException e) {
                required = "?";
            }
            state = localization.getLocalizedMessage(mcRPGPlayer, LocalizationKey.QUEST_DETAIL_GUI_STATE_PREVIEW);
        }

        ItemBuilder builder = ItemBuilder.from(localization.getLocalizedSection(mcRPGPlayer,
                        LocalizationKey.QUEST_DETAIL_GUI_OBJECTIVE_SLOT_DISPLAY_ITEM))
                .addPlaceholders(placeholders);

        for (int i = 1; i < descLines.length; i++) {
            builder.addDisplayLore(localization.getLocalizedMessage(mcRPGPlayer,
                    LocalizationKey.QUEST_DETAIL_GUI_OBJECTIVE_DESCRIPTION_CONTINUATION,
                    Map.of("line", descLines[i])));
        }

        builder.addDisplayLore(localization.getLocalizedMessage(mcRPGPlayer,
                LocalizationKey.QUEST_DETAIL_GUI_OBJECTIVE_PROGRESS,
                Map.of("progress", progress, "required", required)));
        builder.addDisplayLore(localization.getLocalizedMessage(mcRPGPlayer,
                LocalizationKey.QUEST_DETAIL_GUI_OBJECTIVE_STATE,
                Map.of("state", state)));

        if (!objectiveDef.getRewards().isEmpty()) {
            builder.addDisplayLore("");
            builder.addDisplayLore(localization.getLocalizedMessage(mcRPGPlayer,
                    LocalizationKey.QUEST_DETAIL_GUI_INLINE_REWARD_HEADER));
            for (QuestRewardType reward : objectiveDef.getRewards()) {
                builder.addDisplayLore(localization.getLocalizedMessage(mcRPGPlayer,
                        LocalizationKey.QUEST_DETAIL_GUI_INLINE_REWARD_LINE,
                        Map.of("reward", reward.describeForDisplay(mcRPGPlayer))));
            }
        }

        return builder;
    }

    @NotNull
    private String resolveObjectiveStateLabel(@NotNull McRPGLocalizationManager localization,
                                              @NotNull McRPGPlayer mcRPGPlayer,
                                              @NotNull QuestObjectiveState state) {
        var route = switch (state) {
            case NOT_STARTED -> LocalizationKey.QUEST_DETAIL_GUI_OBJECTIVE_STATE_NOT_STARTED;
            case IN_PROGRESS -> LocalizationKey.QUEST_DETAIL_GUI_OBJECTIVE_STATE_IN_PROGRESS;
            case COMPLETED -> LocalizationKey.QUEST_DETAIL_GUI_OBJECTIVE_STATE_COMPLETED;
            case CANCELLED -> LocalizationKey.QUEST_DETAIL_GUI_OBJECTIVE_STATE_CANCELLED;
        };
        return localization.getLocalizedMessage(mcRPGPlayer, route);
    }

    @NotNull
    @Override
    public Set<Class<?>> getValidGuiTypes() {
        return Set.of(QuestDetailGui.class);
    }
}
