package us.eunoians.mcrpg.listener.combat;

import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.statistic.PlayerStatisticData;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.combat.CombatSessionEndReason;
import us.eunoians.mcrpg.combat.CombatType;
import us.eunoians.mcrpg.combat.state.CombatStateSnapshot;
import us.eunoians.mcrpg.combat.stat.CombatSessionStatisticKey;
import us.eunoians.mcrpg.combat.stat.CombatSessionStatistics;
import us.eunoians.mcrpg.combat.stat.CombatSessionStatisticsSnapshot;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.CombatConfigFile;
import us.eunoians.mcrpg.entity.McRPGPlayerManager;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.event.combat.CombatCumulativeStatisticUpdateEvent;
import us.eunoians.mcrpg.event.combat.CombatSessionEndEvent;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.statistic.McRPGStatistic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@DisplayName("OnCombatSessionEndStatUpdateListener")
class OnCombatSessionEndStatUpdateListenerTest extends McRPGBaseTest {

    private McRPGPlayerManager playerManager;
    private YamlDocument combatConfig;
    private OnCombatSessionEndStatUpdateListener listener;

    @BeforeEach
    void setUp() {
        playerManager = mock(McRPGPlayerManager.class);
        mcRPG.registryAccess().registry(RegistryKey.MANAGER).register(playerManager);

        combatConfig = mock(YamlDocument.class);
        when(mcRPG.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE)
                .getFile(FileType.COMBAT_CONFIG)).thenReturn(combatConfig);
        when(combatConfig.getBoolean(CombatConfigFile.FEED_TO_CUMULATIVE, true)).thenReturn(true);

