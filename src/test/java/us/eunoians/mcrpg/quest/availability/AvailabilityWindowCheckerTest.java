package us.eunoians.mcrpg.quest.availability;

import com.diamonddagger590.mccore.util.TimeProvider;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.chain.QuestChainDefinition;
import us.eunoians.mcrpg.quest.chain.QuestChainRegistry;
import us.eunoians.mcrpg.quest.chain.QuestChainStep;
import us.eunoians.mcrpg.quest.chain.availability.AvailabilityConfig;
import us.eunoians.mcrpg.quest.chain.availability.AvailabilityWindowDefinition;
import us.eunoians.mcrpg.quest.chain.availability.WindowBoundary;
import us.eunoians.mcrpg.quest.chain.availability.WindowClosePolicy;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.definition.QuestDefinitionRegistry;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("AvailabilityWindowChecker")
class AvailabilityWindowCheckerTest extends McRPGBaseTest {

    private static final ZoneId UTC = ZoneId.of("UTC");

    private QuestChainRegistry chainRegistry;
    private QuestDefinitionRegistry definitionRegistry;
    private AvailabilityWindowChecker checker;
    private TimeProvider timeProvider;

    @BeforeEach
    void setUp() {
        chainRegistry = McRPG.getInstance().registryAccess().registry(McRPGRegistryKey.QUEST_CHAIN);
        definitionRegistry = McRPG.getInstance().registryAccess().registry(McRPGRegistryKey.QUEST_DEFINITION);

        timeProvider = McRPG.getInstance().getTimeProvider();

        checker = new AvailabilityWindowChecker(mcRPG, 60);
    }

    @NotNull
    private NamespacedKey key(@NotNull String name) {
        return new NamespacedKey(mcRPG, name);
    }

    @NotNull
    private AvailabilityConfig availableWindowConfig(@NotNull ZonedDateTime now) {
        WindowBoundary from = new WindowBoundary.Fixed(
                LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth() - 1, 0, 0)
        );
        WindowBoundary until = new WindowBoundary.Fixed(
                LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth() + 1, 23, 59)
        );
        AvailabilityWindowDefinition window = new AvailabilityWindowDefinition("test-window", from, until);
        return new AvailabilityConfig(Map.of("test-window", window), UTC, WindowClosePolicy.EXPIRE_ACTIVE, null);
    }

    @NotNull
    private AvailabilityConfig unavailableWindowConfig(@NotNull ZonedDateTime now) {
        WindowBoundary from = new WindowBoundary.Fixed(
                LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth() + 2, 0, 0)
        );
        WindowBoundary until = new WindowBoundary.Fixed(
                LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth() + 3, 23, 59)
        );
        AvailabilityWindowDefinition window = new AvailabilityWindowDefinition("closed-window", from, until);
        return new AvailabilityConfig(Map.of("closed-window", window), UTC, WindowClosePolicy.EXPIRE_ACTIVE, null);
    }

    @NotNull
    private QuestChainDefinition buildChain(@NotNull NamespacedKey chainKey, @NotNull AvailabilityConfig config) {
        NamespacedKey sourceKey = key("test-source");
        NamespacedKey triggerKey = key("test-trigger");
        NamespacedKey questKey = key("quest-" + chainKey.getKey());
        return new QuestChainDefinition.Builder(chainKey, sourceKey, triggerKey, List.of(QuestChainStep.simple(questKey)))
                .availabilityConfig(config)
                .build();
    }

    @NotNull
    private QuestChainDefinition buildChainNoAvailability(@NotNull NamespacedKey chainKey) {
        NamespacedKey sourceKey = key("test-source");
        NamespacedKey triggerKey = key("test-trigger");
        NamespacedKey questKey = key("quest-" + chainKey.getKey());
        return new QuestChainDefinition.Builder(chainKey, sourceKey, triggerKey, List.of(QuestChainStep.simple(questKey)))
                .build();
    }

    @Nested
    @DisplayName("isChainAvailable")
    class IsChainAvailable {

        @Test
        @DisplayName("Given an unregistered chain key, returns false")
        void isChainAvailable_returnsFalse_whenChainNotRegistered() {
            NamespacedKey unknownKey = key("nonexistent-chain");
            when(timeProvider.now()).thenReturn(Instant.now());

            assertFalse(checker.isChainAvailable(unknownKey));
        }

        @Test
        @DisplayName("Given a registered chain with no availability config, returns true")
        void isChainAvailable_returnsTrue_whenNoAvailabilityConfig() {
            NamespacedKey chainKey = key("no-config-chain");
            QuestChainDefinition definition = buildChainNoAvailability(chainKey);
            chainRegistry.register(definition);

            when(timeProvider.now()).thenReturn(Instant.now());

            assertTrue(checker.isChainAvailable(chainKey));
        }

        @Test
        @DisplayName("Given a registered chain within its availability window, returns true")
        void isChainAvailable_returnsTrue_whenWithinWindow() {
            ZonedDateTime now = ZonedDateTime.of(
                    LocalDateTime.of(2026, Month.JUNE, 15, 12, 0),
                    UTC
            );
            when(timeProvider.now()).thenReturn(now.toInstant());

            NamespacedKey chainKey = key("available-chain");
            AvailabilityConfig config = availableWindowConfig(now);
            QuestChainDefinition definition = buildChain(chainKey, config);
            chainRegistry.register(definition);

            assertTrue(checker.isChainAvailable(chainKey));
        }

        @Test
        @DisplayName("Given a registered chain outside its availability window, returns false")
        void isChainAvailable_returnsFalse_whenOutsideWindow() {
            ZonedDateTime now = ZonedDateTime.of(
                    LocalDateTime.of(2026, Month.JUNE, 15, 12, 0),
                    UTC
            );
            when(timeProvider.now()).thenReturn(now.toInstant());

            NamespacedKey chainKey = key("unavailable-chain");
            AvailabilityConfig config = unavailableWindowConfig(now);
            QuestChainDefinition definition = buildChain(chainKey, config);
            chainRegistry.register(definition);

            assertFalse(checker.isChainAvailable(chainKey));
        }
    }

    @Nested
    @DisplayName("isQuestAvailable")
    class IsQuestAvailable {

        @Test
        @DisplayName("Given an unregistered quest key, returns false")
        void isQuestAvailable_returnsFalse_whenQuestNotRegistered() {
            NamespacedKey unknownKey = key("nonexistent-quest");

            assertFalse(checker.isQuestAvailable(unknownKey));
        }

        @Test
        @DisplayName("Given a registered quest definition, returns true")
        void isQuestAvailable_returnsTrue_whenQuestRegistered() {
            NamespacedKey questKey = key("registered-quest");
            QuestDefinition definition = mock(QuestDefinition.class);
            when(definition.getQuestKey()).thenReturn(questKey);
            definitionRegistry.register(definition);

            assertTrue(checker.isQuestAvailable(questKey));
        }
    }

    @Nested
    @DisplayName("Constructor")
    class ConstructorTests {

        @Test
        @DisplayName("Constructs without error for valid interval")
        void constructor_succeeds_withValidInterval() {
            AvailabilityWindowChecker newChecker = new AvailabilityWindowChecker(mcRPG, 30);
            assertSame(mcRPG, newChecker.getPlugin());
        }
    }

    @Nested
    @DisplayName("getPlugin")
    class GetPlugin {

        @Test
        @DisplayName("Returns the McRPG plugin instance")
        void getPlugin_returnsMcRPG() {
            assertSame(mcRPG, checker.getPlugin());
        }
    }
}
