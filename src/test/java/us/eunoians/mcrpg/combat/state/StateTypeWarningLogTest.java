package us.eunoians.mcrpg.combat.state;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.logging.Level;
import java.util.logging.Logger;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class StateTypeWarningLogTest extends McRPGBaseTest {

    private Logger mockLogger;
    private StateTypeWarningLog warningLog;

    @BeforeEach
    void setUp() {
        mockLogger = mock(Logger.class);
        warningLog = new StateTypeWarningLog(mockLogger);
    }

    @DisplayName("warnOnce logs a warning on the first call for a key")
    @Test
    void warnOnce_logsWarning_onFirstCall() {
        NamespacedKey key = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "test_state");
        RuntimeException cause = new RuntimeException("test cause");

        warningLog.warnOnce(key, "Something went wrong", cause);

        verify(mockLogger, times(1)).log(Level.WARNING, "Something went wrong", cause);
    }

    @DisplayName("warnOnce suppresses duplicate warnings for the same key")
    @Test
    void warnOnce_suppressesDuplicate_forSameKey() {
        NamespacedKey key = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "dup_state");

        warningLog.warnOnce(key, "First warning", null);
        warningLog.warnOnce(key, "Second warning", null);
        warningLog.warnOnce(key, "Third warning", null);

        verify(mockLogger, times(1)).log(Level.WARNING, "First warning", (Throwable) null);
        verifyNoMoreInteractions(mockLogger);
    }

    @DisplayName("warnOnce logs independently for different keys")
    @Test
    void warnOnce_logsIndependently_forDifferentKeys() {
        NamespacedKey keyA = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "state_a");
        NamespacedKey keyB = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "state_b");

        warningLog.warnOnce(keyA, "Warning A", null);
        warningLog.warnOnce(keyB, "Warning B", null);

        verify(mockLogger, times(1)).log(Level.WARNING, "Warning A", (Throwable) null);
        verify(mockLogger, times(1)).log(Level.WARNING, "Warning B", (Throwable) null);
    }

    @DisplayName("warnOnce logs warning with null cause")
    @Test
    void warnOnce_logsWithNullCause() {
        NamespacedKey key = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "null_cause_state");

        warningLog.warnOnce(key, "No cause", null);

        verify(mockLogger, times(1)).log(Level.WARNING, "No cause", (Throwable) null);
    }

    @DisplayName("warnOnce suppresses for same key even with different messages")
    @Test
    void warnOnce_suppressesForSameKey_evenWithDifferentMessages() {
        NamespacedKey key = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "same_key");

        warningLog.warnOnce(key, "Message 1", null);
        warningLog.warnOnce(key, "Message 2", new RuntimeException());

        verify(mockLogger, times(1)).log(Level.WARNING, "Message 1", (Throwable) null);
        verifyNoMoreInteractions(mockLogger);
    }

    @DisplayName("separate StateTypeWarningLog instances track keys independently")
    @Test
    void separateInstances_trackKeysIndependently() {
        Logger anotherLogger = mock(Logger.class);
        StateTypeWarningLog anotherLog = new StateTypeWarningLog(anotherLogger);
        NamespacedKey key = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "shared_key");

        warningLog.warnOnce(key, "From log 1", null);
        anotherLog.warnOnce(key, "From log 2", null);

        verify(mockLogger, times(1)).log(Level.WARNING, "From log 1", (Throwable) null);
        verify(anotherLogger, times(1)).log(Level.WARNING, "From log 2", (Throwable) null);
    }
}
