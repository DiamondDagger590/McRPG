package us.eunoians.mcrpg.external.papi.placeholder.combat;

import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.combat.CombatSession;
import us.eunoians.mcrpg.combat.CombatTrackerManager;
import us.eunoians.mcrpg.external.papi.placeholder.McRPGPlaceholder;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Locale;
import java.util.Optional;

/**
 * PAPI placeholder that returns the seconds remaining until the player's combat
 * session times out at current inactivity. Computed live from the session's
 * last-activity timestamp and configured timeout. Returns {@code "0.0"} if the
 * player has no active session. Identifier: {@code combat_seconds_remaining}.
 */
public class CombatSecondsRemainingPlaceholder extends McRPGPlaceholder {

    private static final String PLACEHOLDER = "combat_seconds_remaining";

    /**
     * Constructs a new {@link CombatSecondsRemainingPlaceholder}.
     */
    public CombatSecondsRemainingPlaceholder() {
        super(PLACEHOLDER);
    }

    @Nullable
    @Override
    public String parsePlaceholder(@NotNull OfflinePlayer offlinePlayer) {
        CombatTrackerManager combatTrackerManager = McRPG.getInstance().registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.COMBAT_TRACKER);
        Optional<CombatSession> sessionOpt = combatTrackerManager.getSession(offlinePlayer.getUniqueId());
        if (sessionOpt.isEmpty()) {
            return "0.0";
        }

        CombatSession session = sessionOpt.get();
        long now = McRPG.getInstance().getTimeProvider().now().toEpochMilli();
        long elapsed = now - session.getLastActivityMillis();
        double remaining = (session.getTimeoutMillis() - elapsed) / 1000.0;
        return String.format(Locale.ROOT, "%.1f", Math.max(0.0, remaining));
    }
}
