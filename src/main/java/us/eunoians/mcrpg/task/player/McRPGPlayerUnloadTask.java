package us.eunoians.mcrpg.task.player;

import com.diamonddagger590.mccore.database.transaction.BatchTransaction;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.task.player.PlayerUnloadTask;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.database.table.PlayerLoginTimeDAO;
import us.eunoians.mcrpg.entity.McRPGPlayerManager;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;

/**
 * A task used to save and unload the player data
 */
public final class McRPGPlayerUnloadTask extends PlayerUnloadTask {

    private final boolean playerLogoutInSafeZone;

    public McRPGPlayerUnloadTask(@NotNull McRPG mcRPG, @NotNull McRPGPlayer mcRPGPlayer) {
        super(mcRPG, mcRPGPlayer);
        // Do this on main thread
        playerLogoutInSafeZone = mcRPGPlayer.isStandingInSafeZone(true);
    }

    @Override
    public McRPGPlayer getCorePlayer() {
        return (McRPGPlayer) super.getCorePlayer();
    }

    @NotNull
    @Override
    public McRPG getPlugin() {
        return (McRPG) super.getPlugin();
    }

    @Override
    protected boolean unloadPlayer() {
        McRPGPlayerManager playerManager = getPlugin().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.PLAYER);
        // Ensure they are registered... probs a better way to do this
        var corePlayerOptional = playerManager.getPlayer(getCorePlayer().getUUID());

        if (corePlayerOptional.isPresent()) {
            McRPGPlayer mcRPGPlayer = corePlayerOptional.get();

            try (Connection connection = getPlugin().getDatabase().getConnection()) {
                mcRPGPlayer.savePlayer(connection);
            /*
             We don't want to include the last seen time or the last logout time with saving general information about
             the player because the existence of an McRPGPlayer doesn't have any contract with whether a player is online or not,
             so we update it externally here.
             */
                Instant logoutTime = Instant.now();
                BatchTransaction lastLogoutTransaction = new BatchTransaction(connection);
                lastLogoutTransaction.addAll(PlayerLoginTimeDAO.saveLastLogoutTime(connection, mcRPGPlayer.getUUID(), logoutTime));
                lastLogoutTransaction.addAll(PlayerLoginTimeDAO.saveLastSeenTime(connection, mcRPGPlayer.getUUID(), logoutTime));
                lastLogoutTransaction.addAll(PlayerLoginTimeDAO.saveLoggedOutInSafeZone(connection, mcRPGPlayer.getUUID(), playerLogoutInSafeZone));
                lastLogoutTransaction.executeTransaction();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onTaskExpire() {

    }

    @Override
    protected void onDelayComplete() {

    }

    @Override
    protected void onIntervalStart() {

    }

    @Override
    protected void onIntervalPause() {

    }

    @Override
    protected void onIntervalResume() {

    }
}
