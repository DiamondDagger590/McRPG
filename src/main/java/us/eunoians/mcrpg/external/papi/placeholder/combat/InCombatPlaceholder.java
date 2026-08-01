package us.eunoians.mcrpg.external.papi.placeholder.combat;

import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.combat.CombatTrackerManager;
import us.eunoians.mcrpg.external.papi.placeholder.McRPGPlaceholder;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

/**
 * PAPI placeholder that returns {@code "true"} if the player has an active combat
 * session, {@code "false"} otherwise. Identifier: {@code in_combat}.
 */
public class InCombatPlaceholder extends McRPGPlaceholder {

    private static final String PLACEHOLDER = "in_combat";

    /**
     * Constructs a new {@link InCombatPlaceholder}.
     */
    public InCombatPlaceholder() {
        super(PLACEHOLDER);
    }

    @Nullable
    @Override
    public String parsePlaceholder(@NotNull OfflinePlayer offlinePlayer) {
        CombatTrackerManager combatTrackerManager = McRPG.getInstance().registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.COMBAT_TRACKER);
        return String.valueOf(combatTrackerManager.hasActiveSession(offlinePlayer.getUniqueId()));
    }
}
