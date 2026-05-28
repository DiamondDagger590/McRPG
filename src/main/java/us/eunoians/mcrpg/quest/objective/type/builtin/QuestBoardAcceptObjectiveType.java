package us.eunoians.mcrpg.quest.objective.type.builtin;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.quest.impl.objective.QuestObjectiveInstance;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveProgressContext;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveType;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.Optional;

/**
 * Built-in objective type for tracking when a player accepts an offering from a quest board.
 * <p>
 * Optionally filters to a specific board via the {@code board} config key. When no filter is
 * configured, any board acceptance counts toward progress. Because this is a purely event-driven
 * objective, auto-complete is not supported.
 */
public class QuestBoardAcceptObjectiveType implements QuestObjectiveType {

    public static final NamespacedKey KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "quest_board_accept");

    @Nullable
    private final NamespacedKey boardFilter;

    /**
     * Creates an unconfigured base instance for registry registration.
     */
    public QuestBoardAcceptObjectiveType() {
        this.boardFilter = null;
    }

    private QuestBoardAcceptObjectiveType(@Nullable NamespacedKey boardFilter) {
        this.boardFilter = boardFilter;
    }

    @NotNull
    @Override
    public NamespacedKey getKey() {
        return KEY;
    }

    @NotNull
    @Override
    public QuestBoardAcceptObjectiveType parseConfig(@NotNull Section section) {
        NamespacedKey parsedBoard = null;
        if (section.contains("board")) {
            String rawBoard = section.getString("board");
            parsedBoard = NamespacedKey.fromString(rawBoard);
            if (parsedBoard == null) {
                McRPG.getInstance().getLogger().warning(
                        "quest_board_accept objective has invalid 'board' value: '"
                                + rawBoard + "' — objective will never match");
                parsedBoard = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "unknown");
            }
        }
        return new QuestBoardAcceptObjectiveType(parsedBoard);
    }

    @Override
    public boolean canProcess(@NotNull QuestObjectiveProgressContext context) {
        return context instanceof QuestBoardAcceptQuestContext;
    }

    @Override
    public long processProgress(@NotNull QuestObjectiveInstance instance,
                                @NotNull QuestObjectiveProgressContext context) {
        if (!(context instanceof QuestBoardAcceptQuestContext boardContext)) {
            return 0;
        }
        if (boardFilter != null && !boardFilter.equals(boardContext.getBoardKey())) {
            return 0;
        }
        return 1;
    }

    @NotNull
    @Override
    public String describeObjective(@NotNull McRPGPlayer player, long requiredProgress) {
        var localization = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION);
        return localization.getLocalizedMessage(player, LocalizationKey.QUEST_OBJECTIVE_QUEST_BOARD_ACCEPT_FORMAT);
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.of(McRPGExpansion.EXPANSION_KEY);
    }
}
