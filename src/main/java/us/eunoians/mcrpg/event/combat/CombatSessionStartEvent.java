package us.eunoians.mcrpg.event.combat;

import com.diamonddagger590.mccore.util.item.CustomEntityWrapper;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.combat.ParticipantType;

import java.util.UUID;

public class CombatSessionStartEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final UUID entityUUID;
    private final UUID triggerParticipantUUID;
    private final ParticipantType triggerParticipantType;
    private final CustomEntityWrapper triggerEntityWrapper;
    private boolean cancelled;

    /**
     * Constructs a new {@link CombatSessionStartEvent}.
     *
     * @param entityUUID              The UUID of the entity entering combat.
     * @param triggerParticipantUUID   The UUID of the entity that triggered combat entry.
     * @param triggerParticipantType   The {@link ParticipantType} of the triggering entity.
     * @param triggerEntityWrapper      The {@link CustomEntityWrapper} of the triggering entity.
     */
    public CombatSessionStartEvent(@NotNull UUID entityUUID, @NotNull UUID triggerParticipantUUID,
                                    @NotNull ParticipantType triggerParticipantType,
                                    @NotNull CustomEntityWrapper triggerEntityWrapper) {
        this.entityUUID = entityUUID;
        this.triggerParticipantUUID = triggerParticipantUUID;
        this.triggerParticipantType = triggerParticipantType;
        this.triggerEntityWrapper = triggerEntityWrapper;
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
