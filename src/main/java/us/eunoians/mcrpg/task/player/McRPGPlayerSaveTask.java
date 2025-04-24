package us.eunoians.mcrpg.task.player;

import com.diamonddagger590.mccore.database.transaction.BatchTransaction;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.task.core.CancellableCoreTask;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.database.table.PlayerLoginTimeDAO;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * This task will run continually in the background to save player data
 * on a timer.
 */
public final class McRPGPlayerSaveTask extends CancellableCoreTask {

    public McRPGPlayerSaveTask(@NotNull McRPG plugin, double taskDelay, double taskFrequency) {
        super(plugin, taskDelay, taskFrequency);
    }

    @NotNull
    @Override
    public McRPG getPlugin() {
        return (McRPG) super.getPlugin();
    }

    @Override
    protected void onCancel() {

    }

    @Override
    protected void onDelayComplete() {

    }

    @Override
    protected void onIntervalStart() {

    }

    @Override
    protected void onIntervalComplete() {
        // Get a copy of all online players that need to be saved
        Set<McRPGPlayer> players = new HashSet<>(getPlugin().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.PLAYER).getAllPlayers());
        try (Connection connection = getPlugin().getDatabase().getConnection()) {
            BatchTransaction lastSeenTimeTransaction = new BatchTransaction(connection);
            Instant lastSeenTime = Instant.now();
            players.forEach(mcRPGPlayer -> {
                mcRPGPlayer.savePlayer(connection);
                /*
                 We don't want to include the last seen time with saving general information about the player
                 because the existence of an McRPGPlayer doesn't have any contract with whether a player is online or not,
                 so we update it externally here.
                 */
                lastSeenTimeTransaction.addAll(PlayerLoginTimeDAO.saveLastSeenTime(connection, mcRPGPlayer.getUUID(), lastSeenTime));
            });
            lastSeenTimeTransaction.executeTransaction();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onIntervalPause() {

    }

    @Override
    protected void onIntervalResume() {

    }
}
