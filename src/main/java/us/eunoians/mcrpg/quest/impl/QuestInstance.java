package us.eunoians.mcrpg.quest.impl;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.database.transaction.FailSafeTransaction;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.util.Methods;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.event.quest.QuestCancelEvent;
import us.eunoians.mcrpg.event.quest.QuestCompleteEvent;
import us.eunoians.mcrpg.event.quest.QuestExpireEvent;
import us.eunoians.mcrpg.event.quest.PreQuestStartEvent;
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
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.UUID;
import java.util.logging.Level;

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
    private Instant startTime;
    private Instant endTime;
    private Instant expirationTime;

    private final List<QuestStageInstance> questStageInstances;
    private volatile boolean dirty;
    private final QuestSource questSource;
    private final String scopeDisplayName;
    private NamespacedKey boardRarityKey;
    private boolean nearExpiryNotified;

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
            Instant now = McRPG.getInstance().getTimeProvider().now();
            this.expirationTime = now.plus(expiration);
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
     * @param startTime        the start timestamp, or {@code null}
     * @param endTime          the end timestamp, or {@code null}
     * @param expirationTime   the expiration timestamp, or {@code null}
     * @param questSource      the source that originated this quest
     * @param scopeDisplayName the display name for the scope context, or {@code null}
     */
    public QuestInstance(@NotNull NamespacedKey questKey, @NotNull UUID questUUID, @NotNull NamespacedKey scopeType,
                         @NotNull QuestState questState, @Nullable QuestScope questScope,
                         @Nullable Instant startTime, @Nullable Instant endTime, @Nullable Instant expirationTime,
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
     * Returns the overall completion fraction of this quest as a value in {@code [0.0, 1.0]},
     * computed by summing current and required progression across every objective in every stage.
     * Returns {@code 0.0} when the total required progression is zero.
     *
     * @return A {@code double} in the range {@code [0.0, 1.0]}.
     */
    public double getOverallProgress() {
        long totalRequired = 0;
        long totalCurrent = 0;
        for (QuestStageInstance stage : questStageInstances) {
            for (QuestObjectiveInstance obj : stage.getQuestObjectives()) {
                totalRequired += obj.getRequiredProgression();
                totalCurrent += obj.getCurrentProgression();
            }
        }
        return totalRequired > 0 ? (double) totalCurrent / totalRequired : 0.0;
    }

    /**
     * Returns the overall completion progress of this quest rendered as a MiniMessage-formatted
     * progress bar string produced by {@link com.diamonddagger590.mccore.util.Methods#getProgressBarAsString}.
     *
     * @param barLength The number of characters in the progress bar.
     * @return A MiniMessage-formatted progress bar string.
     */
    @NotNull
    public String getOverallProgressBar(int barLength) {
        return Methods.getProgressBarAsString(getOverallProgress(), barLength);
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
     * Gets the timestamp when this quest was activated, if it has been started.
     *
     * @return an {@link Optional} containing the start time, or empty if not yet started
     */
    @NotNull
    public Optional<Instant> getStartTime() {
        return Optional.ofNullable(startTime);
    }

    /**
     * Gets the timestamp when this quest ended (completed or cancelled), if it has ended.
     *
     * @return an {@link Optional} containing the end time, or empty if still active
     */
    @NotNull
    public Optional<Instant> getEndTime() {
        return Optional.ofNullable(endTime);
    }

    /**
     * Gets the timestamp at which this quest expires, if it has an expiration.
     *
     * @return an {@link Optional} containing the expiration time, or empty if the quest does not expire
     */
    @NotNull
    public Optional<Instant> getExpirationTime() {
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
     * @param expirationTime the expiration time, or {@code null} to remove expiration
     */
    public void setExpirationTime(@Nullable Instant expirationTime) {
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
        return !McRPG.getInstance().getTimeProvider().now().isBefore(expirationTime);
    }

    /**
     * Returns whether the periodic near-expiry scan has already sent a notification
     * for this quest instance. This flag is only used by the scan task; the login
     * reminder path deliberately ignores it so players always receive a reminder on login.
     *
     * @return {@code true} if the scan has already notified for this quest
     */
    public boolean isNearExpiryNotified() {
        return nearExpiryNotified;
    }

    /**
     * Marks that the periodic scan has sent a near-expiry notification for this quest.
     * Once set, the scan will not send another notification for the same instance.
     *
     * @param notified {@code true} to mark as notified, {@code false} to clear the flag
     */
    public void setNearExpiryNotified(boolean notified) {
        this.nearExpiryNotified = notified;
    }

    /**
     * Transitions this instance to {@link QuestState#IN_PROGRESS}, activates phase-0 stages,
     * and fires {@link QuestStartEvent}.
     * <p>
     * Marked internal because all quest starts must route through {@link QuestManager#startQuest}
     * so {@link PreQuestStartEvent} can gate the operation — direct external calls would bypass
     * that gate.
     *
     * @param definition  the quest definition driving this instance
     * @param starterUUID the UUID of the player who initiated the quest start, or {@code null} if system-initiated
     */
    @ApiStatus.Internal
    public void start(@NotNull QuestDefinition definition, @Nullable UUID starterUUID) {
        activate();
        for (QuestStageInstance stage : getStagesForPhase(0)) {
            stage.activate();
        }
        Bukkit.getPluginManager().callEvent(new QuestStartEvent(this, definition, questSource, starterUUID));
    }

    /**
     * Overload for backward compatibility (system-initiated or test-driven starts without a specific starter).
     *
     * @param definition the quest definition driving this instance
     */
    @ApiStatus.Internal
    public void start(@NotNull QuestDefinition definition) {
        start(definition, null);
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
        cancelAsExpiration();
    }

    /**
     * Cancels this quest and all of its in-progress stages, then fires a {@link QuestCancelEvent}.
     */
    public void cancel() {
        cancelInternal(false);
    }

    /**
     * Cancels this quest as the result of expiration and fires a {@link QuestCancelEvent}
     * with {@link QuestCancelEvent#isExpiration()} returning {@code true}.
     */
    public void cancelAsExpiration() {
        cancelInternal(true);
    }

    /**
     * Internal cancel implementation shared by {@link #cancel()} and {@link #cancelAsExpiration()}.
     *
     * @param isExpiration {@code true} if the cancellation was caused by expiry, {@code false} for manual abandon
     */
    private void cancelInternal(boolean isExpiration) {
        if (questState == QuestState.IN_PROGRESS || questState == QuestState.NOT_STARTED) {
            questState = QuestState.CANCELLED;
            endTime = McRPG.getInstance().getTimeProvider().now();
            for (QuestStageInstance stage : questStageInstances) {
                stage.cancel();
            }
            var definitionOpt = McRPG.getInstance().registryAccess()
                    .registry(McRPGRegistryKey.QUEST_DEFINITION).get(questKey);
            if (definitionOpt.isPresent()) {
                Bukkit.getPluginManager().callEvent(new QuestCancelEvent(this, definitionOpt.get(), isExpiration));
            } else {
                Bukkit.getPluginManager().callEvent(new QuestCancelEvent(this));
            }
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
                        try {
                            reward.grant(player);
                        } catch (RuntimeException e) {
                            McRPG.getInstance().getLogger().log(Level.SEVERE, "Failed to grant reward '" + reward.getKey()
                                    + "' for quest " + questKey + " to player " + playerUUID
                                    + "; other rewards are unaffected.", e);
                        }
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
        Instant now = McRPG.getInstance().getTimeProvider().now();
        long nowMillis = now.toEpochMilli();
        long expiresAt = nowMillis + TimeUnit.DAYS.toMillis(expiryDays);

        Database database = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.DATABASE).getDatabase();
        database.getDatabaseExecutorService().submit(() -> {
            try (Connection connection = database.getConnection()) {
                List<PreparedStatement> statements = new ArrayList<>();
                for (QuestRewardType reward : rewards) {
                    PendingReward pending = new PendingReward(
                            UUID.randomUUID(),
                            playerUUID,
                            reward.getKey(),
                            reward.serializeConfig(),
                            questKey,
                            nowMillis,
                            expiresAt
                    );
                    statements.addAll(PendingRewardDAO.savePendingReward(connection, pending));
                }
                new FailSafeTransaction(connection, statements).executeTransaction();
            } catch (SQLException e) {
                McRPG.getInstance().getLogger().log(Level.SEVERE,
                        "Failed to persist pending rewards for offline player " + playerUUID + " (quest: " + questKey + ")", e);
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
            startTime = McRPG.getInstance().getTimeProvider().now();
        }
    }

    /**
     * Internal helper that transitions this quest to {@link QuestState#COMPLETED}
     * and records the end time.
     */
    private void markAsCompleted() {
        if (questState == QuestState.IN_PROGRESS && endTime == null) {
            questState = QuestState.COMPLETED;
            endTime = McRPG.getInstance().getTimeProvider().now();
        }
    }
}
