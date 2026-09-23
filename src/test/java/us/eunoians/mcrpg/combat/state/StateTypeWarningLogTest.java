package us.eunoians.mcrpg.combat.state;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;

import java.util.logging.Level;
import java.util.logging.Logger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class StateTypeWarningLogTest extends McRPGBaseTest {

    private Logger logger;
    private StateTypeWarningLog warningLog;

    @BeforeEach
    void setup() {
        logger = mock(Logger.class);
        warningLog = new StateTypeWarningLog(logger);
    }

    @DisplayName("First call to warnOnce logs the warning")
    @Test
    void warnOnce_firstCall_logsWarning() {
        NamespacedKey key = new NamespacedKey(mcRPG, "test_state");
        RuntimeException cause = new RuntimeException("test error");

        warningLog.warnOnce(key, "Something went wrong", cause);

        verify(logger).log(Level.WARNING, "Something went wrong", cause);
    }

    @DisplayName("Second call to warnOnce with the same key does not log")
    @Test
    void warnOnce_sameKeyTwice_logsOnlyOnce() {
        NamespacedKey key = new NamespacedKey(mcRPG, "duplicate_state");
        RuntimeException cause = new RuntimeException("first");

        warningLog.warnOnce(key, "First warning", cause);
        warningLog.warnOnce(key, "Second warning", new RuntimeException("second"));

        verify(logger).log(Level.WARNING, "First warning", cause);
        verify(logger, never()).log(eq(Level.WARNING), eq("Second warning"), any(Throwable.class));
    }

    @DisplayName("Calls with different keys each log independently")
    @Test
    void warnOnce_differentKeys_logBoth() {
        NamespacedKey key1 = new NamespacedKey(mcRPG, "state_one");
        NamespacedKey key2 = new NamespacedKey(mcRPG, "state_two");

        warningLog.warnOnce(key1, "Warning one", null);
        warningLog.warnOnce(key2, "Warning two", null);

        verify(logger).log(Level.WARNING, "Warning one", (Throwable) null);
        verify(logger).log(Level.WARNING, "Warning two", (Throwable) null);
    }

    @DisplayName("Null cause is passed to the logger without NPE")
    @Test
    void warnOnce_nullCause_logsWithoutError() {
        NamespacedKey key = new NamespacedKey(mcRPG, "null_cause_state");

        warningLog.warnOnce(key, "No cause", null);

        verify(logger).log(Level.WARNING, "No cause", (Throwable) null);
    }

    @DisplayName("Non-null cause is passed to the logger")
    @Test
    void warnOnce_nonNullCause_passedToLogger() {
        NamespacedKey key = new NamespacedKey(mcRPG, "cause_state");
        IllegalArgumentException cause = new IllegalArgumentException("bad value");

        warningLog.warnOnce(key, "Bad resolver", cause);

        verify(logger).log(Level.WARNING, "Bad resolver", cause);
    }
}
