package us.eunoians.mcrpg.event.ability.herbalism;

import com.diamonddagger590.mccore.configuration.ReloadableContentManager;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.block.Block;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeRegistry;
import us.eunoians.mcrpg.ability.impl.herbalism.InstantIrrigation;
import us.eunoians.mcrpg.ability.impl.herbalism.MassHarvest;
import us.eunoians.mcrpg.ability.impl.herbalism.TooManyPlants;
import us.eunoians.mcrpg.configuration.FileManager;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for the herbalism ability activate events: {@link MassHarvestActivateEvent},
 * {@link TooManyPlantsActivateEvent}, and {@link InstantIrrigationActivateEvent}.
 * Verifies clamping contracts, getter/setter behavior, and the {@link org.bukkit.event.Cancellable} contract.
 */
class HerbalismAbilityEventTest extends McRPGBaseTest {

    private AbilityHolder abilityHolder;

    @BeforeEach
    void setUp() {
        AbilityAttributeRegistry abilityAttributeRegistry = new AbilityAttributeRegistry();
        RegistryAccess.registryAccess().register(abilityAttributeRegistry);

        ReloadableContentManager reloadableContentManager = new ReloadableContentManager(mcRPG);
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(reloadableContentManager);

        YamlDocument herbalismConfig = mock(YamlDocument.class);
        FileManager fileManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.FILE);
        when(fileManager.getFile(FileType.HERBALISM_CONFIG)).thenReturn(herbalismConfig);
        lenient().when(herbalismConfig.getStringList(any(Route.class))).thenReturn(List.of());

        AbilityRegistry abilityRegistry = new AbilityRegistry(mcRPG);
        RegistryAccess.registryAccess().register(abilityRegistry);

        MassHarvest massHarvest = new MassHarvest(mcRPG);
        abilityRegistry.register(massHarvest);

        TooManyPlants tooManyPlants = new TooManyPlants(mcRPG);
        abilityRegistry.register(tooManyPlants);

        InstantIrrigation instantIrrigation = new InstantIrrigation(mcRPG);
        abilityRegistry.register(instantIrrigation);

