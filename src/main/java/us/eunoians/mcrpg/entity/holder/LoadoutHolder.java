package us.eunoians.mcrpg.entity.holder;

import com.diamonddagger590.mccore.registry.RegistryKey;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.impl.type.UnlockableAbility;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.MainConfigFile;
import us.eunoians.mcrpg.exception.loadout.SelectedLoadoutAboveMaxException;
import us.eunoians.mcrpg.loadout.Loadout;
import us.eunoians.mcrpg.loadout.LoadoutResolution;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
 * To get ALL abilities a holder can use (including 'innate abilities'), use {@link #getAvailableAbilitiesToUse()}.
 */
public class LoadoutHolder extends AbilityHolder {

    private final Map<Integer, Loadout> loadouts;
    private int currentLoadout;

    public LoadoutHolder(@NotNull McRPG mcRPG, @NotNull UUID uuid) {
        super(mcRPG, uuid);
        this.loadouts = new HashMap<>();
        this.currentLoadout = 1;
    }

    public LoadoutHolder(@NotNull McRPG mcRPG, @NotNull UUID uuid, int currentLoadout, Map<Integer, Loadout> loadouts) {
        super(mcRPG, uuid);
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
        return loadoutSlot >= 1 && (loadouts.containsKey(loadoutSlot) || loadoutSlot <= getMaxLoadoutAmount());
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
        return getAvailableAbilities().stream().filter(namespacedKey -> !McRPG.getInstance().registryAccess().registry(McRPGRegistryKey.ABILITY).getRegisteredAbility(namespacedKey).isPassive()).collect(Collectors.toSet());
    }

    /**
     * Gets a {@link Set} of all the {@link NamespacedKey}s that represent all {@link Ability Abilities}
     * that are 'default abilities', or ones that don't require unlocking to use.
     *
     * @return Gets a {@link Set} of all the {@link NamespacedKey}s that represent all {@link Ability Abilities}
     * that are 'default abilities', or ones that don't require unlocking to use.
     */
    private Set<NamespacedKey> getAvailableDefaultAbilities() {
        return getAvailableAbilities().stream().filter(namespacedKey -> !(McRPG.getInstance().registryAccess().registry(McRPGRegistryKey.ABILITY).getRegisteredAbility(namespacedKey) instanceof UnlockableAbility)).collect(Collectors.toSet());
    }

    /**
     * Resolves a player-supplied string to a {@link Loadout} using a priority chain:
     * <ol>
     *   <li>Slot index — if the input is a valid integer that maps to an existing slot.</li>
     *   <li>Exact name match — case-insensitive comparison against each loadout's plain-text display name.</li>
     *   <li>Substring match — case-insensitive containment check against each loadout's plain-text display name.</li>
     * </ol>
     * <p>
     * For name matching, only loadouts with a user-set display name (non-null in their {@link us.eunoians.mcrpg.loadout.LoadoutDisplay})
     * are considered. If multiple loadouts match at any name step, an {@link LoadoutResolution.Ambiguous} result is returned.
     *
     * @param input The raw string the player provided (e.g. {@code "1"}, {@code "mining loadout"}, {@code "mining"}).
     * @return A {@link LoadoutResolution} describing whether a unique match was found, multiple matches were found, or nothing matched.
     */
    @NotNull
    public LoadoutResolution resolveLoadout(@NotNull String input) {
        // Step 1: try slot index
        try {
            int slot = Integer.parseInt(input);
            if (hasLoadout(slot)) {
                return new LoadoutResolution.Found(getLoadout(slot));
            }
            // Input is a valid integer but not a valid slot; fall through to name matching
            // so a loadout whose display name happens to be a number (e.g. "5") is still reachable.
        } catch (NumberFormatException ignored) {
            // Not an integer — fall through to name matching
        }

        // Collect all loadouts that have a user-set display name.
        // Iterate the backing map directly so we never auto-create slots that don't exist yet.
        // Guard against stale entries whose slot exceeds the current maximum (e.g. after a config change).
        int maxSlots = getMaxLoadoutAmount();
        List<Loadout> namedLoadouts = new ArrayList<>();
        for (Loadout loadout : loadouts.values()) {
            int slot = loadout.getLoadoutSlot();
            if (slot >= 1 && slot <= maxSlots && loadout.getDisplay().getDisplayName().isPresent()) {
                namedLoadouts.add(loadout);
            }
        }

        // Step 2: exact name match (case-insensitive, against plain-text name)
        List<Loadout> exactMatches = namedLoadouts.stream()
                .filter(loadout -> getPlainDisplayName(loadout).equalsIgnoreCase(input))
                .toList();
        if (exactMatches.size() == 1) {
            return new LoadoutResolution.Found(exactMatches.get(0));
        }
        if (exactMatches.size() > 1) {
            return new LoadoutResolution.Ambiguous(exactMatches);
        }

        // Step 3: substring match (case-insensitive)
        String lowerInput = input.toLowerCase();
        List<Loadout> substringMatches = namedLoadouts.stream()
                .filter(loadout -> getPlainDisplayName(loadout).toLowerCase().contains(lowerInput))
                .toList();
        if (substringMatches.size() == 1) {
            return new LoadoutResolution.Found(substringMatches.get(0));
        }
        if (substringMatches.size() > 1) {
            return new LoadoutResolution.Ambiguous(substringMatches);
        }

        return new LoadoutResolution.NotFound();
    }

    /**
     * Returns the plain-text display name of a {@link Loadout}, stripping any MiniMessage formatting tags.
     *
     * @param loadout The {@link Loadout} to get the plain-text name of.
     * @return The plain-text display name, or an empty string if no display name is set.
     */
    @NotNull
    private String getPlainDisplayName(@NotNull Loadout loadout) {
        return loadout.getDisplay().getDisplayName()
                .map(name -> PlainTextComponentSerializer.plainText().serialize(McRPG.getInstance().getMiniMessage().deserialize(name)))
                .orElse("");
    }

    /**
     * Returns the number of {@link Loadout}s currently held in memory for this holder.
     * <p>
     * Only loadouts that have been explicitly created or loaded from storage are counted.
     * Slots that have never been accessed are not included, unlike {@link #hasLoadout(int)}
     * which considers all slots up to {@link #getMaxLoadoutAmount()}.
     *
     * @return The number of loadouts present in the backing store.
     */
    public int getLoadedLoadoutCount() {
        return loadouts.size();
    }

    /**
     * Gets the maximum amount of {@link Loadout}s that this holder can have.
     *
     * @return The maximum amount of {@link Loadout}s that this holder can have.
     */
    public int getMaxLoadoutAmount() {
        return McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE).getFile(FileType.MAIN_CONFIG).getInt(MainConfigFile.MAX_LOADOUT_AMOUNT);
    }
}
