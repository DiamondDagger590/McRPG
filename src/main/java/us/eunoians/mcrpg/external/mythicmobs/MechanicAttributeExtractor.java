package us.eunoians.mcrpg.external.mythicmobs;

import io.lumine.mythic.api.config.MythicLineConfig;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.attribute.AbilityAttribute;

import java.util.Optional;

/**
 * Extracts an {@link AbilityAttribute} from a MythicMobs {@link MythicLineConfig}
 * for the {@code mcrpg_ability} mechanic.
 * <p>
 * McRPG ships a built-in extractor for the {@code tier} config parameter. Third-party
 * plugins can register additional extractors via
 * {@link MythicMobsHook#registerMechanicAttributeExtractor(String, MechanicAttributeExtractor)}
 * to support custom ability metadata (e.g., {@code charge_time}, {@code element}).
 * <p>
 * Extractors are invoked during {@link McRPGAbilityMechanic} construction and during
 * {@link MythicMobAbilityParser} spawn-time parsing, so the mechanic and parser both
 * pick up custom attributes automatically.
 */
@FunctionalInterface
public interface MechanicAttributeExtractor {

    /**
     * Attempts to extract an {@link AbilityAttribute} from the given MythicMobs line config.
     * Returns {@link Optional#empty()} if the config does not contain a value for this
     * extractor's parameter, signaling that the attribute should not be attached.
     *
     * @param config The MythicMobs line config from the {@code mcrpg_ability} mechanic
     * @return An {@link Optional} containing the extracted attribute, or empty if not present
     */
    @NotNull
    Optional<AbilityAttribute<?>> extract(@NotNull MythicLineConfig config);
}
