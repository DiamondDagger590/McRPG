package us.eunoians.mcrpg.loadout;

import com.google.common.collect.ImmutableSet;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.impl.Ability;
import us.eunoians.mcrpg.ability.impl.ActiveAbility;
import us.eunoians.mcrpg.ability.impl.UnlockableAbility;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.MainConfigFile;
import us.eunoians.mcrpg.exception.loadout.LoadoutAlreadyHasActiveAbilityException;
import us.eunoians.mcrpg.exception.loadout.LoadoutMaxSizeExceededException;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * A loadout is a collection of {@link Ability Abilities} that a {@link us.eunoians.mcrpg.entity.holder.LoadoutHolder}
 * can use. Only abilities in a loadout are usable for the holder, even if they might have abilities that aren't in the loadout.
 * <p>
 * A {@link us.eunoians.mcrpg.entity.holder.LoadoutHolder} also can possess multiple loadouts. Each loadout comes with an id that
 * is tied to its holder, representing the slot of the loadout for that holder.
 * <p>
 * A loadout also has a restriction where it can only possess one {@link ActiveAbility} per {@link us.eunoians.mcrpg.skill.Skill}.
 */
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

    /**
     * Gets the {@link UUID} of the {@link us.eunoians.mcrpg.entity.holder.LoadoutHolder} that
     * owns this loadout.
     *
     * @return The {@link UUID} of the {@link us.eunoians.mcrpg.entity.holder.LoadoutHolder} that
     * owns this loadout.
     */
    @NotNull
    public UUID getLoadoutHolder() {
        return loadoutHolder;
    }

    /**
     * Gets the numerical slot id of this loadout for the owning {@link us.eunoians.mcrpg.entity.holder.LoadoutHolder}.
     *
     * @return The numerical slot id of this loadout for the owning {@link us.eunoians.mcrpg.entity.holder.LoadoutHolder}.
     */
    public int getLoadoutSlot() {
        return loadoutSlot;
    }

    /**
     * Adds the {@link NamespacedKey} to this loadout.
     *
     * @param key The {@link NamespacedKey} corresponding to the {@link Ability} to add to this loadout.
     * @throws LoadoutMaxSizeExceededException         If the loadout is at or above the {@link #getMaxLoadoutSize()}.
     * @throws LoadoutAlreadyHasActiveAbilityException If the loadout already has an {@link ActiveAbility} for the {@link us.eunoians.mcrpg.skill.Skill}
     *                                                 belonging to the ability.
     */
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

    /**
     * Removes the provided {@link NamespacedKey} from this loadout.
     *
     * @param key The {@link NamespacedKey} to remove.
     */
    public void removeAbility(@NotNull NamespacedKey key) {
        abilities.remove(key);
    }

    /**
     * Removes an existing ability to replace it with a new one.
     *
     * @param oldAbility The {@link NamespacedKey} to remove.
     * @param newAbility The {@link NamespacedKey} to add.
     */
    public void replaceAbility(@NotNull NamespacedKey oldAbility, @NotNull NamespacedKey newAbility) {
        removeAbility(oldAbility);
        addAbility(newAbility);
    }

    /**
     * Checks to see if the provided {@link NamespacedKey} is in the loadout.
     *
     * @param key The {@link NamespacedKey} to check.
     * @return {@code true} if the provided {@link NamespacedKey} is in this loadout.
     */
    public boolean isAbilityInLoadout(@NotNull NamespacedKey key) {
        return abilities.contains(key);
    }

    /**
     * Checks to see if the provided {@link NamespacedKey} can be added to this loadout.
     *
     * @param key The {@link NamespacedKey} to check.
     * @return {@code true} if the provided {@link NamespacedKey} can be added to this loadout.
     */
    // TODO this shit broken
    public boolean canAbilityBeInLoadout(@NotNull NamespacedKey key) {
        Ability ability = McRPG.getInstance().getAbilityRegistry().getRegisteredAbility(key);
        // Check if it's a default ability
        if (!(ability instanceof UnlockableAbility unlockableAbility)) {
            return false;
        }
        for (NamespacedKey abilityKey : abilities) {
            if (ability instanceof ActiveAbility && ability.getSkill().isPresent()) {
                NamespacedKey skillKey = ability.getSkill().get();
                Ability abilityInLoadout = McRPG.getInstance().getAbilityRegistry().getRegisteredAbility(abilityKey);
                // Check for active abilities in the same skill
                if (abilityInLoadout instanceof ActiveAbility && abilityInLoadout.getSkill().isPresent() && abilityInLoadout.getSkill().get().equals(skillKey)) {
                    return false;
                }
            }
            // Check for same ability
            else if (abilityKey.equals(key)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets an {@link ImmutableSet} of {@link NamespacedKey}s for all abilities in this loadout.
     *
     * @return An {@link ImmutableSet} of {@link NamespacedKey} for all abilities in this loadout.
     */
    @NotNull
    public Set<NamespacedKey> getAbilities() {
        return ImmutableSet.copyOf(abilities);
    }

    /**
     * Gets the remaining amount of abilities that can be added to this loadout.
     *
     * @return The remaining amount of abilities that can be added to this loadout.
     */
    public int getRemainingLoadoutSize() {
        return getMaxLoadoutSize() - abilities.size();
    }

    /**
     * Creates a copy of the provided loadout that is owned by the provided uuid.
     *
     * @param loadoutHolder The uuid of the {@link us.eunoians.mcrpg.entity.holder.LoadoutHolder} that will own the new loadout.
     * @param loadoutSlot   The slot of the loadout for the {@link us.eunoians.mcrpg.entity.holder.LoadoutHolder}
     * @return A copy of the provided loadout that is owned by the provided uuid.
     */
    @NotNull
    public Loadout copyLoadout(@NotNull UUID loadoutHolder, int loadoutSlot) {
        return new Loadout(loadoutHolder, loadoutSlot, new HashSet<>(abilities));
    }

    /**
     * Gets the maximum size of a loadout.
     *
     * @return The maximum size of a loadout.
     */
    private int getMaxLoadoutSize() {
        return McRPG.getInstance().getFileManager().getFile(FileType.MAIN_CONFIG).getInt(MainConfigFile.MAX_LOADOUT_SIZE);
    }
}
