package us.eunoians.mcrpg.ability.impl.herbalism;

import com.diamonddagger590.mccore.configuration.ReloadableContentManager;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeRegistry;
import us.eunoians.mcrpg.configuration.FileManager;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.MainConfigFile;
import us.eunoians.mcrpg.configuration.file.skill.HerbalismConfigFile;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.entity.player.McRPGPlayerExtension;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests that {@link MassHarvest#getItemBuilderPlaceholders(McRPGPlayer)} includes the
 * {@code mana-cost} key in the returned map.
 */
@org.junit.jupiter.api.extension.ExtendWith(McRPGPlayerExtension.class)
class MassHarvestManaCostPlaceholderTest extends McRPGBaseTest {

    private MassHarvest massHarvest;

    @BeforeEach
    void setUp() {
        AbilityAttributeRegistry abilityAttributeRegistry = new AbilityAttributeRegistry();
        RegistryAccess.registryAccess().register(abilityAttributeRegistry);

        ReloadableContentManager reloadableContentManager = new ReloadableContentManager(mcRPG);
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(reloadableContentManager);

        YamlDocument herbalismConfig = mock(YamlDocument.class);
        YamlDocument mainConfig = mock(YamlDocument.class);

        FileManager fileManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE);
        when(fileManager.getFile(FileType.HERBALISM_CONFIG)).thenReturn(herbalismConfig);
        when(fileManager.getFile(FileType.MAIN_CONFIG)).thenReturn(mainConfig);

        when(herbalismConfig.getInt(HerbalismConfigFile.MASS_HARVEST_AMOUNT_OF_TIERS)).thenReturn(5);
        when(herbalismConfig.contains(any(Route.class))).thenReturn(false);
        when(herbalismConfig.getString(any(Route.class))).thenReturn("15");
        when(herbalismConfig.getString(any(Route.class), any())).thenReturn("15");
        when(herbalismConfig.getStringList(any(Route.class))).thenReturn(List.of());
        when(mainConfig.getInt(MainConfigFile.MANA_MINIMUM_ABILITY_COST, 5)).thenReturn(1);

        AbilityRegistry abilityRegistry = new AbilityRegistry(mcRPG);
        RegistryAccess.registryAccess().register(abilityRegistry);
        massHarvest = new MassHarvest(mcRPG);
        abilityRegistry.register(massHarvest);
    }

    @Test
    @DisplayName("Given a player, when getItemBuilderPlaceholders() is called, then the map contains the \"mana-cost\" key")
    void getItemBuilderPlaceholders_includesManaCost(McRPGPlayer mcRPGPlayer) {
        Map<String, String> placeholders = massHarvest.getItemBuilderPlaceholders(mcRPGPlayer);
        assertTrue(placeholders.containsKey("mana-cost"),
                "Placeholders should contain the 'mana-cost' key");
        assertNotNull(placeholders.get("mana-cost"));
    }
}
