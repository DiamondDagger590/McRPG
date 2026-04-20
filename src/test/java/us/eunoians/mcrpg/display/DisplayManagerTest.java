package us.eunoians.mcrpg.display;

import com.diamonddagger590.mccore.configuration.ReloadableContentManager;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.configuration.FileManager;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.display.hud.ActionBarHudDisplay;
import us.eunoians.mcrpg.display.impl.PlayerDisplay;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.entity.player.McRPGPlayerExtension;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Exercises {@link DisplayManager}'s generic {@code get/has/set/remove/
 * getOrCreateDisplay} surface plus the HUD-specific
 * {@link DisplayManager#getOrCreateActionBarHud(McRPGPlayer)} helper.
 * <p>
 * The manager is a thin coordinator on top of
 * {@link McRPGPlayer}'s display container, so these tests pin down two
 * contracts downstream display callers depend on:
 * <ul>
 *     <li>Delegation — generic API calls are forwarded to the player's
 *         container without surprising side effects.</li>
 *     <li>Factory-idempotence — {@link DisplayManager#getOrCreateDisplay
 *         getOrCreateDisplay} invokes the factory exactly once per player,
 *         returning the same registered instance on subsequent calls.</li>
 * </ul>
 * <p>
 * {@link DisplayManager}'s constructor resolves its shared renderer + config
 * flag via the registry, so the {@link #setUp()} hook stubs the HUD config
 * file on the already-mocked {@link FileManager} and registers a real
 * {@link ReloadableContentManager}. The manager under test is constructed
 * directly rather than looked up via the registry — it isn't registered in
 * {@link us.eunoians.mcrpg.TestBootstrap} and we only want to exercise its
 * own delegation, not the plugin-wide wiring.
 */
@ExtendWith(McRPGPlayerExtension.class)
public class DisplayManagerTest extends McRPGBaseTest {

    /**
     * Minimal {@link PlayerDisplay} with a no-op cleanup; used purely as a
     * stand-in so the manager API contracts can be exercised without pulling
     * in a concrete McRPG display subclass.
     */
    private static final class NoopDisplay extends PlayerDisplay {
        NoopDisplay(McRPGPlayer player) {
            super(player);
        }

        @Override
        public void cleanDisplay() {
        }
    }

    private DisplayManager manager;

    @BeforeEach
    void setUp() {
        FileManager fileManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE);
        YamlDocument hudConfig = mock(YamlDocument.class);
        when(hudConfig.getBoolean(any(Route.class), anyBoolean())).thenReturn(true);
        when(fileManager.getFile(FileType.HUD_CONFIG)).thenReturn(hudConfig);

        ReloadableContentManager reloadableContentManager = new ReloadableContentManager(mcRPG);
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(reloadableContentManager);

        manager = new DisplayManager(mcRPG);
    }

    @Test
    @DisplayName("Given no display registered, when getOrCreateDisplay is called, then the factory is invoked exactly once and the result is registered")
    void getOrCreateDisplay_invokesFactoryOnce_whenNoDisplayRegistered(McRPGPlayer mcRPGPlayer) {
        AtomicInteger factoryCalls = new AtomicInteger();

        NoopDisplay created = manager.getOrCreateDisplay(mcRPGPlayer, NoopDisplay.class, player -> {
            factoryCalls.incrementAndGet();
            return new NoopDisplay(player);
        });

        assertNotNull(created);
        assertEquals(1, factoryCalls.get());
        assertTrue(manager.hasDisplay(mcRPGPlayer, NoopDisplay.class));
        assertSame(created, manager.getDisplay(mcRPGPlayer, NoopDisplay.class).orElseThrow());
    }

    @Test
    @DisplayName("Given an existing display, when getOrCreateDisplay is called a second time, then the factory is skipped and the existing instance is returned")
    void getOrCreateDisplay_skipsFactory_whenDisplayAlreadyRegistered(McRPGPlayer mcRPGPlayer) {
        NoopDisplay seeded = new NoopDisplay(mcRPGPlayer);
        manager.setDisplay(mcRPGPlayer, NoopDisplay.class, seeded);
        AtomicInteger factoryCalls = new AtomicInteger();

        NoopDisplay resolved = manager.getOrCreateDisplay(mcRPGPlayer, NoopDisplay.class, player -> {
            factoryCalls.incrementAndGet();
            return new NoopDisplay(player);
        });

        assertSame(seeded, resolved);
        assertEquals(0, factoryCalls.get(),
                "Factory must not run when a display of the requested type already exists");
    }

    @Test
    @DisplayName("Given a player with no action bar HUD, when getOrCreateActionBarHud is called, then a HUD wired to the shared renderer is created and registered")
    void getOrCreateActionBarHud_createsHudWithSharedRenderer_whenNoneExists(McRPGPlayer mcRPGPlayer) {
        ActionBarHudDisplay hud = manager.getOrCreateActionBarHud(mcRPGPlayer);

        assertNotNull(hud);
        assertSame(mcRPGPlayer, hud.getMcRPGPlayer());
        assertTrue(manager.hasDisplay(mcRPGPlayer, ActionBarHudDisplay.class));
        assertSame(hud, manager.getOrCreateActionBarHud(mcRPGPlayer),
                "Second call should return the already-registered HUD instance");
    }

    @Test
    @DisplayName("Given a registered display, when removeDisplay is called, then the display is cleared from the container")
    void removeDisplay_clearsContainer_whenDisplayRegistered(McRPGPlayer mcRPGPlayer) {
        manager.setDisplay(mcRPGPlayer, NoopDisplay.class, new NoopDisplay(mcRPGPlayer));

        manager.removeDisplay(mcRPGPlayer, NoopDisplay.class);

        assertFalse(manager.hasDisplay(mcRPGPlayer, NoopDisplay.class));
        assertEquals(Optional.empty(), manager.getDisplay(mcRPGPlayer, NoopDisplay.class));
    }
}
