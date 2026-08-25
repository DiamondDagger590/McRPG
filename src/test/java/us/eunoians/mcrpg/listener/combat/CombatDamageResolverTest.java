package us.eunoians.mcrpg.listener.combat;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.projectiles.BlockProjectileSource;
import org.bukkit.projectiles.ProjectileSource;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("CombatDamageResolver")
class CombatDamageResolverTest extends McRPGBaseTest {

    private CombatDamageResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new CombatDamageResolver();
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

    @Nested
    @DisplayName("Direct melee hits")
    class DirectMeleeHits {

        @Test
        @DisplayName("resolves player-vs-mob as Combatants")
        void resolve_playerVsMob_returnsCombatants() {
            PlayerMock player = server.addPlayer();
            Zombie mob = spawnEntity(Zombie.class);

            Optional<CombatDamageResolver.Combatants> result = resolver.resolve(damageEvent(player, mob));

            assertTrue(result.isPresent());
            assertSame(player, result.get().source());
            assertSame(mob, result.get().target());
        }

        @Test
        @DisplayName("resolves mob-vs-player as Combatants")
        void resolve_mobVsPlayer_returnsCombatants() {
            Zombie mob = spawnEntity(Zombie.class);
            PlayerMock player = server.addPlayer();

            Optional<CombatDamageResolver.Combatants> result = resolver.resolve(damageEvent(mob, player));

            assertTrue(result.isPresent());
            assertSame(mob, result.get().source());
            assertSame(player, result.get().target());
        }

        @Test
        @DisplayName("resolves player-vs-player as Combatants")
        void resolve_playerVsPlayer_returnsCombatants() {
            PlayerMock attacker = server.addPlayer();
            PlayerMock victim = server.addPlayer();

            Optional<CombatDamageResolver.Combatants> result = resolver.resolve(damageEvent(attacker, victim));

            assertTrue(result.isPresent());
            assertSame(attacker, result.get().source());
            assertSame(victim, result.get().target());
        }

        @Test
        @DisplayName("resolves mob-vs-mob as Combatants")
        void resolve_mobVsMob_returnsCombatants() {
            Zombie attacker = spawnEntity(Zombie.class);
            Zombie target = spawnEntity(Zombie.class);

            Optional<CombatDamageResolver.Combatants> result = resolver.resolve(damageEvent(attacker, target));

            assertTrue(result.isPresent());
            assertSame(attacker, result.get().source());
            assertSame(target, result.get().target());
        }
    }

    @Nested
    @DisplayName("Projectile unwrapping")
    class ProjectileUnwrapping {

        @Test
        @DisplayName("unwraps projectile to its living shooter")
        void resolve_projectileWithLivingShooter_unwrapsToShooter() {
            PlayerMock shooter = server.addPlayer();
            Zombie target = spawnEntity(Zombie.class);
            Arrow arrow = mock(Arrow.class);
            when(arrow.getShooter()).thenReturn(shooter);

            Optional<CombatDamageResolver.Combatants> result = resolver.resolve(damageEvent(arrow, target));

            assertTrue(result.isPresent());
            assertSame(shooter, result.get().source());
            assertSame(target, result.get().target());
        }

        @Test
        @DisplayName("returns empty when projectile shooter is not an Entity")
        void resolve_projectileWithNonEntityShooter_returnsEmpty() {
            Zombie target = spawnEntity(Zombie.class);
            Arrow arrow = mock(Arrow.class);
            when(arrow.getShooter()).thenReturn(mock(BlockProjectileSource.class));

            Optional<CombatDamageResolver.Combatants> result = resolver.resolve(damageEvent(arrow, target));

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("returns empty when projectile shooter is null")
        void resolve_projectileWithNullShooter_returnsEmpty() {
            Zombie target = spawnEntity(Zombie.class);
            Arrow arrow = mock(Arrow.class);
            when(arrow.getShooter()).thenReturn(null);

            Optional<CombatDamageResolver.Combatants> result = resolver.resolve(damageEvent(arrow, target));

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("returns empty when projectile shooter is an Entity but not a LivingEntity")
        void resolve_projectileWithNonLivingEntityShooter_returnsEmpty() {
            Zombie target = spawnEntity(Zombie.class);
            Arrow arrow = mock(Arrow.class);
            NonLivingShooter nonLivingShooter = mock(NonLivingShooter.class);
            when(arrow.getShooter()).thenReturn(nonLivingShooter);

            Optional<CombatDamageResolver.Combatants> result = resolver.resolve(damageEvent(arrow, target));

            assertTrue(result.isEmpty());
        }
    }

    /**
     * Test interface combining {@link Entity} and {@link ProjectileSource}
     * without extending {@link LivingEntity}, so Mockito can produce a mock that passes the
     * {@code instanceof Entity} check in the projectile-unwrap branch but fails the
     * {@code instanceof LivingEntity} guard.
     */
    private interface NonLivingShooter extends Entity, ProjectileSource {
    }

    @Nested
    @DisplayName("Rejection guards")
    class RejectionGuards {

        @Test
        @DisplayName("returns empty when damager is not a LivingEntity")
        void resolve_nonLivingDamager_returnsEmpty() {
            Entity nonLiving = mock(Entity.class);
            Zombie target = spawnEntity(Zombie.class);

            Optional<CombatDamageResolver.Combatants> result = resolver.resolve(damageEvent(nonLiving, target));

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("returns empty when target is not a LivingEntity")
        void resolve_nonLivingTarget_returnsEmpty() {
            PlayerMock player = server.addPlayer();
            Entity nonLiving = mock(Entity.class);

            Optional<CombatDamageResolver.Combatants> result = resolver.resolve(damageEvent(player, nonLiving));

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("returns empty for self-damage")
        void resolve_selfDamage_returnsEmpty() {
            PlayerMock player = server.addPlayer();

            Optional<CombatDamageResolver.Combatants> result = resolver.resolve(damageEvent(player, player));

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Combatants record")
    class CombatantsRecord {

        @Test
        @DisplayName("sourceUUID returns the source entity's UUID")
        void sourceUUID_returnsSourceEntityUUID() {
            PlayerMock player = server.addPlayer();
            Zombie mob = spawnEntity(Zombie.class);

            Optional<CombatDamageResolver.Combatants> result = resolver.resolve(damageEvent(player, mob));

            assertTrue(result.isPresent());
            assertEquals(player.getUniqueId(), result.get().sourceUUID());
        }

        @Test
        @DisplayName("targetUUID returns the target entity's UUID")
        void targetUUID_returnsTargetEntityUUID() {
            PlayerMock player = server.addPlayer();
            Zombie mob = spawnEntity(Zombie.class);

            Optional<CombatDamageResolver.Combatants> result = resolver.resolve(damageEvent(player, mob));

            assertTrue(result.isPresent());
            assertEquals(mob.getUniqueId(), result.get().targetUUID());
        }

        @Test
        @DisplayName("source and target references match the resolved entities")
        void combatants_sourceAndTarget_matchResolvedEntities() {
            Zombie attacker = spawnEntity(Zombie.class);
            Zombie target = spawnEntity(Zombie.class);
            Arrow arrow = mock(Arrow.class);
            when(arrow.getShooter()).thenReturn(attacker);

            Optional<CombatDamageResolver.Combatants> result = resolver.resolve(damageEvent(arrow, target));

            assertTrue(result.isPresent());
            assertSame(attacker, result.get().source());
            assertSame(target, result.get().target());
            assertEquals(attacker.getUniqueId(), result.get().sourceUUID());
            assertEquals(target.getUniqueId(), result.get().targetUUID());
        }
    }
}
