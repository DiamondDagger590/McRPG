package us.eunoians.mcrpg.event.combat;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.combat.CombatSession;
import us.eunoians.mcrpg.combat.CombatType;
import us.eunoians.mcrpg.combat.log.CombatLogPunishmentType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Fired after {@link PlayerCombatLogEvent} passes (not cancelled). Carries the
 * punishment map — every type registered in
 * {@link us.eunoians.mcrpg.combat.log.CombatLogPunishmentTypeRegistry} is
 * automatically included with its current {@link us.eunoians.mcrpg.combat.log.CombatLogPunishmentType#isEnabled()}
 * state. Listeners can toggle individual entries; the enforcer reads the final
 * map after all listeners have run.
 * <p>
 * This event is not globally cancellable. To exempt a player entirely, set
 * {@link PlayerCombatLogEvent#setApplyPunishment(boolean)} to {@code false} instead.
 */
public class CombatLogPunishmentEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Player player;
    private final CombatSession session;
    private final CombatType combatType;
    private final Map<CombatLogPunishmentType, Boolean> punishments;

    /**
     * Constructs a new {@link CombatLogPunishmentEvent}.
     *
     * @param player      The player who combat logged.
     * @param session     The player's active combat session.
     * @param combatType  The derived combat type at logout time.
     * @param punishments The initial punishment map, populated from configuration.
     */
    public CombatLogPunishmentEvent(@NotNull Player player, @NotNull CombatSession session,
                                    @NotNull CombatType combatType,
                                    @NotNull Map<CombatLogPunishmentType, Boolean> punishments) {
        this.player = player;
        this.session = session;
        this.combatType = combatType;
        this.punishments = new LinkedHashMap<>(punishments);
    }

    /**
     * Gets the player who combat logged.
     *
     * @return The {@link Player}.
     */
    @NotNull
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the player's active combat session.
     *
     * @return The {@link CombatSession}.
     */
    @NotNull
    public CombatSession getSession() {
        return session;
    }

    /**
     * Gets the derived combat type at logout time.
     *
     * @return The {@link CombatType}.
     */
    @NotNull
    public CombatType getCombatType() {
        return combatType;
    }

    /**
     * Checks whether a specific punishment type is enabled.
     *
     * @param type The punishment type to check.
     * @return {@code true} if the punishment is enabled.
     */
    public boolean isPunishmentEnabled(@NotNull CombatLogPunishmentType type) {
        return punishments.getOrDefault(type, false);
    }

    /**
     * Enables or disables a specific punishment type.
     *
     * @param type    The punishment type to modify.
     * @param enabled {@code true} to enable, {@code false} to disable.
     */
    public void setPunishmentEnabled(@NotNull CombatLogPunishmentType type, boolean enabled) {
        punishments.put(type, enabled);
    }

    /**
     * Gets all punishment types that are currently enabled.
     *
     * @return A list of enabled {@link CombatLogPunishmentType}s.
     */
    @NotNull
    public List<CombatLogPunishmentType> getEnabledPunishments() {
        List<CombatLogPunishmentType> enabled = new ArrayList<>();
        for (Map.Entry<CombatLogPunishmentType, Boolean> entry : punishments.entrySet()) {
            if (entry.getValue()) {
                enabled.add(entry.getKey());
            }
        }
        return enabled;
    }

    /**
     * Checks whether any punishment is still enabled. If all punishments have been
     * disabled by listeners, no punishment is applied.
     *
     * @return {@code true} if at least one punishment is enabled.
     */
    public boolean hasAnyPunishment() {
        return punishments.containsValue(true);
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    /**
     * Gets the static handler list for this event type.
     *
     * @return The {@link HandlerList}.
     */
    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
