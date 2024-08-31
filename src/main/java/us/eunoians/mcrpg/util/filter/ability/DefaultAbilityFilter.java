package us.eunoians.mcrpg.util.filter.ability;

import com.diamonddagger590.mccore.player.CorePlayer;
import com.diamonddagger590.mccore.util.PlayerContextFilter;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.impl.Ability;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeManager;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;

import java.util.Collection;

/**
 * This filter is used to filter a collection of {@link Ability Abilities} so only abilities that aren't unlocked are
 * left.
 */
public class DefaultAbilityFilter implements PlayerContextFilter<Ability> {

    @Override
    public Collection<Ability> filter(@NotNull CorePlayer corePlayer, @NotNull Collection<Ability> collection) {
        if (corePlayer instanceof McRPGPlayer mcRPGPlayer) {
            SkillHolder skillHolder = mcRPGPlayer.asSkillHolder();
            collection = collection.stream()
                    .filter(ability -> {
                        var abilityData = skillHolder.getAbilityData(ability);
                        if (abilityData.isPresent()) {
                            var unlockedAttribute = abilityData.get().getAbilityAttribute(AbilityAttributeManager.ABILITY_UNLOCKED_ATTRIBUTE);
                            return unlockedAttribute.isEmpty();
                        }
                        return false;
                    }).toList();
        }
        return collection;
    }
}
