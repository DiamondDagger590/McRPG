package us.eunoians.mcrpg.event.ability.swords;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import org.bukkit.entity.LivingEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.impl.swords.Vampire;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class VampireActivateEventTest extends McRPGBaseTest {

    @BeforeEach
    void setUp() {
        AbilityRegistry abilityRegistry = new AbilityRegistry(mcRPG);
        RegistryAccess.registryAccess().register(abilityRegistry);
        abilityRegistry.register(new Vampire(mcRPG));
    }

    @Test
    @DisplayName("Negative amountToHeal clamped to 0 at construction")
    void getAmountToHeal_returnsZero_whenConstructedWithNegativeAmount() {
        AbilityHolder holder = mock(AbilityHolder.class);
        LivingEntity entity = mock(LivingEntity.class);
        VampireActivateEvent event = new VampireActivateEvent(holder, entity, -5.0);
        assertEquals(0.0, event.getAmountToHeal());
    }

    @Test
    @DisplayName("Zero amountToHeal preserved at construction")
    void getAmountToHeal_returnsZero_whenConstructedWithZeroAmount() {
        AbilityHolder holder = mock(AbilityHolder.class);
        LivingEntity entity = mock(LivingEntity.class);
        VampireActivateEvent event = new VampireActivateEvent(holder, entity, 0.0);
        assertEquals(0.0, event.getAmountToHeal());
    }

    @Test
    @DisplayName("Positive amountToHeal preserved at construction")
    void getAmountToHeal_returnsValue_whenConstructedWithPositiveAmount() {
        AbilityHolder holder = mock(AbilityHolder.class);
        LivingEntity entity = mock(LivingEntity.class);
        VampireActivateEvent event = new VampireActivateEvent(holder, entity, 3.5);
        assertEquals(3.5, event.getAmountToHeal());
    }

    @Test
    @DisplayName("setAmountToHeal clamps negative to 0")
    void setAmountToHeal_clampsToZero_whenGivenNegativeValue() {
        AbilityHolder holder = mock(AbilityHolder.class);
        LivingEntity entity = mock(LivingEntity.class);
        VampireActivateEvent event = new VampireActivateEvent(holder, entity, 5.0);
        event.setAmountToHeal(-2.0);
        assertEquals(0.0, event.getAmountToHeal());
    }

    @Test
    @DisplayName("setAmountToHeal preserves positive value")
    void setAmountToHeal_preservesValue_whenGivenPositiveValue() {
        AbilityHolder holder = mock(AbilityHolder.class);
        LivingEntity entity = mock(LivingEntity.class);
        VampireActivateEvent event = new VampireActivateEvent(holder, entity, 5.0);
        event.setAmountToHeal(8.0);
        assertEquals(8.0, event.getAmountToHeal());
    }

    @Test
    @DisplayName("getBleedingEntity returns the entity passed at construction")
    void getBleedingEntity_returnsConstructorEntity() {
        AbilityHolder holder = mock(AbilityHolder.class);
        LivingEntity entity = mock(LivingEntity.class);
        VampireActivateEvent event = new VampireActivateEvent(holder, entity, 3.0);
        assertSame(entity, event.getBleedingEntity());
    }

    @Test
    @DisplayName("getAbility returns Vampire instance")
    void getAbility_returnsVampireInstance() {
        AbilityHolder holder = mock(AbilityHolder.class);
        LivingEntity entity = mock(LivingEntity.class);
        VampireActivateEvent event = new VampireActivateEvent(holder, entity, 3.0);
        assertInstanceOf(Vampire.class, event.getAbility());
    }

    @Test
    @DisplayName("Event is not cancelled by default")
    void isCancelled_returnsFalse_byDefault() {
        AbilityHolder holder = mock(AbilityHolder.class);
        LivingEntity entity = mock(LivingEntity.class);
        VampireActivateEvent event = new VampireActivateEvent(holder, entity, 3.0);
        assertFalse(event.isCancelled());
    }

    @Test
    @DisplayName("setCancelled(true) makes event cancelled")
    void setCancelled_makesEventCancelled() {
        AbilityHolder holder = mock(AbilityHolder.class);
        LivingEntity entity = mock(LivingEntity.class);
        VampireActivateEvent event = new VampireActivateEvent(holder, entity, 3.0);
        event.setCancelled(true);
        assertTrue(event.isCancelled());
    }

    @Test
    @DisplayName("getAbilityHolder returns the holder passed at construction")
    void getAbilityHolder_returnsConstructorHolder() {
        AbilityHolder holder = mock(AbilityHolder.class);
        LivingEntity entity = mock(LivingEntity.class);
        VampireActivateEvent event = new VampireActivateEvent(holder, entity, 3.0);
        assertSame(holder, event.getAbilityHolder());
    }
}
