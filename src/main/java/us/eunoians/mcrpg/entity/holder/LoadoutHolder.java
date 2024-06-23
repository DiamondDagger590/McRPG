package us.eunoians.mcrpg.entity.holder;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.impl.Ability;
import us.eunoians.mcrpg.ability.impl.UnlockableAbility;

import java.util.Set;
import java.util.UUID;

/**
 * A loadout holder is a more specific type of {@link AbilityHolder}. A loadout
 * holder has a specific set of abilities in what is called a "loadout". These abilities
 * are the only ones that can be activated for the holder, even though they may have other
 * abilities unlocked.
 * <p>
 * A loadout can only hold {@link UnlockableAbility UnlockableAbilities},
 * so it should only be treated as a representation of all unlocked abilities that a holder can use.
 * <p>
 * To get ALL abilities a holder can use (including 'default abilities'), use {@link #getAvailableAbilitiesToUse()}.
 */
public class LoadoutHolder extends AbilityHolder {

    public LoadoutHolder(@NotNull UUID uuid) {
        super(uuid);
    }

    /**
     * Gets a {@link Set} of all {@link NamespacedKey}s that represent {@link Ability Abilities}
     * that are in the holder's loadout.
     *
     * @return A {@link Set} of all {@link NamespacedKey}s that represent {@link Ability Abilities}
     * that are in the holder's loadout.
     */
    public Set<NamespacedKey> getAbilitiesInLoadout() {
        return getAvailableAbilities();
    }

    /**
     * Gets a {@link Set} of all the {@link NamespacedKey}s that represent {@link Ability Abilities}
     * that this holder can activate.
     * <p>
     * This set is a combination of all abilities inside of {@link #getAbilitiesInLoadout()} and any 'default abilities'
     * the holder might have that don't require unlocking.
     *
     * @return A {@link Set} of all the {@link NamespacedKey}s that represent {@link Ability Abilities}
     * that this holder can activate.
     */
    public Set<NamespacedKey> getAvailableAbilitiesToUse() {
        //TODO properly implement
        return getAvailableAbilities();
    }
}
