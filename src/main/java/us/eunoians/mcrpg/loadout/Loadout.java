package us.eunoians.mcrpg.loadout;

import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.impl.type.ActiveAbility;
import us.eunoians.mcrpg.ability.impl.type.SkillAbility;
import us.eunoians.mcrpg.ability.impl.type.UnlockableAbility;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.MainConfigFile;
import us.eunoians.mcrpg.exception.loadout.InvalidAbilityForLoadoutException;
import us.eunoians.mcrpg.exception.loadout.LoadoutMaxSizeExceededException;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * A loadout is a collection of {@link Ability Abilities} that a {@link us.eunoians.mcrpg.entity.holder.LoadoutHolder}
 * can use. Only abilities in a loadout are usable for the holder, even if they might have abilities that aren't in the loadout.
 * <p>
 * A {@link us.eunoians.mcrpg.entity.holder.LoadoutHolder} also can possess multiple loadouts. Each loadout comes with an id that
 * is tied to its holder, representing the slot of the loadout for that holder.
 * <p>
 * Abilities are stored in insertion order. This order is persisted in the database via a {@code slot_number} column
 * so that the player's preferred arrangement is preserved across sessions. When two abilities are both already present
 * in the loadout, {@link #replaceAbility(NamespacedKey, NamespacedKey)} swaps their positions rather than removing one.
 */
public final class Loadout {

    private final UUID loadoutHolder;
    private final int loadoutSlot;
    private final List<NamespacedKey> abilities;
    @NotNull
    private LoadoutDisplay loadoutDisplay;

    public Loadout(@NotNull UUID loadoutHolder, int loadoutSlot) {
        this.loadoutHolder = loadoutHolder;
        this.loadoutSlot = loadoutSlot;
        this.abilities = new ArrayList<>();
        this.loadoutDisplay = getDefaultDisplayItem();
    }

    public Loadout(@NotNull UUID loadoutHolder, int loadoutSlot, @NotNull Set<NamespacedKey> abilities) {
        this.loadoutHolder = loadoutHolder;
        this.loadoutSlot = loadoutSlot;
        this.abilities = new ArrayList<>(abilities);
        this.loadoutDisplay = getDefaultDisplayItem();
    }

    public Loadout(@NotNull UUID loadoutHolder, int loadoutSlot, @NotNull Set<NamespacedKey> abilities, @NotNull LoadoutDisplay loadoutDisplay) {
        this.loadoutHolder = loadoutHolder;
        this.loadoutSlot = loadoutSlot;
        this.abilities = new ArrayList<>(abilities);
        this.loadoutDisplay = loadoutDisplay;
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
     * Adds the {@link NamespacedKey} to this loadout at the next available position.
     *
     * @param key The {@link NamespacedKey} corresponding to the {@link Ability} to add to this loadout.
     * @throws LoadoutMaxSizeExceededException   If the loadout is at or above the {@link #getMaxLoadoutSize()}.
     * @throws InvalidAbilityForLoadoutException If the ability cannot be added (already present or exceeds the active ability limit).
     */
    public void addAbility(@NotNull NamespacedKey key) {
        if (abilities.size() >= getMaxLoadoutSize()) {
            throw new LoadoutMaxSizeExceededException(this, String.format("Loadout %d for user %s tried to exceed the maximum loadout size of %d. The current loadout size is %d",
                    loadoutSlot, loadoutHolder, getMaxLoadoutSize(), abilities.size()));
        }
        if (!canAbilityBeAddedToLoadout(key)) {
            throw new InvalidAbilityForLoadoutException(this, key, String.format("Loadout %d for user %s cannot add %s: ability is already present or the maximum number of active abilities has been reached.",
                    loadoutSlot, loadoutHolder, key));
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
     * Replaces an existing ability in the loadout with a new one, preserving positions.
     * <p>
     * If {@code newAbility} is already present in the loadout at a different position, the two abilities
     * swap positions — both remain in the loadout. Otherwise, {@code newAbility} is inserted at the
     * position previously held by {@code oldAbility}.
     *
     * @param oldAbility The {@link NamespacedKey} to replace.
     * @param newAbility The {@link NamespacedKey} to place into the loadout.
     * @throws InvalidAbilityForLoadoutException If {@link #canAbilityBeReplacedIntoLoadout(NamespacedKey, NamespacedKey)}
     *                                           returns {@code false} for the given pair.
     */
    public void replaceAbility(@NotNull NamespacedKey oldAbility, @NotNull NamespacedKey newAbility) {
        if (!canAbilityBeReplacedIntoLoadout(oldAbility, newAbility)) {
            throw new InvalidAbilityForLoadoutException(this, newAbility, String.format("Loadout %d for user %s tried to replace %s with %s, but the replacement is not valid.",
                    loadoutSlot, loadoutHolder, oldAbility, newAbility));
        }
        int oldIndex = abilities.indexOf(oldAbility);
        int newIndex = abilities.indexOf(newAbility);
        if (newIndex != -1) {
            // Both abilities are already in the loadout — swap their positions.
            abilities.set(oldIndex, newAbility);
            abilities.set(newIndex, oldAbility);
        } else {
            // Normal replacement: put the new ability at the position previously held by the old one.
            abilities.set(oldIndex, newAbility);
        }
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
    public boolean canAbilityBeAddedToLoadout(@NotNull NamespacedKey key) {
        Ability ability = McRPG.getInstance().registryAccess().registry(McRPGRegistryKey.ABILITY).getRegisteredAbility(key);
        if (!(ability instanceof UnlockableAbility)) {
            return false;
        }
        if (abilities.contains(key)) {
            return false;
        }
        if (ability instanceof ActiveAbility) {
            long activeCount = abilities.stream()
                    .map(k -> McRPG.getInstance().registryAccess().registry(McRPGRegistryKey.ABILITY).getRegisteredAbility(k))
                    .filter(a -> a instanceof ActiveAbility)
                    .count();
            return activeCount < getMaxActiveLoadoutSize();
        }
        return true;
    }

    /**
     * Gets the maximum number of {@link ActiveAbility ActiveAbilities} allowed in this loadout.
     *
     * @return The maximum active ability count from config.
     */
    private int getMaxActiveLoadoutSize() {
        return McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE).getFile(FileType.MAIN_CONFIG).getInt(MainConfigFile.MAX_ACTIVE_LOADOUT_SIZE, 3);
    }

    /**
     * Checks to see if the provided ability key that is being replaced can be replaced by the new ability key.
     * <p>
     * A replacement is considered valid if:
     * <ul>
     *   <li>The new ability is already in the loadout at a different position (positional swap).</li>
     *   <li>The old and new abilities are both active abilities belonging to the same skill (skill-slot swap).</li>
     *   <li>The new ability passes {@link #canAbilityBeAddedToLoadout(NamespacedKey)} (fresh addition at this slot).</li>
     * </ul>
     *
     * @param oldAbilityKey The old ability key that is being replaced.
     * @param newAbilityKey The new ability key that is replacing the old ability key.
     * @return {@code true} if the provided ability key that is being replaced can be replaced by the new ability key.
     */
    public boolean canAbilityBeReplacedIntoLoadout(@NotNull NamespacedKey oldAbilityKey, @NotNull NamespacedKey newAbilityKey) {
        Ability newAbility = McRPG.getInstance().registryAccess().registry(McRPGRegistryKey.ABILITY).getRegisteredAbility(newAbilityKey);
        Ability oldAbility = McRPG.getInstance().registryAccess().registry(McRPGRegistryKey.ABILITY).getRegisteredAbility(oldAbilityKey);
        if (!(newAbility instanceof UnlockableAbility)) {
            return false;
        } else if (oldAbilityKey.equals(newAbilityKey)) {
            return false;
        }
        // If the new ability is already in the loadout at a different position, the operation is a positional swap.
        if (abilities.contains(newAbilityKey)) {
            return true;
        }
        // If both abilities are active abilities belonging to the same skill, allow the replacement (skill-slot swap).
        if (oldAbility instanceof ActiveAbility && oldAbility instanceof SkillAbility oldSkillAbility
                && newAbility instanceof ActiveAbility && newAbility instanceof SkillAbility newSkillAbility
                && oldSkillAbility.getSkillKey().equals(newSkillAbility.getSkillKey())) {
            return true;
        }
        return canAbilityBeAddedToLoadout(newAbilityKey);
    }

    /**
     * Gets a {@link Set} of {@link NamespacedKey}s for all abilities in this loadout.
     * <p>
     * The returned set is unordered. To iterate abilities in their stored slot order, use {@link #getOrderedAbilities()}.
     *
     * @return An unordered {@link Set} of {@link NamespacedKey}s for all abilities in this loadout.
     */
    @NotNull
    public Set<NamespacedKey> getAbilities() {
        return Set.copyOf(abilities);
    }

    /**
     * Gets the abilities in this loadout in their stored slot order.
     * <p>
     * The order reflects the position each ability occupies in the loadout GUI. Use this when the position
     * of each ability matters (e.g., saving to the database or rendering in slot order).
     *
     * @return An ordered, unmodifiable {@link List} of {@link NamespacedKey}s for all abilities in this loadout.
     */
    @NotNull
    public List<NamespacedKey> getOrderedAbilities() {
        return List.copyOf(abilities);
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
     * Creates a copy of the provided loadout that is owned by the provided uuid, preserving ability order.
     *
     * @param loadoutHolder The uuid of the {@link us.eunoians.mcrpg.entity.holder.LoadoutHolder} that will own the new loadout.
     * @param loadoutSlot   The slot of the loadout for the {@link us.eunoians.mcrpg.entity.holder.LoadoutHolder}
     * @return A copy of the provided loadout that is owned by the provided uuid.
     */
    @NotNull
    public Loadout copyLoadout(@NotNull UUID loadoutHolder, int loadoutSlot) {
        Loadout copy = new Loadout(loadoutHolder, loadoutSlot);
        copy.abilities.addAll(this.abilities);
        return copy;
    }

    /**
     * Gets the {@link LoadoutDisplay} used to display this loadout.
     *
     * @return The {@link LoadoutDisplay} used to display this loadout.
     */
    @NotNull
    public LoadoutDisplay getDisplay() {
        return loadoutDisplay;
    }

    /**
     * Sets the {@link LoadoutDisplay} used to display this loadout.
     *
     * @param loadoutDisplay The {@link LoadoutDisplay} used to display this loadout.
     */
    public void setLoadoutDisplay(@NotNull LoadoutDisplay loadoutDisplay) {
        this.loadoutDisplay = loadoutDisplay;
    }

    /**
     * Checks to see if the {@link LoadoutDisplay} for this loadout needs to be saved.
     *
     * @return {@code true} if the {@link LoadoutDisplay} for this loadout needs to be saved.
     */
    public boolean shouldSaveDisplay() {
        return !loadoutDisplay.equals(getDefaultDisplayItem());
    }

    /**
     * Gets the maximum size of a loadout.
     *
     * @return The maximum size of a loadout.
     */
    private int getMaxLoadoutSize() {
        return McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE).getFile(FileType.MAIN_CONFIG).getInt(MainConfigFile.MAX_LOADOUT_SIZE);
    }

    /**
     * Gets the default {@link LoadoutDisplay} for loadouts.
     *
     * @return The default {@link LoadoutDisplay} for loadouts.
     */
    private LoadoutDisplay getDefaultDisplayItem() {
        return new LoadoutDisplay(Material.CHERRY_SIGN, "<gray>Loadout <gold>" + getLoadoutSlot());
    }

}
