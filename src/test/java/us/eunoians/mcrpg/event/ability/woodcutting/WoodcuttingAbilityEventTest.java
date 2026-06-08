package us.eunoians.mcrpg.event.ability.woodcutting;

import com.diamonddagger590.mccore.configuration.ReloadableContentManager;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeRegistry;
import us.eunoians.mcrpg.ability.impl.woodcutting.DryadsGift;
import us.eunoians.mcrpg.ability.impl.woodcutting.ExtraLumber;
import us.eunoians.mcrpg.ability.impl.woodcutting.HeavySwing;
import us.eunoians.mcrpg.ability.impl.woodcutting.NymphsVitality;
import us.eunoians.mcrpg.configuration.FileManager;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.skill.WoodcuttingConfigFile;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for all woodcutting ability activation events:
 * {@link ExtraLumberActivateEvent}, {@link HeavySwingActivateEvent},
 * {@link DryadsGiftActivateEvent}, {@link NymphsVitalityActivateEvent},
 * and {@link HeavySwingFakeBlockBreakEvent}.
 */
class WoodcuttingAbilityEventTest extends McRPGBaseTest {

    private AbilityHolder holder;

    @BeforeEach
    void setUp() {
        AbilityAttributeRegistry abilityAttributeRegistry = new AbilityAttributeRegistry();
        RegistryAccess.registryAccess().register(abilityAttributeRegistry);

        ReloadableContentManager reloadableContentManager = new ReloadableContentManager(mcRPG);
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(reloadableContentManager);

        YamlDocument woodcuttingConfig = mock(YamlDocument.class);
        FileManager fileManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.FILE);
        when(fileManager.getFile(FileType.WOODCUTTING_CONFIG)).thenReturn(woodcuttingConfig);

        when(woodcuttingConfig.getInt(WoodcuttingConfigFile.HEAVY_SWING_AMOUNT_OF_TIERS)).thenReturn(5);
        when(woodcuttingConfig.getInt(WoodcuttingConfigFile.DRYADS_GIFT_AMOUNT_OF_TIERS)).thenReturn(5);
        when(woodcuttingConfig.getInt(WoodcuttingConfigFile.NYMPHS_VITALITY_AMOUNT_OF_TIERS)).thenReturn(5);

        lenient().when(woodcuttingConfig.getStringList(any(Route.class))).thenReturn(List.of());

        AbilityRegistry abilityRegistry = new AbilityRegistry(mcRPG);
        RegistryAccess.registryAccess().register(abilityRegistry);

        abilityRegistry.register(new ExtraLumber(mcRPG));
        abilityRegistry.register(new HeavySwing(mcRPG));
        abilityRegistry.register(new DryadsGift(mcRPG));
        abilityRegistry.register(new NymphsVitality(mcRPG));

