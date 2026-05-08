package us.eunoians.mcrpg.ability.config;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.ability.impl.herbalism.MassHarvest;
import us.eunoians.mcrpg.ability.impl.herbalism.VerdantSurge;
import us.eunoians.mcrpg.ability.impl.mining.ItsATriple;
import us.eunoians.mcrpg.ability.impl.mining.OreScanner;
import us.eunoians.mcrpg.ability.impl.mining.RemoteTransfer;
import us.eunoians.mcrpg.ability.impl.swords.DeeperWound;
import us.eunoians.mcrpg.ability.impl.swords.EnhancedBleed;
import us.eunoians.mcrpg.ability.impl.swords.RageSpike;
import us.eunoians.mcrpg.ability.impl.swords.SerratedStrikes;
import us.eunoians.mcrpg.ability.impl.swords.Vampire;
import us.eunoians.mcrpg.ability.impl.type.configurable.ConfigurableTierableAbility;
import us.eunoians.mcrpg.ability.impl.type.configurable.ParserConfigKeys;
import us.eunoians.mcrpg.ability.impl.woodcutting.DryadsGift;
import us.eunoians.mcrpg.ability.impl.woodcutting.HeavySwing;
import us.eunoians.mcrpg.ability.impl.woodcutting.NymphsVitality;
import us.eunoians.mcrpg.configuration.file.skill.HerbalismConfigFile;
import us.eunoians.mcrpg.configuration.file.skill.MiningConfigFile;
import us.eunoians.mcrpg.configuration.file.skill.SwordsConfigFile;
import us.eunoians.mcrpg.configuration.file.skill.WoodcuttingConfigFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Validates that every Parser-backed tier-config key declared via {@link ParserConfigKeys}
 * exists in the corresponding bundled YAML configuration file.
 * <p>
 * This test maintains an explicit registry mapping each {@link ConfigurableTierableAbility}
 * implementation to its config file and tier-configuration route. When the exhaustiveness
 * guard ({@link ParserConfigKeysPresenceTest}) fails because a new ability is missing its
 * annotation, the developer must also add a registry entry here.
 */
class ParserConfigCoverageTest {

    private static final Path SKILL_CONFIG_ROOT = Path.of("src", "main", "resources", "skill_configuration");

    private static final String[] GENERIC_KEYS = {"unlock-level", "upgrade-point-cost"};

    @Test
    @DisplayName("All @ParserConfigKeys-declared tier-config keys exist in bundled YAML")
    void allAnnotatedKeys_existInBundledYaml() throws IOException {
        List<String> failures = new ArrayList<>();

        for (AbilityConfigEntry entry : abilityConfigRegistry()) {
            ParserConfigKeys annotation = entry.abilityClass().getAnnotation(ParserConfigKeys.class);
            if (annotation == null) {
                failures.add(entry.abilityClass().getSimpleName() + " is missing @ParserConfigKeys annotation");
                continue;
            }

            YamlDocument document = loadConfig(entry.configFileName());
            int maxTiers = document.getInt(entry.amountOfTiersRoute());

            String[] declaredKeys = annotation.value();
            String[] allKeys = combineWithGenericKeys(declaredKeys);

            for (int tier = 1; tier <= maxTiers; tier++) {
                for (String key : allKeys) {
                    Route tierRoute = Route.addTo(Route.addTo(entry.tierConfigRoute(), "tier-" + tier), key);
                    Route allTiersRoute = Route.addTo(Route.addTo(entry.tierConfigRoute(), "all-tiers"), key);
                    if (!document.contains(tierRoute) && !document.contains(allTiersRoute)) {
                        failures.add("%s (tier %d): missing key '%s' — expected at %s or %s".formatted(
                                entry.abilityClass().getSimpleName(), tier, key, tierRoute, allTiersRoute));
                    }
                }
            }
        }

        assertTrue(failures.isEmpty(),
                "Parser config coverage failures:\n" + String.join("\n", failures));
    }

    /**
     * Loads a bundled skill config YAML file from disk.
     *
     * @param fileName The config file name relative to skill_configuration/.
     * @return The loaded YAML document.
     * @throws IOException If the file cannot be loaded.
     */
    private YamlDocument loadConfig(String fileName) throws IOException {
        return YamlDocument.create(SKILL_CONFIG_ROOT.resolve(fileName).toFile());
    }

    /**
     * Combines ability-specific keys with the generic keys shared by all tierable abilities.
     *
     * @param abilityKeys The ability-specific parser keys.
     * @return Combined array of generic + ability-specific keys.
     */
    private String[] combineWithGenericKeys(String[] abilityKeys) {
        String[] combined = new String[GENERIC_KEYS.length + abilityKeys.length];
        System.arraycopy(GENERIC_KEYS, 0, combined, 0, GENERIC_KEYS.length);
        System.arraycopy(abilityKeys, 0, combined, GENERIC_KEYS.length, abilityKeys.length);
        return combined;
    }

