package us.eunoians.mcrpg.ability.impl.type;

import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.AbilityData;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityCooldownAttribute;
import us.eunoians.mcrpg.builder.item.ability.AbilityItemPlaceholderKeys;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.event.ability.AbilityPutOnCooldownEvent;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

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
        return cooldown > 0 && cooldown > McRPG.getInstance().getTimeProvider().now().toEpochMilli();
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
        if (getApplicableAttributes().contains(AbilityAttributeRegistry.ABILITY_COOLDOWN_ATTRIBUTE_KEY)) {
            var abilityDataOptional = abilityHolder.getAbilityData(this);
            if (abilityDataOptional.isPresent()) {
                AbilityData abilityData = abilityDataOptional.get();
                var cooldownOptional = abilityData.getAbilityAttribute(AbilityAttributeRegistry.ABILITY_COOLDOWN_ATTRIBUTE_KEY);
                if (cooldownOptional.isPresent()) {
                    AbilityCooldownAttribute cooldownAttribute = (AbilityCooldownAttribute) cooldownOptional.get();
                    return cooldownAttribute.getContent();
                }
            }
        }
        return 0;
    }

    /**
     * Notifies the {@link McRPGPlayer} that their cooldown is still active if that holder
     * is also a {@link Player}.
     *
     * @param mcRPGPlayer The {@link McRPGPlayer} to notify
     */
    default void notifyCooldownActive(@NotNull McRPGPlayer mcRPGPlayer) {
        var playerOptional = mcRPGPlayer.getAsBukkitPlayer();
        playerOptional.ifPresent(player -> {
            McRPG mcRPG = mcRPGPlayer.getPlugin();
            McRPGLocalizationManager localizationManager = mcRPG.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
            player.sendMessage(localizationManager.getLocalizedMessageAsComponent(mcRPGPlayer, LocalizationKey.ABILITY_STILL_ON_COOLDOWN, Map.of(AbilityItemPlaceholderKeys.ABILITY.getKey(), getName(mcRPGPlayer))));
        });
    }

    @NotNull
    @Override
    default Set<NamespacedKey> getApplicableAttributes() {
        return Set.of(AbilityAttributeRegistry.ABILITY_COOLDOWN_ATTRIBUTE_KEY);
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
     * @return The cooldown that was actually applied.
     */
    default long putHolderOnCooldown(@NotNull AbilityHolder abilityHolder) {
        return putHolderOnCooldown(abilityHolder, getCooldown(abilityHolder));
    }

    /**
     * Puts the provided {@link AbilityHolder} on cooldown for this ability for the provided
     * duration in seconds.
     *
     * @param abilityHolder The {@link AbilityHolder} to put on cooldown.
     * @param cooldown      The duration of the cooldown in seconds.
     * @return The cooldown that was actually applied.
     */
    default long putHolderOnCooldown(@NotNull AbilityHolder abilityHolder, long cooldown) {
        // Sanity check
        if (getApplicableAttributes().contains(AbilityAttributeRegistry.ABILITY_COOLDOWN_ATTRIBUTE_KEY)) {
            var abilityDataOptional = abilityHolder.getAbilityData(this);
            if (abilityDataOptional.isPresent()) {
                AbilityData abilityData = abilityDataOptional.get();
                var cooldownOptional = McRPG.getInstance().registryAccess().registry(McRPGRegistryKey.ABILITY_ATTRIBUTE).getAttribute(AbilityAttributeRegistry.ABILITY_COOLDOWN_ATTRIBUTE_KEY);
                AtomicLong cooldownToReturn = new AtomicLong(cooldown);
                cooldownOptional.ifPresent(abilityAttribute -> {
                    AbilityPutOnCooldownEvent abilityPutOnCooldownEvent = new AbilityPutOnCooldownEvent(abilityHolder, this, cooldown);
                    Bukkit.getPluginManager().callEvent(abilityPutOnCooldownEvent);
                    abilityData.addAttribute(((AbilityCooldownAttribute) abilityAttribute).create(McRPG.getInstance().getTimeProvider().now().toEpochMilli() + (abilityPutOnCooldownEvent.getCooldown() * 1000)));
                    cooldownToReturn.set(abilityPutOnCooldownEvent.getCooldown());
                });
                return cooldownToReturn.get();
            }
        }
        return 0;
    }
}
