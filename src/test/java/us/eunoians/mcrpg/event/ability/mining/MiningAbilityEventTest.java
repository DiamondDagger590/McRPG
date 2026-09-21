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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
        when(emptySection.getRoutesAsStrings(false)).thenReturn(Collections.emptySet());
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
            ExtraOreActivateEvent event = new ExtraOreActivateEvent(holder, 4);
            assertEquals(4, event.getDropMultiplier());
        }

        @Test
        @DisplayName("getDropMultiplier clamps negative to 1")
        void getDropMultiplier_clampsToOne_whenNegative() {
            ExtraOreActivateEvent event = new ExtraOreActivateEvent(holder, -5);
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
            event.setDropMultiplier(8);
            assertEquals(8, event.getDropMultiplier());
        }

        @Test
        @DisplayName("setDropMultiplier clamps negative to 1")
        void setDropMultiplier_clampsToOne_whenNegative() {
            ExtraOreActivateEvent event = new ExtraOreActivateEvent(holder, 5);
            event.setDropMultiplier(-10);
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

        @Test
        @DisplayName("getAbilityHolder returns the provided holder")
        void getAbilityHolder_returnsProvidedHolder() {
            ExtraOreActivateEvent event = new ExtraOreActivateEvent(holder, 2);
            assertEquals(holder, event.getAbilityHolder());
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

        @Test
        @DisplayName("getAbilityHolder returns the provided holder")
        void getAbilityHolder_returnsProvidedHolder() {
            ItsATripleActivateEvent event = new ItsATripleActivateEvent(holder);
            assertEquals(holder, event.getAbilityHolder());
        }
    }

    @Nested
    @DisplayName("OreScannerActivateEvent")
    class OreScannerActivateEventTests {

        @Test
        @DisplayName("getInstancesOfBlocks returns an immutable copy of the provided map")
        @SuppressWarnings("deprecation")
        void getInstancesOfBlocks_returnsImmutableCopy() {
            OreScannerBlockType type = new OreScannerBlockType(
                    Set.of(org.bukkit.Material.COAL_ORE), "Coal",
                    org.bukkit.ChatColor.DARK_GRAY, 1);
            Map<OreScannerBlockType, Set<Location>> blocks = new HashMap<>();
            blocks.put(type, new HashSet<>(Set.of(new Location(mock(World.class), 0, 0, 0))));
            OreScannerActivateEvent event = new OreScannerActivateEvent(holder, blocks);

            Map<OreScannerBlockType, Set<Location>> result = event.getInstancesOfBlocks();
            assertNotNull(result);
            assertFalse(result.isEmpty());

            @SuppressWarnings("deprecation")
            OreScannerBlockType newType = new OreScannerBlockType(
                    Set.of(org.bukkit.Material.IRON_ORE), "Iron",
                    org.bukkit.ChatColor.WHITE, 5);
            blocks.put(newType, new HashSet<>());
            assertFalse(result.containsKey(newType));
        }

        @Test
        @DisplayName("getLocationsOfBlockType returns empty set for unmatched type")
        void getLocationsOfBlockType_returnsEmptySet_whenTypeNotPresent() {
            Map<OreScannerBlockType, Set<Location>> blocks = new HashMap<>();
            OreScannerActivateEvent event = new OreScannerActivateEvent(holder, blocks);

            @SuppressWarnings("deprecation")
            OreScannerBlockType unknownType = new OreScannerBlockType(
                    Set.of(org.bukkit.Material.NETHERITE_BLOCK), "Netherite",
                    org.bukkit.ChatColor.DARK_RED, 20);

            Set<Location> result = event.getLocationsOfBlockType(unknownType);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("getLocationsOfBlockType returns locations for a matched type")
        void getLocationsOfBlockType_returnsLocations_whenTypePresent() {
            @SuppressWarnings("deprecation")
            OreScannerBlockType diamondType = new OreScannerBlockType(
                    Set.of(org.bukkit.Material.DIAMOND_ORE), "Diamond",
                    org.bukkit.ChatColor.AQUA, 10);
            Location location = new Location(mock(World.class), 5, 10, 15);
            Map<OreScannerBlockType, Set<Location>> blocks = new HashMap<>();
            blocks.put(diamondType, new HashSet<>(Set.of(location)));

            OreScannerActivateEvent event = new OreScannerActivateEvent(holder, blocks);

            Set<Location> result = event.getLocationsOfBlockType(diamondType);
            assertEquals(1, result.size());
            assertTrue(result.contains(location));
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

        @Test
        @DisplayName("getAbilityHolder returns the provided holder")
        void getAbilityHolder_returnsProvidedHolder() {
            OreScannerActivateEvent event = new OreScannerActivateEvent(holder, new HashMap<>());
            assertEquals(holder, event.getAbilityHolder());
        }
    }

    @Nested
    @DisplayName("RemoteTransferActivateEvent")
    class RemoteTransferActivateEventTests {

        @Test
        @DisplayName("getRemoteTransferDestination returns the provided location")
        void getRemoteTransferDestination_returnsProvidedLocation() {
            Location destination = new Location(mock(World.class), 100, 64, 200);
            RemoteTransferActivateEvent event = new RemoteTransferActivateEvent(holder, destination);

            assertEquals(destination, event.getRemoteTransferDestination());
        }

        @Test
        @DisplayName("isCancelled returns false by default")
        void isCancelled_returnsFalse_byDefault() {
            Location destination = new Location(mock(World.class), 0, 0, 0);
            RemoteTransferActivateEvent event = new RemoteTransferActivateEvent(holder, destination);
            assertFalse(event.isCancelled());
        }

        @Test
        @DisplayName("setCancelled(true) makes isCancelled return true")
        void setCancelled_makesEventCancelled_whenSetToTrue() {
            Location destination = new Location(mock(World.class), 0, 0, 0);
            RemoteTransferActivateEvent event = new RemoteTransferActivateEvent(holder, destination);
            event.setCancelled(true);
            assertTrue(event.isCancelled());
        }

        @Test
        @DisplayName("getAbility returns RemoteTransfer")
        void getAbility_returnsRemoteTransfer() {
            Location destination = new Location(mock(World.class), 0, 0, 0);
            RemoteTransferActivateEvent event = new RemoteTransferActivateEvent(holder, destination);
            assertInstanceOf(RemoteTransfer.class, event.getAbility());
        }

        @Test
        @DisplayName("getAbilityHolder returns the provided holder")
        void getAbilityHolder_returnsProvidedHolder() {
            Location destination = new Location(mock(World.class), 0, 0, 0);
            RemoteTransferActivateEvent event = new RemoteTransferActivateEvent(holder, destination);
            assertEquals(holder, event.getAbilityHolder());
        }
    }
}
