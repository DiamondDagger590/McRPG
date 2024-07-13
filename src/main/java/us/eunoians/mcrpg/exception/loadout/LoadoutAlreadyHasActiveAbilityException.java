package us.eunoians.mcrpg.exception.loadout;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.loadout.Loadout;

public class LoadoutAlreadyHasActiveAbilityException extends RuntimeException {

    private final Loadout loadout;
    private final NamespacedKey abilityKey;

    public LoadoutAlreadyHasActiveAbilityException(@NotNull Loadout loadout, @NotNull NamespacedKey abilityKey) {
        this.loadout = loadout;
        this.abilityKey = abilityKey;
    }
    public LoadoutAlreadyHasActiveAbilityException(@NotNull Loadout loadout, @NotNull NamespacedKey abilityKey, @NotNull String message) {
        super(message);
        this.loadout = loadout;
        this.abilityKey = abilityKey;
    }

    @NotNull
    public Loadout getLoadout() {
        return loadout;
    }

    @NotNull
    public NamespacedKey getAbilityKey() {
        return abilityKey;
    }
}
