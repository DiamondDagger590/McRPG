package us.eunoians.mcrpg.event.ability.swords;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import org.bukkit.entity.LivingEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.impl.swords.DeeperWound;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class DeeperWoundActivateEventTest extends McRPGBaseTest {

    @BeforeEach
    void setUp() {
        AbilityRegistry abilityRegistry = new AbilityRegistry(mcRPG);
        RegistryAccess.registryAccess().register(abilityRegistry);
        abilityRegistry.register(new DeeperWound(mcRPG));
    }

    @Test
    @DisplayName("Negative additional bleed cycles clamped to 0 at construction")
    void getAdditionalBleedCycles_returnsZero_whenConstructedWithNegativeCycles() {
        AbilityHolder holder = mock(AbilityHolder.class);
        LivingEntity entity = mock(LivingEntity.class);
        DeeperWoundActivateEvent event = new DeeperWoundActivateEvent(holder, entity, -3);
        assertEquals(0, event.getAdditionalBleedCycles());
    }

    @Test
    @DisplayName("Zero additional bleed cycles preserved at construction")
    void getAdditionalBleedCycles_returnsZero_whenConstructedWithZeroCycles() {
        AbilityHolder holder = mock(AbilityHolder.class);
        LivingEntity entity = mock(LivingEntity.class);
        DeeperWoundActivateEvent event = new DeeperWoundActivateEvent(holder, entity, 0);
        assertEquals(0, event.getAdditionalBleedCycles());
    }

    @Test
    @DisplayName("Positive additional bleed cycles preserved at construction")
    void getAdditionalBleedCycles_returnsValue_whenConstructedWithPositiveCycles() {
        AbilityHolder holder = mock(AbilityHolder.class);
        LivingEntity entity = mock(LivingEntity.class);
        DeeperWoundActivateEvent event = new DeeperWoundActivateEvent(holder, entity, 5);
        assertEquals(5, event.getAdditionalBleedCycles());
    }

    @Test
    @DisplayName("setAdditionalBleedCycles clamps negative to 0")
    void setAdditionalBleedCycles_clampsToZero_whenGivenNegativeValue() {
        AbilityHolder holder = mock(AbilityHolder.class);
        LivingEntity entity = mock(LivingEntity.class);
        DeeperWoundActivateEvent event = new DeeperWoundActivateEvent(holder, entity, 5);
        event.setAdditionalBleedCycles(-2);
        assertEquals(0, event.getAdditionalBleedCycles());
    }

    @Test
    @DisplayName("getBleedingEntity returns the entity passed at construction")
    void getBleedingEntity_returnsConstructorEntity() {
        AbilityHolder holder = mock(AbilityHolder.class);
        LivingEntity entity = mock(LivingEntity.class);
        DeeperWoundActivateEvent event = new DeeperWoundActivateEvent(holder, entity, 3);
        assertSame(entity, event.getBleedingEntity());
    }

    @Test
    @DisplayName("getAbility returns DeeperWound instance")
    void getAbility_returnsDeeperWoundInstance() {
        AbilityHolder holder = mock(AbilityHolder.class);
        LivingEntity entity = mock(LivingEntity.class);
        DeeperWoundActivateEvent event = new DeeperWoundActivateEvent(holder, entity, 3);
        assertInstanceOf(DeeperWound.class, event.getAbility());
    }

    @Test
    @DisplayName("Event is not cancelled by default")
    void isCancelled_returnsFalse_byDefault() {
        AbilityHolder holder = mock(AbilityHolder.class);
        LivingEntity entity = mock(LivingEntity.class);
        DeeperWoundActivateEvent event = new DeeperWoundActivateEvent(holder, entity, 3);
        assertFalse(event.isCancelled());
    }

    @Test
    @DisplayName("setCancelled(true) makes event cancelled")
    void setCancelled_makesEventCancelled() {
        AbilityHolder holder = mock(AbilityHolder.class);
        LivingEntity entity = mock(LivingEntity.class);
        DeeperWoundActivateEvent event = new DeeperWoundActivateEvent(holder, entity, 3);
        event.setCancelled(true);
        assertTrue(event.isCancelled());
    }

    @Test
    @DisplayName("getAbilityHolder returns the holder passed at construction")
    void getAbilityHolder_returnsConstructorHolder() {
        AbilityHolder holder = mock(AbilityHolder.class);
        LivingEntity entity = mock(LivingEntity.class);
        DeeperWoundActivateEvent event = new DeeperWoundActivateEvent(holder, entity, 3);
        assertSame(holder, event.getAbilityHolder());
    }
}
