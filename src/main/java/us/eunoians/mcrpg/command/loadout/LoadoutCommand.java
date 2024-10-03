package us.eunoians.mcrpg.command.loadout;

import com.diamonddagger590.mccore.player.PlayerManager;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.CommandManager;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.command.McRPGCommandBase;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.loadout.LoadoutSelectionGui;

public class LoadoutCommand extends McRPGCommandBase {

    public static void registerCommand() {
        CommandManager<CommandSourceStack> commandManager = McRPG.getInstance().getCommandManager().getCommandManager();
        MiniMessage miniMessage = McRPG.getInstance().getMiniMessage();
        commandManager.command(commandManager.commandBuilder("loadout")
                .handler(commandContext -> {
                    CommandSender commandSender = commandContext.sender().getSender();
                    if (commandSender instanceof Player player) {
                        Audience audience = McRPG.getInstance().getAdventure().player(player);
                        PlayerManager playerManager = McRPG.getInstance().getPlayerManager();
                        playerManager.getPlayer(player.getUniqueId()).ifPresent(corePlayer -> {
                            if (corePlayer instanceof McRPGPlayer mcRPGPlayer) {
                                LoadoutSelectionGui loadoutGui = new LoadoutSelectionGui(mcRPGPlayer);
                                McRPG.getInstance().getGuiTracker().trackPlayerGui(player, loadoutGui);
                                player.openInventory(loadoutGui.getInventory());
                            }
                        });
                    }
                }));
    }
}
