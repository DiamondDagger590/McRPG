package us.eunoians.mcrpg.ability;

import org.bukkit.NamespacedKey;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

/**
 *
 * An ability doesn't always belong to a skill, while a skill will always have abilities
 * tied to it.
 */
public abstract class Ability implements Listener {

    private final NamespacedKey abilityKey;

    public Ability(@NotNull NamespacedKey abilityKey){
        this.abilityKey = abilityKey;
    }

    @NotNull
    public NamespacedKey getAbilityKey() {
        return abilityKey;
    }
}
