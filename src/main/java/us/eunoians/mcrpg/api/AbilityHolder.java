package us.eunoians.mcrpg.api;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.ability.Ability;

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
     * ease of access of data, however this does not equate to the abilty being unlocked.
     */
    private final @NotNull Map<NamespacedKey, Ability> abilities;

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
        this.isPlayer = true;
        this.uuid = player.getUniqueId();

        //TODO populate abilities
    }

    public AbilityHolder(@NotNull UUID uuid, boolean isPlayer) {
        this.abilities = new HashMap<>();
        this.isPlayer = isPlayer;
        this.uuid = uuid;

        //TODO populate abilities
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
        return null;
    }
}
