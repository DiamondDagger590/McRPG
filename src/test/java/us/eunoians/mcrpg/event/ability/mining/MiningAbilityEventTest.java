package us.eunoians.mcrpg.event.ability.mining;

import com.diamonddagger590.mccore.configuration.ReloadableContentManager;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeRegistry;
import us.eunoians.mcrpg.ability.impl.mining.ExtraOre;
import us.eunoians.mcrpg.ability.impl.mining.ItsATriple;
import us.eunoians.mcrpg.ability.impl.mining.OreScanner;
import us.eunoians.mcrpg.ability.impl.mining.RemoteTransfer;
import us.eunoians.mcrpg.ability.impl.mining.orescanner.OreScannerBlockType;
import us.eunoians.mcrpg.configuration.FileManager;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.skill.MiningConfigFile;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for all mining ability activation events:
 * {@link ExtraOreActivateEvent}, {@link ItsATripleActivateEvent},
 * {@link OreScannerActivateEvent}, and {@link RemoteTransferActivateEvent}.
 */
@SuppressWarnings("deprecation")
class MiningAbilityEventTest extends McRPGBaseTest {

    private AbilityHolder holder;

    @BeforeEach
    void setUp() {
        AbilityAttributeRegistry abilityAttributeRegistry = new AbilityAttributeRegistry();
        RegistryAccess.registryAccess().register(abilityAttributeRegistry);

        ReloadableContentManager reloadableContentManager = new ReloadableContentManager(mcRPG);
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(reloadableContentManager);

        YamlDocument miningConfig = mock(YamlDocument.class);
        FileManager fileManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.FILE);
        when(fileManager.getFile(FileType.MINING_CONFIG)).thenReturn(miningConfig);

        when(miningConfig.getInt(MiningConfigFile.ITS_A_TRIPLE_AMOUNT_OF_TIERS)).thenReturn(5);
        when(miningConfig.getInt(MiningConfigFile.ORE_SCANNER_AMOUNT_OF_TIERS)).thenReturn(5);
        when(miningConfig.getInt(MiningConfigFile.REMOTE_TRANSFER_AMOUNT_OF_TIERS)).thenReturn(5);

        lenient().when(miningConfig.getStringList(any(Route.class))).thenReturn(List.of());

        Section emptySection = mock(Section.class);
        lenient().when(emptySection.getRoutesAsStrings(false)).thenReturn(new HashSet<>());
        lenient().when(miningConfig.getSection(any(Route.class))).thenReturn(emptySection);
        lenient().when(miningConfig.getSection(any(String.class))).thenReturn(emptySection);

        AbilityRegistry abilityRegistry = new AbilityRegistry(mcRPG);
        RegistryAccess.registryAccess().register(abilityRegistry);

        abilityRegistry.register(new ExtraOre(mcRPG));
        abilityRegistry.register(new ItsATriple(mcRPG));
        abilityRegistry.register(new OreScanner(mcRPG));
        abilityRegistry.register(new RemoteTransfer(mcRPG));

