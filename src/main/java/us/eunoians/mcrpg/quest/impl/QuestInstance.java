package us.eunoians.mcrpg.quest.impl;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.event.quest.QuestCancelEvent;
import us.eunoians.mcrpg.event.quest.QuestCompleteEvent;
import us.eunoians.mcrpg.event.quest.QuestExpireEvent;
import us.eunoians.mcrpg.event.quest.QuestStartEvent;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.MainConfigFile;
import us.eunoians.mcrpg.database.table.quest.PendingRewardDAO;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.definition.QuestObjectiveDefinition;
import us.eunoians.mcrpg.quest.definition.QuestPhaseDefinition;
import us.eunoians.mcrpg.quest.reward.PendingReward;
import us.eunoians.mcrpg.quest.reward.QuestRewardType;
import us.eunoians.mcrpg.quest.definition.QuestStageDefinition;
import us.eunoians.mcrpg.quest.impl.objective.QuestObjectiveInstance;
import us.eunoians.mcrpg.quest.impl.scope.QuestScope;
import us.eunoians.mcrpg.quest.impl.stage.QuestStageInstance;
import us.eunoians.mcrpg.quest.impl.stage.QuestStageState;
import us.eunoians.mcrpg.quest.source.QuestSource;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.UUID;

/**
 * Mutable runtime instance of a quest, tracking state, timestamps, scope, and child stage instances.
 * <p>
 * Created from a {@link QuestDefinition} and persisted to SQL.
 */
public class QuestInstance {

    private final UUID questUUID;
    private final NamespacedKey questKey;
    private final NamespacedKey scopeType;
    private QuestState questState;
    private QuestScope questScope;
    private Long startTime;
    private Long endTime;
    private Long expirationTime;

    private final List<QuestStageInstance> questStageInstances;
    private volatile boolean dirty;
    private final QuestSource questSource;
    private final String scopeDisplayName;
    private NamespacedKey boardRarityKey;

    /**
     * Creates a new quest instance from a definition with an explicit quest source.
     *
     * @param definition       the quest definition to instantiate
     * @param scope            the scope to assign, or {@code null} to assign later
     * @param variables        variables for resolving dynamic required progress values
     * @param questSource      the source that originated this quest
     * @param scopeDisplayName the display name for the scope context, or {@code null}
     */
    public QuestInstance(@NotNull QuestDefinition definition,
                         @Nullable QuestScope scope,
                         @NotNull Map<String, Object> variables,
                         @NotNull QuestSource questSource,
                         @Nullable String scopeDisplayName) {
        this.questKey = definition.getQuestKey();
        this.scopeType = definition.getScopeType();
        this.questUUID = UUID.randomUUID();
        this.questState = QuestState.NOT_STARTED;
        this.questStageInstances = new ArrayList<>();
        this.questSource = questSource;
        this.scopeDisplayName = scopeDisplayName;

        if (scope != null) {
            this.questScope = scope;
        }

        definition.getExpiration().ifPresent(expiration -> {
            long now = McRPG.getInstance().getTimeProvider().now().toEpochMilli();
            this.expirationTime = now + expiration.toMillis();
        });

        for (QuestPhaseDefinition phaseDef : definition.getPhases()) {
            for (QuestStageDefinition stageDef : phaseDef.getStages()) {
                QuestStageInstance stageInstance = new QuestStageInstance(
                        stageDef.getStageKey(),
                        phaseDef.getPhaseIndex(),
                        this
                );

                for (QuestObjectiveDefinition objDef : stageDef.getObjectives()) {
                    QuestObjectiveInstance objInstance = new QuestObjectiveInstance(
                            objDef.getObjectiveKey(),
                            stageInstance
                    );
                    objInstance.setRequiredProgression(objDef.resolveRequiredProgress(variables));
                    stageInstance.addQuestObjective(objInstance);
                }

                this.questStageInstances.add(stageInstance);
            }
        }
    }

