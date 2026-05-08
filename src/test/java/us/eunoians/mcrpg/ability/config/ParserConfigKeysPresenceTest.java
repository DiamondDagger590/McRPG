package us.eunoians.mcrpg.ability.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.impl.herbalism.InstantIrrigation;
import us.eunoians.mcrpg.ability.impl.herbalism.MassHarvest;
import us.eunoians.mcrpg.ability.impl.herbalism.TooManyPlants;
import us.eunoians.mcrpg.ability.impl.herbalism.VerdantSurge;
import us.eunoians.mcrpg.ability.impl.mining.ExtraOre;
import us.eunoians.mcrpg.ability.impl.mining.ItsATriple;
import us.eunoians.mcrpg.ability.impl.mining.OreScanner;
import us.eunoians.mcrpg.ability.impl.mining.RemoteTransfer;
import us.eunoians.mcrpg.ability.impl.swords.Bleed;
import us.eunoians.mcrpg.ability.impl.swords.DeeperWound;
import us.eunoians.mcrpg.ability.impl.swords.EnhancedBleed;
import us.eunoians.mcrpg.ability.impl.swords.RageSpike;
import us.eunoians.mcrpg.ability.impl.swords.SerratedStrikes;
import us.eunoians.mcrpg.ability.impl.swords.Vampire;
import us.eunoians.mcrpg.ability.impl.type.configurable.ConfigurableTierableAbility;
import us.eunoians.mcrpg.ability.impl.type.configurable.ParserConfigKeys;
import us.eunoians.mcrpg.ability.impl.woodcutting.DryadsGift;
import us.eunoians.mcrpg.ability.impl.woodcutting.ExtraLumber;
import us.eunoians.mcrpg.ability.impl.woodcutting.HeavySwing;
import us.eunoians.mcrpg.ability.impl.woodcutting.NymphsVitality;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Exhaustiveness guard that verifies every {@link ConfigurableTierableAbility} registered in
 * {@link us.eunoians.mcrpg.expansion.McRPGExpansion McRPGExpansion} carries the
 * {@link ParserConfigKeys} annotation.
 * <p>
 * This list mirrors {@code McRPGExpansion.createAbilities()} and must stay in sync with it.
 * When a new ability is added to the expansion, it must also be added here. If you forget,
 * the {@link ParserConfigCoverageTest} registry will also be incomplete.
 */
class ParserConfigKeysPresenceTest {

    /**
     * All native McRPG ability classes, mirroring {@code McRPGExpansion.createAbilities()}.
     * Keep this list synchronized when adding new abilities.
     */
    private static final List<Class<? extends Ability>> NATIVE_ABILITY_CLASSES = List.of(
            // Swords
            Bleed.class, DeeperWound.class, Vampire.class,
            EnhancedBleed.class, RageSpike.class, SerratedStrikes.class,
            // Mining
            ExtraOre.class, ItsATriple.class, RemoteTransfer.class, OreScanner.class,
            // Woodcutting
            ExtraLumber.class, HeavySwing.class, DryadsGift.class, NymphsVitality.class,
            // Herbalism
            InstantIrrigation.class, TooManyPlants.class, VerdantSurge.class, MassHarvest.class
    );

    @Test
    @DisplayName("All native ConfigurableTierableAbility implementations carry @ParserConfigKeys")
    void allTierableAbilities_haveAnnotation() {
        List<String> missingAnnotation = new ArrayList<>();

        for (Class<? extends Ability> abilityClass : NATIVE_ABILITY_CLASSES) {
            if (ConfigurableTierableAbility.class.isAssignableFrom(abilityClass)) {
                if (!abilityClass.isAnnotationPresent(ParserConfigKeys.class)) {
                    missingAnnotation.add(abilityClass.getSimpleName());
                }
            }
        }

        assertTrue(missingAnnotation.isEmpty(),
                "@ParserConfigKeys annotation missing on: " + String.join(", ", missingAnnotation));
    }
}
