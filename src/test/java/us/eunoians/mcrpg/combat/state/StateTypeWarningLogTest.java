package us.eunoians.mcrpg.combat.state;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.logging.Level;
import java.util.logging.Logger;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@DisplayName("StateTypeWarningLog")
class StateTypeWarningLogTest {

    private Logger mockLogger;
    private StateTypeWarningLog warningLog;

    @BeforeEach
    void setUp() {
        mockLogger = mock(Logger.class);
        warningLog = new StateTypeWarningLog(mockLogger);
    }

    @Test
    @DisplayName("warnOnce logs the first warning for a given key")
    void warnOnce_logsFirstWarning() {
        NamespacedKey key = new NamespacedKey("mcrpg", "test_state");
        RuntimeException cause = new RuntimeException("boom");

        warningLog.warnOnce(key, "something broke", cause);

        verify(mockLogger).log(Level.WARNING, "something broke", cause);
    }

    @Test
    @DisplayName("warnOnce suppresses duplicate warnings for the same key")
    void warnOnce_suppressesDuplicate() {
        NamespacedKey key = new NamespacedKey("mcrpg", "test_state");
        RuntimeException cause = new RuntimeException("boom");

        warningLog.warnOnce(key, "something broke", cause);
        warningLog.warnOnce(key, "something broke again", cause);

        verify(mockLogger, times(1)).log(Level.WARNING, "something broke", cause);
    }

    @Test
    @DisplayName("warnOnce allows warnings for different keys independently")
    void warnOnce_differentKeysAreIndependent() {
        NamespacedKey keyA = new NamespacedKey("mcrpg", "state_a");
        NamespacedKey keyB = new NamespacedKey("mcrpg", "state_b");
        RuntimeException causeA = new RuntimeException("a broke");
        RuntimeException causeB = new RuntimeException("b broke");

        warningLog.warnOnce(keyA, "A failed", causeA);
        warningLog.warnOnce(keyB, "B failed", causeB);

        verify(mockLogger).log(Level.WARNING, "A failed", causeA);
        verify(mockLogger).log(Level.WARNING, "B failed", causeB);
    }

    @Test
    @DisplayName("warnOnce accepts a null cause")
    void warnOnce_acceptsNullCause() {
        NamespacedKey key = new NamespacedKey("mcrpg", "null_cause");

        warningLog.warnOnce(key, "no cause", null);

        verify(mockLogger).log(Level.WARNING, "no cause", (Throwable) null);
    }

    @Test
    @DisplayName("no warnings are logged when warnOnce is never called")
    void noWarnings_whenNeverCalled() {
        verifyNoInteractions(mockLogger);
    }
}
