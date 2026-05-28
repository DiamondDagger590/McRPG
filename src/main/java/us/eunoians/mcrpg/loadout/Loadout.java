package us.eunoians.mcrpg.loadout;

import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.AbilityType;
import us.eunoians.mcrpg.ability.combo.ComboActivatable;
import us.eunoians.mcrpg.ability.impl.type.SkillAbility;
import us.eunoians.mcrpg.ability.impl.type.UnlockableAbility;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.MainConfigFile;
import us.eunoians.mcrpg.event.loadout.LoadoutAbilityChangeEvent;
import us.eunoians.mcrpg.event.loadout.LoadoutAbilityChangeEvent.ChangeReason;
import us.eunoians.mcrpg.event.loadout.LoadoutPositionSwapEvent;
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
     * Internal — callers outside this class must use {@link #equipAbility(NamespacedKey)}.
     *
     * @param key The {@link NamespacedKey} corresponding to the {@link Ability} to add to this loadout.
     * @throws LoadoutMaxSizeExceededException   If the loadout is at or above the {@link #getMaxLoadoutSize()}.
     * @throws InvalidAbilityForLoadoutException If the ability cannot be added (already present or exceeds the active ability limit).
     */
    private void addAbility(@NotNull NamespacedKey key) {
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
     * Internal — callers outside this class must use {@link #unequipAbility(NamespacedKey)}.
     *
     * @param key The {@link NamespacedKey} to remove.
     */
    private void removeAbility(@NotNull NamespacedKey key) {
        abilities.remove(key);
    }

    /**
     * Replaces an existing ability in the loadout with a new one, preserving positions.
     * Internal — callers outside this class must use {@link #swapAbility(NamespacedKey, NamespacedKey)}.
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
    private void replaceAbility(@NotNull NamespacedKey oldAbility, @NotNull NamespacedKey newAbility) {
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
     * Equips an ability to this loadout and fires a {@link LoadoutAbilityChangeEvent}
     * with {@link ChangeReason#EQUIP}.
     * Prefer this over the internal {@code addAbility} for all external and GUI callsites.
     *
     * @param key the ability key to equip
     * @return {@code true} if the ability was successfully equipped
     */
    public boolean equipAbility(@NotNull NamespacedKey key) {
        try {
            addAbility(key);
        } catch (LoadoutMaxSizeExceededException | InvalidAbilityForLoadoutException e) {
            return false;
        }
        Bukkit.getPluginManager().callEvent(
                new LoadoutAbilityChangeEvent(loadoutHolder, ChangeReason.EQUIP, null, key, loadoutSlot));
        return true;
    }

    /**
     * Unequips an ability from this loadout and fires a {@link LoadoutAbilityChangeEvent}
     * with {@link ChangeReason#UNEQUIP}.
     * Prefer this over the internal {@code removeAbility} for all external and GUI callsites.
     *
     * @param key the ability key to unequip
     * @return {@code true} if the ability was present and removed
     */
    public boolean unequipAbility(@NotNull NamespacedKey key) {
        if (!abilities.contains(key)) {
            return false;
        }
        removeAbility(key);
        Bukkit.getPluginManager().callEvent(
                new LoadoutAbilityChangeEvent(loadoutHolder, ChangeReason.UNEQUIP, key, null, loadoutSlot));
        return true;
    }

    /**
     * Swaps an ability in this loadout and fires a {@link LoadoutAbilityChangeEvent}
     * with {@link ChangeReason#SWAP}.
     * If both abilities are already in the loadout, their positions are exchanged.
     * Otherwise, {@code oldAbility} is replaced by {@code newAbility}.
     * Prefer this over the internal {@code replaceAbility} for all external and GUI callsites.
     *
     * @param oldAbility the ability key to replace
     * @param newAbility the replacement ability key
     * @return {@code true} if the swap was successful
     */
    public boolean swapAbility(@NotNull NamespacedKey oldAbility, @NotNull NamespacedKey newAbility) {
        try {
            replaceAbility(oldAbility, newAbility);
        } catch (InvalidAbilityForLoadoutException e) {
            return false;
        }
        Bukkit.getPluginManager().callEvent(
                new LoadoutAbilityChangeEvent(loadoutHolder, ChangeReason.SWAP, oldAbility, newAbility, loadoutSlot));
        return true;
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
        var abilityRegistry = McRPG.getInstance().registryAccess().registry(McRPGRegistryKey.ABILITY);
        if (ability.getAbilityType() == AbilityType.ACTIVE) {
            long activeCount = abilities.stream()
                    .map(abilityRegistry::getRegisteredAbility)
                    .filter(a -> a.getAbilityType() == AbilityType.ACTIVE)
                    .count();
            return activeCount < getMaxActiveLoadoutSize();
        }
        long passiveCount = abilities.stream()
                .map(abilityRegistry::getRegisteredAbility)
                .filter(a -> a.getAbilityType() != AbilityType.ACTIVE)
                .count();
        return passiveCount < getMaxPassiveLoadoutSize();
    }

    /**
     * Returns the maximum number of {@link ActiveAbility ActiveAbilities} allowed in this loadout.
     * <p>
     * Hardcoded to 3 — the combo system is built around exactly three active-ability slots
     * (one per combo pattern). A configurable limit created inconsistencies when players
     * had 1 or 2 actives and combo slots were left empty.
     *
     * @return The maximum active ability count (always 3).
     */
    private int getMaxActiveLoadoutSize() {
        return 3;
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
        if (oldAbility.getAbilityType() == AbilityType.ACTIVE && oldAbility instanceof SkillAbility oldSkillAbility
                && newAbility.getAbilityType() == AbilityType.ACTIVE && newAbility instanceof SkillAbility newSkillAbility
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
     * Gets the active (combo) abilities in this loadout in their stored slot order.
     * <p>
     * Returns only {@link ComboActivatable} abilities, preserving the same encounter order
     * as {@link #getOrderedAbilities()}. The Nth entry maps to combo slot N in the
     * {@link us.eunoians.mcrpg.ability.combo.ComboPattern} enumeration.
     *
     * @return An ordered, unmodifiable {@link List} of {@link NamespacedKey}s for all
     *         {@link ComboActivatable} abilities in this loadout.
     */
    @NotNull
    public List<NamespacedKey> getOrderedActiveAbilities() {
        var abilityRegistry = McRPG.getInstance().registryAccess().registry(McRPGRegistryKey.ABILITY);
        return abilities.stream()
                .filter(key -> abilityRegistry.getRegisteredAbility(key) instanceof ComboActivatable)
                .toList();
    }

    /**
     * Swaps the loadout positions of two active ({@link ComboActivatable}) abilities.
     * <p>
     * {@code fromComboSlot} and {@code toComboSlot} are 1-indexed positions into the ordered
     * active ability list (i.e., the Nth {@link ComboActivatable} in {@link #getOrderedAbilities()}).
     * If both slots refer to the same position, or if either index is out of range, this method
     * is a no-op.
     * <p>
     * After the swap the ability previously in {@code fromComboSlot} will activate via the
     * combo pattern for {@code toComboSlot} and vice versa, since
     * {@link us.eunoians.mcrpg.listener.ability.OnComboCompleteListener} resolves abilities
     * by their ordinal position in this list.
     *
     * @param fromComboSlot The 1-indexed combo slot of the ability to move (source).
     * @param toComboSlot   The 1-indexed combo slot to move the ability into (target).
     */
    public void swapActivePositions(int fromComboSlot, int toComboSlot) {
        if (fromComboSlot == toComboSlot) {
            return;
        }
        var abilityRegistry = McRPG.getInstance().registryAccess().registry(McRPGRegistryKey.ABILITY);
        List<Integer> activeIndices = new ArrayList<>();
        for (int i = 0; i < abilities.size(); i++) {
            if (abilityRegistry.getRegisteredAbility(abilities.get(i)) instanceof ComboActivatable) {
                activeIndices.add(i);
            }
        }
        if (fromComboSlot < 1 || fromComboSlot > activeIndices.size()
                || toComboSlot < 1 || toComboSlot > activeIndices.size()) {
            return;
        }
        int fromListIndex = activeIndices.get(fromComboSlot - 1);
        int toListIndex = activeIndices.get(toComboSlot - 1);
        NamespacedKey temp = abilities.get(fromListIndex);
        abilities.set(fromListIndex, abilities.get(toListIndex));
        abilities.set(toListIndex, temp);
        Bukkit.getPluginManager().callEvent(
                new LoadoutPositionSwapEvent(loadoutHolder, fromComboSlot, toComboSlot));
    }

    /**
     * Gets the maximum total number of abilities (active + passive) allowed in a loadout.
     *
     * @return The sum of {@link #getMaxActiveLoadoutSize()} and {@link #getMaxPassiveLoadoutSize()}.
     */
    private int getMaxLoadoutSize() {
        return getMaxActiveLoadoutSize() + getMaxPassiveLoadoutSize();
    }

    /**
     * Gets the maximum number of passive (non-active) abilities allowed in a loadout.
     * <p>
     * This value is read from the {@code max-passive-loadout-size} config key. Active abilities have
     * their own separate budget via {@link #getMaxActiveLoadoutSize()}.
     *
     * @return The maximum passive ability count for a single loadout.
     */
    public int getMaxPassiveLoadoutSize() {
        return McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE).getFile(FileType.MAIN_CONFIG).getInt(MainConfigFile.MAX_PASSIVE_LOADOUT_SIZE);
    }

    /**
     * Gets the default {@link LoadoutDisplay} for loadouts.
     *
     * @return The default {@link LoadoutDisplay} for loadouts.
     */
    private LoadoutDisplay getDefaultDisplayItem() {
        return new LoadoutDisplay(Material.CHERRY_SIGN, "Loadout " + getLoadoutSlot());
    }

}
