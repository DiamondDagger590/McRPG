package us.eunoians.mcrpg.util.filter.key;

import com.diamonddagger590.mccore.player.CorePlayer;
import com.diamonddagger590.mccore.util.PlayerContextFilter;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * A {@link PlayerContextFilter} that filters out all {@link us.eunoians.mcrpg.ability.impl.Ability Abilities} that can't be added to the
 * {@link McRPGPlayer}'s {@link us.eunoians.mcrpg.loadout.Loadout}.
 */
public class AbilityKeyInLoadoutFilter implements PlayerContextFilter<NamespacedKey> {

    @Override
    public Collection<NamespacedKey> filter(@NotNull CorePlayer corePlayer, @NotNull Collection<NamespacedKey> list) {
        if (corePlayer instanceof McRPGPlayer mcRPGPlayer) {
            return list.stream().filter(namespacedKey -> mcRPGPlayer.asSkillHolder().getLoadout().canAbilityBeInLoadout(namespacedKey)).collect(Collectors.toSet());
        }
        return list;
    }
}
