package us.eunoians.mcrpg.listener.combat;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.combat.CombatSession;
import us.eunoians.mcrpg.combat.CombatTrackerManager;
import us.eunoians.mcrpg.combat.stat.CombatSessionStatisticKey;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("OnCombatDamageStatListener")
class OnCombatDamageStatListenerTest extends McRPGBaseTest {

    private CombatTrackerManager manager;
    private OnCombatDamageStatListener listener;

    @BeforeEach
    void setUp() {
        manager = mock(CombatTrackerManager.class);
        listener = new OnCombatDamageStatListener(manager, new CombatDamageResolver());
    }

    private EntityDamageByEntityEvent damageEvent(Entity damager, Entity target, double finalDamage) {
        EntityDamageByEntityEvent event = mock(EntityDamageByEntityEvent.class);
        when(event.getDamager()).thenReturn(damager);
        when(event.getEntity()).thenReturn(target);
        when(event.getFinalDamage()).thenReturn(finalDamage);
        return event;
    }

    private CombatSession session(UUID owner) {
        return new CombatSession(owner, 16, 8000L);
    }

    @Test
    @DisplayName("increments damage_dealt and hits_landed on the source's session")
    void incrementsDamageDealtAndHitsLanded_onSourceSession() {
        PlayerMock source = server.addPlayer();
        Zombie target = spawnEntity(Zombie.class);
        CombatSession sourceSession = session(source.getUniqueId());
        when(manager.getSession(source.getUniqueId())).thenReturn(Optional.of(sourceSession));
        when(manager.getSession(target.getUniqueId())).thenReturn(Optional.empty());

        listener.onEntityDamageByEntity(damageEvent(source, target, 8.5));

        assertEquals(8.5, sourceSession.getStatistics().getDouble(CombatSessionStatisticKey.DAMAGE_DEALT));
        assertEquals(1L, sourceSession.getStatistics().getLong(CombatSessionStatisticKey.HITS_LANDED));
    }

    @Test
    @DisplayName("increments damage_taken and hits_received on the target's session")
    void incrementsDamageTakenAndHitsReceived_onTargetSession() {
        Zombie source = spawnEntity(Zombie.class);
        PlayerMock target = server.addPlayer();
        CombatSession targetSession = session(target.getUniqueId());
        when(manager.getSession(source.getUniqueId())).thenReturn(Optional.empty());
        when(manager.getSession(target.getUniqueId())).thenReturn(Optional.of(targetSession));

        listener.onEntityDamageByEntity(damageEvent(source, target, 4.0));

        assertEquals(4.0, targetSession.getStatistics().getDouble(CombatSessionStatisticKey.DAMAGE_TAKEN));
        assertEquals(1L, targetSession.getStatistics().getLong(CombatSessionStatisticKey.HITS_RECEIVED));
    }

    @Test
    @DisplayName("resolves projectile shooters as the source")
    void resolvesProjectileShooter_asSource() {
        PlayerMock source = server.addPlayer();
        Zombie target = spawnEntity(Zombie.class);
        Arrow arrow = mock(Arrow.class);
        when(arrow.getShooter()).thenReturn(source);
        CombatSession sourceSession = session(source.getUniqueId());
        when(manager.getSession(source.getUniqueId())).thenReturn(Optional.of(sourceSession));
        when(manager.getSession(target.getUniqueId())).thenReturn(Optional.empty());

        listener.onEntityDamageByEntity(damageEvent(arrow, target, 3.0));

        assertEquals(3.0, sourceSession.getStatistics().getDouble(CombatSessionStatisticKey.DAMAGE_DEALT));
    }

    @Test
    @DisplayName("records target stats and does not throw when the source has no session")
    void recordsTargetStats_whenSourceHasNoSession() {
        PlayerMock source = server.addPlayer();
        Zombie target = spawnEntity(Zombie.class);
        when(manager.getSession(source.getUniqueId())).thenReturn(Optional.empty());
        CombatSession targetSession = session(target.getUniqueId());
        when(manager.getSession(target.getUniqueId())).thenReturn(Optional.of(targetSession));

        assertDoesNotThrow(() -> listener.onEntityDamageByEntity(damageEvent(source, target, 5.0)));

        // The absent source session must not suppress the target's own stats.
        assertEquals(5.0, targetSession.getStatistics().getDouble(CombatSessionStatisticKey.DAMAGE_TAKEN));
        assertEquals(1L, targetSession.getStatistics().getLong(CombatSessionStatisticKey.HITS_RECEIVED));
    }

    @Test
    @DisplayName("records source stats and does not throw when the target has no session")
    void recordsSourceStats_whenTargetHasNoSession() {
        PlayerMock source = server.addPlayer();
        Zombie target = spawnEntity(Zombie.class);
        CombatSession sourceSession = session(source.getUniqueId());
        when(manager.getSession(source.getUniqueId())).thenReturn(Optional.of(sourceSession));
        when(manager.getSession(target.getUniqueId())).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> listener.onEntityDamageByEntity(damageEvent(source, target, 5.0)));

        // The absent target session must not suppress the source's own stats.
        assertEquals(5.0, sourceSession.getStatistics().getDouble(CombatSessionStatisticKey.DAMAGE_DEALT));
        assertEquals(1L, sourceSession.getStatistics().getLong(CombatSessionStatisticKey.HITS_LANDED));
    }

    @Test
    @DisplayName("records all four stats on the correct sessions when both sides are in combat")
    void recordsBothSides_whenBothHaveSessions() {
        PlayerMock source = server.addPlayer();
        PlayerMock target = server.addPlayer();
        CombatSession sourceSession = session(source.getUniqueId());
        CombatSession targetSession = session(target.getUniqueId());
        when(manager.getSession(source.getUniqueId())).thenReturn(Optional.of(sourceSession));
        when(manager.getSession(target.getUniqueId())).thenReturn(Optional.of(targetSession));

        listener.onEntityDamageByEntity(damageEvent(source, target, 6.0));

        // The session-less tests above can't prove attribution: with only one session present, a
        // listener that wrote both sides to the same session would still pass them.
        assertEquals(6.0, sourceSession.getStatistics().getDouble(CombatSessionStatisticKey.DAMAGE_DEALT));
        assertEquals(1L, sourceSession.getStatistics().getLong(CombatSessionStatisticKey.HITS_LANDED));
        assertEquals(0.0, sourceSession.getStatistics().getDouble(CombatSessionStatisticKey.DAMAGE_TAKEN));
        assertEquals(6.0, targetSession.getStatistics().getDouble(CombatSessionStatisticKey.DAMAGE_TAKEN));
        assertEquals(1L, targetSession.getStatistics().getLong(CombatSessionStatisticKey.HITS_RECEIVED));
        assertEquals(0.0, targetSession.getStatistics().getDouble(CombatSessionStatisticKey.DAMAGE_DEALT));
    }

    @Test
    @DisplayName("ignores self-damage")
    void ignoresSelfDamage() {
        PlayerMock player = server.addPlayer();
        CombatSession session = session(player.getUniqueId());
        when(manager.getSession(player.getUniqueId())).thenReturn(Optional.of(session));

        listener.onEntityDamageByEntity(damageEvent(player, player, 5.0));

        assertEquals(0.0, session.getStatistics().getDouble(CombatSessionStatisticKey.DAMAGE_DEALT));
        assertEquals(0.0, session.getStatistics().getDouble(CombatSessionStatisticKey.DAMAGE_TAKEN));
    }
}
