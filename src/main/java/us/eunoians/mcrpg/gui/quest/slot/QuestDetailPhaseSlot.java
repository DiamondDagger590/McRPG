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
import us.eunoians.mcrpg.quest.definition.PhaseCompletionMode;
import us.eunoians.mcrpg.quest.definition.QuestPhaseDefinition;
import us.eunoians.mcrpg.quest.reward.QuestRewardType;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Phase header slot in the {@link QuestDetailGui}, showing phase number and completion mode.
 */
public class QuestDetailPhaseSlot implements McRPGSlot {

    private final QuestPhaseDefinition phaseDef;
    private final int totalPhases;

    public QuestDetailPhaseSlot(@NotNull QuestPhaseDefinition phaseDef, int totalPhases) {
        this.phaseDef = phaseDef;
        this.totalPhases = totalPhases;
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
        placeholders.put("phase_number", String.valueOf(phaseDef.getPhaseIndex() + 1));
        placeholders.put("phase_total", String.valueOf(totalPhases));
        placeholders.put("completion_mode", resolveCompletionModeLabel(localization, mcRPGPlayer));

        ItemBuilder builder = ItemBuilder.from(localization.getLocalizedSection(mcRPGPlayer, LocalizationKey.QUEST_DETAIL_GUI_PHASE_HEADER_DISPLAY_ITEM))
                .addPlaceholders(placeholders);
        builder.applyTagReplacements(localization.getPaletteReplacements());

        if (!phaseDef.getRewards().isEmpty()) {
            builder.addDisplayLore("");
            builder.addDisplayLore(localization.getLocalizedMessage(mcRPGPlayer,
                    LocalizationKey.QUEST_DETAIL_GUI_INLINE_REWARD_HEADER));
            for (QuestRewardType reward : phaseDef.getRewards()) {
                builder.addDisplayLore(localization.getLocalizedMessage(mcRPGPlayer,
                        LocalizationKey.QUEST_DETAIL_GUI_INLINE_REWARD_LINE,
                        Map.of("reward", reward.describeForDisplay(mcRPGPlayer))));
            }
        }

        return builder;
    }

    @NotNull
    private String resolveCompletionModeLabel(@NotNull McRPGLocalizationManager localization,
                                              @NotNull McRPGPlayer mcRPGPlayer) {
        var route = phaseDef.getCompletionMode() == PhaseCompletionMode.ANY
                ? LocalizationKey.QUEST_DETAIL_GUI_COMPLETION_MODE_ANY
                : LocalizationKey.QUEST_DETAIL_GUI_COMPLETION_MODE_ALL;
        return localization.getLocalizedMessage(mcRPGPlayer, route);
    }

    @NotNull
    @Override
    public Set<Class<?>> getValidGuiTypes() {
        return Set.of(QuestDetailGui.class);
    }
}
