package us.eunoians.mcrpg.listener.ability;

import com.diamonddagger590.mccore.registry.RegistryKey;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.impl.type.SkillAbility;
import us.eunoians.mcrpg.ability.impl.type.UnlockableAbility;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.holder.LoadoutHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.event.ability.AbilityUnlockEvent;
import us.eunoians.mcrpg.loadout.Loadout;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.Skill;

import java.util.Map;

public class OnAbilityUnlockListener implements Listener {

    @EventHandler
    public void onAbilityUnlock(AbilityUnlockEvent abilityUnlockEvent) {
        AbilityHolder abilityHolder = abilityUnlockEvent.getAbilityHolder();
        UnlockableAbility unlockableAbility = abilityUnlockEvent.getAbility();
        MiniMessage miniMessage = McRPG.getInstance().getMiniMessage();
        var playerOptional = McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.PLAYER).getPlayer(abilityHolder.getUUID());
        if (playerOptional.isEmpty() || playerOptional.get().getAsBukkitPlayer().isEmpty()) {
            return;
        }
        McRPGPlayer mcRPGPlayer = playerOptional.get();
        Audience player = mcRPGPlayer.getAsBukkitPlayer().get();
        McRPGLocalizationManager localizationManager = McRPG.getInstance().registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION);

        Component abilityDisplayName = unlockableAbility.getDisplayName(mcRPGPlayer);
        String serializedAbilityName = miniMessage.serialize(abilityDisplayName);
        player.sendMessage(localizationManager.getLocalizedMessageAsComponent(mcRPGPlayer, LocalizationKey.ABILITY_UNLOCKED_MESSAGE,
                Map.of("ability", serializedAbilityName)));

        if (abilityHolder instanceof LoadoutHolder loadoutHolder) {
            Loadout loadout = loadoutHolder.getLoadout();
            if (loadout.getRemainingLoadoutSize() > 0) {
                if (loadout.canAbilityBeAddedToLoadout(unlockableAbility.getAbilityKey())) {
                    loadout.addAbility(unlockableAbility.getAbilityKey());
                    player.sendMessage(localizationManager.getLocalizedMessageAsComponent(mcRPGPlayer, LocalizationKey.ABILITY_ADDED_TO_LOADOUT_MESSAGE));
                } else if (unlockableAbility instanceof SkillAbility skillAbility) {
                    Skill skill = McRPG.getInstance().registryAccess().registry(McRPGRegistryKey.SKILL).getRegisteredSkill(skillAbility.getSkillKey());
                    Component skillDisplayName = skill.getDisplayName(mcRPGPlayer);
                    String serializedSkillName = miniMessage.serialize(skillDisplayName);
                    player.sendMessage(localizationManager.getLocalizedMessageAsComponent(mcRPGPlayer, LocalizationKey.ABILITY_NOT_ADDED_DUPLICATE_SKILL_MESSAGE,
                            Map.of("skill", serializedSkillName, "ability", serializedAbilityName)));
                }
            }
        }
    }
}
