package us.eunoians.mcrpg.event.ability.mining;

import com.diamonddagger590.mccore.configuration.ReloadableContentManager;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.Location;
import org.bukkit.World;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.impl.mining.RemoteTransfer;
import us.eunoians.mcrpg.configuration.FileManager;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.impl.mining.Mining;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RemoteTransferActivateEventTest extends McRPGBaseTest {

    @BeforeEach
    void setUp() {
        // Stub FileManager BEFORE any reference to RemoteTransfer triggers its static
        // ReloadableRemoteTransferMap initializer, which calls getFile(MINING_CONFIG)
        FileManager fileManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.FILE);
        YamlDocument mockDocument = mock(YamlDocument.class);
        when(fileManager.getFile(FileType.MINING_CONFIG)).thenReturn(mockDocument);
        Section mockSection = mock(Section.class);
        when(mockDocument.getSection(any(Route.class))).thenReturn(mockSection);
        when(mockSection.getRoutesAsStrings(false)).thenReturn(Set.of());

        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                .register(mock(ReloadableContentManager.class));

        AbilityRegistry abilityRegistry = new AbilityRegistry(mcRPG);
        RegistryAccess.registryAccess().register(abilityRegistry);

        RemoteTransfer mockRemoteTransfer = mock(RemoteTransfer.class);
        when(mockRemoteTransfer.getAbilityKey()).thenReturn(RemoteTransfer.REMOTE_TRANSFER_KEY);
        when(mockRemoteTransfer.getSkillKey()).thenReturn(Mining.MINING_KEY);
        when(mockRemoteTransfer.getReloadableContent()).thenReturn(Set.of());
        abilityRegistry.register(mockRemoteTransfer);
    }

    @Test
    @DisplayName("getRemoteTransferDestination returns the location passed at construction")
    void getRemoteTransferDestination_returnsConstructorLocation() {
        AbilityHolder holder = mock(AbilityHolder.class);
        World world = mock(World.class);
        Location destination = new Location(world, 10, 64, -20);
        RemoteTransferActivateEvent event = new RemoteTransferActivateEvent(holder, destination);
        assertSame(destination, event.getRemoteTransferDestination());
    }

    @Test
    @DisplayName("getRemoteTransferDestination preserves coordinates")
    void getRemoteTransferDestination_preservesCoordinates() {
        AbilityHolder holder = mock(AbilityHolder.class);
        World world = mock(World.class);
        Location destination = new Location(world, 100.5, 200.0, -300.25);
        RemoteTransferActivateEvent event = new RemoteTransferActivateEvent(holder, destination);
        Location returned = event.getRemoteTransferDestination();
        assertEquals(100.5, returned.getX());
        assertEquals(200.0, returned.getY());
        assertEquals(-300.25, returned.getZ());
    }

    @Test
    @DisplayName("getAbility returns RemoteTransfer instance")
    void getAbility_returnsRemoteTransferInstance() {
        AbilityHolder holder = mock(AbilityHolder.class);
        World world = mock(World.class);
        Location destination = new Location(world, 0, 0, 0);
        RemoteTransferActivateEvent event = new RemoteTransferActivateEvent(holder, destination);
        assertInstanceOf(RemoteTransfer.class, event.getAbility());
    }

    @Test
    @DisplayName("Event is not cancelled by default")
    void isCancelled_returnsFalse_byDefault() {
        AbilityHolder holder = mock(AbilityHolder.class);
        World world = mock(World.class);
        Location destination = new Location(world, 0, 0, 0);
        RemoteTransferActivateEvent event = new RemoteTransferActivateEvent(holder, destination);
        assertFalse(event.isCancelled());
    }

    @Test
    @DisplayName("setCancelled(true) makes event cancelled")
    void setCancelled_makesEventCancelled() {
        AbilityHolder holder = mock(AbilityHolder.class);
        World world = mock(World.class);
        Location destination = new Location(world, 0, 0, 0);
        RemoteTransferActivateEvent event = new RemoteTransferActivateEvent(holder, destination);
        event.setCancelled(true);
        assertTrue(event.isCancelled());
    }

    @Test
    @DisplayName("setCancelled(false) restores non-cancelled state")
    void setCancelled_restoresNonCancelledState() {
        AbilityHolder holder = mock(AbilityHolder.class);
        World world = mock(World.class);
        Location destination = new Location(world, 0, 0, 0);
        RemoteTransferActivateEvent event = new RemoteTransferActivateEvent(holder, destination);
        event.setCancelled(true);
        event.setCancelled(false);
        assertFalse(event.isCancelled());
    }

    @Test
    @DisplayName("getAbilityHolder returns the holder passed at construction")
    void getAbilityHolder_returnsConstructorHolder() {
        AbilityHolder holder = mock(AbilityHolder.class);
        World world = mock(World.class);
        Location destination = new Location(world, 0, 0, 0);
        RemoteTransferActivateEvent event = new RemoteTransferActivateEvent(holder, destination);
        assertSame(holder, event.getAbilityHolder());
    }
}
