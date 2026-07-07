package us.eunoians.mcrpg.event.ability.swords;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.impl.swords.RageSpike;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class RageSpikeActivateEventTest extends McRPGBaseTest {

    @BeforeEach
    void setUp() {
        AbilityRegistry abilityRegistry = new AbilityRegistry(mcRPG);
        RegistryAccess.registryAccess().register(abilityRegistry);
        abilityRegistry.register(new RageSpike(mcRPG));
    }

    @Test
    @DisplayName("getAbility returns RageSpike instance")
    void getAbility_returnsRageSpikeInstance() {
        AbilityHolder holder = mock(AbilityHolder.class);
        RageSpikeActivateEvent event = new RageSpikeActivateEvent(holder);
        assertInstanceOf(RageSpike.class, event.getAbility());
    }

    @Test
    @DisplayName("Event is not cancelled by default")
    void isCancelled_returnsFalse_byDefault() {
        AbilityHolder holder = mock(AbilityHolder.class);
        RageSpikeActivateEvent event = new RageSpikeActivateEvent(holder);
        assertFalse(event.isCancelled());
    }

    @Test
    @DisplayName("setCancelled(true) makes event cancelled")
    void setCancelled_makesEventCancelled() {
        AbilityHolder holder = mock(AbilityHolder.class);
        RageSpikeActivateEvent event = new RageSpikeActivateEvent(holder);
        event.setCancelled(true);
        assertTrue(event.isCancelled());
    }

    @Test
    @DisplayName("setCancelled(false) reverts cancellation")
    void setCancelled_revertsCancellation() {
        AbilityHolder holder = mock(AbilityHolder.class);
        RageSpikeActivateEvent event = new RageSpikeActivateEvent(holder);
        event.setCancelled(true);
        event.setCancelled(false);
        assertFalse(event.isCancelled());
    }

    @Test
    @DisplayName("getAbilityHolder returns the holder passed at construction")
    void getAbilityHolder_returnsConstructorHolder() {
        AbilityHolder holder = mock(AbilityHolder.class);
        RageSpikeActivateEvent event = new RageSpikeActivateEvent(holder);
        assertSame(holder, event.getAbilityHolder());
    }
}
