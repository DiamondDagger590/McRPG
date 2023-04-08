package us.eunoians.mcrpg.entity.holder;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;

//TODO javadoc
public class LoadoutHolder extends AbilityHolder {

    public LoadoutHolder(@NotNull UUID uuid) {
        super(uuid);
    }

    //TODO actually implement these, these are just stubbed for now
    public Set<NamespacedKey> getAbilitiesInLoadout() {
        return getAvailableAbilities();
    }

    public Set<NamespacedKey> getAvailableAbilitiesToUse() {
        return getAvailableAbilities();
    }
}