        listener = new OnCombatSessionEndStatUpdateListener(mcRPG);
    }

    @AfterEach
    void cleanUp() {
        CombatCumulativeStatisticUpdateEvent.getHandlerList().unregister(mcRPG);
    }

    private CombatSessionEndEvent endEventFor(UUID entityUUID, CombatSessionStatisticsSnapshot statistics) {
        return new CombatSessionEndEvent(entityUUID, CombatSessionEndReason.PLUGIN, List.of(), CombatType.PVE,
                1000L, statistics, new CombatStateSnapshot(Map.of(), Map.of()));
    }

    private McRPGPlayer mockLoadedPlayer(UUID uuid) {
        McRPGPlayer mcRPGPlayer = mock(McRPGPlayer.class);
        PlayerStatisticData statisticData = mock(PlayerStatisticData.class);
        when(mcRPGPlayer.getStatisticData()).thenReturn(statisticData);
        when(playerManager.getPlayer(uuid)).thenReturn(Optional.of(mcRPGPlayer));
        return mcRPGPlayer;
    }

    @Test
    @DisplayName("applies session stats to cumulative McCore statistics when config is enabled")
    void appliesSessionStats_whenConfigEnabled() {
        UUID uuid = UUID.randomUUID();
        McRPGPlayer mcRPGPlayer = mockLoadedPlayer(uuid);

        CombatSessionStatistics stats = new CombatSessionStatistics();
        stats.incrementDouble(CombatSessionStatisticKey.HEALING_DEALT, 5.0);
        stats.incrementLong(CombatSessionStatisticKey.HITS_LANDED, 2);

        listener.onCombatSessionEnd(endEventFor(uuid, stats.snapshot()));

        verify(mcRPGPlayer.getStatisticData()).incrementDouble(McRPGStatistic.HEALING_DEALT.getStatisticKey(), 5.0);
        verify(mcRPGPlayer.getStatisticData()).incrementLong(McRPGStatistic.HITS_LANDED.getStatisticKey(), 2L);
    }

    @Test
    @DisplayName("does not apply when feed-to-cumulative config is false")
    void doesNotApply_whenConfigDisabled() {
        when(combatConfig.getBoolean(CombatConfigFile.FEED_TO_CUMULATIVE, true)).thenReturn(false);
        UUID uuid = UUID.randomUUID();
        mockLoadedPlayer(uuid);

        listener.onCombatSessionEnd(endEventFor(uuid, new CombatSessionStatistics().snapshot()));

        verifyNoInteractions(playerManager);
    }

    @Test
    @DisplayName("fires CombatCumulativeStatisticUpdateEvent before update")
    void firesCumulativeUpdateEvent_beforeApplying() {
        UUID uuid = UUID.randomUUID();
        mockLoadedPlayer(uuid);
        List<CombatCumulativeStatisticUpdateEvent> captured = new ArrayList<>();
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onUpdate(CombatCumulativeStatisticUpdateEvent event) {
                captured.add(event);
            }
        }, mcRPG);

        listener.onCombatSessionEnd(endEventFor(uuid, new CombatSessionStatistics().snapshot()));

        assertEquals(1, captured.size());
        assertEquals(uuid, captured.get(0).getEntityUUID());
    }

    @Test
    @DisplayName("does not update cumulative statistics when the update event is cancelled")
    void doesNotApply_whenUpdateEventCancelled() {
        UUID uuid = UUID.randomUUID();
        McRPGPlayer mcRPGPlayer = mockLoadedPlayer(uuid);
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onUpdate(CombatCumulativeStatisticUpdateEvent event) {
                event.setCancelled(true);
            }
        }, mcRPG);

        CombatSessionStatistics stats = new CombatSessionStatistics();
        stats.incrementLong(CombatSessionStatisticKey.KILLS, 1);

        listener.onCombatSessionEnd(endEventFor(uuid, stats.snapshot()));

        verifyNoInteractions(mcRPGPlayer.getStatisticData());
    }

    @Test
    @DisplayName("does not apply for non-player entities")
    void doesNotApply_forNonPlayerEntities() {
        UUID uuid = UUID.randomUUID();
        when(playerManager.getPlayer(uuid)).thenReturn(Optional.empty());
        List<CombatCumulativeStatisticUpdateEvent> captured = new ArrayList<>();
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onUpdate(CombatCumulativeStatisticUpdateEvent event) {
                captured.add(event);
            }
        }, mcRPG);

        listener.onCombatSessionEnd(endEventFor(uuid, new CombatSessionStatistics().snapshot()));

        assertTrue(captured.isEmpty());
    }

    @Test
    @DisplayName("maps built-in per-session stat keys to the correct cumulative statistics")
    void mapsBuiltInKeys_toCorrectCumulativeStatistics() {
        UUID uuid = UUID.randomUUID();
        McRPGPlayer mcRPGPlayer = mockLoadedPlayer(uuid);

        CombatSessionStatistics stats = new CombatSessionStatistics();
        stats.incrementDouble(CombatSessionStatisticKey.HEALING_DEALT, 1.0);
        stats.incrementDouble(CombatSessionStatisticKey.HEALING_RECEIVED, 2.0);
        stats.incrementLong(CombatSessionStatisticKey.HITS_LANDED, 3);
        stats.incrementLong(CombatSessionStatisticKey.HITS_RECEIVED, 4);
        stats.incrementLong(CombatSessionStatisticKey.KILLS, 5);

        listener.onCombatSessionEnd(endEventFor(uuid, stats.snapshot()));

        PlayerStatisticData statisticData = mcRPGPlayer.getStatisticData();
        verify(statisticData).incrementDouble(McRPGStatistic.HEALING_DEALT.getStatisticKey(), 1.0);
        verify(statisticData).incrementDouble(McRPGStatistic.HEALING_RECEIVED.getStatisticKey(), 2.0);
        verify(statisticData).incrementLong(McRPGStatistic.HITS_LANDED.getStatisticKey(), 3L);
        verify(statisticData).incrementLong(McRPGStatistic.HITS_RECEIVED.getStatisticKey(), 4L);
        verify(statisticData).incrementLong(McRPGStatistic.COMBAT_KILLS.getStatisticKey(), 5L);
    }
}
