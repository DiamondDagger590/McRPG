package us.eunoians.mcrpg.event.ability.mining;

import com.diamonddagger590.mccore.configuration.ReloadableContentManager;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.impl.mining.ExtraOre;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.skill.impl.mining.Mining;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExtraOreActivateEventTest extends McRPGBaseTest {

    @BeforeEach
    void setUp() {
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                .register(mock(ReloadableContentManager.class));

        AbilityRegistry abilityRegistry = new AbilityRegistry(mcRPG);
        RegistryAccess.registryAccess().register(abilityRegistry);

        ExtraOre mockExtraOre = mock(ExtraOre.class);
        when(mockExtraOre.getAbilityKey()).thenReturn(ExtraOre.EXTRA_ORE_KEY);
        when(mockExtraOre.getSkillKey()).thenReturn(Mining.MINING_KEY);
        when(mockExtraOre.getReloadableContent()).thenReturn(Set.of());
        abilityRegistry.register(mockExtraOre);
    }

    @Test
    @DisplayName("Negative drop multiplier clamped to 1 at construction")
    void getDropMultiplier_returnsOne_whenConstructedWithNegativeValue() {
        AbilityHolder holder = mock(AbilityHolder.class);
        ExtraOreActivateEvent event = new ExtraOreActivateEvent(holder, -3);
        assertEquals(1, event.getDropMultiplier());
    }

    @Test
    @DisplayName("Zero drop multiplier clamped to 1 at construction")
    void getDropMultiplier_returnsOne_whenConstructedWithZeroValue() {
        AbilityHolder holder = mock(AbilityHolder.class);
        ExtraOreActivateEvent event = new ExtraOreActivateEvent(holder, 0);
        assertEquals(1, event.getDropMultiplier());
    }

    @Test
    @DisplayName("Positive drop multiplier preserved at construction")
    void getDropMultiplier_returnsValue_whenConstructedWithPositiveValue() {
        AbilityHolder holder = mock(AbilityHolder.class);
        ExtraOreActivateEvent event = new ExtraOreActivateEvent(holder, 5);
        assertEquals(5, event.getDropMultiplier());
    }

    @Test
    @DisplayName("setDropMultiplier clamps negative to 1")
    void setDropMultiplier_clampsToOne_whenGivenNegativeValue() {
        AbilityHolder holder = mock(AbilityHolder.class);
        ExtraOreActivateEvent event = new ExtraOreActivateEvent(holder, 3);
        event.setDropMultiplier(-2);
        assertEquals(1, event.getDropMultiplier());
    }

    @Test
    @DisplayName("setDropMultiplier clamps zero to 1")
    void setDropMultiplier_clampsToOne_whenGivenZeroValue() {
        AbilityHolder holder = mock(AbilityHolder.class);
        ExtraOreActivateEvent event = new ExtraOreActivateEvent(holder, 3);
        event.setDropMultiplier(0);
        assertEquals(1, event.getDropMultiplier());
    }

    @Test
    @DisplayName("setDropMultiplier preserves positive value")
    void setDropMultiplier_preservesValue_whenGivenPositiveValue() {
        AbilityHolder holder = mock(AbilityHolder.class);
        ExtraOreActivateEvent event = new ExtraOreActivateEvent(holder, 2);
        event.setDropMultiplier(7);
        assertEquals(7, event.getDropMultiplier());
    }

    @Test
    @DisplayName("getAbility returns ExtraOre instance")
    void getAbility_returnsExtraOreInstance() {
        AbilityHolder holder = mock(AbilityHolder.class);
        ExtraOreActivateEvent event = new ExtraOreActivateEvent(holder, 2);
        assertInstanceOf(ExtraOre.class, event.getAbility());
    }

    @Test
    @DisplayName("Event is not cancelled by default")
    void isCancelled_returnsFalse_byDefault() {
        AbilityHolder holder = mock(AbilityHolder.class);
        ExtraOreActivateEvent event = new ExtraOreActivateEvent(holder, 2);
        assertFalse(event.isCancelled());
    }

    @Test
    @DisplayName("setCancelled(true) makes event cancelled")
    void setCancelled_makesEventCancelled() {
        AbilityHolder holder = mock(AbilityHolder.class);
        ExtraOreActivateEvent event = new ExtraOreActivateEvent(holder, 2);
        event.setCancelled(true);
        assertTrue(event.isCancelled());
    }

    @Test
    @DisplayName("setCancelled(false) restores non-cancelled state")
    void setCancelled_restoresNonCancelledState() {
        AbilityHolder holder = mock(AbilityHolder.class);
        ExtraOreActivateEvent event = new ExtraOreActivateEvent(holder, 2);
        event.setCancelled(true);
        event.setCancelled(false);
        assertFalse(event.isCancelled());
    }

    @Test
    @DisplayName("getAbilityHolder returns the holder passed at construction")
    void getAbilityHolder_returnsConstructorHolder() {
        AbilityHolder holder = mock(AbilityHolder.class);
        ExtraOreActivateEvent event = new ExtraOreActivateEvent(holder, 2);
        assertSame(holder, event.getAbilityHolder());
    }
}
