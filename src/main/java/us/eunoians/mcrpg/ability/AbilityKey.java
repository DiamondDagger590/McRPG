package us.eunoians.mcrpg.ability;

import org.jetbrains.annotations.NotNull;

/**
 * A key that allows type-safe access to an {@link Ability} through the {@link AbilityRegistry}.
 * <p>
 * To access an ability, users will need to call {@link RegistryAccess#registryAccess()} and provide
 * {@link us.eunoians.mcrpg.registry.McRPGRegistryKey#ABILITY} to get back the {@link AbilityRegistry}.
 * <p>
 * From there, users can call {@link AbilityRegistry#ability(AbilityKey)} to get the ability belonging to the
 * provided key without requiring any casting.
 * <p>
 * Example usage:
 * <pre>{@code
 * // Before (with casting):
 * NymphsVitality nymphsVitality = (NymphsVitality) McRPG.getInstance()
 *     .registryAccess()
 *     .registry(McRPGRegistryKey.ABILITY)
 *     .getRegisteredAbility(NymphsVitality.NYMPHS_VITALITY_KEY);
 *
 * // After (with typed key):
 * NymphsVitality nymphsVitality = McRPG.getInstance()
 *     .registryAccess()
 *     .registry(McRPGRegistryKey.ABILITY)
 *     .ability(McRPGAbilityKey.NYMPHS_VITALITY);
 * }</pre>
 *
 * @param <A> The {@link Ability} being represented by this key.
 */
public interface AbilityKey<A extends Ability> {

    /**
     * Gets the {@link Class} of the {@link Ability} represented by this key.
     *
     * @return The {@link Class} of the {@link Ability} represented by this key.
     */
    @NotNull
    Class<A> abilityClass();
}
