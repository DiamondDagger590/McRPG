package us.eunoians.mcrpg.event.ability;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.Ability;

/**
 * This event is called whenever an {@link Ability} is unregistered from McRPG by using
 * {@link us.eunoians.mcrpg.ability.AbilityRegistry#unregisterAbility(NamespacedKey)} or
 * temporarily soft-disabled via
 * {@link us.eunoians.mcrpg.ability.AbilityRegistry#softDisableAbility(NamespacedKey)}.
 * <p>
 * Use {@link #getReason()} to distinguish between a permanent removal and a temporary
 * soft-disable that may be reversed on reload.
 */
public class AbilityUnregisterEvent extends AbilityEvent {

    /**
     * Describes why the ability was unregistered.
     */
    public enum UnregisterReason {
        /** The ability is permanently removed from the registry. */
        PERMANENT,
        /** The ability is temporarily disabled and may be re-enabled on reload. */
        SOFT_DISABLE
    }

    private final UnregisterReason reason;

    /**
     * Creates a new ability unregister event with an explicit reason.
     *
     * @param ability the ability being unregistered
     * @param reason  why the ability is being unregistered
     */
    public AbilityUnregisterEvent(@NotNull Ability ability, @NotNull UnregisterReason reason) {
        super(ability);
        this.reason = reason;
    }

    /**
     * Creates a new ability unregister event defaulting to {@link UnregisterReason#PERMANENT}.
     *
     * @param ability the ability being unregistered
     */
    public AbilityUnregisterEvent(@NotNull Ability ability) {
        this(ability, UnregisterReason.PERMANENT);
    }

    /**
     * Gets the reason the ability is being unregistered.
     *
     * @return the unregister reason
     */
    @NotNull
    public UnregisterReason getReason() {
        return reason;
    }
}