    /**
     * Reconstruction constructor for loading a quest instance from the database.
     *
     * @param questKey         the definition key
     * @param questUUID        the persisted UUID
     * @param scopeType        the scope type key
     * @param questState       the persisted state
     * @param questScope       the scope, or {@code null} if not yet loaded
     * @param startTime        the start timestamp in epoch millis, or {@code null}
     * @param endTime          the end timestamp in epoch millis, or {@code null}
     * @param expirationTime   the expiration timestamp in epoch millis, or {@code null}
     * @param questSource      the source that originated this quest
     * @param scopeDisplayName the display name for the scope context, or {@code null}
     */
    public QuestInstance(@NotNull NamespacedKey questKey, @NotNull UUID questUUID, @NotNull NamespacedKey scopeType,
                         @NotNull QuestState questState, @Nullable QuestScope questScope,
                         @Nullable Long startTime, @Nullable Long endTime, @Nullable Long expirationTime,
                         @NotNull QuestSource questSource, @Nullable String scopeDisplayName) {
        this.questKey = questKey;
        this.questUUID = questUUID;
        this.scopeType = scopeType;
        this.questScope = questScope;
        this.questState = questState;
        this.startTime = startTime;
        this.endTime = endTime;
        this.expirationTime = expirationTime;
        this.questStageInstances = new ArrayList<>();
        this.questSource = questSource;
        this.scopeDisplayName = scopeDisplayName;
    }

    /**
     * Appends a stage instance to the end of this quest's stage list.
     *
     * @param questStageInstance the stage instance to add
     */
    public void addQuestStage(@NotNull QuestStageInstance questStageInstance) {
        this.questStageInstances.add(questStageInstance);
    }

    /**
     * Inserts a stage instance at the specified index in this quest's stage list.
     *
     * @param questStageInstance the stage instance to add
     * @param index              the index at which to insert
     */
    public void addQuestStage(@NotNull QuestStageInstance questStageInstance, int index) {
        this.questStageInstances.add(index, questStageInstance);
    }

    /**
     * Appends all provided stage instances to this quest's stage list.
     *
     * @param questStageInstances the stage instances to add
     */
    public void addQuestStageInstances(@NotNull Collection<QuestStageInstance> questStageInstances) {
        this.questStageInstances.addAll(questStageInstances);
    }

    /**
     * Gets an immutable copy of all stage instances belonging to this quest.
     *
     * @return an immutable list of stage instances
     */
    @NotNull
    public List<QuestStageInstance> getQuestStageInstances() {
        return List.copyOf(this.questStageInstances);
    }

    /**
     * Gets an immutable list of all stages belonging to the given phase index.
     *
     * @param phaseIndex the zero-based phase index
     * @return an immutable list of the stage instances for that phase
     */
    @NotNull
    public List<QuestStageInstance> getStagesForPhase(int phaseIndex) {
        return List.copyOf(questStageInstances.stream()
                .filter(stage -> stage.getPhaseIndex() == phaseIndex)
                .toList());
    }

    /**
     * Gets the first stage instance that is currently in progress.
     *
     * @return the first active stage, or empty if none are in progress
     */
    @NotNull
    public Optional<QuestStageInstance> getActiveQuestStage() {
        return questStageInstances.stream()
                .filter(stage -> stage.getQuestStageState() == QuestStageState.IN_PROGRESS)
                .findFirst();
    }

    /**
     * Gets an immutable list of all currently active (in-progress) stages across all phases.
     *
     * @return an immutable list of in-progress stage instances
     */
    @NotNull
    public List<QuestStageInstance> getActiveQuestStages() {
        return List.copyOf(questStageInstances.stream()
                .filter(stage -> stage.getQuestStageState() == QuestStageState.IN_PROGRESS)
                .toList());
    }

    /**
     * Sets the scope for this quest instance. Can only be called once; subsequent calls
     * will throw {@link IllegalStateException}.
     *
     * @param questScope the scope to assign
     * @throws IllegalStateException if a scope has already been set
     */
    public void setQuestScope(@NotNull QuestScope questScope) {
        if (this.questScope == null) {
            this.questScope = questScope;
        } else {
            throw new IllegalStateException(String.format("QuestScope already set for quest %s", questUUID));
        }
    }

    /**
     * Gets the scope assigned to this quest, if any.
     *
     * @return the quest scope, or empty if not yet assigned
     */
    @NotNull
    public Optional<QuestScope> getQuestScope() {
        return Optional.ofNullable(questScope);
    }

    /**
     * Gets the unique identifier for this quest instance.
     *
     * @return the quest UUID
     */
    @NotNull
    public UUID getQuestUUID() {
        return questUUID;
    }

    /**
     * Gets the {@link NamespacedKey} of the definition this instance was created from.
     *
     * @return the definition key
     */
    @NotNull
    public NamespacedKey getQuestKey() {
        return questKey;
    }

