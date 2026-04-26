package us.eunoians.mcrpg.gui.quest.slot;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.quest.QuestDetailGui;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.quest.definition.QuestStageDefinition;
import us.eunoians.mcrpg.quest.impl.stage.QuestStageInstance;
import us.eunoians.mcrpg.quest.impl.stage.QuestStageState;
import us.eunoians.mcrpg.quest.reward.QuestRewardType;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Stage header slot in the {@link QuestDetailGui}, showing stage number, objective count, and state.
 * <p>
 * Visually groups objectives that belong to the same stage within a phase, which is especially
 * important for {@link us.eunoians.mcrpg.quest.definition.PhaseCompletionMode#ANY ANY}-mode phases
 * where each stage represents an alternative completion path.
 */
public class QuestDetailStageSlot implements McRPGSlot {

    private final QuestStageDefinition stageDef;
    @Nullable
    private final QuestStageInstance stageInstance;
    private final int stageNumber;
    private final int totalStages;

    /**
     * Creates a new stage header slot.
     *
     * @param stageDef      the stage definition to display
     * @param stageInstance the runtime instance for this stage, or {@code null} for preview/history views
     * @param stageNumber   the 1-based index of this stage within its parent phase
     * @param totalStages   the total number of stages in the parent phase
     */
    public QuestDetailStageSlot(@NotNull QuestStageDefinition stageDef,
                                @Nullable QuestStageInstance stageInstance,
                                int stageNumber,
                                int totalStages) {
        this.stageDef = stageDef;
        this.stageInstance = stageInstance;
        this.stageNumber = stageNumber;
        this.totalStages = totalStages;
    }

    @Override
    public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
        return true;
    }

    @NotNull
    @Override
    public ItemBuilder getItem(@NotNull McRPGPlayer mcRPGPlayer) {
        McRPGLocalizationManager localization = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION);

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("stage_number", String.valueOf(stageNumber));
        placeholders.put("stage_total", String.valueOf(totalStages));
        placeholders.put("objective_count", String.valueOf(stageDef.getObjectives().size()));

        ItemBuilder builder = ItemBuilder.from(localization.getLocalizedSection(mcRPGPlayer,
                        LocalizationKey.QUEST_DETAIL_GUI_STAGE_SLOT_DISPLAY_ITEM))
                .addPlaceholders(placeholders);

        String stateLabel = resolveStageStateLabel(localization, mcRPGPlayer);
        builder.addDisplayLore(localization.getLocalizedMessage(mcRPGPlayer,
                LocalizationKey.QUEST_DETAIL_GUI_STAGE_STATE_LINE,
                Map.of("state", stateLabel)));

        if (!stageDef.getRewards().isEmpty()) {
            builder.addDisplayLore("");
            builder.addDisplayLore(localization.getLocalizedMessage(mcRPGPlayer,
                    LocalizationKey.QUEST_DETAIL_GUI_INLINE_REWARD_HEADER));
            for (QuestRewardType reward : stageDef.getRewards()) {
                builder.addDisplayLore(localization.getLocalizedMessage(mcRPGPlayer,
                        LocalizationKey.QUEST_DETAIL_GUI_INLINE_REWARD_LINE,
                        Map.of("reward", reward.describeForDisplay(mcRPGPlayer))));
            }
        }

        return builder;
    }

    /**
     * Resolves the localized display label for the current stage state, or the preview label
     * when no runtime instance is available.
     *
     * @param localization the localization manager
     * @param mcRPGPlayer  the player viewing the GUI
     * @return the localized state label
     */
    @NotNull
    private String resolveStageStateLabel(@NotNull McRPGLocalizationManager localization,
                                          @NotNull McRPGPlayer mcRPGPlayer) {
        if (stageInstance == null) {
            return localization.getLocalizedMessage(mcRPGPlayer, LocalizationKey.QUEST_DETAIL_GUI_STATE_PREVIEW);
        }
        var route = switch (stageInstance.getQuestStageState()) {
            case NOT_STARTED -> LocalizationKey.QUEST_DETAIL_GUI_STAGE_STATE_NOT_STARTED;
            case IN_PROGRESS -> LocalizationKey.QUEST_DETAIL_GUI_STAGE_STATE_IN_PROGRESS;
            case COMPLETED -> LocalizationKey.QUEST_DETAIL_GUI_STAGE_STATE_COMPLETED;
            case CANCELLED -> LocalizationKey.QUEST_DETAIL_GUI_STAGE_STATE_CANCELLED;
        };
        return localization.getLocalizedMessage(mcRPGPlayer, route);
    }

    @NotNull
    @Override
    public Set<Class<?>> getValidGuiTypes() {
        return Set.of(QuestDetailGui.class);
    }
}
