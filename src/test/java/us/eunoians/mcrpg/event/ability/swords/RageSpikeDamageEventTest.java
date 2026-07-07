package us.eunoians.mcrpg.event.ability.swords;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import org.bukkit.entity.LivingEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.impl.swords.RageSpike;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class RageSpikeDamageEventTest extends McRPGBaseTest {

    @BeforeEach
    void setUp() {
        AbilityRegistry abilityRegistry = new AbilityRegistry(mcRPG);
        RegistryAccess.registryAccess().register(abilityRegistry);
        abilityRegistry.register(new RageSpike(mcRPG));
    }

    @Test
    @DisplayName("Negative damage clamped to 0 at construction")
    void getDamage_returnsZero_whenConstructedWithNegativeDamage() {
        AbilityHolder holder = mock(AbilityHolder.class);
        LivingEntity entity = mock(LivingEntity.class);
        RageSpikeDamageEvent event = new RageSpikeDamageEvent(holder, entity, -5.0);
        assertEquals(0.0, event.getDamage());
    }

    @Test
    @DisplayName("Zero damage preserved at construction")
    void getDamage_returnsZero_whenConstructedWithZeroDamage() {
        AbilityHolder holder = mock(AbilityHolder.class);
        LivingEntity entity = mock(LivingEntity.class);
        RageSpikeDamageEvent event = new RageSpikeDamageEvent(holder, entity, 0.0);
        assertEquals(0.0, event.getDamage());
    }

    @Test
    @DisplayName("Positive damage preserved at construction")
    void getDamage_returnsValue_whenConstructedWithPositiveDamage() {
        AbilityHolder holder = mock(AbilityHolder.class);
        LivingEntity entity = mock(LivingEntity.class);
        RageSpikeDamageEvent event = new RageSpikeDamageEvent(holder, entity, 7.5);
        assertEquals(7.5, event.getDamage());
    }

    @Test
    @DisplayName("setDamage clamps negative to 0")
    void setDamage_clampsToZero_whenGivenNegativeValue() {
        AbilityHolder holder = mock(AbilityHolder.class);
        LivingEntity entity = mock(LivingEntity.class);
        RageSpikeDamageEvent event = new RageSpikeDamageEvent(holder, entity, 5.0);
        event.setDamage(-3.0);
        assertEquals(0.0, event.getDamage());
    }

    @Test
    @DisplayName("setDamage preserves positive value")
    void setDamage_preservesValue_whenGivenPositiveValue() {
        AbilityHolder holder = mock(AbilityHolder.class);
        LivingEntity entity = mock(LivingEntity.class);
        RageSpikeDamageEvent event = new RageSpikeDamageEvent(holder, entity, 5.0);
        event.setDamage(12.0);
        assertEquals(12.0, event.getDamage());
    }

    @Test
    @DisplayName("getDamagedEntity returns the entity passed at construction")
    void getDamagedEntity_returnsConstructorEntity() {
        AbilityHolder holder = mock(AbilityHolder.class);
        LivingEntity entity = mock(LivingEntity.class);
        RageSpikeDamageEvent event = new RageSpikeDamageEvent(holder, entity, 5.0);
        assertSame(entity, event.getDamagedEntity());
    }

    @Test
    @DisplayName("getAbilityHolder returns the holder passed at construction")
    void getAbilityHolder_returnsConstructorHolder() {
        AbilityHolder holder = mock(AbilityHolder.class);
        LivingEntity entity = mock(LivingEntity.class);
        RageSpikeDamageEvent event = new RageSpikeDamageEvent(holder, entity, 5.0);
        assertSame(holder, event.getAbilityHolder());
    }

    @Test
    @DisplayName("getAbility returns RageSpike instance")
    void getAbility_returnsRageSpikeInstance() {
        AbilityHolder holder = mock(AbilityHolder.class);
        LivingEntity entity = mock(LivingEntity.class);
        RageSpikeDamageEvent event = new RageSpikeDamageEvent(holder, entity, 5.0);
        assertInstanceOf(RageSpike.class, event.getAbility());
    }

    @Test
    @DisplayName("Event is not cancelled by default")
    void isCancelled_returnsFalse_byDefault() {
        AbilityHolder holder = mock(AbilityHolder.class);
        LivingEntity entity = mock(LivingEntity.class);
        RageSpikeDamageEvent event = new RageSpikeDamageEvent(holder, entity, 5.0);
        assertFalse(event.isCancelled());
    }

    @Test
    @DisplayName("setCancelled(true) makes event cancelled")
    void setCancelled_makesEventCancelled() {
        AbilityHolder holder = mock(AbilityHolder.class);
        LivingEntity entity = mock(LivingEntity.class);
        RageSpikeDamageEvent event = new RageSpikeDamageEvent(holder, entity, 5.0);
        event.setCancelled(true);
        assertTrue(event.isCancelled());
    }
}
