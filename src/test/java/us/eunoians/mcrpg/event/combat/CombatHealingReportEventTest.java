package us.eunoians.mcrpg.event.combat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("CombatHealingReportEvent")
class CombatHealingReportEventTest extends McRPGBaseTest {

    @Nested
    @DisplayName("Constructor and getters")
    class ConstructorAndGetters {

        @Test
        @DisplayName("constructor stores healer UUID")
        void constructor_storesHealerUUID() {
            UUID healer = UUID.randomUUID();
            UUID target = UUID.randomUUID();
            var event = new CombatHealingReportEvent(healer, target, 5.0);

            assertEquals(healer, event.getHealerUUID());
        }

        @Test
        @DisplayName("constructor stores target UUID")
        void constructor_storesTargetUUID() {
            UUID healer = UUID.randomUUID();
            UUID target = UUID.randomUUID();
            var event = new CombatHealingReportEvent(healer, target, 5.0);

            assertEquals(target, event.getTargetUUID());
        }

        @Test
        @DisplayName("constructor stores amount")
        void constructor_storesAmount() {
            UUID healer = UUID.randomUUID();
            UUID target = UUID.randomUUID();
            var event = new CombatHealingReportEvent(healer, target, 7.5);

            assertEquals(7.5, event.getAmount());
        }

        @Test
        @DisplayName("constructor accepts zero amount")
        void constructor_acceptsZeroAmount() {
            var event = new CombatHealingReportEvent(UUID.randomUUID(), UUID.randomUUID(), 0.0);

            assertEquals(0.0, event.getAmount());
        }

        @Test
        @DisplayName("constructor accepts negative amount")
        void constructor_acceptsNegativeAmount() {
            var event = new CombatHealingReportEvent(UUID.randomUUID(), UUID.randomUUID(), -3.0);

            assertEquals(-3.0, event.getAmount());
        }
    }

    @Nested
    @DisplayName("Mutable amount")
    class MutableAmount {

        @Test
        @DisplayName("setAmount updates the healing amount")
        void setAmount_updatesAmount() {
            var event = new CombatHealingReportEvent(UUID.randomUUID(), UUID.randomUUID(), 5.0);

            event.setAmount(10.0);

            assertEquals(10.0, event.getAmount());
        }

        @Test
        @DisplayName("setAmount to zero clears the amount")
        void setAmount_toZero() {
            var event = new CombatHealingReportEvent(UUID.randomUUID(), UUID.randomUUID(), 5.0);

            event.setAmount(0.0);

            assertEquals(0.0, event.getAmount());
        }

        @Test
        @DisplayName("setAmount accepts negative values for scaling down")
        void setAmount_acceptsNegative() {
            var event = new CombatHealingReportEvent(UUID.randomUUID(), UUID.randomUUID(), 5.0);

            event.setAmount(-2.0);

            assertEquals(-2.0, event.getAmount());
        }

        @Test
        @DisplayName("setAmount can be called multiple times")
        void setAmount_canBeCalledMultipleTimes() {
            var event = new CombatHealingReportEvent(UUID.randomUUID(), UUID.randomUUID(), 5.0);

            event.setAmount(10.0);
            event.setAmount(20.0);

            assertEquals(20.0, event.getAmount());
        }
    }

    @Nested
    @DisplayName("Cancellable contract")
    class CancellableContract {

        @Test
        @DisplayName("isCancelled defaults to false")
        void isCancelled_defaultsFalse() {
            var event = new CombatHealingReportEvent(UUID.randomUUID(), UUID.randomUUID(), 5.0);

            assertFalse(event.isCancelled());
        }

        @Test
        @DisplayName("setCancelled(true) makes isCancelled return true")
        void setCancelled_true() {
            var event = new CombatHealingReportEvent(UUID.randomUUID(), UUID.randomUUID(), 5.0);

            event.setCancelled(true);

            assertTrue(event.isCancelled());
        }

        @Test
        @DisplayName("setCancelled can revert to false")
        void setCancelled_canRevert() {
            var event = new CombatHealingReportEvent(UUID.randomUUID(), UUID.randomUUID(), 5.0);

            event.setCancelled(true);
            event.setCancelled(false);

            assertFalse(event.isCancelled());
        }
    }

    @Nested
    @DisplayName("HandlerList")
    class HandlerListTests {

        @Test
        @DisplayName("getHandlers returns non-null")
        void getHandlers_returnsNonNull() {
            var event = new CombatHealingReportEvent(UUID.randomUUID(), UUID.randomUUID(), 5.0);

            assertNotNull(event.getHandlers());
        }

        @Test
        @DisplayName("static getHandlerList returns non-null")
        void getHandlerList_returnsNonNull() {
            assertNotNull(CombatHealingReportEvent.getHandlerList());
        }

        @Test
        @DisplayName("instance and static handler lists are the same object")
        void handlerLists_areSameObject() {
            var event = new CombatHealingReportEvent(UUID.randomUUID(), UUID.randomUUID(), 5.0);

            assertNotNull(event.getHandlers());
            assertEquals(CombatHealingReportEvent.getHandlerList(), event.getHandlers());
        }
    }
}
