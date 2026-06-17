package us.eunoians.mcrpg.ability.component.activatable;

import org.bukkit.entity.Entity;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("OnAttackComponent")
public class OnAttackComponentTest extends McRPGBaseTest {

    private static final UUID HOLDER_UUID = UUID.randomUUID();
    private AbilityHolder holder;

    @BeforeEach
    public void setup() {
        holder = mock(AbilityHolder.class);
        when(holder.getUUID()).thenReturn(HOLDER_UUID);
    }

    @Nested
    @DisplayName("shouldActivate")
    class ShouldActivate {

        @DisplayName("returns true when damager matches holder and affectsEntity is true")
        @Test
        public void shouldActivate_returnsTrue_whenDamagerMatchesAndAffectsEntity() {
            Entity damager = mock(Entity.class);
            Entity damaged = mock(Entity.class);
            when(damager.getUniqueId()).thenReturn(HOLDER_UUID);

            EntityDamageByEntityEvent event = mock(EntityDamageByEntityEvent.class);
            when(event.getDamager()).thenReturn(damager);
            when(event.getEntity()).thenReturn(damaged);

            OnAttackComponent component = new OnAttackComponent() {
                @Override
                public boolean affectsEntity(@NotNull Entity entity) {
                    return true;
                }
            };

            assertTrue(component.shouldActivate(holder, event));
        }

        @DisplayName("returns false when damager UUID does not match holder UUID")
        @Test
        public void shouldActivate_returnsFalse_whenDamagerDoesNotMatchHolder() {
            Entity damager = mock(Entity.class);
            Entity damaged = mock(Entity.class);
            when(damager.getUniqueId()).thenReturn(UUID.randomUUID());

            EntityDamageByEntityEvent event = mock(EntityDamageByEntityEvent.class);
            when(event.getDamager()).thenReturn(damager);
            when(event.getEntity()).thenReturn(damaged);

            OnAttackComponent component = new OnAttackComponent() {
                @Override
                public boolean affectsEntity(@NotNull Entity entity) {
                    return true;
                }
            };

            assertFalse(component.shouldActivate(holder, event));
        }

        @DisplayName("returns false when affectsEntity returns false")
        @Test
        public void shouldActivate_returnsFalse_whenAffectsEntityReturnsFalse() {
            Entity damager = mock(Entity.class);
            Entity damaged = mock(Entity.class);
            when(damager.getUniqueId()).thenReturn(HOLDER_UUID);

            EntityDamageByEntityEvent event = mock(EntityDamageByEntityEvent.class);
            when(event.getDamager()).thenReturn(damager);
            when(event.getEntity()).thenReturn(damaged);

            OnAttackComponent component = new OnAttackComponent() {
                @Override
                public boolean affectsEntity(@NotNull Entity entity) {
                    return false;
                }
            };

            assertFalse(component.shouldActivate(holder, event));
        }

        @DisplayName("returns false for non-EntityDamageByEntityEvent")
        @Test
        public void shouldActivate_returnsFalse_whenEventIsNotEntityDamageByEntityEvent() {
            BlockBreakEvent event = mock(BlockBreakEvent.class);

            OnAttackComponent component = new OnAttackComponent() {
                @Override
                public boolean affectsEntity(@NotNull Entity entity) {
                    return true;
                }
            };

            assertFalse(component.shouldActivate(holder, event));
        }

        @DisplayName("passes the damaged entity to affectsEntity")
        @Test
        public void shouldActivate_passesDamagedEntityToAffectsEntity() {
            Entity damager = mock(Entity.class);
            Entity damaged = mock(Entity.class);
            UUID damagedUuid = UUID.randomUUID();
            when(damager.getUniqueId()).thenReturn(HOLDER_UUID);
            when(damaged.getUniqueId()).thenReturn(damagedUuid);

            EntityDamageByEntityEvent event = mock(EntityDamageByEntityEvent.class);
            when(event.getDamager()).thenReturn(damager);
            when(event.getEntity()).thenReturn(damaged);

            final Entity[] receivedEntity = new Entity[1];
            OnAttackComponent component = new OnAttackComponent() {
                @Override
                public boolean affectsEntity(@NotNull Entity entity) {
                    receivedEntity[0] = entity;
                    return true;
                }
            };

            component.shouldActivate(holder, event);
            assertTrue(receivedEntity[0] != null && receivedEntity[0].getUniqueId().equals(damagedUuid));
        }
    }
}
