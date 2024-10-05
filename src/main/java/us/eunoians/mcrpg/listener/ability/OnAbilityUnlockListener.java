package us.eunoians.mcrpg.listener.ability;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.impl.UnlockableAbility;
import us.eunoians.mcrpg.event.event.ability.AbilityUnlockEvent;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.holder.LoadoutHolder;
import us.eunoians.mcrpg.loadout.Loadout;
import us.eunoians.mcrpg.skill.Skill;

public class OnAbilityUnlockListener implements Listener {

    @EventHandler
    public void onAbilityUnlock(AbilityUnlockEvent abilityUnlockEvent) {
        AbilityHolder abilityHolder = abilityUnlockEvent.getAbilityHolder();
        UnlockableAbility unlockableAbility = abilityUnlockEvent.getAbility();
        MiniMessage miniMessage = McRPG.getInstance().getMiniMessage();
        Audience player = McRPG.getInstance().getAdventure().player(abilityHolder.getUUID());
        player.sendMessage(miniMessage.deserialize(String.format("<green>You have unlocked a new ability! <gold>%s<green> is now available for use.", abilityUnlockEvent.getAbility().getDisplayName())));

        if (abilityHolder instanceof LoadoutHolder loadoutHolder) {
            Loadout loadout = loadoutHolder.getLoadout();
            if (loadout.getRemainingLoadoutSize() > 0) {
                if (loadout.canAbilityBeInLoadout(unlockableAbility.getAbilityKey())) {
                    loadout.addAbility(unlockableAbility.getAbilityKey());
                    player.sendMessage(miniMessage.deserialize("<green>The new ability has automatically been added to your current loadout."));
                }
                else {
                    Skill skill = McRPG.getInstance().getSkillRegistry().getRegisteredSkill(unlockableAbility.getSkill().get());
                    player.sendMessage(miniMessage.deserialize("<red>You already have an active ability for the skill " + skill.getDisplayName() + " in your loadout, so "
                            + unlockableAbility.getDisplayName() + " was not automatically added."));
                }
            }
        }
    }
}
