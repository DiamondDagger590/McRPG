package us.eunoians.mcrpg.quest.board;

import com.diamonddagger590.mccore.configuration.ReloadableContent;
import com.diamonddagger590.mccore.configuration.common.ReloadableInteger;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.configuration.file.BoardConfigFile;

import java.util.Optional;
import java.util.Set;

/**
 * Represents a single quest board. Phase 1 has one global board ({@code mcrpg:default_board}).
 * <p>
 * Config-derived values use {@link ReloadableContent} wrappers so they stay in sync with
 * {@code board.yml} on plugin reload.
 */
public class QuestBoard {

    private final NamespacedKey boardKey;
    private final ReloadableInteger maxAcceptedQuests;
    private final ReloadableInteger minimumTotalOfferings;
    private BoardRotation currentDailyRotation;
    private BoardRotation currentWeeklyRotation;

    public QuestBoard(@NotNull NamespacedKey boardKey,
                      @NotNull YamlDocument boardConfig) {
        this.boardKey = boardKey;
        this.maxAcceptedQuests = new ReloadableInteger(boardConfig, BoardConfigFile.MAX_ACCEPTED_QUESTS);
        this.minimumTotalOfferings = new ReloadableInteger(boardConfig, BoardConfigFile.MINIMUM_TOTAL_OFFERINGS);
    }

    /**
     * Returns the unique identifier for this board.
     *
     * @return the board key
     */
    @NotNull
    public NamespacedKey getBoardKey() {
        return boardKey;
    }

    /**
     * Returns the maximum number of board quests a player can have active simultaneously.
     * This is the base value before permission-based bonuses.
     *
     * @return the max accepted quest count
     */
    public int getMaxAcceptedQuests() {
        return maxAcceptedQuests.getContent();
    }

    /**
     * Returns the minimum total offerings the board must display each rotation.
     * Used as a floor during slot allocation.
     *
     * @return the minimum total offerings
     */
    public int getMinimumTotalOfferings() {
        return minimumTotalOfferings.getContent();
    }

    /**
     * Returns the current daily rotation, if one has been loaded or triggered.
     *
     * @return the current daily rotation, or empty if none is active
     */
    @NotNull
    public Optional<BoardRotation> getCurrentDailyRotation() {
        return Optional.ofNullable(currentDailyRotation);
    }

    /**
     * Sets the current daily rotation. Called when a new daily rotation is triggered
     * or when loading the current rotation from the database at startup.
     *
     * @param rotation the new daily rotation
     */
    public void setCurrentDailyRotation(@NotNull BoardRotation rotation) {
        this.currentDailyRotation = rotation;
    }

    /**
     * Returns the current weekly rotation, if one has been loaded or triggered.
     *
     * @return the current weekly rotation, or empty if none is active
     */
    @NotNull
    public Optional<BoardRotation> getCurrentWeeklyRotation() {
        return Optional.ofNullable(currentWeeklyRotation);
    }

    /**
     * Sets the current weekly rotation. Called when a new weekly rotation is triggered
     * or when loading the current rotation from the database at startup.
     *
     * @param rotation the new weekly rotation
     */
    public void setCurrentWeeklyRotation(@NotNull BoardRotation rotation) {
        this.currentWeeklyRotation = rotation;
    }

    /**
     * Returns all {@link ReloadableContent} fields for tracking with the
     * {@link com.diamonddagger590.mccore.configuration.ReloadableContentManager}.
     *
     * @return the set of reloadable content instances
     */
    @NotNull
    public Set<ReloadableContent<?>> getReloadableContent() {
        return Set.of(maxAcceptedQuests, minimumTotalOfferings);
    }
}
