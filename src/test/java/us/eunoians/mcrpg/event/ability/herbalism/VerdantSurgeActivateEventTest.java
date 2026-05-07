package us.eunoians.mcrpg.event.ability.herbalism;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeRegistry;
import us.eunoians.mcrpg.ability.impl.herbalism.VerdantSurge;
import us.eunoians.mcrpg.configuration.FileManager;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.skill.HerbalismConfigFile;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link VerdantSurgeActivateEvent} verifying the clamping contract on
 * construction and the {@link org.bukkit.event.Cancellable} contract.
 */
class VerdantSurgeActivateEventTest extends McRPGBaseTest {

    private AbilityHolder abilityHolder;

    @BeforeEach
    void setUp() {
        AbilityAttributeRegistry abilityAttributeRegistry = new AbilityAttributeRegistry();
        RegistryAccess.registryAccess().register(abilityAttributeRegistry);

        YamlDocument herbalismConfig = mock(YamlDocument.class);
        FileManager fileManager = RegistryAccess.registryAccess()
                .registry(com.diamonddagger590.mccore.registry.RegistryKey.MANAGER)
                .manager(McRPGManagerKey.FILE);
        when(fileManager.getFile(FileType.HERBALISM_CONFIG)).thenReturn(herbalismConfig);
        when(herbalismConfig.getInt(HerbalismConfigFile.VERDANT_SURGE_AMOUNT_OF_TIERS)).thenReturn(5);

        AbilityRegistry abilityRegistry = new AbilityRegistry(mcRPG);
        RegistryAccess.registryAccess().register(abilityRegistry);
        VerdantSurge verdantSurge = new VerdantSurge(mcRPG);
        abilityRegistry.register(verdantSurge);

        abilityHolder = mock(AbilityHolder.class);
        when(abilityHolder.getUUID()).thenReturn(UUID.randomUUID());
    }

    @Test
    @DisplayName("Given a negative pulseCount, when VerdantSurgeActivateEvent is constructed, then getPulseCount() returns 0")
    void getPulseCount_returnsZero_whenConstructedWithNegativePulseCount() {
        VerdantSurgeActivateEvent event = new VerdantSurgeActivateEvent(abilityHolder, -5, 3.0);
        assertEquals(0, event.getPulseCount());
    }

    @Test
    @DisplayName("Given a negative radius, when VerdantSurgeActivateEvent is constructed, then getMaxPulseRadius() returns 0")
    void getMaxPulseRadius_returnsZero_whenConstructedWithNegativeRadius() {
        VerdantSurgeActivateEvent event = new VerdantSurgeActivateEvent(abilityHolder, 3, -10.0);
        assertEquals(0.0, event.getMaxPulseRadius());
    }

    @Test
    @DisplayName("Given a freshly-constructed event, when isCancelled() is called, then it returns false")
    void isCancelled_returnsFalse_byDefault() {
        VerdantSurgeActivateEvent event = new VerdantSurgeActivateEvent(abilityHolder, 3, 5.0);
        assertFalse(event.isCancelled());
    }

    @Test
    @DisplayName("Given an event, when setCancelled(true) is called, then isCancelled() returns true")
    void setCancelled_makesEventCancelled_whenSetToTrue() {
        VerdantSurgeActivateEvent event = new VerdantSurgeActivateEvent(abilityHolder, 3, 5.0);
        event.setCancelled(true);
        assertTrue(event.isCancelled());
    }
}
