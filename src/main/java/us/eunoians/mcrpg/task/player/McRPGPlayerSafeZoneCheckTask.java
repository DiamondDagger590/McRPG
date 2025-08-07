package us.eunoians.mcrpg.task.player;

import com.diamonddagger590.mccore.configuration.common.ReloadableInteger;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.task.core.CancellableCoreTask;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.entity.McRPGPlayerManager;
import us.eunoians.mcrpg.event.entity.player.PlayerSafeZoneStateChangeEvent;
import us.eunoians.mcrpg.external.common.SafeZonePluginHook;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.ArrayList;
import java.util.List;

public class McRPGPlayerSafeZoneCheckTask extends CancellableCoreTask {

    private List<Player> players;
    // How often do we want to best case check every player
    private final ReloadableInteger ticksBetweenChecks;
    // How much work does a tick start with
    private final ReloadableInteger baseChecksPerTick;
    // What is the hard cap of players we can process per tick to prevent runaway lag
    private final ReloadableInteger maxChecksPerTick;
    private int currentIndex = 0;

    public McRPGPlayerSafeZoneCheckTask(@NotNull McRPG mcRPG) {
        super(mcRPG, 0, 0.2);
        resetQueue();
        YamlDocument config = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE).getFile(FileType.MAIN_CONFIG);
        // TODO add actual config fields
        this.ticksBetweenChecks = new ReloadableInteger(config, null);
        this.baseChecksPerTick  = new ReloadableInteger(config, null);
        this.maxChecksPerTick  = new ReloadableInteger(config, null);
    }

    @Override
    protected void onIntervalComplete() {
        int playerCount = players.size();
        // Figure out how far behind we are
        int idealChecksPerTick = Math.max(baseChecksPerTick.getContent(), (int) Math.ceil((double) playerCount / ticksBetweenChecks.getContent()));
        int actualChecksPerTick = Math.min(idealChecksPerTick, maxChecksPerTick.getContent());

        double tps = Bukkit.getTPS()[0];
        if (tps < 17.0) {
            actualChecksPerTick = (int) (actualChecksPerTick * 0.5); // Degrade under lag
        }

        McRPGPlayerManager playerManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.PLAYER);
        List<SafeZonePluginHook> safeZonePluginHooks = RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK).pluginHooks(SafeZonePluginHook.class);
        // Run the actual checks
        for (int i = 0; i < actualChecksPerTick && !players.isEmpty(); i++) {
            Player player = players.get(currentIndex);
            playerManager.getPlayer(player.getUniqueId()).ifPresent(mcRPGPlayer -> {
                boolean isPlayerInSafeZone = safeZonePluginHooks.stream()
                        .map(safeZonePluginHook -> safeZonePluginHook.isPlayerInSafeZone(player))
                        .reduce(Boolean::logicalOr)
                        .orElse(false);
                boolean wasPlayerInSafeZone = mcRPGPlayer.isStandingInSafeZone();
                if (isPlayerInSafeZone != wasPlayerInSafeZone) {
                    PlayerSafeZoneStateChangeEvent playerSafeZoneStateChangeEvent = new PlayerSafeZoneStateChangeEvent(mcRPGPlayer,
                            isPlayerInSafeZone ? PlayerSafeZoneStateChangeEvent.SafeZoneStateChangeType.ENTERED : PlayerSafeZoneStateChangeEvent.SafeZoneStateChangeType.LEFT);
                    Bukkit.getPluginManager().callEvent(playerSafeZoneStateChangeEvent);
                    mcRPGPlayer.setStandingInSafeZone(isPlayerInSafeZone);
                }

            });
            currentIndex++;
        }
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
