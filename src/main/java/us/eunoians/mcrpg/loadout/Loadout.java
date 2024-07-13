package us.eunoians.mcrpg.loadout;

import com.google.common.collect.ImmutableSet;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.impl.Ability;
import us.eunoians.mcrpg.ability.impl.ActiveAbility;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.MainConfigFile;
import us.eunoians.mcrpg.exception.loadout.LoadoutAlreadyHasActiveAbilityException;
import us.eunoians.mcrpg.exception.loadout.LoadoutMaxSizeExceededException;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Loadout {

    private final UUID loadoutHolder;
    private final int loadoutSlot;
    private final Set<NamespacedKey> abilities;

    public Loadout(@NotNull UUID loadoutHolder, int loadoutSlot) {
        this.loadoutHolder = loadoutHolder;
        this.loadoutSlot = loadoutSlot;
        this.abilities = new HashSet<>();
    }

    public Loadout(@NotNull UUID loadoutHolder, int loadoutSlot, @NotNull Set<NamespacedKey> abilities) {
        this.loadoutHolder = loadoutHolder;
        this.loadoutSlot = loadoutSlot;
        this.abilities = abilities;
    }

    @NotNull
    public UUID getLoadoutHolder() {
        return loadoutHolder;
    }

    public int getLoadoutSlot() {
        return loadoutSlot;
    }

    public void addAbility(@NotNull NamespacedKey key) {
        if (abilities.size() >= getMaxLoadoutSize()) {
            throw new LoadoutMaxSizeExceededException(this, String.format("Loadout %d for user %s tried to exceed the maximum loadout size of %d. The current loadout size is %d",
                    loadoutSlot, loadoutHolder, getMaxLoadoutSize(), abilities.size()));
        }
        if (!canAbilityBeInLoadout(key)) {
            throw new LoadoutAlreadyHasActiveAbilityException(this, key, String.format("Loadout %d for user %s already has an active ability with the same skill as %s.", loadoutSlot, loadoutHolder, key));
        }
        abilities.add(key);
    }

    public void removeAbility(@NotNull NamespacedKey key) {
        abilities.remove(key);
    }

    public boolean isAbilityInLoadout(@NotNull NamespacedKey key) {
        return abilities.contains(key);
    }

    public boolean canAbilityBeInLoadout(@NotNull NamespacedKey key) {
        Ability ability = McRPG.getInstance().getAbilityRegistry().getRegisteredAbility(key);
        if (ability instanceof ActiveAbility && ability.getSkill().isPresent()) {
            NamespacedKey skillKey = ability.getSkill().get();
            for (NamespacedKey abilityKey : abilities) {
                Ability abilityInLoadout = McRPG.getInstance().getAbilityRegistry().getRegisteredAbility(abilityKey);
                if (abilityInLoadout instanceof ActiveAbility && abilityInLoadout.getSkill().isPresent() && abilityInLoadout.getSkill().get().equals(skillKey)) {
                    return false;
                }
            }
        }
        return true;
    }

    @NotNull
    public Set<NamespacedKey> getAbilities() {
        return ImmutableSet.copyOf(abilities);
    }

    public int getRemainingLoadoutSize() {
        return getMaxLoadoutSize() - abilities.size();
    }

    @NotNull
    public Loadout copyLoadout(@NotNull UUID loadoutHolder, int loadoutSlot) {
        return new Loadout(loadoutHolder, loadoutSlot, new HashSet<>(abilities));
    }

    private int getMaxLoadoutSize() {
        return McRPG.getInstance().getFileManager().getFile(FileType.MAIN_CONFIG).getInt(MainConfigFile.MAX_LOADOUT_SIZE);
    }
}
