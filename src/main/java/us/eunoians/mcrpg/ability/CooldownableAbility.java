package us.eunoians.mcrpg.ability;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public interface CooldownableAbility extends Ability {

    /**
     * A map of all cooldowns for this ability
     */
    Map<UUID, Long> cooldowns = new HashMap<>();

    /**
     * Gets the amount of millis left on cooldown for this {@link CooldownableAbility} for the {@link UUID} provided
     *
     * @param uuid The {@link UUID} to check
     * @return The remaining milis of the cooldown or {@code -1} if there is no cooldown.
     */
    default long getMilisLeftOnCooldown(@NotNull UUID uuid) {

        long cooldownRemaining = cooldowns.getOrDefault(uuid, -1L);

        if (cooldownRemaining <= System.currentTimeMillis()) {
            cooldowns.remove(uuid);
            cooldownRemaining = -1;
        }

        return cooldownRemaining - System.currentTimeMillis();
    }

    public boolean isOnCooldown();

    public boolean setOnCooldown();

    public long getMilisLeftOnCooldown();
}
