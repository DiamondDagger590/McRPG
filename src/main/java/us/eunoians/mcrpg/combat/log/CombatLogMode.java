package us.eunoians.mcrpg.combat.log;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.combat.CombatType;

/**
 * Determines which combat session types trigger combat log detection and punishment
 * when a player disconnects with an active session.
 */
public enum CombatLogMode {

    /**
     * Combat log detection is disabled. No punishment is applied regardless of session state.
     */
    DISABLED,

    /**
     * Only punish if the session's derived type is {@link CombatType#PVP} — at least one
     * player participant in the roster at the time of logout.
     */
    PLAYERS,

    /**
     * Punish for any active combat session regardless of participant types.
     */
    MOBS_AND_PLAYERS;

    /**
     * Evaluates whether this mode would punish a combat log for the given combat type.
     *
     * @param combatType The derived combat type of the session at logout time.
     * @return {@code true} if this mode triggers punishment for the given type.
     */
    public boolean shouldPunish(@NotNull CombatType combatType) {
        return switch (this) {
            case DISABLED -> false;
            case PLAYERS -> combatType == CombatType.PVP;
            case MOBS_AND_PLAYERS -> true;
        };
    }

    /**
     * Checks whether this mode has any detection enabled.
     *
     * @return {@code true} if this mode is not {@link #DISABLED}.
     */
    public boolean isEnabled() {
        return this != DISABLED;
    }
}