        holder = mock(AbilityHolder.class);
        when(holder.getUUID()).thenReturn(UUID.randomUUID());
    }

    @Nested
    @DisplayName("ExtraLumberActivateEvent")
    class ExtraLumberActivateEventTests {

        @Test
        @DisplayName("getDropMultiplier returns constructor value when positive")
        void getDropMultiplier_returnsConstructorValue_whenPositive() {
            ExtraLumberActivateEvent event = new ExtraLumberActivateEvent(holder, 5);
            assertEquals(5, event.getDropMultiplier());
        }

        @Test
        @DisplayName("getDropMultiplier clamps negative to 1")
        void getDropMultiplier_clampsToOne_whenNegative() {
            ExtraLumberActivateEvent event = new ExtraLumberActivateEvent(holder, -3);
            assertEquals(1, event.getDropMultiplier());
        }

        @Test
        @DisplayName("getDropMultiplier clamps zero to 1")
        void getDropMultiplier_clampsToOne_whenZero() {
            ExtraLumberActivateEvent event = new ExtraLumberActivateEvent(holder, 0);
            assertEquals(1, event.getDropMultiplier());
        }

        @Test
        @DisplayName("setDropMultiplier updates value when positive")
        void setDropMultiplier_updatesValue_whenPositive() {
            ExtraLumberActivateEvent event = new ExtraLumberActivateEvent(holder, 2);
            event.setDropMultiplier(10);
            assertEquals(10, event.getDropMultiplier());
        }

        @Test
        @DisplayName("setDropMultiplier clamps negative to 1")
        void setDropMultiplier_clampsToOne_whenNegative() {
            ExtraLumberActivateEvent event = new ExtraLumberActivateEvent(holder, 5);
            event.setDropMultiplier(-7);
            assertEquals(1, event.getDropMultiplier());
        }

        @Test
        @DisplayName("isCancelled returns false by default")
        void isCancelled_returnsFalse_byDefault() {
            ExtraLumberActivateEvent event = new ExtraLumberActivateEvent(holder, 2);
            assertFalse(event.isCancelled());
        }

        @Test
        @DisplayName("setCancelled(true) makes isCancelled return true")
        void setCancelled_makesEventCancelled_whenSetToTrue() {
            ExtraLumberActivateEvent event = new ExtraLumberActivateEvent(holder, 2);
            event.setCancelled(true);
            assertTrue(event.isCancelled());
        }

        @Test
        @DisplayName("getAbility returns ExtraLumber")
        void getAbility_returnsExtraLumber() {
            ExtraLumberActivateEvent event = new ExtraLumberActivateEvent(holder, 2);
            assertInstanceOf(ExtraLumber.class, event.getAbility());
        }
    }

    @Nested
    @DisplayName("HeavySwingActivateEvent")
    class HeavySwingActivateEventTests {

        @Test
        @DisplayName("getToBreakLocations returns the provided set")
        void getToBreakLocations_returnsProvidedSet() {
            Location location = new Location(mock(World.class), 1, 2, 3);
            Set<Location> locations = new HashSet<>(Set.of(location));
            HeavySwingActivateEvent event = new HeavySwingActivateEvent(holder, locations);

            assertSame(locations, event.getToBreakLocations());
        }

        @Test
        @DisplayName("setToBreakLocations replaces the set")
        void setToBreakLocations_replacesSet() {
            Location location1 = new Location(mock(World.class), 1, 2, 3);
            Location location2 = new Location(mock(World.class), 4, 5, 6);
            Set<Location> original = new HashSet<>(Set.of(location1));
            Set<Location> replacement = new HashSet<>(Set.of(location2));

            HeavySwingActivateEvent event = new HeavySwingActivateEvent(holder, original);
            event.setToBreakLocations(replacement);

            assertSame(replacement, event.getToBreakLocations());
        }

        @Test
        @DisplayName("isCancelled returns false by default")
        void isCancelled_returnsFalse_byDefault() {
            HeavySwingActivateEvent event = new HeavySwingActivateEvent(holder, new HashSet<>());
            assertFalse(event.isCancelled());
        }

        @Test
        @DisplayName("setCancelled(true) makes isCancelled return true")
        void setCancelled_makesEventCancelled_whenSetToTrue() {
            HeavySwingActivateEvent event = new HeavySwingActivateEvent(holder, new HashSet<>());
            event.setCancelled(true);
            assertTrue(event.isCancelled());
        }

        @Test
        @DisplayName("getAbility returns HeavySwing")
        void getAbility_returnsHeavySwing() {
            HeavySwingActivateEvent event = new HeavySwingActivateEvent(holder, new HashSet<>());
            assertInstanceOf(HeavySwing.class, event.getAbility());
        }
    }

    @Nested
    @DisplayName("DryadsGiftActivateEvent")
    class DryadsGiftActivateEventTests {

        @Test
        @DisplayName("getExperienceToDrop returns constructor value when positive")
        void getExperienceToDrop_returnsConstructorValue_whenPositive() {
            DryadsGiftActivateEvent event = new DryadsGiftActivateEvent(holder, 10);
            assertEquals(10, event.getExperienceToDrop());
        }

        @Test
        @DisplayName("getExperienceToDrop clamps negative to 1")
        void getExperienceToDrop_clampsToOne_whenNegative() {
            DryadsGiftActivateEvent event = new DryadsGiftActivateEvent(holder, -5);
            assertEquals(1, event.getExperienceToDrop());
        }

        @Test
        @DisplayName("getExperienceToDrop clamps zero to 1")
        void getExperienceToDrop_clampsToOne_whenZero() {
            DryadsGiftActivateEvent event = new DryadsGiftActivateEvent(holder, 0);
            assertEquals(1, event.getExperienceToDrop());
        }

        @Test
        @DisplayName("setDropMultiplier updates value when positive")
        void setDropMultiplier_updatesValue_whenPositive() {
            DryadsGiftActivateEvent event = new DryadsGiftActivateEvent(holder, 5);
            event.setDropMultiplier(20);
            assertEquals(20, event.getExperienceToDrop());
        }

        @Test
        @DisplayName("setDropMultiplier clamps negative to 1")
        void setDropMultiplier_clampsToOne_whenNegative() {
            DryadsGiftActivateEvent event = new DryadsGiftActivateEvent(holder, 5);
            event.setDropMultiplier(-3);
            assertEquals(1, event.getExperienceToDrop());
        }

        @Test
        @DisplayName("isCancelled returns false by default")
        void isCancelled_returnsFalse_byDefault() {
            DryadsGiftActivateEvent event = new DryadsGiftActivateEvent(holder, 5);
            assertFalse(event.isCancelled());
        }

        @Test
        @DisplayName("setCancelled(true) makes isCancelled return true")
        void setCancelled_makesEventCancelled_whenSetToTrue() {
            DryadsGiftActivateEvent event = new DryadsGiftActivateEvent(holder, 5);
            event.setCancelled(true);
            assertTrue(event.isCancelled());
        }

        @Test
        @DisplayName("getAbility returns DryadsGift")
        void getAbility_returnsDryadsGift() {
            DryadsGiftActivateEvent event = new DryadsGiftActivateEvent(holder, 5);
            assertInstanceOf(DryadsGift.class, event.getAbility());
        }
    }

    @Nested
    @DisplayName("NymphsVitalityActivateEvent")
    class NymphsVitalityActivateEventTests {

        @Test
        @DisplayName("isCancelled returns false by default")
        void isCancelled_returnsFalse_byDefault() {
            NymphsVitalityActivateEvent event = new NymphsVitalityActivateEvent(holder);
            assertFalse(event.isCancelled());
        }

        @Test
        @DisplayName("setCancelled(true) makes isCancelled return true")
        void setCancelled_makesEventCancelled_whenSetToTrue() {
            NymphsVitalityActivateEvent event = new NymphsVitalityActivateEvent(holder);
            event.setCancelled(true);
            assertTrue(event.isCancelled());
        }

        @Test
        @DisplayName("getAbility returns NymphsVitality")
        void getAbility_returnsNymphsVitality() {
            NymphsVitalityActivateEvent event = new NymphsVitalityActivateEvent(holder);
            assertInstanceOf(NymphsVitality.class, event.getAbility());
        }
    }

    @Nested
    @DisplayName("HeavySwingFakeBlockBreakEvent")
    class HeavySwingFakeBlockBreakEventTests {

        @Test
        @DisplayName("getPlayer returns the provided player")
        void getPlayer_returnsProvidedPlayer() {
            Player player = mock(Player.class);
            Block block = mock(Block.class);
            HeavySwingFakeBlockBreakEvent event = new HeavySwingFakeBlockBreakEvent(player, block);

            assertEquals(player, event.getPlayer());
        }

        @Test
        @DisplayName("getBlock returns the provided block")
        void getBlock_returnsProvidedBlock() {
            Player player = mock(Player.class);
            Block block = mock(Block.class);
            HeavySwingFakeBlockBreakEvent event = new HeavySwingFakeBlockBreakEvent(player, block);

            assertEquals(block, event.getBlock());
        }

        @Test
        @DisplayName("hasPassedChecks returns true by default")
        void hasPassedChecks_returnsTrue_byDefault() {
            Player player = mock(Player.class);
            Block block = mock(Block.class);
            HeavySwingFakeBlockBreakEvent event = new HeavySwingFakeBlockBreakEvent(player, block);

            assertTrue(event.hasPassedChecks());
        }

        @Test
        @DisplayName("setPassedChecks(false) makes hasPassedChecks return false")
        void setPassedChecks_makesChecksFail_whenSetToFalse() {
            Player player = mock(Player.class);
            Block block = mock(Block.class);
            HeavySwingFakeBlockBreakEvent event = new HeavySwingFakeBlockBreakEvent(player, block);
            event.setPassedChecks(false);

            assertFalse(event.hasPassedChecks());
        }
    }
}
