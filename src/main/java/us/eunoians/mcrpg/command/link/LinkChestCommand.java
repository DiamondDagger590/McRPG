package us.eunoians.mcrpg.command.link;

import com.diamonddagger590.mccore.player.PlayerManager;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.incendo.cloud.CommandManager;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.AbilityData;
import us.eunoians.mcrpg.ability.attribute.AbilityLocationAttribute;
import us.eunoians.mcrpg.ability.impl.mining.RemoteTransfer;
import us.eunoians.mcrpg.api.event.fake.FakeChestOpenEvent;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;

public class LinkChestCommand {

    public static void registerCommand() {
        CommandManager<CommandSourceStack> commandManager = McRPG.getInstance().getCommandManager();
        MiniMessage miniMessage = McRPG.getInstance().getMiniMessage();
        commandManager.command(commandManager.commandBuilder("mcrpg")
                .literal("link")
                .handler(commandContext -> {
                    CommandSender commandSender = commandContext.sender().getSender();
                    if (commandSender instanceof Player player) {
                        Audience audience = McRPG.getInstance().getAdventure().player(player);
                        PlayerManager playerManager = McRPG.getInstance().getPlayerManager();
                        playerManager.getPlayer(player.getUniqueId()).ifPresent(corePlayer -> {
                            if (corePlayer instanceof McRPGPlayer mcRPGPlayer) {
                                RemoteTransfer remoteTransfer = (RemoteTransfer) McRPG.getInstance().getAbilityRegistry().getRegisteredAbility(RemoteTransfer.REMOTE_TRANSFER_KEY);
                                Block block = player.getTargetBlock(null, 100);
                                if (block.getType() != Material.CHEST) {
                                    audience.sendMessage(miniMessage.deserialize("<red>You must be looking at a chest in order to link Remote Transfer."));
                                    return;
                                }
                                else if (!remoteTransfer.isAbilityEnabled()) {
                                    audience.sendMessage(miniMessage.deserialize("<red>Remote Transfer is not currently enabled."));
                                    return;
                                }
                                FakeChestOpenEvent fakeChestOpenEvent = new FakeChestOpenEvent(player, block.getLocation());
                                Bukkit.getPluginManager().callEvent(fakeChestOpenEvent);
                                if (fakeChestOpenEvent.useInteractedBlock() == Event.Result.ALLOW) {
                                    AbilityHolder abilityHolder = mcRPGPlayer.asSkillHolder();
                                    var abilityDataOptional = abilityHolder.getAbilityData(remoteTransfer);
                                    if (abilityDataOptional.isPresent()) {
                                        AbilityData abilityData = abilityDataOptional.get();
                                        abilityData.addAttribute(new AbilityLocationAttribute(block.getLocation()));
                                        audience.sendMessage(miniMessage.deserialize("<gray>Successfully linked chest to your Remote Transfer ability"));
                                    }
                                }
                            }
                        });
                    }
                }));
    }
}
