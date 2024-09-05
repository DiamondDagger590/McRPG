package us.eunoians.mcrpg.ability.impl;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.AbilityData;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeManager;
import us.eunoians.mcrpg.ability.attribute.AbilityCooldownAttribute;
import us.eunoians.mcrpg.api.event.ability.AbilityPutOnCooldownEvent;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

import java.util.Set;

/**
 * This interface represents an {@link Ability} that can be put on cooldowns
 * after it has been activated.
 */
public interface CooldownableAbility extends Ability {

    /**
     * Checks to see if the provided {@link AbilityHolder} is currently
     * on cooldown for this ability.
     *
     * @param abilityHolder The {@link AbilityHolder} to check.
     * @return {@code true} if the provided {@link AbilityHolder} is currently
     * on cooldown for this ability.
     */
    default boolean isAbilityOnCooldown(@NotNull AbilityHolder abilityHolder) {
        long cooldown = getCooldownForHolder(abilityHolder);
        return cooldown > 0 && cooldown > System.currentTimeMillis();
    }

    /**
     * Gets the cooldown end time in milliseconds of this ability for the provided {@link AbilityHolder}.
     *
     * @param abilityHolder The {@link AbilityHolder} to get the cooldown end time for.
     * @return The time in milliseconds that the cooldown for this ability expires, or 0 if there is no cooldown.
     * (The returned time may be in the past)
     */
    default long getCooldownForHolder(@NotNull AbilityHolder abilityHolder) {
        // Sanity check
        if (getApplicableAttributes().contains(AbilityAttributeManager.ABILITY_COOLDOWN_ATTRIBUTE_KEY)) {
            var abilityDataOptional = abilityHolder.getAbilityData(this);
            if (abilityDataOptional.isPresent()) {
                AbilityData abilityData = abilityDataOptional.get();
                var cooldownOptional = abilityData.getAbilityAttribute(AbilityAttributeManager.ABILITY_COOLDOWN_ATTRIBUTE_KEY);
                if (cooldownOptional.isPresent()) {
                    AbilityCooldownAttribute cooldownAttribute = (AbilityCooldownAttribute) cooldownOptional.get();
                    return cooldownAttribute.getContent();
                }
            }
        }
        return 0;
    }

    /**
     * Notifies the {@link AbilityHolder} that their cooldown is still active if that holder
     * is also a {@link Player}.
     *
     * @param abilityHolder The {@link AbilityHolder} to notify
     */
    default void notifyCooldownActive(@NotNull AbilityHolder abilityHolder) {
        if (Bukkit.getEntity(abilityHolder.getUUID()) instanceof Player player) {
            MiniMessage miniMessage = MiniMessage.miniMessage();
            Audience audience = McRPG.getInstance().getAdventure().player(player);
            audience.sendMessage(miniMessage.deserialize("<red>" + getDisplayName() + " is still on cooldown."));
        }
    }

    @NotNull
    @Override
    default Set<NamespacedKey> getApplicableAttributes() {
        return Set.of(AbilityAttributeManager.ABILITY_COOLDOWN_ATTRIBUTE_KEY);
    }

    /**
     * Gets the duration of the cooldown for this ability for the provided {@link AbilityHolder}
     * in seconds.
     *
     * @param abilityHolder The {@link AbilityHolder} to get the cooldown duration for.
     * @return The duration of the cooldown for this ability for the provided {@link AbilityHolder}
     * in seconds.
     */
    long getCooldown(@NotNull AbilityHolder abilityHolder);

    /**
     * Puts the provided {@link AbilityHolder} on cooldown for this ability using the value of
     * {@link #getCooldown(AbilityHolder)} for the duration.
     *
     * @param abilityHolder The {@link AbilityHolder} to put on cooldown.
     */
    default void putHolderOnCooldown(@NotNull AbilityHolder abilityHolder) {
        putHolderOnCooldown(abilityHolder, getCooldown(abilityHolder));
    }

    /**
     * Puts the provided {@link AbilityHolder} on cooldown for this ability for the provided
     * duration in seconds.
     *
     * @param abilityHolder The {@link AbilityHolder} to put on cooldown.
     * @param cooldown      The duration of the cooldown in seconds.
     */
    default void putHolderOnCooldown(@NotNull AbilityHolder abilityHolder, long cooldown) {
        // Sanity check
        if (getApplicableAttributes().contains(AbilityAttributeManager.ABILITY_COOLDOWN_ATTRIBUTE_KEY)) {
            var abilityDataOptional = abilityHolder.getAbilityData(this);
            if (abilityDataOptional.isPresent()) {
                AbilityData abilityData = abilityDataOptional.get();
                var cooldownOptional = McRPG.getInstance().getAbilityAttributeManager().getAttribute(AbilityAttributeManager.ABILITY_COOLDOWN_ATTRIBUTE_KEY);
                cooldownOptional.ifPresent(abilityAttribute -> {
                    AbilityPutOnCooldownEvent abilityPutOnCooldownEvent = new AbilityPutOnCooldownEvent(abilityHolder, this, cooldown);
                    Bukkit.getPluginManager().callEvent(abilityPutOnCooldownEvent);
                    abilityData.addAttribute(((AbilityCooldownAttribute) abilityAttribute).create(System.currentTimeMillis() + (abilityPutOnCooldownEvent.getCooldown() * 1000)));
                });
            }
        }
    }
}
