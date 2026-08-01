package us.eunoians.mcrpg.combat.state;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.combat.CombatSession;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

@DisplayName("CombatStateResolver")
class CombatStateResolverTest extends McRPGBaseTest {

    @DisplayName("resolve returns the transformed value given session and raw input")
    @Test
    void resolve_returnsTransformedValue() {
        CombatStateResolver<Integer> doubler = (session, rawValue) -> rawValue * 2;
        CombatSession session = new CombatSession(UUID.randomUUID(), 16, 8000L);

        assertEquals(10, doubler.resolve(session, 5));
    }

    @DisplayName("resolve receives the exact raw value and session passed in")
    @Test
    void resolve_receivesExactArguments() {
        CombatSession session = new CombatSession(UUID.randomUUID(), 16, 8000L);
        CombatSession[] capturedSession = new CombatSession[1];
        Integer[] capturedRaw = new Integer[1];

        CombatStateResolver<Integer> capturing = (s, rawValue) -> {
            capturedSession[0] = s;
            capturedRaw[0] = rawValue;
            return rawValue;
        };

        capturing.resolve(session, 7);

        assertSame(session, capturedSession[0]);
        assertEquals(7, capturedRaw[0]);
    }
}
