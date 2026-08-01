package us.eunoians.mcrpg.listener.combat;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.combat.CombatTrackerManager;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("OnCombatDamageListener")
class OnCombatDamageListenerTest extends McRPGBaseTest {

    private CombatTrackerManager manager;
    private OnCombatDamageListener listener;

    @BeforeEach
    void setUp() {
        manager = mock(CombatTrackerManager.class);
        listener = new OnCombatDamageListener(manager, new CombatDamageResolver());
    }

    /**
     * Builds a mock {@link EntityDamageByEntityEvent} with the given damager and target.
     *
     * @param damager The damaging entity.
     * @param target  The damaged entity.
     * @return A mock event.
     */
    private EntityDamageByEntityEvent damageEvent(Entity damager, Entity target) {
        EntityDamageByEntityEvent event = mock(EntityDamageByEntityEvent.class);
        when(event.getDamager()).thenReturn(damager);
        when(event.getEntity()).thenReturn(target);
        return event;
    }

    @Test
    @DisplayName("reports the interaction for a direct living-entity hit")
    void reportsInteraction_forDirectLivingHit() {
        PlayerMock player = server.addPlayer();
        Zombie mob = spawnEntity(Zombie.class);

        listener.onEntityDamageByEntity(damageEvent(player, mob));

        verify(manager).handleCombatInteraction(player.getUniqueId(), mob.getUniqueId(), player, mob);
    }

    @Test
    @DisplayName("resolves the projectile shooter as the source")
    void resolvesProjectileShooter_asSource() {
        PlayerMock player = server.addPlayer();
        Zombie mob = spawnEntity(Zombie.class);
        Arrow arrow = mock(Arrow.class);
        when(arrow.getShooter()).thenReturn(player);

        listener.onEntityDamageByEntity(damageEvent(arrow, mob));

        verify(manager).handleCombatInteraction(player.getUniqueId(), mob.getUniqueId(), player, mob);
    }

    @Test
    @DisplayName("ignores a projectile with no entity shooter")
    void ignoresProjectile_withoutEntityShooter() {
        Zombie mob = spawnEntity(Zombie.class);
        Arrow arrow = mock(Arrow.class);
        when(arrow.getShooter()).thenReturn(null);

        listener.onEntityDamageByEntity(damageEvent(arrow, mob));

        verify(manager, never()).handleCombatInteraction(any(), any(), any(Entity.class), any(Entity.class));
    }

    @Test
    @DisplayName("ignores a non-living damager")
    void ignoresNonLivingDamager() {
        Zombie mob = spawnEntity(Zombie.class);
        Entity nonLiving = mock(Entity.class);

        listener.onEntityDamageByEntity(damageEvent(nonLiving, mob));

        verify(manager, never()).handleCombatInteraction(any(), any(), any(Entity.class), any(Entity.class));
    }

    @Test
    @DisplayName("ignores self-damage where source and target are the same entity")
    void ignoresSelfDamage() {
        PlayerMock player = server.addPlayer();

        listener.onEntityDamageByEntity(damageEvent(player, player));

        verify(manager, never()).handleCombatInteraction(any(), any(), any(Entity.class), any(Entity.class));
    }
}
