package us.eunoians.mcrpg.event.ability;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.ability.impl.type.CooldownableAbility;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link AbilityPutOnCooldownEvent}.
 */
class AbilityPutOnCooldownEventTest extends McRPGBaseTest {

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("Given a positive cooldown, When constructed, Then getCooldown returns that value")
        void getCooldown_returnsValue_whenConstructedWithPositiveCooldown() {
            AbilityHolder holder = mock(AbilityHolder.class);
            CooldownableAbility ability = mock(CooldownableAbility.class);
            AbilityPutOnCooldownEvent event = new AbilityPutOnCooldownEvent(holder, ability, 10);
            assertEquals(10, event.getCooldown());
        }

        @Test
        @DisplayName("Given a negative cooldown, When constructed, Then getCooldown returns 0")
        void getCooldown_returnsZero_whenConstructedWithNegativeCooldown() {
            AbilityHolder holder = mock(AbilityHolder.class);
            CooldownableAbility ability = mock(CooldownableAbility.class);
            AbilityPutOnCooldownEvent event = new AbilityPutOnCooldownEvent(holder, ability, -5);
            assertEquals(0, event.getCooldown());
        }

        @Test
        @DisplayName("Given zero cooldown, When constructed, Then getCooldown returns 0")
        void getCooldown_returnsZero_whenConstructedWithZeroCooldown() {
            AbilityHolder holder = mock(AbilityHolder.class);
            CooldownableAbility ability = mock(CooldownableAbility.class);
            AbilityPutOnCooldownEvent event = new AbilityPutOnCooldownEvent(holder, ability, 0);
            assertEquals(0, event.getCooldown());
        }

        @Test
        @DisplayName("Given a large cooldown, When constructed, Then getCooldown preserves large value")
        void getCooldown_preservesLargeValue_whenConstructedWithLargeCooldown() {
            AbilityHolder holder = mock(AbilityHolder.class);
            CooldownableAbility ability = mock(CooldownableAbility.class);
            AbilityPutOnCooldownEvent event = new AbilityPutOnCooldownEvent(holder, ability, Long.MAX_VALUE);
            assertEquals(Long.MAX_VALUE, event.getCooldown());
        }
    }

    @Nested
    @DisplayName("setCooldown")
    class SetCooldown {

        @Test
        @DisplayName("Given a positive value, When setCooldown is called, Then getCooldown returns the new value")
        void setCooldown_updatesValue_whenGivenPositiveValue() {
            AbilityHolder holder = mock(AbilityHolder.class);
            CooldownableAbility ability = mock(CooldownableAbility.class);
            AbilityPutOnCooldownEvent event = new AbilityPutOnCooldownEvent(holder, ability, 10);
            event.setCooldown(20);
            assertEquals(20, event.getCooldown());
        }

        @Test
        @DisplayName("Given a negative value, When setCooldown is called, Then getCooldown returns 0")
        void setCooldown_clampsToZero_whenGivenNegativeValue() {
            AbilityHolder holder = mock(AbilityHolder.class);
            CooldownableAbility ability = mock(CooldownableAbility.class);
            AbilityPutOnCooldownEvent event = new AbilityPutOnCooldownEvent(holder, ability, 10);
            event.setCooldown(-3);
            assertEquals(0, event.getCooldown());
        }

        @Test
        @DisplayName("Given zero, When setCooldown is called, Then getCooldown returns 0")
        void setCooldown_returnsZero_whenGivenZero() {
            AbilityHolder holder = mock(AbilityHolder.class);
            CooldownableAbility ability = mock(CooldownableAbility.class);
            AbilityPutOnCooldownEvent event = new AbilityPutOnCooldownEvent(holder, ability, 10);
            event.setCooldown(0);
            assertEquals(0, event.getCooldown());
        }
    }

    @Nested
    @DisplayName("Getters")
    class Getters {

        @Test
        @DisplayName("getAbilityHolder returns the holder passed at construction")
        void getAbilityHolder_returnsConstructorHolder() {
            AbilityHolder holder = mock(AbilityHolder.class);
            CooldownableAbility ability = mock(CooldownableAbility.class);
            AbilityPutOnCooldownEvent event = new AbilityPutOnCooldownEvent(holder, ability, 5);
            assertSame(holder, event.getAbilityHolder());
        }

        @Test
        @DisplayName("getAbility returns a CooldownableAbility")
        void getAbility_returnsCooldownableAbility() {
            AbilityHolder holder = mock(AbilityHolder.class);
            CooldownableAbility ability = mock(CooldownableAbility.class);
            AbilityPutOnCooldownEvent event = new AbilityPutOnCooldownEvent(holder, ability, 5);
            assertSame(ability, event.getAbility());
        }

        @Test
        @DisplayName("getHandlers returns the same list as getHandlerList")
        void getHandlers_matchesStaticHandlerList() {
            AbilityHolder holder = mock(AbilityHolder.class);
            CooldownableAbility ability = mock(CooldownableAbility.class);
            AbilityPutOnCooldownEvent event = new AbilityPutOnCooldownEvent(holder, ability, 5);
            assertEquals(AbilityPutOnCooldownEvent.getHandlerList(), event.getHandlers());
        }
    }
}
