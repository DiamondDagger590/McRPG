package us.eunoians.mcrpg.listener.combat;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;

import java.util.Optional;
import java.util.UUID;

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

    @Nested
    @DisplayName("resolve()")
    class ResolveTests {

        @Test
        @DisplayName("Given a direct melee hit between two LivingEntities, then returns Combatants with correct source and target")
        void resolve_returnsCombatants_whenDirectMeleeHit() {
            LivingEntity attacker = mock(LivingEntity.class);
            LivingEntity victim = mock(LivingEntity.class);
            when(attacker.getUniqueId()).thenReturn(UUID.randomUUID());
            when(victim.getUniqueId()).thenReturn(UUID.randomUUID());

            EntityDamageByEntityEvent event = mock(EntityDamageByEntityEvent.class);
            when(event.getDamager()).thenReturn(attacker);
            when(event.getEntity()).thenReturn(victim);

            Optional<CombatDamageResolver.Combatants> result = resolver.resolve(event);

            assertTrue(result.isPresent());
            assertSame(attacker, result.get().source());
            assertSame(victim, result.get().target());
        }

        @Test
        @DisplayName("Given a projectile with an Entity shooter, then unwraps to the shooter as source")
        void resolve_unwrapsProjectileToShooter_whenShooterIsEntity() {
            Player shooter = mock(Player.class);
            UUID shooterUUID = UUID.randomUUID();
            when(shooter.getUniqueId()).thenReturn(shooterUUID);

            // Projectile that also implements Entity (mock both interfaces via the Projectile type)
            Projectile projectile = mock(Projectile.class);
            when(projectile.getShooter()).thenReturn(shooter);

            LivingEntity target = mock(LivingEntity.class);
            when(target.getUniqueId()).thenReturn(UUID.randomUUID());

            EntityDamageByEntityEvent event = mock(EntityDamageByEntityEvent.class);
            when(event.getDamager()).thenReturn(projectile);
            when(event.getEntity()).thenReturn(target);

            Optional<CombatDamageResolver.Combatants> result = resolver.resolve(event);

            assertTrue(result.isPresent());
            assertSame(shooter, result.get().source());
            assertSame(target, result.get().target());
        }

        @Test
        @DisplayName("Given a projectile whose shooter is not an Entity, then returns empty")
        void resolve_returnsEmpty_whenProjectileShooterIsNotEntity() {
            Projectile projectile = mock(Projectile.class);
            when(projectile.getShooter()).thenReturn(mock(org.bukkit.projectiles.ProjectileSource.class));

            LivingEntity target = mock(LivingEntity.class);

            EntityDamageByEntityEvent event = mock(EntityDamageByEntityEvent.class);
            when(event.getDamager()).thenReturn(projectile);
            when(event.getEntity()).thenReturn(target);

            Optional<CombatDamageResolver.Combatants> result = resolver.resolve(event);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Given a projectile with a null shooter, then returns empty")
        void resolve_returnsEmpty_whenProjectileShooterIsNull() {
            Projectile projectile = mock(Projectile.class);
            when(projectile.getShooter()).thenReturn(null);

            LivingEntity target = mock(LivingEntity.class);

            EntityDamageByEntityEvent event = mock(EntityDamageByEntityEvent.class);
            when(event.getDamager()).thenReturn(projectile);
            when(event.getEntity()).thenReturn(target);

            Optional<CombatDamageResolver.Combatants> result = resolver.resolve(event);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Given a non-LivingEntity damager, then returns empty")
        void resolve_returnsEmpty_whenDamagerIsNotLivingEntity() {
            Entity nonLivingDamager = mock(Entity.class);

            LivingEntity target = mock(LivingEntity.class);

            EntityDamageByEntityEvent event = mock(EntityDamageByEntityEvent.class);
            when(event.getDamager()).thenReturn(nonLivingDamager);
            when(event.getEntity()).thenReturn(target);

            Optional<CombatDamageResolver.Combatants> result = resolver.resolve(event);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Given a non-LivingEntity target, then returns empty")
        void resolve_returnsEmpty_whenTargetIsNotLivingEntity() {
            LivingEntity attacker = mock(LivingEntity.class);
            when(attacker.getUniqueId()).thenReturn(UUID.randomUUID());

            Entity nonLivingTarget = mock(Entity.class);

            EntityDamageByEntityEvent event = mock(EntityDamageByEntityEvent.class);
            when(event.getDamager()).thenReturn(attacker);
            when(event.getEntity()).thenReturn(nonLivingTarget);

            Optional<CombatDamageResolver.Combatants> result = resolver.resolve(event);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Given self-damage (same UUID), then returns empty")
        void resolve_returnsEmpty_whenSelfDamage() {
            UUID sharedUUID = UUID.randomUUID();

            LivingEntity entity = mock(LivingEntity.class);
            when(entity.getUniqueId()).thenReturn(sharedUUID);

            EntityDamageByEntityEvent event = mock(EntityDamageByEntityEvent.class);
            when(event.getDamager()).thenReturn(entity);
            when(event.getEntity()).thenReturn(entity);

            Optional<CombatDamageResolver.Combatants> result = resolver.resolve(event);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Combatants record")
    class CombatantsTests {

        @Test
        @DisplayName("sourceUUID returns the source entity's UUID")
        void sourceUUID_returnsSourceEntityUUID() {
            UUID sourceUUID = UUID.randomUUID();
            LivingEntity source = mock(LivingEntity.class);
            when(source.getUniqueId()).thenReturn(sourceUUID);
            LivingEntity target = mock(LivingEntity.class);
            when(target.getUniqueId()).thenReturn(UUID.randomUUID());

            CombatDamageResolver.Combatants combatants = new CombatDamageResolver.Combatants(source, target);

            assertEquals(sourceUUID, combatants.sourceUUID());
        }

        @Test
        @DisplayName("targetUUID returns the target entity's UUID")
        void targetUUID_returnsTargetEntityUUID() {
            UUID targetUUID = UUID.randomUUID();
            LivingEntity source = mock(LivingEntity.class);
            when(source.getUniqueId()).thenReturn(UUID.randomUUID());
            LivingEntity target = mock(LivingEntity.class);
            when(target.getUniqueId()).thenReturn(targetUUID);

            CombatDamageResolver.Combatants combatants = new CombatDamageResolver.Combatants(source, target);

            assertEquals(targetUUID, combatants.targetUUID());
        }

        @Test
        @DisplayName("source and target accessors return the entities passed to the constructor")
        void accessors_returnConstructorEntities() {
            LivingEntity source = mock(LivingEntity.class);
            when(source.getUniqueId()).thenReturn(UUID.randomUUID());
            LivingEntity target = mock(LivingEntity.class);
            when(target.getUniqueId()).thenReturn(UUID.randomUUID());

            CombatDamageResolver.Combatants combatants = new CombatDamageResolver.Combatants(source, target);

            assertSame(source, combatants.source());
            assertSame(target, combatants.target());
        }
    }
}