    /**
     * The explicit registry of ability-to-config mappings. Each entry associates an ability
     * class with the YAML file it reads from and the tier-configuration route within that file.
     * <p>
     * This serves as a living index of "which ability reads from where."
     *
     * @return The list of all registered ability config entries.
     */
    private List<AbilityConfigEntry> abilityConfigRegistry() {
        return List.of(
                // Swords
                entry(EnhancedBleed.class, "swords_configuration.yml",
                        SwordsConfigFile.ENHANCED_BLEED_TIER_CONFIGURATION_HEADER,
                        SwordsConfigFile.ENHANCED_BLEED_AMOUNT_OF_TIERS),
                entry(DeeperWound.class, "swords_configuration.yml",
                        SwordsConfigFile.DEEPER_WOUND_TIER_CONFIGURATION_HEADER,
                        SwordsConfigFile.DEEPER_WOUND_AMOUNT_OF_TIERS),
                entry(Vampire.class, "swords_configuration.yml",
                        SwordsConfigFile.VAMPIRE_TIER_CONFIGURATION_HEADER,
                        SwordsConfigFile.VAMPIRE_AMOUNT_OF_TIERS),
                entry(SerratedStrikes.class, "swords_configuration.yml",
                        SwordsConfigFile.SERRATED_STRIKES_CONFIGURATION_HEADER,
                        SwordsConfigFile.SERRATED_STRIKES_AMOUNT_OF_TIERS),
                entry(RageSpike.class, "swords_configuration.yml",
                        SwordsConfigFile.RAGE_SPIKE_CONFIGURATION_HEADER,
                        SwordsConfigFile.RAGE_SPIKE_AMOUNT_OF_TIERS),
                // Mining
                entry(ItsATriple.class, "mining_configuration.yml",
                        MiningConfigFile.ITS_A_TRIPLE_CONFIGURATION_HEADER,
                        MiningConfigFile.ITS_A_TRIPLE_AMOUNT_OF_TIERS),
                entry(RemoteTransfer.class, "mining_configuration.yml",
                        MiningConfigFile.REMOTE_TRANSFER_CONFIGURATION_HEADER,
                        MiningConfigFile.REMOTE_TRANSFER_AMOUNT_OF_TIERS),
                entry(OreScanner.class, "mining_configuration.yml",
                        MiningConfigFile.ORE_SCANNER_CONFIGURATION_HEADER,
                        MiningConfigFile.ORE_SCANNER_AMOUNT_OF_TIERS),
                // Herbalism
                entry(VerdantSurge.class, "herbalism_configuration.yml",
                        HerbalismConfigFile.VERDANT_SURGE_TIER_CONFIGURATION_HEADER,
                        HerbalismConfigFile.VERDANT_SURGE_AMOUNT_OF_TIERS),
                entry(MassHarvest.class, "herbalism_configuration.yml",
                        HerbalismConfigFile.MASS_HARVEST_TIER_CONFIGURATION_HEADER,
                        HerbalismConfigFile.MASS_HARVEST_AMOUNT_OF_TIERS),
                // Woodcutting
                entry(HeavySwing.class, "woodcutting_configuration.yml",
                        WoodcuttingConfigFile.HEAVY_SWING_CONFIGURATION_HEADER,
                        WoodcuttingConfigFile.HEAVY_SWING_AMOUNT_OF_TIERS),
                entry(DryadsGift.class, "woodcutting_configuration.yml",
                        WoodcuttingConfigFile.DRYADS_GIFT_CONFIGURATION_HEADER,
                        WoodcuttingConfigFile.DRYADS_GIFT_AMOUNT_OF_TIERS),
                entry(NymphsVitality.class, "woodcutting_configuration.yml",
                        WoodcuttingConfigFile.NYMPHS_VITALITY_CONFIGURATION_HEADER,
                        WoodcuttingConfigFile.NYMPHS_VITALITY_AMOUNT_OF_TIERS)
        );
    }

    /**
     * Creates an ability config entry for the registry.
     *
     * @param abilityClass    The concrete ability class.
     * @param configFileName  The skill config YAML file name.
     * @param tierConfigRoute The tier-configuration route within the YAML.
     * @param amountOfTiersRoute The route to the amount-of-tiers value.
     * @return The config entry.
     */
    private AbilityConfigEntry entry(Class<? extends ConfigurableTierableAbility> abilityClass,
                                     String configFileName,
                                     Route tierConfigRoute,
                                     Route amountOfTiersRoute) {
        return new AbilityConfigEntry(abilityClass, configFileName, tierConfigRoute, amountOfTiersRoute);
    }

    private record AbilityConfigEntry(
            Class<? extends ConfigurableTierableAbility> abilityClass,
            String configFileName,
            Route tierConfigRoute,
            Route amountOfTiersRoute) {
    }
}
