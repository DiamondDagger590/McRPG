package us.eunoians.mcrpg.ability.impl.mining;

import com.diamonddagger590.mccore.configuration.ReloadableContentManager;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.route.Route;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeRegistry;
import us.eunoians.mcrpg.configuration.FileManager;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.MainConfigFile;
import us.eunoians.mcrpg.configuration.file.skill.MiningConfigFile;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.entity.player.McRPGPlayerExtension;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests that {@link OreScanner#getItemBuilderPlaceholders(McRPGPlayer)} includes
 * the {@code mana-cost} key in the returned map.
 */
@ExtendWith(McRPGPlayerExtension.class)
class OreScannerManaCostPlaceholderTest extends McRPGBaseTest {

    private OreScanner oreScanner;

    @BeforeEach
    void setUp() {
        AbilityAttributeRegistry abilityAttributeRegistry = new AbilityAttributeRegistry();
        RegistryAccess.registryAccess().register(abilityAttributeRegistry);

        ReloadableContentManager reloadableContentManager = new ReloadableContentManager(mcRPG);
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(reloadableContentManager);

        YamlDocument miningConfig = mock(YamlDocument.class);
        YamlDocument mainConfig = mock(YamlDocument.class);

        FileManager fileManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE);
        when(fileManager.getFile(FileType.MINING_CONFIG)).thenReturn(miningConfig);
        when(fileManager.getFile(FileType.MAIN_CONFIG)).thenReturn(mainConfig);

        Section emptySection = mock(Section.class);
        when(emptySection.getRoutesAsStrings(false)).thenReturn(Set.of());
        when(miningConfig.getSection(any(Route.class))).thenReturn(emptySection);
        when(miningConfig.getInt(MiningConfigFile.ORE_SCANNER_AMOUNT_OF_TIERS)).thenReturn(5);
        when(miningConfig.contains(any(Route.class))).thenReturn(false);
        when(miningConfig.getString(any(Route.class))).thenReturn("15");
        when(miningConfig.getString(any(Route.class), any())).thenReturn("15");
        when(miningConfig.getStringList(any(Route.class))).thenReturn(List.of());
        when(mainConfig.getInt(MainConfigFile.MANA_MINIMUM_ABILITY_COST, 5)).thenReturn(1);

        AbilityRegistry abilityRegistry = new AbilityRegistry(mcRPG);
        RegistryAccess.registryAccess().register(abilityRegistry);
        oreScanner = new OreScanner(mcRPG);
        abilityRegistry.register(oreScanner);
    }

    @Test
    @DisplayName("Given a player, when getItemBuilderPlaceholders() is called, then the map contains the \"mana-cost\" key")
    void getItemBuilderPlaceholders_includesManaCost(McRPGPlayer mcRPGPlayer) {
        Map<String, String> placeholders = oreScanner.getItemBuilderPlaceholders(mcRPGPlayer);
        assertTrue(placeholders.containsKey("mana-cost"),
                "Placeholders should contain the 'mana-cost' key");
        assertNotNull(placeholders.get("mana-cost"));
    }
}
