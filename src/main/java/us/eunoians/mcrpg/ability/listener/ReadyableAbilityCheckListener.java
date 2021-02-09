package us.eunoians.mcrpg.ability.listener;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.ReadyableAbility;
import us.eunoians.mcrpg.ability.ToggleableAbility;
import us.eunoians.mcrpg.api.AbilityHolder;
import us.eunoians.mcrpg.api.event.ability.AbilityReadyEvent;
import us.eunoians.mcrpg.api.manager.ReadyTaskManager;

/**
 * This listener handles attempting to put {@link ReadyableAbility}s on "ready" status in a generic manner
 *
 * @author DiamondDagger590
 */
public class ReadyableAbilityCheckListener implements Listener {

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void handleInteract(PlayerInteractEvent playerInteractEvent){

        AbilityHolder abilityHolder = new AbilityHolder(playerInteractEvent.getPlayer());

        ReadyTaskManager readyTaskManager = McRPG.getInstance().getReadyTaskManager();

        if(!readyTaskManager.hasReadyStatus(abilityHolder.getUniqueId())) {

            for (Ability ability : abilityHolder.getLoadout()) {

                if (ability instanceof ReadyableAbility) {

                    ReadyableAbility readyableAbility = (ReadyableAbility) ability;

                    //Handle toggled abilities
                    if(ability instanceof ToggleableAbility && !(((ToggleableAbility) ability).isToggled())){
                        continue;
                    }

                    //Short circuit before calling the handle ready attempt where it calls the ready activation code
                    if(readyableAbility.readyFromBlock() && readyableAbility.isValidReadyableBlock(playerInteractEvent.getClickedBlock())
                            && readyableAbility.handleReadyAttempt(playerInteractEvent)){

                        AbilityReadyEvent abilityReadyEvent = new AbilityReadyEvent(abilityHolder, readyableAbility, readyableAbility.getReadyDurationSeconds());
                        Bukkit.getPluginManager().callEvent(abilityReadyEvent);

                        if(abilityReadyEvent.isCancelled()){
                            return;
                        }

                        readyableAbility.startReady(abilityReadyEvent.getReadySeconds());
                        //Return because there can only be one "ready" ability at once, and if all statements are true, then they are now on ready status
                        return;
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void handleInteract(PlayerInteractAtEntityEvent playerInteractAtEntityEvent){

        AbilityHolder abilityHolder = new AbilityHolder(playerInteractAtEntityEvent.getPlayer());

        ReadyTaskManager readyTaskManager = McRPG.getInstance().getReadyTaskManager();

        if(!readyTaskManager.hasReadyStatus(abilityHolder.getUniqueId())) {

            for (Ability ability : abilityHolder.getLoadout()) {

                if (ability instanceof ReadyableAbility) {

                    ReadyableAbility readyableAbility = (ReadyableAbility) ability;

                    //Handle toggled abilities
                    if(ability instanceof ToggleableAbility && !(((ToggleableAbility) ability).isToggled())){
                        continue;
                    }

                    //Short circuit before calling the handle ready attempt where it calls the ready activation code
                    if(readyableAbility.readyFromEntity() && readyableAbility.isValidReadyableEntity(playerInteractAtEntityEvent.getRightClicked())
                            && readyableAbility.handleReadyAttempt(playerInteractAtEntityEvent)){

                        AbilityReadyEvent abilityReadyEvent = new AbilityReadyEvent(abilityHolder, readyableAbility, readyableAbility.getReadyDurationSeconds());
                        Bukkit.getPluginManager().callEvent(abilityReadyEvent);

                        if(abilityReadyEvent.isCancelled()){
                            return;
                        }

                        readyableAbility.startReady(abilityReadyEvent.getReadySeconds());
                        //Return because there can only be one "ready" ability at once, and if all statements are true, then they are now on ready status
                        return;
                    }
                }
            }
        }
    }
}
