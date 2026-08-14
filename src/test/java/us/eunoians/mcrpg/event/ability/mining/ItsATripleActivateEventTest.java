package us.eunoians.mcrpg.event.ability.mining;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.impl.mining.ItsATriple;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class ItsATripleActivateEventTest extends McRPGBaseTest {

    @BeforeEach
    void setUp() {
        AbilityRegistry abilityRegistry = new AbilityRegistry(mcRPG);
        RegistryAccess.registryAccess().register(abilityRegistry);
        abilityRegistry.register(new ItsATriple(mcRPG));
    }

    @Test
    @DisplayName("getAbility returns ItsATriple instance")
    void getAbility_returnsItsATripleInstance() {
        AbilityHolder holder = mock(AbilityHolder.class);
        ItsATripleActivateEvent event = new ItsATripleActivateEvent(holder);
        assertInstanceOf(ItsATriple.class, event.getAbility());
    }

    @Test
    @DisplayName("Event is not cancelled by default")
    void isCancelled_returnsFalse_byDefault() {
        AbilityHolder holder = mock(AbilityHolder.class);
        ItsATripleActivateEvent event = new ItsATripleActivateEvent(holder);
        assertFalse(event.isCancelled());
    }

    @Test
    @DisplayName("setCancelled(true) makes event cancelled")
    void setCancelled_makesEventCancelled() {
        AbilityHolder holder = mock(AbilityHolder.class);
        ItsATripleActivateEvent event = new ItsATripleActivateEvent(holder);
        event.setCancelled(true);
        assertTrue(event.isCancelled());
    }

    @Test
    @DisplayName("setCancelled(false) restores non-cancelled state")
    void setCancelled_restoresNonCancelledState() {
        AbilityHolder holder = mock(AbilityHolder.class);
        ItsATripleActivateEvent event = new ItsATripleActivateEvent(holder);
        event.setCancelled(true);
        event.setCancelled(false);
        assertFalse(event.isCancelled());
    }

    @Test
    @DisplayName("getAbilityHolder returns the holder passed at construction")
    void getAbilityHolder_returnsConstructorHolder() {
        AbilityHolder holder = mock(AbilityHolder.class);
        ItsATripleActivateEvent event = new ItsATripleActivateEvent(holder);
        assertSame(holder, event.getAbilityHolder());
    }
}
