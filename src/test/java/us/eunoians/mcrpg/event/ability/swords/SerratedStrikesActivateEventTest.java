package us.eunoians.mcrpg.event.ability.swords;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.impl.swords.SerratedStrikes;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link SerratedStrikesActivateEvent}.
 * <p>
 * Verifies that duration is clamped to at least 0 and that the event honours the
 * {@link org.bukkit.event.Cancellable} contract.
 */
class SerratedStrikesActivateEventTest extends McRPGBaseTest {

    @BeforeEach
    void setUp() {
        // SerratedStrikesActivateEvent has a static initializer that resolves SerratedStrikes
        // from the AbilityRegistry. Ensure the registry is populated before the class is
        // initialized so the static field is non-null when behavioral tests instantiate the event.
        AbilityRegistry abilityRegistry = new AbilityRegistry(mcRPG);
        RegistryAccess.registryAccess().register(abilityRegistry);
        abilityRegistry.register(new SerratedStrikes(mcRPG));
    }

    @Test
    @DisplayName("Given a negative duration at construction, when getting duration, then it is clamped to zero")
    void getDuration_returnsZero_whenConstructedWithNegativeDuration() {
        AbilityHolder holder = mock(AbilityHolder.class);
        SerratedStrikesActivateEvent event = new SerratedStrikesActivateEvent(holder, -5);
        assertEquals(0, event.getDuration());
    }

    @Test
    @DisplayName("Given a negative value, when setDuration is called, then duration is clamped to zero")
    void setDuration_clampsToZero_whenGivenNegativeValue() {
        AbilityHolder holder = mock(AbilityHolder.class);
        SerratedStrikesActivateEvent event = new SerratedStrikesActivateEvent(holder, 10);
        event.setDuration(-3);
        assertEquals(0, event.getDuration());
    }

    @Test
    @DisplayName("Given a positive duration, when getting duration, then it is preserved")
    void getDuration_returnsPositiveValue_whenConstructedWithPositiveDuration() {
        AbilityHolder holder = mock(AbilityHolder.class);
        SerratedStrikesActivateEvent event = new SerratedStrikesActivateEvent(holder, 7);
        assertEquals(7, event.getDuration());
    }

    @Test
    @DisplayName("When a new event is created, then it is not cancelled by default")
    void isCancelled_returnsFalse_byDefault() {
        AbilityHolder holder = mock(AbilityHolder.class);
        SerratedStrikesActivateEvent event = new SerratedStrikesActivateEvent(holder, 5);
        assertFalse(event.isCancelled());
    }

    @Test
    @DisplayName("Given a new event, when setCancelled(true) is called, then isCancelled returns true")
    void setCancelled_makesEventCancelled_whenSetToTrue() {
        AbilityHolder holder = mock(AbilityHolder.class);
        SerratedStrikesActivateEvent event = new SerratedStrikesActivateEvent(holder, 5);
        event.setCancelled(true);
        assertTrue(event.isCancelled());
    }
}
