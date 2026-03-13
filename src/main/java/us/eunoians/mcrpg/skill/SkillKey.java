package us.eunoians.mcrpg.skill;

import org.jetbrains.annotations.NotNull;

/**
 * A key that allows type-safe access to a {@link Skill} through the {@link SkillRegistry}.
 * <p>
 * To access a skill, users will need to call {@link com.diamonddagger590.mccore.registry.RegistryAccess#registryAccess()}
 * and provide {@link us.eunoians.mcrpg.registry.McRPGRegistryKey#SKILL} to get back the {@link SkillRegistry}.
 * <p>
 * From there, users can call {@link SkillRegistry#skill(SkillKey)} to get the skill belonging to the
 * provided key without requiring any casting.
 * <p>
 * Example usage:
 * <pre>{@code
 * // Before (with casting):
 * Swords swords = (Swords) McRPG.getInstance()
 *     .registryAccess()
 *     .registry(McRPGRegistryKey.SKILL)
 *     .getRegisteredSkill(Swords.SWORDS_KEY);
 *
 * // After (with typed key):
 * Swords swords = McRPG.getInstance()
 *     .registryAccess()
 *     .registry(McRPGRegistryKey.SKILL)
 *     .skill(McRPGSkillKey.SWORDS);
 * }</pre>
 *
 * @param <S> The {@link Skill} being represented by this key.
 */
public interface SkillKey<S extends Skill> {

    /**
     * Gets the {@link Class} of the {@link Skill} represented by this key.
     *
     * @return The {@link Class} of the {@link Skill} represented by this key.
     */
    @NotNull
    Class<S> skillClass();
}
