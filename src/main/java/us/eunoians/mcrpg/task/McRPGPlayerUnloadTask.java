package us.eunoians.mcrpg.task;

import com.diamonddagger590.mccore.database.table.impl.MutexDAO;
import com.diamonddagger590.mccore.player.CorePlayer;
import com.diamonddagger590.mccore.player.PlayerManager;
import com.diamonddagger590.mccore.task.PlayerUnloadTask;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.event.entity.player.McRPGPlayerUnloadEvent;
import us.eunoians.mcrpg.database.table.PlayerLoadoutDAO;
import us.eunoians.mcrpg.database.table.SkillDAO;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;

import java.sql.Connection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * A task used to save and unload the player data
 */
public class McRPGPlayerUnloadTask extends PlayerUnloadTask {

    public McRPGPlayerUnloadTask(@NotNull McRPG mcRPG, @NotNull McRPGPlayer mcRPGPlayer) {
        super(mcRPG, mcRPGPlayer);
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
        Bukkit.getPluginManager().callEvent(new McRPGPlayerUnloadEvent(getCorePlayer()));
        // Cleanup timers
        getCorePlayer().asSkillHolder().cleanupHolder();
        getPlugin().getEntityManager().removeAbilityHolder(getCorePlayer().getUUID());
        PlayerManager playerManager = getPlugin().getPlayerManager();
        Optional<CorePlayer> corePlayerOptional = playerManager.removePlayer(getCorePlayer().getUUID());

        if (corePlayerOptional.isPresent() && corePlayerOptional.get() instanceof McRPGPlayer mcRPGPlayer) {
            SkillHolder skillHolder = mcRPGPlayer.asSkillHolder();
            Connection connection = getPlugin().getDatabaseManager().getDatabase().getConnection();

            CompletableFuture<Void> completableFuture = CompletableFuture.allOf(SkillDAO.saveAllSkillHolderInformation(connection, skillHolder),
                    PlayerLoadoutDAO.saveAllPlayerLoadouts(connection, skillHolder));
            completableFuture.thenAccept(unused -> {
                // If the player's mutex is locked
                if (mcRPGPlayer.useMutex()) {
                    MutexDAO.updateUserMutex(connection, mcRPGPlayer.getUUID(), false)
                            .thenAccept(unused1 -> playerManager.removePlayer(mcRPGPlayer.getUUID())) // TODO this is gonna fuckin break since i already remove player but i cba rn
                            .exceptionally(throwable -> {
                                throwable.printStackTrace();
                                return null;
                            });
                } else {
                    playerManager.removePlayer(mcRPGPlayer.getUUID());
                }
            }).exceptionally(throwable -> {
                throwable.printStackTrace();
                return null;
            });
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