    /**
     * Gets the {@link NamespacedKey} identifying the scope type for this quest instance.
     *
     * @return the scope type key
     */
    @NotNull
    public NamespacedKey getScopeType() {
        return scopeType;
    }

    /**
     * Gets the current state of this quest instance.
     *
     * @return the quest state
     */
    @NotNull
    public QuestState getQuestState() {
        return questState;
    }

    /**
     * Gets the timestamp (epoch millis) when this quest was activated, if it has been started.
     *
     * @return an {@link Optional} containing the start time, or empty if not yet started
     */
    @NotNull
    public Optional<Long> getStartTime() {
        return Optional.ofNullable(startTime);
    }

    /**
     * Gets the timestamp (epoch millis) when this quest ended (completed or cancelled),
     * if it has ended.
     *
     * @return an {@link Optional} containing the end time, or empty if still active
     */
    @NotNull
    public Optional<Long> getEndTime() {
        return Optional.ofNullable(endTime);
    }

    /**
     * Gets the timestamp (epoch millis) at which this quest expires, if it has an expiration.
     *
     * @return an {@link Optional} containing the expiration time, or empty if the quest does not expire
     */
    @NotNull
    public Optional<Long> getExpirationTime() {
        return Optional.ofNullable(expirationTime);
    }

    /**
     * Gets the source that originated this quest.
     *
     * @return the quest source
     */
    @NotNull
    public QuestSource getQuestSource() {
        return questSource;
    }

    /**
     * Gets the display name for the quest's scope context, resolved at creation time.
     *
     * @return an {@link Optional} containing the scope display name, or empty
     */
    @NotNull
    public Optional<String> getScopeDisplayName() {
        return Optional.ofNullable(scopeDisplayName);
    }

    /**
     * Gets the board rarity key associated with this quest instance, if it was accepted
     * from a board offering. Used by the distribution resolver for rarity-gated tiers.
     *
     * @return an {@link Optional} containing the rarity key, or empty if not board-sourced
     */
    @NotNull
    public Optional<NamespacedKey> getBoardRarityKey() {
        return Optional.ofNullable(boardRarityKey);
    }

    /**
     * Sets the board rarity key for this quest instance. Should be called once during quest
     * acceptance from a {@link us.eunoians.mcrpg.quest.board.BoardOffering}.
     *
     * @param boardRarityKey the rarity key from the offering
     */
    public void setBoardRarityKey(@NotNull NamespacedKey boardRarityKey) {
        this.boardRarityKey = boardRarityKey;
    }

    /**
     * Checks whether this quest has unsaved changes since the last save or creation.
     *
     * @return {@code true} if the quest has been modified since it was last saved
     */
    public boolean isDirty() {
        return dirty;
    }

    /**
     * Marks this quest as having unsaved changes. Called internally when objective progress
     * is applied or state transitions occur.
     */
    public void markDirty() {
        this.dirty = true;
    }

    /**
     * Clears the dirty flag, indicating all changes have been persisted.
     * Called after a successful save.
     */
    public void clearDirty() {
        this.dirty = false;
    }

    /**
     * Sets the expiration timestamp for this quest instance.
     *
     * @param expirationTime the expiration time in epoch millis, or {@code null} to remove expiration
     */
    public void setExpirationTime(@Nullable Long expirationTime) {
        this.expirationTime = expirationTime;
    }

    /**
     * Checks if this quest instance has expired based on the current time.
     *
     * @return {@code true} if the quest has an expiration time and it has passed
     */
    public boolean isExpired() {
        if (expirationTime == null) {
            return false;
        }
        return McRPG.getInstance().getTimeProvider().now().toEpochMilli() >= expirationTime;
    }

    /**
     * Starts this quest by activating it, activating all stages in phase 0, and firing
     * a {@link QuestStartEvent}.
     *
     * @param definition the quest definition this instance was created from
     */
    public void start(@NotNull QuestDefinition definition) {
        activate();
        for (QuestStageInstance stage : getStagesForPhase(0)) {
            stage.activate();
        }
        Bukkit.getPluginManager().callEvent(new QuestStartEvent(this, definition));
    }

