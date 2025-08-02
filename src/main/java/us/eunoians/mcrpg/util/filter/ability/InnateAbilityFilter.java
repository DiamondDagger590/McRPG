package us.eunoians.mcrpg.util.filter.ability;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeRegistry;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.util.filter.core.McRPGPlayerContextFilter;

import java.util.Collection;

/**
 * This filter is used to filter a collection of {@link Ability Abilities} so only abilities that aren't unlocked are
 * left.
 */
public class InnateAbilityFilter implements McRPGPlayerContextFilter<Ability> {

    @NotNull
    @Override
    public Collection<Ability> filter(@NotNull McRPGPlayer mcRPGPlayer, @NotNull Collection<Ability> collection) {
        SkillHolder skillHolder = mcRPGPlayer.asSkillHolder();
        return collection = collection.stream()
                .filter(ability -> {
                    var abilityData = skillHolder.getAbilityData(ability);
                    if (abilityData.isPresent()) {
                        var unlockedAttribute = abilityData.get().getAbilityAttribute(AbilityAttributeRegistry.ABILITY_UNLOCKED_ATTRIBUTE);
                        return unlockedAttribute.isEmpty();
                    }
                    return false;
                }).toList();
    }
}
