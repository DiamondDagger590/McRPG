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
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
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
        when(miningConfig.getInt(MiningConfigFile.REMOTE_TRANSFER_AMOUNT_OF_TIERS)).thenReturn(5);
        when(miningConfig.getInt(MiningConfigFile.ORE_SCANNER_AMOUNT_OF_TIERS)).thenReturn(5);

        lenient().when(miningConfig.getStringList(any(Route.class))).thenReturn(List.of());

        Section emptySection = mock(Section.class);
        when(emptySection.getRoutesAsStrings(false)).thenReturn(Set.of());
        lenient().when(miningConfig.getSection(any(Route.class))).thenReturn(emptySection);

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
        @DisplayName("getDropMultiplier returns constructor value when positive")
        void getDropMultiplier_returnsConstructorValue_whenPositive() {
            ExtraOreActivateEvent event = new ExtraOreActivateEvent(holder, 5);
            assertEquals(5, event.getDropMultiplier());
        }

        @Test
        @DisplayName("getDropMultiplier clamps negative to 1")
        void getDropMultiplier_clampsToOne_whenNegative() {
            ExtraOreActivateEvent event = new ExtraOreActivateEvent(holder, -3);
            assertEquals(1, event.getDropMultiplier());
        }

        @Test
        @DisplayName("getDropMultiplier clamps zero to 1")
        void getDropMultiplier_clampsToOne_whenZero() {
            ExtraOreActivateEvent event = new ExtraOreActivateEvent(holder, 0);
            assertEquals(1, event.getDropMultiplier());
        }

        @Test
        @DisplayName("setDropMultiplier updates value when positive")
        void setDropMultiplier_updatesValue_whenPositive() {
            ExtraOreActivateEvent event = new ExtraOreActivateEvent(holder, 2);
            event.setDropMultiplier(10);
            assertEquals(10, event.getDropMultiplier());
        }

        @Test
        @DisplayName("setDropMultiplier clamps negative to 1")
        void setDropMultiplier_clampsToOne_whenNegative() {
            ExtraOreActivateEvent event = new ExtraOreActivateEvent(holder, 5);
            event.setDropMultiplier(-7);
            assertEquals(1, event.getDropMultiplier());
        }

        @Test
        @DisplayName("isCancelled returns false by default")
        void isCancelled_returnsFalse_byDefault() {
            ExtraOreActivateEvent event = new ExtraOreActivateEvent(holder, 2);
            assertFalse(event.isCancelled());
        }

        @Test
        @DisplayName("setCancelled(true) makes isCancelled return true")
        void setCancelled_makesEventCancelled_whenSetToTrue() {
            ExtraOreActivateEvent event = new ExtraOreActivateEvent(holder, 2);
            event.setCancelled(true);
            assertTrue(event.isCancelled());
        }

        @Test
        @DisplayName("getAbility returns ExtraOre")
        void getAbility_returnsExtraOre() {
            ExtraOreActivateEvent event = new ExtraOreActivateEvent(holder, 2);
            assertInstanceOf(ExtraOre.class, event.getAbility());
        }
    }

    @Nested
    @DisplayName("ItsATripleActivateEvent")
    class ItsATripleActivateEventTests {

        @Test
        @DisplayName("isCancelled returns false by default")
        void isCancelled_returnsFalse_byDefault() {
            ItsATripleActivateEvent event = new ItsATripleActivateEvent(holder);
            assertFalse(event.isCancelled());
        }

        @Test
        @DisplayName("setCancelled(true) makes isCancelled return true")
        void setCancelled_makesEventCancelled_whenSetToTrue() {
            ItsATripleActivateEvent event = new ItsATripleActivateEvent(holder);
            event.setCancelled(true);
            assertTrue(event.isCancelled());
        }

        @Test
        @DisplayName("getAbility returns ItsATriple")
        void getAbility_returnsItsATriple() {
            ItsATripleActivateEvent event = new ItsATripleActivateEvent(holder);
            assertInstanceOf(ItsATriple.class, event.getAbility());
        }
    }

    @Nested
    @DisplayName("OreScannerActivateEvent")
    class OreScannerActivateEventTests {

        @Test
        @DisplayName("getInstancesOfBlocks returns immutable copy")
        void getInstancesOfBlocks_returnsImmutableCopy() {
            OreScannerBlockType blockType = mock(OreScannerBlockType.class);
            Location location = new Location(mock(World.class), 1, 2, 3);
            Map<OreScannerBlockType, Set<Location>> map = new HashMap<>();
            map.put(blockType, new HashSet<>(Set.of(location)));

            OreScannerActivateEvent event = new OreScannerActivateEvent(holder, map);
            Map<OreScannerBlockType, Set<Location>> returned = event.getInstancesOfBlocks();

            assertEquals(1, returned.size());
            assertTrue(returned.containsKey(blockType));
            assertThrows(UnsupportedOperationException.class, () -> returned.put(mock(OreScannerBlockType.class), Set.of()));
        }

        @Test
        @DisplayName("getLocationsOfBlockType returns immutable copy for existing type")
        void getLocationsOfBlockType_returnsCopy_whenTypeExists() {
            OreScannerBlockType blockType = mock(OreScannerBlockType.class);
            Location location = new Location(mock(World.class), 1, 2, 3);
            Map<OreScannerBlockType, Set<Location>> map = new HashMap<>();
            map.put(blockType, new HashSet<>(Set.of(location)));

            OreScannerActivateEvent event = new OreScannerActivateEvent(holder, map);
            Set<Location> locations = event.getLocationsOfBlockType(blockType);

            assertEquals(1, locations.size());
            assertTrue(locations.contains(location));
            assertThrows(UnsupportedOperationException.class, () -> locations.add(new Location(mock(World.class), 9, 9, 9)));
        }

        @Test
        @DisplayName("getLocationsOfBlockType returns empty set for missing type")
        void getLocationsOfBlockType_returnsEmptySet_whenTypeMissing() {
            OreScannerActivateEvent event = new OreScannerActivateEvent(holder, new HashMap<>());
            OreScannerBlockType missingType = mock(OreScannerBlockType.class);

            Set<Location> locations = event.getLocationsOfBlockType(missingType);

            assertTrue(locations.isEmpty());
        }

        @Test
        @DisplayName("isCancelled returns false by default")
        void isCancelled_returnsFalse_byDefault() {
            OreScannerActivateEvent event = new OreScannerActivateEvent(holder, new HashMap<>());
            assertFalse(event.isCancelled());
        }

        @Test
        @DisplayName("setCancelled(true) makes isCancelled return true")
        void setCancelled_makesEventCancelled_whenSetToTrue() {
            OreScannerActivateEvent event = new OreScannerActivateEvent(holder, new HashMap<>());
            event.setCancelled(true);
            assertTrue(event.isCancelled());
        }

        @Test
        @DisplayName("getAbility returns OreScanner")
        void getAbility_returnsOreScanner() {
            OreScannerActivateEvent event = new OreScannerActivateEvent(holder, new HashMap<>());
            assertInstanceOf(OreScanner.class, event.getAbility());
        }
    }

    @Nested
    @DisplayName("RemoteTransferActivateEvent")
    class RemoteTransferActivateEventTests {

        @Test
        @DisplayName("getRemoteTransferDestination returns the location")
        void getRemoteTransferDestination_returnsLocation() {
            Location location = new Location(mock(World.class), 1, 2, 3);
            RemoteTransferActivateEvent event = new RemoteTransferActivateEvent(holder, location);

            assertEquals(location, event.getRemoteTransferDestination());
        }

        @Test
        @DisplayName("isCancelled returns false by default")
        void isCancelled_returnsFalse_byDefault() {
            Location location = new Location(mock(World.class), 1, 2, 3);
            RemoteTransferActivateEvent event = new RemoteTransferActivateEvent(holder, location);
            assertFalse(event.isCancelled());
        }

        @Test
        @DisplayName("setCancelled(true) makes isCancelled return true")
        void setCancelled_makesEventCancelled_whenSetToTrue() {
            Location location = new Location(mock(World.class), 1, 2, 3);
            RemoteTransferActivateEvent event = new RemoteTransferActivateEvent(holder, location);
            event.setCancelled(true);
            assertTrue(event.isCancelled());
        }

        @Test
        @DisplayName("getAbility returns RemoteTransfer")
        void getAbility_returnsRemoteTransfer() {
            Location location = new Location(mock(World.class), 1, 2, 3);
            RemoteTransferActivateEvent event = new RemoteTransferActivateEvent(holder, location);
            assertInstanceOf(RemoteTransfer.class, event.getAbility());
        }
    }
}
