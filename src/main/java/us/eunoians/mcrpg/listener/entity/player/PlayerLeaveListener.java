package us.eunoians.mcrpg.listener.entity.player;

import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.combat.CombatSession;
import us.eunoians.mcrpg.combat.CombatSessionEndReason;
import us.eunoians.mcrpg.combat.CombatTrackerManager;
import us.eunoians.mcrpg.combat.ParticipantRemovalReason;
import us.eunoians.mcrpg.combat.log.CombatLogManager;
import us.eunoians.mcrpg.entity.McRPGPlayerManager;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.registry.plugin.McRPGPluginHookKey;
import us.eunoians.mcrpg.task.player.McRPGPlayerUnloadTask;

import java.util.Optional;
import java.util.UUID;

/**
 * This listener will manage unloading player data
 */
public class PlayerLeaveListener implements Listener {

    private final CombatLogManager combatLogManager;

    /**
     * Constructs a new {@link PlayerLeaveListener}.
     *
     * @param combatLogManager The {@link CombatLogManager} used to evaluate and apply combat
     *                          log punishment before the player's combat session is torn down.
     */
    public PlayerLeaveListener(@NotNull CombatLogManager combatLogManager) {
        this.combatLogManager = combatLogManager;
    }

    @EventHandler
    public void handleQuit(PlayerQuitEvent playerQuitEvent) {
        Player player = playerQuitEvent.getPlayer();
        UUID playerUUID = player.getUniqueId();

        CombatTrackerManager combatTrackerManager = McRPG.getInstance().registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.COMBAT_TRACKER);

        // Combat log detection — must run while the session is still alive so the enforcer
        // can evaluate combat type and participant roster.
        Optional<CombatSession> session = combatTrackerManager.getSession(playerUUID);
        session.ifPresent(combatSession -> combatLogManager.evaluateAndEnforce(player, combatSession));

        // Combat teardown — must run while McRPGPlayer is still loaded so the
        // cumulative stat update chain (OnCombatSessionEndStatUpdateListener)
        // can access player statistic data.
        // If KILL_ON_LOGOUT killed the player above, the death listener already ended the
        // session, so this call is a safe no-op.
        combatTrackerManager.endSession(playerUUID, CombatSessionEndReason.LOGOUT);
        combatTrackerManager.removeParticipantFromAllSessions(playerUUID, ParticipantRemovalReason.LOGOUT);
        // Deferred, not immediate: the cache must outlive the write endSession just queued, so a
        // fast relog reads the fresh in-memory value rather than racing the database write.
        combatTrackerManager.clearPersistentStateCacheWhenWritesSettle(playerUUID);

        McRPGPlayerManager playerManager = McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.PLAYER);

        if (playerManager.getPlayer(player.getUniqueId()).isPresent()) {
            McRPGPlayer mcRPGPlayer = playerManager.getPlayer(player.getUniqueId()).get();
            new McRPGPlayerUnloadTask(McRPG.getInstance(), mcRPGPlayer).runTask();
        }

        QuestManager questManager = McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.QUEST);
        for (QuestInstance quest : questManager.getActiveQuestsForPlayer(player.getUniqueId())) {
            questManager.saveQuestAsync(quest);
        }
        questManager.deindexPlayer(player.getUniqueId());

        McRPG.getInstance().registryAccess().registry(RegistryKey.PLUGIN_HOOK).pluginHook(McRPGPluginHookKey.LUNAR_CLIENT)
                .ifPresent(lunarClientHook -> lunarClientHook.clearCooldowns(player.getUniqueId()));
    }
}
