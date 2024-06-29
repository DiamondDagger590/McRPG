package us.eunoians.mcrpg.listener.ability;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import us.eunoians.mcrpg.api.event.ability.swords.BleedActivateEvent;
import us.eunoians.mcrpg.api.event.ability.swords.RageSpikeActivateEvent;
import us.eunoians.mcrpg.api.event.ability.swords.SerratedStrikesActivateEvent;

public class OnAbilityActivateListener implements Listener {

    @EventHandler
    public void onAbilityActivate(RageSpikeActivateEvent event){
        Bukkit.broadcastMessage("activated " + event.getAbility().getDisplayName());
    }

    @EventHandler
    public void onAbilityActivate(SerratedStrikesActivateEvent event){
        Bukkit.broadcastMessage("activated " + event.getAbility().getDisplayName());
    }

    @EventHandler
    public void onAbilityActivate(BleedActivateEvent event){
        Bukkit.broadcastMessage("activated " + event.getAbility().getDisplayName());
    }
}
