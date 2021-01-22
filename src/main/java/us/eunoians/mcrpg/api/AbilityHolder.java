package us.eunoians.mcrpg.api;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.ability.Ability;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * This class represents a wrapper for a {@link org.bukkit.entity.LivingEntity} that can utilize various Abilities.
 *
 * @author DiamondDagger590
 */
public class AbilityHolder {

    /**
     * This map contains all abilities that this {@link AbilityHolder} possess.
     * <p>
     * This map contains even locked abilities for players so it should be assumed
     * that a {@link us.eunoians.mcrpg.player.McRPGPlayer} "has" all abilities for
     * ease of access of data, however this does not equate to the ability being unlocked.
     */
    private final @NotNull Map<NamespacedKey, Ability> abilities;

    /**
     * This map contains all abilities that this {@link AbilityHolder} currently has "equipped".
     * <p>
     * An ability being in here means that all listeners for the ability have been initialized which is an
     * assumption that can't be made about {@link #abilities}.
     */
    private final @NotNull Map<NamespacedKey, Ability> abilityLoadout;

    /**
     * Represents if this {@link AbilityHolder} is an {@link Player} as
     * an {@link AbilityHolder} can be any {@link LivingEntity} code wise
     * but the distinction helps.
     */
    private final boolean isPlayer;

    /**
     * The {@link UUID} of this {@link AbilityHolder}
     */
    private final @NotNull UUID uuid;

    public AbilityHolder(@NotNull Player player) {
        this.abilities = new HashMap<>();
        this.abilityLoadout = new HashMap<>();
        this.isPlayer = true;
        this.uuid = player.getUniqueId();

        //TODO populate abilities
    }

    public AbilityHolder(@NotNull UUID uuid, boolean isPlayer) {
        this.abilities = new HashMap<>();
        this.abilityLoadout = new HashMap<>();
        this.isPlayer = isPlayer;
        this.uuid = uuid;

        //TODO populate abilities
    }

    /**
     * Gets the {@link Ability} mapped to the provided {@link NamespacedKey}
     *
     * @param namespacedKey The {@link NamespacedKey} that links to the desired {@link Ability}
     * @return {@code null} if there isn't an {@link Ability} that matches the provided {@link NamespacedKey} or the
     * {@link Ability} that maps if present.
     */
    @Nullable
    public Ability getAbility(@NotNull NamespacedKey namespacedKey) {
        return abilities.get(namespacedKey);
    }

    /**
     * Checks to see if the {@link AbilityHolder} has the {@link Ability} that maps
     * to the provided {@link NamespacedKey}
     *
     * @param namespacedKey The {@link NamespacedKey} to check
     * @return {@code true} if the {@link AbilityHolder} has an {@link Ability} that maps to the
     * provided {@link NamespacedKey}
     */
    public boolean hasAbility(@NotNull NamespacedKey namespacedKey) {
        return abilities.containsKey(namespacedKey);
    }

    /**
     * Gets the {@link UUID} of this {@link AbilityHolder}
     *
     * @return The {@link UUID} of this {@link AbilityHolder}
     */
    @NotNull
    public UUID getUniqueId() {
        return uuid;
    }

    /**
     * Gets the {@link LivingEntity} that this object maps to
     *
     * @return The {@link LivingEntity} that this object maps to or {@code null} if invalid/dead/offline
     */
    @Nullable
    public LivingEntity getEntity() {
        if (isPlayer) {
            return Bukkit.getPlayer(uuid);
        }
        else {
            Entity entity = Bukkit.getEntity(uuid);

            return entity instanceof LivingEntity ? (LivingEntity) entity : null;
        }
    }

    /**
     * Gets a {@link Collection} of {@link Ability}s that the {@link AbilityHolder} currently has in their loadout.
     * <p>
     * This {@link Collection} may be empty but there will always be one returned. If a single ability is needed,
     * see {@link #getAbilityFromLoadout(NamespacedKey)}.
     *
     * @return A {@link Collection} that contains all {@link Ability}s that the {@link AbilityHolder} currently has in their loadout
     */
    @NotNull
    public Collection<Ability> getLoadout() {
        return this.abilityLoadout.values();
    }

    /**
     * Gets an {@link Ability} from the {@link AbilityHolder}'s loadout.
     * <p>
     * This ability will have all the listeners registered which is an assumption that shouldn't be made
     * when using {@link #getAbility(NamespacedKey)}.
     *
     * @param namespacedKey The {@link NamespacedKey} that maps to the {@link Ability} that is desired
     * @return {@code null} if the {@link NamespacedKey} provided is invalid or the {@link Ability} that matches the {@link NamespacedKey}
     */
    @Nullable
    public Ability getAbilityFromLoadout(@NotNull NamespacedKey namespacedKey) {
        return this.abilityLoadout.get(namespacedKey);
    }

    /**
     * This method is used to get an {@link AbilityHolder} from a {@link LivingEntity}.
     * <p>
     * Not all {@link LivingEntity}s are an {@link AbilityHolder} however. Usually they need
     * to be created through this class in order to be a valid {@link AbilityHolder}
     *
     * @param livingEntity The {@link LivingEntity} to get the {@link AbilityHolder} from
     * @return An {@link AbilityHolder} that contains info about the {@link LivingEntity} passed
     * in or {@code null} if invalid.
     */
    @Nullable
    public static AbilityHolder getFromEntity(@NotNull LivingEntity livingEntity) {
        //TODO
        return null;
    }

    /**
     * Checks to see if the {@link LivingEntity} is an ability holder
     *
     * @param livingEntity The {@link LivingEntity} to check
     * @return {@code true} if the {@link LivingEntity} is a valid {@link AbilityHolder}
     */
    public static boolean isAbilityHolder(@NotNull LivingEntity livingEntity) {
        return livingEntity instanceof Player || getFromEntity(livingEntity) != null;
    }
}
