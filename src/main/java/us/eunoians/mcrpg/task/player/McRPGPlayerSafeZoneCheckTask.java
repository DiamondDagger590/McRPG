package us.eunoians.mcrpg.task.player;

import com.diamonddagger590.mccore.configuration.common.ReloadableInteger;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.ManagerKey;
import com.diamonddagger590.mccore.task.core.CancellableCoreTask;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.MainConfigFile;
import us.eunoians.mcrpg.entity.McRPGPlayerManager;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * This task is responsible for updating a {@link us.eunoians.mcrpg.entity.player.McRPGPlayer}'s
 * safe zone status on a periodic basis
 */
public class McRPGPlayerSafeZoneCheckTask extends CancellableCoreTask {

    // How often do we want to best case check every player
    private static final ReloadableInteger TICKS_BETWEEN_CHECKS;
    // How much work does a tick start with
    private static final ReloadableInteger BASE_CHECKS_PER_TICK;
    // What is the hard cap of players we can process per tick to prevent runaway lag
    private static final ReloadableInteger MAX_CHECKS_PER_TICK;

    static {
        YamlDocument config = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE).getFile(FileType.MAIN_CONFIG);
        TICKS_BETWEEN_CHECKS = new ReloadableInteger(config, MainConfigFile.SAFE_ZONE_UPDATE_TASK_IDEAL_TICKS);
        BASE_CHECKS_PER_TICK = new ReloadableInteger(config, MainConfigFile.SAFE_ZONE_UPDATE_TASK_MINIMUM_CHECKS_PER_TICK);
        MAX_CHECKS_PER_TICK = new ReloadableInteger(config, MainConfigFile.SAFE_ZONE_UPDATE_TASK_MAXIMUM_CHECKS_PER_TICK);
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(ManagerKey.RELOADABLE_CONTENT).trackReloadableContent(Set.of(TICKS_BETWEEN_CHECKS, BASE_CHECKS_PER_TICK, MAX_CHECKS_PER_TICK));
    }

    private List<Player> players;
    private int currentIndex = 0;

    public McRPGPlayerSafeZoneCheckTask(@NotNull McRPG mcRPG, double delay, double frequency) {
        super(mcRPG, delay, frequency);
        resetQueue();
    }

    @Override
    protected void onIntervalComplete() {
        int playerCount = players.size();
        // Figure out how far behind we are
        int idealChecksPerTick = Math.max(BASE_CHECKS_PER_TICK.getContent(), (int) Math.ceil((double) playerCount / TICKS_BETWEEN_CHECKS.getContent()));
        int actualChecksPerTick = Math.min(idealChecksPerTick, MAX_CHECKS_PER_TICK.getContent());

        double tps = Bukkit.getTPS()[0];
        if (tps < 17.0) {
            actualChecksPerTick = (int) (actualChecksPerTick * 0.5); // Degrade under lag
        }

        McRPGPlayerManager playerManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.PLAYER);
        // Run the actual checks
        for (int i = 0; i < actualChecksPerTick && !players.isEmpty(); i++) {
            // Ensure we don't get an index out of bounds exception
            if (currentIndex == players.size()) {
                resetQueue();
                return;
            }

            Player player = players.get(currentIndex);
            playerManager.getPlayer(player.getUniqueId()).ifPresent(McRPGPlayer::refreshSafeZoneState);
            currentIndex++;
        }
        // If we've reached the end of the list, reset
        if (currentIndex == players.size()) {
            resetQueue();
        }
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
    protected void onIntervalPause() {

    }

    @Override
    protected void onIntervalResume() {
        resetQueue();
    }

    private void resetQueue() {
        this.players = new ArrayList<>(Bukkit.getOnlinePlayers());
        currentIndex = 0;
    }
}
