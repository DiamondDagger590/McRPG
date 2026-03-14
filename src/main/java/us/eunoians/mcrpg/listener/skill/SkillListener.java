package us.eunoians.mcrpg.listener.skill;

import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.MainConfigFile;
import us.eunoians.mcrpg.entity.EntityManager;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.Skill;
import us.eunoians.mcrpg.skill.SkillRegistry;
import us.eunoians.mcrpg.skill.experience.context.GainReason;
import us.eunoians.mcrpg.skill.experience.context.McRPGGainReason;
import us.eunoians.mcrpg.skill.experience.context.SkillExperienceContext;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * A generic listener that provides base implementation for any {@link Event} that
 * can be used to level up a {@link us.eunoians.mcrpg.skill.Skill}.
 */
public interface SkillListener extends Listener {

    /**
     * Attempts to pass in the provided {@link Event} to all {@link us.eunoians.mcrpg.skill.Skill}s owned
     * by the provided {@link UUID} to be parsed for leveling.
     *
     * @param uuid  The {@link UUID} of the {@link SkillHolder} to attempt to level skills for.
     * @param event The {@link Event} being passed in for leveling.
     */
    default void levelSkill(@NotNull UUID uuid, @NotNull Event event) {
        McRPG mcRPG = McRPG.getInstance();
        EntityManager entityManager = mcRPG.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.ENTITY);
        SkillRegistry skillRegistry = mcRPG.registryAccess().registry(McRPGRegistryKey.SKILL);
        entityManager.getAbilityHolder(uuid).ifPresent(abilityHolder -> {

            // Validate that the holder specific context allows for McRPG to be used here.
            if (!mcRPG.registryAccess().registry(McRPGRegistryKey.MANAGER).manager(McRPGManagerKey.WORLD).isMcRPGEnabledForHolder(abilityHolder)) {
                return;
            }

            if (abilityHolder instanceof SkillHolder skillHolder) {
                Set<NamespacedKey> allSkills = skillHolder.getSkills();

                allSkills.stream().map(skillRegistry::getRegisteredSkill)
                        .filter(skill -> skill.canEventLevelSkill(event))
                        .forEach(skill -> skillHolder.getSkillHolderData(skill).ifPresent(skillHolderData -> {
                            int exp = skill.calculateExperienceToGive(skillHolder, event);
                            if (exp > 0) {
                                // At max level: accumulate raw XP silently — no modifiers consumed,
                                // no boosted/rested XP spent, no display updates triggered
                                if (skillHolderData.getCurrentLevel() >= skill.getMaxLevel()) {
                                    skillHolderData.addExperience(exp, McRPGGainReason.OTHER);
                                    return;
                                }
                                var eventContextOptional = getEventContext(skillHolder, skill, exp, event);
                                GainReason gainReason = McRPGGainReason.OTHER;
                                if (eventContextOptional.isPresent()) {
                                    SkillExperienceContext<?> context = eventContextOptional.get();
                                    double modifier = Math.min(mcRPG.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE).getFile(FileType.MAIN_CONFIG).getDouble(MainConfigFile.EXPERIENCE_MULTIPLIER_LIMIT), mcRPG.registryAccess().registry(McRPGRegistryKey.EXPERIENCE_MODIFIER).calculateModifierForContext(context));
                                    exp = (int) (exp * modifier);
                                    gainReason = context.getGainReason();
                                }
                                skillHolderData.addExperience(exp, gainReason);
                            }
                        }));
            }
        });
    }

    /**
     * Get an {@link Optional} containing {@link SkillExperienceContext} for an {@link Event} that this listener handles.
     *
     * @param skillHolder The {@link SkillHolder} who the context is about.
     * @param skill       The {@link Skill} gaining experience in this context.
     * @param experience  The base amount of experience being given in this context.
     * @param event       The {@link Event} that is creating the context.
     * @return An {@link Optional} containing {@link SkillExperienceContext} for an {@link Event} that this listener handles.
     */
    @NotNull
    Optional<SkillExperienceContext<?>> getEventContext(@NotNull SkillHolder skillHolder, @NotNull Skill skill, int experience, @NotNull Event event);
}
