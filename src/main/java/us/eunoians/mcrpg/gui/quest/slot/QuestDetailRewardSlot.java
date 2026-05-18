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
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.reward.QuestRewardType;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Reward summary slot shown in the quest detail GUI, displaying only
 * quest-completion rewards. Phase, stage, and objective rewards are shown
 * inline on their respective slots.
 */
public class QuestDetailRewardSlot implements McRPGSlot {

    private final QuestDefinition definition;

    public QuestDetailRewardSlot(@NotNull QuestDefinition definition) {
        this.definition = definition;
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
        List<String> rewardLines = new ArrayList<>();

        List<QuestRewardType> questRewards = definition.getRewards();
        if (!questRewards.isEmpty()) {
            rewardLines.add(localization.getLocalizedMessage(mcRPGPlayer,
                    LocalizationKey.QUEST_DETAIL_GUI_REWARD_COMPLETION_HEADER));
            for (QuestRewardType reward : questRewards) {
                rewardLines.add(localization.getLocalizedMessage(mcRPGPlayer,
                        LocalizationKey.QUEST_DETAIL_GUI_REWARD_ENTRY_LINE,
                        Map.of("reward", reward.describeForDisplay(mcRPGPlayer))));
            }
        } else {
            rewardLines.add(localization.getLocalizedMessage(mcRPGPlayer,
                    LocalizationKey.QUEST_DETAIL_GUI_REWARD_SLOT_NO_REWARDS));
        }

        placeholders.put("quest_name", definition.getDisplayName(mcRPGPlayer));

        ItemBuilder builder = ItemBuilder.from(localization.getLocalizedSection(mcRPGPlayer,
                        LocalizationKey.QUEST_DETAIL_GUI_REWARD_SLOT_DISPLAY_ITEM))
                .addPlaceholders(placeholders);
        builder.applyTagReplacements(localization.getPaletteReplacements());

        for (String line : rewardLines) {
            builder.addDisplayLore(line);
        }

        return builder;
    }

    @NotNull
    @Override
    public Set<Class<?>> getValidGuiTypes() {
        return Set.of(QuestDetailGui.class);
    }
}
