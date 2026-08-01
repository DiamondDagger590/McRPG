package us.eunoians.mcrpg.listener.combat;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityRegainHealthEvent;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@DisplayName("OnCombatHealingStatListener")
class OnCombatHealingStatListenerTest extends McRPGBaseTest {

    private CombatTrackerManager manager;
    private OnCombatHealingStatListener listener;

    @BeforeEach
    void setUp() {
        manager = mock(CombatTrackerManager.class);
        listener = new OnCombatHealingStatListener(manager);
    }

    private EntityRegainHealthEvent healEvent(LivingEntity entity, double amount) {
        EntityRegainHealthEvent event = mock(EntityRegainHealthEvent.class);
        when(event.getEntity()).thenReturn(entity);
        when(event.getAmount()).thenReturn(amount);
        return event;
    }

    @Test
    @DisplayName("increments healing_received on the target's active session")
    void incrementsHealingReceived_onActiveSession() {
        PlayerMock player = server.addPlayer();
        CombatSession session = new CombatSession(player.getUniqueId(), 16, 8000L);
        when(manager.getSession(player.getUniqueId())).thenReturn(Optional.of(session));

        listener.onEntityRegainHealth(healEvent(player, 3.5));

        assertEquals(3.5, session.getStatistics().getDouble(CombatSessionStatisticKey.HEALING_RECEIVED));
    }

    @Test
    @DisplayName("does not increment when the target has no active session")
    void doesNotIncrement_whenNoActiveSession() {
        PlayerMock player = server.addPlayer();
        when(manager.getSession(player.getUniqueId())).thenReturn(Optional.empty());

        listener.onEntityRegainHealth(healEvent(player, 3.5));

        verify(manager).getSession(player.getUniqueId());
        verifyNoMoreInteractions(manager);
    }

    @Test
    @DisplayName("does not create sessions or add participants")
    void doesNotCreateSessionsOrAddParticipants() {
        PlayerMock player = server.addPlayer();
        when(manager.getSession(player.getUniqueId())).thenReturn(Optional.empty());

        listener.onEntityRegainHealth(healEvent(player, 3.5));

        verify(manager, never()).handleCombatInteraction(any(UUID.class), any(UUID.class), any(Entity.class), any(Entity.class));
        verify(manager, never()).reportCombatActivity(any(UUID.class), any(UUID.class));
    }
}
