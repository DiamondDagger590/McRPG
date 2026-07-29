package us.eunoians.mcrpg.listener.combat;

import com.diamonddagger590.mccore.configuration.common.ReloadableBoolean;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.ManagerKey;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.combat.stat.CombatSessionStatisticKey;
import us.eunoians.mcrpg.combat.stat.CombatSessionStatisticsSnapshot;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.CombatConfigFile;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.event.combat.CombatCumulativeStatisticUpdateEvent;
import us.eunoians.mcrpg.event.combat.CombatSessionEndEvent;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.statistic.McRPGStatistic;

import java.util.Optional;
import java.util.UUID;

/**
 * Listens to {@link CombatSessionEndEvent} at {@link EventPriority#MONITOR} priority and applies
 * per-session statistics to cumulative McCore statistics. Gated by the
 * {@link CombatConfigFile#FEED_TO_CUMULATIVE} config flag. Fires
 * {@link CombatCumulativeStatisticUpdateEvent} (cancellable) before performing the update.
 * <p>
 * The mapping from per-session keys to cumulative statistics deliberately excludes
 * {@code DAMAGE_DEALT} / {@code DAMAGE_TAKEN} — those are already tracked incrementally by
 * {@link us.eunoians.mcrpg.listener.statistic.CombatStatisticListener} on every damage event, so
 * re-applying the per-session total here would double-count.
 */
public class OnCombatSessionEndStatUpdateListener implements Listener {

    private final McRPG mcRPG;
    private final ReloadableBoolean feedToCumulative;

    /**
     * Constructs a new {@link OnCombatSessionEndStatUpdateListener}.
     *
     * @param mcRPG The McRPG plugin instance.
     */
    public OnCombatSessionEndStatUpdateListener(@NotNull McRPG mcRPG) {
        this.mcRPG = mcRPG;
        var config = mcRPG.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.FILE).getFile(FileType.COMBAT_CONFIG);
        this.feedToCumulative = new ReloadableBoolean(config, CombatConfigFile.FEED_TO_CUMULATIVE);
        mcRPG.registryAccess().registry(RegistryKey.MANAGER)
                .manager(ManagerKey.RELOADABLE_CONTENT)
                .trackReloadableContent(feedToCumulative);
    }

    /**
     * Applies per-session statistics to cumulative McCore statistics. Gated by the
     * {@code feed-to-cumulative} config flag. Fires {@link CombatCumulativeStatisticUpdateEvent}
     * before the update — if cancelled, no cumulative updates are applied.
     *
     * @param event The session end event.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onCombatSessionEnd(@NotNull CombatSessionEndEvent event) {
        if (!feedToCumulative.getContent()) {
            return;
        }

        UUID entityUUID = event.getEntityUUID();
        Optional<McRPGPlayer> playerOptional = mcRPG.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.PLAYER).getPlayer(entityUUID);
        if (playerOptional.isEmpty()) {
            return;
        }
        McRPGPlayer mcRPGPlayer = playerOptional.get();

        CombatSessionStatisticsSnapshot statistics = event.getStatistics();

        CombatCumulativeStatisticUpdateEvent updateEvent =
                new CombatCumulativeStatisticUpdateEvent(entityUUID, statistics);
        Bukkit.getPluginManager().callEvent(updateEvent);
        if (updateEvent.isCancelled()) {
            return;
        }

        var statisticData = mcRPGPlayer.getStatisticData();
        statisticData.incrementDouble(McRPGStatistic.HEALING_DEALT.getStatisticKey(),
                statistics.getDouble(CombatSessionStatisticKey.HEALING_DEALT));
        statisticData.incrementDouble(McRPGStatistic.HEALING_RECEIVED.getStatisticKey(),
                statistics.getDouble(CombatSessionStatisticKey.HEALING_RECEIVED));
        statisticData.incrementLong(McRPGStatistic.HITS_LANDED.getStatisticKey(),
                statistics.getLong(CombatSessionStatisticKey.HITS_LANDED));
        statisticData.incrementLong(McRPGStatistic.HITS_RECEIVED.getStatisticKey(),
                statistics.getLong(CombatSessionStatisticKey.HITS_RECEIVED));
        statisticData.incrementLong(McRPGStatistic.COMBAT_KILLS.getStatisticKey(),
                statistics.getLong(CombatSessionStatisticKey.KILLS));
    }
}
