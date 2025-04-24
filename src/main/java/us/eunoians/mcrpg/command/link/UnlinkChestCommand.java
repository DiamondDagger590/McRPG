package us.eunoians.mcrpg.command.link;

import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.ManagerKey;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.CommandManager;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.AbilityData;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeRegistry;
import us.eunoians.mcrpg.ability.impl.mining.RemoteTransfer;
import us.eunoians.mcrpg.entity.McRPGPlayerManager;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

public class UnlinkChestCommand {

    public static void registerCommand() {
        CommandManager<CommandSourceStack> commandManager = McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(ManagerKey.COMMAND).getCommandManager();
        MiniMessage miniMessage = McRPG.getInstance().getMiniMessage();
        commandManager.command(commandManager.commandBuilder("mcrpg")
                .literal("unlink")
                .handler(commandContext -> {
                    CommandSender commandSender = commandContext.sender().getSender();
                    if (commandSender instanceof Player player) {
                        Audience audience = McRPG.getInstance().getAdventure().player(player);
                        McRPGPlayerManager playerManager = McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.PLAYER);
                        playerManager.getPlayer(player.getUniqueId()).ifPresent(mcRPGPlayer -> {
                            RemoteTransfer remoteTransfer = (RemoteTransfer) McRPG.getInstance().registryAccess().registry(McRPGRegistryKey.ABILITY).getRegisteredAbility(RemoteTransfer.REMOTE_TRANSFER_KEY);
                            AbilityHolder abilityHolder = mcRPGPlayer.asSkillHolder();
                            var abilityDataOptional = abilityHolder.getAbilityData(remoteTransfer);
                            if (abilityDataOptional.isPresent()) {
                                AbilityData abilityData = abilityDataOptional.get();
                                var abilityAttributeOptional = abilityData.getAbilityAttribute(AbilityAttributeRegistry.ABILITY_LOCATION_ATTRIBUTE);
                                if (abilityAttributeOptional.isPresent()) {
                                    abilityData.removeAttribute(abilityAttributeOptional.get());
                                    audience.sendMessage(miniMessage.deserialize("<gray>You have unlinked your chest."));
                                }
                                else {
                                    audience.sendMessage(miniMessage.deserialize("<gray>You don't have a linked chest."));
                                }
                            }
                        });
                    }
                }));
    }
}