    /**
     * Completes this quest by marking it as {@link QuestState#COMPLETED} and firing
     * a {@link QuestCompleteEvent}. Called by the internal state listener when the
     * last phase finishes. Does nothing if the quest is not currently in progress.
     *
     * @param definition the quest definition this instance was created from
     */
    public void complete(@NotNull QuestDefinition definition) {
        if (questState != QuestState.IN_PROGRESS) {
            return;
        }
        markAsCompleted();
        Bukkit.getPluginManager().callEvent(new QuestCompleteEvent(this, definition));
        saveAsync();
    }

    /**
     * Expires this quest by firing a {@link QuestExpireEvent} and then cancelling it.
     * Does nothing if the quest is not currently in progress or not yet started.
     */
    public void expire() {
        if (questState != QuestState.IN_PROGRESS && questState != QuestState.NOT_STARTED) {
            return;
        }
        Bukkit.getPluginManager().callEvent(new QuestExpireEvent(this));
        cancel();
    }

    /**
     * Cancels this quest and all of its in-progress stages, then fires a {@link QuestCancelEvent}.
     */
    public void cancel() {
        if (questState == QuestState.IN_PROGRESS || questState == QuestState.NOT_STARTED) {
            questState = QuestState.CANCELLED;
            endTime = McRPG.getInstance().getTimeProvider().now().toEpochMilli();
            for (QuestStageInstance stage : questStageInstances) {
                stage.cancel();
            }
            Bukkit.getPluginManager().callEvent(new QuestCancelEvent(this));
            saveAsync();
        }
    }

    /**
     * Grants the provided rewards to all players currently in this quest's scope.
     * Online players receive rewards immediately. Offline players have their rewards
     * queued in the database via {@link PendingRewardDAO} and will receive them on
     * their next login, subject to a configurable expiry.
     *
     * @param rewards the configured reward types to grant
     */
    public void grantRewards(@NotNull List<QuestRewardType> rewards) {
        if (rewards.isEmpty()) {
            return;
        }
        getQuestScope().ifPresent(scope -> {
            for (UUID playerUUID : scope.getCurrentPlayersInScope()) {
                Player player = Bukkit.getPlayer(playerUUID);
                if (player != null && player.isOnline()) {
                    for (QuestRewardType reward : rewards) {
                        reward.grant(player);
                    }
                } else {
                    queueRewardsForOfflinePlayer(playerUUID, rewards);
                }
            }
        });
    }

    /**
     * Queues rewards for an offline player by persisting them to the database.
     * The rewards will be granted when the player next logs in.
     *
     * @param playerUUID the UUID of the offline player
     * @param rewards    the rewards to queue
     */
    private void queueRewardsForOfflinePlayer(@NotNull UUID playerUUID, @NotNull List<QuestRewardType> rewards) {
        int expiryDays = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.FILE)
                .getFile(FileType.MAIN_CONFIG)
                .getInt(MainConfigFile.QUEST_PENDING_REWARDS_EXPIRY_DAYS, 30);
        long now = McRPG.getInstance().getTimeProvider().now().toEpochMilli();
        long expiresAt = now + TimeUnit.DAYS.toMillis(expiryDays);

        Database database = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.DATABASE).getDatabase();
        database.getDatabaseExecutorService().submit(() -> {
            try (Connection connection = database.getConnection()) {
                for (QuestRewardType reward : rewards) {
                    PendingReward pending = new PendingReward(
                            UUID.randomUUID(),
                            playerUUID,
                            reward.getKey(),
                            reward.serializeConfig(),
                            questKey,
                            now,
                            expiresAt
                    );
                    for (PreparedStatement stmt : PendingRewardDAO.savePendingReward(connection, pending)) {
                        stmt.executeUpdate();
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Triggers an asynchronous save of this quest's full tree via the {@link QuestManager}.
     */
    public void saveAsync() {
        QuestManager questManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.QUEST);
        questManager.saveQuestAsync(this);
    }

    /**
     * Internal helper that transitions this quest to {@link QuestState#IN_PROGRESS}
     * and records the start time.
     */
    private void activate() {
        if (questState == QuestState.NOT_STARTED) {
            questState = QuestState.IN_PROGRESS;
            startTime = McRPG.getInstance().getTimeProvider().now().toEpochMilli();
        }
    }

    /**
     * Internal helper that transitions this quest to {@link QuestState#COMPLETED}
     * and records the end time.
     */
    private void markAsCompleted() {
        if (questState == QuestState.IN_PROGRESS && endTime == null) {
            questState = QuestState.COMPLETED;
            endTime = McRPG.getInstance().getTimeProvider().now().toEpochMilli();
        }
    }
}
