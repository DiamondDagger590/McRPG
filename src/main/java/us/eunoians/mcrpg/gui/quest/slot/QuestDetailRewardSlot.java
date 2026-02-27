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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

/**
 * Reward summary slot shown in the board preview detail GUI,
 * displaying all rewards the quest can grant.
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

        StringJoiner rewardLines = new StringJoiner("\n");

        for (QuestRewardType reward : definition.getRewards()) {
            appendRewardLine(rewardLines, reward);
        }

        for (QuestPhaseDefinition phase : definition.getPhases()) {
            for (QuestStageDefinition stage : phase.getStages()) {
                for (QuestObjectiveDefinition obj : stage.getObjectives()) {
                    for (QuestRewardType reward : obj.getRewards()) {
                        appendRewardLine(rewardLines, reward);
                    }
                }
            }
        }

        if (rewardLines.length() == 0) {
            rewardLines.add("<gray>No rewards listed");
        }

        placeholders.put("reward_list", rewardLines.toString());
        placeholders.put("quest_name", definition.getDisplayName(mcRPGPlayer));

        return ItemBuilder.from(RegistryAccess.registryAccess()
                        .registry(RegistryKey.MANAGER)
                        .manager(McRPGManagerKey.LOCALIZATION)
                        .getLocalizedSection(mcRPGPlayer, LocalizationKey.QUEST_DETAIL_GUI_REWARD_SLOT_DISPLAY_ITEM))
                .addPlaceholders(placeholders);
    }

    private void appendRewardLine(@NotNull StringJoiner joiner, @NotNull QuestRewardType reward) {
        String typeName = reward.getKey().value().replace('_', ' ');
        String capitalized = typeName.substring(0, 1).toUpperCase() + typeName.substring(1);
        reward.getNumericAmount().ifPresentOrElse(
                amount -> joiner.add("<gold>" + capitalized + ": <white>" + amount),
                () -> joiner.add("<gold>" + capitalized)
        );
    }

    @NotNull
    @Override
    public Set<Class<?>> getValidGuiTypes() {
        return Set.of(QuestDetailGui.class);
    }
}
