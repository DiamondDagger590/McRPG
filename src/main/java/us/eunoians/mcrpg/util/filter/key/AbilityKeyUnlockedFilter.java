package us.eunoians.mcrpg.util.filter.key;

import com.diamonddagger590.mccore.player.CorePlayer;
import com.diamonddagger590.mccore.util.PlayerContextFilter;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.impl.UnlockableAbility;

import java.util.Collection;

/**
 * A {@link PlayerContextFilter} that filters out all {@link us.eunoians.mcrpg.ability.impl.Ability Abilities}
 * that aren't unlockable abilities.
 */
public class AbilityKeyUnlockedFilter implements PlayerContextFilter<NamespacedKey> {

    @Override
    public Collection<NamespacedKey> filter(@NotNull CorePlayer corePlayer, @NotNull Collection<NamespacedKey> collection) {
        return collection.stream()
                .filter(namespacedKey -> McRPG.getInstance().getAbilityRegistry().isAbilityRegistered(namespacedKey))
                .filter(namespacedKey -> McRPG.getInstance().getAbilityRegistry().getRegisteredAbility(namespacedKey) instanceof UnlockableAbility)
                .toList();
    }
}
