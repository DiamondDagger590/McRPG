package us.eunoians.mcrpg.ability.listener;

import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.BaseAbility;
import us.eunoians.mcrpg.ability.CooldownableAbility;
import us.eunoians.mcrpg.ability.ReadyableAbility;
import us.eunoians.mcrpg.api.AbilityHolder;
import us.eunoians.mcrpg.api.event.ability.AbilityReadyEvent;
import us.eunoians.mcrpg.api.event.ability.CooldownableAbilityActivateEvent;
import us.eunoians.mcrpg.api.manager.CooldownManager;
import us.eunoians.mcrpg.player.McRPGPlayer;

/**
 * This class handles listening to ability activate events and will cancel them if the ability activated is still on cooldown.
 * <p>
 * Additionally this class handles putting abilities on cooldown automatically by listening to {@link CooldownableAbilityActivateEvent}s and
 * requires little effort on someone who is making an {@link CooldownableAbility} to integrate. They simply have to call the {@link CooldownableAbilityActivateEvent}
 * for their {@link CooldownableAbility} and it will be automatically handled.
 *
 * @author DiamondDagger590
 */
public class CooldownableAbilityListener implements Listener {

    /**
     * Handles cancelling {@link CooldownableAbilityActivateEvent} on {@link EventPriority#LOWEST} to ensure that it is handled before other
     * plugins attempt to listen to it.
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void handleCooldownCheck(CooldownableAbilityActivateEvent abilityActivateEvent) {

        CooldownableAbility ability = abilityActivateEvent.getAbility();
        AbilityHolder abilityHolder = abilityActivateEvent.getAbilityHolder();

        if (ability instanceof BaseAbility) {

            BaseAbility baseAbility = (BaseAbility) ability;
            NamespacedKey abilityKey = Ability.getId(baseAbility.getClass());

            CooldownManager cooldownManager = McRPG.getInstance().getCooldownManager();

            CooldownManager.CooldownWrapper cooldownWrapper = cooldownManager.getCooldownWrapper(abilityHolder.getUniqueId());
            CooldownManager.SkillCooldownWrapper skillCooldownWrapper = cooldownWrapper.getSkillCooldownWrapper(ability.getSkill());

            if (skillCooldownWrapper.isOnCooldown(abilityKey)) {
                abilityActivateEvent.setCancelled(true);

                if (abilityHolder instanceof McRPGPlayer) {
                    int secondsRemaining = (int) Math.min(1, skillCooldownWrapper.getMilisLeftOnCooldown(abilityKey) / 1000L);
                    McRPG.getInstance().getMessageSender()
                            .sendMessage(((McRPGPlayer) abilityHolder).getEntity(), ChatColor.RED + "You are on cooldown for " + ChatColor.GOLD + secondsRemaining + ChatColor.RED + " seconds.", false);
                }
            }
        }
    }

    /**
     * Handles cancelling {@link AbilityReadyEvent} on {@link EventPriority#LOWEST} to ensure that it is handled before other
     * plugins attempt to listen to it.
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void handleCooldownCheck(AbilityReadyEvent abilityReadyEvent) {

        ReadyableAbility ability = abilityReadyEvent.getAbility();
        AbilityHolder abilityHolder = abilityReadyEvent.getAbilityHolder();

        if (ability instanceof CooldownableAbility && ability instanceof BaseAbility) {

            BaseAbility baseAbility = (BaseAbility) ability;
            NamespacedKey abilityKey = Ability.getId(baseAbility.getClass());

            CooldownManager cooldownManager = McRPG.getInstance().getCooldownManager();

            CooldownManager.CooldownWrapper cooldownWrapper = cooldownManager.getCooldownWrapper(abilityHolder.getUniqueId());
            CooldownManager.SkillCooldownWrapper skillCooldownWrapper = cooldownWrapper.getSkillCooldownWrapper(ability.getSkill());

            if (skillCooldownWrapper.isOnCooldown(abilityKey)) {
                abilityReadyEvent.setCancelled(true);

                if (abilityHolder instanceof McRPGPlayer) {
                    int secondsRemaining = (int) Math.min(1, skillCooldownWrapper.getMilisLeftOnCooldown(abilityKey) / 1000L);
                    McRPG.getInstance().getMessageSender()
                            .sendMessage(((McRPGPlayer) abilityHolder).getEntity(), ChatColor.RED + "You are on cooldown for " + ChatColor.GOLD + secondsRemaining + ChatColor.RED + " seconds.", false);
                }
            }
        }
    }

    /**
     * Handles automatically putting abilities on cooldown on activation
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void handlePuttingOnCooldown(CooldownableAbilityActivateEvent abilityActivateEvent) {

        CooldownableAbility ability = abilityActivateEvent.getAbility();
        AbilityHolder abilityHolder = abilityActivateEvent.getAbilityHolder();

        if (ability instanceof BaseAbility) {

            BaseAbility baseAbility = (BaseAbility) ability;
            NamespacedKey abilityKey = Ability.getId(baseAbility.getClass());

            CooldownManager cooldownManager = McRPG.getInstance().getCooldownManager();

            CooldownManager.CooldownWrapper cooldownWrapper = cooldownManager.getCooldownWrapper(abilityHolder.getUniqueId());
            CooldownManager.SkillCooldownWrapper skillCooldownWrapper = cooldownWrapper.getSkillCooldownWrapper(ability.getSkill());

            cooldownWrapper.putAbilityOnCooldown(baseAbility, System.currentTimeMillis() + (abilityActivateEvent.getCooldownSeconds() * 1000L), true);
            skillCooldownWrapper.setCooldown(abilityKey, System.currentTimeMillis() + (abilityActivateEvent.getCooldownSeconds() * 1000L));
        }
    }
}
