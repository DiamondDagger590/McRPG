package us.eunoians.mcrpg.quest.source;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.source.builtin.BoardPersonalQuestSource;
import us.eunoians.mcrpg.quest.source.builtin.ManualQuestSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class QuestSourceRegistryTest extends McRPGBaseTest {

    private QuestSourceRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new QuestSourceRegistry();
    }

    @DisplayName("register and get by key returns registered source")
    @Test
    void registerAndGetByKey() {
        var manual = new ManualQuestSource();
        registry.register(manual);

        var retrieved = registry.get(ManualQuestSource.KEY);
        assertTrue(retrieved.isPresent());
        assertSame(manual, retrieved.get());
    }

    @DisplayName("unregistered key returns empty Optional")
    @Test
    void unregisteredKeyReturnsEmpty() {
        var result = registry.get(new NamespacedKey("mcrpg", "nonexistent"));
        assertTrue(result.isEmpty());
    }

    @DisplayName("duplicate registration throws IllegalStateException")
    @Test
    void duplicateRegistrationThrows() {
        registry.register(new ManualQuestSource());
        assertThrows(IllegalStateException.class, () -> registry.register(new ManualQuestSource()));
    }

    @DisplayName("getAll returns all registered sources")
    @Test
    void getAllReturnsAllRegistered() {
        var manual = new ManualQuestSource();
        var board = new BoardPersonalQuestSource();
        registry.register(manual);
        registry.register(board);

        var all = registry.getAll();
        assertTrue(all.size() == 2);
        assertTrue(all.contains(manual));
        assertTrue(all.contains(board));
    }

    @DisplayName("registered returns true for registered source, false for unregistered")
    @Test
    void registeredReturnsCorrectly() {
        var manual = new ManualQuestSource();
        var board = new BoardPersonalQuestSource();
        registry.register(manual);

        assertTrue(registry.registered(manual));
        assertFalse(registry.registered(board));
    }
}
