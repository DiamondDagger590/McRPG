package us.eunoians.mcrpg.listener.combat;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.EntityRemoveEvent;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.combat.CombatTrackerManager;
import us.eunoians.mcrpg.combat.ParticipantRemovalReason;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("OnCombatEntityRemoveListener")
class OnCombatEntityRemoveListenerTest extends McRPGBaseTest {

    private CombatTrackerManager manager;
    private OnCombatEntityRemoveListener listener;

    @BeforeEach
    void setUp() {
        manager = mock(CombatTrackerManager.class);
        listener = new OnCombatEntityRemoveListener(manager);
    }

    /**
     * Builds a mock {@link EntityRemoveEvent} for the given entity and cause.
     *
     * @param entity The removed entity.
     * @param cause  The removal cause.
     * @return A mock event.
     */
    private EntityRemoveEvent removeEvent(Entity entity, EntityRemoveEvent.Cause cause) {
        EntityRemoveEvent event = mock(EntityRemoveEvent.class);
        when(event.getEntity()).thenReturn(entity);
        when(event.getCause()).thenReturn(cause);
        return event;
    }

    @Test
    @DisplayName("removes a despawning non-player living entity from all sessions")
    void removesLivingEntity_onDespawn() {
        Zombie mob = spawnEntity(Zombie.class);

        listener.onEntityRemove(removeEvent(mob, EntityRemoveEvent.Cause.DESPAWN));

        verify(manager).removeParticipantFromAllSessions(mob.getUniqueId(), ParticipantRemovalReason.DESPAWN);
    }

    @Test
    @DisplayName("ignores DEATH-cause removals (handled by the death listener)")
    void ignoresDeathCause() {
        Zombie mob = spawnEntity(Zombie.class);

        listener.onEntityRemove(removeEvent(mob, EntityRemoveEvent.Cause.DEATH));

        verify(manager, never()).removeParticipantFromAllSessions(any(), any());
    }

    @Test
    @DisplayName("ignores player removals (handled by the quit listener)")
    void ignoresPlayers() {
        PlayerMock player = server.addPlayer();

        listener.onEntityRemove(removeEvent(player, EntityRemoveEvent.Cause.DESPAWN));

        verify(manager, never()).removeParticipantFromAllSessions(eq(player.getUniqueId()), any());
    }

    @Test
    @DisplayName("ignores non-living entities (items, projectiles, orbs)")
    void ignoresNonLivingEntities() {
        Entity nonLiving = mock(Entity.class);

        listener.onEntityRemove(removeEvent(nonLiving, EntityRemoveEvent.Cause.DESPAWN));

        verify(manager, never()).removeParticipantFromAllSessions(any(), any());
    }
}
