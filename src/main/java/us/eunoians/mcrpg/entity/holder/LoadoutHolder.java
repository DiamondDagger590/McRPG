package us.eunoians.mcrpg.entity.holder;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.impl.Ability;
import us.eunoians.mcrpg.ability.impl.UnlockableAbility;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.MainConfigFile;
import us.eunoians.mcrpg.exception.loadout.SelectedLoadoutAboveMaxException;
import us.eunoians.mcrpg.loadout.Loadout;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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

    private final Map<Integer, Loadout> loadouts;
    private int currentLoadout;

    public LoadoutHolder(@NotNull UUID uuid) {
        super(uuid);
        this.loadouts = new HashMap<>();
        this.currentLoadout = 1;
    }

    public LoadoutHolder(@NotNull UUID uuid, int currentLoadout, Map<Integer, Loadout> loadouts) {
        super(uuid);
        this.currentLoadout = currentLoadout;
        this.loadouts = loadouts;
    }

    /**
     * Sets the current loadout slot for the holder.
     *
     * @param currentLoadout The new loadout slot for the holder.
     * @throws SelectedLoadoutAboveMaxException whenever the provided slot is greater than {@link #getMaxLoadoutAmount()}
     */
    public void setCurrentLoadoutSlot(int currentLoadout) {
        if (currentLoadout > getMaxLoadoutAmount()) {
            throw new SelectedLoadoutAboveMaxException(this, currentLoadout);
        }
        this.currentLoadout = currentLoadout;
    }

    /**
     * Gets the slot id of the currently equipped loadout.
     *
     * @return The slot id of the currently equipped loadout.
     */
    public int getCurrentLoadoutSlot() {
        return currentLoadout;
    }

    /**
     * Gets a {@link Set} of all {@link NamespacedKey}s that represent {@link Ability Abilities}
     * that are in the holder's loadout.
     *
     * @return A {@link Set} of all {@link NamespacedKey}s that represent {@link Ability Abilities}
     * that are in the holder's loadout.
     */
    @NotNull
    public Loadout getLoadout() {
        return getLoadout(currentLoadout);
    }

    /**
     * Gets the {@link Loadout} for the provided slot. If no loadout exists at
     * the provided slot, a new empty one is created.
     *
     * @param loadoutSlot The slot to get the {@link Loadout} of
     * @return The {@link Loadout} belonging to the provided slot.
     * @throws SelectedLoadoutAboveMaxException if the provided slot is greater than {@link #getMaxLoadoutAmount()}.
     */
    public Loadout getLoadout(int loadoutSlot) {
        if (loadoutSlot > getMaxLoadoutAmount()) {
            throw new SelectedLoadoutAboveMaxException(this, loadoutSlot);
        }
        if (!loadouts.containsKey(loadoutSlot)) {
            loadouts.put(loadoutSlot, new Loadout(getUUID(), loadoutSlot));
        }
        return loadouts.get(loadoutSlot);
    }

    /**
     * Sets the {@link Loadout} at the {@link Loadout#getLoadoutSlot()} slot.
     *
     * @param loadout The {@link Loadout} to set.
     * @throws SelectedLoadoutAboveMaxException if {@link Loadout#getLoadoutSlot()} is greater than {@link #getMaxLoadoutAmount()}.
     */
    public void setLoadout(@NotNull Loadout loadout) {
        int loadoutSlot = loadout.getLoadoutSlot();
        if (loadoutSlot > getMaxLoadoutAmount()) {
            throw new SelectedLoadoutAboveMaxException(this, loadoutSlot);
        }
        loadouts.put(loadoutSlot, loadout);
    }

    /**
     * Checks to see if the provided slot is valid for getting a {@link Loadout}
     * from this holder.
     *
     * @param loadoutSlot The slot to check.
     * @return {@code true} if the provided slot is valid for getting a {@link Loadout}
     * from this holder.
     */
    public boolean hasLoadout(int loadoutSlot) {
        return loadouts.containsKey(loadoutSlot) || loadoutSlot <= getMaxLoadoutAmount();
    }

    /**
     * Gets a {@link Set} of all the {@link NamespacedKey}s that represent {@link Ability Abilities}
     * that this holder can activate.
     * <p>
     * This set is a combination of all abilities inside of {@link #getLoadout()} and any 'default abilities'
     * the holder might have that don't require unlocking.
     *
     * @return A {@link Set} of all the {@link NamespacedKey}s that represent {@link Ability Abilities}
     * that this holder can activate.
     */
    public Set<NamespacedKey> getAvailableAbilitiesToUse() {
        Set<NamespacedKey> abilities = new HashSet<>(getLoadout().getAbilities());
        abilities.addAll(getAvailableDefaultAbilities());
        return abilities;
    }

    /**
     * Gets a {@link Set} of all the {@link NamespacedKey}s that represent all {@link Ability Abilities}
     * that have some sort of active action component to their activation.
     *
     * @return Gets a {@link Set} of all the {@link NamespacedKey}s that represent all {@link Ability Abilities}
     * that have some sort of active action component to their activation.
     */
    public Set<NamespacedKey> getAvailableActiveAbilities() {
        return getAvailableAbilities().stream().filter(namespacedKey -> !McRPG.getInstance().getAbilityRegistry().getRegisteredAbility(namespacedKey).isPassive()).collect(Collectors.toSet());
    }

    /**
     * Gets a {@link Set} of all the {@link NamespacedKey}s that represent all {@link Ability Abilities}
     * that are 'default abilities', or ones that don't require unlocking to use.
     *
     * @return Gets a {@link Set} of all the {@link NamespacedKey}s that represent all {@link Ability Abilities}
     * that are 'default abilities', or ones that don't require unlocking to use.
     */
    private Set<NamespacedKey> getAvailableDefaultAbilities() {
        return getAvailableAbilities().stream().filter(namespacedKey -> !(McRPG.getInstance().getAbilityRegistry().getRegisteredAbility(namespacedKey) instanceof UnlockableAbility)).collect(Collectors.toSet());
    }

    /**
     * Gets the maximum amount of {@link Loadout}s that this holder can have.
     *
     * @return The maximum amount of {@link Loadout}s that this holder can have.
     */
    public int getMaxLoadoutAmount() {
        return McRPG.getInstance().getFileManager().getFile(FileType.MAIN_CONFIG).getInt(MainConfigFile.MAX_LOADOUT_AMOUNT);
    }
}
