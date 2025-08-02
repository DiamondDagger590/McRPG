package us.eunoians.mcrpg.util.filter.skill;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.skill.Skill;
import us.eunoians.mcrpg.skill.SkillRegistry;
import us.eunoians.mcrpg.util.filter.core.McRPGPlayerContextFilter;

import java.util.Collection;

/**
 * This filter filters out any {@link Skill}s for which a given {@link McRPGPlayer}
 * doesn't have {@link us.eunoians.mcrpg.entity.holder.SkillHolder.SkillHolderData} for.
 */
public class SkillHolderDataPresentFilter implements McRPGPlayerContextFilter<Skill> {

    @NotNull
    @Override
    public Collection<Skill> filter(@NotNull McRPGPlayer mcRPGPlayer, @NotNull Collection<Skill> collection) {
        SkillHolder skillHolder = mcRPGPlayer.asSkillHolder();
        SkillRegistry skillRegistry = RegistryAccess.registryAccess().registry(McRPGRegistryKey.SKILL);
        return skillRegistry.getRegisteredSkills()
                .stream()
                .filter(skill -> skillHolder.getSkillHolderData(skill).isPresent())
                .toList();
    }
}
