package us.eunoians.mcrpg.combat.state;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;

import java.util.logging.Level;
import java.util.logging.Logger;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@DisplayName("StateTypeWarningLog")
class StateTypeWarningLogTest extends McRPGBaseTest {

    private Logger mockLogger;
    private StateTypeWarningLog warningLog;

    @BeforeEach
    void setUp() {
        mockLogger = mock(Logger.class);
        warningLog = new StateTypeWarningLog(mockLogger);
    }

    @Nested
    @DisplayName("warnOnce")
    class WarnOnce {

        @Test
        @DisplayName("logs warning on first call for a key")
        @SuppressWarnings("deprecation")
        void warnOnce_firstCall_logsWarning() {
            NamespacedKey key = new NamespacedKey("mcrpg", "test_state");
            String message = "resolver threw for test_state";

            warningLog.warnOnce(key, message, null);

            verify(mockLogger).log(Level.WARNING, message, (Throwable) null);
        }

        @Test
        @DisplayName("does not log on second call for the same key")
        @SuppressWarnings("deprecation")
        void warnOnce_secondCallSameKey_doesNotLog() {
            NamespacedKey key = new NamespacedKey("mcrpg", "test_state");

            warningLog.warnOnce(key, "first warning", null);
            warningLog.warnOnce(key, "second warning", null);

            verify(mockLogger).log(Level.WARNING, "first warning", (Throwable) null);
            verify(mockLogger, never()).log(Level.WARNING, "second warning", (Throwable) null);
        }

        @Test
        @DisplayName("logs warning for each distinct key")
        @SuppressWarnings("deprecation")
        void warnOnce_differentKeys_logsBoth() {
            NamespacedKey key1 = new NamespacedKey("mcrpg", "state_a");
            NamespacedKey key2 = new NamespacedKey("mcrpg", "state_b");

            warningLog.warnOnce(key1, "warning for A", null);
            warningLog.warnOnce(key2, "warning for B", null);

            verify(mockLogger).log(Level.WARNING, "warning for A", (Throwable) null);
            verify(mockLogger).log(Level.WARNING, "warning for B", (Throwable) null);
        }

        @Test
        @DisplayName("passes throwable cause to the logger")
        @SuppressWarnings("deprecation")
        void warnOnce_withCause_passesCauseToLogger() {
            NamespacedKey key = new NamespacedKey("mcrpg", "broken_resolver");
            RuntimeException cause = new RuntimeException("resolver failed");

            warningLog.warnOnce(key, "resolver threw", cause);

            verify(mockLogger).log(Level.WARNING, "resolver threw", cause);
        }

        @Test
        @DisplayName("suppresses duplicate even when cause differs")
        @SuppressWarnings("deprecation")
        void warnOnce_sameKeyDifferentCause_suppresses() {
            NamespacedKey key = new NamespacedKey("mcrpg", "same_key");
            RuntimeException cause1 = new RuntimeException("first");
            RuntimeException cause2 = new RuntimeException("second");

            warningLog.warnOnce(key, "warning", cause1);
            warningLog.warnOnce(key, "warning", cause2);

            verify(mockLogger).log(Level.WARNING, "warning", cause1);
            verify(mockLogger, never()).log(Level.WARNING, "warning", cause2);
        }

        @Test
        @DisplayName("keys from different namespaces are treated as distinct")
        @SuppressWarnings("deprecation")
        void warnOnce_differentNamespaces_logsBoth() {
            NamespacedKey key1 = new NamespacedKey("mcrpg", "my_state");
            NamespacedKey key2 = new NamespacedKey("thirdparty", "my_state");

            warningLog.warnOnce(key1, "mcrpg warning", null);
            warningLog.warnOnce(key2, "thirdparty warning", null);

            verify(mockLogger).log(Level.WARNING, "mcrpg warning", (Throwable) null);
            verify(mockLogger).log(Level.WARNING, "thirdparty warning", (Throwable) null);
        }
    }
}
