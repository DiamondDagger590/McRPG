package us.eunoians.mcrpg.entity.holder;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * A holder that associates a player with their tracked quests.
 * <p>
 * Delegates to the new {@link QuestManager} for quest lookups via the player contribution index.
 */
public class QuestHolder {

    private final UUID uuid;
    private volatile int activeBoardQuestCount;

    /**
     * Creates a new quest holder for the given player UUID.
     *
     * @param uuid the player's UUID
     */
    public QuestHolder(@NotNull UUID uuid) {
        this.uuid = uuid;
    }

    /**
     * Gets the UUID of this holder.
     *
     * @return the holder's UUID
     */
    @NotNull
    public UUID getUUID() {
        return uuid;
    }

    /**
     * Gets the set of active quest UUIDs for this holder by querying the new quest system's
     * player contribution index.
     *
     * @return an immutable set of active quest UUIDs
     */
    @NotNull
    public Set<UUID> getActiveQuests() {
        QuestManager questManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.QUEST);
        return questManager.getIndexedQuestUUIDs(uuid);
    }

    /**
     * Checks if the given quest UUID is currently active for this holder.
     *
     * @param questUUID the quest UUID to check
     * @return {@code true} if the quest is active for this holder
     */
    public boolean isQuestActive(@NotNull UUID questUUID) {
        return getActiveQuests().contains(questUUID);
    }

    /**
     * Gets the number of active board quests this player currently has.
     * Seeded during player load from the database and maintained in-memory
     * via {@link #incrementBoardQuestCount()} / {@link #decrementBoardQuestCount()}.
     *
     * @return the current active board quest count
     */
    public int getActiveBoardQuestCount() {
        return activeBoardQuestCount;
    }

    /**
     * Sets the initial active board quest count. Called once during player load
     * after querying the database.
     *
     * @param count the count from the database
     */
    public void setActiveBoardQuestCount(int count) {
        this.activeBoardQuestCount = count;
    }

    /**
     * Increments the active board quest count by one. Called when a player
     * accepts a quest from the board.
     */
    public void incrementBoardQuestCount() {
        this.activeBoardQuestCount++;
    }

    /**
     * Decrements the active board quest count by one. Called when a board quest
     * is completed, cancelled, or expired. Will not go below zero.
     */
    public void decrementBoardQuestCount() {
        if (this.activeBoardQuestCount > 0) {
            this.activeBoardQuestCount--;
        }
    }

}
