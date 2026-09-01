package us.eunoians.mcrpg.combat.state;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class StateTypeWarningLogTest {

    private static final NamespacedKey KEY_A = new NamespacedKey("mcrpg", "state_a");
    private static final NamespacedKey KEY_B = new NamespacedKey("mcrpg", "state_b");

    private StateTypeWarningLog warningLog;
    private List<LogRecord> capturedRecords;

    @BeforeEach
    void setUp() {
        Logger logger = Logger.getLogger("StateTypeWarningLogTest");
        logger.setUseParentHandlers(false);
        for (Handler h : logger.getHandlers()) {
            logger.removeHandler(h);
        }
        capturedRecords = new ArrayList<>();
        logger.addHandler(new Handler() {
            @Override
            public void publish(LogRecord record) {
                capturedRecords.add(record);
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        });
        logger.setLevel(Level.ALL);
        warningLog = new StateTypeWarningLog(logger);
    }

    @Test
    @DisplayName("Given first call for a key, when warnOnce is called, then warning is logged")
    void warnOnce_logsWarning_onFirstCall() {
        warningLog.warnOnce(KEY_A, "something went wrong", null);

        assertEquals(1, capturedRecords.size());
        assertEquals("something went wrong", capturedRecords.getFirst().getMessage());
        assertEquals(Level.WARNING, capturedRecords.getFirst().getLevel());
    }

    @Test
    @DisplayName("Given same key called twice, when warnOnce is called, then warning is logged only once")
    void warnOnce_suppressesDuplicate_onSecondCall() {
        warningLog.warnOnce(KEY_A, "first warning", null);
        warningLog.warnOnce(KEY_A, "duplicate warning", null);

        assertEquals(1, capturedRecords.size());
        assertEquals("first warning", capturedRecords.getFirst().getMessage());
    }

    @Test
    @DisplayName("Given different keys, when warnOnce is called for each, then both warnings are logged")
    void warnOnce_logsBoth_forDifferentKeys() {
        warningLog.warnOnce(KEY_A, "warning A", null);
        warningLog.warnOnce(KEY_B, "warning B", null);

        assertEquals(2, capturedRecords.size());
        assertEquals("warning A", capturedRecords.get(0).getMessage());
        assertEquals("warning B", capturedRecords.get(1).getMessage());
    }

    @Test
    @DisplayName("Given a throwable cause, when warnOnce is called, then cause is attached to log record")
    void warnOnce_attachesCause_whenProvided() {
        RuntimeException cause = new RuntimeException("root cause");
        warningLog.warnOnce(KEY_A, "warning with cause", cause);

        assertEquals(1, capturedRecords.size());
        assertEquals(cause, capturedRecords.getFirst().getThrown());
    }

    @Test
    @DisplayName("Given null cause, when warnOnce is called, then log record has no thrown")
    void warnOnce_hasNoThrown_whenCauseIsNull() {
        warningLog.warnOnce(KEY_A, "warning without cause", null);

        assertEquals(1, capturedRecords.size());
        assertNull(capturedRecords.getFirst().getThrown());
    }
}
