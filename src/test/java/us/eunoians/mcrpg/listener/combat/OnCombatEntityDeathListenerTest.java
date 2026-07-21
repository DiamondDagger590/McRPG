package us.eunoians.mcrpg.listener.combat;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.EntityDeathEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.combat.CombatSession;
import us.eunoians.mcrpg.combat.CombatSessionEndReason;
import us.eunoians.mcrpg.combat.CombatTrackerManager;
import us.eunoians.mcrpg.combat.ParticipantRemovalReason;
import us.eunoians.mcrpg.combat.stat.CombatSessionStatisticKey;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("OnCombatEntityDeathListener")
class OnCombatEntityDeathListenerTest extends McRPGBaseTest {

    private CombatTrackerManager manager;
    private OnCombatEntityDeathListener listener;

    @BeforeEach
    void setUp() {
        manager = mock(CombatTrackerManager.class);
        listener = new OnCombatEntityDeathListener(manager);
    }

    private EntityDeathEvent deathEvent(LivingEntity entity, Player killer) {
        EntityDeathEvent event = mock(EntityDeathEvent.class);
        when(event.getEntity()).thenReturn(entity);
        when(entity.getKiller()).thenReturn(killer);
        return event;
    }

    /**
     * Spawns a real MockBukkit mob wrapped in a spy so {@code getKiller()} can be stubbed — the
     * listener reads the killer off the dead entity itself.
     */
    private Zombie mob() {
        return spawnEntity(Zombie.class);
    }

    @Test
    @DisplayName("increments kills on the killer's session when a player kills a mob")
    void incrementsKills_whenPlayerKillsMob() {
        PlayerMock killer = server.addPlayer();
        Zombie mob = mob();
        CombatSession killerSession = new CombatSession(killer.getUniqueId(), 16, 8000L);
        when(manager.getSession(killer.getUniqueId())).thenReturn(Optional.of(killerSession));

        listener.onEntityDeath(deathEvent(mob, killer));

        assertEquals(1L, killerSession.getStatistics().getLong(CombatSessionStatisticKey.KILLS));
    }

    @Test
    @DisplayName("increments kills on the killer's session when a player kills another player")
    void incrementsKills_whenPlayerKillsPlayer() {
        PlayerMock killer = server.addPlayer();
        // spy(), not mock(): PlayerMock is a real MockBukkit implementation, and getKiller() must
        // be stubbed on it directly since EntityDeathEvent.getEntity() returns the victim itself.
        PlayerMock victim = spy(server.addPlayer());
        CombatSession killerSession = new CombatSession(killer.getUniqueId(), 16, 8000L);
        when(manager.getSession(killer.getUniqueId())).thenReturn(Optional.of(killerSession));

        listener.onEntityDeath(deathEvent(victim, killer));

        assertEquals(1L, killerSession.getStatistics().getLong(CombatSessionStatisticKey.KILLS));
    }

    @Test
    @DisplayName("does not increment when there is no killer")
    void doesNotIncrement_whenNoKiller() {
        Zombie mob = mob();

        listener.onEntityDeath(deathEvent(mob, null));

        verify(manager, never()).getSession(any(UUID.class));
    }

    @Test
    @DisplayName("does not increment when the killer has no active session")
    void doesNotIncrement_whenKillerHasNoSession() {
        PlayerMock killer = server.addPlayer();
        Zombie mob = mob();
        when(manager.getSession(killer.getUniqueId())).thenReturn(Optional.empty());

        listener.onEntityDeath(deathEvent(mob, killer));

        verify(manager).getSession(killer.getUniqueId());
    }

    @Test
    @DisplayName("ends the dead entity's session and drops it from every other session's roster")
    void endsSessionAndRemovesParticipant_forDeadEntity() {
        PlayerMock killer = server.addPlayer();
        Zombie mob = mob();
        when(manager.getSession(killer.getUniqueId())).thenReturn(Optional.empty());

        listener.onEntityDeath(deathEvent(mob, killer));

        verify(manager).endSession(mob.getUniqueId(), CombatSessionEndReason.DEATH);
        verify(manager).removeParticipantFromAllSessions(mob.getUniqueId(), ParticipantRemovalReason.DEATH);
    }
}
