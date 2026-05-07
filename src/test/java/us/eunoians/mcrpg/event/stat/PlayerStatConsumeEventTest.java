package us.eunoians.mcrpg.event.stat;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

/**
 * Verifies the contract of {@link PlayerStatConsumeEvent}.
 */
class PlayerStatConsumeEventTest extends McRPGBaseTest {

    private static final NamespacedKey MANA_KEY = new NamespacedKey("test", "mana");
    private static final double REQUESTED_AMOUNT = 25.0;

    private AbilityHolder mockHolder;
    private PlayerStatConsumeEvent event;

    @BeforeEach
    void setUp() {
        mockHolder = mock(AbilityHolder.class);
        event = new PlayerStatConsumeEvent(mockHolder, MANA_KEY, REQUESTED_AMOUNT);
    }

    @DisplayName("Initial effectiveAmount equals requestedAmount")
    @Test
    void initialEffectiveAmountEqualsRequested() {
        assertEquals(REQUESTED_AMOUNT, event.getEffectiveAmount());
    }

    @DisplayName("getRequestedAmount is immutable after setEffectiveAmount")
    @Test
    void requestedAmountIsImmutable() {
        event.setEffectiveAmount(10.0);
        assertEquals(REQUESTED_AMOUNT, event.getRequestedAmount());
    }

    @DisplayName("setEffectiveAmount(0) succeeds — free cast")
    @Test
    void setEffectiveAmountZeroSucceeds() {
        event.setEffectiveAmount(0.0);
        assertEquals(0.0, event.getEffectiveAmount());
    }

    @DisplayName("setEffectiveAmount(-1) throws IllegalArgumentException")
    @Test
    void setEffectiveAmountNegativeThrows() {
        assertThrows(IllegalArgumentException.class, () -> event.setEffectiveAmount(-1.0));
    }

    @DisplayName("setEffectiveAmount with a positive value updates effectiveAmount")
    @Test
    void setEffectiveAmountPositiveUpdates() {
        event.setEffectiveAmount(50.0);
        assertEquals(50.0, event.getEffectiveAmount());
    }
}
