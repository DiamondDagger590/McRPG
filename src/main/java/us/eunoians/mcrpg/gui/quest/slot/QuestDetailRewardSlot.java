package us.eunoians.mcrpg.gui.quest.slot;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.quest.QuestDetailGui;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.definition.QuestObjectiveDefinition;
import us.eunoians.mcrpg.quest.definition.QuestPhaseDefinition;
import us.eunoians.mcrpg.quest.definition.QuestStageDefinition;
import us.eunoians.mcrpg.quest.reward.QuestRewardType;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Reward summary slot shown in the board preview detail GUI,
 * displaying all rewards the quest can grant grouped by source level.
 */
public class QuestDetailRewardSlot implements McRPGSlot {

    private final QuestDefinition definition;
    private final McRPGPlayer viewer;

    public QuestDetailRewardSlot(@NotNull QuestDefinition definition, @NotNull McRPGPlayer viewer) {
        this.definition = definition;
        this.viewer = viewer;
    }

    @Override
    public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
        return true;
    }

    @NotNull
    @Override
    public ItemBuilder getItem(@NotNull McRPGPlayer mcRPGPlayer) {
        Map<String, String> placeholders = new HashMap<>();
        List<String> rewardLines = new ArrayList<>();
        boolean hasAnyReward = false;

        // Quest-level rewards
        List<QuestRewardType> questRewards = definition.getRewards();
        if (!questRewards.isEmpty()) {
            rewardLines.add("<gold><bold>Completion Rewards:");
            for (QuestRewardType reward : questRewards) {
                rewardLines.add("<gray>  - <white>" + reward.describeForDisplay());
            }
            hasAnyReward = true;
        }

        // Phase/stage/objective-level rewards
        int phaseNum = 0;
        for (QuestPhaseDefinition phase : definition.getPhases()) {
            phaseNum++;
            for (QuestStageDefinition stage : phase.getStages()) {
                for (QuestObjectiveDefinition obj : stage.getObjectives()) {
                    List<QuestRewardType> objRewards = obj.getRewards();
                    if (!objRewards.isEmpty()) {
                        if (hasAnyReward) {
                            rewardLines.add("");
                        }
                        String objDesc = obj.getDescription(mcRPGPlayer, definition.getQuestKey());
                        String objLabel = truncate(objDesc, 40);
                        rewardLines.add("<gold>Phase " + phaseNum + " - " + objLabel + ":");
                        for (QuestRewardType reward : objRewards) {
                            rewardLines.add("<gray>  - <white>" + reward.describeForDisplay());
                        }
                        hasAnyReward = true;
                    }
                }
            }
        }

        if (!hasAnyReward) {
            rewardLines.add("<gray>No rewards listed");
        }

        placeholders.put("quest_name", definition.getDisplayName(mcRPGPlayer));

        ItemBuilder builder = ItemBuilder.from(RegistryAccess.registryAccess()
                        .registry(RegistryKey.MANAGER)
                        .manager(McRPGManagerKey.LOCALIZATION)
                        .getLocalizedSection(mcRPGPlayer, LocalizationKey.QUEST_DETAIL_GUI_REWARD_SLOT_DISPLAY_ITEM))
                .addPlaceholders(placeholders);

        for (String line : rewardLines) {
            builder.addDisplayLore(line);
        }

        return builder;
    }

    @NotNull
    private static String truncate(@NotNull String text, int maxLen) {
        String firstLine = text.contains("\n") ? text.substring(0, text.indexOf('\n')) : text;
        if (firstLine.length() <= maxLen) {
            return firstLine;
        }
        return firstLine.substring(0, maxLen - 3) + "...";
    }

    @NotNull
    @Override
    public Set<Class<?>> getValidGuiTypes() {
        return Set.of(QuestDetailGui.class);
    }
}
