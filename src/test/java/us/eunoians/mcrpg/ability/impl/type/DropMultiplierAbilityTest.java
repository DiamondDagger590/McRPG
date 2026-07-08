package us.eunoians.mcrpg.ability.impl.type;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.builder.item.ability.AbilityItemBuilder;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DropMultiplierAbilityTest {

    private Map<Location, Integer> multiplierMap;
    private DropMultiplierAbility ability;
    private World world;

    @BeforeEach
    void setUp() {
        multiplierMap = new HashMap<>();
        world = mock(World.class);
        ability = new TestDropMultiplierAbility(multiplierMap);
    }

    @Nested
    @DisplayName("isBlockMultiplied")
    class IsBlockMultiplied {

        @Test
        @DisplayName("returns false for location without multiplier")
        void returnsFalse_whenNoMultiplier() {
            Location location = new Location(world, 10, 20, 30);
            assertFalse(ability.isBlockMultiplied(location));
        }

        @Test
        @DisplayName("returns true for location with multiplier")
        void returnsTrue_whenMultiplierExists() {
            Location location = new Location(world, 10, 20, 30);
            multiplierMap.put(location, 2);
            assertTrue(ability.isBlockMultiplied(location));
        }

        @Test
        @DisplayName("delegates from block to location")
        void delegatesFromBlock_toLocation() {
            Location location = new Location(world, 5, 10, 15);
            Block block = mock(Block.class);
            when(block.getLocation()).thenReturn(location);

            assertFalse(ability.isBlockMultiplied(block));

            multiplierMap.put(location, 3);
            assertTrue(ability.isBlockMultiplied(block));
        }
    }

    @Nested
    @DisplayName("getMultiplier")
    class GetMultiplier {

        @Test
        @DisplayName("returns 1 when no multiplier set")
        void returnsDefault_whenNoMultiplier() {
            Location location = new Location(world, 10, 20, 30);
            assertEquals(1, ability.getMultiplier(location));
        }

        @Test
        @DisplayName("returns stored multiplier value")
        void returnsStoredValue() {
            Location location = new Location(world, 10, 20, 30);
            multiplierMap.put(location, 5);
            assertEquals(5, ability.getMultiplier(location));
        }

        @Test
        @DisplayName("delegates from block to location")
        void delegatesFromBlock_toLocation() {
            Location location = new Location(world, 5, 10, 15);
            Block block = mock(Block.class);
            when(block.getLocation()).thenReturn(location);

            multiplierMap.put(location, 4);
            assertEquals(4, ability.getMultiplier(block));
        }
    }

    @Nested
    @DisplayName("addMultiplier")
    class AddMultiplier {

        @Test
        @DisplayName("stores multiplier for location")
        void storesMultiplier_forLocation() {
            Location location = new Location(world, 10, 20, 30);
            ability.addMultiplier(location, 3);

            assertTrue(multiplierMap.containsKey(location));
            assertEquals(3, multiplierMap.get(location));
        }

        @Test
        @DisplayName("overwrites existing multiplier")
        void overwritesExistingMultiplier() {
            Location location = new Location(world, 10, 20, 30);
            ability.addMultiplier(location, 2);
            ability.addMultiplier(location, 5);

            assertEquals(5, multiplierMap.get(location));
        }

        @Test
        @DisplayName("delegates from block to location")
        void delegatesFromBlock_toLocation() {
            Location location = new Location(world, 5, 10, 15);
            Block block = mock(Block.class);
            when(block.getLocation()).thenReturn(location);

            ability.addMultiplier(block, 3);
            assertEquals(3, multiplierMap.get(location));
        }
    }

    @Nested
    @DisplayName("processDropEvent")
    class ProcessDropEvent {

        @Test
        @DisplayName("multiplies item amounts for multiplied block")
        void multipliesItemAmounts_forMultipliedBlock() {
            Location location = new Location(world, 10, 20, 30);
            multiplierMap.put(location, 3);

            Block block = mock(Block.class);
            when(block.getLocation()).thenReturn(location);

            ItemStack itemStack1 = new ItemStack(Material.DIAMOND, 1);
            ItemStack itemStack2 = new ItemStack(Material.COBBLESTONE, 4);

            Item item1 = mock(Item.class);
            Item item2 = mock(Item.class);
            when(item1.getItemStack()).thenReturn(itemStack1);
            when(item2.getItemStack()).thenReturn(itemStack2);

            BlockDropItemEvent event = mock(BlockDropItemEvent.class);
            when(event.getBlock()).thenReturn(block);
            when(event.getItems()).thenReturn(List.of(item1, item2));

            ability.processDropEvent(event);

            assertEquals(3, itemStack1.getAmount());
            assertEquals(12, itemStack2.getAmount());
        }

        @Test
        @DisplayName("removes multiplier after processing")
        void removesMultiplier_afterProcessing() {
            Location location = new Location(world, 10, 20, 30);
            multiplierMap.put(location, 2);

            Block block = mock(Block.class);
            when(block.getLocation()).thenReturn(location);

            BlockDropItemEvent event = mock(BlockDropItemEvent.class);
            when(event.getBlock()).thenReturn(block);
            when(event.getItems()).thenReturn(List.of());

            ability.processDropEvent(event);

            assertFalse(multiplierMap.containsKey(location));
        }

        @Test
        @DisplayName("ignores non-BlockDropItemEvent")
        void ignoresNonBlockDropItemEvent() {
            Location location = new Location(world, 10, 20, 30);
            multiplierMap.put(location, 2);

            Block block = mock(Block.class);
            when(block.getLocation()).thenReturn(location);

            BlockBreakEvent event = mock(BlockBreakEvent.class);
            when(event.getBlock()).thenReturn(block);

            ability.processDropEvent(event);

            assertTrue(multiplierMap.containsKey(location));
        }

        @Test
        @DisplayName("ignores BlockDropItemEvent for non-multiplied block")
        void ignoresEvent_whenBlockNotMultiplied() {
            Location location = new Location(world, 10, 20, 30);

            Block block = mock(Block.class);
            when(block.getLocation()).thenReturn(location);

            ItemStack itemStack = new ItemStack(Material.DIAMOND, 1);
            Item item = mock(Item.class);
            when(item.getItemStack()).thenReturn(itemStack);

            BlockDropItemEvent event = mock(BlockDropItemEvent.class);
            when(event.getBlock()).thenReturn(block);
            when(event.getItems()).thenReturn(List.of(item));

            ability.processDropEvent(event);

            assertEquals(1, itemStack.getAmount());
        }

        @Test
        @DisplayName("handles empty item list")
        void handlesEmptyItemList() {
            Location location = new Location(world, 10, 20, 30);
            multiplierMap.put(location, 3);

            Block block = mock(Block.class);
            when(block.getLocation()).thenReturn(location);

            BlockDropItemEvent event = mock(BlockDropItemEvent.class);
            when(event.getBlock()).thenReturn(block);
            when(event.getItems()).thenReturn(List.of());

            ability.processDropEvent(event);

            assertFalse(multiplierMap.containsKey(location));
        }

        @Test
        @DisplayName("identity multiplier leaves amounts unchanged")
        void identityMultiplier_leavesAmountsUnchanged() {
            Location location = new Location(world, 10, 20, 30);
            multiplierMap.put(location, 1);

            Block block = mock(Block.class);
            when(block.getLocation()).thenReturn(location);

            ItemStack itemStack1 = new ItemStack(Material.DIAMOND, 2);
            ItemStack itemStack2 = new ItemStack(Material.COBBLESTONE, 7);

            Item item1 = mock(Item.class);
            Item item2 = mock(Item.class);
            when(item1.getItemStack()).thenReturn(itemStack1);
            when(item2.getItemStack()).thenReturn(itemStack2);

            BlockDropItemEvent event = mock(BlockDropItemEvent.class);
            when(event.getBlock()).thenReturn(block);
            when(event.getItems()).thenReturn(List.of(item1, item2));

            ability.processDropEvent(event);

            assertEquals(2, itemStack1.getAmount());
            assertEquals(7, itemStack2.getAmount());
            assertFalse(multiplierMap.containsKey(location));
        }

        @Test
        @DisplayName("different locations tracked independently")
        void differentLocations_trackedIndependently() {
            Location loc1 = new Location(world, 10, 20, 30);
            Location loc2 = new Location(world, 40, 50, 60);
            multiplierMap.put(loc1, 2);
            multiplierMap.put(loc2, 5);

            Block block = mock(Block.class);
            when(block.getLocation()).thenReturn(loc1);

            BlockDropItemEvent event = mock(BlockDropItemEvent.class);
            when(event.getBlock()).thenReturn(block);
            when(event.getItems()).thenReturn(List.of());

            ability.processDropEvent(event);

            assertFalse(multiplierMap.containsKey(loc1));
            assertTrue(multiplierMap.containsKey(loc2));
            assertEquals(5, multiplierMap.get(loc2));
        }
    }

    private static final class TestDropMultiplierAbility implements DropMultiplierAbility {

        private final Map<Location, Integer> map;
        private final NamespacedKey key;

        TestDropMultiplierAbility(@NotNull Map<Location, Integer> map) {
            this.map = map;
            this.key = NamespacedKey.fromString("test:drop_multiplier");
        }

        @Override
        public @NotNull Map<Location, Integer> getMultiplierMap() {
            return map;
        }

        @Override
        public @NotNull Plugin getPlugin() {
            return mock(Plugin.class);
        }

        @Override
        public @NotNull NamespacedKey getAbilityKey() {
            return key;
        }

        @Override
        public @NotNull Set<NamespacedKey> getApplicableAttributes() {
            return Set.of();
        }

        @Override
        public boolean activateAbility(@NotNull AbilityHolder holder, @NotNull Event event) {
            return true;
        }

        @Override
        public boolean isAbilityEnabled() {
            return true;
        }

        @Override
        public @NotNull String getDatabaseName() {
            return "test_drop_multiplier";
        }

        @Override
        public @NotNull String getName(@NotNull McRPGPlayer player) {
            return "Test Drop Multiplier";
        }

        @Override
        public @NotNull String getName() {
            return "Test Drop Multiplier";
        }

        @Override
        public @NotNull Component getDisplayName(@NotNull McRPGPlayer player) {
            return Component.text("Test Drop Multiplier");
        }

        @Override
        public @NotNull Component getDisplayName() {
            return Component.text("Test Drop Multiplier");
        }

        @Override
        public @NotNull AbilityItemBuilder getDisplayItemBuilder(@NotNull McRPGPlayer player) {
            return mock(AbilityItemBuilder.class);
        }

        @Override
        public @NotNull Optional<NamespacedKey> getExpansionKey() {
            return Optional.empty();
        }
    }
}
