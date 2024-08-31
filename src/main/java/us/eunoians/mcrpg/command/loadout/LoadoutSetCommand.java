package us.eunoians.mcrpg.command.loadout;

import com.diamonddagger590.mccore.player.PlayerManager;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.key.CloudKey;
import org.incendo.cloud.minecraft.extras.RichDescription;
import org.incendo.cloud.parser.standard.IntegerParser;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.command.McRPGCommandBase;
import us.eunoians.mcrpg.entity.holder.LoadoutHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;

/**
 * This command sets the player's current {@link us.eunoians.mcrpg.loadout.Loadout} to
 * the slot provided.
 */
public class LoadoutSetCommand extends McRPGCommandBase {

    public static void registerCommand() {
        CommandManager<CommandSourceStack> commandManager = McRPG.getInstance().getCommandManager();
        MiniMessage miniMessage = McRPG.getInstance().getMiniMessage();
        commandManager.command(commandManager.commandBuilder("loadout")
                .literal("set")
                .required("slot", IntegerParser.integerParser(1), RichDescription.richDescription(miniMessage.deserialize("<gray>The loadout to set.")))

                .handler(commandContext -> {
                    CommandSender commandSender = commandContext.sender().getSender();
                    CloudKey<Integer> amountKey = CloudKey.of("slot", Integer.class);
                    int loadoutSlot = commandContext.get(amountKey);
                    if (commandSender instanceof Player player) {
                        Audience audience = McRPG.getInstance().getAdventure().player(player);
                        PlayerManager playerManager = McRPG.getInstance().getPlayerManager();
                        playerManager.getPlayer(player.getUniqueId()).ifPresent(corePlayer -> {
                            if (corePlayer instanceof McRPGPlayer mcRPGPlayer) {
                                LoadoutHolder loadoutHolder = mcRPGPlayer.asSkillHolder();
                                if (!loadoutHolder.hasLoadout(loadoutSlot)) {
                                    audience.sendMessage(miniMessage.deserialize("<red>You do not have a loadout slot with that id."));
                                    return;
                                }
                                loadoutHolder.setCurrentLoadoutSlot(loadoutSlot);
                                audience.sendMessage(miniMessage.deserialize("<gray>Your active loadout is now <gold>loadout #" + loadoutSlot + "</gold>.</gray>"));
                            }
                        });
                    }
                }));
    }
}
