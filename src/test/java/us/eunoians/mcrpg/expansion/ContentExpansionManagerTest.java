package us.eunoians.mcrpg.expansion;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.event.content.ContentPackRegisteredEvent;
import us.eunoians.mcrpg.exception.expansion.ContentPackFailedProcessingException;
import us.eunoians.mcrpg.expansion.content.McRPGContent;
import us.eunoians.mcrpg.expansion.content.McRPGContentPack;
import us.eunoians.mcrpg.expansion.handler.ContentPackProcessor;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockbukkit.mockbukkit.matcher.plugin.PluginManagerFiredEventClassMatcher.hasFiredEventInstance;

@DisplayName("ContentExpansionManager")
public class ContentExpansionManagerTest extends McRPGBaseTest {

    private ContentExpansionManager manager;

    @BeforeEach
    public void setup() {
        manager = new ContentExpansionManager(mcRPG);
    }

    @Nested
    @DisplayName("hasContentExpansion")
    class HasContentExpansion {

        @Test
        @DisplayName("returns false for unregistered key")
        public void hasContentExpansion_returnsFalse_whenNotRegistered() {
            NamespacedKey key = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "nonexistent");
            assertFalse(manager.hasContentExpansion(key));
        }

        @Test
        @DisplayName("returns true after registration")
        public void hasContentExpansion_returnsTrue_afterRegistration() {
            StubExpansion expansion = new StubExpansion(mcRPG, Set.of());
            manager.registerContentExpansion(expansion);
            assertTrue(manager.hasContentExpansion(expansion.getExpansionKey()));
        }
    }

    @Nested
    @DisplayName("getContentExpansion")
    class GetContentExpansion {

        @Test
        @DisplayName("returns empty for unregistered key")
        public void getContentExpansion_returnsEmpty_whenNotRegistered() {
            NamespacedKey key = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "missing");
            assertTrue(manager.getContentExpansion(key).isEmpty());
        }

        @Test
        @DisplayName("returns expansion after registration")
        public void getContentExpansion_returnsExpansion_afterRegistration() {
            StubExpansion expansion = new StubExpansion(mcRPG, Set.of());
            manager.registerContentExpansion(expansion);

            Optional<ContentExpansion> result = manager.getContentExpansion(expansion.getExpansionKey());
            assertTrue(result.isPresent());
            assertEquals(expansion, result.orElseThrow());
        }
    }

    @Nested
    @DisplayName("registerContentHandler")
    class RegisterContentHandler {

        @Test
        @DisplayName("registered handler processes matching content packs")
        public void registerContentHandler_processesMatchingPacks() {
            AtomicInteger callCount = new AtomicInteger(0);
            ContentPackProcessor processor = (plugin, pack) -> {
                callCount.incrementAndGet();
                return true;
            };

            manager.registerContentHandler(processor);

            StubContentPack pack = new StubContentPack(null);
            StubExpansion expansion = new StubExpansion(mcRPG, Set.of(pack));
            manager.registerContentExpansion(expansion);

            assertEquals(1, callCount.get());
        }

        @Test
        @DisplayName("duplicate handler registration is idempotent")
        public void registerContentHandler_duplicateIsIdempotent() {
            AtomicInteger callCount = new AtomicInteger(0);
            ContentPackProcessor processor = (plugin, pack) -> {
                callCount.incrementAndGet();
                return true;
            };

            manager.registerContentHandler(processor);
            manager.registerContentHandler(processor);

            StubContentPack pack = new StubContentPack(null);
            StubExpansion expansion = new StubExpansion(mcRPG, Set.of(pack));
            manager.registerContentExpansion(expansion);

            assertEquals(1, callCount.get());
        }
    }

    @Nested
    @DisplayName("registerContentExpansion")
    class RegisterContentExpansion {

        @Test
        @DisplayName("processes all content packs in expansion")
        public void registerContentExpansion_processesAllPacks() {
            AtomicInteger callCount = new AtomicInteger(0);
            manager.registerContentHandler((plugin, pack) -> {
                callCount.incrementAndGet();
                return true;
            });

            StubContentPack pack1 = new StubContentPack(null);
            StubContentPack pack2 = new StubContentPack(null);
            StubExpansion expansion = new StubExpansion(mcRPG, Set.of(pack1, pack2));
            manager.registerContentExpansion(expansion);

            assertEquals(2, callCount.get());
        }

        @Test
        @DisplayName("fires ContentPackRegisteredEvent on successful processing")
        public void registerContentExpansion_firesEvent_onSuccess() {
            manager.registerContentHandler((plugin, pack) -> true);

            StubContentPack pack = new StubContentPack(null);
            StubExpansion expansion = new StubExpansion(mcRPG, Set.of(pack));

            server.getPluginManager().clearEvents();
            manager.registerContentExpansion(expansion);

            assertThat(server.getPluginManager(), hasFiredEventInstance(ContentPackRegisteredEvent.class));
        }

        @Test
        @DisplayName("throws ContentPackFailedProcessingException when no handler matches")
        public void registerContentExpansion_throwsException_whenNoHandlerMatches() {
            StubContentPack pack = new StubContentPack(null);
            StubExpansion expansion = new StubExpansion(mcRPG, Set.of(pack));

            assertThrows(ContentPackFailedProcessingException.class,
                    () -> manager.registerContentExpansion(expansion));
        }

        @Test
        @DisplayName("throws when all handlers return false")
        public void registerContentExpansion_throwsException_whenAllHandlersReturnFalse() {
            manager.registerContentHandler((plugin, pack) -> false);

            StubContentPack pack = new StubContentPack(null);
            StubExpansion expansion = new StubExpansion(mcRPG, Set.of(pack));

            assertThrows(ContentPackFailedProcessingException.class,
                    () -> manager.registerContentExpansion(expansion));
        }

        @Test
        @DisplayName("expansion with empty content set registers without processing")
        public void registerContentExpansion_emptyContentSet_registersSuccessfully() {
            StubExpansion expansion = new StubExpansion(mcRPG, Set.of());
            manager.registerContentExpansion(expansion);

            assertTrue(manager.hasContentExpansion(expansion.getExpansionKey()));
        }

        @Test
        @DisplayName("first successful handler short-circuits event to single fire")
        public void registerContentExpansion_firstSuccessfulHandler_firesEventOnce() {
            AtomicInteger firstCount = new AtomicInteger(0);
            AtomicInteger secondCount = new AtomicInteger(0);

            manager.registerContentHandler((plugin, pack) -> {
                firstCount.incrementAndGet();
                return true;
            });
            manager.registerContentHandler((plugin, pack) -> {
                secondCount.incrementAndGet();
                return true;
            });

            StubContentPack pack = new StubContentPack(null);
            StubExpansion expansion = new StubExpansion(mcRPG, Set.of(pack));

            server.getPluginManager().clearEvents();
            manager.registerContentExpansion(expansion);

            assertThat(server.getPluginManager(), hasFiredEventInstance(ContentPackRegisteredEvent.class));
        }
    }

    private static class StubContent implements McRPGContent {
        @Override
        public Optional<NamespacedKey> getExpansionKey() {
            return Optional.empty();
        }
    }

    private static class StubContentPack extends McRPGContentPack<StubContent> {
        public StubContentPack(ContentExpansion expansion) {
            super(expansion);
        }
    }

    private static class StubExpansion extends ContentExpansion {

        private static int counter = 0;
        private final Set<McRPGContentPack<? extends McRPGContent>> packs;

        public StubExpansion(McRPG mcRPG, Set<McRPGContentPack<? extends McRPGContent>> packs) {
            super(new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "stub_expansion_" + counter++));
            this.packs = packs;
        }

        @Override
        public Set<McRPGContentPack<? extends McRPGContent>> getExpansionContent() {
            return packs;
        }

        @Override
        public String getExpansionName(McRPGPlayer player) {
            return "Stub Expansion";
        }
    }
}
