package us.eunoians.mcrpg.event.ability.swords;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import org.bukkit.entity.LivingEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.impl.swords.EnhancedBleed;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class EnhancedBleedActivateEventTest extends McRPGBaseTest {

    @BeforeEach
    void setUp() {
        AbilityRegistry abilityRegistry = new AbilityRegistry(mcRPG);
        RegistryAccess.registryAccess().register(abilityRegistry);
        abilityRegistry.register(new EnhancedBleed(mcRPG));
    }

    @Test
    @DisplayName("Negative additional bleed damage clamped to 0 at construction")
    void getAdditionalBleedDamage_returnsZero_whenConstructedWithNegativeDamage() {
        AbilityHolder holder = mock(AbilityHolder.class);
        LivingEntity entity = mock(LivingEntity.class);
        EnhancedBleedActivateEvent event = new EnhancedBleedActivateEvent(holder, entity, -3.0);
        assertEquals(0.0, event.getAdditionalBleedDamage());
    }

    @Test
    @DisplayName("Zero additional bleed damage preserved at construction")
    void getAdditionalBleedDamage_returnsZero_whenConstructedWithZeroDamage() {
        AbilityHolder holder = mock(AbilityHolder.class);
        LivingEntity entity = mock(LivingEntity.class);
        EnhancedBleedActivateEvent event = new EnhancedBleedActivateEvent(holder, entity, 0.0);
        assertEquals(0.0, event.getAdditionalBleedDamage());
    }

    @Test
    @DisplayName("Positive additional bleed damage preserved at construction")
    void getAdditionalBleedDamage_returnsValue_whenConstructedWithPositiveDamage() {
        AbilityHolder holder = mock(AbilityHolder.class);
        LivingEntity entity = mock(LivingEntity.class);
        EnhancedBleedActivateEvent event = new EnhancedBleedActivateEvent(holder, entity, 4.5);
        assertEquals(4.5, event.getAdditionalBleedDamage());
    }

    @Test
    @DisplayName("setAdditionalBleedDamage clamps negative to 0")
    void setAdditionalBleedDamage_clampsToZero_whenGivenNegativeValue() {
        AbilityHolder holder = mock(AbilityHolder.class);
        LivingEntity entity = mock(LivingEntity.class);
        EnhancedBleedActivateEvent event = new EnhancedBleedActivateEvent(holder, entity, 5.0);
        event.setAdditionalBleedDamage(-1.0);
        assertEquals(0.0, event.getAdditionalBleedDamage());
    }

    @Test
    @DisplayName("getBleedingEntity returns the entity passed at construction")
    void getBleedingEntity_returnsConstructorEntity() {
        AbilityHolder holder = mock(AbilityHolder.class);
        LivingEntity entity = mock(LivingEntity.class);
        EnhancedBleedActivateEvent event = new EnhancedBleedActivateEvent(holder, entity, 3.0);
        assertSame(entity, event.getBleedingEntity());
    }

    @Test
    @DisplayName("getAbility returns EnhancedBleed instance")
    void getAbility_returnsEnhancedBleedInstance() {
        AbilityHolder holder = mock(AbilityHolder.class);
        LivingEntity entity = mock(LivingEntity.class);
        EnhancedBleedActivateEvent event = new EnhancedBleedActivateEvent(holder, entity, 3.0);
        assertInstanceOf(EnhancedBleed.class, event.getAbility());
    }

    @Test
    @DisplayName("Event is not cancelled by default")
    void isCancelled_returnsFalse_byDefault() {
        AbilityHolder holder = mock(AbilityHolder.class);
        LivingEntity entity = mock(LivingEntity.class);
        EnhancedBleedActivateEvent event = new EnhancedBleedActivateEvent(holder, entity, 3.0);
        assertFalse(event.isCancelled());
    }

    @Test
    @DisplayName("setCancelled(true) makes event cancelled")
    void setCancelled_makesEventCancelled() {
        AbilityHolder holder = mock(AbilityHolder.class);
        LivingEntity entity = mock(LivingEntity.class);
        EnhancedBleedActivateEvent event = new EnhancedBleedActivateEvent(holder, entity, 3.0);
        event.setCancelled(true);
        assertTrue(event.isCancelled());
    }

    @Test
    @DisplayName("getAbilityHolder returns the holder passed at construction")
    void getAbilityHolder_returnsConstructorHolder() {
        AbilityHolder holder = mock(AbilityHolder.class);
        LivingEntity entity = mock(LivingEntity.class);
        EnhancedBleedActivateEvent event = new EnhancedBleedActivateEvent(holder, entity, 3.0);
        assertSame(holder, event.getAbilityHolder());
    }
}
