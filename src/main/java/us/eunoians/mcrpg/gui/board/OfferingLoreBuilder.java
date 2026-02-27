package us.eunoians.mcrpg.gui.board;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.quest.board.rarity.QuestRarity;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.definition.QuestObjectiveDefinition;
import us.eunoians.mcrpg.quest.reward.QuestRewardType;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Stateless utility that builds rich, localized lore for board offering items.
 * All displayed text is resolved through {@link McRPGLocalizationManager} to respect
 * the player's locale chain.
 */
public final class OfferingLoreBuilder {

    private OfferingLoreBuilder() {}

    @NotNull
    private static McRPGLocalizationManager localizationManager() {
        return RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION);
    }

    /**
     * Builds the full lore for a board offering item.
     *
     * @param player          the viewing player (for locale resolution)
     * @param definition      the resolved quest definition
     * @param rarity          the offering's quest rarity
     * @param categoryDisplay the display name for the category
     * @return ordered list of lore components
     */
    @NotNull
    public static List<Component> buildOfferingLore(
            @NotNull McRPGPlayer player,
            @NotNull QuestDefinition definition,
            @NotNull QuestRarity rarity,
            @NotNull String categoryDisplay) {

        McRPGLocalizationManager localization = localizationManager();
        List<Component> lore = new ArrayList<>();

        lore.add(localization.getLocalizedMessageAsComponent(player,
                LocalizationKey.QUEST_BOARD_OFFERING_CATEGORY,
                Map.of("category", categoryDisplay)));

        lore.add(Component.empty());

        lore.addAll(buildObjectiveSummary(player, definition, localization));

        lore.add(Component.empty());

        lore.addAll(buildRewardPreview(player, definition, localization));

        lore.add(Component.empty());
        lore.add(localization.getLocalizedMessageAsComponent(player, LocalizationKey.QUEST_BOARD_CLICK_TO_ACCEPT));

        return lore;
    }

    @NotNull
    private static List<Component> buildObjectiveSummary(
            @NotNull McRPGPlayer player,
            @NotNull QuestDefinition definition,
            @NotNull McRPGLocalizationManager localization) {

        List<Component> lines = new ArrayList<>();
        lines.add(localization.getLocalizedMessageAsComponent(player, LocalizationKey.QUEST_BOARD_OBJECTIVES_HEADER));

        for (var phase : definition.getPhases()) {
            for (var stage : phase.getStages()) {
                for (QuestObjectiveDefinition objective : stage.getObjectives()) {
                    lines.add(localization.getLocalizedMessageAsComponent(player,
                            LocalizationKey.QUEST_BOARD_OBJECTIVE_LINE,
                            Map.of("type", objective.getObjectiveType().getKey().value(),
                                    "amount", String.valueOf(objective.getRequiredProgress()))));
                }
            }
        }
        return lines;
    }

    @NotNull
    private static List<Component> buildRewardPreview(
            @NotNull McRPGPlayer player,
            @NotNull QuestDefinition definition,
            @NotNull McRPGLocalizationManager localization) {

        List<Component> lines = new ArrayList<>();
        lines.add(localization.getLocalizedMessageAsComponent(player, LocalizationKey.QUEST_BOARD_REWARDS_HEADER));

        for (QuestRewardType reward : definition.getRewards()) {
            reward.getNumericAmount().ifPresentOrElse(
                    amount -> lines.add(localization.getLocalizedMessageAsComponent(player,
                            LocalizationKey.QUEST_BOARD_REWARD_LINE,
                            Map.of("type", reward.getKey().value(),
                                    "amount", String.valueOf(amount)))),
                    () -> lines.add(localization.getLocalizedMessageAsComponent(player,
                            LocalizationKey.QUEST_BOARD_REWARD_LINE_NO_AMOUNT,
                            Map.of("type", reward.getKey().value())))
            );
        }
        return lines;
    }

    /**
     * Builds a timer line showing time remaining, localized for the given player.
     *
     * @param player      the viewing player (for locale resolution)
     * @param remainingMs remaining time in milliseconds
     * @return the timer component, or empty if no time remaining
     */
    @NotNull
    public static Optional<Component> buildTimerLine(@NotNull McRPGPlayer player, long remainingMs) {
        if (remainingMs <= 0) {
            return Optional.empty();
        }
        McRPGLocalizationManager localization = localizationManager();
        return Optional.of(localization.getLocalizedMessageAsComponent(player,
                LocalizationKey.QUEST_BOARD_EXPIRES_IN,
                Map.of("time", McRPGMethods.formatDuration(remainingMs))));
    }

}