        abilityHolder = mock(AbilityHolder.class);
        when(abilityHolder.getUUID()).thenReturn(UUID.randomUUID());
    }

    @Nested
    @DisplayName("MassHarvestActivateEvent")
    class MassHarvestActivateEventTests {

        @Test
        @DisplayName("getMaxPulseRadius returns constructor value")
        void getMaxPulseRadius_returnsConstructorValue() {
            MassHarvestActivateEvent event = new MassHarvestActivateEvent(abilityHolder, 5.0);
            assertEquals(5.0, event.getMaxPulseRadius());
        }

        @Test
        @DisplayName("constructor clamps negative maxPulseRadius to 0")
        void constructor_clampsNegativeRadius_toZero() {
            MassHarvestActivateEvent event = new MassHarvestActivateEvent(abilityHolder, -3.0);
            assertEquals(0.0, event.getMaxPulseRadius());
        }

        @Test
        @DisplayName("setMaxPulseRadius updates value")
        void setMaxPulseRadius_updatesValue() {
            MassHarvestActivateEvent event = new MassHarvestActivateEvent(abilityHolder, 5.0);
            event.setMaxPulseRadius(10);
            assertEquals(10.0, event.getMaxPulseRadius());
        }

        @Test
        @DisplayName("setMaxPulseRadius clamps negative to 0")
        void setMaxPulseRadius_clampsNegative_toZero() {
            MassHarvestActivateEvent event = new MassHarvestActivateEvent(abilityHolder, 5.0);
            event.setMaxPulseRadius(-7);
            assertEquals(0.0, event.getMaxPulseRadius());
        }

        @Test
        @DisplayName("isCancelled returns false by default")
        void isCancelled_returnsFalse_byDefault() {
            MassHarvestActivateEvent event = new MassHarvestActivateEvent(abilityHolder, 5.0);
            assertFalse(event.isCancelled());
        }

        @Test
        @DisplayName("setCancelled(true) makes event cancelled")
        void setCancelled_makesEventCancelled_whenSetToTrue() {
            MassHarvestActivateEvent event = new MassHarvestActivateEvent(abilityHolder, 5.0);
            event.setCancelled(true);
            assertTrue(event.isCancelled());
        }

        @Test
        @DisplayName("getAbility returns MassHarvest")
        void getAbility_returnsMassHarvest() {
            MassHarvestActivateEvent event = new MassHarvestActivateEvent(abilityHolder, 5.0);
            assertTrue(event.getAbility() instanceof MassHarvest);
        }
    }

    @Nested
    @DisplayName("TooManyPlantsActivateEvent")
    class TooManyPlantsActivateEventTests {

        @Test
        @DisplayName("getDropMultiplier returns constructor value")
        void getDropMultiplier_returnsConstructorValue() {
            TooManyPlantsActivateEvent event = new TooManyPlantsActivateEvent(abilityHolder, 3);
            assertEquals(3, event.getDropMultiplier());
        }

        @Test
        @DisplayName("constructor clamps negative dropMultiplier to 1")
        void constructor_clampsNegativeMultiplier_toOne() {
            TooManyPlantsActivateEvent event = new TooManyPlantsActivateEvent(abilityHolder, -5);
            assertEquals(1, event.getDropMultiplier());
        }

        @Test
        @DisplayName("constructor clamps zero dropMultiplier to 1")
        void constructor_clampsZeroMultiplier_toOne() {
            TooManyPlantsActivateEvent event = new TooManyPlantsActivateEvent(abilityHolder, 0);
            assertEquals(1, event.getDropMultiplier());
        }

        @Test
        @DisplayName("setDropMultiplier updates value")
        void setDropMultiplier_updatesValue() {
            TooManyPlantsActivateEvent event = new TooManyPlantsActivateEvent(abilityHolder, 2);
            event.setDropMultiplier(5);
            assertEquals(5, event.getDropMultiplier());
        }

        @Test
        @DisplayName("setDropMultiplier clamps negative to 1")
        void setDropMultiplier_clampsNegative_toOne() {
            TooManyPlantsActivateEvent event = new TooManyPlantsActivateEvent(abilityHolder, 2);
            event.setDropMultiplier(-10);
            assertEquals(1, event.getDropMultiplier());
        }

        @Test
        @DisplayName("isCancelled returns false by default")
        void isCancelled_returnsFalse_byDefault() {
            TooManyPlantsActivateEvent event = new TooManyPlantsActivateEvent(abilityHolder, 2);
            assertFalse(event.isCancelled());
        }

        @Test
        @DisplayName("setCancelled(true) makes event cancelled")
        void setCancelled_makesEventCancelled_whenSetToTrue() {
            TooManyPlantsActivateEvent event = new TooManyPlantsActivateEvent(abilityHolder, 2);
            event.setCancelled(true);
            assertTrue(event.isCancelled());
        }

        @Test
        @DisplayName("getAbility returns TooManyPlants")
        void getAbility_returnsTooManyPlants() {
            TooManyPlantsActivateEvent event = new TooManyPlantsActivateEvent(abilityHolder, 2);
            assertTrue(event.getAbility() instanceof TooManyPlants);
        }
    }

    @Nested
    @DisplayName("InstantIrrigationActivateEvent")
    class InstantIrrigationActivateEventTests {

        @Test
        @DisplayName("getBlock returns the constructor block")
        void getBlock_returnsConstructorBlock() {
            Block block = mock(Block.class);
            InstantIrrigationActivateEvent event = new InstantIrrigationActivateEvent(abilityHolder, block);
            assertSame(block, event.getBlock());
        }

        @Test
        @DisplayName("isCancelled returns false by default")
        void isCancelled_returnsFalse_byDefault() {
            Block block = mock(Block.class);
            InstantIrrigationActivateEvent event = new InstantIrrigationActivateEvent(abilityHolder, block);
            assertFalse(event.isCancelled());
        }

        @Test
        @DisplayName("setCancelled(true) makes event cancelled")
        void setCancelled_makesEventCancelled_whenSetToTrue() {
            Block block = mock(Block.class);
            InstantIrrigationActivateEvent event = new InstantIrrigationActivateEvent(abilityHolder, block);
            event.setCancelled(true);
            assertTrue(event.isCancelled());
        }
    }
}
