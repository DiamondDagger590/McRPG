package us.eunoians.mcrpg.event.ability.mining;

import com.diamonddagger590.mccore.configuration.ReloadableContentManager;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.impl.mining.OreScanner;
import us.eunoians.mcrpg.ability.impl.mining.orescanner.OreScannerBlockType;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.skill.impl.mining.Mining;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("deprecation")
class OreScannerActivateEventTest extends McRPGBaseTest {

    @BeforeEach
    void setUp() {
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                .register(mock(ReloadableContentManager.class));

        AbilityRegistry abilityRegistry = new AbilityRegistry(mcRPG);
        RegistryAccess.registryAccess().register(abilityRegistry);

        OreScanner mockOreScanner = mock(OreScanner.class);
        when(mockOreScanner.getAbilityKey()).thenReturn(OreScanner.ORE_SCANNER_KEY);
        when(mockOreScanner.getSkillKey()).thenReturn(Mining.MINING_KEY);
        when(mockOreScanner.getReloadableContent()).thenReturn(Set.of());
        abilityRegistry.register(mockOreScanner);
    }

    @Test
    @DisplayName("getInstancesOfBlocks returns defensive copy that is unaffected by original mutation")
    void getInstancesOfBlocks_returnsDefensiveCopy() {
        AbilityHolder holder = mock(AbilityHolder.class);
        World world = mock(World.class);
        OreScannerBlockType blockType = new OreScannerBlockType(
                Set.of(Material.DIAMOND_ORE), "Diamond", ChatColor.AQUA, 10);
        Location loc = new Location(world, 1, 2, 3);
        Map<OreScannerBlockType, Set<Location>> blocks = new HashMap<>();
        blocks.put(blockType, Set.of(loc));

        OreScannerActivateEvent event = new OreScannerActivateEvent(holder, blocks);
        Map<OreScannerBlockType, Set<Location>> returned = event.getInstancesOfBlocks();
        assertEquals(1, returned.size());
        assertTrue(returned.containsKey(blockType));

        blocks.clear();
        assertEquals(1, returned.size(), "Returned map should be unaffected by original map mutation");
    }

    @Test
    @DisplayName("getInstancesOfBlocks returns empty map when constructed with empty map")
    void getInstancesOfBlocks_returnsEmptyMap_whenConstructedEmpty() {
        AbilityHolder holder = mock(AbilityHolder.class);
        OreScannerActivateEvent event = new OreScannerActivateEvent(holder, Map.of());
        assertTrue(event.getInstancesOfBlocks().isEmpty());
    }

    @Test
    @DisplayName("getLocationsOfBlockType returns locations for a known block type")
    void getLocationsOfBlockType_returnsLocations_whenBlockTypePresent() {
        AbilityHolder holder = mock(AbilityHolder.class);
        World world = mock(World.class);
        OreScannerBlockType blockType = new OreScannerBlockType(
                Set.of(Material.IRON_ORE), "Iron", ChatColor.WHITE, 5);
        Location loc1 = new Location(world, 10, 20, 30);
        Location loc2 = new Location(world, 40, 50, 60);
        Map<OreScannerBlockType, Set<Location>> blocks = new HashMap<>();
        blocks.put(blockType, Set.of(loc1, loc2));

        OreScannerActivateEvent event = new OreScannerActivateEvent(holder, blocks);
        Set<Location> result = event.getLocationsOfBlockType(blockType);
        assertEquals(2, result.size());
        assertTrue(result.contains(loc1));
        assertTrue(result.contains(loc2));
    }

    @Test
    @DisplayName("getLocationsOfBlockType returns empty set for unknown block type")
    void getLocationsOfBlockType_returnsEmptySet_whenBlockTypeNotPresent() {
        AbilityHolder holder = mock(AbilityHolder.class);
        OreScannerBlockType unknownType = new OreScannerBlockType(
                Set.of(Material.GOLD_ORE), "Gold", ChatColor.GOLD, 8);

        OreScannerActivateEvent event = new OreScannerActivateEvent(holder, Map.of());
        Set<Location> result = event.getLocationsOfBlockType(unknownType);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getAbility returns OreScanner instance")
    void getAbility_returnsOreScannerInstance() {
        AbilityHolder holder = mock(AbilityHolder.class);
        OreScannerActivateEvent event = new OreScannerActivateEvent(holder, Map.of());
        assertInstanceOf(OreScanner.class, event.getAbility());
    }

    @Test
    @DisplayName("Event is not cancelled by default")
    void isCancelled_returnsFalse_byDefault() {
        AbilityHolder holder = mock(AbilityHolder.class);
        OreScannerActivateEvent event = new OreScannerActivateEvent(holder, Map.of());
        assertFalse(event.isCancelled());
    }

    @Test
    @DisplayName("setCancelled(true) makes event cancelled")
    void setCancelled_makesEventCancelled() {
        AbilityHolder holder = mock(AbilityHolder.class);
        OreScannerActivateEvent event = new OreScannerActivateEvent(holder, Map.of());
        event.setCancelled(true);
        assertTrue(event.isCancelled());
    }

    @Test
    @DisplayName("setCancelled(false) restores non-cancelled state")
    void setCancelled_restoresNonCancelledState() {
        AbilityHolder holder = mock(AbilityHolder.class);
        OreScannerActivateEvent event = new OreScannerActivateEvent(holder, Map.of());
        event.setCancelled(true);
        event.setCancelled(false);
        assertFalse(event.isCancelled());
    }

    @Test
    @DisplayName("getAbilityHolder returns the holder passed at construction")
    void getAbilityHolder_returnsConstructorHolder() {
        AbilityHolder holder = mock(AbilityHolder.class);
        OreScannerActivateEvent event = new OreScannerActivateEvent(holder, Map.of());
        assertSame(holder, event.getAbilityHolder());
    }

    @Test
    @DisplayName("Multiple block types are all returned")
    void getInstancesOfBlocks_returnsAllTypes_whenMultipleBlockTypesPresent() {
        AbilityHolder holder = mock(AbilityHolder.class);
        World world = mock(World.class);
        OreScannerBlockType diamondType = new OreScannerBlockType(
                Set.of(Material.DIAMOND_ORE), "Diamond", ChatColor.AQUA, 10);
        OreScannerBlockType ironType = new OreScannerBlockType(
                Set.of(Material.IRON_ORE), "Iron", ChatColor.WHITE, 5);
        Map<OreScannerBlockType, Set<Location>> blocks = new HashMap<>();
        blocks.put(diamondType, Set.of(new Location(world, 1, 2, 3)));
        blocks.put(ironType, Set.of(new Location(world, 4, 5, 6)));

        OreScannerActivateEvent event = new OreScannerActivateEvent(holder, blocks);
        Map<OreScannerBlockType, Set<Location>> returned = event.getInstancesOfBlocks();
        assertEquals(2, returned.size());
        assertTrue(returned.containsKey(diamondType));
        assertTrue(returned.containsKey(ironType));
    }
}
