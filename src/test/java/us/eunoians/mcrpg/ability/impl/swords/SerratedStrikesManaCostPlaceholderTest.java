package us.eunoians.mcrpg.ability.impl.swords;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
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
import us.eunoians.mcrpg.configuration.file.skill.SwordsConfigFile;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.entity.player.McRPGPlayerExtension;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests that {@link SerratedStrikes#getItemBuilderPlaceholders(McRPGPlayer)} includes
 * the {@code mana-cost} key and that pre-existing keys are still present.
 */
@ExtendWith(McRPGPlayerExtension.class)
class SerratedStrikesManaCostPlaceholderTest extends McRPGBaseTest {

    private SerratedStrikes serratedStrikes;

    @BeforeEach
    void setUp() {
        AbilityAttributeRegistry abilityAttributeRegistry = new AbilityAttributeRegistry();
        RegistryAccess.registryAccess().register(abilityAttributeRegistry);

        YamlDocument swordsConfig = mock(YamlDocument.class);
        YamlDocument mainConfig = mock(YamlDocument.class);

        FileManager fileManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE);
        when(fileManager.getFile(FileType.SWORDS_CONFIG)).thenReturn(swordsConfig);
        when(fileManager.getFile(FileType.MAIN_CONFIG)).thenReturn(mainConfig);

        when(swordsConfig.getInt(SwordsConfigFile.SERRATED_STRIKES_AMOUNT_OF_TIERS)).thenReturn(5);
        when(swordsConfig.contains(any(Route.class))).thenReturn(false);
        when(swordsConfig.getString(any(Route.class))).thenReturn("10");
        when(swordsConfig.getString(any(Route.class), any())).thenReturn("10");
        when(swordsConfig.getStringList(any(Route.class))).thenReturn(List.of());
        when(mainConfig.getInt(MainConfigFile.MANA_MINIMUM_ABILITY_COST, 5)).thenReturn(1);

        AbilityRegistry abilityRegistry = new AbilityRegistry(mcRPG);
        RegistryAccess.registryAccess().register(abilityRegistry);
        serratedStrikes = new SerratedStrikes(mcRPG);
        abilityRegistry.register(serratedStrikes);
    }

    @Test
    @DisplayName("Given a player, when getItemBuilderPlaceholders() is called, then the map contains the \"mana-cost\" key")
    void getItemBuilderPlaceholders_includesManaCost(McRPGPlayer mcRPGPlayer) {
        Map<String, String> placeholders = serratedStrikes.getItemBuilderPlaceholders(mcRPGPlayer);
        assertTrue(placeholders.containsKey("mana-cost"), "Placeholders should contain 'mana-cost'");
        assertNotNull(placeholders.get("mana-cost"));
    }

    @Test
    @DisplayName("Given a player, when getItemBuilderPlaceholders() is called, then the map still contains the \"ability-duration\" key")
    void getItemBuilderPlaceholders_includesDuration(McRPGPlayer mcRPGPlayer) {
        Map<String, String> placeholders = serratedStrikes.getItemBuilderPlaceholders(mcRPGPlayer);
        assertTrue(placeholders.containsKey("ability-duration"), "Placeholders should still contain 'ability-duration'");
    }

    @Test
    @DisplayName("Given a player, when getItemBuilderPlaceholders() is called, then the map still contains the \"cooldown\" key")
    void getItemBuilderPlaceholders_includesCooldown(McRPGPlayer mcRPGPlayer) {
        Map<String, String> placeholders = serratedStrikes.getItemBuilderPlaceholders(mcRPGPlayer);
        assertTrue(placeholders.containsKey("cooldown"), "Placeholders should still contain 'cooldown'");
    }

    @Test
    @DisplayName("Given a player, when getItemBuilderPlaceholders() is called, then the map still contains the \"activation-chance-increase\" key")
    void getItemBuilderPlaceholders_includesActivationChanceIncrease(McRPGPlayer mcRPGPlayer) {
        Map<String, String> placeholders = serratedStrikes.getItemBuilderPlaceholders(mcRPGPlayer);
        assertTrue(placeholders.containsKey("activation-chance-increase"),
                "Placeholders should still contain 'activation-chance-increase'");
    }
}
