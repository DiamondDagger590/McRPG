package us.eunoians.mcrpg.event.ability.swords;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import org.bukkit.entity.LivingEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.impl.swords.Bleed;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class BleedActivateEventTest extends McRPGBaseTest {

    @BeforeEach
    void setUp() {
        AbilityRegistry abilityRegistry = new AbilityRegistry(mcRPG);
        RegistryAccess.registryAccess().register(abilityRegistry);
        abilityRegistry.register(new Bleed(mcRPG));
    }

    @Test
    @DisplayName("Negative bleed cycles clamped to 1 at construction")
    void getBleedCycles_returnsOne_whenConstructedWithNegativeCycles() {
        AbilityHolder holder = mock(AbilityHolder.class);
        LivingEntity entity = mock(LivingEntity.class);
        BleedActivateEvent event = new BleedActivateEvent(holder, entity, -5, 3.0);
        assertEquals(1, event.getBleedCycles());
    }

    @Test
    @DisplayName("Zero bleed cycles clamped to 1 at construction")
    void getBleedCycles_returnsOne_whenConstructedWithZeroCycles() {
        AbilityHolder holder = mock(AbilityHolder.class);
        LivingEntity entity = mock(LivingEntity.class);
        BleedActivateEvent event = new BleedActivateEvent(holder, entity, 0, 3.0);
        assertEquals(1, event.getBleedCycles());
    }

    @Test
    @DisplayName("Positive bleed cycles preserved at construction")
    void getBleedCycles_returnsValue_whenConstructedWithPositiveCycles() {
        AbilityHolder holder = mock(AbilityHolder.class);
        LivingEntity entity = mock(LivingEntity.class);
        BleedActivateEvent event = new BleedActivateEvent(holder, entity, 4, 3.0);
        assertEquals(4, event.getBleedCycles());
    }

    @Test
    @DisplayName("setBleedCycles clamps negative to 1")
    void setBleedCycles_clampsToOne_whenGivenNegativeValue() {
        AbilityHolder holder = mock(AbilityHolder.class);
        LivingEntity entity = mock(LivingEntity.class);
        BleedActivateEvent event = new BleedActivateEvent(holder, entity, 3, 3.0);
        event.setBleedCycles(-2);
        assertEquals(1, event.getBleedCycles());
    }

    @Test
    @DisplayName("Negative bleed damage clamped to 1 at construction")
    void getBleedDamage_returnsOne_whenConstructedWithNegativeDamage() {
        AbilityHolder holder = mock(AbilityHolder.class);
        LivingEntity entity = mock(LivingEntity.class);
        BleedActivateEvent event = new BleedActivateEvent(holder, entity, 3, -2.0);
        assertEquals(1.0, event.getBleedDamage());
    }

    @Test
    @DisplayName("Zero bleed damage clamped to 1 at construction")
    void getBleedDamage_returnsOne_whenConstructedWithZeroDamage() {
        AbilityHolder holder = mock(AbilityHolder.class);
        LivingEntity entity = mock(LivingEntity.class);
        BleedActivateEvent event = new BleedActivateEvent(holder, entity, 3, 0.0);
        assertEquals(1.0, event.getBleedDamage());
    }

    @Test
    @DisplayName("Positive bleed damage preserved at construction")
    void getBleedDamage_returnsValue_whenConstructedWithPositiveDamage() {
        AbilityHolder holder = mock(AbilityHolder.class);
        LivingEntity entity = mock(LivingEntity.class);
        BleedActivateEvent event = new BleedActivateEvent(holder, entity, 3, 5.5);
        assertEquals(5.5, event.getBleedDamage());
    }

    @Test
    @DisplayName("setBleedDamage clamps negative to 1")
    void setBleedDamage_clampsToOne_whenGivenNegativeValue() {
        AbilityHolder holder = mock(AbilityHolder.class);
        LivingEntity entity = mock(LivingEntity.class);
        BleedActivateEvent event = new BleedActivateEvent(holder, entity, 3, 5.0);
        event.setBleedDamage(-1.0);
        assertEquals(1.0, event.getBleedDamage());
    }

    @Test
    @DisplayName("getBleedingEntity returns the entity passed at construction")
    void getBleedingEntity_returnsConstructorEntity() {
        AbilityHolder holder = mock(AbilityHolder.class);
        LivingEntity entity = mock(LivingEntity.class);
        BleedActivateEvent event = new BleedActivateEvent(holder, entity, 3, 5.0);
        assertSame(entity, event.getBleedingEntity());
    }

    @Test
    @DisplayName("getAbility returns Bleed instance")
    void getAbility_returnsBleedInstance() {
        AbilityHolder holder = mock(AbilityHolder.class);
        LivingEntity entity = mock(LivingEntity.class);
        BleedActivateEvent event = new BleedActivateEvent(holder, entity, 3, 5.0);
        assertInstanceOf(Bleed.class, event.getAbility());
    }

    @Test
    @DisplayName("Event is not cancelled by default")
    void isCancelled_returnsFalse_byDefault() {
        AbilityHolder holder = mock(AbilityHolder.class);
        LivingEntity entity = mock(LivingEntity.class);
        BleedActivateEvent event = new BleedActivateEvent(holder, entity, 3, 5.0);
        assertFalse(event.isCancelled());
    }

    @Test
    @DisplayName("setCancelled(true) makes event cancelled")
    void setCancelled_makesEventCancelled() {
        AbilityHolder holder = mock(AbilityHolder.class);
        LivingEntity entity = mock(LivingEntity.class);
        BleedActivateEvent event = new BleedActivateEvent(holder, entity, 3, 5.0);
        event.setCancelled(true);
        assertTrue(event.isCancelled());
    }

    @Test
    @DisplayName("getAbilityHolder returns the holder passed at construction")
    void getAbilityHolder_returnsConstructorHolder() {
        AbilityHolder holder = mock(AbilityHolder.class);
        LivingEntity entity = mock(LivingEntity.class);
        BleedActivateEvent event = new BleedActivateEvent(holder, entity, 3, 5.0);
        assertSame(holder, event.getAbilityHolder());
    }
}