        holder = mock(AbilityHolder.class);
        when(holder.getUUID()).thenReturn(UUID.randomUUID());
    }

    @Nested
    @DisplayName("ExtraOreActivateEvent")
    class ExtraOreActivateEventTests {

        @Test
        @DisplayName("Negative drop multiplier clamped to 1 at construction")
        void getDropMultiplier_returnsOne_whenConstructedWithNegativeValue() {
            ExtraOreActivateEvent event = new ExtraOreActivateEvent(holder, -5);
            assertEquals(1, event.getDropMultiplier());
        }

        @Test
        @DisplayName("Zero drop multiplier clamped to 1 at construction")
        void getDropMultiplier_returnsOne_whenConstructedWithZero() {
            ExtraOreActivateEvent event = new ExtraOreActivateEvent(holder, 0);
            assertEquals(1, event.getDropMultiplier());
        }

        @Test
        @DisplayName("Positive drop multiplier preserved at construction")
        void getDropMultiplier_returnsValue_whenConstructedWithPositiveValue() {
            ExtraOreActivateEvent event = new ExtraOreActivateEvent(holder, 4);
            assertEquals(4, event.getDropMultiplier());
        }

        @Test
        @DisplayName("setDropMultiplier clamps negative to 1")
        void setDropMultiplier_clampsToOne_whenGivenNegativeValue() {
            ExtraOreActivateEvent event = new ExtraOreActivateEvent(holder, 3);
            event.setDropMultiplier(-2);
            assertEquals(1, event.getDropMultiplier());
        }

        @Test
        @DisplayName("setDropMultiplier clamps zero to 1")
        void setDropMultiplier_clampsToOne_whenGivenZero() {
            ExtraOreActivateEvent event = new ExtraOreActivateEvent(holder, 3);
            event.setDropMultiplier(0);
            assertEquals(1, event.getDropMultiplier());
        }

        @Test
        @DisplayName("setDropMultiplier preserves positive value")
        void setDropMultiplier_preservesValue_whenGivenPositiveValue() {
            ExtraOreActivateEvent event = new ExtraOreActivateEvent(holder, 2);
            event.setDropMultiplier(7);
            assertEquals(7, event.getDropMultiplier());
        }

        @Test
        @DisplayName("getAbility returns ExtraOre instance")
        void getAbility_returnsExtraOreInstance() {
            ExtraOreActivateEvent event = new ExtraOreActivateEvent(holder, 2);
            assertInstanceOf(ExtraOre.class, event.getAbility());
        }

        @Test
        @DisplayName("getAbilityHolder returns the holder passed at construction")
        void getAbilityHolder_returnsConstructorHolder() {
            ExtraOreActivateEvent event = new ExtraOreActivateEvent(holder, 2);
            assertSame(holder, event.getAbilityHolder());
        }

        @Test
        @DisplayName("Event is not cancelled by default")
        void isCancelled_returnsFalse_byDefault() {
            ExtraOreActivateEvent event = new ExtraOreActivateEvent(holder, 2);
            assertFalse(event.isCancelled());
        }

        @Test
        @DisplayName("setCancelled(true) makes event cancelled")
        void setCancelled_makesEventCancelled() {
            ExtraOreActivateEvent event = new ExtraOreActivateEvent(holder, 2);
            event.setCancelled(true);
            assertTrue(event.isCancelled());
        }

        @Test
        @DisplayName("setCancelled(false) after cancel restores uncancelled state")
        void setCancelled_restoresUncancelled_afterCancel() {
            ExtraOreActivateEvent event = new ExtraOreActivateEvent(holder, 2);
            event.setCancelled(true);
            event.setCancelled(false);
            assertFalse(event.isCancelled());
        }
    }

    @Nested
    @DisplayName("ItsATripleActivateEvent")
    class ItsATripleActivateEventTests {

        @Test
        @DisplayName("getAbility returns ItsATriple instance")
        void getAbility_returnsItsATripleInstance() {
            ItsATripleActivateEvent event = new ItsATripleActivateEvent(holder);
            assertInstanceOf(ItsATriple.class, event.getAbility());
        }

        @Test
        @DisplayName("getAbilityHolder returns the holder passed at construction")
        void getAbilityHolder_returnsConstructorHolder() {
            ItsATripleActivateEvent event = new ItsATripleActivateEvent(holder);
            assertSame(holder, event.getAbilityHolder());
        }

        @Test
        @DisplayName("Event is not cancelled by default")
        void isCancelled_returnsFalse_byDefault() {
            ItsATripleActivateEvent event = new ItsATripleActivateEvent(holder);
            assertFalse(event.isCancelled());
        }

        @Test
        @DisplayName("setCancelled(true) makes event cancelled")
        void setCancelled_makesEventCancelled() {
            ItsATripleActivateEvent event = new ItsATripleActivateEvent(holder);
            event.setCancelled(true);
            assertTrue(event.isCancelled());
        }

        @Test
        @DisplayName("setCancelled(false) after cancel restores uncancelled state")
        void setCancelled_restoresUncancelled_afterCancel() {
            ItsATripleActivateEvent event = new ItsATripleActivateEvent(holder);
            event.setCancelled(true);
            event.setCancelled(false);
            assertFalse(event.isCancelled());
        }
    }

    @Nested
    @DisplayName("OreScannerActivateEvent")
    class OreScannerActivateEventTests {

        @Test
        @DisplayName("getInstancesOfBlocks returns immutable copy")
        void getInstancesOfBlocks_returnsImmutableCopy() {
            OreScannerBlockType blockType = new OreScannerBlockType(
                    Set.of(Material.DIAMOND_ORE), "Diamond", ChatColor.AQUA, 10
            );
            World world = server.addSimpleWorld("test_world");
            Location loc = new Location(world, 10, 64, 10);
            Map<OreScannerBlockType, Set<Location>> blocks = new HashMap<>();
            blocks.put(blockType, Set.of(loc));

            OreScannerActivateEvent event = new OreScannerActivateEvent(holder, blocks);
            Map<OreScannerBlockType, Set<Location>> result = event.getInstancesOfBlocks();

            assertThrows(UnsupportedOperationException.class, () -> result.put(
                    new OreScannerBlockType(Set.of(Material.GOLD_ORE), "Gold", ChatColor.GOLD, 5),
                    Set.of()
            ));
        }

        @Test
        @DisplayName("getInstancesOfBlocks returns all entries")
        void getInstancesOfBlocks_returnsAllEntries() {
            OreScannerBlockType diamondType = new OreScannerBlockType(
                    Set.of(Material.DIAMOND_ORE), "Diamond", ChatColor.AQUA, 10
            );
            OreScannerBlockType goldType = new OreScannerBlockType(
                    Set.of(Material.GOLD_ORE), "Gold", ChatColor.GOLD, 5
            );
            World world = server.addSimpleWorld("test_world");
            Location loc1 = new Location(world, 10, 64, 10);
            Location loc2 = new Location(world, 20, 32, 20);

            Map<OreScannerBlockType, Set<Location>> blocks = new HashMap<>();
            blocks.put(diamondType, Set.of(loc1));
            blocks.put(goldType, Set.of(loc2));

            OreScannerActivateEvent event = new OreScannerActivateEvent(holder, blocks);
            assertEquals(2, event.getInstancesOfBlocks().size());
        }

        @Test
        @DisplayName("getInstancesOfBlocks returns empty map when constructed with empty map")
        void getInstancesOfBlocks_returnsEmpty_whenConstructedWithEmptyMap() {
            OreScannerActivateEvent event = new OreScannerActivateEvent(holder, Map.of());
            assertTrue(event.getInstancesOfBlocks().isEmpty());
        }

        @Test
        @DisplayName("getLocationsOfBlockType returns locations for a known block type")
        void getLocationsOfBlockType_returnsLocations_whenBlockTypePresent() {
            OreScannerBlockType blockType = new OreScannerBlockType(
                    Set.of(Material.DIAMOND_ORE), "Diamond", ChatColor.AQUA, 10
            );
            World world = server.addSimpleWorld("test_world");
            Location loc1 = new Location(world, 10, 64, 10);
            Location loc2 = new Location(world, 15, 60, 15);

            Map<OreScannerBlockType, Set<Location>> blocks = new HashMap<>();
            blocks.put(blockType, Set.of(loc1, loc2));

            OreScannerActivateEvent event = new OreScannerActivateEvent(holder, blocks);
            Set<Location> result = event.getLocationsOfBlockType(blockType);

            assertEquals(2, result.size());
            assertTrue(result.contains(loc1));
            assertTrue(result.contains(loc2));
        }

        @Test
        @DisplayName("getLocationsOfBlockType returns empty set for an unknown block type")
        void getLocationsOfBlockType_returnsEmpty_whenBlockTypeAbsent() {
            OreScannerBlockType knownType = new OreScannerBlockType(
                    Set.of(Material.DIAMOND_ORE), "Diamond", ChatColor.AQUA, 10
            );
            OreScannerBlockType unknownType = new OreScannerBlockType(
                    Set.of(Material.GOLD_ORE), "Gold", ChatColor.GOLD, 5
            );
            World world = server.addSimpleWorld("test_world");
            Location loc = new Location(world, 10, 64, 10);

            Map<OreScannerBlockType, Set<Location>> blocks = new HashMap<>();
            blocks.put(knownType, Set.of(loc));

            OreScannerActivateEvent event = new OreScannerActivateEvent(holder, blocks);
            assertTrue(event.getLocationsOfBlockType(unknownType).isEmpty());
        }

        @Test
        @DisplayName("getLocationsOfBlockType returns immutable set")
        void getLocationsOfBlockType_returnsImmutableSet() {
            OreScannerBlockType blockType = new OreScannerBlockType(
                    Set.of(Material.DIAMOND_ORE), "Diamond", ChatColor.AQUA, 10
            );
            World world = server.addSimpleWorld("test_world");
            Location loc = new Location(world, 10, 64, 10);

            Map<OreScannerBlockType, Set<Location>> blocks = new HashMap<>();
            blocks.put(blockType, Set.of(loc));

            OreScannerActivateEvent event = new OreScannerActivateEvent(holder, blocks);
            Set<Location> result = event.getLocationsOfBlockType(blockType);

            assertThrows(UnsupportedOperationException.class, () ->
                    result.add(new Location(world, 99, 99, 99)));
        }

        @Test
        @DisplayName("getAbility returns OreScanner instance")
        void getAbility_returnsOreScannerInstance() {
            OreScannerActivateEvent event = new OreScannerActivateEvent(holder, Map.of());
            assertInstanceOf(OreScanner.class, event.getAbility());
        }

        @Test
        @DisplayName("getAbilityHolder returns the holder passed at construction")
        void getAbilityHolder_returnsConstructorHolder() {
            OreScannerActivateEvent event = new OreScannerActivateEvent(holder, Map.of());
            assertSame(holder, event.getAbilityHolder());
        }

        @Test
        @DisplayName("Event is not cancelled by default")
        void isCancelled_returnsFalse_byDefault() {
            OreScannerActivateEvent event = new OreScannerActivateEvent(holder, Map.of());
            assertFalse(event.isCancelled());
        }

        @Test
        @DisplayName("setCancelled(true) makes event cancelled")
        void setCancelled_makesEventCancelled() {
            OreScannerActivateEvent event = new OreScannerActivateEvent(holder, Map.of());
            event.setCancelled(true);
            assertTrue(event.isCancelled());
        }

        @Test
        @DisplayName("setCancelled(false) after cancel restores uncancelled state")
        void setCancelled_restoresUncancelled_afterCancel() {
            OreScannerActivateEvent event = new OreScannerActivateEvent(holder, Map.of());
            event.setCancelled(true);
            event.setCancelled(false);
            assertFalse(event.isCancelled());
        }
    }

    @Nested
    @DisplayName("RemoteTransferActivateEvent")
    class RemoteTransferActivateEventTests {

        @Test
        @DisplayName("getRemoteTransferDestination returns the location passed at construction")
        void getRemoteTransferDestination_returnsConstructorLocation() {
            World world = server.addSimpleWorld("test_world");
            Location destination = new Location(world, 100, 64, 200);
            RemoteTransferActivateEvent event = new RemoteTransferActivateEvent(holder, destination);
            assertSame(destination, event.getRemoteTransferDestination());
        }

        @Test
        @DisplayName("getRemoteTransferDestination preserves location coordinates")
        void getRemoteTransferDestination_preservesCoordinates() {
            World world = server.addSimpleWorld("test_world");
            Location destination = new Location(world, 42.5, 70.0, -15.3);
            RemoteTransferActivateEvent event = new RemoteTransferActivateEvent(holder, destination);

            Location result = event.getRemoteTransferDestination();
            assertEquals(42.5, result.getX());
            assertEquals(70.0, result.getY());
            assertEquals(-15.3, result.getZ());
        }

        @Test
        @DisplayName("getAbility returns RemoteTransfer instance")
        void getAbility_returnsRemoteTransferInstance() {
            World world = server.addSimpleWorld("test_world");
            Location destination = new Location(world, 0, 64, 0);
            RemoteTransferActivateEvent event = new RemoteTransferActivateEvent(holder, destination);
            assertInstanceOf(RemoteTransfer.class, event.getAbility());
        }

        @Test
        @DisplayName("getAbilityHolder returns the holder passed at construction")
        void getAbilityHolder_returnsConstructorHolder() {
            World world = server.addSimpleWorld("test_world");
            Location destination = new Location(world, 0, 64, 0);
            RemoteTransferActivateEvent event = new RemoteTransferActivateEvent(holder, destination);
            assertSame(holder, event.getAbilityHolder());
        }

        @Test
        @DisplayName("Event is not cancelled by default")
        void isCancelled_returnsFalse_byDefault() {
            World world = server.addSimpleWorld("test_world");
            Location destination = new Location(world, 0, 64, 0);
            RemoteTransferActivateEvent event = new RemoteTransferActivateEvent(holder, destination);
            assertFalse(event.isCancelled());
        }

        @Test
        @DisplayName("setCancelled(true) makes event cancelled")
        void setCancelled_makesEventCancelled() {
            World world = server.addSimpleWorld("test_world");
            Location destination = new Location(world, 0, 64, 0);
            RemoteTransferActivateEvent event = new RemoteTransferActivateEvent(holder, destination);
            event.setCancelled(true);
            assertTrue(event.isCancelled());
        }

        @Test
        @DisplayName("setCancelled(false) after cancel restores uncancelled state")
        void setCancelled_restoresUncancelled_afterCancel() {
            World world = server.addSimpleWorld("test_world");
            Location destination = new Location(world, 0, 64, 0);
            RemoteTransferActivateEvent event = new RemoteTransferActivateEvent(holder, destination);
            event.setCancelled(true);
            event.setCancelled(false);
            assertFalse(event.isCancelled());
        }
    }
}
