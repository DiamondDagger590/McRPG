package us.eunoians.mcrpg.event.combat;

import com.diamonddagger590.mccore.util.item.CustomEntityWrapper;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.combat.ParticipantType;

import java.util.Optional;
import java.util.UUID;

/**
 * Fired before a new combat session is created for an entity. Cancellable — cancelling prevents the
 * session from being created.
 * <p>
 * A session can be started two ways, distinguished by {@link #getTriggeringConditionKey()}:
 * <ul>
 *   <li><b>Damage-triggered</b> — the trigger participant is the other combatant, and the
 *       triggering condition key is absent.</li>
 *   <li><b>Condition-triggered</b> — a periodic {@link us.eunoians.mcrpg.combat.condition.CombatCondition}
 *       (e.g. a proximity or region condition) put the entity in combat with no specific opponent.
 *       In that case the trigger participant is the entity's own UUID and the triggering condition
 *       key is present.</li>
 * </ul>
 */
public class CombatSessionStartEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final UUID entityUUID;
    private final UUID triggerParticipantUUID;
    private final ParticipantType triggerParticipantType;
    private final CustomEntityWrapper triggerEntityWrapper;
    @Nullable
    private final NamespacedKey triggeringConditionKey;
    private boolean cancelled;

    /**
     * Constructs a damage-triggered {@link CombatSessionStartEvent} with no triggering condition.
     *
     * @param entityUUID              The UUID of the entity entering combat.
     * @param triggerParticipantUUID   The UUID of the entity that triggered combat entry.
     * @param triggerParticipantType   The {@link ParticipantType} of the triggering entity.
     * @param triggerEntityWrapper      The {@link CustomEntityWrapper} of the triggering entity.
     */
    public CombatSessionStartEvent(@NotNull UUID entityUUID, @NotNull UUID triggerParticipantUUID,
                                    @NotNull ParticipantType triggerParticipantType,
                                    @NotNull CustomEntityWrapper triggerEntityWrapper) {
        this(entityUUID, triggerParticipantUUID, triggerParticipantType, triggerEntityWrapper, null);
    }

    /**
     * Constructs a new {@link CombatSessionStartEvent}.
     *
     * @param entityUUID               The UUID of the entity entering combat.
     * @param triggerParticipantUUID   The UUID of the entity that triggered combat entry. For a
     *                                 condition-triggered start this is the entity's own UUID.
     * @param triggerParticipantType   The {@link ParticipantType} of the triggering entity.
     * @param triggerEntityWrapper     The {@link CustomEntityWrapper} of the triggering entity.
     * @param triggeringConditionKey   The {@link NamespacedKey} of the condition that triggered this
     *                                 start, or {@code null} for a damage-triggered start.
     */
    public CombatSessionStartEvent(@NotNull UUID entityUUID, @NotNull UUID triggerParticipantUUID,
                                    @NotNull ParticipantType triggerParticipantType,
                                    @NotNull CustomEntityWrapper triggerEntityWrapper,
                                    @Nullable NamespacedKey triggeringConditionKey) {
        this.entityUUID = entityUUID;
        this.triggerParticipantUUID = triggerParticipantUUID;
        this.triggerParticipantType = triggerParticipantType;
        this.triggerEntityWrapper = triggerEntityWrapper;
        this.triggeringConditionKey = triggeringConditionKey;
    }

    /**
     * Gets the UUID of the entity entering combat.
     *
     * @return The entity UUID.
     */
    @NotNull
    public UUID getEntityUUID() {
        return entityUUID;
    }

    /**
     * Gets the UUID of the entity that triggered combat entry (the other combatant).
     *
     * @return The trigger participant UUID.
     */
    @NotNull
    public UUID getTriggerParticipantUUID() {
        return triggerParticipantUUID;
    }

    /**
     * Gets the {@link ParticipantType} of the triggering entity.
     *
     * @return The trigger participant type.
     */
    @NotNull
    public ParticipantType getTriggerParticipantType() {
        return triggerParticipantType;
    }

    /**
     * Gets the {@link CustomEntityWrapper} of the triggering entity.
     *
     * @return The trigger entity wrapper.
     */
    @NotNull
    public CustomEntityWrapper getTriggerEntityWrapper() {
        return triggerEntityWrapper;
    }

    /**
     * Gets the key of the {@link us.eunoians.mcrpg.combat.condition.CombatCondition} that triggered
     * this combat start, if any. Present for condition-triggered starts (proximity/region conditions)
     * and empty for damage-triggered starts.
     *
     * @return An {@link Optional} containing the triggering condition's {@link NamespacedKey}, or
     *         empty for a damage-triggered start.
     */
    @NotNull
    public Optional<NamespacedKey> getTriggeringConditionKey() {
        return Optional.ofNullable(triggeringConditionKey);
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
